package de.heuboe.by.c2vba.datex2.mst.localisation.server.kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import de.heuboe.kafka.listener.KafkaException;
import de.heuboe.kafka.listener.KafkaProtoTypedReceiver;
import de.heuboe.kafka.listener.KafkaReceiverFactory.ListenInitPosition;
import de.heuboe.log.Logger;
import eu.vmis_ehe.vmis2.tls.received.UFDBetriebsparameter;
import eu.vmis_ehe.vmis2.tls.received.UFDBetriebsparameterList;

/**
 * 
 * Subscribes on WZGBetriebsart
 * 
 * @author peters
 *
 */
public class UFDBetriebsparamterReceiver extends KafkaProtoTypedReceiver<UFDBetriebsparameterList> {
	
	private static final Logger LOGGER = Logger.getLogger(UFDBetriebsparamterReceiver.class);
	
	private Map<String,UFDBetriebsparameter> betriebsparameter = new HashMap<>();
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param rs				Subscription details
	 * @throws KafkaException	Error	
	 */
	public UFDBetriebsparamterReceiver() throws KafkaException {
		super( UFDBetriebsparameterList.class );
	}
	
	@Override
	public void handleMessage(String key, String topic, Map<String,String> headers, UFDBetriebsparameterList message) {
		for( UFDBetriebsparameter bp : message.getElementsList() ) {
			betriebsparameter.put( bp.getId(), bp );
		}
	}

	@Override
	public ListenInitPosition getReceiveMode() {
		return ListenInitPosition.STATE;
	}

	@Override
	public void handleDeletion(String key, String topic, Map<String, String> headers) {
		// Not exepected
	}

	@Override
	public void handleError(String key, String topic, Map<String, String> headers, byte[] data, String errorMsg) {
		LOGGER.error( "Error receiving UFDBetriebsparameter: " + errorMsg );
	}
	
	public Map<String,Integer> getDetectionPeriods()  {
		return betriebsparameter.entrySet().stream()
				               .collect( Collectors.toMap( e -> e.getKey(), e -> e.getValue().getErfassungsperiodendauer() ));
	}
}
