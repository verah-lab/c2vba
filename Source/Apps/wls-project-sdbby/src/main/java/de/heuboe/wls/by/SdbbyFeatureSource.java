package de.heuboe.wls.by;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.heuboe.data.Data;
import de.heuboe.data.DataReader;
import de.heuboe.data.DataStore;
import de.heuboe.data.Filter;
import de.heuboe.data.csv.CsvDataStore;
import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;
import de.heuboe.wls.by.cfg.ConfigService;
import de.heuboe.wls.by.cfg.Q;
import de.heuboe.wls.by.cfg.SystemConfiguration;
import de.heuboe.wls.data.geofeature.GeoFeature;
import de.heuboe.wls.data.roadmap.ParameterAttributes;
import de.heuboe.wls.data.roadmap.RoadElement;
import de.heuboe.wls.data.roadmap.RouteDefinition;
import de.heuboe.wls.data.roadmap.SegmentedAttribute;
import de.heuboe.wls.data.sdbby.SdbbyRoute;
import de.heuboe.wls.data.sdbby.SdbbyStrategy;
import de.heuboe.wls.data.tcc.TccPointFeature;
import de.heuboe.wls.data.tcc.TccPointFeatureGroup;
import de.heuboe.wls.data.wls.Direction;
import de.heuboe.wls.data.wls.ErrorLocation;
import de.heuboe.wls.data.wls.GeoLocation;
import de.heuboe.wls.data.wls.LinearLocation;
import de.heuboe.wls.data.wls.Location;
import de.heuboe.wls.data.wls.Parameter;
import de.heuboe.wls.data.wls.ParameterDistance;
import de.heuboe.wls.data.wls.ParameterList;
import de.heuboe.wls.data.wls.PointLocation;
import de.heuboe.wls.data.wls.Property;
import de.heuboe.wls.geofeature.GeoFeatureUtil;
import de.heuboe.wls.iface.Fault;
import de.heuboe.wls.plugin.roadmap.RoadMapPlugin;
import de.heuboe.wls.plugin.roadmap.RoadNetPlugin;
import de.heuboe.wls.roadmap.GeoToNormConverter;
import de.heuboe.wls.roadmap.GeoToRouteConverter;
import de.heuboe.wls.roadmap.NormToGeoConverter;
import de.heuboe.wls.roadmap.RoadMapConstants;
import de.heuboe.wls.roadmap.RoadNet;
import de.heuboe.wls.utils.CoordinateConverter;
import de.heuboe.wls.utils.CoordinateTransformer;
import de.heuboe.wls.utils.GeometryTools;
import de.heuboe.wls.utils.LineMover;
import de.heuboe.wls.utils.MultiSource;
import eu.vmis_ehe.vmis2.configservice.CfgAq;

/**
 * 
 * Reads TLS-Konf-Tab files, WWW chain definition file and and strategy routes file 
 * 
 * Creates 
 *         TccPointFeature from AQs
 *         SdbbyRoutes from strategy route definitions
 *         SdbbyStrategy
 * 
 * @author peters
 *
 */
public class SdbbyFeatureSource implements MultiSource {
	
	@lombok.Data
	class AqLocation {
		private String id;
		private String type;
		private String roadNr;
		private String direction;	// 'S', 'N', 'W', 'O'
		private Double km = null;
		private Double latitude = null;
		private Double longitude = null;
		private Double bearing = null;
	}

	private static final Logger LOGGER = Logger.getLogger(SdbbyFeatureSource.class);
	private static final double EPS = 0.5;
	private static final String STRATEGY = "Strategy ";
	private static final String INVALID_COORDINATES = ": invalid coordinates ";
	
	private static final String WWWPANEL_LINE_START = "WWWPANEL\t";
	
	private static final String AQ_ID_TO_CLUSTER_ID_FILE = "idConversion/aqId2ClusterId.txt";
	
	private boolean sampleMode = false;

	@Autowired
	private ConfigService configService;

	private static List<String> typeNames = Arrays.asList(
			//TccPointFeature.class.getName(),
			TccPointFeatureGroup.class.getName(),
			SdbbyStrategy.class.getName()
			);
	
	private static final String STRATEGIE = "STRATEGIE";
	
	private static final String TYPE_FIELD = "TYP";
	private static final String ID_FIELD = "ID";
	private static final String NAME_FIELD = "NAME";
	private static final String BABNR_FIELD = "BAB";
	private static final String BABKM_FIELD = "BABKM";
	private static final String FAHRTRICHTUNG_FIELD = "FAHRTRICHTUNG";
	private static final String DE_FIELD = "DE";
	private static final String EAK_FIELD = "EAK";
	private static final String LATITUDE_FIELD = "LATITUDE";
	private static final String LONGITUDE_FIELD = "LONGITUDE";
	
	private static final String FAHRTRICHTUNG_PROPERTY = "FAHRTRICHTUNG";
	private static final String BABNR_PROPERTY = "BAB";
	private static final String BABKM_PROPERTY = "BABKM";
	private static final String WINKEL_PROPERTY = "WINKEL";
	private static final String BEARING_PROPERTY = "BEARING";
	private static final String AQTYPE_PROPERTY = "AQTYPE";
	private static final String SEQNUM_PROPERTY = "SEQNUM";

	private static final double MIN_OFFSET = 10.0;
	private static final double DRIFT = 0.1;
	
	private String filenameAq;
	private String filenameWww;
	private String filenameStrg;
	private String fileDirStrategyRoutes;
	private String aqLocationFile;
	private int wlsSrid;
	private GeometryFactory factory;
	private Map<Integer, CoordinateTransformer> transformers = new HashMap<>();
	
	private RoadNet roadNet;
	private NormToGeoConverter normToGeoConverter;
	private GeoToRouteConverter geoToRouteConverter;
	private RoadMapPlugin roadMapPlugin;
	private RoadNetPlugin roadNetPlugin;
	private GeoToNormConverter geoConverter;
	private SystemConfiguration systemConfiguration;
	
	private Map<String,AqLocation> id2AqLocation = new HashMap<>();
	private Map<String,GeoFeature> id2AqGeoFeature = new HashMap<>();
	
	private List<TccPointFeature> features;
	private List<TccPointFeatureGroup> featureGroups;
	private Map<String, SdbbyStrategy> strategyMap;
	private Map<String, SdbbyRoute> routeMap;
	private Map<String,LinearLocation> routeLinLocMap = new HashMap<>();
	
	@Override
	public void init() {
		if (filenameAq == null || filenameWww == null || filenameStrg == null || wlsSrid == 0 || roadNet == null) {
			throw new IllegalArgumentException("SdbbyFeatureSource.init: not properly initialized");			
		}
		features = new ArrayList<>();
		featureGroups = new ArrayList<>();
		strategyMap = new HashMap<>();
		routeMap = new HashMap<>();
		
		geoConverter = new GeoToNormConverter(roadNet);
		geoToRouteConverter = new GeoToRouteConverter(roadNet, wlsSrid, 10.);
		factory = new GeometryFactory(new PrecisionModel(), wlsSrid);
		
		normToGeoConverter = new NormToGeoConverter(roadNet);
		
		readAqs();
		readWWWs();
		readRouteLinearLocations();
		readRoutes();
		readStrategies();
	}
	
	private void readRouteLinearLocations() {
	    ObjectMapper objectMapper = new ObjectMapper();

	    File dir = new File( fileDirStrategyRoutes );
		if( !dir.exists() || !dir.isDirectory() ) {
			throw new IllegalArgumentException( "<" + fileDirStrategyRoutes + "> is not a directory" );
		}
		
		File[] files = dir.listFiles();
		
		if( ( files == null ) || ( files.length == 0 ) ) {
			throw new IllegalArgumentException( "<" + fileDirStrategyRoutes + "> is not a route location file directory" );
		}
		
		for( File f : files ) {
			if( !f.isDirectory() ) {
				String id = f.getName();
				try {
					String xml = FileUtils.readFileToString( f, StandardCharsets.UTF_8 );
					LinearLocation linLoc = objectMapper.readValue( xml, LinearLocation.class );
					routeLinLocMap.put( id, linLoc );
				} catch (IOException e) {
					throw new IllegalArgumentException( "Cannot read route location file <" + f.getName() + ">" );
				}
			}
		}
	}

	/**
	 * 
	 * Reads route configuration
	 * 
	 */
	public void readRoutes() {		  // NOSONAR
		LOGGER.info("reading route file: " + filenameStrg);
		SdbbyRoute currentRoute = null;
		SortedMap<Integer, Coordinate> currentCoors = new TreeMap<>();
		SortedMap<Integer, String> currentRoadElems = new TreeMap<>();
		Properties properties = new Properties();
		properties.setProperty(CsvDataStore.SEPARATOR_KEY, "\t");
		properties.setProperty( CsvDataStore.CHARSET_KEY, StandardCharsets.ISO_8859_1.name() );
		DataStore store = new CsvDataStore(null, filenameStrg, properties);
		DataReader reader = store.getReader();
		
		boolean hasErrors = false;
		
		try {
			int pos = 0;
			int line = 0;
			String currentId = "";
			while (reader.hasNext()) {
				line++;
				
				Data record = reader.next();
				String id = record.getMember("ID").getAsString();
				String name = record.getMember("NAME").getAsString();
				String type = record.getMember("TYPE").getAsString();
				if ("ROUTE".equals(type)) {
					if( currentRoute != null ) {
						if( !createLineGeometry(currentRoute, currentCoors, currentRoadElems) ) {  // NOSONAR
							hasErrors = true;
						}
					}
					currentCoors.clear();
					currentRoadElems.clear();
					currentRoute = new SdbbyRoute();
					currentRoute.setId(id);
					currentRoute.setName(name);
					pos = 0;
					routeMap.put(id, currentRoute);
					
					currentId = id;
				}
				if ("ROUTE+".equals(type) || "ROUTE".equals(type)) {
					++pos;
					
					if( hasCoordinate( record ) ) {
						Coordinate c = createCoordinate( currentId, line, record );
						if (c != null) {
							currentCoors.put(pos, c);
						} else {
							hasErrors = true;
						}
					} else {
						String re = record.getMember("ROADELEMENT").getAsString();
						if( !re.isEmpty() ) {
							currentRoadElems.put(pos, re);
						} else {
							LOGGER.error(  STRATEGY + currentId + ", line " + line + ": no ROADELEMENT" );    // NOSONAR
							hasErrors = true;
						}
					}
				}
				if (STRATEGIE.equals(type)) {
					if( currentRoute != null ) {   // NOSONAR
						String sname = record.getMember("NAME").getAsString();
						if( ( sname != null ) && !sname.isEmpty() ) {
							currentRoute.setName(sname);
						}
					}
				}
			}
			// don't forget the last one...
			if (currentRoute != null) {
				if( !createLineGeometry(currentRoute, currentCoors, currentRoadElems) ) {  // NOSONAR
					hasErrors = true;
				}
			}
		} finally {
			if ( reader != null ) {
				reader.close();
			}
		}
		
		if( hasErrors ) {
			throw new IllegalArgumentException( "Cannot create geometries from route location definitions");
		}
	}

	/**
	 * 
	 * Reads strategy configuration
	 * 
	 */
	public void readStrategies() {		
		LOGGER.info("reading strategy file: " + filenameStrg);
		SdbbyStrategy currentStrategy = null;
		Properties properties = new Properties();
		properties.setProperty(CsvDataStore.SEPARATOR_KEY, "\t");
		properties.setProperty( CsvDataStore.CHARSET_KEY, StandardCharsets.ISO_8859_1.name() );
		DataStore store = new CsvDataStore(null, filenameStrg, properties);
		DataReader reader = store.getReader();
		try {
			while (reader.hasNext()) {    // NOSONAR
				Data record = reader.next();
				String id = record.getMember("ID").getAsString();
				String name = record.getMember("NAME").getAsString();
				String type = record.getMember("TYPE").getAsString();
				if (STRATEGIE.equals(type)) {
					currentStrategy = new SdbbyStrategy();
					currentStrategy.setId(id);
					currentStrategy.setName(name);
					currentStrategy.setAreaCode(record.getMember("AREA").getAsString());
					SdbbyRoute mainRoute = routeMap.get(record.getMember("MAINROUTE").getAsString());
					SdbbyRoute altRoute = routeMap.get(record.getMember("ALTERNATEROUTE").getAsString());
					if (mainRoute == null) {
						LOGGER.fatal("Invalid main route for strategy " + id);
						continue;
					}
					SdbbyRoute route = copySdbbyRoute(mainRoute);
					route.setMainRoute(true);
					route.setRecommended(altRoute == null);
					currentStrategy.getRoutes().add(route);
					Coordinate[] coors = route.getGeometry().getCoordinates();
					
					Coordinate triggerCoor = coors[0];
					Coordinate triggerCoorDest = coors[coors.length - 1];
					try {
						Geometry triggerGeometry = createPointGeometry(wlsSrid, triggerCoor.x, triggerCoor.y);
						GeoLocation triggerLocation = new GeoLocation(id, name, triggerGeometry, wlsSrid, triggerGeometry.getEnvelopeInternal());
						currentStrategy.setTrigger(triggerLocation);
						
						// Trigger destination
						triggerGeometry = createPointGeometry(wlsSrid, triggerCoorDest.x, triggerCoorDest.y);
						triggerLocation = new GeoLocation(id, name, triggerGeometry, wlsSrid, triggerGeometry.getEnvelopeInternal());
						currentStrategy.setTriggerDestination( triggerLocation );

					} catch (FactoryException | TransformException e) {
						LOGGER.fatal("cannot create trigger for strategy " + id);
						continue;
					}
					if (altRoute != null) {
						route = copySdbbyRoute(altRoute);
						route.setMainRoute(false);
						route.setRecommended(true);
						currentStrategy.getRoutes().add(route);
					}
					strategyMap.put(id, currentStrategy);
				}
				if ("STRATEGIE+".equals(type) || STRATEGIE.equals(type)) {
					currentStrategy.getWwwPanelSet().add(record.getMember("WWW").getAsString());
				}
			}
		} finally {
			if ( reader != null ) {
				reader.close();
			}
		}
	}

	private SdbbyRoute copySdbbyRoute(SdbbyRoute r) {		
		return new SdbbyRoute(r.getId(), r.getName(), r.getGeometry(), r.getSrid(), r.getLinearLocation(), r.isMainRoute(), r.isRecommended());
	}
	
	private void readAqLocations() {
		
		LOGGER.info( "Read " +  aqLocationFile + " ... ");
		
		Properties props = new Properties();
		props.setProperty( CsvDataStore.SEPARATOR_KEY, ";" );
		props.setProperty( CsvDataStore.CHARSET_KEY, StandardCharsets.ISO_8859_1.name() );
		DataStore store = new CsvDataStore(null, aqLocationFile, props);
		DataReader reader = store.getReader();
		int n = 0;
		try {
			while (reader.hasNext()) {
				n++;
				Data record = reader.next();
				
				AqLocation aqLocation = new AqLocation();
				aqLocation.setId( record.getMember("ID").getAsString() );
				aqLocation.setType( record.getMember("TYPE").getAsString() );
				aqLocation.setRoadNr( record.getMember("ROADNR").getAsString() );
				aqLocation.setDirection( record.getMember("FR").getAsString() );
				
				if( !record.getMember("KM").isNull() && !record.getMember("KM").getAsString().isEmpty() ) {
					aqLocation.setKm( record.getMember("KM").getAsDouble() );
				}
				if( !record.getMember("LAT").isNull() && !record.getMember("LAT").getAsString().isEmpty() ) {
					aqLocation.setLatitude( record.getMember("LAT").getAsDouble() );
				}
				if( !record.getMember("LON").isNull() && !record.getMember("LON").getAsString().isEmpty() ) {
					aqLocation.setLongitude( record.getMember("LON").getAsDouble() );
				}
				if( !record.getMember(BEARING_PROPERTY).isNull() && !record.getMember(BEARING_PROPERTY).getAsString().isEmpty() ) {
					aqLocation.setBearing( record.getMember(BEARING_PROPERTY).getAsDouble() );
				}
				
				id2AqLocation.put( aqLocation.getId(), aqLocation );
			}
			
			LOGGER.info( aqLocationFile + ": " + n + " records" );

			
		} catch (Exception ex ) {
			LOGGER.error( ex.toString() );
			LOGGER.fatal( CallStack.getStackTraceAsString( ex ) );
			throw new IllegalArgumentException( "SdbbyFeatureSource.init: error reading AQ locations from " + 
			                                    aqLocationFile + ", line " + n + ": " + ex.toString() );			
		}
	}

	private void readWWWs() {    // NOSONAR
		LOGGER.info("reading WWW file: " + filenameWww);
		
		List<String> aqCoords = new ArrayList<>();
		aqCoords.add( "ID;TYPE;ROADNR;FR;KM;LAT;LON;BEARING" );

		TccPointFeatureGroup currentFeatureGroup = null;
		Properties props = new Properties();
		props.setProperty( CsvDataStore.SEPARATOR_KEY, "\t" );
		props.setProperty( CsvDataStore.CHARSET_KEY, StandardCharsets.ISO_8859_1.name() );
		DataStore store = new CsvDataStore(null, filenameWww, props);
		DataReader reader = store.getReader();
		try {
			int n=0;
			while (reader.hasNext()) {
				Data record = reader.next();
				String id = record.getMember("ID").getAsString();
				String name = record.getMember("NAME").getAsString();
				String type = record.getMember("TYPE").getAsString();
				List<Property> properties = new ArrayList<>();
				if ("WWWSET".equals(type)) {
					if (currentFeatureGroup != null) {
						setGeometry(currentFeatureGroup);
					}
					currentFeatureGroup = new TccPointFeatureGroup(id, name, null, null, null, properties);
					featureGroups.add(currentFeatureGroup);
					n=0;
				}
				if ("WWWPANEL".equals(type)) {
					int srid = record.getMember("SRID").getAsInt();
					String xStr = record.getMember("X").getAsString().trim();
					double x = Double.parseDouble(xStr.replace(',', '.'));
					String yStr = record.getMember("Y").getAsString().trim();
					double y = Double.parseDouble(yStr.replace(',', '.'));
					try {  // NOSONAR
						Geometry geometry = createPointGeometry(srid, x, y);
						Integer bearing = getBearing(id, geometry);
						if (bearing != null) {
							properties.add(new Property(WINKEL_PROPERTY, Integer.toString(bearing)));
						}
						properties.add(new Property(AQTYPE_PROPERTY, "WWW"));
						properties.add(new Property(SEQNUM_PROPERTY, Integer.toString(++n)));						
						TccPointFeature wwwFeature = new TccPointFeature(id, name, geometry, srid, properties);
						currentFeatureGroup.getPointFeatures().add(wwwFeature);
						
						if( sampleMode ) {
							features.add(wwwFeature);
							String aqCoord = id + ";WWW;" + ";;" + 
					                 ";" + x + ";" + y + ";" + ( ( bearing == null ) ? -1 : bearing );
							aqCoords.add( aqCoord );
						}
						
					} catch (FactoryException | TransformException e) {
						LOGGER.fatal("cannot create WWW " + id + ": " + e);
					}
				}
			}
			setGeometry(currentFeatureGroup);
		} catch( Throwable ex )  {     // NOSONAR
			LOGGER.error( ex.toString() );
			LOGGER.fatal( CallStack.getStackTraceAsString( ex ) );
			throw new IllegalArgumentException("SdbbyFeatureSource.init: error reding WWWs: " + ex.toString() );			
		} finally {
			
			if( sampleMode ) {
				try {
					FileUtils.writeLines(  new File( filenameWww + "-aq-loc.csv" ), StandardCharsets.ISO_8859_1.name(), aqCoords );
				} catch ( IOException ex ) {
					LOGGER.error( ex.toString() );
				}
			}
			if ( reader != null ) {
				reader.close();
			}
		}
	}

	private Geometry createPointGeometry(int srid, double x, double y) throws FactoryException, TransformException {
		if (srid != wlsSrid) {
			CoordinateTransformer transformer = transformers.get(srid);    // NOSONAR
			if (transformer == null) {
				transformer = new CoordinateTransformer(srid, wlsSrid);
				transformers.put(srid, transformer);
			}
			Coordinate[] c = transformer.transform(new Coordinate(x, y));
			return factory.createPoint(c[0]);
		}
		return factory.createPoint(new Coordinate(x, y));
	}
	
	private boolean createLineGeometry( SdbbyRoute route, SortedMap<Integer, Coordinate> currentCoors, SortedMap<Integer,String> currentRoadElems) {
		if (currentCoors.isEmpty()) {
			return createLineGeometryByRoadElems(route, currentRoadElems);
		} else {
			return createLineGeometryByCoors(route, currentCoors); 
		}
	}

	private boolean createLineGeometryByCoors(SdbbyRoute route, SortedMap<Integer, Coordinate> coorMap ) {
		
		try {
			
		
			if( coorMap.size() < 2 ) {
	            LOGGER.error( STRATEGY + route.getId() + ": too few coordinates" );
	            return false;
			}
			List<Coordinate> coors = new ArrayList<>( coorMap.values() );
			
			Location routeLoc;
			
			LinearLocation ll = routeLinLocMap.get( route.getId() );
			if( ll != null ) {
				routeLoc = ll;
			} else {
		        Location startGeo = new GeoLocation( "S", "S", GeometryTools.createPoint( coors.get(0 ) ), wlsSrid, null );
		        Location endGeo = new GeoLocation( "E", "E", GeometryTools.createPoint( coors.get( coors.size() -1 ) ), wlsSrid, null );
		        
		        Location startLoc = roadMapPlugin.convertLocation( PointLocation.class.getName(), startGeo, null );
		        if( ( startLoc == null ) || ( startLoc instanceof ErrorLocation ) ) {
		            LOGGER.error( STRATEGY + route.getId() + INVALID_COORDINATES + coors.get( 0 ).toString()  );
		            return false;
		        }
		
		        Location endLoc = roadMapPlugin.convertLocation( PointLocation.class.getName(), endGeo, null );
		        if( ( endLoc == null ) || ( endLoc instanceof ErrorLocation ) ) {
		            LOGGER.error( STRATEGY + route.getId() + INVALID_COORDINATES + coors.get( coors.size() -1 ).toString()  );
		            return false;
		        }
		
		        List<Location> viaLocs = new ArrayList<>();
		        int i = 1;
		        for( Coordinate coor : coors.subList( 1, coors.size() -1 ) ) {
		            Location vpGeo = new GeoLocation( "V" + i, "V" + i, GeometryTools.createPoint( coor ), wlsSrid, null );
		            Location vp = roadMapPlugin.convertLocation( PointLocation.class.getName(), vpGeo, null );
		            
		            if( ( vp == null ) || ( vp instanceof ErrorLocation ) ) {
		                LOGGER.error( STRATEGY + route.getId() + INVALID_COORDINATES + coor.toString()  );
		                return false;
		            }
		            
		            viaLocs.add( vp);
		            i++;
		        }
		
		        RouteDefinition rd = new RouteDefinition("S-T", "S-T", startLoc, endLoc, viaLocs);
				
		        routeLoc = roadNetPlugin.convertLocation( LinearLocation.class.getName(), rd, null );
			} 
			
	        if( routeLoc instanceof LinearLocation ) {
	        	LinearLocation linLoc = (LinearLocation)routeLoc;
				Geometry geometry = normToGeoConverter.convertToGeometry( linLoc, null);
				route.setGeometry(geometry);
				route.setSrid(wlsSrid);
				route.setLinearLocation( linLoc );
	        } else {
	        	LOGGER.error( STRATEGY + route.getId() + ": no route for coordinates of strategy" );
	        }
        
		} catch( Fault ex ) {
            LOGGER.error( STRATEGY + route.getId() + ": error converting to WlS route: " + ex.toString()  );
            return false;
		}
        
        
        return true;
	}
	
	
	private boolean createLineGeometryByCoorsMapMatching(SdbbyRoute route, SortedMap<Integer, Coordinate> currentCoors) {
		
		
		Coordinate[] coordinates = currentCoors.values().toArray(new Coordinate[0]);
		LineString line = factory.createLineString(coordinates);
		
		GeoLocation geo = new GeoLocation(route.getId(), route.getName(), line, wlsSrid, line.getEnvelopeInternal());
		
		LinearLocation linloc = null;
		try {
			linloc = geoToRouteConverter.createLinearLocation(geo, null, null);
			if (linloc == null) {
				LOGGER.error( STRATEGY + route.getId() + ": cannot convert route to LinearLocation" );
				return false;
			}
		} catch( Fault ex ) {
			LOGGER.error( STRATEGY + route.getId() + ": cannot convert route to LinearLocation: " + ex.toString() );
			return false;
		}
			
		try {
			Geometry geometry = normToGeoConverter.convertToGeometry(linloc, null);
			route.setGeometry(geometry);
			route.setSrid(wlsSrid);
			route.setLinearLocation(linloc);
		} catch( Fault ex ) {
			LOGGER.error( STRATEGY + route.getId() + ": cannot convert route to geometry: " + ex.toString() );
			return false;
		}
		
		return true;
	}
	

	
	private boolean createLineGeometryByRoadElems(SdbbyRoute route, SortedMap<Integer, String> roadElems) {
		SortedMap<Integer, Coordinate> coors = new TreeMap<>();
		int pos = 0;
		for(String reId : roadElems.values()) {
			RoadElement re = roadNet.getRoadElement(reId);
			if (re == null) {
				LOGGER.error( STRATEGY + route.getId() + ": unknown RoadElement ID: " + reId );
				return false;
			}
			Geometry g = re.getGeometry();
			if (re.getDirection() == Direction.NEGATIVE) {
				g = g.reverse();
			}
			double offset = re.getLength() / 2.0;
			if (pos == 0) {
				offset = MIN_OFFSET;
			}
			if (pos == roadElems.size()-1) {
				offset = re.getLength() - MIN_OFFSET;
			}
			de.heuboe.geo.Geometry geometry = CoordinateConverter.toGeoGeometry(g);
			de.heuboe.geo.Geometry point = LineMover.movePoint(geometry, offset, DRIFT);
			if (point == null) {
				LOGGER.error( STRATEGY + route.getId() + ": cannot convert RoadElement " + reId + " to geometry" );
				return false;
			}
			Coordinate[] coordinates = CoordinateConverter.toJtsCoordinates(point.getCoordinates().toArray(new de.heuboe.geo.Coordinate[0]));
			coors.put(pos, coordinates[0]);
			++pos;
		}
		return createLineGeometryByCoorsMapMatching(route, coors);
	}
	
	private boolean hasCoordinate( Data record ) {
		int srid = record.getMember("SRID").getAsInt();
		String xStr = record.getMember("X").getAsString().trim();
		String yStr = record.getMember("Y").getAsString().trim();
		
		return ( srid != 0 ) && !xStr.isEmpty() && !yStr.isEmpty();
	}

	private Coordinate createCoordinate( String id, int line, Data record) {
		
		try { 
			int srid = record.getMember("SRID").getAsInt();
			String xStr = record.getMember("X").getAsString().trim();
			double x = Double.parseDouble(xStr.replace(',', '.'));
			String yStr = record.getMember("Y").getAsString().trim();
			double y = Double.parseDouble(yStr.replace(',', '.'));
			if (srid != wlsSrid) {
				CoordinateTransformer transformer = transformers.get(srid);     // NOSONAR
				if (transformer == null) {
					transformer = new CoordinateTransformer(srid, wlsSrid);
					transformers.put(srid, transformer);
				}
				Coordinate[] c = transformer.transform(new Coordinate(x, y));
				return c[0];
			}
			return new Coordinate(x, y);
		} catch( FactoryException | TransformException | NumberFormatException ex ) {
			LOGGER.error( STRATEGY + id + ", line " + line + " error converting coordinate: " + ex.toString()  );
		}
		
		return null;
	}

	private void setGeometry(TccPointFeatureGroup feature) {
		List<Coordinate> coordinates = new ArrayList<>();
		for(TccPointFeature f : feature.getPointFeatures()) {
			Coordinate[] coors = f.getGeometry().getCoordinates();
			coordinates.add(coors[0]);
		}
		Geometry geometry = null;
		if (coordinates.size() == 1) {
			geometry = GeometryTools.createPoint(wlsSrid, coordinates.get(0));
		}
		if (coordinates.size() > 1) {
			geometry = GeometryTools.createPolyline(wlsSrid, coordinates.toArray(new Coordinate[0]));
		}
		feature.setGeometry(geometry);
		feature.setSrid(wlsSrid);

		String fahrtr = getOrientation(geometry);
		if( fahrtr != null ) {
			feature.getProperties().add(new Property(FAHRTRICHTUNG_PROPERTY, fahrtr));
		}
	}
	
	private boolean copyProperty( GeoFeature gf, String srcName, String targetName, List<Property> properties, boolean nonEmpty ) {
		String tn = targetName;
		if( tn == null ) {
			tn = srcName;
		}
		String value = GeoFeatureUtil.getProperty( gf, srcName );
		if( ( value != null ) && ( !nonEmpty || !value.isEmpty() ) ) {
			properties.add(new Property( tn, value ));
			return true;
		}
		
		return false;
	}
	
	private boolean copyProperty( GeoFeature gf, String srcName, String targetName, List<Property> properties ) {
		return copyProperty( gf, srcName, targetName, properties, false );
	}

	
	private boolean copyProperty( GeoFeature gf, String name, List<Property> properties ) {
		return copyProperty( gf, name, name, properties, false );
	}

	private boolean setProperty( String value, String name, List<Property> properties, boolean onlyNonEmpty ) {
		if( ( value != null ) && ( !onlyNonEmpty || !value.isEmpty() )  ) {
			properties.add(new Property( name, value ));
			return true;
		}
		
		return false;
	}

	private boolean setProperty( String value, String name, List<Property> properties ) {
		return setProperty( value, name, properties, false );
	}

	private boolean setProperty( Double value, String name, List<Property> properties ) {
		if( value != null ) {
			return setProperty( "" + value, name, properties, false );
		} 
		
		return false;
	}
	
	private boolean setProperty( Enum<?> value, String name, List<Property> properties, boolean onlyNonEmpty ) {
		if( ( value != null ) && ( !onlyNonEmpty || !value.name().equals( "UNKNOWN" ) )  ) {
			properties.add(new Property( name, value.name() ));
			return true;
		}
		
		return false;
	}

	
	private void readAqsFromConfigService() {     // NOSONAR
		
		List<String> aqId2ClusterId = new ArrayList<>();
		readAqLocations();
		
		int locFromFileCount = 0;
		
		List<CfgAq> cfgAqs = configService.getAllAQs( null );
		for( CfgAq aq : cfgAqs ) {
			
			String id = aq.getId();
			
			aq.getClusterIdsList();
			if( sampleMode ) {
				aqId2ClusterId.add( id + ";" + aq.getClusterIdsList().get(0) );
			}
			
			String name = aq.getName();
			List<Property> properties = new ArrayList<>();
			
			boolean hasBearing = false;
			double lat = aq.getLocation().getLatitude();
			double lon = aq.getLocation().getLongitude();
			
			AqLocation aqLocation = id2AqLocation.get( id );
			if( aqLocation != null ) {
				
				locFromFileCount++;
				
				lat = aqLocation.getLatitude();
				lon = aqLocation.getLongitude();

				setProperty( aqLocation.getType(), AQTYPE_PROPERTY, properties, true );
				
				String road = aqLocation.getRoadNr();
				boolean hasRoad = setProperty( road, BABNR_PROPERTY, properties, true );
				if( !hasRoad ) {
					setProperty( aq.getLocation().getRoadId(), BABNR_PROPERTY, properties );
				}
				
				String dir = aqLocation.getDirection();
				setProperty( dir, FAHRTRICHTUNG_PROPERTY, properties, true );

				Double km = aqLocation.getKm();
				setProperty( km, BABKM_PROPERTY, properties );
				
				hasBearing = setProperty( aqLocation.getBearing(), WINKEL_PROPERTY, properties );
				setProperty( aqLocation.getBearing(), "Heading", properties );

				GeoFeature gf = id2AqGeoFeature.get( id );
				if( gf != null ) {
					
					try {
						Geometry  wgs84Geom = GeometryTools.transformGeometry( gf.getGeometry(), wlsSrid, CoordinateTransformer.WGS84_ID);
						lat = wgs84Geom.getCoordinate().getX();
						lon = wgs84Geom.getCoordinate().getY();
					} catch (Fault e) {
					}
					
					if( road == null || road.isBlank() ) {
						copyProperty( gf, QFileReader.ATTR_ROAD, BABKM_PROPERTY, properties, true );
					}
					if( km == null ) {
						copyProperty( gf, "Kilometer", BABKM_PROPERTY, properties );
					}
					if( ( dir == null ) || dir.isBlank() ) {
						copyProperty( gf, "RoadDir", FAHRTRICHTUNG_PROPERTY, properties, true );
					}
					
					copyProperty( gf, QFileReader.ATTR_KNOTEN_NR, properties );
					copyProperty( gf, QFileReader.ATTR_KM, properties );
					copyProperty( gf, QFileReader.ATTR_UZ, properties );
					copyProperty( gf, QFileReader.ATTR_TARGET, properties );
					copyProperty( gf, QFileReader.ATTR_DIRECTION, properties );

				} else {
					Q  q = systemConfiguration.getQ( id );
					properties.add( new Property( QFileReader.ATTR_KNOTEN_NR, "" + q.getKnNr() ));
				}
			} else {
				GeoFeature gf = id2AqGeoFeature.get( id );
				if( gf != null ) {
					try {
						Geometry wgs84Geom = GeometryTools.transformGeometry( gf.getGeometry(), wlsSrid, CoordinateTransformer.WGS84_ID);
						lat = wgs84Geom.getCoordinate().getX();
						lon = wgs84Geom.getCoordinate().getY();
						
						boolean roadExists = copyProperty( gf, QFileReader.ATTR_ROAD, BABNR_PROPERTY, properties, true );
						copyProperty( gf, QFileReader.ATTR_ROAD, QFileReader.ATTR_ROAD, properties, true );
						
						if( !roadExists ) {
							String road = aq.getLocation().getRoadId();
							setProperty( road, BABNR_PROPERTY, properties );
							setProperty( road, QFileReader.ATTR_ROAD, properties );
						}
						
						setProperty( aq.getType(), AQTYPE_PROPERTY, properties, true );
						copyProperty( gf, QFileReader.ATTR_KM, properties );
						copyProperty( gf, "RoadDir", FAHRTRICHTUNG_PROPERTY, properties, true );
						
						hasBearing = copyProperty( gf, BEARING_PROPERTY, WINKEL_PROPERTY, properties );
						copyProperty( gf, BEARING_PROPERTY, "Heading", properties );

						copyProperty( gf, QFileReader.ATTR_KM, properties );
						copyProperty( gf, QFileReader.ATTR_KNOTEN_NR, properties );
						copyProperty( gf, QFileReader.ATTR_UZ, properties );
						copyProperty( gf, QFileReader.ATTR_TARGET, properties );
						copyProperty( gf, QFileReader.ATTR_DIRECTION, properties );
						
					} catch (Fault ex ) {
						LOGGER.warn("Error converting coordinate " + gf.getGeometry().getCoordinate().toString() + ": "  +ex.toString() );
					}
				} else {
					setProperty( aq.getType(), AQTYPE_PROPERTY, properties, true );
					setProperty( aq.getLocation().getRoadId(), BABNR_PROPERTY, properties, true );
				}
			}
			
			if (!id.isEmpty() && lat > 0. && lon > 0.) {
				try {
					Geometry geometry = createPointGeometry(CoordinateTransformer.WGS84_ID, lat, lon);
					
					if( !hasBearing ) {
						Integer bearing = getBearing( id, geometry);
						if (bearing != null) {
							properties.add(new Property(WINKEL_PROPERTY, Integer.toString(bearing)));
						}
					}
					
					TccPointFeature feature = new TccPointFeature(id, name, geometry, wlsSrid, properties);
					features.add(feature);
				} catch (FactoryException | TransformException e) {
					LOGGER.fatal("cannot create AQ " + id + ": " + e);
				}
			} else {
				LOGGER.warn("No location (coordinates) for AQ " + id );
			}
		}
		
		if( sampleMode ) {
			try {
				FileUtils.writeLines( new File( AQ_ID_TO_CLUSTER_ID_FILE ), aqId2ClusterId );
			} catch (IOException ex ) {
				LOGGER.error( ex.toString()  );
			}
		}
		
		LOGGER.info( locFromFileCount + " AQs found in previous configuration" );
	}

	private void readAqs() {
		readAqsFromConfigService(); 
	}
	
	@SuppressWarnings("unused")
	private void readAq(String filename) {      // NOSONAR
		Properties props = new Properties();
		props.setProperty(CsvDataStore.SEPARATOR_KEY, "\t");

		DataStore store = new CsvDataStore(null,filename,props);
		DataReader reader = store.getReader((Filter)null);		
		String suffix = null;
		Set<String> usedAqIds = new HashSet<>();
		
		List<String> aqCoords = new ArrayList<>();
		aqCoords.add( "ID;TYPE;ROADNR;FR;KM;LAT;LON;BEARING" );
		
		try {
			while (reader.hasNext()) {     // NOSONAR
				Data record = reader.next();
				String type = record.getMember(TYPE_FIELD).getAsString();
				if (!"AQ".equals(type)) {
					continue;
				}
				String id = record.getMember(ID_FIELD).getAsString();
				int eak = record.getMember(EAK_FIELD).getAsInt();
				if (usedAqIds.contains(id+"_"+eak)) {
					continue;
				}
				int de = record.getMember(DE_FIELD).getAsInt();
				if (suffix == null) {
					suffix = "_" + de;
				}
				if (de < 193 || de > 223) {
					continue;
				}
				String name = record.getMember(NAME_FIELD).getAsString();
				List<Property> properties = new ArrayList<>();
				String babnr = record.getMember(BABNR_FIELD).getAsString();
				properties.add(new Property(BABNR_PROPERTY, babnr));
				String babkm = record.getMember(BABKM_FIELD).getAsString();
				properties.add(new Property(BABKM_PROPERTY, babkm));
				String fahrtr = record.getMember(FAHRTRICHTUNG_FIELD).getAsString();
				properties.add(new Property(FAHRTRICHTUNG_PROPERTY, fahrtr));
				properties.add(new Property(AQTYPE_PROPERTY, "AQ"));
				double lat = record.getMember(LATITUDE_FIELD).getAsDouble();
				double lon = record.getMember(LONGITUDE_FIELD).getAsDouble();
				if (!id.isEmpty() && lat > 0. && lon > 0.) {
					try {
						Geometry geometry = createPointGeometry(CoordinateTransformer.WGS84_ID, lat, lon);
						Integer bearing = getBearing(id, geometry);
						if (bearing != null) {
							properties.add(new Property(WINKEL_PROPERTY, Integer.toString(bearing)));
						}
						usedAqIds.add(id+"_"+eak);
						id = "AQ_" + id + suffix;
						TccPointFeature feature = new TccPointFeature(id, name, geometry, wlsSrid, properties);
						features.add(feature);
						
						String aqCoord = id + ";AQ;" + babnr.replaceAll("\\s+","") + ";" + fahrtr + ";" + 
						                 babkm.replace(",", ".") + ";" + 
								         lat + ";" + lon + ";" + ( ( bearing == null ) ? -1 : bearing );
						aqCoords.add( aqCoord );

					} catch (FactoryException | TransformException e) {
						LOGGER.fatal("cannot create AQ " + id + ": " + e);
					}
					suffix = null;
				}
				
				
			}
		} finally {
			
			try {
				FileUtils.writeLines(  new File( filename + "-aq-loc.csv" ), StandardCharsets.ISO_8859_1.name(), aqCoords );
			} catch (IOException e) {  // NOSONAR
			}
			
			if ( reader != null ) {
				reader.close();
			}
		}
	}
	
	private Integer getBearing(String id, Geometry geometry) {
		GeoLocation geo = new GeoLocation(id, id, geometry, geometry.getSRID(), geometry.getEnvelopeInternal());
		ParameterList parameter = new ParameterList();
		parameter.getParameters().add(new ParameterDistance(150.));
		parameter.getParameters().add(createStrategicNetParameter());
		// get matching road element
		Location loc;
		try {
			loc = geoConverter.convertToNormalisedLocation(geo, parameter, PointLocation.class);
		} catch (Fault e) {
			LOGGER.fatal("cannot match TccPointFeature " + id + ": "+ e);
			return null;
		}
		if (!(loc instanceof PointLocation)) {
			LOGGER.error("TccPointFeature " + id + " does not match map");
			return null;
		}
		PointLocation pLoc = (PointLocation) loc;
		RoadElement re = roadNet.getRoadElement(pLoc.getElement().getId());
		if (re == null) {
			LOGGER.fatal("cannot get road element for TccPointFeature " + id);
			return null;
		}
		// the first point for the bearing is at the offset of the road element
		Double offset1 = pLoc.getFromOffset();
		if( offset1 < EPS ) {
			offset1 = Math.min( 5.0, re.getLength() / 2.0 );
		}
		
		if( re.getLength() - offset1 < EPS ) {
			offset1 = Math.max( re.getLength() - 5.0, re.getLength() / 2.0 );
		}
		
		Geometry p1 = GeometryTools.extractPoint(re.getGeometry(), offset1);
		
		double offset2 = offset1 + (pLoc.getElement().isInverseOrder() ? -30. : 30.);
		if (offset2 < 0.0) {
			offset2 = 0.0;
		}
		if (offset2 > re.getLength()) {
			offset2 = re.getLength();
		}
		Geometry p2 = GeometryTools.extractPoint(re.getGeometry(), offset2);
		
		Coordinate[] c1 = p1.getCoordinates();
		Coordinate[] c2 = p2.getCoordinates();
		Double bearing = GeometryTools.bearing(c1[0], c2[0]);
		return (int) Math.round(bearing);
	}

	private String getOrientation(Geometry geometry) {    // NOSONAR
		if ( geometry == null ) {
			return null;
		}

		Coordinate[] coordinates = geometry.getCoordinates();
		if (coordinates.length == 0) {
			return null;
		}
		if (coordinates.length == 1) {
			Integer b = getBearing("ID", geometry);
			if( b == null ) {
				return "N";
			}
			return getOrientation(b);
		}
		Coordinate fromCoor = coordinates[0];
		Coordinate toCoor = coordinates[coordinates.length-1];
		double diffX = toCoor.x - fromCoor.x;
		double diffY = toCoor.y - fromCoor.y;
		boolean horizontal = Math.abs(diffX) > Math.abs(diffY);
		if (horizontal) {
			if (Math.abs(diffX) > 2*Math.abs(diffY)) {
				return diffX > 0.0 ? "O" : "W";
			} else if (diffY > 0.0 ) {
				return diffX > 0.0 ? "NO" : "NW";
			} else {
				return diffX > 0.0 ? "SO" : "SW";
			}
		} else {
			if (Math.abs(diffY) > 2*Math.abs(diffX)) {
				return diffY > 0.0 ? "N" : "S";
			} else if (diffX > 0.0) {
				return diffY > 0.0 ? "NO" : "SO";
			} else {
				return diffY > 0.0 ? "NW" : "SW";
			}
		}
	}
	
	private String getOrientation(Integer b) {
		if (b >= 0 && b <= 30) {
			return "N";
		}
		if (b >= 30 && b <= 60) {
			return "NW";
		}
		if (b >= 60 && b <= 120) {
			return "W";
		}
		if (b >= 120 && b <= 150) {
			return "SW";
		}
		if (b >= 150 && b <= 210) {
			return "S";
		}
		if (b >= 210 && b <= 240) {
			return "SO";
		}
		if (b >= 240 && b <= 300) {
			return "O";
		}
		if (b >= 30 && b <= 330) {
			return "NO";
		}
		if (b >= 330 && b <= 360) {
			return "N";
		}
		return null;
	}

	private Parameter createStrategicNetParameter() {
		SegmentedAttribute attr = new SegmentedAttribute(RoadMapConstants.SEGMENT_ATTR_STRATEGIC_NET, 
				RoadMapConstants.SEGMENT_ATTR_VALUE_TRUE, null, null, null);
		ParameterAttributes param = new ParameterAttributes();
		param.setMatchAll(true);
		param.getAttributes().add(attr);
		return param;
	}
	
	public void setFilenameAq(String filename) {
		this.filenameAq = filename;
	}

	public void setFilenameWww(String filename) {
		this.filenameWww = filename;
	}

	public void setFilenameStrg(String filename) {
		this.filenameStrg = filename;
	}
	
	public void setFileDirStrategyRoutes(String fileDirStrategyRoutes) {
		this.fileDirStrategyRoutes = fileDirStrategyRoutes;
	}
	
	public void setAqLocationFile( String aqLocationFile ) { 
		this.aqLocationFile = aqLocationFile;
	}

	public void setSrid(int srid) {
		this.wlsSrid = srid;
	}

	public void setRoadNetPlugin(RoadNetPlugin roadNetPlugin) {
		this.roadNetPlugin = roadNetPlugin;
		this.roadNet = roadNetPlugin.getRoadNet();
	}

	public void setRoadMapPlugin(RoadMapPlugin roadMapPlugin) {
		this.roadMapPlugin = roadMapPlugin;
	}
	
	public void setSystemConfiguration(SystemConfiguration systemConfiguration) {
		this.systemConfiguration = systemConfiguration;
	}

	public void setAqGeoFeatures( Collection<GeoFeature> gfs ) {
		gfs.forEach( g -> id2AqGeoFeature.put( g.getId(), g ) );
	}
	
	@Override
	public Set<String> getTypeNames() {
		return new HashSet<>(typeNames);
	}

	@Override
	public Collection<Location> getLocations(String type) {
		Class<?> clazz = null;
		try {
			clazz = Class.forName(type);
		} catch (ClassNotFoundException ex ) {
			LOGGER.error("sdbbyFeatureSource.getLocations: " + ex );
			throw new IllegalArgumentException( "sdbbyFeatureSource.getLocations: " + ": unknown class " + type );			
		}
		if (clazz.isAssignableFrom(TccPointFeatureGroup.class)) {
			return new ArrayList<>(featureGroups);
		}
		if (clazz.isAssignableFrom(SdbbyStrategy.class)) {
			return new ArrayList<>(strategyMap.values());
		}
		
		throw new IllegalArgumentException( "sdbbyFeatureSource.getLocations: not supported class " + type );			
	}
	
	public List<TccPointFeature> getTccPointFeatures() {
		return features;
	}

    public List<SdbbyStrategy> getStrategies() {
        return strategyMap.values().stream().collect( Collectors.toList() );
    }
	
    public List<SdbbyRoute> getRoutes() {
        return routeMap.values().stream().collect( Collectors.toList() );
    }
    
    
    /**
     * 
     * Converts AQ IDs in WWW file
     * 
     * @param idConvFile			ID lookup file
     * @param aqId2ClusterIdFile	AQ --> ClusterId
     * @param wwwFile				WWW file		
     * @param convertedWwwFile		Converted WWW file
     * @throws IOException			Error
     */
	public static void convertWWWFile( String idConvFile, 
            						   String aqId2ClusterIdFile,
			                           String wwwFile, 
			                           String convertedWwwFile  ) throws IOException {
		
		LOGGER.info( "Start ID conversion of rule file <" + wwwFile + ">" );

		List<String> idLines = FileUtils.readLines( new File( idConvFile ), StandardCharsets.ISO_8859_1 );
		List<String> c2iLines = FileUtils.readLines( new File( aqId2ClusterIdFile ), StandardCharsets.ISO_8859_1 );

		Map<String,String> id2newId = new HashMap<>();
		for( String cl : idLines ) {
			String[] parts = cl.split( ";" );
			if( ( parts != null ) && ( parts.length >= 2 ) )  {
				id2newId.put( parts[1], parts[0] );
			}
 		}
		
		Map<String,String> cid2id = new HashMap<>();
		for( String cl : c2iLines ) {
			String[] parts = cl.split( ";" );
			if( ( parts != null ) && ( parts.length >= 2 ) )  {
				cid2id.put( parts[1], parts[0] );
			}
 		}
		
		List<String> convertedWwwLines = new ArrayList<>();
		List<String> wwwLines = FileUtils.readLines( new File( wwwFile ), StandardCharsets.ISO_8859_1 );
		
		for( String rl : wwwLines ) {    // NOSONAR
			if( rl.startsWith( WWWPANEL_LINE_START ) ) {
				String part = rl.substring( WWWPANEL_LINE_START.length() );
				int endOfAqId = part.indexOf( '\t' );
				if( endOfAqId == -1 ) {
					convertedWwwLines.add( rl );
					LOGGER.warn( "    Line <" + rl + ">: cannot find AQ ID" );    // NOSONAR
					continue;
				}
				
				String id = part.substring( 0, endOfAqId );
				String cId = id2newId.get( id );
				if( cId == null ) {
					convertedWwwLines.add( rl );
					LOGGER.warn( "    Line <" + rl + ">: no new ID for <" + id + ">" );  // NOSONAR
					continue;
				}
				
				String newId = cid2id.get( cId );
				if( newId == null ) {
					convertedWwwLines.add( rl );
					LOGGER.warn( "    Line <" + rl + ">: no new ID for <" + id + ">" );
					continue;
				}
				
				String crl = WWWPANEL_LINE_START + newId + part.substring( endOfAqId );
				convertedWwwLines.add( crl );
			} else {
				convertedWwwLines.add( rl );
			}
		}
		
		FileUtils.writeLines( new File( convertedWwwFile ) ,
                              StandardCharsets.UTF_8.name(), 
                              convertedWwwLines );
		
		LOGGER.info( "Converted WWW content written to <" + convertedWwwFile + ">" );
	}
    
	
    /**
     * 
     * Converts AQ IDs in AQ location file
     * 
     * @param idConvFile			ID lookup file
     * @param aqId2ClusterId		clusterId --> aqId file
     * @param aqLocFile				AQ file		
     * @param convertedAqLocFile	Converted AQ file
     * @throws IOException			Error
     */
	public static void convertAQLocationFile( String idConvFile, 
			                                  String aqId2ClusterId,
			                                  String aqLocFile, 
			                                  String convertedAqLocFile  ) throws IOException {
		
		LOGGER.info( "Start ID conversion of rule file <" + aqLocFile + ">" );

		List<String> idLines = FileUtils.readLines( new File( idConvFile ), StandardCharsets.ISO_8859_1 );
		List<String> c2iLines = FileUtils.readLines( new File( aqId2ClusterId ), StandardCharsets.ISO_8859_1 );

		Map<String,String> id2newId = new HashMap<>();
		for( String cl : idLines ) {
			String[] parts = cl.split( ";" );
			if( ( parts != null ) && ( parts.length >= 2 ) )  {
				id2newId.put( parts[1], parts[0] );
			}
 		}

		Map<String,String> cid2id = new HashMap<>();
		for( String cl : c2iLines ) {
			String[] parts = cl.split( ";" );
			if( ( parts != null ) && ( parts.length >= 2 ) )  {
				cid2id.put( parts[1], parts[0] );
			}
 		}
		
		List<String> convertedAqLocLines = new ArrayList<>();
		List<String> locLines = FileUtils.readLines( new File( aqLocFile ), StandardCharsets.ISO_8859_1 );
		
		for( String rl : locLines ) {    // NOSONAR
				int endOfAqId = rl.indexOf( ';' );
				if( endOfAqId == -1 ) {
					convertedAqLocLines.add( rl );
					LOGGER.warn( "    Line <" + rl + ">: cannot find AQ ID" );
					continue;
				}
				
				String id = rl.substring( 0, endOfAqId );
				String cId = id2newId.get( id );
				if( cId == null ) {
					convertedAqLocLines.add( rl );
					LOGGER.warn( "    Line <" + rl + ">: no new ID for <" + id + ">" );
					continue;
				}
				
				String newId = cid2id.get( cId );
				if( newId == null ) {
					convertedAqLocLines.add( rl );
					LOGGER.warn( "    Line <" + rl + ">: no new ID for <" + id + ">" );
					continue;
				}
				
				String crl = newId + rl.substring( endOfAqId );
				convertedAqLocLines.add( crl );
		}
		
		FileUtils.writeLines( new File( convertedAqLocFile ) ,
                              StandardCharsets.UTF_8.name(), 
                              convertedAqLocLines );
		
		LOGGER.info( "Converted location content written to <" + convertedAqLocFile + ">" );
	}

	
	/**
	 * 
	 * Execute convertWWWFile()
	 * 
	 * @param args	Program arguments
	 */
	public static void main(String[] args) {
		try {
			convertAQLocationFile( "idConversion/FG4-ID-Lookup.csv", 
					               AQ_ID_TO_CLUSTER_ID_FILE,
					               "map/aq-location.csv", 
					               "idConversion/aq-location-converted.csv" );
		} catch ( IOException ex ) {
			System.out.println( "Conversion failed" );    						// NOSONAR
			System.out.println( CallStack.getStackTraceAsString( ex ) );		// NOSONAR
		}
		
		try {
			convertWWWFile( "idConversion/FG4-ID-Lookup.csv", 
					        AQ_ID_TO_CLUSTER_ID_FILE,
					        "map/www_v1.0.txt", 
					        "idConversion/www_v1.0-converted.txt" );
		} catch ( IOException ex ) {
			System.out.println( "Conversion failed" );    						// NOSONAR
			System.out.println( CallStack.getStackTraceAsString( ex ) );		// NOSONAR
		}		
	}	
}
