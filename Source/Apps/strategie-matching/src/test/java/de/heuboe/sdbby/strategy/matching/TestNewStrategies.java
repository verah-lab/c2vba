package de.heuboe.sdbby.strategy.matching;


import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import de.heuboe.data.impl.DataInt;
import de.heuboe.data.impl.DataString;
import de.heuboe.sdbby.strategy.matching.Dataset.Value;

@EnableAutoConfiguration
public class TestNewStrategies extends BaseT {

	@Autowired
	DataHandler dataHandler;
	
	@Autowired
	DataReceiver dataReceiver;
	
	
	@Test
	public void testCondition() {
		try {
			Thread.sleep( 3000L );   // NOSONAR
		} catch ( InterruptedException ex ) {
		}
		testCondition( 242, 242, 242, 0, false );	
		testCondition( 241, 241, 241, 1, false );	
		testCondition( 242, 241, 242, 1, false );	
		testCondition( 242, 242, 241, 1, false );	
		testCondition( 242, 242, 242, 1, true );	
	}
	
	@Test
	private void testCondition( int code1, int code2, int code3, int fb, boolean expectedResult ) {
		List<Dataset> dss = new ArrayList<>();

		{
			Map<String,Value> values = new HashMap<>();
			values.put( "id", new Value( new DataString(  "wzg.wzg_92_3720_mch_w1_226.m.de" ) ) );
			values.put( "stellcode", new Value( new DataInt(  code1 ) ) );
			values.put( "funktionsbyte", new Value( new DataInt(  fb ) ) );
			values.put( "anzahl", new Value( new DataInt( 0 ) ) );
			DatasetImpl ds = new DatasetImpl( DataHandler.DK_WZG_STELLZUSTAND, values );
			dss.add( ds );
		}
		{
			Map<String,Value> values = new HashMap<>();
			values.put( "id", new Value( new DataString(  "wzg.wzg_92_3720_mch_w2_227.m.de" ) ) );
			values.put( "stellcode", new Value( new DataInt(  code2 ) ) );
			values.put( "funktionsbyte", new Value( new DataInt(  1 ) ) );
			values.put( "anzahl", new Value( new DataInt( 0 ) ) );
			DatasetImpl ds = new DatasetImpl( DataHandler.DK_WZG_STELLZUSTAND, values );
			dss.add( ds );
		}
		{
			Map<String,Value> values = new HashMap<>();
			values.put( "id", new Value( new DataString(  "wzg.wzg_92_3210_mch_w3_228.m.de" ) ) );
			values.put( "stellcode", new Value( new DataInt(  code3 ) ) );
			values.put( "funktionsbyte", new Value( new DataInt(  1 ) ) );
			values.put( "anzahl", new Value( new DataInt( 0 ) ) );
			DatasetImpl ds = new DatasetImpl( DataHandler.DK_WZG_STELLZUSTAND, values );
			dss.add( ds );
		}
		
		dataReceiver.consume( dss );
		
		
		try {
			Thread.sleep( 3000L );   // NOSONAR
		} catch ( InterruptedException ex ) {
		}
		Boolean cv = dataHandler.getRuleMan().getStrategyValidity( "DE-10048" );
		assertTrue( Boolean.TRUE.equals( cv ) == expectedResult  );
	}
}
