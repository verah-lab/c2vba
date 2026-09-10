package de.heuboe.datex2.vms.service.uz;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.heuboe.datex2.vms.service.ObjectKey;
import de.heuboe.datex2.vms.service.VMSServiceException;
import de.heuboe.ims.base.DataObserver;
import de.heuboe.ims.base.ImsException;
import de.heuboe.ims.base.VmsImageService;
import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;
import de.heuboe.zst.ims.data.ImageInfo;
import de.heuboe.zst.ims.iface.ImageService.AttributeType;


public class ImageManager 
{
	private static final Logger LOGGER = Logger.getLogger( ImageManager.class );
	private static final SimpleDateFormat TIME_SDF = new SimpleDateFormat( "HH:mm:ss");
	public static final int CLIENT_ID = 1;
	
	private VmsImageService imageService = null;
	private double zoomFactor = 1.0;
	
	private Set<ObjectKey> objectKeys = new HashSet<>();
	private Set<ObjectKey> newObjectKeys = new HashSet<>();
	
	public ImageManager( VmsImageService imageService, double zoomFactor ) {
		
		this.imageService = imageService;
		this.zoomFactor = zoomFactor;
	}

	public void start() {
		imageService.start();
	}
	
	public void add2AllObjectKeys( Set<ObjectKey> oks )
	{
		for( ObjectKey ok : oks )
		{
			if( !imageService.isInfraObject( ok.getType(),  ok.getId() ) ) {
				LOGGER.warn("Unknown object key: " + ok.getType() + "/" + ok.getId() );
			}
			
			if( !objectKeys.contains( ok ) )
				newObjectKeys.add( ok );
			
			objectKeys.add( ok );
		}
	}
	
	public ImageInfo getWzgImage( Date since, String wzgId )
			throws VMSServiceException 
	{
		try
		{
			Collection<ImageInfo> iis = imageService.getWzgImages( CLIENT_ID,
																   Arrays.asList( wzgId ), 
											  					   since,
											  					   zoomFactor );
			
			if( ( iis == null ) || iis.isEmpty() )
				return null;
			
			return iis.iterator().next();
		}
		catch( ImsException ex )
		{
			throw new VMSServiceException( VMSServiceException.VMS_SVC_ERR_IMAGE,
										"Error retrieving image of WZG <" + wzgId + ">",
										ex );
		}
	}
	
	public ImageInfo getAqImage( Date since, String aqId )
			throws VMSServiceException 
	{
		try
		{
			Collection<ImageInfo> iis = imageService.getAqImages( CLIENT_ID,
																  Arrays.asList( aqId ), 
											  					  since,
											  					  zoomFactor );
			
			
			if( ( iis == null ) || iis.isEmpty() )
				return null;
			
			return iis.iterator().next();
		}
		catch( ImsException ex )
		{
			throw new VMSServiceException( VMSServiceException.VMS_SVC_ERR_IMAGE,
										   "Error retrieving image of AQ <" + aqId + ">",
										   ex );
		}
	}
	
	public Map<ObjectKey,ImageInfo> getImageInfoUpdates( Date lastQueryDate,
			                                             Set<String> notifiedAqIds, 
			                                             Set<String> notifiedWzgIds )
			throws VMSServiceException
	{
		try
		{
			Map<ObjectKey,ImageInfo> imageInfos = new HashMap<>();
			
			{
				List<String> aqIds = new ArrayList<>();
				for( ObjectKey ok : newObjectKeys )
				{
					if( ok.getType().equals( ObjectKey.TYPE_AQ ) )
						aqIds.add( ok.getId() );
				}
				
				if( !aqIds.isEmpty() )
				{
					List<ImageInfo> iis = imageService.getAqImages( CLIENT_ID,
																	aqIds, 
										      						null,
												  					zoomFactor );
					
					for( ImageInfo ii : iis )
					{
						ObjectKey ok = new ObjectKey( ObjectKey.TYPE_AQ, ii.getId() );
						imageInfos.put( ok, ii );
					}
				}
			}
			
			
			{
				List<String> aqIds = new ArrayList<>();
				for( ObjectKey ok : objectKeys )
				{
					if( 
						ok.getType().equals( ObjectKey.TYPE_AQ ) 
						&& 
						!newObjectKeys.contains( ok ) 
						&& 
						( ( notifiedAqIds == null ) || ( notifiedAqIds.contains( ok.getId() ) ) )
					   )
						aqIds.add( ok.getId() );
				}
				
				if( !aqIds.isEmpty() )
				{
					try
					{
						List<ImageInfo> iis = imageService.getAqImages( CLIENT_ID,
																		aqIds, 
											      						lastQueryDate,
													  					zoomFactor );
					
						for( ImageInfo ii : iis )
						{
							ObjectKey ok = new ObjectKey( ObjectKey.TYPE_AQ, ii.getId() );
							imageInfos.put( ok, ii );
						}
					}
					catch( Throwable ex )
					{
						throw new VMSServiceException( VMSServiceException.VMS_SVC_ERR_IMAGE,
													   "Error retrieving images !",
													   ex );
					}
					
				}
			}
			
			
			{
				List<String> wzgIds = new ArrayList<>();
				for( ObjectKey ok : newObjectKeys )
				{
					if( ok.getType().equals( ObjectKey.TYPE_WZG ) )
						wzgIds.add( ok.getId() );
				}

				
				if( !wzgIds.isEmpty() )
				{
					List<ImageInfo> iis = imageService.getWzgImages( CLIENT_ID,
																	 wzgIds, 
										      						 null,
												  					 zoomFactor );
					
					for( ImageInfo ii : iis )
					{
						ObjectKey ok = new ObjectKey( ObjectKey.TYPE_WZG, ii.getId() );
						imageInfos.put( ok, ii );
					}
				}
			}
			
			{
				List<String> wzgIds = new ArrayList<>();
				for( ObjectKey ok : objectKeys )
				{
					if( 
						ok.getType().equals( ObjectKey.TYPE_WZG ) 
						&& 
						!newObjectKeys.contains( ok ) 
						&& 
						( ( notifiedWzgIds == null ) || ( notifiedWzgIds.contains( ok.getId() ) ) )
					  )
						wzgIds.add( ok.getId() );
				}
				
				if( wzgIds.size() > 0 )
				{
					List<ImageInfo> iis = imageService.getWzgImages( CLIENT_ID,
																	 wzgIds, 
										      						 lastQueryDate,
												  					 zoomFactor );
					
					for( ImageInfo ii : iis )
					{
						ObjectKey ok = new ObjectKey( ObjectKey.TYPE_WZG, ii.getId() );
						imageInfos.put( ok, ii );
					}
				}
			}
			
			newObjectKeys.clear();
			
			LOGGER.debug( "" );
			if( lastQueryDate == null )
			{
				LOGGER.debug( "LastQueryDate: null" );
			}
			else	
			{
				LOGGER.debug( "LastQueryDate: " + TIME_SDF.format( lastQueryDate ) );
			}
			LOGGER.debug( "# ImageInfo:   " +  imageInfos.size() );
			Iterator<ImageInfo> iii = imageInfos.values().iterator();
			while( iii.hasNext() )
			{
				ImageInfo ii = iii.next();
				LOGGER.debug( "ID:     		  " + ii.getId() );
				LOGGER.debug( "Timestamp:     " + TIME_SDF.format( ii.getTimestamp() ) );
				LOGGER.debug( "Code:   		  " + ii.getAttribute(AttributeType.WZG_CODE.name()) );
			}
			LOGGER.debug( "" );
			
			return imageInfos;
		}
		catch( ImsException ex )
		{
			throw new VMSServiceException( VMSServiceException.VMS_SVC_ERR_IMAGE,
										   "Error retrieving images !",
										   ex );
		}
		catch( Throwable ex )
		{
			LOGGER.fatal( "Fatal error calling ImageService !" );
			LOGGER.fatal( ex.toString() );
			LOGGER.fatal( CallStack.getStackTraceAsString( ex ) );
			Util.exit( -1 );
			
			return null;
		}
	}
	
	
	public boolean imageUrlPrefixProvided() {
		return imageService.imageUrlPrefixProvided();
	}
	
	public boolean isDataNotifier() {
		return imageService.isDataNotifier();
	}

	/**
	 * 
	 * Registers DataObserver
	 * 
	 * @param dataObserver DataObserver
	 */
	public void registerDataObserver( DataObserver dataObserver ) {
		imageService.registerDataObserver(dataObserver);
	}
	
	public VmsImageService getImageService() {
		return imageService;
	}
}
