package de.heuboe.datex2.vms.service.uz;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.heuboe.datex2.vms.service.VMSServiceException;
import de.heuboe.ims.base.VmsImageService;
import de.heuboe.log.Logger;
import de.heuboe.nrw.ims.DataReceiver;
import de.heuboe.nrw.ims.ImageServiceNRW;
import de.heuboe.nrw.ims.SiteConfigServiceIface;

/**
 * 
 * Creates data-receiving/image service
 * 
 * @author peters
 *
 */
public class ImageServiceFactory
{
	/**
	 * 
	 * Image service types ( SDBBY not supported )
	 * 
	 * @author peters
	 *
	 */
	public enum ImageServiceType {
		SDBBY,
		NRW,
	}
	
	private static final Logger LOGGER = Logger.getLogger( ImageServiceFactory.class );
	
	private ImageServiceType imageServiceType;
	private String messageBrokerAddresses;  
	private String imageUrlPrefixHost;
	private int imageUrlPrefixPort;
	private String imageUrlPrefix;
	private double zoomFactor;
	private Set<String> aqIds;
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param imageServiceType			Image service type:
	 * @param messageBrokerAddresses	MQTT addresses	
	 * @param imageUrlPrefixHost		Host in image URLs
	 * @param imageUrlPrefixPort		Port in image URLs
	 * @param imageUrlPrefix			Complete prefix of image URLs: includes host and port
	 * @param zoomFactor				Zoom factor of images
	 * @param aqIds						Relevant AQ IDs
	 */
	public ImageServiceFactory( String imageServiceType,
								String messageBrokerAddresses,  
								String imageUrlPrefixHost,
								int imageUrlPrefixPort,
								String imageUrlPrefix,
								double zoomFactor,
								List<String> aqIds ) {
		
		this.imageServiceType = ImageServiceType.valueOf( imageServiceType );
		this.messageBrokerAddresses = messageBrokerAddresses;
		this.imageUrlPrefixHost = imageUrlPrefixHost;
		this.imageUrlPrefixPort = imageUrlPrefixPort;
		this.imageUrlPrefix = imageUrlPrefix;
		this.zoomFactor = zoomFactor;
		this.aqIds = new HashSet<>( aqIds );
	}
	
	
	/**
	 * 
	 * Creates image service
	 * 
	 * @param dataReceiver			Data receiver
	 * @return						Image service instance	
	 * @throws VMSServiceException	Error
	 */
	public VmsImageService createImageService( SiteConfigServiceIface siteConfigService, DataReceiver dataReceiver ) throws VMSServiceException {
		
		if( imageServiceType == ImageServiceType.NRW ) {
			return connectNRW( siteConfigService, dataReceiver );
		} else {
			throw new VMSServiceException( VMSServiceException.VMS_SVC_ERR_PARAM, "Unknown ImageServiceType <" + imageServiceType.name() + ">" );
		}
	}
	
	private VmsImageService connectNRW( SiteConfigServiceIface siteConfigService, DataReceiver dataReceiver ) throws VMSServiceException
	{
		LOGGER.info( "Connect to Data/ImageService ..." );
		try {
			if( dataReceiver != null ) {
				return new ImageServiceNRW( siteConfigService, 
						dataReceiver,  
						imageUrlPrefixHost,
						imageUrlPrefixPort,
						imageUrlPrefix,
						zoomFactor,
						aqIds );
			} else {
				return new ImageServiceNRW( siteConfigService, 
						messageBrokerAddresses,  
						imageUrlPrefixHost,
						imageUrlPrefixPort,
						imageUrlPrefix,
						zoomFactor,
						aqIds );
			}
		} catch( Exception ex ) {
			throw new VMSServiceException( VMSServiceException.VMS_SVC_ERR_IMAGE_SVC_INIT, 
					                       "Error intializing image service", ex );
		}
		
	}
	
	public double getZoomFactor() {
		return zoomFactor;
	}
	
}
