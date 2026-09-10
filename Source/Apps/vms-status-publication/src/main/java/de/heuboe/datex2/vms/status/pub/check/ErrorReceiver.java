package de.heuboe.datex2.vms.status.pub.check;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.heuboe.kafka.listener.KafkaException;
import de.heuboe.kafka.listener.KafkaProtoTypedReceiver;
import de.heuboe.kafka.listener.KafkaReceiverFactory.ListenInitPosition;
import de.heuboe.log.Logger;
import eu.vmis_ehe.vmis2.tls.received.WZGDeFehler;


/**
 * 
 * WZGDeFehler receiver 
 * 
 * @author peters
 *
 */
public class ErrorReceiver extends KafkaProtoTypedReceiver<WZGDeFehler>{
	
	private Set<String> idsWithError = new HashSet<>();
	
	private static final Logger LOGGER = Logger.getLogger( ErrorReceiver.class );
	
	/**
	 * 
	 * Constructor
	 * 
	 * @throws KafkaException	Error
	 */
	public ErrorReceiver() throws KafkaException {
		super( WZGDeFehler.class );
	}

	@Override
	public ListenInitPosition getReceiveMode() {
		return ListenInitPosition.STATE;
	}

	@Override
	public void handleMessage( String key, String topic, Map<String, String> headers, WZGDeFehler message ) {
		if( message.getFehlercode() != 0 ) {
			idsWithError.add( message.getId() );
		}
		
	}

	@Override
	public void handleError(String key, String topic, Map<String, String> headers, byte[] data, String errorMsg) {
		LOGGER.error( "Error receiving WZGStellzustandList" );
		LOGGER.error( errorMsg );
	}
	
	@Override
	public void handleDeletion(String key, String topic, Map<String, String> headers) {
		// Not called
	}
	
	public Set<String> getIdsWithError() {
		return idsWithError;
	}
}
