package de.heuboe.datex2.vms.service.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import de.heuboe.datex2.vms.service.VMSServiceException;
import de.heuboe.datex2.vms.service.datamsg.GeoDataMsgSvcClient;
import de.heuboe.datex2.vms.service.db.Persistence;
import de.heuboe.datex2.vms.service.kafka.WZGDeFehlerReceiver;
import de.heuboe.datex2.vms.service.kafka.WZGStellzustandReceiver;
import de.heuboe.datex2.vms.service.uz.ConfigManager;
import de.heuboe.datex2.vms.service.uz.ConfigManagerIS;
import de.heuboe.datex2.vms.service.uz.ConfigManagerSBA;
import de.heuboe.datex2.vms.service.uz.ConfigManagerSBANonSite;
import de.heuboe.datex2.vms.service.uz.ConfigService;
import de.heuboe.datex2.vms.service.uz.ConfigServiceNRW;
import de.heuboe.datex2.vms.service.uz.ConfigServiceSBANonSite;
import de.heuboe.datex2.vms.service.uz.DataManager;
import de.heuboe.datex2.vms.service.uz.ImageManager;
import de.heuboe.datex2.vms.service.uz.ImageServiceFactory;
import de.heuboe.datex2.vms.service.uz.ImageServiceFactory.ImageServiceType;
import de.heuboe.datex2.vms.service.uz.LocationManager;
import de.heuboe.datex2.vms.service.uz.LocationService;
import de.heuboe.datex2.vms.service.uz.LocationServiceAQ;
import de.heuboe.datex2.vms.service.uz.SiteConfigService;
import de.heuboe.datex2.vms.service.uz.SiteConfigServiceGrpc;
import de.heuboe.datex2.vms.service.uz.SiteConfigServiceSoap;
import de.heuboe.datex2.vms.service.uz.UzService;
import de.heuboe.ims.base.VmsImageService;
import de.heuboe.ims.base.VmsImageServiceIms;
import de.heuboe.kafka.listener.KafkaException;
import de.heuboe.kafka.listener.KafkaReceiverSubscription;
import de.heuboe.log.Logger;
import de.heuboe.nrw.guisvc.sitecfg.data.SiteConfigError;
import de.heuboe.nrw.ims.DataReceiver;
import de.heuboe.nrw.ims.SiteConfigServiceIface;
import eu.vmis_ehe.vmis2.tls.received.pojo.PWZGDeFehler;
import eu.vmis_ehe.vmis2.tls.received.pojo.PWZGStellzustandList;


/**
 * 
 * Spring configuration
 * 
 * @author peters
 *
 */
@Configuration
@EnableAutoConfiguration
@EnableMongoRepositories(basePackages = "de.heuboe.datex2.vms.service.db")
@ComponentScan( basePackages = { "de.heuboe.datex2.vms.service.config", 
		                         "de.heuboe.datex2.vms.service.db", 
		                         "de.heuboe.kafka.listener",
		                         "de.heuboe.datex2.vms.service.kafka" } )               
public class Config {
	
	private static final Logger LOGGER = Logger.getLogger( Config.class );
	
	@Bean
	Persistence persistence() throws VMSServiceException {
		return new Persistence();
	}
	
	@Bean
	LocationManager locationManager( Properties properties ) throws VMSServiceException {
		String locSrvUrl = properties.getWlsUrl();
		if( ( locSrvUrl != null ) && !locSrvUrl.isEmpty() ) {
			
			String aqLocationType = properties.getAqLocationType();
			
			if(  ( aqLocationType != null ) && !aqLocationType.isEmpty() ) {
				
				if( aqLocationType.equals( "AQ" ) ) {
					LocationService locationService = new LocationServiceAQ( properties.getWlsSRID(), properties.isUseCarriagewayAttr()  );
					locationService.connect( locSrvUrl ); 
					return  locationService;
				} else {
					String error = "Unknown aqLocationType <" + aqLocationType + ">!";
					LOGGER.error( error );
					throw new VMSServiceException( error );
				}
			} else {
				LocationService locationService = new LocationService( properties.getWlsSRID(), properties.isUseCarriagewayAttr() );
				locationService.connect( locSrvUrl ); 
				return  locationService;
			}
		} else {
			return new LocationServiceAQ( properties.getWlsSRID(), properties.isUseCarriagewayAttr() );
		}
	}
	
	
	@Bean
	ImageServiceFactory imageServiceFactory( Properties properties ) {
		return new ImageServiceFactory( properties.getImageServiceType(),
										properties.getMessageBrokerAddresses(), 
										properties.getImageUrlPrefixHost(),
										properties.getImageUrlPrefixPort(),
										properties.getImageUrlPrefix(),
										properties.getZoomFactor(),
										properties.getAqIds() );
	}

	@Bean
	ImageManager imageManager( SiteConfigServiceIface siteConfigServiceIface, 
			                   ImageServiceFactory imageServiceFactory, 
			                   DataReceiver dataReceiver ) throws VMSServiceException {
		return new ImageManager( imageServiceFactory.createImageService( siteConfigServiceIface, dataReceiver ),
								 imageServiceFactory.getZoomFactor() );
	}
	
	@Bean(name = "MAIN")
	ConfigManager configManager( Properties properties, 
			                     SiteConfigServiceIface siteConfigServiceImpl, 
			                     @Autowired(required = false) ConfigManagerSBANonSite configManagerSBA, 
			                     ImageManager imageManager ) throws VMSServiceException {
		
		ImageServiceType ist = ImageServiceType.valueOf( properties.getImageServiceType() );
		if( ist == ImageServiceType.SDBBY ) {
			VmsImageService is = imageManager.getImageService();
			if( is instanceof VmsImageServiceIms ) {
				return new ConfigManagerIS( ((VmsImageServiceIms)is).getImageService() );
			} else {
				throw new VMSServiceException( "Internal error: SDBBY mode, but no appropriate ImageService" );
			}
		}
		
		String cfgServiceType = properties.getCfgServiceType();
		if(  ( cfgServiceType != null ) && !cfgServiceType.isEmpty() ) {
			
			if( cfgServiceType.equals( "NRW-CFG" ) ) {
				ConfigServiceNRW cfgService = new ConfigServiceNRW();
				cfgService.connect( properties.getCfgServiceUrl() );
				return cfgService;
			} else if( cfgServiceType.equals( "NRW" ) ) {
				
				SiteConfigService siteCfgSvc = new SiteConfigService( siteConfigServiceImpl );
				if( configManagerSBA != null ) {
					return new ConfigManagerSBA( siteCfgSvc, configManagerSBA );
				} else {
					return siteCfgSvc;
				}
			} else {
				String error = "Unknown cfgServiceType <" + cfgServiceType + ">!";
				LOGGER.error( error );
				throw new VMSServiceException( error );
			}
		} else {
			ConfigService cfgService = new ConfigService();
			cfgService.connect( properties.getCfgServiceUrl() );
			return cfgService;
		}
		
	}
	
	@Bean
	DataManager dataManager( Properties properties, 
			                 ImageManager imageManager, 
			                 @Autowired(required = false) GeoDataMsgSvcClient geoServerDataMsgClient ) {
		return new DataManager( imageManager,
				                geoServerDataMsgClient,
				                properties.getUpdateInterval(),
				                properties.getConnTimeout(),
				                properties.getFullUpdateInterval(),
				                properties.getExtFilePathPrefix() );
	}

	@Bean(initMethod = "connect")
	SiteConfigServiceIface siteConfigService( Properties properties ) {
		
		String type = properties.getSiteCfgSvcType();
		
		SiteConfigServiceIface siteConfigService;
		if( type.equals( "SOAP" ) ) {
			siteConfigService = new SiteConfigServiceSoap( properties.getCfgServiceUrl() );
		} else {
			siteConfigService = new SiteConfigServiceGrpc( properties.isUseAggregatedAqs() );
		}
		
		return siteConfigService;
	}
	
	
	@Bean(initMethod = "init")
	ConfigServiceSBANonSite configServiceSBA( Properties properties ) {
		return new ConfigServiceSBANonSite( properties.getSbaAqTypes() );
	}

	
	@Bean
	ConfigManagerSBANonSite configManagerSBA( Properties properties, 
			                           SiteConfigServiceIface siteCfgSvc, 
			                           ConfigServiceSBANonSite configService ) throws SiteConfigError {
		
		List<String> sbaSites = properties.getSbaSites();
		List<String> siteAqs = new ArrayList<>();
		for( String sbaSite : sbaSites ) {
			siteCfgSvc.getSiteAqs( sbaSite ).getQ().forEach( q -> siteAqs.add( q.getId() ) );
		}
		
		return new ConfigManagerSBANonSite( configService, siteAqs );
	}

	
	@Bean
	GeoDataMsgSvcClient geoServerDataMsgClient( Properties properties ) {
		
		String svcHost =  properties.getGeoDataMsgSvcHost();
		if( ( svcHost != null ) && !svcHost.isEmpty() ) {
			String address = UzService.getUrl( properties.getServiceHost(), properties.getServicePort() );
			GeoDataMsgSvcClient client = new GeoDataMsgSvcClient( svcHost, 
					                                              properties.getGeoDataMsgSvcPort(), 
					                                              address,
					                                              properties.getGeoDataMsgImageZoomFactor() );
			try {
				client.init();
				return client;
			} catch (Exception ex) {
				LOGGER.warn("Cannot connect to GeoDataMsgSvc: " + ex.toString() );
			}
		}
		
		return null;
	}
	
	@Bean
	WZGStellzustandReceiver stellzustandReceiver( Properties properties, WZGDeFehlerReceiver errorReceiver ) throws KafkaException {
		
		KafkaReceiverSubscription rs = new KafkaReceiverSubscription( properties.getConsumerGroupId(), 
																	  properties.getTopicStellzustand(),
				                                                      PWZGStellzustandList.class.getName(), 
				                                                      null );

		
		return new WZGStellzustandReceiver( rs, errorReceiver );
	}
	
	@Bean
	WZGDeFehlerReceiver fehlerReceiver( Properties properties ) throws KafkaException {
		
		KafkaReceiverSubscription rs = new KafkaReceiverSubscription( properties.getConsumerGroupId(), 
																	  properties.getTopicFehler(),
				                                                      PWZGDeFehler.class.getName(), 
				                                                      null );

		
		return new WZGDeFehlerReceiver( rs );
	}

}

