package de.heuboe.sdbby.strategy.matching;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import de.heuboe.log.Logger;
import de.heuboe.sdbby.strategy.matching.kafka.WZGBetriebsartReceiver;
import de.heuboe.sdbby.strategy.matching.kafka.WZGDeFehlerReceiver;
import de.heuboe.sdbby.strategy.matching.kafka.WZGKanalsteuerungReceiver;
import de.heuboe.sdbby.strategy.matching.kafka.WZGStellzustandReceiver;


/**
 * 
 * Receives all relevant switching data and supplies to a consumer
 * 
 * @author peters
 *
 */
public class DataReceiver implements DataConsumer {
	
	private static final Logger LOGGER = Logger.getLogger(DataReceiver.class);

	private Map< String, List<Dataset> > stateDatsets = new HashMap<>();

    @Autowired 
    private WZGBetriebsartReceiver betriebsartReceiver;
    @Autowired 
    private WZGStellzustandReceiver stellzustandReceiver;
    @Autowired 
    private WZGKanalsteuerungReceiver kanalsteuerungReceiver;
    @Autowired 
    private WZGDeFehlerReceiver fehlerReceiver;
    
    private int numDatakinds = 4;
    
	private DataConsumer consumer;
	
	/**
	 * 
	 * Single receivers subscribe
	 * 
	 * @param consumer	DataConsumer
	 */
	public void subscribe( DataConsumer consumer ) {
		this.consumer = consumer;
		this.betriebsartReceiver.subscribe( this );
		this.stellzustandReceiver.subscribe( this );
		this.kanalsteuerungReceiver.subscribe( this );
		this.fehlerReceiver.subscribe( this );
	}
	
	/**
	 * 
	 * Single receivers start receiving data
	 * 
	 */
	public void start() {
		betriebsartReceiver.start();
		stellzustandReceiver.start();
		kanalsteuerungReceiver.start();
		fehlerReceiver.start();
	}

	@Override
	public void consume( List<Dataset> datasets) {
		consumer.consume( datasets );
	}
	
	@Override
	public void consumeState( String datakind, List<Dataset> datasets ) {
		if( stateDatsets.containsKey( datakind ) ) {
			LOGGER.warn( "state data for " + datakind + " already seen." );
		}
		LOGGER.info( "Initial state of " + datakind + " state: # " + datasets.size() + " messages received" );
		stateDatsets.put( datakind, datasets );
		
		
		if( stateDatsets.size() == numDatakinds ) {
			consumer.consume( stateDatsets.entrySet()
					                .stream()
					                .flatMap( e -> e.getValue().stream() )
					                .collect( Collectors.toList() ) );
		}
	}

	@Override
	public void error( int errorCode, String errorMsg ) {
		LOGGER.error( "Kafka data error (" + errorCode + "): " + errorMsg  );
	}

}

