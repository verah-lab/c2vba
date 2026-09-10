package de.heuboe.datex2.vms.service.kafka;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.protobuf.util.Timestamps;

import de.heuboe.kafka.listener.KafkaException;
import de.heuboe.kafka.listener.KafkaReceiverFactory.ListenInitPosition;
import de.heuboe.log.Logger;
import de.heuboe.nrw.ims.SwitchingDataProvider;
import de.heuboe.nrw.ims.SwitchingDataProvider.ErrorType;
import de.heuboe.kafka.listener.KafkaReceiverSubscription;
import de.heuboe.kafka.listener.SpringKafkaProtoTypedReceiver;
import eu.vmis_ehe.vmis2.tls.received.WZGDeFehler;
import eu.vmis_ehe.vmis2.tls.received.WZGDeFehlerList;

public class WZGDeFehlerReceiver extends SpringKafkaProtoTypedReceiver<WZGDeFehlerList> {
	
    private static final Logger LOGGER = Logger.getLogger( WZGDeFehlerReceiver.class );
    
    private SwitchingDataProvider dataProvider;
    private Set<String> wzgIds = new HashSet<>();
    
	public WZGDeFehlerReceiver( KafkaReceiverSubscription rs ) throws KafkaException {
		super( WZGDeFehlerList.class, rs );
	}
	
	@Override
	public void handleMessage(String key, String topic, Map<String,String> headers, WZGDeFehlerList message) {
		
    	for( WZGDeFehler deFehler : message.getElementsList() ) {
	    	String wzgId = deFehler.getId();
	    	if( wzgIds.isEmpty() || wzgIds.contains( wzgId ) ) {
				ErrorType et = toErrorType( deFehler.getFehlercode() );
				dataProvider.handleError( wzgId, Timestamps.toMillis( deFehler.getTlsTime() ), et );
	    	}    	
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
	public ListenInitPosition getReceiveMode() {
		return ListenInitPosition.STATE;
	}

	@Override
	public void handleError(String key, String topic, Map<String, String> headers, byte[] data, String errorMsg) {
		LOGGER.error( "Error receiving WZGDeFehler: " + errorMsg );
	}

	@Override
	public void handleDeletion(String key, String topic, Map<String, String> headers) {
		// not called
	}

	public void start(SwitchingDataProvider dataProvider) {
		this.dataProvider = dataProvider;
		this.start();
	}

	public void start(SwitchingDataProvider dataProvider, Set<String> wzgIds) {
		this.dataProvider = dataProvider;
		this.wzgIds = wzgIds;
		this.start();
	}
	
}
