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
public class TestAndCondition extends BaseT {

	@Autowired
	DataHandler dataHandler;
	
	@Autowired
	DataReceiver dataReceiver;
	
	
	@Test
	public void testAndCondition() {
		testAndCondition( 11, 12, false );	
		testAndCondition( 243, 0, false );	
		testAndCondition( 243, 243, true );	
		testAndCondition( 243, 243, true );	
		testAndCondition( 243, 241, false );	
		testAndCondition( 243, 243, true );	
		testAndCondition( 11, 12, false );	
		testAndCondition( 11, 12, false );	
	}
	
	@Test
	private void testAndCondition( int code1, int code2, boolean expectedResult ) {
		List<Dataset> dss = new ArrayList<>();

		{
			Map<String,Value> values = new HashMap<>();
			values.put( "id", new Value( new DataString(  "wzg.wzg9-125-nbg_w5_6.m.de" ) ) );
			values.put( "stellcode", new Value( new DataInt(  code1 ) ) );
			values.put( "funktionsbyte", new Value( new DataInt(  1 ) ) );
			values.put( "anzahl", new Value( new DataInt( 0 ) ) );
			DatasetImpl ds = new DatasetImpl( DataHandler.DK_WZG_STELLZUSTAND, values );
			dss.add( ds );
		}
		{
			Map<String,Value> values = new HashMap<>();
			values.put( "id", new Value( new DataString(  "wzg.wzg9-125-nbg_w6_7.m.de" ) ) );
			values.put( "stellcode", new Value( new DataInt(  code2 ) ) );
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
		Boolean cv = dataHandler.getRuleMan().getConditionValidity( "AQ9-125-Nbg" );
		assertTrue( Boolean.TRUE.equals( cv ) == expectedResult  );
	}
}
