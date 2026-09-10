package de.heuboe.sdbby.strategy.matching.kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import de.heuboe.kafka.listener.KafkaException;
import de.heuboe.kafka.listener.KafkaReceiverFactory.ListenInitPosition;
import de.heuboe.kafka.listener.KafkaReceiverSubscription;
import de.heuboe.kafka.listener.SpringKafkaProtoTypedReceiver;
import de.heuboe.sdbby.strategy.matching.DataConsumer;
import de.heuboe.sdbby.strategy.matching.Dataset;


/**
 * 
 * Base class of Kafka receivers
 * 
 * @author peters
 *
 * @param <T>	Type of received data
 * @param <S>	Type of sub items of received data (received data often is a list of items)
 */
public abstract class KafkaReceiver<T,S> extends SpringKafkaProtoTypedReceiver<T> {
	
	/**
	 * 
	 * Consructor
	 * 
	 * @param clast				Class of received data 
	 * @param rs				Subscription details
	 * @throws KafkaException	Error
	 */
	public KafkaReceiver(Class<T> clast, @NotNull KafkaReceiverSubscription rs) throws KafkaException {
		super(clast, rs);
	}

	protected DataConsumer consumer;
	protected List<Dataset> stateDatasets = new ArrayList<>();
	
	/**
	 * 
	 * Subscription of consumer
	 * 
	 * @param consumer	Data consumer
	 */
	public void subscribe( DataConsumer consumer ) {
		this.consumer = consumer;
	}

	protected abstract Dataset toDataset( S sz );

	@Override
	public void handleDeletion(String key, String topic, Map<String, String> headers) {
		// not called
	}

	@Override
	public ListenInitPosition getReceiveMode() {
		return ListenInitPosition.STATE;
	}
	
	@Override
	public void handleError(String key, String topic, Map<String,String> headers, byte[] data, String errorMsg) {
	}
	
	public abstract String getDatakind();

	@Override
	public void notifyStateFinished() {
		consumer.consumeState( getDatakind(), stateDatasets );
		stateDatasets.clear();
	}

}
