package de.heuboe.datex2.vms.service.kafka;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import com.google.protobuf.util.Timestamps;

import de.heuboe.log.Logger;
import de.heuboe.nrw.ims.SwitchingDataProvider;
import de.heuboe.nrw.ims.SwitchingDataProvider.ErrorType;
import eu.vmis_ehe.vmis2.tls.received.WZGDeFehler;

/**
 * 
 * Validity sections receiver
 * 
 * @author peters
 *
 */
@Configuration
public class KafkaDataReceiverError implements ConsumerSeekAware { 
    
    private static final Logger LOGGER = Logger.getLogger( KafkaDataReceiverError.class );
    private static final String RECEIVED = "Received: ";
    
    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
    
    private SwitchingDataProvider dataProvider;
    private Set<String> wzgIds = new HashSet<>();
    
    private int numReceivedE = 0;
    
    private long startLastOffset = 0; 
    private long lastOffset = 0;
    private boolean initFinished = false;
    
    @Value("${spring.kafka.consumer.group-id}")   
    private String kafkaConsumerId;
    
	public int getNumReceivedE()
	{
		return numReceivedE;
	}
    
    /**
     * 
     * Receives switching error updates
     * 
     * @param deFehler   Fehler
     * @param key        Kafka message key
     * @param offset     Message offset in Kafka queue
     */
    @KafkaListener( id = "${spring.kafka.consumer.group-id}"+ "_fehler_listener", 
					properties = {"enable.auto.commit:false", "auto.offset.reset:latest"},
    		        topics = "${de.heuboe.datex2.vms.service.topicFehler}", 
    		        autoStartup = "false" )
    public synchronized void listen( final WZGDeFehler deFehler, 
    		                         @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
    		                         @Header(KafkaHeaders.OFFSET) long offset ) {
    	
    	LOGGER.debug( RECEIVED + key );
		numReceivedE++;
		
    	synchronized( this ) {
    		lastOffset = offset;
    	}	
    	
    	if( !initFinished && stateReceived() ) {
    		LOGGER.info( "Initial state 'fehler' received (Kafka)" );
    		initFinished = true;
    	}
   	
    	String wzgId = deFehler.getId();
    	
    	if( wzgIds.isEmpty() || wzgIds.contains( wzgId ) ) {
			ErrorType et = toErrorType( deFehler.getFehlercode() );
			dataProvider.handleError( wzgId, Timestamps.toMillis( deFehler.getTlsTime() ), et );
    	}    	
    }
    
    private ErrorType toErrorType( int errorCode ) {
    	if( errorCode != 0 ) {
    		return ErrorType.OTHER; 
    	} else {
    		return ErrorType.NONE;
    	}
    } 
    
    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        assignments.keySet().forEach(tp -> callback.seekToBeginning(tp.topic(), tp.partition()));
        
        if( !assignments.isEmpty() ) {
        	synchronized( this ) {
        		startLastOffset = assignments.entrySet().iterator().next().getValue();
        	}
        }
    }
    
    
    /**
     * 
     * Starts Kafka message receiving
     * 
     * @param dataProvider SwitchingDataProvider
     */
	public void start(SwitchingDataProvider dataProvider) {
		this.dataProvider = dataProvider;
		kafkaListenerEndpointRegistry.getListenerContainer( kafkaConsumerId + "_fehler_listener" ).start();
	}

    /**
     * 
     * Starts Kafka message receiving
     * 
     * @param dataProvider 	SwitchingDataProvider
     * @param wzgIds		Relevant WZG IDs
     */
	public void start(SwitchingDataProvider dataProvider, Set<String> wzgIds) {
		this.dataProvider = dataProvider;
		this.wzgIds = wzgIds;
		kafkaListenerEndpointRegistry.getListenerContainer( kafkaConsumerId + "_fehler_listener" ).start();
	}

	/**
	 * 
	 * Checks if initial data has been received
	 * 
	 * @return true: initial data has been received
	 */
	public synchronized boolean stateReceived() {
		return lastOffset >= startLastOffset - 1;
	}
	
}
