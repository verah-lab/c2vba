package de.heuboe.sdbby.strategy.matching;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import de.heuboe.data.Data;
import de.heuboe.data.DataReader;
import de.heuboe.data.DataStore;
import de.heuboe.data.Filter;
import de.heuboe.data.csv.CsvDataStore;
import de.heuboe.log.Logger;
import de.heuboe.sdbby.strategy.matching.data.ActivationCause;
import de.heuboe.sdbby.strategy.matching.data.Condition;
import de.heuboe.sdbby.strategy.matching.data.DeContent;
import de.heuboe.sdbby.strategy.matching.data.Requirement;
import de.heuboe.sdbby.strategy.matching.data.Rule;
import de.heuboe.sdbby.strategy.matching.data.RuleSet;


/**
 * 
 * Reads rule set file
 * 
 * @author peters
 *
 */
public class RuleReader {
	
	public static class RuleConfig {
		private RuleSet ruleSet;
		private Set<String> fairNames;

		public RuleConfig(RuleSet ruleSet, Set<String> fairNames) {
			super();
			this.ruleSet = ruleSet;
			this.fairNames = fairNames;
		}
		public RuleSet getRuleSet() {
			return ruleSet;
		}
		public void setRuleSet(RuleSet ruleSet) {
			this.ruleSet = ruleSet;
		}
		public Set<String> getFairNames() {
			return fairNames;
		}
		public void setFairNames(Set<String> fairNames) {
			this.fairNames = fairNames;
		}
	}
	
	private static final Logger LOGGER = Logger.getLogger(RuleReader.class);
	
	private static final String FAIR_NAMES_FILE = "MesseNamen";
	private static final String FAIR_NAMES = "<MesseName>";
	private static final String PROGRAM = "PROGRAM";
	private static final String MATCHLEVEL = "MATCHLEVEL";
	private static final String CONTENT = "CONTENT";
	private static final String STRATEGY = "STRATEGY";
	
	private static final String ODER = "ODER";
	private static final String UND = "UND";
	private static final String AND = "AND";
	
	private RuleReader() {
	}
	
	/**
	 * 
	 * Reads rule set file
	 * 
	 * @param filename		Rule file name
	 * @param fairNameFile	Fair file name
	 * @param fairNames		Fair names
	 * @return				Strategy rules
	 * @throws IOException 
	 */
	public static RuleConfig readRuleSetCSV( String filename ) throws IOException {  // NOSONAR: complexity acceptable
		
		Set<String> wzgIds = new HashSet<>();
		
        String fairNameFile = null;
        Set<String> fairNames = null;
		
		String filePath = ( new File( filename  ) ).getParent();
		String fileExt = ".txt";
		int pos = filename.indexOf( "_v" );
		if( pos != -1 ) {
			fileExt = filename.substring( pos );
		}
		
		File fairNamesFile = new File( filePath + File.separator + FAIR_NAMES_FILE + fileExt );
		if( fairNamesFile.exists() ) {
			fairNames = FileUtils.readLines( fairNamesFile, StandardCharsets.ISO_8859_1.toString()  )
								.stream().collect( Collectors.toSet() );
		} else {
			fairNamesFile = new File( filePath + File.separator + FAIR_NAMES_FILE + ".txt" );
			if( fairNamesFile.exists() ) {
				fairNames = FileUtils.readLines( fairNamesFile, StandardCharsets.ISO_8859_1.toString() )
									.stream().collect( Collectors.toSet() );
			}
		}
		
		Properties properties = new Properties();
		properties.setProperty(CsvDataStore.SEPARATOR_KEY, "\t");
		properties.setProperty(CsvDataStore.CHARSET_KEY, StandardCharsets.UTF_8.name() );
		
		DataStore store = new CsvDataStore(null,filename,properties);
		DataReader reader = store.getReader((Filter)null);

		RuleSet rSet = new RuleSet();
		Rule rule = null;
		Requirement req = null;
		Condition cond = null;
		DeContent content = null;
		int line = 1;
		while (reader.hasNext()) {
			Data record = reader.next();
			String type = record.getMember("TYPE").getAsString();
			String name = record.getMember("NAME").getAsString();
			Integer matchLevel = null;
			String logicalOperator = "";
			
			String causeDe = "";
			String causeEn = "";
			
			if( record.getMember(MATCHLEVEL).isNull() ) {
				LOGGER.fatal( filename + ": too few values in line " + line );
				throw new RuntimeException( "Fatal error!" );  // NOSONAR
			}
			
			if (!record.getMember(MATCHLEVEL).getAsString().isEmpty()) {
				if( type.equals( CONTENT ) ) {   
					logicalOperator = record.getMember(MATCHLEVEL).getAsString();
					if( !logicalOperator.equals( ODER ) && !logicalOperator.equals( AND ) && !logicalOperator.equals( UND ) ) {
						LOGGER.fatal( filename + ": illegal logical operator " + logicalOperator + " in line " + line + ": " );
						throw new RuntimeException( "Fatal error!" );  // NOSONAR
					}
				} else if( type.equals( STRATEGY ) ) { 
					causeDe = record.getMember(MATCHLEVEL).getAsString();
				} else {
					double ml = record.getMember(MATCHLEVEL).getAsDouble();
					matchLevel = (int)Math.round( ml * 100.0 );
				}
			}
			String de = record.getMember("DE").getAsString();
			
			if( type.equals( STRATEGY ) && ( de !=null ) && !de.isEmpty() ) {
				causeEn = de;
			}
			
			Integer prg = null;
			
			if( record.getMember(PROGRAM).isNull() ) {
				LOGGER.fatal( filename + ": too few values in line " + line + ": " );
				throw new RuntimeException( "Fatal error!" );  // NOSONAR
			}
			
			if (!record.getMember(PROGRAM).getAsString().isEmpty()) {
				prg = record.getMember(PROGRAM).getAsInt();
			}
			String text = null;
			if( !record.getMember("TEXT").isNull() && !record.getMember("TEXT").getAsString().isEmpty() ) {
				text = record.getMember("TEXT").getAsString();
				
				if( text.equals( FAIR_NAMES ) &&
				    ( ( ( fairNames == null ) || fairNames.isEmpty() ) && ( fairNameFile == null ) ) ) {
					throw new RuntimeException( FAIR_NAMES  + " is used, but no fair names defined!" );   // NOSONAR
				}
			}
			switch(type) {
				case "RULE":
					rule = new Rule(name, null, new ArrayList<>(), null, matchLevel, null);
					rSet.getRule().add(rule);
					break;
				case STRATEGY:
					if( rule == null ) {
						throw new RuntimeException( "No rule for strategy '" + name + "'" );  // NOSONAR
					}
					rule.setStrategy(name);
					
					if( ( causeDe !=null ) && !causeDe.isEmpty() ) {
						rule.getCause().add( new ActivationCause( "de", causeDe ) );
					}
					if( ( causeEn !=null ) && !causeEn.isEmpty() ) {
						rule.getCause().add( new ActivationCause( "en", causeEn ) );
					}
					
					break;
				case "MAIN_ROUTE_STRATEGY":
					if( rule == null ) {
						throw new RuntimeException( "No rule for strategy '" + name + "'" );  // NOSONAR
					}
					rule.setMainRouteStrategy(name);
					break;
				case "REQUIREMENT":
					req = new Requirement(name, matchLevel, null);
					if( rule == null ) {
						throw new RuntimeException( "No rule for strategy '" + name + "'" );  // NOSONAR
					}
					rule.getRequirement().add(req);
					break;
				case "CONDITION":
					cond = new Condition(name, null);
					req.getCondition().add(cond);    // NOSONAR
					break;
				case CONTENT:
					prg = ( prg == null ) ? -1 : prg;
					
					if( ( de == null ) || de.isEmpty() ) {
						LOGGER.fatal( filename + ": no DE defined in (CONTENT) line " + line );
						throw new RuntimeException( "Fatal error!" );  // NOSONAR
					}
					
					content = new DeContent(de, logicalOperator, prg, text);
					cond.getDeContent().add( content );    // NOSONAR
					wzgIds.add( de );
					break;
				default:
					break;
			}
			
			line++;
		}
		return new RuleConfig( rSet, fairNames);
	}
}
