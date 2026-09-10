package de.heuboe.sdbby.strategy.matching;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import de.heuboe.c2vba.data.pojo.PStrategyStates;
import de.heuboe.kafka.listener.KafkaException;
import de.heuboe.kafka.listener.KafkaReceiverSubscription;
import de.heuboe.kafka.producer.KafkaProducer;
import de.heuboe.sdbby.strategy.matching.kafka.WZGBetriebsartReceiver;
import de.heuboe.sdbby.strategy.matching.kafka.WZGDeFehlerReceiver;
import de.heuboe.sdbby.strategy.matching.kafka.WZGKanalsteuerungReceiver;
import de.heuboe.sdbby.strategy.matching.kafka.WZGStellzustandReceiver;
import eu.vmis_ehe.vmis2.tls.received.pojo.PWZGBetriebsartList;
import eu.vmis_ehe.vmis2.tls.received.pojo.PWZGDeFehler;
import eu.vmis_ehe.vmis2.tls.received.pojo.PWZGKanalsteuerungList;
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
@EnableMongoRepositories(basePackages = "de.heuboe.sdbby.strategy.matching.db")
@ComponentScan( basePackages = { "de.heuboe.sdbby.strategy.matching", 
		                         "de.heuboe.sdbby.strategy.matching.db",
		                         "de.heuboe.kafka.listener" } )               
public class ConfigSpring {       
	
	@Bean(initMethod = "init" )
	ConfigService configService() {
		return new ConfigService();
	}

	@Bean
	GeoManager geoManager() {
		return  new GeoManager();
	}

	@Bean(initMethod = "init" )
	StrategyMatching strategyMatching( Properties properties ) {

		return new StrategyMatching( properties.getRulesFile() );
	}
	
	@Bean
	DataHandler dataHandler( Properties properties ) {
		return new DataHandler( properties.getUpdateCycle(), properties.getPrecedingFontChars() );
	}
	
	@Bean
	DataReceiver dataReceiver() {
		return new DataReceiver();
	}
	
	@Bean
	WZGBetriebsartReceiver betriebsartReceiver( Properties properties ) throws KafkaException {
		
		KafkaReceiverSubscription rs = new KafkaReceiverSubscription( properties.getConsumerGroupId(), 
				                                                      properties.getWzgBetriebsartTopic(),
				                                                      PWZGBetriebsartList.class.getName(), 
				                                                      null );

		
		return new WZGBetriebsartReceiver( rs );
	}

	@Bean
	WZGStellzustandReceiver stellzustandReceiver( Properties properties ) throws KafkaException {
		
		KafkaReceiverSubscription rs = new KafkaReceiverSubscription( properties.getConsumerGroupId(), 
																	  properties.getWzgStellzustandTopic(),
				                                                      PWZGStellzustandList.class.getName(), 
				                                                      null );

		
		return new WZGStellzustandReceiver( rs );
	}
	
	@Bean
	WZGKanalsteuerungReceiver kanalsteuerungReceiver( Properties properties ) throws KafkaException {
		
		KafkaReceiverSubscription rs = new KafkaReceiverSubscription( properties.getConsumerGroupId(), 
																	  properties.getWzgKanalSteuerungTopic(), 
				                                                      PWZGKanalsteuerungList.class.getName(), 
				                                                      null );

		
		return new WZGKanalsteuerungReceiver( rs );
	}

	@Bean
	WZGDeFehlerReceiver fehlerReceiver( Properties properties ) throws KafkaException {
		
		KafkaReceiverSubscription rs = new KafkaReceiverSubscription( properties.getConsumerGroupId(), 
																	  properties.getWzgDeFehlerTopic(),
				                                                      PWZGDeFehler.class.getName(), 
				                                                      null );

		
		return new WZGDeFehlerReceiver( rs );
	}

	
	
	@Bean
	KafkaProducer<PStrategyStates> strategyStatesProducer( Properties properties ) {
		KafkaProducer<PStrategyStates>  kp = new KafkaProducer<>();
		kp.setTopic( properties.getStrategyStateTopic() );
		return kp;
	}

}