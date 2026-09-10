package de.heuboe.datex2.vms.status.pub;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import de.heuboe.datex2.exception.D2Exception;
import de.heuboe.datex2.vms.service.D2VMSUnitMessage;
import de.heuboe.datex2.vms.service.ObjectKey;
import de.heuboe.datex2.vms.service.VMSDataListener;
import de.heuboe.datex2.vms.service.VMSPublicationMode;
import de.heuboe.datex2.vms.service.VMSService;
import de.heuboe.datex2.vms.status.pub.check.UpdateObserver;
import de.heuboe.datex2.vms.status.pub.config.Properties;
import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;
import de.heuboe.util.DirectoryCleaner;
import eu.datex2.schema._2._2_0.D2LogicalModel;

/**
 * 
 * Manages operating
 * 
 * @author peters
 *
 *
 * Implements process logic
 *
 */
public class Manager implements VMSDataListener
{
	private static final Logger LOGGER = Logger.getLogger( Manager.class );
	
	private String listenerAddress; 
	private String listenerAddressExt; 
	
	private Properties properties;
	private VMSPublicationMode publicationMode;
	private int pubInterval;
	private long checkInterval;
	private String fileDir;
	
	private String tableVersion = "1";
	
	private Publisher publisher; 
	private Reader reader;
	
	@Autowired
	private Sender sender;
	
	private VMSService vmsService;
	private DataListener dataListener; 								// NOSONAR
	private Set<ObjectKey> updatedObjects = new HashSet<>();
	
	private UpdateObserver updateObserver = null;
	
	private DirectoryCleaner dc; 
	
	private Date lastSnapshot = null;
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param 		properties		Configuration properties
	 * @exception 	VMSException  	Error
	 * 
	 */
	public Manager( Properties properties )
			throws VMSException
	{
		this.properties = properties;
		
		String thisHost = properties.getVmsServiceListenerHost();
		if( ( thisHost == null ) || thisHost.isEmpty() ) {
			thisHost = Util.getThisHostNamne();
		}
		
		listenerAddress = "http://" +  
						  thisHost +
						  ":" + properties.getVmsServiceListenerPort() +  
						  "/Datex2/VMSPublication/DataListener/" + 
						  properties.getPubInstance();
		
		listenerAddressExt = listenerAddress;
		int listenerPortExt = properties.getVmsServiceListenerPortExt();
		if( listenerPortExt != 0 ) {
			listenerAddressExt = "http://" +  
								properties.getVmsServiceListenerHostExt() +
						     	":" + listenerPortExt +  
						     	"/Datex2/VMSPublication/DataListener/" + 
						     	properties.getPubInstance();
		}
		
		this.fileDir = properties.getFileDir();
		
		File dir = new File( fileDir );
		if( !dir.exists() )
		{
			if( !dir.mkdirs() )   // NOSONAR
			{
				throw new VMSException( VMSException.ERROR_IO, 
										"Cannot create fileDir <" + fileDir + "> !" );
			}
		}
		
		if( !dir.isDirectory() )
		{
			throw new VMSException( VMSException.ERROR_IO, 
									"fileDir <" + fileDir + "> is no directory !" );
		}
		
		try
		{
			dc = new DirectoryCleaner( fileDir );
			dc.start();
		} catch( IOException ex ) {
			throw new VMSException( VMSException.ERROR_IO, 
								    "Exception on creating DirectoryCleaner !", ex  );
			
		}
		
		publicationMode = properties.getPubMode();
		pubInterval = properties.getPubInterval();
		checkInterval = properties.getCheckInterval();
		if( ( publicationMode == VMSPublicationMode.CyclicComplete ) || 
		    ( publicationMode == VMSPublicationMode.CyclicOnChangeComplete ) ) {
			checkInterval = pubInterval * 1000L;
		}
		
		this.publisher = new Publisher( properties );
		
		connect2VMSService();		
		
		this.reader = new Reader( vmsService, 
				  				  properties.getTableId(),
				  				  properties.getTableVersion() );
		reader.init();
		
		tableVersion = reader.getTableVersion();

		publisher.init();
		
		start();
	}
	
	private void connect2VMSService()
			throws VMSException
	{
		try
		{
			JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
			factory.setServiceClass( VMSService.class );
			factory.setWsdlLocation( "wsdl/VMSService.wsdl" );
			factory.setAddress( properties.getVmsServiceUrl() );
			
			vmsService = (VMSService)factory.create();

	        Client client = ClientProxy.getClient( vmsService );
	        
	        if (client != null) 
	        {
				HTTPConduit conduit = (HTTPConduit) client.getConduit();
				HTTPClientPolicy policy = new HTTPClientPolicy();
				policy.setConnectionTimeout( properties.getConnTimeout() );
				policy.setReceiveTimeout( properties.getConnTimeout() );
				conduit.setClient(policy);
	        }
			vmsService.ping( "" );
		} catch( Throwable ex ) {   // NOSONAR
			LOGGER.fatal( "Failed to connect to VMSPublicationService" );
			LOGGER.fatal( ex.toString() );
			LOGGER.fatal( CallStack.getStackTraceAsString( ex ) );
			
			throw new VMSException( VMSException.ERROR_VMS_SVC, 
					                "Cannot connect to VMSPublicationService", 
					                ex );
		}
		
	}
	
	/**
	 * 
	 * Starts data update listening
	 * 
	 * @throws VMSException   Error
	 */
	public void start() throws VMSException {
		
		try
		{
			dataListener = new DataListener( properties, listenerAddress, listenerAddressExt, this );
			dataListener.start( vmsService ); 
		} catch( Throwable ex ) {    // NOSONAR
			LOGGER.fatal( "Failed to connect to VMSPublicationService" );
			LOGGER.fatal( ex.toString() );
			LOGGER.fatal( CallStack.getStackTraceAsString( ex ) );
			
			throw new VMSException( VMSException.ERROR_VMS_SVC, 
					                "Cannot connect to VMSPublicationService", 
					                ex );
		}
	}
	
	
	/**
	 * 
	 * Waits until next import time
	 * 
	 */
	public void waitForNextCycle()
	{
		long now = (new Date()).getTime();
		long next = now / checkInterval * checkInterval + checkInterval;
		
		
		if( ( publicationMode == VMSPublicationMode.CyclicComplete ) ||
		    ( publicationMode == VMSPublicationMode.CyclicOnChangeComplete ) ) {
			LOGGER.info( "Waiting for next period ... ( until " + (new Date(next)).toString() + " )" );
		} else {
			LOGGER.trace( "Waiting for next period ... ( until " + (new Date(next)).toString() + " )" );
		}

		Date nextDate = new Date( next );
		while( (new Date()).before( nextDate ) )
		{
			Util.sleep( 100 );
		}
	}
	
	
	private void writePublication( Date now, boolean snapshot, Set<ObjectKey> updated )
			throws VMSException
	{
		LOGGER.info( "" );
		LOGGER.info( "Create publication" );
		LOGGER.info( "" );
		List<D2VMSUnitMessage> ums = reader.getVMSMessages( updated, snapshot );
		
		if( updateObserver != null )
		{
			updateObserver.notifyUpdate( ums, snapshot);
		}
		
		D2LogicalModel d2lm = publisher.createPublication( tableVersion, ums, snapshot, reader );
		
		if( d2lm != null )
		{
			String pubString = publisher.toString( d2lm );
			
			String fileName = fileDir + 
							  "/D2VMSPub_" + 
							  properties.getTableId() + "_" + 
							  tableVersion + 
							  "_" + now.getTime() + 
							  ".xml";
			
			try
			{
				LOGGER.info( "Write publication to <" + fileName +  ">" );
				FileUtils.write( new File(fileName), pubString );
			} catch( IOException ex ) {
				try
				{
					sender.send( now, pubInterval, ex.toString(), null, null );
				} catch( D2Exception dex ) {
					LOGGER.fatal( "Error sending publication !");
					LOGGER.fatal( dex.toString() );
					LOGGER.fatal( CallStack.getStackTraceAsString( dex ) );
					
					Util.exit(-1);
				}
				
				throw new VMSException( VMSException.ERROR_IO, ex.toString(), ex);
			}
			
			try
			{
				LOGGER.info( "Send publication ..." );
				
				sender.send( now, pubInterval, "", d2lm, fileName );
			} catch( D2Exception dex ) {
				LOGGER.fatal( "Error sending publication !");
				LOGGER.fatal( dex.toString() );
				LOGGER.fatal( CallStack.getStackTraceAsString( dex ) );
				
				Util.exit(-1);
			}
		}
		
		if( snapshot && ( d2lm != null ) ) {
			lastSnapshot = now;
		}
	}

	
	/**
	 * 
	 * Import loop
	 * 
	 * @throws VMSException   Error
	 */
	public void loop() 
	          throws VMSException
	{
	    boolean once = false;
	    String onceStr = System.getProperty( Util.TEST_MODE_FLAG );
	    if( onceStr != null )
	    {
	        once = true;
	    }
	    
	    boolean updatePubMode = ( publicationMode == VMSPublicationMode.OnUpdateCyclicSnapshot ) ||
	    		                ( publicationMode == VMSPublicationMode.OnUpdate );

		while( true )
		{
			waitForNextCycle();
			
			long n = (new Date()).getTime() / checkInterval * checkInterval;
			Date now = new Date( n );
			
			boolean snapshot = true;
			boolean enforced = ( lastSnapshot == null ); 
			
			if( publicationMode == VMSPublicationMode.OnUpdateCyclicSnapshot )
			{
				if( lastSnapshot != null )  // NOSONAR
				{
					long t = lastSnapshot.getTime();
					if( n < t + pubInterval * 1000L ) {
						snapshot = false; 
						enforced = false;
					} else {
						enforced = true;
					}
						
				}
			}
			
			Set<ObjectKey> updated = getUpdated(); 
			
			if( updatePubMode && !enforced && ( ( updated == null ) || updated.isEmpty() ) )
			{
				LOGGER.trace( "" );
				LOGGER.trace( "No data change: no publication created." );
				LOGGER.trace( "" );
			} else {
				writePublication( now, snapshot, updated );
			}
			
			if( once )
			{
			    return;
			}
		}
	}

	@Override
	public void ping( String clientId ) { /** Succeeds if caller gets no exception  **/ }
	
	@Override
	public void error( String reason )
	{
		LOGGER.fatal( "Process is terminated !" );
		Util.exit( -1 );
	}

	@Override
	public synchronized void notifyChange( List<ObjectKey> objIds)
	{
		LOGGER.debug( "" );
		for( ObjectKey objId : objIds ) {
			LOGGER.debug( "Update for " + objId.getType() + " <" + objId.getId() + ">" );
		}
		LOGGER.debug( "" );
		updatedObjects.addAll( objIds );
	}
	
	private synchronized Set<ObjectKey> getUpdated()
	{
		Set<ObjectKey> updated  = new HashSet<>();
		updated.addAll( updatedObjects );
		updatedObjects.clear();
		
		return updated;
	}
	
	/**
	 * 
	 * Checks if updates have arrived
	 * 
	 * @return		true: updates have arrived
	 */
	public synchronized boolean changed()
	{
		return !updatedObjects.isEmpty();
	}
	
	public void setUpdateObserver( UpdateObserver updateObserver )
	{
		this.updateObserver = updateObserver;
	}
	
	public Reader getReader()
	{
		return reader;
	}
}
