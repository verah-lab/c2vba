package de.heuboe.c2vba.datex2.kafka;

import java.util.Date;
import java.util.List;

/**
 * 
 * Retrieves attributes of a DATEX-II object (e.g. of type D2LogicalModel or Situation)
 * 
 * @author peters
 *
 * @param <T>
 */
public interface Resolver<T> {

	String getId( T object ) throws C2VbaException;
	String getVersion( T object );
	Date getTime( T object ) throws C2VbaException;
	int getElementCount( T object ) throws C2VbaException;
	String getContent( T object ) throws C2VbaException;
	T getObject( String content ) throws C2VbaException;;
	String getSchema( T object );
	
	List<T> sortByTime( List<T> objects );
	
	// Only applies to T = D2LogicalModel
	default boolean isSnapshot( T object ) throws C2VbaException { return false; }; 
	default String getTableId( T object ) throws C2VbaException { return ""; }; 
	default String getTableVersion( T object ) throws C2VbaException { return ""; }; 
	
}
