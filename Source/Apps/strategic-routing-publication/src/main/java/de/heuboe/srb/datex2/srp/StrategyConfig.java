package de.heuboe.srb.datex2.srp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import de.heuboe.sdbby.strategy.matching.RuleReader;
import de.heuboe.sdbby.strategy.matching.RuleReader.RuleConfig;
import de.heuboe.sdbby.strategy.matching.data.ActivationCause;
import de.heuboe.sdbby.strategy.matching.data.Rule;

/**
 * 
 * Strategy configuration
 * 
 * @author peters
 *
 */
public class StrategyConfig {
	
	/**
	 * 
	 * Strategy cause
	 * 
	 * @author peters
	 *
	 */
	public static class StrategyCause {
		private String nameDe;
		private String nameEn;
		private Set<String> strategyIds;
		
		/**
		 * 
		 * Constructor
		 * 
		 * @param nameDe		German name
		 * @param nameEn		Englisch name
		 * @param strategyIds	Strategy IDs	
		 */
		public StrategyCause( String nameDe, String nameEn,
				              Set<String> strategyIds) {
			super();
			this.nameDe = nameDe;
			this.nameEn = nameEn;
			this.strategyIds = strategyIds;
		}

		public StrategyCause() {
		}

		public String getNameDe() {
			return nameDe;
		}
		public void setNameDe(String nameDe) {
			this.nameDe = nameDe;
		}
		public String getNameEn() {
			return nameEn;
		}
		public void setNameEn(String nameEn) {
			this.nameEn = nameEn;
		}
		public Set<String> getStrategyIds() {
			return strategyIds;
		}
		public void setStrategyIds(Set<String> strategyIds) {
			this.strategyIds = strategyIds;
		}
		
		/**
		 * 
		 * Checks if cause applies to strategy (ID)
		 * 
		 * @param strategyId	Strategy ID
		 * @return true/false
		 */
		public boolean applies( String strategyId ) {
			return strategyIds.contains( strategyId );
		}
	}
	
	private List<StrategyCause> strategyCauses = new ArrayList<>();
	private Map<String,StrategyCause> strategyId2Causes = new HashMap<>();
	private Map<String,String> strategyId2Comment = new HashMap<>();

	public void init( String ruleFile, String causeFile ) throws IOException, SRPException {   // NOSONAR
		
		List<String> lines = null;
		if( ( causeFile == null ) || causeFile.isEmpty() ) {
			 InputStream is = StrategyConfig.class.getResourceAsStream( "/config/strategyCause.txt" );
			 lines = IOUtils.readLines( is, StandardCharsets.UTF_8 );
		} else {
			 lines = IOUtils.readLines( new FileInputStream( new File(causeFile) ), StandardCharsets.UTF_8 );
		}
		
		String causeName = null;
		String nameDe = null;
		String nameEn = null;
		Set<String> strategyIds = new HashSet<>();
		for( String line : lines ) {
			if( line.startsWith( "Cause" ) ) {
				if( causeName != null ) {
					strategyCauses.add( new StrategyCause( nameDe, nameEn, strategyIds ) );
				}
				causeName = line;
				
			}
			if( line.startsWith( "nameDe" ) ) {
				String[] parts = line.split( "=" );
				if( parts.length != 2 ) {
					throw new SRPException( SRPException.ERROR_ARG_PARSER, "Invalid line <" + line + "> in strategyCause.txt" );  // NOSONAR
				}
				nameDe = parts[1];
			}
			if( line.startsWith( "nameEn" ) ) {
				String[] parts = line.split( "=" );
				if( parts.length != 2 ) {
					throw new SRPException( SRPException.ERROR_ARG_PARSER, "Invalid line <" + line + "> in strategyCause.txt" );   // NOSONAR
				}
				nameEn = parts[1];
			}
			if( line.startsWith( "strategies" ) ) {
				String[] parts = line.split( "=" );
				if( parts.length != 2 ) {
					throw new SRPException( SRPException.ERROR_ARG_PARSER, "Invalid line <" + line + "> in strategyCause.txt" );   // NOSONAR 
				}
				String[] sIds = parts[1].split( "," ); 
				strategyIds = Arrays.<String>asList( sIds ).stream().map( String::trim ).collect( Collectors.toSet() );
			}
		}
		if( causeName != null ) {
			strategyCauses.add( new StrategyCause( nameDe, nameEn, strategyIds ) );
		}
		
		if( ( ruleFile == null ) || ruleFile.isEmpty() ) {
			throw new SRPException( SRPException.ERROR_ARG_PARSER, "No strategy rule file ( de.heuboe.sdbby.srp.strategyRuleFile) defined!" );   // NOSONAR 
		}
		
		RuleConfig rc = RuleReader.readRuleSetCSV( ruleFile );
		
		for( Rule rule : rc.getRuleSet().getRule() ) {
			List<ActivationCause> acs = rule.getCause();
			if( ( acs != null ) && ( acs.size() == 2 ) ) {
				StrategyCause sc = new StrategyCause( acs.get(0).getDescription(), acs.get(1).getDescription(), 
						                              new HashSet<>( Arrays.asList( rule.getStrategy() ) ) );
				strategyId2Causes.put( rule.getStrategy(), sc );
			}
			
			String rn = rule.getName();
			if( isC2VBAStrategy( rn ) ) {
				int pos = rn.indexOf( ' ' );
				if( pos != -1 ) {
					rn = rn.substring( pos + 1 ); 
				}
				strategyId2Comment.put( rule.getStrategy(), rn );
			}
		}
	}
	
	/**
	 * 
	 * Retrieves strategy cause
	 * 
	 * @param strategyId		Strategy ID
	 * @return					Cause
	 */
	public StrategyCause getStrategyCause( String strategyId ) {
		
		StrategyCause strategyCause = strategyId2Causes.get( strategyId );
		if( strategyCause != null ) {
			return strategyCause;
		}
		
		for( StrategyCause sc : strategyCauses ) {
			if( sc.applies( strategyId ) ) {
				return sc;
			}
		}
		
		return null;
	}
	
	public String getStrategyComment( String strategyId ) {
		return strategyId2Comment.get( strategyId );		
	}
	
	/**
	 * 
	 * Checks for C2VBA ID
	 * 
	 * @param strategyId  Strategy ID
	 * @return true/false
	 */
	public static boolean isC2VBAStrategy( String strategyId ) {
		return strategyId.startsWith( "DE-" );
	}
	
	/**
	 * 
	 * Get main part of C2VBA strategy ID
	 * 
	 * @param strategyId  Strategy ID
	 * @return Main part of C2VBA strategy ID
	 */
	public static String getC2VBAStrategyIdMainPart( String strategyId ) {
		int pos = strategyId.indexOf( '.' );
		if( pos != -1 ) {
			return strategyId.substring( 0, pos );
		}
		
		return strategyId;
	}
}
