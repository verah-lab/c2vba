package de.heuboe.sdbby.strategy.matching.kafka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.heuboe.data.impl.DataInt;
import de.heuboe.data.impl.DataLong;
import de.heuboe.data.impl.DataString;
import de.heuboe.kafka.listener.KafkaException;
import de.heuboe.kafka.listener.KafkaReceiverSubscription;
import de.heuboe.sdbby.strategy.matching.DataHandler;
import de.heuboe.sdbby.strategy.matching.Dataset;
import de.heuboe.sdbby.strategy.matching.Dataset.Value;
import de.heuboe.sdbby.strategy.matching.DatasetImpl;
import eu.vmis_ehe.vmis2.tls.received.WZGStellzustand;
import eu.vmis_ehe.vmis2.tls.received.WZGStellzustandList;

public class WZGStellzustandReceiver extends KafkaReceiver<WZGStellzustandList,WZGStellzustand> {
	
	public WZGStellzustandReceiver( KafkaReceiverSubscription rs ) throws KafkaException {
		super( WZGStellzustandList.class, rs );
	}
	
	@Override
	public void handleMessage(String key, String topic, Map<String,String> headers, WZGStellzustandList message) {
		List<Dataset> datasets = new ArrayList<>();
		for( WZGStellzustand bp : message.getElementsList() ) {
			datasets.add( toDataset( bp ) );
		}
		
		if( !isStateFinished() ) {
			stateDatasets.addAll( datasets );
		} else {
			consumer.consume( datasets );
		}
	}
	
	public String getDatakind() {
		return DataHandler.DK_WZG_STELLZUSTAND;
	}
	
	protected Dataset toDataset( WZGStellzustand sz ) {
		Map<String,Value> values = new HashMap<>();
		values.put( "id", new Value( new DataString(  sz.getId() ) ) );
		
		values.put( "time", new Value( new DataLong(  sz.getTlsTime().getSeconds() ) ) );
		values.put( "stellcode", new Value( new DataInt(  sz.getStellcode() ) ) );
		values.put( "funktionsbyte", new Value( new DataInt(  sz.getFunktionsbyte() ) ) );
		values.put( "anzahl", new Value( new DataInt(  sz.getTextzeichen().length() ) ) );
		values.put( "textzeichen", new Value( new DataString(  sz.getTextzeichen() ) ) );
		
		return new DatasetImpl( DataHandler.DK_WZG_STELLZUSTAND, values );
	}
}
