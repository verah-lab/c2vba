package de.heuboe.sdbby.service.config.kafka;

import java.util.Map;

import de.heuboe.kafka.listener.KafkaException;
import de.heuboe.kafka.listener.KafkaReceiverFactory.ListenInitPosition;
import de.heuboe.kafka.listener.KafkaReceiverSubscription;
import de.heuboe.kafka.listener.SpringKafkaProtoTypedReceiver;
import de.heuboe.log.Logger;
import de.heuboe.sdbby.service.impl.CommStatMsgConsumer;
import eu.vmis_ehe.vmis2.tls.received.SYSFehlerDUEList;


/**
 * 
 * WZGStellzustandList receiver
 * 
 * @author peters
 *
 */
public class SYSFehlerDUEReceiver extends SpringKafkaProtoTypedReceiver<SYSFehlerDUEList> {

	private static final Logger LOGGER = Logger.getLogger( SYSFehlerDUEReceiver.class );
	
	private CommStatMsgConsumer  msgConsumer = null;;
	
	/**
	 * 
	 * Constructor
	 * 
	 * @throws KafkaException	Error
	 */
	public SYSFehlerDUEReceiver( KafkaReceiverSubscription rs ) throws KafkaException {
		super( SYSFehlerDUEList.class, rs );
	}
	
	public void registerMsgConsumer( CommStatMsgConsumer msgConsumer )  {
		this.msgConsumer = msgConsumer;
	}

	@Override
	public ListenInitPosition getReceiveMode() {
		return ListenInitPosition.STATE;
	}

	@Override
	public void handleMessage(String key, String topic, Map<String, String> headers, SYSFehlerDUEList message) {
		msgConsumer.update( message.getElementsList() );
	}

	@Override
	public void handleError(String key, String topic, Map<String, String> headers, byte[] data, String errorMsg) {
		LOGGER.error( "Error receiving SYSFehlerDUE" );
		LOGGER.error( errorMsg );
	}

	@Override
	public void handleDeletion(String key, String topic, Map<String, String> headers) {
		// Not called
	}
	
	@Override
	public void notifyStateFinished() {
		LOGGER.info( "SYSFehlerDUEReceiver: state received" );
		msgConsumer.stateReceived();
	}

}
