package de.heuboe.by.c2vba.datex2.mst.localisation.server.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import de.heuboe.kafka.listener.KafkaException;
import de.heuboe.kafka.listener.KafkaReceiverFactory;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEBetriebsparameterList;
import eu.vmis_ehe.vmis2.tls.received.pojo.PUFDBetriebsparameterList;

public class KafkaManager {
	
	private String betriebsparameterTopic;
	private String umfeldparameterTopic;

	@Value( "${spring.kafka.consumer.group-id}" )
	private String consumerGroupId;
	
	@Autowired 
    private KafkaReceiverFactory kafkaReceiverFactory;

    public KafkaManager( String betriebsparameterTopic, String umfeldparameterTopic) {
		super();
		this.betriebsparameterTopic = betriebsparameterTopic;
		this.umfeldparameterTopic = umfeldparameterTopic;
	}

	
    public LVEBetriebsparamterReceiver createLVEBetriebsparamterReceiver() throws KafkaException {
    	LVEBetriebsparamterReceiver bpr = new LVEBetriebsparamterReceiver();
    	
		kafkaReceiverFactory.setListener( bpr, 
				  consumerGroupId, 
				  betriebsparameterTopic, 
				  PLVEBetriebsparameterList.class.getName(), 
				  null );
    	
    	
    	return bpr;
    }
    
    public UFDBetriebsparamterReceiver createUFDBetriebsparamterReceiver() throws KafkaException {
    	UFDBetriebsparamterReceiver bpr = new UFDBetriebsparamterReceiver();
    	
		kafkaReceiverFactory.setListener( bpr, 
				  consumerGroupId, 
				  umfeldparameterTopic, 
				  PUFDBetriebsparameterList.class.getName(), 
				  null );
    	
    	
    	return bpr;
    }

}
