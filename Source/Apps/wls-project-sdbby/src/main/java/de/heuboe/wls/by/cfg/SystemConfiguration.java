package de.heuboe.wls.by.cfg;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import de.heuboe.log.Logger;
import de.heuboe.wls.by.QFileReader;
import de.heuboe.wls.util.WlsException;
import eu.vmis_ehe.vmis2.configservice.CfgAq;
import eu.vmis_ehe.vmis2.configservice.CfgMq;
import eu.vmis_ehe.vmis2.configservice.CfgUdeSensor;
import eu.vmis_ehe.vmis2.configservice.CfgVdeSensor;
import eu.vmis_ehe.vmis2.configservice.CfgWzg;
import eu.vmis_ehe.vmis2.configservice.LanePos.LaneType;
import eu.vmis_ehe.vmis2.configservice.TlsDevice;
import eu.vmis_ehe.vmis2.configservice.TlsEa;
import eu.vmis_ehe.vmis2.configservice.TlsFg;



/**
 * 
 * System-Konfiguration (Querschnitte) 
 * 
 * @author peters
 *
 */
public class SystemConfiguration {
	
	
	/**
	 * 
	 * Configuration key attributes
	 * 
	 * @author peters
	 *
	 */
	public static class KnNrDeKey {
		private int knNr;
		private int deNr;

		/**
		 * 
		 * Constructor
		 * 
		 * @param knNr	Knoten-Nr
		 * @param deNr	De-Nummer	
		 */
		public KnNrDeKey(int knNr, int deNr) {
			super();
			this.knNr = knNr;
			this.deNr = deNr;
		}
		
		public int getKnNr() {
			return knNr;
		}
		public void setKnNr(int knNr) {
			this.knNr = knNr;
		}
		public int getDeNr() {
			return deNr;
		}
		public void setDeNr(int deNr) {
			this.deNr = deNr;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + deNr;
			result = prime * result + knNr;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			KnNrDeKey other = (KnNrDeKey) obj;
			if (deNr != other.deNr) {
				return false;
			}
			if (knNr != other.knNr) {  // NOSONAR
				return false;
			}
			return true;
		}
	}
	
	/**
	 * 
	 * Configuration key attributes
	 * 
	 * @author peters
	 *
	 */
	public static class CfgKey {
		private int knNr;
		private int fg;
		private Set<Integer> deNr;
		private String id;
		
		public CfgKey( String id, int knNr, int fg, Set<Integer> deNr) {   // NOSONAR
			super();
			this.setId(id);
			this.knNr = knNr;
			this.fg = fg;
			this.deNr = deNr;
		}
		public int getKnNr() {
			return knNr;
		}
		public void setKnNr(int knNr) {
			this.knNr = knNr;
		}
		public int getFg() {
			return fg;
		}
		public void setFg(int fg) {
			this.fg = fg;
		}
		public Set<Integer> getDeNr() {
			return deNr;
		}
		public void setDeNr(Set<Integer> deNr) {
			this.deNr = deNr;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((deNr == null) ? 0 : deNr.hashCode());
			result = prime * result + fg;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			result = prime * result + knNr;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			CfgKey other = (CfgKey) obj;
			if (deNr == null) {
				if (other.deNr != null) {
					return false;
				}
			} else if (!deNr.equals(other.deNr)) {
				return false;
			}
			if (fg != other.fg) {
				return false;
			}
			if (id == null) {
				if (other.id != null) {
					return false;
				}
			} else if (!id.equals(other.id)) {
				return false;
			}
			if (knNr != other.knNr) {    // NOSONAR
				return false;
			}
			return true;
		}
	}
	
	
	private static final String CANNOT_WRITE_FILE = "Cannot write file <";
	private static final Logger LOGGER = Logger.getLogger(SystemConfiguration.class);
	private static final String UNKNOWN = "UNKNOWN";
	private static final String WZG = "Wzg <";
	private static final String UFD = "UFD <";
    
	private ConfigService configService;
	private String wancomKeyFileDir;

	private Map<String, Q> qs = new TreeMap<>();
	private Map<Integer, List<Mq>> knNr2Mq = new HashMap<>();
	private Map<Integer, List<Aq>> knNr2Aq = new HashMap<>();
	private Map<Integer, List<Ufd>> knNr2Ufd = new HashMap<>();
	private Map<String,String> kri2UzName = new HashMap<>();
	
	private List<CfgKey> geoObjectKeys = new ArrayList<>();
	
	private boolean firstWancomStatistics = true;
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param configService		ConfigService
	 * @param kri2UzFile		File with UZ names for KRI names
	 * @param wancomKeyFileDir	Directory of Wancom TLS key files
	 * @throws IOException		I/O error	
	 */
	public SystemConfiguration( ConfigService configService, String kri2UzFile, String wancomKeyFileDir ) throws IOException {
		this.wancomKeyFileDir = wancomKeyFileDir;
		this.configService = configService;
		
		List<String> lines = FileUtils.readLines( new File( kri2UzFile ), StandardCharsets.ISO_8859_1 );
		for( String line : lines ) {
			String[] parts = line.split( ";" );
			if( ( parts != null ) && ( parts.length == 2 ) ) {
				kri2UzName.put( parts[0], parts[1] );
			}
		}
	}
	
	
	/**
	 * 
	 * Initialisation
	 * 
	 * @throws WlsException    Error
	 */
	public void init() throws WlsException {
		LOGGER.info("Retrieving MQs and UFDs from configuration");
		readQs();

        LOGGER.info("Reading of Q data finished, found " + qs.size() + " Q");
	}
	
	private void readQs() throws WlsException {	    // NOSONAR
		
		List<TlsDevice> tlsDevices = configService.getAllDevices( null );

		Map<String, Set<CfgKey> > objId2CfgKeys = new HashMap<>();

		// Collect all CfgKeys for each object ID
		for( TlsDevice tlsDevice : tlsDevices ) {
			for( TlsFg fg : tlsDevice.getFgsList() ) {
				for( TlsEa ea : fg.getEasList() ) {
					CfgKey key = new CfgKey( ea.getEaid(), 
							                 tlsDevice.getOsi7Address(), 
							                 fg.getNumber(), 
							                 new HashSet<>( Arrays.asList( ea.getDeNummer() ) ) );
					
					objId2CfgKeys.computeIfAbsent( ea.getEaid(), id -> new HashSet<>()  ).add( key );
				}
			}
		}
		
		
		List<CfgAq> cfgAqs = configService.getAllAQs( null );
		for( CfgAq cfgAq : cfgAqs ) {
			Set<Integer> knNrs = new HashSet<>();
			Set<Integer> wzgDes = new HashSet<>();
			Set<Integer> nonClusterDes = new HashSet<>();
			for( CfgWzg wzg : cfgAq.getWzgs().getWzgsList() ) {
				Set<CfgKey> cfgKeys = objId2CfgKeys.get( wzg.getId() );
				if( cfgKeys == null ) {
					LOGGER.warn(WZG + wzg.getId() + "> not in device configuration"); 			// NOSONAR
				} else if( cfgKeys.size() > 1 ) {
					LOGGER.warn(WZG + wzg.getId() + "> has multiple device configurations");  	// NOSONAR
				} else {
					wzgDes.add( cfgKeys.iterator().next().getDeNr().iterator().next() );
					nonClusterDes.add( cfgKeys.iterator().next().getDeNr().iterator().next() );
					knNrs.add( cfgKeys.iterator().next().getKnNr() );
				}
			}
			
			
			for( String cid : cfgAq.getClusterIdsList() ) {
				Set<CfgKey> cfgKeys = objId2CfgKeys.get(cid );
				if( cfgKeys == null ) {
					LOGGER.warn(WZG + cid + "> not in device configuration"); 					// NOSONAR
				} else if( cfgKeys.size() > 1 ) {
					LOGGER.warn(WZG + cid + "> has multiple device configurations");  			// NOSONAR
				} else {
					wzgDes.add( cfgKeys.iterator().next().getDeNr().iterator().next() );
					knNrs.add( cfgKeys.iterator().next().getKnNr() );
				}
				
			}
			
			if( knNrs.isEmpty() ) {
				LOGGER.warn("AQ <" + cfgAq.getId() + "> has no device configurations");			// NOSONAR
			} else if( knNrs.size() > 1 ) {
				LOGGER.warn("AQ <" + cfgAq.getId() + "> has multiple device configurations");
			} else {
				int knNr = knNrs.iterator().next();
				Aq aq = new Aq( cfgAq.getId(), cfgAq.getLocation().getRoadId(), null, knNr, -1, wzgDes );
				aq.setNonClusterDes( nonClusterDes );
				qs.put( cfgAq.getId(), aq );
				knNr2Aq.computeIfAbsent( knNr, m -> new ArrayList<>() ).add( aq );
			}
		}


		List<CfgMq> cfgMqs = configService.getAllMQs( null );
		for( CfgMq cfgMq : cfgMqs ) {
			Set<Integer> knNrs = new HashSet<>();
			Set<LaneType> laneTypes = new HashSet<>();
			Set<Integer> sensorDes = new HashSet<>();
			for( CfgVdeSensor sensor : cfgMq.getVdeSensors().getSensorsList() ) {
				Set<CfgKey> cfgKeys = objId2CfgKeys.get( sensor.getId() );
				if( cfgKeys == null ) {
					LOGGER.warn("VdeSensor <" + sensor.getId() + "> not in device configuration");
				} else if( cfgKeys.size() > 1 ) {
					LOGGER.warn("VdeSensor <" + sensor.getId() + "> has multiple device configurations");  // NOSONAR
				} else {
					laneTypes.add( sensor.getLanePos().getLaneType() );
					sensorDes.add( cfgKeys.iterator().next().getDeNr().iterator().next() );
					knNrs.add( cfgKeys.iterator().next().getKnNr() );
				}
			}
			
			if( knNrs.isEmpty() ) {
				LOGGER.warn("MQ <" + cfgMq.getId() + "> has no device configurations");
			} else if( knNrs.size() > 1 ) {
				LOGGER.warn("MQ <" + cfgMq.getId() + "> has multiple device configurations");
			} else {
				
				int knNr = knNrs.iterator().next();
				int firstDe = sensorDes.iterator().next();
				int deNr = firstDe / 8 * 8 + 7;
				Mq mq = new Mq( cfgMq.getId(), cfgMq.getLocation().getRoadId(), null, knNr, deNr, sensorDes, laneTypes.iterator().next() );
				qs.put( cfgMq.getId(), mq );
				knNr2Mq.computeIfAbsent( knNr, m -> new ArrayList<>() ).add( mq );
			}
		}

			
		List<CfgUdeSensor> cfgUfds = configService.getAllUFDs( null );
		for( CfgUdeSensor cfgUfd : cfgUfds ) {     // NOSONAR
			Set<Integer> knNrs = new HashSet<>();
			
			Set<CfgKey> cfgKeys = objId2CfgKeys.get( cfgUfd.getId() );
			if( cfgKeys == null ) {
				LOGGER.warn(UFD + cfgUfd.getId() + "> not in device configuration");
				continue;
			} else if( cfgKeys.size() > 1 ) {
				LOGGER.warn(UFD + cfgUfd.getId() + "> has multiple device configurations");
				continue;
			} else {
				knNrs.add( cfgKeys.iterator().next().getKnNr() );
			}
			
			if( knNrs.size() != 1 ) {
				LOGGER.warn(UFD + cfgUfd.getId() + "> has no device configurations");
			} else {
				
				int knNr = knNrs.iterator().next();
				int deNr = cfgKeys.iterator().next().getDeNr().iterator().next();
				Ufd ufd = new Ufd( cfgUfd.getType(), 
						           cfgUfd.getId(), 
						           cfgUfd.getLocation().getRoadId(), 
						           null, 
						           knNr,
						           deNr );
				qs.put( cfgUfd.getId(), ufd );
				knNr2Ufd.computeIfAbsent( knNr, m -> new ArrayList<>() ).add( ufd );
			}
		}
		
		LOGGER.info( cfgMqs.size() + " MQs in configuration" );
		LOGGER.info( cfgAqs.size() + " AQs in configuration" );
		LOGGER.info( cfgUfds.size() + " UFDs in configuration" );

	}

	class EqLaneData {
		Set<String> laneIds;
		int glaneType;
	}

	public Map<String, Q> getQs() {
		return qs;
	}
	
	/**
	 * 
	 * Get Q for ID
	 * 
	 * @param id			ID
	 * @return				Q
	 */
	public Q getQ( String id ) {
		return qs.get(id);
	}

	
	/**
	 * 
	 * Get Q for Knoten-Nr., De-Nr. and FG
	 * 
	 * @param knNr     Knoten-Nr.
     * @param deNr     De-Nr.
     * @param fg       Funktionsgruppe
	 * @return         Q
	 */

	public Q getQ( int knNr, int deNr, int fg ) {    // NOSONAR
		
	    if( fg == 1 ) {
	    	List<Mq> mqs = knNr2Mq.get( knNr );
	    	
	    	if( mqs != null ) {
		    	for( Mq mq : mqs ) {
		    		if( mq.getLaneDes().contains( deNr ) ) {
		    			return mq;
		    		}
		    	}
	    	}
	    } else if( fg == 3 ) {
	    	List<Ufd> ufds = knNr2Ufd.get( knNr );
	    	if( ufds != null ) {
		    	for( Ufd ufd : ufds ) {
		    		if( ufd.getDeNr() == deNr ) {
		    			return ufd;
		    		}
		    	}
	    	}
	    } else if( fg == 4 ) {
	    	List<Aq> aqs = knNr2Aq.get( knNr );
	    	
	    	if( aqs != null ) {
		    	for( Aq aq : aqs ) {
		    		if( aq.getWzgDes().contains( deNr ) ) {
		    			return aq;
		    		}
		    	}
	    	}
	    }	 	    
	    
	    return null;
	}
	
	public List<Q> getAllFg4Q( int knNr, int deNr ) {    // NOSONAR
		
		List<Q> aqsWithDe = new ArrayList<>();
		
    	List<Aq> aqs = knNr2Aq.get( knNr );
    	if( aqs != null ) {
	    	for( Aq aq : aqs ) {
	    		if( aq.getWzgDes().contains( deNr ) ) {
	    			aqsWithDe.add( aq );
	    		}
	    	}
    	}
	    
	    return aqsWithDe;
	}

	
	/**
	 * 
	 * Get Q for Knoten-Nr. and FG
	 * 
	 * @param knNr     Knoten-Nr.
     * @param fg       Funktionsgruppe
	 * @return         Q
	 */

	public Q getFirstQ( int knNr, int fg ) {
		
		for( int de = 1; de <= 255; de++ ) {
			Q q = getQ( knNr, de, fg );
			if( q != null ) {
				return q;
			}
		}
		
		return null;
	}
	
	/**
	 * 
	 * Check if there is only a single Q for knNr
	 * 
	 * @param knNr     Knoten-Nr.
     * @param fg       Funktionsgruppe
	 * @return         true/false
	 */

	public boolean hasSingleQ( int knNr, int fg ) {
		
	    if( fg == 1 ) {
	    	List<Mq> mqs = knNr2Mq.get( knNr );
	    	if( mqs != null ) {
	    		return mqs.size() == 1;
	    	}
	    } else if( fg == 3 ) {
	    	List<Ufd> ufds = knNr2Ufd.get( knNr );
	    	if( ufds != null ) {
	    		return ufds.size() == 1;
	    	}
	    } else if( fg == 4 ) {
	    	List<Aq> aqs = knNr2Aq.get( knNr );
	    	if( aqs != null ) {
	    		return aqs.size() == 1;
	    	}	    	
	    }		
		return false;
	}


	
	/**
	 * 
	 * Add config keys of objects from coordinate table
	 * 
	 * @param goks	config keys of objects from coordinate table	
	 */
	public void addCfgKeys( List<CfgKey> goks ) {
		this.geoObjectKeys.addAll( goks );
	}
	
	/**
	 * 
	 * Clear config keys
	 * 
	 */
	public void clearCfgKeys() {
		this.geoObjectKeys.clear();
	}


	
	/**
	 * 
	 * Logs configuration objects without coordinate data
	 * 
	 * @param fg 			Funktionsgruppe
	 * @param logWarning 	true: log all CfgObjects without coordinate data 
	 * @return				CfgObjects without coordinate data
	 * 
	 */
	public List<Q> detectMissingCoordinateData( int fg, boolean logWarning ) {
		List<Q> mcos = getMissingObjects( geoObjectKeys, fg );
		
		if( logWarning ) {
			if( !mcos.isEmpty() ) {  // NOSONAR
				LOGGER.warn( "No coordinate data for configuration objects [FG" + fg +  "]:");
				
				if( fg == 4 ) {
					LOGGER.warn( "    id;knNr;fg;wzgDes;road") ;
					for( Q q : mcos ) {
						Aq mco = (Aq)q;
						String wzgDes = mco.getWzgDes().stream().map(d -> "" + d ).collect( Collectors.joining(",") );
						LOGGER.warn( "    " + mco.getId() + ";" + 
											  mco.getKnNr() + ";" + 
											  "FG" + mco.getFg() + ";" + 
											  wzgDes + ";" + 
											  mco.getRoad() );
					}
				} else {
					LOGGER.warn( "    id;knNr;fg;deNr;road") ;
					for( Q mco : mcos ) {
						LOGGER.warn( "    " + mco.getId() + ";" + 
											  mco.getKnNr() + ";" + 
											  "FG" + mco.getFg() + ";" + 
											  mco.getDeNr() + ";" + 
											  mco.getRoad() );
					}
				}
			}
		}
		
		return mcos;
	}
	
	
	
	/**
	 * 
	 * Logs coordinate data without corresponding configuration object 
	 * 
	 * @param fg 		Funktionsgruppe
	 * 
	 */
	public void detectUnknownCoordinateObjects( int fg ) {
		List<CfgKey> cks = getUnknownObjects( geoObjectKeys, fg );
		if( !cks.isEmpty() ) {
			LOGGER.warn( "For these coordinate data objects there is no configuration object:") ;
			for( CfgKey ck : cks ) {
				LOGGER.warn( "    " + ck.getId() );
			}
		}
	}
	
	private List<Q> getMissingObjects( List<CfgKey> cfgKeys, List<? extends Q> qs ) {     // NOSONAR
		
		List<Q> mos = new ArrayList<>();
		
		if( cfgKeys == null ) {
			mos.addAll( qs );
		} else if( qs != null ) {
			for( Q q : qs ) {
				boolean found = false;
				for( CfgKey cfgKey : cfgKeys ) {
					Set<Integer> cfgKeyDes = new HashSet<>( cfgKey.getDeNr() ); 
					cfgKeyDes.removeAll( q.getDefiningDes() );
					if( cfgKeyDes.size() < cfgKey.getDeNr().size() )  {
						found = true;
						break;
					}
				}
				if( !found ) {
					mos.add( q );
				}
			}
			
		}
		
		return mos;
		
		
	}	

	/**
	 * 
	 * Returns configuration objects for which no key in cfgKeys is found
	 * 
	 * @param cfgKeys	Config keys of objects supplied in coordinate table
	 * @param fg		Funktionsgruppe
	 * @return			Configuration objects not found in set 'cfgKeys'
	 */
	public List<Q> getMissingObjects( List<CfgKey> cfgKeys, int fg ) {     // NOSONAR
		List<Q> missingObjects = new ArrayList<>();
		
		Map<Integer, List<CfgKey> > knNr2CfgKeys = new HashMap<>(); 
		for( CfgKey cfgKey : cfgKeys ) {
			knNr2CfgKeys.computeIfAbsent( cfgKey.getKnNr(), c -> new ArrayList<>() ).add( cfgKey );
		}
		
		if( fg == 1 ) {
			for( Map.Entry< Integer, List<Mq> > entry : knNr2Mq.entrySet() ) {
				int knNr = entry.getKey();
				List<Q> mos = getMissingObjects( knNr2CfgKeys.get( knNr ), entry.getValue() );
				missingObjects.addAll( mos );
			}
		} else if( fg == 4 ) {
			for( Map.Entry< Integer, List<Aq> > entry : knNr2Aq.entrySet() ) {
				int knNr = entry.getKey();
				List<Q> mos = getMissingObjects( knNr2CfgKeys.get( knNr ), entry.getValue() );
				missingObjects.addAll( mos );
			}
		} else if( fg == 3 ) {
			for( Map.Entry< Integer, List<Ufd> > entry : knNr2Ufd.entrySet() ) {
				int knNr = entry.getKey();
				List<Q> mos = getMissingObjects( knNr2CfgKeys.get( knNr ), entry.getValue() );
				missingObjects.addAll( mos );
			}
		}
		
		
		return missingObjects;
	}
	
	
	/**
	 * 
	 * Returns object keys (from coordinate data) for which there is no corresponding configuration object
	 * 
	 * @param cfgKeys	Config keys of objects supplied in coordinate table
	 * @param fg		Funktionsgruppe
	 * @return			Configuration objects not found in set 'cfgKeys'
	 */
	public List<CfgKey> getUnknownObjects( List<CfgKey> cfgKeys, int fg ) {
		List<CfgKey> unknownObjects = new ArrayList<>();
		for( CfgKey cfgKey : cfgKeys ) {
			List<? extends Q> knNrQs = null;
			if( fg == 1 ) {
				knNrQs = knNr2Mq.get( cfgKey.getKnNr() );
			} else if( fg == 3 ) {
				knNrQs = knNr2Ufd.get( cfgKey.getKnNr() );
			}
			boolean found = false;
			if( knNrQs != null ) {
				for( Q q : knNrQs ) {
					Set<Integer> cfgKeyDes = new HashSet<>( cfgKey.getDeNr() ); 
					cfgKeyDes.retainAll( q.getDefiningDes() );
					if( cfgKeyDes.size() < cfgKey.getDeNr().size() )  {
						found = true;
					}
				}
			}
			
			if( !found ) {
				unknownObjects.add( cfgKey );
			}
		}
		
		return unknownObjects;
	}
	
	private String getUz( int knNr, Map<String,String> knNr2KriName ) {
		String kriName = knNr2KriName.get( "" + knNr );
		if( kriName != null ) {
			String uz = kri2UzName.get( kriName );
			if( uz == null ) {
				return "[" + kriName+ "]";
			} else {
				return uz + " [" + kriName+ "]";
			}
		} else {
			LOGGER.warn( "No UZ name for KnNr " + knNr );
		}
		
		return UNKNOWN;
	}
	
	private void incrementCounts( String wk,
            				      String uz,
            				      Set<String> wancomKeys, 
            				      Set<String> cfgKeysInWancom,
			                      Map<String,Integer> uz2DeCount, 
			                      Map<String,Integer> uz2DeCountData ) {
		Integer n = uz2DeCount.get(uz);
		if( n == null ) {
			n = 0;
		}
		uz2DeCount.put( uz, n + 1 );
		
		if( wancomKeys.contains( wk ) ) {
			cfgKeysInWancom.add( wk );
			
			Integer nd = uz2DeCountData.get(uz);
			if( nd == null ) {
				nd = 0;
			}
			uz2DeCountData.put( uz, nd + 1 );
		}
	}
	
	private String getDe( String wk ) {
		int pos = wk.lastIndexOf( '-' );
		if( pos != -1 ) {
			return wk.substring( pos + 1 );
		}
		
		return wk;
	}

	LinkedList<String> toKnNrDeLines( Collection<String> nonCfgKeys ) {
		LinkedList<String> knNrDeLines = new LinkedList<>();
		
		for( String key : nonCfgKeys ) {
			int pos = key.indexOf( ' ' );
			String locDistDe = key.substring( pos + 1 );
			String[] parts = locDistDe.split( "-" );
			knNrDeLines.add( Integer.parseInt( parts[0] ) * 256 + Integer.parseInt( parts[1] ) + ";" + parts[2]  );
		}
		 
		 return knNrDeLines;
	}

	
	public List<String> uz2MqsAqsUfds() {         // NOSONAR 
		
		List<String> lines = new ArrayList<>();
		
		Map<String,String> knNr2KriName = configService.getKriNames();
		for( Map.Entry<Integer,String> entry : QFileReader.knNr2UzName.entrySet() ) {
			int knNr = entry.getKey();
			String kriName = knNr2KriName.get( "" + knNr );	
			if( kriName != null ) {
				String uzName = kri2UzName.get( kriName );
				if( uzName == null ) {
					kri2UzName.put( kriName, entry.getValue() );
				}
			}
		}

		Map<String,List<String>> uz2Mqs = new HashMap<>();
		for( Map.Entry<Integer, List<Mq> > e : knNr2Mq.entrySet()  ) {
			for( Mq mq : e.getValue() ) {
				String kri = knNr2KriName.get( "" + e.getKey() );
				String uz = kri2UzName.get( kri );
				uz2Mqs.computeIfAbsent( uz, u -> new ArrayList<>() ).add( mq.getId() );
			}
		}

		lines.add( "" );
		lines.add( "" );
		lines.add( "MQs" );
		for( Map.Entry<String, List<String> > e : uz2Mqs.entrySet()  ) {
			lines.add( "" );
			lines.add( e.getKey() );
			for( String m : e.getValue().stream().sorted().collect( Collectors.toList()) ) {
				lines.add( "" + m );
			}
		}
		
		
		Map<String,List<String>> uz2Aqs = new HashMap<>();
		for( Map.Entry<Integer, List<Aq> > e : knNr2Aq.entrySet()  ) {
			for( Aq aq : e.getValue() ) {
				String kri = knNr2KriName.get( "" + e.getKey() );
				String uz = kri2UzName.get( kri );
				uz2Aqs.computeIfAbsent( uz, u -> new ArrayList<>() ).add( aq.getId() );
			}
		}

		lines.add( "" );
		lines.add( "" );
		lines.add( "AQs" );
		for( Map.Entry<String, List<String> > e : uz2Aqs.entrySet()  ) {
			lines.add( "" );
			lines.add( e.getKey() );
			for( String m : e.getValue().stream().sorted().collect( Collectors.toList()) ) {
				lines.add( "" + m );
			}
		}

		
		Map<String,List<String>> uz2Ufds = new HashMap<>();
		for( Map.Entry<Integer, List<Ufd> > e : knNr2Ufd.entrySet()  ) {
			for( Ufd ufd : e.getValue() ) {
				String kri = knNr2KriName.get( "" + e.getKey() );
				String uz = kri2UzName.get( kri );
				uz2Ufds.computeIfAbsent( uz, u -> new ArrayList<>() ).add( ufd.getId() );
			}
		}

		lines.add( "" );
		lines.add( "" );
		lines.add( "UFDs" );
		for( Map.Entry<String, List<String> > e : uz2Ufds.entrySet()  ) {
			lines.add( "" );
			lines.add( e.getKey() );
			for( String m : e.getValue().stream().sorted().collect( Collectors.toList()) ) {
				lines.add( "" + m );
			}
		}
		
		return lines;
	}
	
	/**
	 * 
	 * Checks configuration against Wancom FG1/FG3/FG4 keys received on a day
	 * 
	 * @param wancomKeyFile				Key file
	 * @param fg						Funktionsgruppe: 1, 3 or 4
	 * @param nonCfgKeysFile			Keys not found in configuration
	 * @param cfgWancomStatisticsFile	Cfg/Wancom statistics
	 */
	public List<String>  writeWancomStatistics( String wancomKeyFile, String fg ) {      // NOSONAR
		if( firstWancomStatistics ) {
			Map<String,String> knNr2KriName = configService.getKriNames();
			for( Map.Entry<Integer,String> entry : QFileReader.knNr2UzName.entrySet() ) {
				int knNr = entry.getKey();
				String kriName = knNr2KriName.get( "" + knNr );	
				if( kriName != null ) {
					String uzName = kri2UzName.get( kriName );
					if( uzName == null ) {
						kri2UzName.put( kriName, entry.getValue() );
					}
				}
			}
				
			firstWancomStatistics = false;
		}
		
		
		if( ( wancomKeyFileDir == null ) || wancomKeyFileDir.isEmpty() ) {
			String error = "No file directory defined: cfg.check.wancomKeyFileDir";
			LOGGER.error( error ); 
			return Arrays.asList( error );
		}
		
		Set<String> wancomKeys = null;
		try {
			List<String> knNrs = FileUtils.readLines( new File( wancomKeyFileDir + File.separator + wancomKeyFile ) );
			knNrs = knNrs.stream().filter( k -> k.startsWith( fg ) ).collect( Collectors.toList() );
			wancomKeys = new HashSet<>( knNrs );
		} catch (Exception e) {
			String error = "Cannot read file <" + wancomKeyFileDir + File.separator + wancomKeyFile + ">";
			LOGGER.error( error ); 
			return Arrays.asList( error );
		}
		
		LOGGER.info( "" );
		LOGGER.info( "Executing check of Wancom data for '" + wancomKeyFile + "', FG" + fg  );
		
		Map<String,String> wk2QId = new HashMap<>();
		Map<String,String> wk2KnNr = new HashMap<>();
		Map<String,String> wk2Uz = new HashMap<>();
		Map<String,Integer> uz2DeCount = new HashMap<>();
		Map<String,Integer> uz2DeCountData = new HashMap<>();
		
		Set<String> cfgKeysInWancom = new HashSet<>();
		Set<String> allCfgKeys = new HashSet<>();
		Set<String> cfgKeysIgnore  = new HashSet<>(); 
		
		TreeSet<String> nodes = new TreeSet<>();
		Map<String,String> knNr2KriName = configService.getKriNames();
		
		if( fg.equals("1") ) {
		
			for( Map.Entry<Integer,List<Mq> > entry : knNr2Mq.entrySet() ) {    // NOSONAR
				Integer knNr = entry.getKey();
				String uz = getUz( knNr, knNr2KriName );
				for( Mq mq : entry.getValue() ) {
					for( Integer deDet :  mq.getLaneDes() ) {
						String wk = "1 " + knNr / 256 + "-" + knNr % 256 + "-" + deDet;
						allCfgKeys.add( wk );
						wk2QId.put( wk, mq.getId() );
						wk2KnNr.put( wk, "" + knNr );
						wk2Uz.put( wk, uz );
						
						incrementCounts( wk, uz, wancomKeys, cfgKeysInWancom, uz2DeCount, uz2DeCountData ); 
						
						LOGGER.debug( uz + ". " + knNr );
						nodes.add(wk );
					}
				} 
			}
		} 
		if( fg.equals("4") ) {
			
			for( Map.Entry<Integer,List<Aq> > entry : knNr2Aq.entrySet() ) {    // NOSONAR
				Integer knNr = entry.getKey();
				String uz = getUz( knNr, knNr2KriName );
				for( Aq aq : entry.getValue() ) {
					Set<String> nonClusterDeKeys = new HashSet<>();
					for( Integer deDet :  aq.getNonClusterDes() ) {
						String wk = "4 " + knNr / 256 + "-" + knNr % 256 + "-" + deDet;
						nonClusterDeKeys.add( wk );
						allCfgKeys.add( wk );
						wk2QId.put( wk, aq.getId() );
						wk2KnNr.put( wk, "" + knNr );
						wk2Uz.put( wk, uz );
						
						incrementCounts( wk, uz, wancomKeys, cfgKeysInWancom, uz2DeCount, uz2DeCountData ); 
						
						LOGGER.debug( uz + ". " + knNr );
						nodes.add(wk );
					}
					
					for( Integer deDet :  aq.getWzgDes() ) {
						String wk = "4 " + knNr / 256 + "-" + knNr % 256 + "-" + deDet;
						if( !nonClusterDeKeys.contains( wk ) ) {
							cfgKeysIgnore.add( wk );
						}
					}					
				} 
			}
		} 
		if( fg.equals("3") ) {
			
			for( Map.Entry<Integer,List<Ufd> > entry : knNr2Ufd.entrySet() ) {    // NOSONAR
				Integer knNr = entry.getKey();
				String uz = getUz( knNr, knNr2KriName );
				for( Ufd ufd : entry.getValue() ) {
					int deDet = ufd.getDeNr();
					
					String wk = "3 " + knNr / 256 + "-" + knNr % 256 + "-" + deDet;
					allCfgKeys.add( wk );
					wk2QId.put( wk, ufd.getId() );
					wk2KnNr.put( wk, "" + knNr );
					wk2Uz.put( wk, uz );
					
					incrementCounts( wk, uz, wancomKeys, cfgKeysInWancom, uz2DeCount, uz2DeCountData ); 
					
					LOGGER.debug( uz + ". " + knNr );
					nodes.add(wk );
				} 
			}
		} 
		
		List<String> nonCfgKeys = new ArrayList<>();
		for( String wk : wancomKeys ) {
			if( !cfgKeysInWancom.contains( wk ) ) {
				if( !cfgKeysIgnore.contains( wk ) ) {   // NOSONAR
					nonCfgKeys.add( wk );
				}
			}
		}
		
		Set<String> keys = new HashSet<>( uz2DeCount.keySet() );
		for( String key : keys ) {   // NOSONAR
			if( uz2DeCount.get( key ) == 0 ) {
				uz2DeCount.remove( key );
				uz2DeCountData.remove( key );
			}
		}
		
		LinkedList<String> statLines = new LinkedList<>();
		int numW = 0;
		int numC = 0;
		for( String uz : uz2DeCount.keySet() ) {    // NOSONAR
			numC += uz2DeCount.get(uz);
			
			Integer numData = uz2DeCountData.get(uz);
			if( numData == null ) {
				numData = 0;
			}
			numW += numData;
			
			statLines.add( uz + ";" + uz2DeCount.get(uz) + ";" + numData );
		}
		Collections.sort(statLines);
		statLines.addFirst( "UZ;# Cfg;# Wancom" );
		statLines.addLast( ";" + numC + ";" + numW );
		
		String noDataKeysFile = wancomKeyFileDir + File.separator + "noDataItems_fg" + fg + "_" + wancomKeyFile;
		String nonCfgKeysFile = wancomKeyFileDir + File.separator + "nonCfgKeys_fg" + fg + "_" + wancomKeyFile;
		String wancomStatisticsFile = wancomKeyFileDir + File.separator + "wancomStatistics_fg" + fg + "_" + wancomKeyFile;
		
		LinkedList<String> cfgKeysWithoutData = new LinkedList<>();
		for( String key : allCfgKeys ) {
			if( !cfgKeysInWancom.contains( key ) ) {
				String line = wk2Uz.get( key ) + ";" + wk2QId.get( key ) + ";" + wk2KnNr.get( key ) + ";" + getDe(key);
				cfgKeysWithoutData.add( line );	
			}
		}
		
		try {
			LOGGER.info( "Writing config keys without data" );
			Collections.sort(cfgKeysWithoutData);
			cfgKeysWithoutData.addFirst( "UZ;Q;KnNr;De" );
			FileUtils.writeLines( new File( noDataKeysFile ) , StandardCharsets.ISO_8859_1.name(), cfgKeysWithoutData );
		} catch (IOException e) {
			LOGGER.error( CANNOT_WRITE_FILE + noDataKeysFile + ">" ); 
		}
		
		try {
			LOGGER.info( "Writing non config keys" );

			LinkedList<String> knNrDeLines = toKnNrDeLines( nonCfgKeys );
			Collections.sort(knNrDeLines);
			knNrDeLines.addFirst( "KnNr;De" );
			FileUtils.writeLines( new File( nonCfgKeysFile ) , StandardCharsets.ISO_8859_1.name(), knNrDeLines );
		} catch (IOException e) {
			LOGGER.error( CANNOT_WRITE_FILE + nonCfgKeysFile + ">" ); 
		}
		
		try {
			LOGGER.info( "Writing Wancom statistics" );
			FileUtils.writeLines( new File( wancomStatisticsFile ) , StandardCharsets.ISO_8859_1.name(), statLines );
		} catch (IOException e) {
			LOGGER.error( CANNOT_WRITE_FILE + wancomStatisticsFile + ">" ); 
		}
		
		LOGGER.debug( "----" );
		nodes.forEach( LOGGER::debug );
		LOGGER.debug( "----" );
		
		return statLines;
	}
	
	
}
