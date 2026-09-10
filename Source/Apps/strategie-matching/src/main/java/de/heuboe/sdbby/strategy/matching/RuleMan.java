package de.heuboe.sdbby.strategy.matching;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import de.heuboe.data.Data;
import de.heuboe.data.DataStore;
import de.heuboe.data.DataWriter;
import de.heuboe.data.Factory;
import de.heuboe.data.Type;
import de.heuboe.data.csv.CsvDataStore;
import de.heuboe.data.csv.CsvFactory;
import de.heuboe.log.Logger;
import de.heuboe.sdbby.strategy.matching.RuleReader.RuleConfig;
import de.heuboe.sdbby.strategy.matching.data.Condition;
import de.heuboe.sdbby.strategy.matching.data.DeContent;
import de.heuboe.sdbby.strategy.matching.data.MainRoute;
import de.heuboe.sdbby.strategy.matching.data.MainRouteSet;
import de.heuboe.sdbby.strategy.matching.data.Requirement;
import de.heuboe.sdbby.strategy.matching.data.Rule;
import de.heuboe.sdbby.strategy.matching.data.RuleSet;
import de.heuboe.system.CallStack;

/**
 * The rule manager reads all configured rules, checks them against the supplied configuration
 * and implements a method to evaluate the state of all strategies.
 * 
 * @author ralfz
 *
 */
public class RuleMan {

	private static final Logger LOGGER = Logger.getLogger(RuleMan.class);

	private static final String JAXB_PACKAGES = "de.heuboe.sdbby.strategy.matching.data";
	private static final String CONTENT_LINE_START = "CONTENT\t\t\t";
	
	private static final String MATCHES = " matches";
	private static final String NOT = " NOT.";
	private static final String FAIR_NAMES = "<MesseName>";
	private static final String CONDITION = "Condition ";
	private static final String PROGRAM = "PROGRAM";
	private static final String MATCHLEVEL = "MATCHLEVEL";
	private static final String CONTENT = "CONTENT";
	
	private static final String ODER = "ODER";
	private static final String UND = "UND";
	private static final String AND = "AND";

	private RuleSet ruleSet;
	private MainRouteSet mainRouteSet = new MainRouteSet();
	private Config config;
	private boolean first = true;
	
	private Set<String> fairNames = new HashSet<>();
	
	private Map<String,Boolean> conditionValidity = new HashMap<>();
	private Map<String,Boolean> strategyValidity = new HashMap<>();
	
	
	/**
	 * Default constructor. should only be used for junit tests.
	 */
	public RuleMan() {
	}
	
	/**
	 * Constructor. The rule set is read from the given file and then it is checked against 
	 * the given configuration.
	 * 
	 * @param config the configuration
	 * @param filename the name of the file containing the rule set
	 * @exception IOException Error
	 */
	public RuleMan(Config config, String filename ) throws IOException {
		
		this.config = config;
		if (filename.toLowerCase().endsWith(".xml")) {
			ruleSet = readRuleSetXML(filename);
		}
		if (filename.toLowerCase().endsWith(".txt")) {
			ruleSet = readRuleSetCSV( filename, null );
		}
		if (ruleSet == null) {
			throw new IllegalArgumentException("RuleMan: only XML and CSV supported.");
		}
		
		buildMainRoutes();
		
		if (!checkRules()) {
			throw new IllegalArgumentException("RuleMan: check rules failed.");
		}
		if (!checkStrategies()) {
			throw new IllegalArgumentException("RuleMan: check strategies failed.");
		}
	}
	
	/**
	 * 
	 * Returns current validity of condition with name 'name'
	 * 
	 * @param name	Condition name
	 * @return		true: condition is currently true
	 */
	public Boolean getConditionValidity( String name ) {
		return conditionValidity.get( name );
	}
	
	public Boolean getStrategyValidity( String name ) {
		return strategyValidity.get( name );
	}

	
	private void buildMainRoutes() {
		Map<String, List<String> > mrs2ss = new HashMap<>();
		for( Rule rule : ruleSet.getRule() ) {
			String mrs = rule.getMainRouteStrategy();
			if( mrs != null ) {
				
				if( config.getStrategy( mrs ) == null ) {
					String error = "MAIN_ROUTE_STRATEGY of rule <" + rule.getName() + "> is not a valid strategy ID!";
					throw new IllegalArgumentException( error );
				}
				
				mrs2ss.computeIfAbsent( mrs, m -> new ArrayList<>() ).add( rule.getStrategy() );
			}
		}
		
		LOGGER.info( "MainRoute strategies:");
		for( Map.Entry<String, List<String> > entry : mrs2ss.entrySet() ) {
			MainRoute mr = new MainRoute();
			mr.setMainRouteStrategy( entry.getKey() );
			mr.getReferencingStrategy().addAll( entry.getValue() );
			
			LOGGER.info( "" );
			LOGGER.info( "    " + entry.getKey() );
			for( String r : entry.getValue() ) {
				LOGGER.info( "      referencing: " + r );
			}
			
			mainRouteSet.getMainRoute().add( mr );
		}
	}

	/**
	 * evaluates new states for all strategies.
	 * @return true if at least one state of a strategy has changed
	 */
	public boolean evaluateStrategies() {   // NOSONAR: complexity acceptable
		
		LOGGER.info("*** evaluateStrategies() called. ***");
		// first remember current states and set all strategies to inactive
		Map<String, Boolean> currentStates = new HashMap<>();
		for(Strategie strategy : config.getStrategies()) {
			currentStates.put(strategy.getId(), strategy.isActive());
			strategy.setActive(false);
		}
		// then evaluate new states
		for(Strategie strategy : config.getStrategies()) {
			boolean active = false;
			for(Rule rule : strategy.getRules()) {
				if (evaluateRule(rule)) {
					active = true;
				}
			}
			strategy.setActive(active);
			
			strategyValidity.put( strategy.getId(), active );
		}
		
		// check if main route strategy is active (if all its referencing are not active)
		
		for( MainRoute mr : mainRouteSet.getMainRoute()  ) {
			boolean refIsActive = false;
			for( String rs : mr.getReferencingStrategy() ) {
				Strategie s = config.getStrategy( rs );
				if( ( s != null ) && Boolean.TRUE.equals( s.isActive() ) ) {
					refIsActive = true;
					break;
				}
			}
			
			if( !refIsActive ) {
				Strategie s = config.getStrategy( mr.getMainRouteStrategy() );
				if( s != null ) {
					if( !s.isActive() ) {
						LOGGER.info("");
						LOGGER.info("Strategy " + s.getId() + " set to active as main route (all referencing strategies are not active)");
					}
					s.setActive( true );
				}
			}
		}
		
		// check if a state has changed
		for(Strategie strategy : config.getStrategies()) {
			Boolean oldState = currentStates.get(strategy.getId());
			if (strategy.isActive() != Boolean.TRUE.equals(oldState) ) {
				return true;
			}
		}
		if (first) {
			first = false;
			return true;
		}
		return false;
	}
	
	private boolean evaluateRule(Rule rule) {
		LOGGER.info("+++ evaluate Rule " + rule.getName() + " +++");
		int cnt = 0;
		for(Requirement requirement : rule.getRequirement()) {
			if (evaluateRequirement(requirement)) {
				++cnt;
			}
		}
		int matchLevel = rule.getMatchLevel() != null ? rule.getMatchLevel() : 100;
		LOGGER.info("Rule " + rule.getName() + " matches " + cnt + " of " + rule.getRequirement().size() +
				" requirements.");
		boolean ok = cnt * 100 / rule.getRequirement().size() >= matchLevel; 
		LOGGER.info("Rule " + rule.getName() + MATCHES  + (ok ? "." : NOT));
		return ok;
	}

	private boolean evaluateRequirement(Requirement requirement) {
		LOGGER.debug("--- evaluate Requirement " + requirement.getName() + " ---");
		int cnt = 0;
		for(Condition condition : requirement.getCondition()) {
			if (evaluateCondition(condition)) {
				++cnt;
			}
		}
		int matchLevel = requirement.getMatchLevel() != null ? requirement.getMatchLevel() : 100;
		LOGGER.debug("Requirement " + requirement.getName() + " matches " + cnt + " of " + requirement.getCondition().size() +
				" conditions.");
		boolean ok = cnt * 100 / requirement.getCondition().size() >= matchLevel;
		LOGGER.debug("Requirement " + requirement.getName() + MATCHES  + (ok ? "." : NOT));
		return ok;
	}
	
	/**
	 * 
	 * Checks if displayed text matches strategy condition name
	 * 
	 * @param strategyText		strategy text
	 * @param wzgText			Current WZG text
	 * @param fns				fairy names
	 * @return					true: current WZG text matches strategy (or fairy) name
	 */
	public static boolean match( String strategyText, String wzgText, Set<String> fns ) {

		Set<String> stexts = new TreeSet<>();
		if( strategyText.equals( FAIR_NAMES )  ){
			stexts.addAll( fns );
		} else {
			stexts.add( strategyText );
		}
		
		String wt = wzgText.toLowerCase().replace( " ", "" );
		
		for( String st : stexts ) {
			String s = st.toLowerCase().replace( " ", "" );
			if( wt.length() >= s.length() ) {
				if( s.equals( wt.substring(0, s.length() ) ) ) {    // NOSONAR
					return true;
				}
			}
		}
		return false;
	}
	
	// Is condition an AND combination of deContents?
	// (Assumes that 
	private boolean isConjunction( Condition condition ) {
		for( DeContent content : condition.getDeContent() ) {
			if( AND.equals( content.getLogicalOperator() ) || UND.equals( content.getLogicalOperator() ) ) {
				return true;
			}
		}
		return false;
	}

	private boolean evaluateCondition(Condition condition) {  // NOSONAR: complexity acceptable
		boolean ok = false;
		boolean wrong = false;
		for(DeContent content : condition.getDeContent()) {
			String wwwDeId = content.getWwwDe();
			WWWPrisma prisma = config.getPrisma(wwwDeId);
			
			if( prisma != null ) {
			
				boolean validProgramNumber = ( content.getProgramNumber() == -1 )  ||
									         ( ( prisma.getStellcode() == content.getProgramNumber() ) && prisma.validState() );
				
				
				boolean validText = ( content.getDisplayedText() == null ) ||
						            ( 
						              ( prisma.getDisplayedText() != null ) &&
						              match( content.getDisplayedText(), prisma.getDisplayedText(), fairNames ) && 
						              prisma.validState() 
						            );
				
				if( validProgramNumber && validText ) {
					LOGGER.debug("Prisma " + prisma.getName() + " matches.");
					ok = true;
				} else {
					LOGGER.debug("Prisma " + prisma.getName() +  " matches NOT.");
					wrong = true;
				}
				if( content.getProgramNumber() != -1 ) {
					LOGGER.debug( "(actual: " + prisma.getStellcode() + ", expected: " + content.getProgramNumber() + ")" );
				}
				if( content.getDisplayedText() != null ) {
					LOGGER.debug( "actual: <" + ( ( prisma.getDisplayedText() == null ) ? "!UNKNOWN!" : prisma.getDisplayedText() ) + 
							     ">, expected: <" + content.getDisplayedText() + ">)" );
				}
			
			}
		}
		
		
		if( isConjunction( condition ) ) {
			LOGGER.debug( CONDITION + condition.getName() + MATCHES + (!wrong ? "." : NOT));
			
			conditionValidity.put( condition.getName(), !wrong ); 
			
			return !wrong;
		} else {
			LOGGER.debug( CONDITION + condition.getName() + MATCHES + (ok ? "." : NOT));
			
			conditionValidity.put( condition.getName(), ok ); 
			
			return ok;
		}
	}

	private boolean checkStrategies() {
		boolean ok = true;
		for(Strategie strategy : config.getStrategies()) {
			if (strategy.getRules().isEmpty()) {
				LOGGER.fatal("Strategiy " + strategy.getId() + " has no rules");
				
				ok = false;
			}
		}
		return ok;
	}
	
	private boolean checkRules() {    // NOSONAR: complexity acceptable
		boolean ok = true;
		Set<String> rulesPrismaIds = new HashSet<>();
		for(Rule rule : ruleSet.getRule()) {
			String strategyId = rule.getStrategy();
			Strategie strategy = config.getStrategy(strategyId);
			if (strategy == null) {
				LOGGER.fatal("Strategy " + strategyId + " is not configured");
				ok = false;
			} else {
				strategy.getRules().add(rule);
			}
			for(Requirement requirement : rule.getRequirement()) {
				for(Condition condition : requirement.getCondition()) {
					Set<String> los = new HashSet<>();
					int i = 0;
					for(DeContent content : condition.getDeContent() ) {
						
						if( i > 0 ) {
							String lo = content.getLogicalOperator();
							if( lo == null || lo.isEmpty() ) {
								lo = ODER;
							}
							los.add( lo );
						}
						WWWPrisma prisma = config.getPrisma(content.getWwwDe());
						if (prisma == null) {
							LOGGER.fatal("DeContent Prisma " + content.getWwwDe() + " is not configured");
							ok = false;
						}
						
						rulesPrismaIds.add( content.getWwwDe() );
						
						i++;
					}
					
					if( los.size() > 1 ) {
						LOGGER.fatal( "Condition " + condition.getName() + " of rule " + rule.getName() + 
								      " has inconsistent logical operators: " + 
								      los.stream().collect( Collectors.joining(", " ) ) );
						ok = false;
					}
				}
			}
			
		}
		
		config.registerRulePrismaIds( rulesPrismaIds );

		return ok;
	}

	void saveRuleSetXML(RuleSet config, String xmlFilename) {
		JAXBContext jc;
		try {
			jc = JAXBContext.newInstance(JAXB_PACKAGES);
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			ClassLoader classLoader = getClass().getClassLoader();
			URL xsdUrl = classLoader.getResource("rules.xsd");

			SchemaFactory sf = SchemaFactory
					.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = sf.newSchema(xsdUrl);
			marshaller.setSchema(schema);

			QName name = new QName("http://sdbby.heuboe.de/strategy/matching/data", config.getClass().getSimpleName());
			try (FileOutputStream file = new FileOutputStream(xmlFilename)) {
				JAXBElement<RuleSet> element = new JAXBElement<>(name, RuleSet.class, null, config);
				marshaller.marshal(element, file);
			}
		} catch (JAXBException | IOException | SAXException e) {
			LOGGER.error( "cannot save xml configuration: " + e );
		}
	}

	RuleSet readRuleSetXML(String xmlFilename) {
		try ( FileInputStream is = new FileInputStream(xmlFilename); ) {
			

			JAXBContext context = JAXBContext.newInstance(JAXB_PACKAGES);
			Unmarshaller unmarshaller = context.createUnmarshaller();

			ClassLoader classLoader = getClass().getClassLoader();
			URL xsdUrl = classLoader.getResource("rules.xsd");

			SchemaFactory sf = SchemaFactory
					.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = sf.newSchema(xsdUrl);
			unmarshaller.setSchema(schema);

			JAXBElement<RuleSet> element = unmarshaller.unmarshal(new StreamSource(is), RuleSet.class);

			return element.getValue();
		} catch (JAXBException | SAXException | IOException e) {
			LOGGER.error( "cannot load xml configuration: " + e );
			return null;
		}
	}
	
	/**
	 * 
	 * Replaces old IDs by new ones
	 * 
	 * @param idConvFile			ID conversion file
	 * @param ruleFile				Rule file
	 * @param convertedRuleFile		Result rule file
	 * @throws IOException			Error
	 */
	public static void convertRuleFile( String idConvFile, String ruleFile, String convertedRuleFile  ) throws IOException {
		
		List<String> idLines = FileUtils.readLines( new File( idConvFile ), StandardCharsets.UTF_8 );
		
		LOGGER.info( "Start ID conversion of rule file <" + ruleFile + ">" );

		Map<String,String> id2newId = new HashMap<>();
		for( String cl : idLines ) {
			String[] parts = cl.split( ";" );
			if( ( parts != null ) && ( parts.length >= 2 ) )  {
				id2newId.put( parts[1], parts[0] );
			}
 		}
		
		List<String> convertedRuleLines = new ArrayList<>();
		List<String> ruleLines = FileUtils.readLines( new File( ruleFile ), StandardCharsets.UTF_8 );
		
		for( String rl : ruleLines ) {   // NOSONAR
			if( rl.startsWith( CONTENT_LINE_START ) ) {
				String part = rl.substring( CONTENT_LINE_START.length() );
				int endOfWzgId = part.indexOf( '\t' );
				if( endOfWzgId == -1 ) {
					convertedRuleLines.add( rl );
					LOGGER.warn( "    Line <" + rl + ">: cannot find WZG ID" );
					continue;
				}
				
				String id = part.substring( 0, endOfWzgId );
				String newId = id2newId.get( id );
				if( newId == null ) {
					convertedRuleLines.add( rl );
					LOGGER.warn( "    Line <" + rl + ">: no new ID for <" + id + ">" );
					continue;
				}
				
				String crl = CONTENT_LINE_START + newId + part.substring( endOfWzgId );
				convertedRuleLines.add( crl );
			} else {
				convertedRuleLines.add( rl );
			}
		}
		
		FileUtils.writeLines( new File(convertedRuleFile) ,
                              StandardCharsets.UTF_8.name(), 
                              convertedRuleLines );
		
		LOGGER.info( "Converted rule content written to <" + convertedRuleFile + ">" );
	}

	RuleSet readRuleSetCSV( String filename, String fairNameFile  ) throws IOException {  
		
		RuleConfig rc = RuleReader.readRuleSetCSV( filename );
		fairNames = rc.getFairNames();
		
		return rc.getRuleSet();
	}

	void saveRuleSetCSV(RuleSet config, String filename) {
		new CsvFactory();
		Factory factory = Factory.Singleton.getInstance();
		Map<String, Type> members = new LinkedHashMap<>();
		members.put("TYPE", factory.getStringType(64));
		members.put("NAME", factory.getStringType(64));
		members.put(MATCHLEVEL, factory.getStringType(64));
		members.put("DE", factory.getStringType(64));
		members.put(PROGRAM, factory.getStringType(64));

		Type type = factory.getType("csv", "RuleSet", members, "", "");
		Properties props = new Properties();
		props.setProperty(CsvDataStore.SEPARATOR_KEY, "\t");
		props.setProperty(CsvDataStore.CHARSET_KEY, "utf-8");
		DataStore store = factory.createNewDataStore("csv", type, filename, props);
		DataWriter writer = store.getWriter();

		Data record;
		for(Rule rule : config.getRule()) {
			record = createRecord(type, "RULE", rule.getName());
			setMatchingLevel(record, rule.getMatchLevel());
			writer.add(record);
			record = createRecord(type, "STRATEGY", rule.getStrategy());
			writer.add(record);
			for(Requirement req : rule.getRequirement()) {
				record = createRecord(type, "REQUIREMENT", req.getName());
				setMatchingLevel(record, req.getMatchLevel());
				writer.add(record);
				for(Condition cond : req.getCondition()) {
					record = createRecord(type, "CONDITION", cond.getName());
					writer.add(record);
					for(DeContent content : cond.getDeContent()) {
						record = createRecord(type, CONTENT, "");
						record.getMember("DE").setFromString(content.getWwwDe());
						record.getMember(PROGRAM).setFromInt(content.getProgramNumber());
						writer.add(record);
					}
				}
			}
		}
		writer.close();
	}

	private void setMatchingLevel(Data record, Integer matchLevel) {
		if (matchLevel != null) {
			record.getMember(MATCHLEVEL).setFromInt(matchLevel);
		}
	}

	private Data createRecord(Type type, String typeName, String name) {
		Data record = type.createData();
		record.getMember("TYPE").setFromString(typeName);
		record.getMember("NAME").setFromString(name);
		return record;
	}

	/**
	 * Getter for the configuration.
	 * @return the configuration
	 */
	public Config getConfig() {
		return config;
	}

	public static void main(String[] args) {  // NOSONAR
		try {
			convertRuleFile( "idConversion/FG4-ID-Lookup.csv", 
					         "src/main/resources/rules/StrategieRegeln_v0.19.txt", 
					         "src/main/resources/rules/StrategieRegeln-converted_v0.19.txt" );
		} catch ( IOException ex ) {
			System.out.println( "Conversion failed" );  					// NOSONAR
			System.out.println( CallStack.getStackTraceAsString( ex ) );  	// NOSONAR
		}
	}	
	
}
