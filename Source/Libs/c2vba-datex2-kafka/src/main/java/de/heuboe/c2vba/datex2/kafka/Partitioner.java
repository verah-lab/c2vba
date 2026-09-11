package de.heuboe.c2vba.datex2.kafka;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.heuboe.c2vba.data.DatexIIContent;
import de.heuboe.vmis2.jprotoc.utils.DateUtils;


/**
 * 
 * Partitions a DATEX-II publication into parts (of type DatexIIContent)
 * (and re-assembles parts to a publication)
 * 
 * @author peters
 *
 */
public class Partitioner {
	
	private static final int MAX_PART_SIZE = 10000;
	
	public static class AssembleResult<T> {
		private List<String> incompleteIds = new ArrayList<>();
		private Map<String,String> invalidIds = new HashMap<>();
		private List<T> objects = new ArrayList<>();
		
		public Map<String,String> getInvalidIds() {
			return invalidIds;
		}
		public List<T> getObjects() {
			return objects;
		}
		public List<String> getIncompleteIds() {
			return incompleteIds;
		}
	}

	public <T> List<DatexIIContent> partition( T object, Resolver<T> resolver )  throws C2VbaException {
		String id = resolver.getId( object );
		String version = resolver.getVersion( object );
		String content = resolver.getContent( object );
		
		List<DatexIIContent> parts = new ArrayList<>();
		int numParts = 1;
		if( content.length() > 0 ) {
			numParts = ( content.length() - 1 ) / MAX_PART_SIZE + 1;
		}
		
		for( int i = 0; i < numParts; i++ ) {
			DatexIIContent.Builder part = DatexIIContent.newBuilder();
			part.setId( id );
			part.setChunkCount( numParts );
			part.setChunkNum( i + 1 );

			part.setIid( Partitioner.getKey( part ) );
			part.setVersion( version );
			
	        Instant instant = Instant.ofEpochMilli( resolver.getTime( object ).getTime() );
			part.setTime( DateUtils.fromInstantUtc( instant ) );
			part.setPack( object.getClass().getPackageName() );
			part.setType( object.getClass().getSimpleName() );
			String partCo = content.substring(  i * MAX_PART_SIZE, Math.min( ( i + 1 ) * MAX_PART_SIZE, content.length() ) );
			part.setXmlContent( partCo );
			
			parts.add( part.build() );
		}
		
		return parts;
	}
	
	public <T> AssembleResult<T> assemble( Collection<DatexIIContent> parts, Resolver<T> resolver ) {
		
		Map<String, List<DatexIIContent> > id2Parts = new HashMap<>();
		parts.forEach( part -> id2Parts.computeIfAbsent( part.getId(), p -> new ArrayList<>() ).add( part ) );
		
		AssembleResult<T> result = new AssembleResult<>();
		
		for( Map.Entry<String, List<DatexIIContent> > entry : id2Parts.entrySet() ) {
			
			Map<String,DatexIIContent> key2Part = new HashMap<>();
			entry.getValue().forEach( p -> key2Part.put( getKey(p) , p ) );
			
			List<DatexIIContent> orderedParts = key2Part.values().stream()
					    .sorted( (p1,p2) -> Integer.compare( p1.getChunkNum(), p2.getChunkNum() ) )
					    .collect( Collectors.toList() );
			
			if( orderedParts.get(0).getChunkCount() != orderedParts.size() ) {
				result.getIncompleteIds().add( orderedParts.get(0).getId() );
			} else {
				String content = "";
				for( DatexIIContent part : orderedParts ) {
					content += part.getXmlContent();
				}
				
				try {
					T object = resolver.getObject( content );
					result.getObjects().add( object );
				} catch ( C2VbaException ex ) {
					result.getInvalidIds().put( orderedParts.get(0).getId(), ex.toString() );
				}
			}
		}
		
		return result;
	}
	
	public static String getKey( DatexIIContent content ) {
		return content.getId() + "__" + content.getChunkNum();
	}
	
	public static String getKey( DatexIIContent.Builder content ) {
		return content.getId() + "__" + content.getChunkNum();
	}
	
	public static String getId( String key ) {
		int pos = key.lastIndexOf( "__" );
		if( pos != -1 ) {
			return key.substring( 0, pos );
		}
		return key;
	}

	
	public static Map< String, Set<String> > toIdKeyMap( Set<String> keys ) {
		Map< String, Set<String> > idKeyMap = new HashMap<>();
		
		for( String key : keys ) {
			String id = getId( key );
			idKeyMap.computeIfAbsent( id, i -> new HashSet<>() ).add( key );
		}
		
		return idKeyMap;
	}
}
