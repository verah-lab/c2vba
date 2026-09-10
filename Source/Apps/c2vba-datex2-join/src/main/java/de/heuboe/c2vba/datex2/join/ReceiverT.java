package de.heuboe.c2vba.datex2.join;

import java.util.List;

import javax.validation.constraints.NotNull;

import de.heuboe.c2vba.data.DatexIIContent;
import de.heuboe.c2vba.datex2.kafka.Receiver;
import de.heuboe.c2vba.datex2.kafka.Resolver;
import de.heuboe.kafka.listener.KafkaException;
import de.heuboe.kafka.listener.KafkaReceiverFactory.ListenInitPosition;
import de.heuboe.kafka.listener.KafkaReceiverSubscription;

/**
 * 
 * Kafka receiver of publications
 * 
 * @author peters
 *
 * @param <T>	D2LogicalModel of a profile	
 */
public class ReceiverT<T> extends Receiver<T> {
	
	private String region;
	private Observer<T> observer;
	
	/**
	 * 
	 * Observer
	 * 
	 * @author peters
	 *
	 * @param <T>	Publication type
	 */
	public static interface Observer<T> {
		/**
		 * 
		 * Notify initial state
		 * 
		 * @param region		Region 
		 * @param publications  Publications
		 */
		void notifyState( String region, List<T> publications ); 
		
		/**
		 * 
		 * Notify update
		 * 
		 * @param region		Region 
		 * @param publications	Publications
		 */
		void notifyUpdate( String region, List<T> publications ); 
	}

	/**
	 * 
	 * Constructor
	 * 
	 * @param region			Region
	 * @param clast				DatexIIContent class
	 * @param rs				KafkaReceiverSubscription
	 * @param resolver			Resolver
	 * @throws KafkaException	Error
	 */
	public ReceiverT( String region,
			            Class<DatexIIContent> clast, 
			            @NotNull KafkaReceiverSubscription rs,
			            Resolver<T> resolver) throws KafkaException {
		super( clast, rs, resolver);
		this.region = region;
	}

	@Override
	public ListenInitPosition getReceiveMode() {
		return ListenInitPosition.STATE;
	}

	@Override
	public void handleStateMessages(List<T> publications ) {
		observer.notifyState( region, publications );
	}

	@Override
	public void handleMessages( List<T> publications) {
		observer.notifyUpdate( region, publications );
	}

	public void setObserver( Observer<T> observer ) {
		this.observer = observer;
	}
}
