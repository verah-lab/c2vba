package de.heuboe.datex2.vms.service.uz;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import de.heuboe.datex2.vms.service.D2VMSError;
import de.heuboe.datex2.vms.service.D2VMSErrorType;
import de.heuboe.datex2.vms.service.D2VMSMessage;
import de.heuboe.datex2.vms.service.D2VMSPictogram;
import de.heuboe.datex2.vms.service.ObjectKey;
import de.heuboe.datex2.vms.service.OperationCodeType;
import de.heuboe.datex2.vms.service.VMSDataListener;
import de.heuboe.datex2.vms.service.VMSDataListener.Identity;
import de.heuboe.datex2.vms.service.VMSServiceException;
import de.heuboe.datex2.vms.service.datamsg.GeoDataMsgDataListener;
import de.heuboe.datex2.vms.service.datamsg.GeoDataMsgSvcClient;
import de.heuboe.datex2.vms.service.table.data.D2VMSTable;
import de.heuboe.ims.base.DataObserver;
import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;
import de.heuboe.zst.ims.data.ImageInfo;
import de.heuboe.zst.ims.iface.ImageService.AttributeType;

/**
 * 
 * Manages data 
 * 
 * @author peters
 *
 */
public class DataManager implements DataObserver
{
	/**
	 * 
	 * Data listener
	 * 
	 * @author peters
	 *
	 */
	public static class DataListener
	{
		public DataListener( D2VMSTable d2VMSTable,
							 Identity identity, VMSDataListener vmsDataListener )
		{
			this.d2VMSTable = d2VMSTable;
			this.identity = identity;
			this.vmsDataListener = vmsDataListener;
			this.objectKeys = d2VMSTable.getAllObjectKeys();
		}
		
		public DataListener( Identity identity, Set<ObjectKey> objectKeys )
		{
			this.identity = identity;
			this.objectKeys = objectKeys;
		}


		private D2VMSTable d2VMSTable = null;
		private Identity identity;
		private VMSDataListener vmsDataListener = null;
		
		protected Set<ObjectKey> objectKeys = new HashSet<>();
		private Map<ObjectKey,D2VMSMessage> currentMessages = new HashMap<>();

		public D2VMSTable getD2VMSTable()
		{
			return d2VMSTable;
		}

		public void setD2VMSTable( D2VMSTable d2VMSTable )
		{
			this.d2VMSTable = d2VMSTable;
		}

		public Identity getIdentity()
		{
			return identity;
		}

		public void setIdentity(Identity identity)
		{
			this.identity = identity;
		}

		public VMSDataListener getVmsDataListener()
		{
			return vmsDataListener;
		}

		public void setVmsDataListener(VMSDataListener vmsDataListener)
		{
			this.vmsDataListener = vmsDataListener;
		}

		public Set<ObjectKey> getObjectKeys()
		{
			return objectKeys;
		}

		public void setObjectKeys(Set<ObjectKey> objectKeys)
		{
			this.objectKeys = objectKeys;
		}
		
		/**
		 * 
		 * Listener are notified of data updates
		 * 
		 * @param msgs		Updates as D2VMSMessage
		 */
		public void notityUpdates( Map<ObjectKey,D2VMSMessage> msgs )
		{
			List<ObjectKey> soks = new ArrayList<>();
			for (Map.Entry<ObjectKey, D2VMSMessage> entry : msgs.entrySet() )
			{
				ObjectKey ok = entry.getKey();
				D2VMSMessage newMsg = entry.getValue();
				if( objectKeys.contains( ok ) )
				{
					D2VMSMessage msg = currentMessages.get( ok );
					if( ( msg == null ) || !msg.isEqual( newMsg ) )
					{
						soks.add( ok );
						currentMessages.put( ok, newMsg );
					}
				}
			}
			if( !soks.isEmpty() )
			{
				LOGGER.info( "VMSDataListener notityUpdates()" );
				LOGGER.info( ID + getIdentity().getName() );
				LOGGER.info( ADDRESS + getIdentity().getAddress() );
				vmsDataListener.notifyChange( soks );
			}
		}
	}
	
	class PingTask extends TimerTask
	{
		/**
		 * 
		 * TimerTask: periodically ping listeners
		 * 
		 */
		@Override
		public synchronized void run()
		{
			synchronized( DataManager.this ) {
				pingListeners();
			}
		}
	}
	
	
	class DataRetrievalTask extends TimerTask
	{
		/**
		 * 
		 * TimerTask: periodically ping listeners
		 * 
		 */
		@Override
		public void run()
		{
			synchronized( DataManager.this ) {
				try
				{
					readDataUpdates( null, null );
				} catch( VMSServiceException ex ) {
					LOGGER.fatal( "Error retrieving VMS data update !" );
					LOGGER.fatal( ex.toString() );
					LOGGER.fatal( CallStack.getStackTraceAsString( ex ) );
					Util.exit( -1 );
				}
			}
		}
	}
	
	private static final Logger LOGGER = Logger.getLogger( DataManager.class );
	private static final String ID = "ID:      ";
	private static final String ADDRESS = "Address: ";
	private static final String GEO_DATA_LISTENER_NAME = "GeoDataMsgDataListener";
	
	private int updateInterval;
	private int connTimeout;
	private int fullUpdateInterval;
	private String extFilePathPrefix;
	
	private Date lastFullUpdateTime = new Date();
	private Date lastUpdateTime = new Date();
	private Map<ObjectKey,D2VMSMessage> currentMessages = new HashMap<>();
	
	private Timer notificationTimer = null;
	private List<DataListener> dataListeners = new ArrayList<>();
	private Date lastPingTime = new Date();
	
	ImageManager imageManager = null;	
	GeoDataMsgSvcClient geoServerDataMsgClient = null;
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param imageManager				ImageManager	
	 * @param updateInterval			Update interval
	 * @param connTimeout				Connection timeout
	 * @param fullUpdateInterval		Full update interval
	 * @param extFilePathPrefix			Extension of image URLs		
	 */
	public DataManager( ImageManager imageManager, 
						GeoDataMsgSvcClient geoServerDataMsgClient,
						int updateInterval,
						int connTimeout,
						int fullUpdateInterval,
						String extFilePathPrefix )
	{
		this.imageManager = imageManager;
		this.geoServerDataMsgClient = geoServerDataMsgClient;
		this.updateInterval = updateInterval;
		this.connTimeout = connTimeout;
		this.fullUpdateInterval = fullUpdateInterval;
		this.extFilePathPrefix = extFilePathPrefix;
	}
	
	/**
	 * 
	 * Starts periodic ping of VMSDataListener
	 * 
	 * @return	true
	 */
	public void startPingTask()
	{
		Timer pingTimer = new Timer();
		PingTask pt = new PingTask();
		pingTimer.schedule( pt, 0, VMSDataListener.PING_INTERVAL_MS / 3 ); 
	}

	private void startTimer()
	{
		if( notificationTimer != null )
		{
			notificationTimer.cancel();
			notificationTimer.purge();
		}
		
		notificationTimer = new Timer();
		
		
		DataRetrievalTask drt = new DataRetrievalTask();
		
		if( imageManager.isDataNotifier() ) {
			notificationTimer.schedule( drt, 0 );
		} else {
			notificationTimer.schedule( drt, 
										0, 
										updateInterval ); 
		}
	}
	
	private VMSDataListener connect2Listener( String url )
			throws VMSServiceException
	{
		try
		{
			JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
			factory.setServiceClass( VMSDataListener.class );
			factory.setAddress( url );
			
			VMSDataListener dataListener = (VMSDataListener)factory.create();

	        Client client = ClientProxy.getClient( dataListener );
	        
	        if (client != null) 
	        {
				HTTPConduit conduit = (HTTPConduit) client.getConduit();
				HTTPClientPolicy policy = new HTTPClientPolicy();
				policy.setConnectionTimeout( connTimeout );
				policy.setReceiveTimeout( connTimeout );
				conduit.setClient(policy);
	        }
	        
	        return dataListener;
		}
		catch( Throwable ex )  // NOSONAR
		{
			LOGGER.fatal( "Failed to connect to data listener" );
			LOGGER.fatal( "URL: " + url );
			LOGGER.fatal( ex.toString() );
			LOGGER.fatal( CallStack.getStackTraceAsString( ex ) );
			
			throw new VMSServiceException( VMSServiceException.VMS_SVC_ERR_DATA_LISTENER, 
					                    "Cannot connect to data listener", ex );
		}
	}
	
	/**
	 * 
	 * Register VMSDataListener
	 * 
	 * @param d2VMSTable			D2VMSTable
	 * @param identity				Listener ID
	 * @return						Listener name
	 * @throws VMSServiceException	Error
	 */
	public synchronized String registerDataListener( D2VMSTable d2VMSTable,
									                 VMSDataListener.Identity identity )
			throws VMSServiceException
	{
		LOGGER.info( "WS: registerDataListener()" );
		LOGGER.info( "Client: " + identity.getName() );
		
		VMSDataListener vmsDataListener = connect2Listener( identity.getAddress() ); 
		vmsDataListener.ping( "VMSPublicationService" );
		
		String newAddress = identity.getAddress();
		Iterator<DataListener> it = dataListeners.iterator();
		while( it.hasNext() )
		{
			DataListener dl = it.next(); 
			if( dl.getIdentity().getAddress().equals( newAddress ) ) {
				it.remove();
			}
		}
		
		DataListener dataListener = new DataListener( d2VMSTable,
													  identity, vmsDataListener	);
		dataListeners.add( dataListener );
		
		Set<ObjectKey> objKeys = d2VMSTable.getAllDataObjectKeys();
		imageManager.add2AllObjectKeys( objKeys );
		
		startTimer();

		if( geoServerDataMsgClient != null ) {
			registerGeoDataMsgListener( identity.getAddress(), objKeys );
		}
		
		return identity.getName();
	}
	
	private void registerGeoDataMsgListener( String address, Set<ObjectKey> objKeys ) {
		
		Identity identity = new Identity( GEO_DATA_LISTENER_NAME, address );
		GeoDataMsgDataListener geoDataMsgListener = new GeoDataMsgDataListener( geoServerDataMsgClient, identity, objKeys );
		dataListeners.add( geoDataMsgListener );
	}
	
	private void pingListeners() {    // NOSONAR
		
		Date now = new Date();
		if( now.getTime() > lastPingTime.getTime() + VMSDataListener.PING_INTERVAL_MS )
		{
			Iterator<DataListener> iterator = dataListeners.iterator();
			while( iterator.hasNext() )
			{
				DataListener dataListener = iterator.next();
				if( dataListener.getVmsDataListener() != null ) {
					try
					{
						LOGGER.info( "VMSDataListener ping()" );
						LOGGER.info( ID + dataListener.getIdentity().getName()  );
						LOGGER.info( ADDRESS + dataListener.getIdentity().getAddress()  );
						dataListener.getVmsDataListener().ping( "VMSPublicationService" );
					} catch( Exception ex ) {
						LOGGER.error( "Exception on VMSDataListener ping !" );
						LOGGER.error( ID + dataListener.getIdentity().getName()  );
						LOGGER.error( ADDRESS + dataListener.getIdentity().getAddress()  );
		
						iterator.remove();
					}
				}
			}
			
			lastPingTime = now;
		}
	}
	
	private synchronized void notityUpdates( Map<ObjectKey,D2VMSMessage> msgs )
	{
		Iterator<DataListener> iterator = dataListeners.iterator();
		while( iterator.hasNext() )
		{
			DataListener dataListener = iterator.next();
			try
			{
				dataListener.notityUpdates( msgs );
			} catch( Exception ex ) {
				LOGGER.error( "Exception on VMSDataListener notityUpdates !" );
				LOGGER.error( ID + dataListener.getIdentity().getName()  );
				LOGGER.error( ADDRESS + dataListener.getIdentity().getAddress()  );
				LOGGER.error( CallStack.getStackTraceAsString( ex ) );
				iterator.remove();
			}
		}
	}
	
	/**
	 * 
	 * Retrieves messages
	 * 
	 * @param oks		Object keys
	 * @return			New messages
	 */
	public List<D2VMSMessage> getMessages( Set<ObjectKey> oks )
	{
		List<D2VMSMessage> messages = new ArrayList<>();
		
		for( ObjectKey ok : oks )
		{
			D2VMSMessage message = currentMessages.get( ok ); 
			if( message != null ) {
				messages.add( message );
			}
		}
		
		return messages;
	}
	
	private void readDataUpdates( Set<String> notifiedAqIds,      // NOSONAR
            					  Set<String> notifiedWzgIds )
			throws VMSServiceException
	{
		Date now = new Date();
		
		Date lastTime = lastUpdateTime;
		if( fullUpdateInterval > 0 )
		{
			if( ( now.getTime() - lastFullUpdateTime.getTime() ) / 1000L > fullUpdateInterval )  // NOSONAR
			{
				LOGGER.info( "Read complete data." );
				lastTime = null;
				lastFullUpdateTime = now;
			}
		}
		
		Map<ObjectKey,ImageInfo> updates = imageManager.getImageInfoUpdates( lastTime, notifiedAqIds, notifiedWzgIds );
		Map<ObjectKey,D2VMSMessage> updateMsgs = new HashMap<>();
		for (Map.Entry<ObjectKey, ImageInfo> entry : updates.entrySet()) 
		{
			ObjectKey ok = entry.getKey();
			ImageInfo ii = entry.getValue();
			
			Date lu = ii.getLastUpdate();
			if( lu.after( lastUpdateTime ) ) {
				lastUpdateTime = lu;
			}
			
			D2VMSMessage message = new D2VMSMessage();

			message.setObjType( ok.getType() );
			message.setObjId( ok.getId() );
			if( ok.getType().equals( ObjectKey.TYPE_WZG ) ) {
				message.setOperationCodeType( OperationCodeType.StandardTLS );
			}

			Date time = ii.getTimestamp();
			if( time != null ) {
				message.setTimeLastSet( time );
			}
			
			D2VMSPictogram pictogram = new D2VMSPictogram();
			
			String url = ii.getUrl();
			
			if( url != null ) {
				if( !imageManager.imageUrlPrefixProvided() ) {
					url = extFilePathPrefix + "/" + url;
				}
				pictogram.setImageFilePath( url  );
				message.addPictograms( pictogram );
			} else {
				D2VMSError error = new D2VMSError();
				error.setType( D2VMSErrorType.CommunicationsFailure );
				error.setDescription( "No data" );
				message.setError( error );
			}
			
			
			if( ok.getType().equals( ObjectKey.TYPE_WZG ) )
			{
				Optional<String> val = ii.getAttribute( AttributeType.WZG_CODE.name() );
				if( val.isPresent() ) {
					message.setOperationCode( val.get() );
				}
			}

			Optional<String> val = ii.getAttribute( AttributeType.DESCRIPTION.name() );
			
			if( val.isPresent() )
			{
				message.setText( val.get() );
				pictogram.setDescription( val.get() );
			}	
			
			val = ii.getAttribute( AttributeType.WWW_ROUTE.name() );			
			if( val.isPresent() ) {
				message.setReasonForSetting( val.get() );
			}
			
			val = ii.getAttribute( AttributeType.ERROR.name() );
			if( val.isPresent() )
			{
				D2VMSError error = new D2VMSError();
				error.setType( D2VMSErrorType.Other );
				error.setDescription( val.get() );
				
				Date lut = new Date();	
				D2VMSMessage curMessage = currentMessages.get( ok );
				if( curMessage != null )
				{
					D2VMSError curError = curMessage.getError();
					if( curError != null ) {
						lut = curError.getLastUpdateTime();
					}
				}
				
				error.setLastUpdateTime( lut );
				
				message.setError( error );
			}
			
			updateMsgs.put( ok,  message );
			currentMessages.put( ok, message );
		}		
		
		notityUpdates( updateMsgs );
	}

	@Override
	public synchronized void notifySwitchingDataUpdate( Set<String> aqIds, Set<String> wzgIds )
	{
		try
		{
			readDataUpdates( aqIds, wzgIds );
		} catch( VMSServiceException ex ) {
			LOGGER.fatal( "Error retrieving VMS data update !" );
			LOGGER.fatal( ex.toString() );
			LOGGER.fatal( CallStack.getStackTraceAsString( ex ) );
			Util.exit( -1 );
		}
	}

}
