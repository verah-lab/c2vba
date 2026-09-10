package de.heuboe.datex2.vms.service.kafka;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import com.google.protobuf.util.Timestamps;

import de.heuboe.log.Logger;
import de.heuboe.nrw.ims.DataReceiver;
import de.heuboe.nrw.ims.SwitchingDataProvider;
import de.heuboe.nrw.ims.SwitchingDataProvider.WzgSwitchData;
import eu.vmis_ehe.vmis2.tls.received.WZGStellzustand;
import eu.vmis_ehe.vmis2.tls.received.WZGStellzustandList;

/**
 * 
 * Validity sections receiver
 * 
 * @author peters
 *
 */
//@Configuration
public class KafkaDataReceiver implements ConsumerSeekAware, DataReceiver { 
    
    private static final Logger LOGGER = Logger.getLogger( KafkaDataReceiver.class );
    private static final String RECEIVED = "Received: ";
    
    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
    
    @Autowired
    private KafkaDataReceiverError errorReceiver;
    
    private SwitchingDataProvider dataProvider;
    private Set<String> wzgIds = new HashSet<>();
    
    private long startLastOffset = 0; 
    private long lastOffset = 0;
    private boolean initFinished = false;
    
    private int numReceivedS = 0;
    
    @Value("${spring.kafka.consumer.group-id}")   
    private String kafkaConsumerId;
    
	public int getNumReceivedS()
	{
		return numReceivedS;
	}
	
	/**
	 * 
	 * Checks if initial data has been received
	 * 
	 * @return true: initial data has been received
	 */
	public synchronized boolean stateReceived() {
		return ( lastOffset >= startLastOffset - 1 ) && errorReceiver.stateReceived();
	}
    
    private Byte[] getTextBytes( String text ) {
    	
    	return ArrayUtils.toObject( text.getBytes( StandardCharsets.ISO_8859_1 ) );
    }
    
    /**
     * 
     * Receives switching updates
     * 
     * @param stellzustand   Stellzustände
     * @param key   		 Kafka message key
     * @param offset   		 Offset in partition
     */
    @KafkaListener( id = "${spring.kafka.consumer.group-id}"+ "_stellzustand_listener", 
    				properties = {"enable.auto.commit:false", "auto.offset.reset:latest"},
    		        topics = "${de.heuboe.datex2.vms.service.topicStellzustand}", 
    		        autoStartup = "false" )
    public synchronized void listen( final WZGStellzustandList stellzustand, 
    		                         @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
    		                         @Header(KafkaHeaders.OFFSET) long offset ) {
    	
    	LOGGER.debug( RECEIVED + key );
    	numReceivedS++;
    	
    	synchronized( this ) {
    		lastOffset = offset;
    	}
    	
    	if( !initFinished && stateReceived() ) {
    		LOGGER.info( "Initial state 'stellzustand' received (Kafka)" );
    		initFinished = true;
    	}
    	
    	for( WZGStellzustand wzgStellzustand : stellzustand.getElementsList() ) {
	    	String wzgId = wzgStellzustand.getId();
	    	
	    	if( wzgIds.isEmpty() || wzgIds.contains( wzgId ) ) {
	    	
				WzgSwitchData data = new WzgSwitchData( wzgId );
		
				data.setTimestamp( new Date( Timestamps.toMillis( wzgStellzustand.getTlsTime() ) ) );
				data.setTlsCode( wzgStellzustand.getStellcode() );
				data.setFunktionsbyte( ( wzgStellzustand.getFunktionsbyte() == 0 ) ? 0 : 1  ); 
				data.setText( getTextBytes( wzgStellzustand.getTextzeichen() ) ); 
				data.setNumTextByte( wzgStellzustand.getTextzeichen().length() );
				dataProvider.handleData( Arrays.asList( data ) );
	    	}
    	}
    }
    
    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        if( !assignments.isEmpty() ) {
        	synchronized( this ) {
        		startLastOffset = assignments.entrySet().iterator().next().getValue();
        	}
        }
        assignments.keySet().forEach(tp -> callback.seekToBeginning(tp.topic(), tp.partition()));
    }


	@Override
	public void start(SwitchingDataProvider dataProvider) {
		this.dataProvider = dataProvider;
		kafkaListenerEndpointRegistry.getListenerContainer( kafkaConsumerId + "_stellzustand_listener").start();
		errorReceiver.start( dataProvider );
	}

	@Override
	public void start(SwitchingDataProvider dataProvider, Set<String> wzgIds) {
		this.dataProvider = dataProvider;
		this.wzgIds = wzgIds;
		kafkaListenerEndpointRegistry.getListenerContainer( kafkaConsumerId + "_stellzustand_listener").start();
		errorReceiver.start( dataProvider, wzgIds );
	}
    
}
