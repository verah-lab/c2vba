package de.heuboe.by.c2vba.datex2.mst.localisation.server;


import javax.xml.bind.JAXBException;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import de.heuboe.by.c2vba.datex2.mst.localisation.server.kafka.KafkaManager;
import de.heuboe.datex2.wls.OpenLRLocalizer;
import de.heuboe.wls.data.wls.GetCapabilitiesRequest;
import de.heuboe.wls.iface.Fault;
import de.heuboe.wls.iface.WebLocationServer;
import de.heuboe.wls.util.WlsException;
import de.heuboe.wls.util.WlsWebServiceClient;


/**
 * 
 * Spring configuration
 * 
 * @author peters
 *
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan( basePackages = { "de.heuboe.by.c2vba.datex2.mst.localisation.server", 
		                         "de.heuboe.kafka.listener"} )               
public class Config {       
	
	@Bean
	WebLocationServer createWlsClient( Properties properties ) throws SvcException {
		
		try {
	        String url = properties.getWlsUrl();
	        WlsWebServiceClient wlsClient = new WlsWebServiceClient();
	        wlsClient.connect( url );
	        WebLocationServer wls = wlsClient.getWebLocationService();
			
			if( wls != null ) {
				wls.getCapabilities( new GetCapabilitiesRequest() );
 			}
			
			return wls;
		} catch ( WlsException | Fault ex ) {
			throw new SvcException( SvcException.ERROR_INIT, "Error in createWlsClient()", ex );
		}
	}
	
	
	@Bean
	OpenLRLocalizer openLRLocalizer( Properties properties, WebLocationServer wls ) throws JAXBException {
		return new OpenLRLocalizer( wls );
	}
	
	@Bean
	WlsClient wlsClient( Properties properties ) {
		return new WlsClient( properties.isAddOpenLRLocation() );
	}
	
	@Bean
	ConfigService configService() {
		return new ConfigService();
	}
	
	@Bean
	LocalisationServiceImpl locationService( Properties properties, WlsClient wlsClient, ConfigService configService ) {
		
		return new LocalisationServiceImpl( wlsClient, configService, properties.getSystem() ); 
	}
	
	
	@Bean
	KafkaManager kafkaDataManager( Properties properties ) {
		return new KafkaManager( properties.getLveBetriebsparameterTopic(), properties.getUfdBetriebsparameterTopic() );
	}
}