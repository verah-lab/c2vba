package de.heuboe.sdbby.strategy.matching;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;


public class MatchFairNameTest {
	
	@Test
	public void testMatchFairName() throws Exception {
		
		Set<String> fairNames = FileUtils.readLines( new File( "src/test/resources/rules/MesseNamen_v0.19.txt" ), StandardCharsets.ISO_8859_1.toString()  )
			                                          .stream().collect( Collectors.toSet() );

		assertTrue( RuleMan.match( "GBTA Conference", "GBTA Conference", fairNames ) );
		assertTrue( RuleMan.match( "GBTA Confer", "GBTA Conference", fairNames ) );
		assertFalse( RuleMan.match( "Heim + Handwerkk", "Heim + Handwerk", fairNames ) );
		assertTrue( RuleMan.match( "Heim +", "Heim + Handwerk", fairNames ) );
		assertFalse( RuleMan.match( "Heim +", "GBTA Conference", fairNames ) );
		assertTrue( RuleMan.match( "BAU", "BAU und", fairNames ) );
		assertTrue( RuleMan.match( "<MesseName>", "BAU und", fairNames ) );
		assertTrue( RuleMan.match( "<MesseName>", "GBTA Conference", fairNames ) );
	    assertFalse( RuleMan.match( "<MesseName>", "GBTA Conferrrrence", fairNames ) );
	    assertTrue( RuleMan.match( "<MesseName>", "Heim+Handwerk", fairNames ) );
	}
}
