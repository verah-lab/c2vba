package de.heuboe.c2vba.kafka.test;

import java.util.HashMap;

import javax.xml.bind.JAXBException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

import de.heuboe.c2vba.data.DatexIIContent;
import de.heuboe.c2vba.data.pojo.PDatexIIContent;
import de.heuboe.c2vba.datex2.kafka.Producer;
import de.heuboe.c2vba.datex2.kafka.PublicationResolver;
import de.heuboe.datex2.schema.SchemaUtil;
import de.heuboe.kafka.listener.KafkaException;
import de.heuboe.kafka.listener.KafkaReceiverSubscription;
import de.heuboe.kafka.producer.KafkaProducerProto;
import de.heuboe.util.JAXBUtil;
import eu.datex2.schema._2._2_0.D2LogicalModel;

@Configuration
@EnableKafka
public class TestConfig {
	
	@Bean
	KafkaProducerProto<DatexIIContent,PDatexIIContent> kafkaProducerProto() throws KafkaException {
		return new KafkaProducerProto<DatexIIContent,PDatexIIContent>( DatexIIContent.class, "C2VBA-datex2-publication-vms-sued" );
	}
	
	
	@Bean
	JAXBUtil jaxbUtil() throws JAXBException {
		return new JAXBUtil( "schema/" + SchemaUtil.DATEX2_SCHEMA_2_2_3_FILE, 
				 			 "eu.datex2.schema._2._2_0", 
				 			 SchemaUtil.DATEX2_SCHEMA_NS,
				 			 false );
	}
	
	@Bean
	PublicationResolver<D2LogicalModel> resolver( JAXBUtil jaxbUtil ) {
		return new PublicationResolver<D2LogicalModel>( D2LogicalModel.class, SchemaUtil.DATEX2_SCHEMA_2_2_3_FILE, jaxbUtil );
	}

	@Bean
	Producer<D2LogicalModel> producer( KafkaProducerProto<DatexIIContent,PDatexIIContent> kafkaProducer ) throws KafkaException {
		
		return new Producer<D2LogicalModel>( kafkaProducer, new HashMap<>() );
	}
	
	@Bean ReceiverT testReceiver( PublicationResolver<D2LogicalModel> resolver ) throws KafkaException {
		KafkaReceiverSubscription rs = new KafkaReceiverSubscription( "hugo", 
	            												      "C2VBA-datex2-publication-vms-nord", 
	            												      PDatexIIContent.class.getName(), 
	                                                                  null );
		
		return new ReceiverT( DatexIIContent.class, rs, resolver ); 
	}

}
