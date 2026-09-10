package de.heuboe.datex2.location.service.server;

import javax.xml.bind.JAXBException;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import de.heuboe.datex2.location.service.LocationService;
import de.heuboe.datex2.mst.builder.ws.Properties;

/**
 * 
 * Spring configuration
 * 
 * @author peters
 *
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan( basePackages = { "de.heuboe.datex2.mst.builder.ws", 
                                 "de.heuboe.datex2.config.persistence",
                                 "de.heuboe.datex2.location.service.server" } )               
public class ConfigLoc {
	
	private static final Long CONN_TIMEOUT = 300000L;						// ms
	private static final Long RECEIVE_TIMEOUT = 300000L;					// ms
	
	@Bean
	LocationService locationService( Properties props )
	{
		String url = props.getLocServiceUrl();

		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass( LocationService.class );
		factory.setAddress( url );
		
		LocationService locService = (LocationService)factory.create();

        Client client = ClientProxy.getClient(locService);
        
        if (client != null) 
        {
			HTTPConduit conduit = (HTTPConduit) client.getConduit();
			HTTPClientPolicy policy = new HTTPClientPolicy();
			policy.setConnectionTimeout( CONN_TIMEOUT );
			policy.setReceiveTimeout( RECEIVE_TIMEOUT );
			conduit.setClient(policy);
        }
        
        locService.ping( "datex2MSTLocServiceBuilder" );
        
        return locService;
	}
	
	@Bean
	OpenLRPostProcessor postProcessor() throws JAXBException {
		return new OpenLRPostProcessor();
	}
}
