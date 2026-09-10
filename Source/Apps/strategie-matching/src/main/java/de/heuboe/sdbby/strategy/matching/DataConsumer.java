package de.heuboe.sdbby.strategy.matching;

import java.util.List;

/**
 * 
 * Data consumer
 * 
 * @author peters
 *
 */
public interface DataConsumer {
	/**
	 * 
	 * Consumes state data for a single datakind
	 * 
	 * @param datakind  Datakind name
	 * @param datasets  Datasets
	 */
	void consumeState( String datakind, List<Dataset> datasets );
	
	/**
	 * 
	 * Consumes data received after state delivery
	 * 
	 * @param datasets	Datasets
	 */
	void consume( List<Dataset> datasets );
	
	/**
	 * 
	 * Error notification
	 * 
	 * @param errorCode	Error code
	 * @param errorMsg	Error message
	 */
	default void error( int errorCode, String errorMsg ) {
	}
}

