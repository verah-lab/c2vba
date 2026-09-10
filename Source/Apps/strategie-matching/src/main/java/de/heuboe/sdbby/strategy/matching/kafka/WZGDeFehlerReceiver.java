package de.heuboe.sdbby.strategy.matching.kafka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.heuboe.data.impl.DataInt;
import de.heuboe.data.impl.DataString;
import de.heuboe.kafka.listener.KafkaException;
import de.heuboe.kafka.listener.KafkaReceiverSubscription;
import de.heuboe.sdbby.strategy.matching.DataHandler;
import de.heuboe.sdbby.strategy.matching.Dataset;
import de.heuboe.sdbby.strategy.matching.Dataset.Value;
import de.heuboe.sdbby.strategy.matching.DatasetImpl;
import eu.vmis_ehe.vmis2.tls.received.WZGDeFehler;
import eu.vmis_ehe.vmis2.tls.received.WZGDeFehlerList;

public class WZGDeFehlerReceiver extends KafkaReceiver<WZGDeFehlerList,WZGDeFehler> {
	
	public WZGDeFehlerReceiver( KafkaReceiverSubscription rs ) throws KafkaException {
		super( WZGDeFehlerList.class, rs );
	}
	
	@Override
	public void handleMessage(String key, String topic, Map<String,String> headers, WZGDeFehlerList message) {
		
		List<Dataset> datasets = new ArrayList<>();
		for( WZGDeFehler bp : message.getElementsList() ) {
			datasets.add( toDataset( bp ) );
		}
		
		if( !isStateFinished() ) {
			stateDatasets.addAll( datasets );
		} else {
			consumer.consume( datasets );
		}
	}
	
	public String getDatakind() {
		return DataHandler.DK_WZG_DE_FEHLER;
	}
	
	protected Dataset toDataset( WZGDeFehler df ) {
		Map<String,Value> values = new HashMap<>();
		values.put( "id", new Value( new DataString(  df.getId() ) ) );
		values.put( "fehlercode", new Value( new DataInt(  df.getFehlercode() ) ) );
		
		return new DatasetImpl( DataHandler.DK_WZG_DE_FEHLER, values );
	} 
}
