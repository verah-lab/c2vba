package de.heuboe.geo.manager.control.q;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import de.heuboe.geo.manager.base.cfgsvc.ConfigServiceClient;
import de.heuboe.log.Logger;
import de.heuboe.wls.util.WlsException;
import eu.datex2.schema._2._2_0.CarriagewayEnum;
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

	private static final Logger LOGGER = Logger.getLogger(SystemConfiguration.class);
	private static final String C2VBA = "C2VBA";
	private static final String WZG = "Wzg <";
	private static final String UFD = "UFD <";
    
	private ConfigServiceClient configService;

	private Map<String, Q> qs = new TreeMap<>();
	private Map<Integer, String> knNr2Uz = new HashMap<>();
	private Map<Integer, List<Mq>> knNr2Mq = new HashMap<>();
	private Map<Integer, List<Aq>> knNr2Aq = new HashMap<>();
	private Map<Integer, List<Ufd>> knNr2Ufd = new HashMap<>();
	
	
	private List<CfgKey> geoObjectKeys = new ArrayList<>();
	
	private Map<String,Integer> id2De = new HashMap<>();
	private Map<String, Set<String> > mqId2SensorIds = new HashMap<>();
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param configService	ConfigService
	 */
	public SystemConfiguration( ConfigServiceClient configService ) {
		this.configService = configService;
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
		
		List<TlsDevice> tlsDevices = configService.getAllDevices( new HashMap<>() );

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
					
					id2De.put( ea.getEaid(), ea.getDeNummer()  );
				}
			}
		}
		
		
		List<CfgAq> cfgAqs = configService.getAllAQs( new HashMap<>() );
		for( CfgAq cfgAq : cfgAqs ) {
			Set<Integer> knNrs = new HashSet<>();
			Set<Integer> wzgDes = new HashSet<>();
			for( CfgWzg wzg : cfgAq.getWzgs().getWzgsList() ) {
				Set<CfgKey> cfgKeys = objId2CfgKeys.get( wzg.getId() );
				if( cfgKeys == null ) {
					LOGGER.warn(WZG + wzg.getId() + "> not in device configuration"); 			// NOSONAR
				} else if( cfgKeys.size() > 1 ) {
					LOGGER.warn(WZG + wzg.getId() + "> has multiple device configurations");  	// NOSONAR
				} else {
					wzgDes.add( cfgKeys.iterator().next().getDeNr().iterator().next() );
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
				
				mqId2SensorIds.computeIfAbsent( cfgMq.getId(), s -> new HashSet<>() ).add( sensor.getId() );
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

			
		List<CfgUdeSensor> cfgUfds = configService.getAllUDEs( new HashMap<>() );
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
	
	public List<String> getMqDes( String mqId ) {
	    Set<String> sensors = mqId2SensorIds.get( mqId );
	    if( sensors == null ) {
	        return new ArrayList<>();
	    }
	    
	    Set<Integer> des = new TreeSet<>();
	    for( String sensor : sensors ) {
	        Integer de = id2De.get( sensor );
	        if( de != null ) {
	            des.add( de );
	        }
	    }
	    
	    return des.stream().map( d -> "" + d ).collect( Collectors.toList() );
	}
	
    public CarriagewayEnum determineMqCarriageway( String mqId ) {   // NOSONAR
        
        Set<String> sensorIds = mqId2SensorIds.get( mqId );
        if( sensorIds != null ) {
        
            Map<CarriagewayEnum,Integer> cCount = new HashMap<>();   // NOSONAR
            
            int maxCount = 0; 
            for( String sensorId : sensorIds ) {
                Integer de = id2De.get( sensorId );
                if( de != null ) {
                    
                    CarriagewayEnum carriageway = null;
                    if( ( de >= 1 ) && ( de <= 63 ) ) {
                        carriageway = CarriagewayEnum.MAIN_CARRIAGEWAY;
                    } else if( ( de >= 81 ) && ( de <= 87 ) ) {
                        carriageway = CarriagewayEnum.PARALLEL_CARRIAGEWAY;
                    } else if( de == 89 ) {    // Seitenstreifen
                        carriageway = CarriagewayEnum.MAIN_CARRIAGEWAY;
                    } else if( ( de >= 65 ) && ( de <= 95 ) ) {
                        carriageway =  CarriagewayEnum.EXIT_SLIP_ROAD;
                        
                    } else if( ( de >= 113 ) && ( de <= 119 ) ) {
                        carriageway =  CarriagewayEnum.PARALLEL_CARRIAGEWAY;
                    } else if( de == 121 ) {    // Seitenstreifen
                        carriageway = CarriagewayEnum.MAIN_CARRIAGEWAY;
                    } else if( ( de >= 97 ) && ( de <= 127 ) ) {
                        carriageway =  CarriagewayEnum.EXIT_SLIP_ROAD;
                        
                    } else if( ( de >= 145 ) && ( de <= 151 ) ) {
                        carriageway =  CarriagewayEnum.PARALLEL_CARRIAGEWAY;
                    } else if( ( de >= 129 ) && ( de <= 159 ) ) {
                        carriageway =  CarriagewayEnum.ENTRY_SLIP_ROAD;
                        
                    } else if( ( de >= 177 ) && ( de <= 183 ) ) {
                        carriageway =  CarriagewayEnum.PARALLEL_CARRIAGEWAY;
                    } else if( ( de >= 161 ) && ( de <= 191 ) ) {
                        carriageway =  CarriagewayEnum.ENTRY_SLIP_ROAD;
                        
                    } else  if( ( de >= 225 ) && ( de <= 255 ) ) {
                        carriageway = CarriagewayEnum.MAIN_CARRIAGEWAY;
    
    
                    } else {
                        carriageway = CarriagewayEnum.MAIN_CARRIAGEWAY;
                    }
    
                    Integer count = cCount.get( carriageway );
                    if( count == null ) {
                        count = 1;
                    } else {
                        count++;
                    }
                    
                    maxCount = Math.max( count, maxCount );
                    cCount.put( carriageway, count );
                }
            }
            
            if( !cCount.isEmpty() ) {
                
                for( Map.Entry<CarriagewayEnum,Integer> entry : cCount.entrySet() ) {
                    if( entry.getValue() == maxCount ) {
                        return entry.getKey(); 
                    }
                }
            }
        }
        
        return null;
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
	
	/**
	 * 
	 * Checks configuration against Wancom FG1 keys received on a day
	 * 
	 * @param knNr2UzIds				Knoten-Nr -> UZ-Name assignment
	 * @param wancomKeyFile				Key file
	 * @param nonCfgKeysFile			Keys not found in configuration
	 * @param cfgWancomStatisticsFile	Cfg/Wancom statistics
	 */
	public List<String>  writeWancomStatistics( Map< Integer, String > knNr2UzIdFromCoords,        // NOSONAR
			                           			String wancomKeyFile,
			                           			String nonCfgKeysFile,
			                           			String cfgWancomStatisticsFile ) {    
		
		Set<String> fg1WancomDes = null;
		try {
			List<String> knNrs = FileUtils.readLines( new File( wancomKeyFile ) );
			fg1WancomDes = new HashSet<>( knNrs );
		} catch (Exception e) {
			String error = "Cannot read file <" + wancomKeyFile + ">";
			LOGGER.error( error ); 
			return Arrays.asList( error );
		}
		
		LOGGER.info( "Writing Wancom check statistics:" );
		
		Map<String,Integer> uz2fg1DeCount = new HashMap<>();
		Map<String,Integer> uz2fg1DeCountData = new HashMap<>();
		Set<String> knownIds = new HashSet<>();
		
		for( String uz : knNr2Uz.values() ) {
			uz2fg1DeCount.put( uz, 0 );
			uz2fg1DeCountData.put( uz, 0 );
		}
		if( knNr2Uz.isEmpty() ) {
			uz2fg1DeCount.put( C2VBA, 0 );
			uz2fg1DeCountData.put( C2VBA, 0 );
		}
		
		TreeSet<String> nodes = new TreeSet<>();
		for( Map.Entry<Integer,List<Mq> > entry : knNr2Mq.entrySet() ) {    // NOSONAR
			Integer knNr = entry.getKey();
			String uz = knNr2Uz.get( knNr );
			if( uz == null ) {
				uz = C2VBA;
			}
			for( Mq mq : entry.getValue() ) {
				for( Integer deDet :  mq.getLaneDes() ) {
					String locDist = "1 " + knNr / 256 + "-" + knNr % 256 + "-" + deDet;
					uz2fg1DeCount.put( uz, uz2fg1DeCount.get(uz) + 1 );
					
					if( fg1WancomDes.contains(locDist  ) ) {
						knownIds.add( locDist );
						uz2fg1DeCountData.put( uz, uz2fg1DeCountData.get(uz) + 1 );
					}
					
					LOGGER.debug( uz + ". " + knNr );
					nodes.add(locDist );
				}
			} 
		}
		
		List<String> nonCfgKeys = new ArrayList<>();
		for( String wid : fg1WancomDes ) {
			if( !knownIds.contains( wid ) ) {
				nonCfgKeys.add( wid + "   " + getUzIdFromCoords( wid, knNr2UzIdFromCoords )  );
			}
			
		}
		
		List<String> statLines = new ArrayList<>();
		statLines.add( "UZ;# Cfg;# Wancom" );
		int numW = 0;
		int numC = 0;
		for( String uz : uz2fg1DeCount.keySet() ) {    // NOSONAR
			numC += uz2fg1DeCount.get(uz);
			numW += uz2fg1DeCountData.get(uz);
			statLines.add( uz + ";" + uz2fg1DeCount.get(uz) + ";" + uz2fg1DeCountData.get(uz) );
		}
		statLines.add( ";" + numC + ";" + numW );
		
		if( nonCfgKeysFile != null ) {
			try {
				LOGGER.info( "Writing non config keys" );
				FileUtils.writeLines( new File( nonCfgKeysFile ) , StandardCharsets.ISO_8859_1.name(), nonCfgKeys );
			} catch (IOException e) {
				LOGGER.error( "Cannot write file <" + nonCfgKeysFile + ">" ); 
				return new ArrayList<>();
			}
		}
		
		if( cfgWancomStatisticsFile != null ) {
			try {
				LOGGER.info( "Writing Config/Wancom statistics" );
				FileUtils.writeLines( new File( cfgWancomStatisticsFile ) , StandardCharsets.ISO_8859_1.name(), statLines );
			} catch (IOException e) {
				LOGGER.error( "Cannot write file <" + cfgWancomStatisticsFile + ">" ); 
				return new ArrayList<>();
			}
		}
		
		LOGGER.debug( "----" );
		nodes.forEach( LOGGER::debug );
		LOGGER.debug( "----" );
		
		return statLines;
	}
	
	private String getUzIdFromCoords( String wid, Map< Integer, String > knNr2Uz ) {
		String[] parts = wid.split( " "  );
		String[] locDistDe = parts[1].split( "-" );
		
		int knNr = Integer.parseInt(  locDistDe[0] ) * 256 + Integer.parseInt( locDistDe[1] ); 
		String uzId = knNr2Uz.get( knNr );
		return ( uzId == null ) ? "???" : uzId;
	}
	
	
}
