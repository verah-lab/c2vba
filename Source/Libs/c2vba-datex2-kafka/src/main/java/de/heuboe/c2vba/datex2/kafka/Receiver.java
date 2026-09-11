package de.heuboe.c2vba.datex2.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import de.heuboe.c2vba.data.DatexIIContent;
import de.heuboe.c2vba.datex2.kafka.Partitioner.AssembleResult;
import de.heuboe.kafka.listener.KafkaException;
import de.heuboe.kafka.listener.KafkaReceiverSubscription;
import de.heuboe.kafka.listener.SpringKafkaProtoTypedReceiver;
import de.heuboe.log.Logger;

/**
 * 
 * @author peters
 *
 * @param <T>
 */
public abstract class Receiver<T> extends SpringKafkaProtoTypedReceiver<DatexIIContent> {
	
	private static final Logger LOGGER = Logger.getLogger( Receiver.class );
	
	protected Resolver<T> resolver;
	private Partitioner partitioner;
	private Map<String,DatexIIContent> currentParts = new HashMap<>();
	
	public Receiver( Class<DatexIIContent> clast, 
			         @NotNull KafkaReceiverSubscription rs,
			         Resolver<T> resolver ) throws KafkaException {
		super(clast, rs);
		this.resolver = resolver;
		partitioner = new Partitioner();
	}
	
	/**
	 * 
	 * Initial state messages
	 * 
	 * @param messages	
	 */
	public abstract void handleStateMessages( List<T> messages ); 
	public abstract void handleMessages( List<T> messages ); 
	
	/**
	 * 
	 * Processes DatexIIContent data
	 * 
	 * @param key		Message key
	 * @param topic		Topic
	 * @param message	Message	
	 */
	public synchronized void handleMessage(String key, String topic, Map<String,String> headers, DatexIIContent message ) {
		currentParts.put( Partitioner.getKey( message ), message );
		
		if( isStateFinished() ) {
			
//			LOGGER.info( "upd: " + message.getChunkNum() + "/" + message.getChunkCount() ); 
//			if( message.getChunkCount() == message.getChunkNum() ) {
//				LOGGER.info( "count == number" ); 
//			}				

			
			if( partsCompleted( message ) ) {
				AssembleResult<T> ar = partitioner.assemble( currentParts.values(), resolver );
				
				if( !ar.getInvalidIds().isEmpty() ) {
					for( Map.Entry<String,String> entry : ar.getInvalidIds().entrySet() ) {
						handleError( key, topic, headers, null, "Content of message with ID " + entry.getKey() + " invalid: " + entry.getValue() );
					}
				}
				
				if( !ar.getIncompleteIds().isEmpty() ) {
					String incompleteIds = ar.getIncompleteIds().stream().collect( Collectors.joining(", ") );
					handleError( key, topic, headers, null, "IDs with incomplete content: " + incompleteIds );
				}
				
				handleMessages( resolver.sortByTime( ar.getObjects() ) );
				
				currentParts.clear();
			}
		}
	}

	@Override
	public void notifyStateFinished() {
		AssembleResult<T> ar = partitioner.assemble( currentParts.values(), resolver );
		
		if( !ar.getInvalidIds().isEmpty() ) {
			for( Map.Entry<String,String> entry : ar.getInvalidIds().entrySet() ) {
				handleError( "", rs.getTopic(), new HashMap<>(), null, "Content of message with ID " + entry.getKey() + " invalid: " + entry.getValue() );
			}
		}
		
		if( !ar.getIncompleteIds().isEmpty() ) {
			String incompleteIds = ar.getIncompleteIds().stream().collect( Collectors.joining(", ") );
			handleError( "", rs.getTopic(), new HashMap<>(), null, "IDs with incomplete content: " + incompleteIds );
		}
		
		handleStateMessages( resolver.sortByTime( ar.getObjects() ) );
		currentParts.clear();
	}

	
	public void handleError( String key, String topic, Map<String,String> headers, byte[] data, String errorMsg ) {
		LOGGER.warn( "Error receiving Kafka messages: " + errorMsg );
	}
	
	@Override
	public synchronized void handleDeletion(String key, String topic, Map<String, String> headers) {
		currentParts.remove( key );
	}
	
	private boolean partsCompleted( DatexIIContent message ) {
		
		String id = message.getId();
		long count = currentParts.values().stream().filter( c -> c.getId().equals(id) ).count();
		return ( message.getChunkCount() == count ) && ( message.getChunkNum() == count );
		//return !currentParts.isEmpty() && ( currentParts.values().iterator().next().getChunkCount() == count );
	}
}
