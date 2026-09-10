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
import eu.vmis_ehe.vmis2.tls.received.WZGKanalsteuerung;
import eu.vmis_ehe.vmis2.tls.received.WZGKanalsteuerungList;

public class WZGKanalsteuerungReceiver extends KafkaReceiver<WZGKanalsteuerungList,WZGKanalsteuerung> {
	
	public WZGKanalsteuerungReceiver( KafkaReceiverSubscription rs ) throws KafkaException {
		super( WZGKanalsteuerungList.class, rs );
	}
	
	@Override
	public void handleMessage(String key, String topic, Map<String,String> headers, WZGKanalsteuerungList message) {
		List<Dataset> datasets = new ArrayList<>();
		for( WZGKanalsteuerung bp : message.getElementsList() ) {
			datasets.add( toDataset( bp ) );
		}
		
		if( !isStateFinished() ) {
			stateDatasets.addAll( datasets );
		} else {
			consumer.consume( datasets );
		}
	}
	
	public String getDatakind() {
		return DataHandler.DK_WZG_KANAL_STEUERUNG;
	}
	
	protected Dataset toDataset( WZGKanalsteuerung ks ) {
		Map<String,Value> values = new HashMap<>();
		values.put( "id", new Value( new DataString(  ks.getId() ) ) );
		values.put( "Steuerbyte", new Value( new DataInt(  ks.getKanalsteuerbyte() ) ) );
		
		return new DatasetImpl( DataHandler.DK_WZG_KANAL_STEUERUNG , values );
	} 
}
