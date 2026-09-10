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
import eu.vmis_ehe.vmis2.tls.received.WZGBetriebsart;
import eu.vmis_ehe.vmis2.tls.received.WZGBetriebsartList;

/**
 * 
 * Subscribes on WZGBetriebsart
 * 
 * @author peters
 *
 */
public class WZGBetriebsartReceiver extends KafkaReceiver<WZGBetriebsartList,WZGBetriebsart> {
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param rs				Subscription details
	 * @throws KafkaException	Error	
	 */
	public WZGBetriebsartReceiver( KafkaReceiverSubscription rs ) throws KafkaException {
		super( WZGBetriebsartList.class, rs );
	}
	
	@Override
	public void handleMessage(String key, String topic, Map<String,String> headers, WZGBetriebsartList message) {
		List<Dataset> datasets = new ArrayList<>();
		for( WZGBetriebsart bp : message.getElementsList() ) {
			datasets.add( toDataset( bp ) );
		}
		
		if( !isStateFinished() ) {
			stateDatasets.addAll( datasets );
		} else {
			consumer.consume( datasets );
		}
	}
	
	public String getDatakind() {
		return DataHandler.DK_BETRIEBS_ART;
	}
	
	protected Dataset toDataset( WZGBetriebsart bp ) {
		Map<String,Value> values = new HashMap<>();
		values.put( "id", new Value( new DataString(  bp.getId() ) ) );
		values.put( "WVZBetrArt", new Value( new DataInt(  bp.getBetriebsart() ) ) );
		
		return new DatasetImpl( DataHandler.DK_BETRIEBS_ART, values );
	}
}
