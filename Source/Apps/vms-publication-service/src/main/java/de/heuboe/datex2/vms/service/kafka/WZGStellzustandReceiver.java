package de.heuboe.datex2.vms.service.kafka;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import com.google.protobuf.util.Timestamps;

import de.heuboe.kafka.listener.KafkaException;
import de.heuboe.kafka.listener.KafkaReceiverFactory.ListenInitPosition;
import de.heuboe.log.Logger;
import de.heuboe.nrw.ims.DataReceiver;
import de.heuboe.nrw.ims.SwitchingDataProvider;
import de.heuboe.nrw.ims.SwitchingDataProvider.WzgSwitchData;
import de.heuboe.kafka.listener.KafkaReceiverSubscription;
import de.heuboe.kafka.listener.SpringKafkaProtoTypedReceiver;
import eu.vmis_ehe.vmis2.tls.received.WZGStellzustand;
import eu.vmis_ehe.vmis2.tls.received.WZGStellzustandList;

public class WZGStellzustandReceiver extends SpringKafkaProtoTypedReceiver<WZGStellzustandList> implements DataReceiver {
	
    private static final Logger LOGGER = Logger.getLogger( WZGStellzustandReceiver.class );
    
    private SwitchingDataProvider dataProvider;
    private Set<String> wzgIds = new HashSet<>();
    
    private WZGDeFehlerReceiver errorReceiver;
    
	public WZGStellzustandReceiver( KafkaReceiverSubscription rs, WZGDeFehlerReceiver errorReceiver ) throws KafkaException {
		super( WZGStellzustandList.class, rs );
		this.errorReceiver = errorReceiver;
	}
	
    private Byte[] getTextBytes( String text ) {
    	
    	return ArrayUtils.toObject( text.getBytes( StandardCharsets.ISO_8859_1 ) );
    }
    
	@Override
	public void handleMessage(String key, String topic, Map<String,String> headers, WZGStellzustandList message) {
		
    	for( WZGStellzustand wzgStellzustand : message.getElementsList() ) {
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
	
	public boolean stateReceived() {
		return isStateFinished() && errorReceiver.isStateFinished();
	}

	@Override
	public ListenInitPosition getReceiveMode() {
		return ListenInitPosition.STATE;
	}

	@Override
	public void handleError(String key, String topic, Map<String, String> headers, byte[] data, String errorMsg) {
		LOGGER.error( "" );
	}

	@Override
	public void handleDeletion(String key, String topic, Map<String, String> headers) {
		// not called
	}

	@Override
	public void start(SwitchingDataProvider dataProvider) {
		this.dataProvider = dataProvider;
		this.start();
		errorReceiver.start( dataProvider );
	}

	@Override
	public void start(SwitchingDataProvider dataProvider, Set<String> wzgIds) {
		this.dataProvider = dataProvider;
		this.wzgIds = wzgIds;
		this.start();
		errorReceiver.start( dataProvider, wzgIds );
	}
	
}
