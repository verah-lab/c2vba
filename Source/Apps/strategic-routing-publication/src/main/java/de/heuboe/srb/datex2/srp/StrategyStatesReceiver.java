package de.heuboe.srb.datex2.srp;

import java.util.Map;

import javax.validation.constraints.NotNull;

import de.heuboe.c2vba.data.StrategyStates;
import de.heuboe.kafka.listener.KafkaException;
import de.heuboe.kafka.listener.KafkaReceiverFactory.ListenInitPosition;
import de.heuboe.kafka.listener.KafkaReceiverSubscription;
import de.heuboe.kafka.listener.SpringKafkaProtoTypedReceiver;


/**
 * 
 * Base class of Kafka receivers
 * 
 * @author peters
 *
 * @param <T>	Type of received data
 * @param <S>	Type of sub items of received data (received data often is a list of items)
 */
public class StrategyStatesReceiver extends SpringKafkaProtoTypedReceiver<StrategyStates> {
	
	public interface Consumer {
		void consume( StrategyStates strategyStates );
	}
	
	private StrategyStates strategyStates = null;
	
	/**
	 * 
	 * Consructor
	 * 
	 * @param clast				Class of received data 
	 * @param rs				Subscription details
	 * @throws KafkaException	Error
	 */
	public StrategyStatesReceiver(@NotNull KafkaReceiverSubscription rs) throws KafkaException {
		super(StrategyStates.class, rs);
	}


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

	@Override
	public void notifyStateFinished() {
		if( strategyStates != null ) {
			consumer.consume( strategyStates );
		}
	}


	@Override
	public void handleMessage(String key, String topic, Map<String, String> headers, StrategyStates message) {
		strategyStates = message;
		if( isStateFinished() ) {
			consumer.consume( strategyStates );
		}
	}
	
	public void setConsumer( Consumer consumer ) {
		this.consumer = consumer;
	}

	private Consumer consumer;
}
