package de.heuboe.sdbby.strategy.matching;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import de.heuboe.log.Logger;
import de.heuboe.wls.iface.Fault;
import eu.vmis_ehe.vmis2.configservice.CfgAq;
import eu.vmis_ehe.vmis2.configservice.CfgWzg;
import eu.vmis_ehe.vmis2.configservice.CfgWzgs;
import eu.vmis_ehe.vmis2.geomanager.strategy.Strategy;

/**
 * The configuration consists of all configured www panel sets, www panels, www prisms and all strategies.
 * It is retrieved from the config service and the web location service.
 * 
 * @author ralfz
 *
 */
public class Config {
	
	/**
	 * 
	 * WZG-Info
	 * 
	 * @author peters
	 *
	 */
	public static class WzgInfo {
		private String de;
		private String aqId;
		private Integer knNr;
		private String uzName;

		/**
		 * 
		 * Constructor 
		 * 
		 * @param de		DE-Nummer
		 * @param aqId		AQ ID
		 * @param knNr		Knotennummer
		 * @param uzName	UZ-Name	
		 */
		public WzgInfo( String de, String aqId, Integer knNr, String uzName) {
			super();
			this.de = de;
			this.aqId = aqId;
			this.knNr = knNr;
			this.uzName = uzName;
		}
		
		public String getAqId() {
			return aqId;
		}
		public Integer getKnNr() {
			return knNr;
		}
		public String getUzName() {
			return uzName;
		}

		public String getDe() {
			return de;
		}
	}

	private static final Logger LOGGER = Logger.getLogger(Config.class);
	
	private Map<String, WWWPrisma> allPrismenById = new HashMap<>();
	private Map<String, String> prismId2AqId = new HashMap<>();
	
	private Map<String, WzgInfo> wzgId2WzgInfo = new HashMap<>();
	
	Map<String, Strategie> strategien;
	boolean failed = false;
	
	
	/**
	 * Constructor. It reads all the needed configuration from the config service and the GeoManager
	 * 
	 * @param configService     The config service
	 * @param geoManager 		GeoManager
	 * @param kri2UzNameFile 	UZ names for KRIs
	 * @throws Fault 			the exception indicates an error in the configuration
	 */
	public Config( ConfigService configService, 
			       GeoManager geoManager,
			       String kri2UzNameFile ) { // NOSONAR

		readAllPrismen( configService );
		readStrategien(geoManager);
		
		readWzgInfos( configService, kri2UzNameFile );
		
		LOGGER.info( "Prisms and strategies read.");
	}
	
	
	private void readWzgInfos( ConfigService configService, String kri2UzNameFile ) {
		Map<String,String> kriId2UzName = readKri2UzNames( kri2UzNameFile );
		
		Map<String,String> wzgId2De = configService.getWzgId2De();
		Map<String,Integer> wzgId2KnNr = configService.getWzgId2KnNr();
		Map<Integer,String> knNr2KriId = configService.getKnNr2KriId();
		
		for( Map.Entry<String, String> entry : wzgId2De.entrySet() ) {
			String wzgId = entry.getKey();
			String de = entry.getValue();
			
			Integer knNr = wzgId2KnNr.get( wzgId );
			String kriId = null;
			String uzName = "";
			if( knNr != null ) {
				kriId = knNr2KriId.get( knNr );
				if( kriId != null ) {
					uzName = kriId2UzName.get( kriId );
					if( uzName != null ) {
						uzName += " [" + kriId + "]";
					} else  {
						uzName = "[" + kriId + "]";
					}
				} 
			}
			
			wzgId2WzgInfo.put( wzgId, new WzgInfo( de, getAqId( wzgId ), knNr, uzName) );
		}
	}
	
	private Map<String,String> readKri2UzNames( String kri2UzNameFile ) {
		Map<String,String> kri2UzName = new HashMap<>();
		
		if( ( kri2UzNameFile != null ) && !kri2UzNameFile.isEmpty() ) {
			try {
				List<String> lines = FileUtils.readLines( new File( kri2UzNameFile ), StandardCharsets.ISO_8859_1 );
				for( String line : lines ) {
					String[] parts = line.split( ";" );
					if( ( parts != null ) && ( parts.length == 2 ) ) {
						kri2UzName.put( parts[0], parts[1] );
					}
				}
			} catch( IOException ex ) {
				LOGGER.warn( "Error reading '" + kri2UzNameFile + "': " + ex.toString() );
			}
		}
		
		return kri2UzName;
	}
		


	
	private void readAllPrismen( ConfigService configService ) {
		List<CfgAq> aqs = configService.getAQs( null );
		
		for( CfgAq aq : aqs ) {
			CfgWzgs wzgs = aq.getWzgs();
			
			
			for( CfgWzg wzg : wzgs.getWzgsList() ) {
				String id = wzg.getId();
				String name = wzg.getName();
	
				WWWPrisma prisma = new WWWPrisma(id, name );
				allPrismenById.put(id, prisma);
				prismId2AqId.put( id, aq.getId() );
			}
		}
	}

	private void readStrategien( GeoManager geoManager ) {
		strategien = new HashMap<>();
		
		List<Strategy> strategies = geoManager.getStrategies();
		
		for(Strategy strategiy : strategies ) {
			String id = strategiy.getId();
			String name = strategiy.getName();
			Strategie strategie = new Strategie(id, name);
			strategien.put(id,  strategie);
		}
	}

	/**
	 * Getter for a www prism by the permanent id. 
	 * @param id the permanent id
	 * @return the www prism
	 */
	public WWWPrisma getPrisma(String id) {
		
		return allPrismenById.get( id );
	}
	
	/**
	 * Get number of www prisms
	 * @return 
	 */
	public int getNumPrisms() {

		return allPrismenById.size();
	
	}

	/**
	 * Get all prism IDs
	 * @return 
	 */
	public Collection<String> getPrismIds() {

		return allPrismenById.keySet();
	
	}
	

	/**
	 * Get all prism IDs
	 * @return 
	 */
	public Collection<String> getAllPrismIds() {

		return prismId2AqId.keySet();
	
	}

	/**
	 * 
	 * Get AQ ID of WZG
	 * 
	 * @param prismId   WZG ID
	 * @return  		AQ ID
	 */
	public String getAqId( String prismId ) {
		return prismId2AqId.get( prismId );
	}

	public WzgInfo getWzgInfo( String wzgId ) {
		return wzgId2WzgInfo.get( wzgId );
	}

	
	/**
	 * Getter for a strategy by the id.
	 * @param id the id
	 * @return the strategy
	 */
	public Strategie getStrategy(String id) {
		return strategien.get(id);
	}
	
	/**
	 * Getter for all configured strategies.
	 * @return the collection of all strategies
	 */
	public Collection<Strategie> getStrategies() {
		return strategien.values();
	}
	
	/**
	 * 
	 * Register alls WZG IDs from strategy rules
	 * 
	 * @param ids	WZG IDs
	 */
	public void registerRulePrismaIds( Set<String> ids ) {
		
		Set<String> allPrismenIds = new HashSet<>( allPrismenById.keySet() );
		for( String apid : allPrismenIds ) {
			if( !ids.contains( apid )) {
				allPrismenById.remove( apid );
			}
		}
	}
}
