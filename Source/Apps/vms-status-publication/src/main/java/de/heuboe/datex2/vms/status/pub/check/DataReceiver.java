package de.heuboe.datex2.vms.status.pub.check;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.util.Timestamps;

import de.heuboe.kafka.listener.KafkaException;
import de.heuboe.kafka.listener.KafkaProtoTypedReceiver;
import de.heuboe.kafka.listener.KafkaReceiverFactory.ListenInitPosition;
import de.heuboe.log.Logger;
import eu.vmis_ehe.vmis2.tls.received.WZGStellzustand;
import eu.vmis_ehe.vmis2.tls.received.WZGStellzustandList;


/**
 * 
 * WZGStellzustandList receiver
 * 
 * @author peters
 *
 */
public class DataReceiver extends KafkaProtoTypedReceiver<WZGStellzustandList> {

	private static final Logger LOGGER = Logger.getLogger( DataReceiver.class );
	
	private Map<String,DbData > data = new HashMap<>();
	
	/**
	 * 
	 * Constructor
	 * 
	 * @throws KafkaException	Error
	 */
	public DataReceiver() throws KafkaException {
		super( WZGStellzustandList.class );
	}

	@Override
	public ListenInitPosition getReceiveMode() {
		return ListenInitPosition.STATE;
	}

	@Override
	public void handleMessage(String key, String topic, Map<String, String> headers, WZGStellzustandList message) {
		for( WZGStellzustand wsz : message.getElementsList() ) {
			DbData d = new DbData();
			d.setId( wsz.getId() );
			d.setCode( wsz.getStellcode() );
			
			if( wsz.getFunktionsbyte() == 0 ) {
				d.setCode( -1 );
			}
			
			d.setTime( new Date( Timestamps.toMillis( wsz.getTlsTime() ) ) );
			d.setSystemTime( new Date( Timestamps.toMillis( wsz.getProcessTime() ) )  );
			
			data.put( wsz.getId(), d );
		}
	}

	@Override
	public void handleError(String key, String topic, Map<String, String> headers, byte[] data, String errorMsg) {
		LOGGER.error( "Error receiving WZGStellzustandList" );
		LOGGER.error( errorMsg );
	}

	public Map<String,DbData> getData() {
		return data;
	}

	@Override
	public void handleDeletion(String key, String topic, Map<String, String> headers) {
		// Not called
	}
}
