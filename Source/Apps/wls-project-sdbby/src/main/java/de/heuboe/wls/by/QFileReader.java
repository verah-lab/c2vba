package de.heuboe.wls.by;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import de.heuboe.data.Data;
import de.heuboe.data.DataReader;
import de.heuboe.data.DataStore;
import de.heuboe.data.Filter;
import de.heuboe.data.csv.CsvDataStore;
import de.heuboe.data.csv.CsvFactory;
import de.heuboe.data.csv.CsvReader;
import de.heuboe.geo.base.Direction;
import de.heuboe.log.Logger;
import de.heuboe.util.PairC;
import de.heuboe.wls.basic.util.DecimalPointFormatter;
import de.heuboe.wls.by.cfg.SystemConfiguration.CfgKey;
import de.heuboe.wls.by.cfg.Q;
import de.heuboe.wls.by.cfg.SystemConfiguration;
import de.heuboe.wls.data.geofeature.DirectedGeoFeature;
import de.heuboe.wls.data.roadmap.RoadComponent;
import de.heuboe.wls.data.roadmap.RoadElement;
import de.heuboe.wls.data.roadmap.RoadNetRestrictionParameter;
import de.heuboe.wls.data.tmc.TmcLocation;
import de.heuboe.wls.data.tmc.TmcRefPoint;
import de.heuboe.wls.data.wls.CompassPoint;
import de.heuboe.wls.data.wls.ErrorLocation;
import de.heuboe.wls.data.wls.Location;
import de.heuboe.wls.data.wls.LocationElement;
import de.heuboe.wls.data.wls.Orientation;
import de.heuboe.wls.data.wls.PointLocation;
import de.heuboe.wls.data.wls.Property;
import de.heuboe.wls.geofeature.GeoFeatureUtil;
import de.heuboe.wls.iface.Fault;
import de.heuboe.wls.plugin.roadmap.RoadMapPlugin;
import de.heuboe.wls.roadmap.DirectedReKey;
import de.heuboe.wls.roadmap.RoadComponentBuilder;
import de.heuboe.wls.roadmap.RoadElementUtils;
import de.heuboe.wls.roadmap.RoadNetRestriction;
import de.heuboe.wls.tmc.roadnet.plugin.TmcRoadnetPlugin;
import de.heuboe.wls.util.WlsException;
import de.heuboe.wls.utils.CoordinateTransformer;
import de.heuboe.wls.utils.GeometryTools;

/**
 * 
 * Reads MQs, AQs or UDEs from text files
 * 
 * @author peters
 *
 */
public class QFileReader  {
	
	
	@SuppressWarnings("serial")
	private static final Map<Integer,String> glanetype2Name = new HashMap<Integer,String>() {{  // NOSONAR
		put( 1, "Hauptfahrbahn" );
		put( 2, "Parallelfahrbahn" );
		put( 3, "Zufahrt" );
		put( 4, "Abfahrt" );
		put( 9, "Einzelfahrstreifen" );
	}}; // NOSONAR

    
    /**
     * 
     * Filters RoadElements on route number and route orientation
     * 
     * @author peters
     *
     */
    public static class RoadNetRestrictionTlsConfig extends RoadNetRestriction {
        
        private static Map<DirectedReKey, List<RoadComponent> > directedRe2Rcs;
        
        private String routeNum;
        private String drivingDirection; // NO or SW
        
        public static void setRoadComponents( Map<DirectedReKey, List<RoadComponent> > dre2Rcs ) {
            directedRe2Rcs = dre2Rcs;
        }
        
        @Override
        public void setProperties(Properties properties) throws Fault {
            routeNum = properties.getProperty( "routeNum" );
            drivingDirection = properties.getProperty( "drivingDirection" );
        }
         
        private DirectedReKey getDirReKey( RoadElement roadElement, String startJunction ) {
            return new DirectedReKey( roadElement.getId(), 
                                      startJunction.equals( roadElement.getFromJunction() ) ?
                                          de.heuboe.wls.data.wls.Direction.POSITIVE :
                                          de.heuboe.wls.data.wls.Direction.NEGATIVE );
        }
        
        private boolean match( RoadComponent rc ) {
            
            if( ( routeNum == null) || routeNum.isEmpty() ) {
                return false;
            }
            
            if( !rc.getRouteNum().equals( routeNum ) ) {
            	return false;
            }
            
            if( drivingDirection == null ) {
            	return true;
            }
            
            boolean no = ( rc.getOrientation() == Orientation.SOUTH_2_NORTH ) ||  ( rc.getOrientation() == Orientation.WEST_2_EAST );
            
            return ( no == drivingDirection.equals( Q.DRIVING_DIRECTION_NO ) );
        }

        @Override
        public boolean isBlocked(String startJunction, RoadElement roadElement, double pathWeight) {
            
            if( !RoadElementUtils.isDrivable( startJunction, roadElement ) ) {
                return true;
            }
            
            DirectedReKey dre = getDirReKey( roadElement, startJunction );
            
            List<RoadComponent> rcs = directedRe2Rcs.get( dre );
            
            if( rcs != null ) {
                for( RoadComponent rc : rcs ) {
                    if( match( rc ) ) {
                        return false;
                    }
                }
            }
            return !( ( routeNum == null) || routeNum.isEmpty() );
        }
    }

	private static final Logger LOGGER = Logger.getLogger(QFileReader.class);
	private static final int WGS84_SRID = 4326;
	private static final DecimalPointFormatter coordFormatter = new DecimalPointFormatter( "#0.00000" );
	private static final DecimalPointFormatter kmFormatter = new DecimalPointFormatter( "#0.000" );
	
//	ATTR_UZ: UZ
//	ATTR_ROAD: Road
//	ATTR_TARGET: Target
//	ATTR_DIRECTION: Direction
//	ATTR_KM: Kilometer
//	ATTR_KNOTEN_NR: Knotennummer
	
	public static final String ATTR_DIRECTION = "Direction";
	public static final String ATTR_TARGET = "Target";
	private static final String ATTR_RE_ID = "RoadElementId";
	private static final String ATTR_RE_OFFSET = "RoadElementOffset";
    private static final String ATTR_RE_DIRECTION = "RoadElementDirection";
    private static final String ATTR_BEARING = "BEARING";
    private static final String ATTR_BABNR = "BabNr";
    private static final String ATTR_ROAD_DIR = "RoadDir";
    private static final String ATTR_DE = "De";
    private static final String ATTR_LOCATION = "Location";
    public static final String ATTR_KNOTEN_NR = "Knotennummer";
    private static final String ATTR_DISTANCE= "Distance";
    public static final String ATTR_UZ = "UZ";
    
    private static final String FG1 = "FG1";
    private static final String FG3 = "FG3";
    private static final String fg1 = "fg1";    // NOSONAR
    private static final String fg3 = "fg3";    // NOSONAR
    private static final String fg4 = "fg4";    // NOSONAR
	
    public static final String ATTR_ROAD = "Road";
	public static final String ATTR_KM = "Kilometer";
    private static final String ATTR_UDE_TYPE = "UDE-Type";
    private static final String ATTR_GLANE_TYPE = "GlaneType";
    
    private static final String COL_KNOTEN_NR_NORD = "Knotennummer (dez)";
    private static final String COL_NAME_NORD = "Querschnitt-ID";
    private static final String COL_ID_NORD = "Querschnitt-ID";
    private static final String COL_ID_NORD_2 = "Querschnitt-ID";
    private static final String COL_LATITUDE_NORD = "Breite";
    private static final String COL_LONGITUDE_NORD = "Länge";
    private static final String COL_ROAD_NORD = "";
    private static final String COL_ROAD_KM_NORD = "";                
    private static final String COL_DE_NORD = "DE";
    
    private static final String COL_VT_HERKUNFT_ID = "VT_HERKUNFT_ID";
    
    private static final String COL_KNOTEN_NR_SUED = "Knotennummer";
    private static final String COL_NAME_SUED = "Querschnitt";
    private static final String COL_ID_SUED = "Querschnitt";
    private static final String COL_LATITUDE_SUED = "Breite_WGS";
    private static final String COL_LONGITUDE_SUED = "Laenge_WGS";
    private static final String COL_EPSG_SUED = "EPSG";
    private static final String COL_ROAD_SUED = "Fahrtrichtung";           // Präfix bis ' ' oder '-'
    private static final String COL_ROAD_KM_SUED = "Betriebs_km";
    private static final String COL_DE_SUED = "de_kanal_max";
    private static final String COL_UZ_SUED = "Unterzentrale";
    
    private static final String ALC_PRIM_LOC = "AlertCPrimaryLocation";
    private static final String ALC_PRIM_LOC_NAME = "AlertCPrimaryLocationName";
    private static final String ALC_DIR = "AlertCDirection";
    private static final String ALC_DIST = "AlertCDistance";
    
    private String colKnNr;
    private String colId;
    private String colName;
    private String colLatitude;
    private String colLongitude;
    private String colEpsg = null;
    private String colFg = "FG";
    private String colRoad;
    private String colKm;
    private String colDe;
    private String colUz;
	
	private int wlsSrid = 32632;
	private String fg;
	private String area;
	private String type;
	private String sourcefile;
	private Map<String, DirectedGeoFeature> featureMap;
	private GeometryFactory factory;	
	private CoordinateTransformer transformer;
	private CoordinateTransformer transformerUTM32;
	private CoordinateTransformer transformerUTM33;
	
	private List<CfgKey> cfgKeys = new ArrayList<>();
	
	private RoadMapPlugin roadMapPlugin;
    private TmcRoadnetPlugin  tmcRoadnetPlugin;
    private RoadComponentBuilder roadComponentBuilder;
	private SystemConfiguration systemConfiguration;
	
	private Map< Integer, Set<String> > knNr2QDes = new HashMap<>();
	private Map< Integer, Integer > knNr2NumObj = new HashMap<>();
	public static Map< Integer, String > knNr2UzName = new HashMap<>();    // NOSONAR
	
	private Map<DirectedReKey, List<RoadComponent> > dre2Rcs = new HashMap<>();
	
	private Map< PairC<String,String>, Map<String,Set<String>> > loc2ObjIds = new HashMap<>();
	
	private List<String> noCoordIds = new ArrayList<>();
    private List<String> noNetLocIds = new ArrayList<>();
    private List<String> noSysIds = new ArrayList<>();
    
    private Map<String,String> sameObjects = new HashMap<>();

    private List<String> noRnDirMatchNetLocIds = new ArrayList<>();
    
    // Gemäß DE-Nummer auf Hauptfahrbahn, aber nicht auf einer RoadComponent verortet
    private List<String> deMainRoadNotOnRc = new ArrayList<>();
    
    // Gemäß DE-Nummer nicht auf Hauptfahrbahn, aber auf einer RoadComponent verortet
    private List<String> deNotMainRoadOnRc = new ArrayList<>();
    
    private Map<String,String> roadMismatchIds = new HashMap<>();
    
    private Map<String,String> id2Road = new HashMap<>();
    private Map<String,String> id2NetRoad = new HashMap<>();
    
    private List<String> alcErrorMsgs = new ArrayList<>();
    
    private Set<String> seenIds = new HashSet<>();
    private Set<String> duplicateIds = new HashSet<>();
    private int noIdCount = 0;
    
    private Map<String,String> seenQs = new HashMap<>();
	
	public void setSourcefile( String sourcefile ) {
		this.sourcefile = sourcefile;
	}

	/**
	 * 
	 * Constructor
	 * 
	 * @param wlsSrid					WLS srid
     * @param fg                        Funktional group: FG1 ord FG3
     * @param area                      Area: Sued or Nord
	 * @param sourcefile				Object source file
     * @param roadMapPlugin             WLS RoadMapPlugin
     * @param tmcRoadnetPlugin          WLS TmcRoadnetPlugin
     * @param roadComponentBuilder      WLS RoadComponentBuilder
     * @param systemConfiguration       System configuration
	 * @throws WlsException				Error
	 */
	public QFileReader( int wlsSrid,               // NOSONAR
			  		    String fg,
			  		    String area,
			  		    String sourcefile,
			  		    RoadMapPlugin roadMapPlugin,
			  		    TmcRoadnetPlugin  tmcRoadnetPlugin,
			  		    RoadComponentBuilder roadComponentBuilder,
			  		    SystemConfiguration systemConfiguration ) throws WlsException {
		
	    this.wlsSrid  = wlsSrid;
		this.sourcefile = sourcefile;
		
		if( area.equalsIgnoreCase( "nord" ) ) {
		    colKnNr = COL_KNOTEN_NR_NORD;
            colId = COL_ID_NORD;
            colName = COL_NAME_NORD;
            colLatitude = COL_LATITUDE_NORD; 
            colLongitude = COL_LONGITUDE_NORD;
            colRoad = COL_ROAD_NORD;
            colKm = COL_ROAD_KM_NORD;
            colDe = COL_DE_NORD;
            colUz = null;
		} else if( area.equalsIgnoreCase( "sued" ) ) {
            colKnNr = COL_KNOTEN_NR_SUED;
		    colId = COL_ID_SUED;
            colName = COL_NAME_SUED;
		    colLatitude = COL_LATITUDE_SUED; 
		    colLongitude = COL_LONGITUDE_SUED;
		    colEpsg = COL_EPSG_SUED;
            colRoad = COL_ROAD_SUED;
            colKm = COL_ROAD_KM_SUED;
            colDe = COL_DE_SUED;
            colUz = COL_UZ_SUED;
		} else {
		    throw new WlsException( "Unknown area <" + area + ">, known: 'Sued' and 'Nord'" );
		}
		
		this.area = area;
		
		if( fg.equalsIgnoreCase( fg1 ) ) {
		    type = "MQ";
		} else if( fg.equalsIgnoreCase( fg3 ) ) {
		    type = "UFD";
		} else if( fg.equalsIgnoreCase( fg4 ) ) {
		    type = "AQ";
        } else {
            throw new WlsException( "Unknown FG <" + fg + ">, known: 'FG1' and 'FG3'" );
        }
		
		this.fg = fg;
		
		this.roadMapPlugin = roadMapPlugin;
		this.tmcRoadnetPlugin = tmcRoadnetPlugin;
		this.roadComponentBuilder = roadComponentBuilder;
		this.systemConfiguration = systemConfiguration;
		
	}
	

	/**
	 * 
	 * Reads data from text file
	 * 
     * @throws WlsException                 Exception
	 * 
	 */
	public void init() throws WlsException {      // NOSONAR
	    
		for( RoadComponent rc : roadComponentBuilder.getRoadComponents().values() ) {
			for( LocationElement le : rc.getLocation().getElements() ) {
				DirectedReKey dre = new DirectedReKey( le.getId(), le.isInverseOrder() ? de.heuboe.wls.data.wls.Direction.NEGATIVE : de.heuboe.wls.data.wls.Direction.POSITIVE );
				dre2Rcs.computeIfAbsent( dre, d -> new ArrayList<>() ).add( rc );
			}
		}
	     
	    RoadNetRestrictionTlsConfig.setRoadComponents( dre2Rcs );
	    
		if ( sourcefile == null ) {
			throw new IllegalStateException("You have to set the source filename");
		}
		new CsvFactory();
		factory = new GeometryFactory( new PrecisionModel(), wlsSrid );
		try {
			transformer = new CoordinateTransformer( WGS84_SRID, wlsSrid );
			transformerUTM32 = new CoordinateTransformer( 32632, wlsSrid );
			transformerUTM33 = new CoordinateTransformer( 32633, wlsSrid );
		} catch (FactoryException e) {
		    String error = "cannot create coordinate transformer: " + e.toString();
			LOGGER.fatal( error );
			throw new WlsException( error );
		}
		featureMap = new HashMap<>();
		readSource();
		
		if( !this.fg.equalsIgnoreCase( fg4 ) ) {
		
			if( noIdCount > 0 ) {
	            LOGGER.warn("");
			    LOGGER.warn("# Objects without ID (column " + colId + ") [" + fg + " " + area + "]: " + noIdCount );
			    LOGGER.warn("");
			}
			
	        if( fg.equalsIgnoreCase( fg1 ) &&  !noSysIds.isEmpty() ) {
	            LOGGER.warn("");
	            LOGGER.warn("Objects not found in database configuration [" + fg + " " + area + "]" );
	            noSysIds.forEach( o -> LOGGER.warn( "    " + o ) );
	            LOGGER.warn("");
	        }
			
			if( !sameObjects.isEmpty() ) {
	            LOGGER.debug("");
		        LOGGER.debug("Coordinate objects with reference the same infra objects [" + fg + " " + area + "]" );
		        sameObjects.entrySet().forEach( e -> LOGGER.debug( "    " + e.getKey() + "  ::  " + e.getValue() ) );
	            LOGGER.debug("");
			}
				
			if( !noCoordIds.isEmpty() ) {
	            LOGGER.warn("");
		        LOGGER.warn("Objects without coordinates [" + fg + " " + area + "]" );
		        noCoordIds.forEach( o -> LOGGER.warn( "    " + o ) );
	            LOGGER.warn("");
			}
	        if( !noRnDirMatchNetLocIds.isEmpty() ) {
	            LOGGER.warn("");
	            LOGGER.warn("Objects without route/orientation mapped road net location [" + fg + " " + area + "]" );
	            noRnDirMatchNetLocIds.forEach( o -> LOGGER.warn( "    " + o ) );
	            LOGGER.warn("");
	        }
	        if( !noNetLocIds.isEmpty() ) {
	            LOGGER.warn("");
	            LOGGER.warn("Objects without road net location [" + fg + " " + area + "]" );
	            noNetLocIds.forEach( o -> LOGGER.warn( "    " + o ) );
	            LOGGER.warn("");
	        }
	        
	        if( !duplicateIds.isEmpty() ) {
	            LOGGER.debug("");
	            LOGGER.debug("IDs used for multiple objects [" + fg + " " + area + "]" );
	            duplicateIds.forEach( o -> LOGGER.debug( "    " + o ) );
	            LOGGER.debug("");
	        }
	
	        if( !deMainRoadNotOnRc.isEmpty() ) {
	            LOGGER.warn("");
	            LOGGER.warn("Objects on main carriageway according to DE but not on any road component [" + fg + " " + area + "]" );
	            deMainRoadNotOnRc.forEach( o -> { String r = id2Road.get( o ); LOGGER.warn( "    " + format(o, ( ( r == null ) ? "" : r ) ) ); } );
	            LOGGER.warn("");
	        }
	        
	        if( !deNotMainRoadOnRc.isEmpty() ) {
	            LOGGER.warn("");
	            LOGGER.warn("Objects not on main carriageway according to DE but located on road component [" + fg + " " + area + "]" );
	            deNotMainRoadOnRc.forEach( o -> { 
	                                              String r1 = id2Road.get( o );
	                                              String r2 = id2NetRoad.get( o ); 
	                                              LOGGER.warn( "    " + 
	                                                           format( o, 
	                                                                   ( ( r1 == null ) ? "" : r1 ), 
	                                                                   ( ( r2 == null ) ? "" : r2 ) ) ); 
	                                             } );
	            LOGGER.warn("");
	        }
	        
	        LOGGER.warn("");
	        LOGGER.warn("Objects at the same location [" + fg + " " + area + "]" );
	        for( Map.Entry< PairC<String,String>, Map<String,Set<String>> > entry : loc2ObjIds.entrySet() ) {
	        	if( entry.getValue().size() > 1 ) {
	        		LOGGER.warn( "    (" + entry.getKey().getFirst() + ", "  + entry.getKey().getSecond() + ")" );
	        		for( Map.Entry<String,Set<String>> ids : entry.getValue().entrySet() ) {
	        			LOGGER.warn( "        " + ids.getKey() + ": " + ids.getValue().parallelStream().collect( Collectors.joining(", ") ) );
	        		}
	        	}
	        }
	        LOGGER.warn("");
	        
	        if( !roadMismatchIds.isEmpty() ) {
	            LOGGER.warn("");
	            LOGGER.warn("Objects with road mismatch [" + fg + " " + area + "]" );
	            roadMismatchIds.entrySet().forEach( m -> LOGGER.warn( "    " + format( m.getKey(), m.getValue() ) ) );
	            LOGGER.warn("");
	        }
	        
	        if( !alcErrorMsgs.isEmpty() ) {
	            LOGGER.warn("");
	            LOGGER.warn("RDS/TMC location errors [" + fg + " " + area + "]" );
	            alcErrorMsgs.forEach( m -> LOGGER.warn( "    " + m ) );
	            LOGGER.warn("");
	        }
		} else {
	        if( !noSysIds.isEmpty() ) {
	            LOGGER.warn("");
	            LOGGER.warn("Objects not found in database configuration [" + fg + " " + area + "]" );
	            (new LinkedHashSet<>(noSysIds)).forEach( o -> LOGGER.warn( "    " + o ) );
	            LOGGER.warn("");
	        }

			if( !noCoordIds.isEmpty() ) {
	            LOGGER.warn("");
		        LOGGER.warn("Objects without coordinates [" + fg + " " + area + "]" );
		        noCoordIds.forEach( o -> LOGGER.warn( "    " + o ) );
	            LOGGER.warn("");
			}
		}
	}
	
	private String prepareCoordinate( String coor ) {
		String c = coor;
		if( c != null ) {
			c = c.replace( ".", "" );  
			c = c.replace( ",", "" );
			
			if( c.length() >= 2 ) {
				if( c.startsWith( "9" ) ) {
					c = c.substring( 0, 1 ) + "." + c.substring( 1, c.length() );
				} else {
					c = c.substring( 0, 2 ) + "." + c.substring( 2, c.length() );
				}
			} else {
				c = null;
			}
		}
		return c;
	}
	
	private String format( String key, String value ) {
	    return key + ":" + StringUtils.repeat(' ', Math.max( 4, 35 - key.length() ) ) + value;
	}

	private String format( String key, String value1, String value2 ) {
        return key + ":" + StringUtils.repeat(' ', Math.max( 4, 35 - key.length() ) ) + value1 + "/" + value2;
    }

	private void readSource() {									// NOSONAR complexity acceptable
		Properties props = new Properties();
		props.put(CsvDataStore.SEPARATOR_KEY, ";");
		props.put(CsvDataStore.CHARSET_KEY, "ISO-8859-1" );
		
		LOGGER.info("Reading source file " + sourcefile + " for " + fg );
		DataStore store = new CsvDataStore(null,sourcefile,props);
		
		{  // NOSONAR
			CsvReader reader = (CsvReader)store.getReader((Filter)null);
			
			while( reader.hasNext() ) {
				Data rec = reader.next();
				int knNr = rec.getMember( colKnNr ).getAsInt();
				
				String functionGroup = rec.getMember( colFg ).getAsString();
				if( fg.equals( "FG"+ functionGroup ) ) {
					Integer numObj = knNr2NumObj.get( knNr );
					if( numObj == null ) { 
						knNr2NumObj.put(knNr, 1);
					} else { 
						knNr2NumObj.put(knNr, numObj + 1 ); 
					}
					
			        Data member = rec.getMember(colDe);
			        if( member != null ) {
			            String de = member.getAsString();
			            if( !de.isEmpty() ) {
			                
			                String[] des = de.split( "," );
			                if( ( des != null ) && ( des.length != 0 ) ){
			                    List<String> deList = Arrays.asList( des );
			                    int fgId = Integer.parseInt( fg.substring(2) );
			                    deList = getQDes(knNr, deList, fgId );
			                    
			                    knNr2QDes.computeIfAbsent( knNr, k -> new HashSet<>() ).addAll( deList);
			                }
			            }
			        }
				}
			}
			reader.close();
		}
		
		CsvReader reader = (CsvReader)store.getReader((Filter)null);
		
		int count = 0;
		while( reader.hasNext() ) {
			List<DirectedGeoFeature> fs = getNextGeoFeature(reader);
			if( fs != null ) {
				for( DirectedGeoFeature f : fs ) {
				    featureMap.put( f.getId(), f);
		            count++;
				}
			}
			
		}
		reader.close();

        LOGGER.info( "Number of objects: " + count );
	}

	private List<DirectedGeoFeature> getNextGeoFeature( DataReader reader) {   // NOSONAR: complexity acceptable
		if ( reader == null || !reader.hasNext()) {
			throw new IllegalStateException();
		}
		
		Data rec = reader.next();
		
		String functionGroup = rec.getMember( colFg ).getAsString();
		if( !fg.equals( "FG"+ functionGroup ) ) {
		    return new ArrayList<>();
		}
		
		String permId = rec.getMember( colId ).getAsString();
		
        if( area.equalsIgnoreCase( "nord" ) ) {
        	if( reader.type().getMemberType( COL_VT_HERKUNFT_ID ) != null ) {     // NOSONAR
	            String region = rec.getMember( COL_VT_HERKUNFT_ID ).getAsString();
	            if( ( region == null ) || region.isEmpty() || !region.equals( "ABDN" ) ) {
	                return new ArrayList<>();
	            }
        	}
        }
        
		
		if( ( ( permId == null ) || permId.isEmpty() ) && area.equalsIgnoreCase( "nord" ) && ( rec.getMember( COL_ID_NORD_2 ).getAsString() != null ) ) {
	        permId = rec.getMember( COL_ID_NORD_2 ).getAsString();
		}
		
		String uz = "";
		if( colUz != null ) {
			uz = rec.getMember( colUz ).getAsString();
		}
		int knNr = rec.getMember( colKnNr ).getAsInt();
		
		if( !uz.isBlank() ) {
			knNr2UzName.put( knNr, uz );
		}
		
		permId += " [" + knNr + "]";
		String name = rec.getMember( colName ).getAsString();
		
		if( permId.trim().isEmpty() ) {
		    noIdCount++;
		    return new ArrayList<>();
		}
		
        List<Property> properties = new ArrayList<>();
        
        properties.add( new Property( ATTR_KNOTEN_NR, "" + knNr ) );
        properties.add( new Property( ATTR_LOCATION, "" + ( knNr / 256 ) ) );
        properties.add( new Property( ATTR_DISTANCE, "" + ( knNr % 256 ) ) );
        properties.add( new Property( ATTR_UZ, uz ) );
        
        String[] des = null;
        String de = null;
        Data member = rec.getMember(colDe);
        if( member != null ) {
            de = member.getAsString();
            if( !de.isEmpty() ) {
                
                des = de.split( "," );
                des = Arrays.asList(des).stream().map( String::trim ).collect(Collectors.toList()).toArray( new String[0] );
                if( ( des != null ) && ( des.length != 0 ) ){
                    de = des[0];
                }
            }
        }
		
        if( ( des != null ) && ( des.length > 0 ) ) {
            permId += " {";
            permId += Arrays.asList( des ).stream().collect( Collectors.joining("-") );
            permId += "}"; 
        } else {
            noSysIds.add( permId );
            return new ArrayList<>();
        }
        
		Set<Integer> deSet = new HashSet<>();
        if( ( des != null ) && ( des.length > 0 ) ) {
        	deSet = Arrays.stream( des ).map( Integer::parseInt ).collect( Collectors.toSet() );
        }
		cfgKeys.add( new CfgKey( permId, knNr, Integer.parseInt( functionGroup ), deSet ) );
        
		
		if( seenIds.contains( permId ) ) {
		    duplicateIds.add( permId );
		}
		seenIds.add( permId );
		
        Double latitude = getDouble( area, rec.getMember( colLatitude ) ); 
        Double longitude = getDouble( area, rec.getMember( colLongitude ) );
        Integer epsg = null;
        if( !rec.getMember( colEpsg ).isNull() && !rec.getMember( colEpsg ).isEmpty() ) {
        	String epsgStr = rec.getMember( colEpsg ).getAsString();
        	if( !epsgStr.isEmpty() ) {
        		epsg = rec.getMember( colEpsg ).getAsInt();
        	}
        }
        
        Geometry geometry = null;
        int bearing = 0;
        CompassPoint cp = null;
		String road = "";
		
        boolean hasCoord = true;
        
        if( latitude == null || ( longitude == null ) ) {
        	hasCoord = false;
        } else {
        
	        Coordinate coordinate = new Coordinate(latitude, longitude);
	        try {
	        	Coordinate[] coors = null;
	        	if( epsg == null ) {
	        		coors = transformer.transform(coordinate);
	        	} else if ( epsg == 32632 ) {
	        		coors = transformerUTM32.transform(coordinate);
	        	} else if ( epsg == 32633 ) {
	        		coors = transformerUTM33.transform(coordinate);
	        	} 
	        	
	        	if( coors == null ) {
	        		LOGGER.error( "Unknown EPSG: " + epsg );
	        		hasCoord = false;
	        	} else {
	        		geometry = factory.createPoint(coors[0]);
	        	}
	        } catch( TransformException ex ) {
	            LOGGER.fatal("Cannot transform coordinate: " + ex.toString() );
	            hasCoord = false;
	        }
	        
	        if( hasCoord ) {
	        
		        String x = coordFormatter.format( coordinate.getX() );
		        String y = coordFormatter.format( coordinate.getY() );
		        
		        loc2ObjIds.computeIfAbsent( new PairC<>(x,y), i -> new HashMap<>() ).
		                   computeIfAbsent( "" + knNr, k -> new HashSet<String>() ).add( permId );
				
		        Data fahrtrichtung = rec.getMember(colRoad);
		        if( fahrtrichtung != null  ) {
		            String r = fahrtrichtung.getAsString();
			        properties.add(new Property( ATTR_TARGET, r ) );
		            if( !r.isEmpty() ) {
		                
		                r = r.replace( "B022", "B22" );
		                
		                boolean isNeu = r.toLowerCase().contains( "neu" );
		                int pos = r.indexOf( ' ' );
		                if( pos != -1 ) {
		                    r = r.substring( 0, pos );
		                }
		                pos = r.indexOf( '-' );
		                if( pos != -1 ) {
		                    r = r.substring( 0, pos );
		                }
		                pos = r.indexOf( '_' );
		                if( pos != -1 ) {
		                    r = r.substring( 0, pos );
		                }
		                
		                if( isNeu ) {
		                    r = r + "n";
		                }
		                
		                if( r.startsWith( "St" ) ) {
		                    r = "ST" + r.substring( 2 );
		                }
		                r = r.replace( "A99a", "A99A" );
		                
		                road = r;
		                
		                id2Road.put( permId, road);
		            }
		        }
		        properties.add(new Property( ATTR_ROAD, road ) );
		        
		        String km = "";
		        member = rec.getMember(colKm);
		        if( member != null  ) {
		            String k = member.getAsString();
		            if( !k.isEmpty() ) {
		                km = k;
		                km = km.replace( ',', '.' );
		            }
		        }
		        properties.add(new Property( ATTR_KM, km ) );
		        
		        cp = createCompassPoint(bearing);
	        }
        }
        
        List<DirectedGeoFeature> dgfs = new ArrayList<>();
        
        int fgId = Integer.parseInt( fg.substring(2) );
        List<String> objDes = Arrays.asList( des );
        if( fgId == 1 ) {
        	objDes = getQDes( knNr, objDes, fgId );
        }
        
        for( String od : objDes ) {    // NOSONAR
        	
        	String objDe = od;
        
        	List<Q> qs = new ArrayList<>();
        	
	        if( fgId == 4 ) {
	        	qs.addAll(  systemConfiguration.getAllFg4Q( knNr, Integer.parseInt( objDe ) ) );
	        	if( qs.isEmpty() ) {
		        	if( systemConfiguration.hasSingleQ(knNr, fgId ) && ( knNr2NumObj.get(knNr) == 1 ) ) {   // NOSONAR
		        		Q aq = systemConfiguration.getFirstQ( knNr, fgId );
		        		if( aq != null ) {
		        			qs.add( aq );
		        		}
		        	}
	        	}
	        } else {
		        Q q = systemConfiguration.getQ( knNr, Integer.parseInt( objDe ), fgId );
		        if( ( q == null ) && ( fgId == 3 ) ) {
		        	q = systemConfiguration.getFirstQ( knNr, fgId );
		        	
			        if( q != null ) {
			        	String firstDe = "" + q.getDeNr();
			        	
			        	Set<String> qDes = knNr2QDes.get( knNr );
			        	if( ( qDes == null ) || qDes.isEmpty() ) {
			        		objDe = firstDe;
			        		qs.add( q );
			        	}
			        }
		        } else {
		        	if( q != null ) {
		        		qs.add( q );
		        	}
		        }
	        }
	        
            if( !hasCoord ) {
    	        if( !qs.isEmpty() ) {
    	        	noCoordIds.add( permId );
    	        }
        		break;
        	}
            
	        if( qs.isEmpty() ) {
	            noSysIds.add( permId );
	            continue;
	        }
           
            for( Q q : qs ) {      // NOSONAR
	        
	        	List<Property> deProperties = new ArrayList<>();
	        	deProperties.addAll( properties );
	        	deProperties.add( new Property( ATTR_DE, objDe ) );
		        
		        if( q != null ) {
		        	
		        	if( ( road == null ) || road.isEmpty() ) {
		        		road = q.getRoad();
		        	}
		        	
		            String objId = seenQs.get( q.getId() );
		            if( objId != null ) {
		            	LOGGER.debug( "<" + permId + ">: Q <" + q.getId() + "> already assigned to <" + objId + ">" );
		            	sameObjects.put( permId, objId );
		            	continue;
		            }
		            
		            if( q.getTypeName() != null ) {
			        	if( fg.equalsIgnoreCase( FG3 ) ) {   // NOSONAR
			        		deProperties.add(new Property( ATTR_UDE_TYPE, "" + q.getTypeName() ) );
			        	}
		            }
		        	
		        	int glt = q.getGlanetype();
		        	if( glt != -1 ) {
		        		String gltName = glanetype2Name.get( glt );
		        		if( gltName != null ) {
		        			deProperties.add(new Property( ATTR_GLANE_TYPE, "" + gltName ) );
		        		} else {
		        			deProperties.add(new Property( ATTR_GLANE_TYPE, "" + glt ) );
		        		}
		        	}
		            
		            seenQs.put( q.getId(), permId );
		        } else {
		            noSysIds.add( permId );
		            continue;
		        }
		        
				DirectedGeoFeature dgf = new DirectedGeoFeature( q.getId(), name, 
				                                                 geometry, wlsSrid, 
				                                                 type, deProperties, 
				                                                 bearing, cp );
				
				addLocationProperties( dgf, road, de, q );
				
				dgfs.add( dgf );
            }
        }
		
		return dgfs; 
	}
	
	private List<String> getQDes( int knNr, List<String> objDes, int fgId ) {
		List<String> relevantDes = new ArrayList<>();
		Set<String> qids = new HashSet<>();
		for( String objDe : objDes ) {
			Q q = systemConfiguration.getQ( knNr, Integer.parseInt( objDe.trim() ), fgId );
			if( q != null ) {
				String qid = q.getId();
				if( !qids.contains( qid ) ) {
					relevantDes.add( objDe );
				}
				
				qids.add( qid );
			}
		}
		
		return relevantDes;
	}

	
	private void addLocationProperties( DirectedGeoFeature dgf, String routeNum, String deNummer, Q q ) {  // NOSONAR
		
		String mainDir = null;
	    
	    RoadNetRestrictionParameter rnrp = 
	            new RoadNetRestrictionParameter( RoadNetRestrictionTlsConfig.class.getName(), 
	                                             new ArrayList<>() );
	    
	    int deNr = Integer.parseInt( deNummer  );
	    String rn = routeNum;
	    if( !Q.getD2Carriageway( deNr ).equals( Q.D2_MAIN_CARRIAGEWAY ) ) {
	        rn = "";
	    }
	    
	    List<Property> properties = new ArrayList<>();
	    properties.add( new Property( "routeNum", rn ) );
	    
        if( fg.equalsIgnoreCase( fg1 ) ) {
    	    String direction = Q.getDrivingDirection( deNr );
            properties.add( new Property( "drivingDirection", direction ) );

            Property pdir = new Property( ATTR_DIRECTION, direction );
    	    dgf.getProperties().add( pdir );
        }
        
        rnrp.getProperties().addAll( properties );
        
		String reId1 = "";    // NOSONAR
        try {
            
            Location loc = roadMapPlugin.convertLocation( PointLocation.class.getName(), dgf, rnrp );
            if( !( loc instanceof PointLocation ) ) {
                noRnDirMatchNetLocIds.add( dgf.getId() );
            } else {
            	PointLocation pl = (PointLocation)loc;
                reId1 = pl.getElement().getId();    // NOSONAR
                
                if( fg.equalsIgnoreCase( fg4 ) ) {
                	de.heuboe.wls.data.wls.Direction dir = de.heuboe.wls.data.wls.Direction.POSITIVE; 
                	if( pl.getElement().isInverseOrder() ) { 
                	   dir = de.heuboe.wls.data.wls.Direction.NEGATIVE; 
                	}
                	List<RoadComponent> rcs = dre2Rcs.get( new DirectedReKey( reId1, dir ) );
                	if( rcs != null && !rcs.isEmpty() ) {
                		String rcId = rcs.get(0).getId();
                		int p = rcId.indexOf( ' ' );
                		if( p != -1 && ( rcId.length() > p + 1 ) ) {
                			mainDir = rcId.substring( p + 1, p + 2 );
                		}
                	}
                }
                
            }
            
            
        } catch (Fault e) {
            noRnDirMatchNetLocIds.add( dgf.getId() );
        }
	    
        @SuppressWarnings("unused")
		String reId2 = "";   // NOSONAR
	    PointLocation pLoc = null;
	    try {
            Location loc = roadMapPlugin.convertLocation( PointLocation.class.getName(), dgf, null );
            if( !( loc instanceof PointLocation ) ) {
                noNetLocIds.add( dgf.getId() );
                return;
            }
            
            pLoc = (PointLocation)loc;
            reId2 = pLoc.getElement().getId();   // NOSONAR
        } catch (Fault e) {
            noNetLocIds.add( dgf.getId() );
            return;
        }
	    
	    Property reId = new Property( ATTR_RE_ID, pLoc.getElement().getId() );
	    dgf.getProperties().add( reId );
        Property reOffset = new Property( ATTR_RE_OFFSET, "" + kmFormatter.format( pLoc.getFromOffset() / 1000.0 ) );
        dgf.getProperties().add( reOffset );
        
        boolean io = pLoc.getElement().isInverseOrder();
        Property reDirection = new Property( ATTR_RE_DIRECTION, io ? Direction.NEGATIVE.name() : Direction.POSITIVE.name() );
        dgf.getProperties().add( reDirection );
        
        Double heading = null;
        RoadElement re = roadMapPlugin.getRoadMap().getRoadElement( pLoc.getElement().getId() );
        if( re != null ) {
            heading = GeometryTools.bearing( re.getGeometry(), Math.max( pLoc.getFromOffset(), 0.0) );
            if( io ) {
                heading += 180.0;
                if( heading >= 360.0 ) {
                    heading -= 360.0;
                }
            }
        }
        if( heading != null ) {
            Property bearing = new Property( ATTR_BEARING, "" + Math.floor( heading ) );
            dgf.getProperties().add( bearing );
        }
        
        if( q != null ) {
            String road = q.getRoad();
            if( ( road != null ) && !road.isEmpty() ) {
                Property babnrProp = new Property( ATTR_BABNR, road );
                dgf.getProperties().add( babnrProp );
            }
        }
        
        if( mainDir != null ) {
            Property fahrtrichtung = new Property( ATTR_ROAD_DIR, mainDir );
            dgf.getProperties().add( fahrtrichtung );
        }
        
        addAlertCLocation( dgf, pLoc, deNummer );
    }
	
	private void addAlertCLocation( DirectedGeoFeature dgf, PointLocation pLoc, String deNummer ) {     // NOSONAR
        if( ( pLoc != null ) && ( deNummer != null ) ) {     // NOSONAR
            int deNr = Integer.parseInt( deNummer );
            if( deNr != 0 ) {     // NOSONAR
                String decw = Q.getD2Carriageway( deNr );
                
                de.heuboe.wls.data.wls.Direction roadDir = de.heuboe.wls.data.wls.Direction.POSITIVE; 
                if( pLoc.getElement().isInverseOrder() ) {
                    roadDir = de.heuboe.wls.data.wls.Direction.NEGATIVE;
                }
                DirectedReKey dre = new DirectedReKey( pLoc.getElement().getId(), roadDir );
                List<RoadComponent> rcs = dre2Rcs.get( dre );
                
                if( decw.equals( Q.D2_MAIN_CARRIAGEWAY ) ) {
                    
                    if( rcs == null ) {
                        if( fg.equals( FG1 ) ) {
                            deMainRoadNotOnRc.add( dgf.getId() );
                        }
                    } else {
                        
                        boolean roadMatch = false;
                        String road = id2Road.get( dgf.getId() );
                        if( road != null ) {
                            for( RoadComponent rc : rcs ) {
                                if( rc.getRouteNum().equals( road ) ) {
                                    roadMatch = true;
                                }
                            }
                            if( !roadMatch ) {
                                roadMismatchIds.put( dgf.getId(), road + "/" + rcs.get(0).getRouteNum() );
                            }
                        }
                        
                    
                        try {
                            Location loc = tmcRoadnetPlugin.convertLocation( TmcRefPoint.class.getName(), pLoc, null );
                            if( loc instanceof TmcRefPoint ) {
                                
                                TmcRefPoint trp = (TmcRefPoint)loc;
                                
                                String primLoc = trp.getLocationId();    
                                Property primLocProp = new Property( ALC_PRIM_LOC, primLoc );
                                dgf.getProperties().add( primLocProp );
        
                                Double dist = trp.getDistance();
                                if( dist == null ) {
                                    dist = 0.0;
                                }
                                Property distProp = new Property( ALC_DIST, "" + kmFormatter.format( dist ) );
                                dgf.getProperties().add( distProp );
                                
                                TmcLocation tmcLoc = tmcRoadnetPlugin.getTmcTable().getTmcLocation( primLoc );
                                if( ( tmcLoc != null ) && ( tmcLoc.getFirstName()  != null ) ) {
                                    Property primLocNameProp = new Property( ALC_PRIM_LOC_NAME, tmcLoc.getFirstName() );
                                    dgf.getProperties().add( primLocNameProp );
                                }
                                
                                de.heuboe.wls.data.wls.Direction dir = trp.getDirection();
                                if( dir != null ) {
                                    Property dirProp = new Property( ALC_DIR, dir.name() );
                                    dgf.getProperties().add( dirProp );
                                }
                                
                            } else if( loc instanceof ErrorLocation ) {
                                alcErrorMsgs.add( "Cannot not determine AlertC location for <"  + dgf.getId() + ">: " + ((ErrorLocation)loc).getMessage() );
                            }
                        } catch( Fault ex ) {
                            alcErrorMsgs.add( "Cannot not determine AlertC location for <"  + dgf.getId() + ">: " + ex.toString() );
                        }
                    }
                } else {
                    if( ( rcs != null) && fg.equals( FG1 ) ) {
                        id2NetRoad.put( dgf.getId(), rcs.get(0).getRouteNum() );
                        deNotMainRoadOnRc.add( dgf.getId() );
                    } 
                    
                }
            }
        }
	}
	
	/**
	 * 
	 * For Config objects without coordinate data retrieve located object with the same KnotenNr and use its coordinate
	 * 
	 * @param fg				Funktionsgruppe
	 * @param missingCfgObjects	Config objects without coordinate data
	 * @param gfs				GeoFeatures
	 */
    public void addMissingGeoFeatures( int fg, List<Q> missingCfgObjects, List<DirectedGeoFeature> gfs ) {  // NOSONAR
    	
		List<Q> mcos = new ArrayList<>();
    	
    	for( Q cfgObject : missingCfgObjects ) {
    		
    		DirectedGeoFeature copyGf = null;
    		for( DirectedGeoFeature gf : gfs ) {
    			
    			String gfKnNr = GeoFeatureUtil.getProperty( gf, ATTR_KNOTEN_NR );
    			if( gfKnNr != null ) {
	    			if( cfgObject.getKnNr() == Integer.parseInt( gfKnNr ) ) {    // NOSONAR
	    				
	    				copyGf = new DirectedGeoFeature();
	    				copyGf.setId( cfgObject.getId() ); 
	    				copyGf.setName( gf.getName() ); 
	    				copyGf.setGeometry( gf.getGeometry() );
	    				copyGf.setSrid( gf.getSrid() );
	    				copyGf.setType( gf.getType() );
	    				copyGf.setHeading( gf.getHeading() );
	    				copyGf.setCompassPoint( gf.getCompassPoint()  );

	    				List<Property> props = new ArrayList<>( gf.getProperties() );
	    				copyGf.getProperties().addAll( props );
	    				
	    	        	if( fg == 3 ) {
	    	        		String ufdType = cfgObject.getTypeName();
	    	        		if( ufdType != null ) {
	    	    				GeoFeatureUtil.removeProperty(copyGf, ATTR_UDE_TYPE);
	    	    				GeoFeatureUtil.setProperty( copyGf, ATTR_UDE_TYPE, "" + ufdType );
	    	        		}
	    	        	}

	    				GeoFeatureUtil.removeProperty(copyGf, ATTR_DE);
	    				GeoFeatureUtil.setProperty( copyGf, ATTR_DE, "" + cfgObject.getDeNr() );
	    				
	    				gfs.add( copyGf );
		    			break;
	    			}
    			}
    		}
    		
    		if( copyGf == null ) {
    			mcos.add( cfgObject );
    		}
    	}
    	
		
		if( !mcos.isEmpty() ) {
			LOGGER.warn( "No coordinate data for configuration objects [FG" + fg +  "]:") ;
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


	private CompassPoint createCompassPoint(Integer bearing) {
		if (bearing == null) {
			return null;
		}
		return GeometryTools.getCompassPoint(bearing);
	}

	private Double getDouble( String area, Data member) {
		if (member.isNull() || member.getAsString().isEmpty()) {
			return null;
		}
		
		String v = null;

		if( area.equalsIgnoreCase( "nord" ) ) {
			v = prepareCoordinate( member.getAsString() );
		} else {
			v = member.getAsString().replace(',', '.');
		}
		try {
			return Double.parseDouble(v);
		} catch( NumberFormatException ex) {
			LOGGER.error( "Cannot convert <" + v + "> to double!" );
			return 0.0;
		}
	}


    public String getType() {
        return type;
    }

    public Map<String, DirectedGeoFeature> getFeatureMap() {
        return featureMap;
    }
	
    public List<CfgKey> getCfgKeys() {
    	return cfgKeys;
    }
}
