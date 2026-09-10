package de.heuboe.sdbby.service.impl;

import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import de.heuboe.c2vba.data.pojo.PCommStates;
import de.heuboe.kafka.listener.KafkaException;
import de.heuboe.kafka.listener.KafkaReceiverSubscription;
import de.heuboe.kafka.producer.KafkaProducer;
import de.heuboe.sdbby.service.config.kafka.SYSFehlerDUEReceiver;
import de.heuboe.sdbby.service.iface.SdbbyService;
import de.heuboe.ws.HbServiceBaseImpl;
import eu.vmis_ehe.vmis2.tls.received.pojo.PSYSFehlerDUEList;

/**
 * 
 * Spring configuration
 * 
 * @author peters
 *
 */
@Configuration
@EnableAutoConfiguration
@EnableMongoRepositories(basePackages = "de.heuboe.sdbby.service.config.db")
@ComponentScan( basePackages = { "de.heuboe.sdbby.service.impl", 
		                         "de.heuboe.sdbby.service.config.db",
		                         "de.heuboe.kafka.listener" } )               
public class Config {

	@Bean
	SdbbyService sdbbyService( CommStatMan commStatMan, StrategyMan strategyMan ) {
		return new SdbbyServiceImpl( commStatMan, strategyMan );
	}
	
	@Bean
	org.apache.cxf.endpoint.Server soapEndpoint( SdbbyService service, Properties properties ) {
		
		String serviceAddress = "http://0.0.0.0:" + properties.getWsPort() + "/SdbbyService";
		
		JaxWsServerFactoryBean svrFactory = new JaxWsServerFactoryBean();
		svrFactory.setServiceClass(SdbbyService.class);
		svrFactory.setAddress(serviceAddress);
		svrFactory.setServiceBean(service);
		svrFactory.getInInterceptors().add(new LoggingInInterceptor());
		svrFactory.getOutInterceptors().add(new LoggingOutInterceptor());
		return svrFactory.create();
	}

	@Bean
	HbServiceBaseImpl sdbbyServiceInfo(SdbbyService sdbbyService) {
		return new HbServiceBaseImpl(SdbbyServiceImpl.class.getName(), sdbbyService);
	}
	
	@Bean
	org.apache.cxf.endpoint.Server soapEndpointInfo(HbServiceBaseImpl service, Properties properties ) {

		String serviceAddress = "http://0.0.0.0:" + properties.getWsPort() + "/SdbbyServiceInfo";
		
		JaxWsServerFactoryBean svrFactory = new JaxWsServerFactoryBean();
		svrFactory.setServiceClass(HbServiceBaseImpl.class);
		svrFactory.setAddress(serviceAddress);
		svrFactory.setServiceBean(service);
		svrFactory.getInInterceptors().add( new LoggingInInterceptor() );
		svrFactory.getOutInterceptors().add( new LoggingOutInterceptor() );
		return svrFactory.create();
	}
	
	@Bean
	StrategyMan strategyMan() {
		return new StrategyMan();
	}

	@Bean(initMethod = "init" )
	CommStatMan commStatMan( Properties properties ) {
		return new CommStatMan( properties.getCfgFilename() );
	}

	@Bean
	SYSFehlerDUEReceiver stellzustandReceiver( Properties properties ) throws KafkaException {
		
		KafkaReceiverSubscription rs = new KafkaReceiverSubscription( properties.getConsumerGroupId(), 
				                                                      properties.getFehlerTopic(), 
				                                                      PSYSFehlerDUEList.class.getName(), 
				                                                      null );

		return new SYSFehlerDUEReceiver( rs );
	}
	
	@Bean
	KafkaProducer<PCommStates> commStatesProducer( Properties properties ) {
		KafkaProducer<PCommStates>  kp = new KafkaProducer<PCommStates>();
		kp.setTopic( properties.getCommStatesTopic() );
		return kp;
	}
}
