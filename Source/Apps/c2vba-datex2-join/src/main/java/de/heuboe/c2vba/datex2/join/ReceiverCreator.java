package de.heuboe.c2vba.datex2.join;

import javax.xml.bind.JAXBException;

import de.heuboe.c2vba.data.DatexIIContent;
import de.heuboe.c2vba.data.pojo.PDatexIIContent;
import de.heuboe.c2vba.datex2.kafka.PublicationResolver;
import de.heuboe.datex2.schema.SchemaUtil;
import de.heuboe.kafka.listener.KafkaException;
import de.heuboe.kafka.listener.KafkaReceiverSubscription;
import de.heuboe.util.JAXBUtil;


/**
 * 
 * Creates receiver
 * 
 * @author peters
 *
 * @param <T> D2Logical model of a profile
 */
public class ReceiverCreator<T> {

	/**
	 * 
	 * Creates region receiver
	 * 
	 * @param props				Spring properties
	 * @param region			Region
	 * @param topic				Kafka topic
	 * @param clasT				Class of receiveed messages
	 * @param schemaFile		DATEX-II schema file
	 * @param schemaPackage		Schema (Java) package
	 * @return					Receiver
	 * @throws KafkaException	Kafka error	
	 * @throws JAXBException	JXB error	
	 */
	public ReceiverT<T> createRegionReceiver( Properties props, 
			                                  String region,
			                                  String topic,
			                                  Class<T> clasT,
			                                  String schemaFile,
			                                  String schemaPackage ) throws KafkaException, JAXBException  {
		
		
		JAXBUtil jaxbUtil = new JAXBUtil( schemaFile, 
					                      schemaPackage, 
					 			          SchemaUtil.DATEX2_SCHEMA_NS,
					 			          false );

		
		PublicationResolver<T> resolver = new PublicationResolver<>( clasT, schemaFile, jaxbUtil );

		KafkaReceiverSubscription rs = new KafkaReceiverSubscription( props.getConsumerGroupId(), 
				                                                      topic, 
	            												      PDatexIIContent.class.getName(), 
	                                                                  null );
		
		return new ReceiverT<>( region, DatexIIContent.class, rs, resolver ); 
	}

}
         