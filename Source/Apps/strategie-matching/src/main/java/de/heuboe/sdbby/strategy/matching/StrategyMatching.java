package de.heuboe.sdbby.strategy.matching;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * This is the main class of the strategy matching. 
 * It creates the configuration and a rule manager and then it starts
 * a data receiver for receiving the states of the prisms.
 * The data receiver lets the rule manage evaluate the new states of the
 * strategies and if one strategy changes, the data receiver stores the
 * new states in database.
 * 
 * @author ralfz
 *
 */
public class StrategyMatching {

	@Autowired
	private ConfigService configService;
	
	@Value("${de.heuboe.sdbby.service.kri2UzNameFile:}")
	private String kri2UzNameFile;
	
	@Autowired
	private GeoManager geoManager;
	
	@Autowired
	private DataHandler dataHandler;
	
	private String rulesFilename;

	/**
	 * 
	 * Constructor
	 * 
	 * @param rulesFilename  Rule file name
	 */
	public StrategyMatching( String rulesFilename ) {
		this.rulesFilename = rulesFilename;
	}
	
	/**
	 * Init-method, invoked by spring.
	 * 
	 * @throws IOException Error
	 */
	public void init() throws IOException {
		Config cfg = new Config( configService, geoManager, kri2UzNameFile );
		RuleMan ruleMan = new RuleMan( cfg, rulesFilename );
		
		dataHandler.start( ruleMan );
	}

}
