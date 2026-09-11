package de.heuboe.c2vba.datex2.kafka;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.heuboe.c2vba.data.DatexIIContent;
import de.heuboe.c2vba.data.pojo.PDatexIIContent;
import de.heuboe.kafka.listener.KafkaException;
import de.heuboe.kafka.listener.KafkaManager;
import de.heuboe.kafka.producer.KafkaProducerProto;
import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;


/**
 * 
 * Divides DATEX-II publications into DatexIIContent parts and puts them onto Kafka
 * 
 * a) The Kafka data stock consists of the part of the last received snapshot publication 
 *    and the parts of subsequent update publications.
 * 
 * b) When saving a snapshot publication (ID = 'PUB') all existing non-snapshot data is erased on Kafka
 * 
 * 
 * @author peters
 *
 * @param <T>
 */
public class Producer<T> {
	
	private static final Logger LOGGER = Logger.getLogger( Producer.class );
	
	private String name = "";
	private KafkaProducerProto<DatexIIContent,PDatexIIContent> kafkaProducer;
	private Partitioner partitioner = new Partitioner();
	
	private Map< String, Set<String> > objId2PartKeys = new HashMap<>();
	
	public Producer( KafkaProducerProto<DatexIIContent,PDatexIIContent> kafkaProducer ) {
		this.kafkaProducer = kafkaProducer;
	}
	
	public Producer( String name, KafkaProducerProto<DatexIIContent,PDatexIIContent> kafkaProducer ) {
		this.setName(name);
		this.kafkaProducer = kafkaProducer;
	}

	public Producer( KafkaProducerProto<DatexIIContent,PDatexIIContent> kafkaProducer, 
			         Map< String, Set<String> > objId2PartKeys ) {
		this.kafkaProducer = kafkaProducer;
		this.objId2PartKeys = objId2PartKeys;
	}
	
	public void init( KafkaManager kafkaManager, long timeout ) throws KafkaException {
		Set<String> keys = kafkaManager.getAllKeysProto( DatexIIContent.class, getTopic(), timeout );
		objId2PartKeys = Partitioner.toIdKeyMap( keys );
	}
	
	public String getTopic() {
		return kafkaProducer.getTopic();
	}
	
	public void setObjId2PartKeys( Map< String, Set<String> > objId2PartKeys ) {
		this.objId2PartKeys = objId2PartKeys;
	}

	/**
	 * 
	 * Partitions each object and publishes each part 
	 * 
	 * @param objects			Objects of type T
	 * @param resolver			Object resolver
	 * @param wholeStock		true: objects is the whole stock of objects (snapshot), 
	 *                                others still existing (in Kafka) are deleted  
	 * @throws C2VbaException	Error
	 */
	public void send( List<T> objects, Resolver<T> resolver, boolean wholeStock ) throws C2VbaException {
		
		try {
			Set<String> objIds = new HashSet<>();
			for( T object : objects ) {
				String objId = resolver.getId(object);
				
				List<DatexIIContent> contents = partitioner.partition( object, resolver );
				deleteKeys( contents, objId, resolver.isSnapshot( object ) );
				for( DatexIIContent content : contents ) {
					kafkaProducer.sendData( Partitioner.getKey( content ), content, new HashMap<>() );
				}
				
				registerKeys( contents );
				
				objIds.add( objId );
			}
			
			
		} catch( KafkaException ex ) {
			LOGGER.error( ex.toString() );
			LOGGER.error( CallStack.getStackTraceAsString( ex ) );
			throw new C2VbaException( C2VbaException.ERROR_SENDING, "Kafka error", ex );
		}
	}
	
	/**
	 * 
	 * Deletes keys which are not present in new parts ( contents )
	 * 
	 * @param contents	Object parts
	 */
	private void deleteKeys( List<DatexIIContent> contents, String id, boolean snapshot ) {
		Set<String> partKeys = contents.stream().map( c -> Partitioner.getKey( c ) ).collect( Collectors.toSet() );
		Set<String> currentPartKeys = objId2PartKeys.get( id );
		if( currentPartKeys != null ) {
			
			for( String k : currentPartKeys.stream().collect( Collectors.toSet() ) ) {
				if( !partKeys.contains( k ) ) { 
					kafkaProducer.deleteData( k, new HashMap<>() ); 
					currentPartKeys.remove( k );
				}
			}
		}
		
		if( snapshot ) {
			for( String key : objId2PartKeys.keySet().stream().collect( Collectors.toSet() ) ) {
				if( !key.equals( id ) ) {
					Set<String> pks = objId2PartKeys.get( key );
					if( pks != null ) {
						objId2PartKeys.remove( key );
						pks.forEach( k -> kafkaProducer.deleteData( k, new HashMap<>() ) );
					}
				}
			}
		}
	}
	
	private void registerKeys( List<DatexIIContent> contents ) {
		for( DatexIIContent content : contents ) {
			objId2PartKeys.computeIfAbsent( content.getId(), c -> new HashSet<>() ).add( Partitioner.getKey( content ) );
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
