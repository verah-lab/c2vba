package de.heuboe.srb.datex2.srp;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import de.heuboe.c2vba.data.StrategyStates;
import de.heuboe.datex2.base.ProfileConverter;
import de.heuboe.datex2.push.D2PubSenderMDM;
import de.heuboe.datex2.schema.SchemaUtil;
import de.heuboe.kafka.listener.KafkaException;
import de.heuboe.kafka.listener.KafkaReceiverSubscription;
import de.heuboe.sdbby.service.iface.SdbbyService;
import de.heuboe.wls.iface.WebLocationServer;
import de.heuboe.wls.util.WlsException;
import de.heuboe.wls.util.WlsWebServiceClient;
import eu.datex2.schema._2._2_0.srp.D2LogicalModel;
import eu.datex2.schema._2._2_0.srp.OpenlrPointLocationReference;


/**
 * 
 * Spring configuration
 * 
 * @author peters
 *
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan( basePackages = { "de.heuboe.srb.datex2.srp", "de.heuboe.kafka.listener" } )               
public class ConfigSpring {       
	
	private static final String SRP_SCHEMA_FILE = "d2Schema/StrategicRouting_withSubscriptionLifecycle-c2vba.xsd";
	private static final String DATEX2_SCHEMA_2_2_3_FILE_SRP = "d2Schema/DATEXIISchema_2_2_3-srp.xsd"; 
	
	@Bean
	WebLocationServer createWlsClient( Properties properties ) throws JAXBException, WlsException {
	
		
		String url = properties.getWlsUrl();
		
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass( WebLocationServer.class );
		factory.setAddress( url );
		factory.setWsdlURL( url + "?wsdl" );
		
		factory.setEndpointName( new QName( "http://wls.heuboe.de/iface/",
				    						"WebLocationServerPort") );
		factory.setServiceName( new QName( "http://wls.heuboe.de/iface/",  
										   "WebLocationServerSrv" ) );

		JAXBContext context = javax.xml.bind.JAXBContext.newInstance( WlsWebServiceClient.getWlsJAXBContext() );
		JAXBDataBinding dataBinding = new JAXBDataBinding( context );
		factory.setDataBinding( dataBinding );
		
		WebLocationServer wlsService = (WebLocationServer)factory.create();

        Client client = ClientProxy.getClient( wlsService );
        
        if (client != null) 
        {
			HTTPConduit conduit = (HTTPConduit) client.getConduit();
			HTTPClientPolicy policy = new HTTPClientPolicy();
			policy.setConnectionTimeout( 300000L );
			policy.setReceiveTimeout( 300000L );
			conduit.setClient(policy);
        }
        
        return wlsService;
	}
	
	
	@Bean
	SdbbyService sdbbyService( Properties properties )
			throws JAXBException, SRPException 
	{
		String sdbbyUrl = properties.getSdbbyUrl();  
		if( ( sdbbyUrl == null ) || sdbbyUrl.isEmpty() )
		{
			throw new SRPException( SRPException.ERROR_SVC, "Invalid URL <" + sdbbyUrl + ">" );
		}

		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass( SdbbyService.class );
		factory.setAddress( sdbbyUrl );
		
		SdbbyService sdbbyService = (SdbbyService)factory.create();

        Client client = ClientProxy.getClient( sdbbyService );
        
        if (client != null) 
        {
			HTTPConduit conduit = (HTTPConduit) client.getConduit();
			HTTPClientPolicy policy = new HTTPClientPolicy();
			policy.setConnectionTimeout( Util.CONN_TIMEOUT );
			policy.setReceiveTimeout( Util.CONN_TIMEOUT );
			conduit.setClient(policy);
        }
        
        return sdbbyService;
	}

    
	@Bean
	Builder builder( Properties properties, Reader reader ) throws Exception {
		return new Builder( reader, 
							properties.getPubInterval(),
				            properties.getPubMode(), 
				            properties.getStrategyRuleFile(),
				            properties.getStrategyCauseFile(),
				            properties.getD2SchemaLocation(),
				            properties.getDatex2Sender(),
				            properties.getDatex2Target() );
	}
	
	@Bean
	Manager manager( Properties properties,
			         Builder builder, 
			         Sender sender ) throws Exception {
		return new Manager( builder, 
							sender, 
				            properties.getFileDir(), 
				            properties.getPubMode(), 
				            properties.getPubInterval(), 
				            properties.getCheckInterval() );
	}

	@Bean
	Publisher publisher() {
		return new Publisher();
	}

	@Bean(initMethod = "init")
	Reader reader( Properties properties, 
			       WebLocationServer wls, 
			       SdbbyService sdbbyService,
			       ProfileConverter<OpenlrPointLocationReference,eu.datex2.schema._2._2_0.OpenlrPointLocationReference> profileConverterOpenLr ) throws Exception {
		return new Reader( wls, 
				           sdbbyService, 
				           properties.getLclVersion(), 
				           properties.getStrategyRoutesFilePath(), 
				           properties.getWlsLocationDir(),
				           properties.getRouteEncoding(),
				           properties.isConvert2BaseSchema() );
	}
	
	@Bean
	Sender sender( Properties properties, D2PubSenderMDM mdmSender ) throws Exception {
		mdmSender.getRemoteConfig().addSchemaLocation( SchemaUtil.DATEX2_SCHEMA_NS, 
				                                       properties.getD2SchemaLocation() );
		return new Sender();
	}

	
	@Bean
	StrategyStatesReceiver stellzustandReceiver( Properties properties ) throws KafkaException {
		
		KafkaReceiverSubscription rs = new KafkaReceiverSubscription( properties.getConsumerGroupId(), 
																	  properties.getStrategyStatesTopic(),
				                                                      StrategyStates.class.getName(), 
				                                                      null );

		
		return new StrategyStatesReceiver( rs );
	}
	
	@Bean
	ProfileConverter<D2LogicalModel,eu.datex2.schema._2._2_0.D2LogicalModel> profileConverter() throws JAXBException {
		return new ProfileConverter<>( SRP_SCHEMA_FILE, 
                                       D2LogicalModel.class,
                                       DATEX2_SCHEMA_2_2_3_FILE_SRP, 
                                       eu.datex2.schema._2._2_0.D2LogicalModel.class );
	}
	
	@Bean
	ProfileConverter<OpenlrPointLocationReference,eu.datex2.schema._2._2_0.OpenlrPointLocationReference> profileConverterOpenLr() throws JAXBException {
		return new ProfileConverter<>( SRP_SCHEMA_FILE, 
                                       OpenlrPointLocationReference.class,
                                       DATEX2_SCHEMA_2_2_3_FILE_SRP, 
                                       eu.datex2.schema._2._2_0.OpenlrPointLocationReference.class );
	}

}