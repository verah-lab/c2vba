package de.heuboe.wls.by;

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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;

import de.heuboe.data.Data;
import de.heuboe.data.DataReader;
import de.heuboe.data.DataStore;
import de.heuboe.data.Filter;
import de.heuboe.data.csv.CsvDataStore;
import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;
import de.heuboe.wls.by.cfg.ConfigService;
import de.heuboe.wls.data.roadmap.ParameterAttributes;
import de.heuboe.wls.data.roadmap.RoadElement;
import de.heuboe.wls.data.roadmap.SegmentedAttribute;
import de.heuboe.wls.data.sdbby.SdbbyRoute;
import de.heuboe.wls.data.sdbby.SdbbyStrategy;
import de.heuboe.wls.data.tcc.TccPointFeature;
import de.heuboe.wls.data.tcc.TccPointFeatureGroup;
import de.heuboe.wls.data.wls.Direction;
import de.heuboe.wls.data.wls.GeoLocation;
import de.heuboe.wls.data.wls.LinearLocation;
import de.heuboe.wls.data.wls.Location;
import de.heuboe.wls.data.wls.Parameter;
import de.heuboe.wls.data.wls.ParameterDistance;
import de.heuboe.wls.data.wls.ParameterList;
import de.heuboe.wls.data.wls.PointLocation;
import de.heuboe.wls.data.wls.Property;
import de.heuboe.wls.iface.Fault;
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
import eu.vmis_ehe.vmis2.configservice.CfgAq.AqType;

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

	private static final Logger LOGGER = Logger.getLogger(SdbbyFeatureSource.class);
	private static final double EPS = 0.5;

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
	private static final String AQTYPE_PROPERTY = "AQTYPE";
	private static final String SEQNUM_PROPERTY = "SEQNUM";

	private static final double MIN_OFFSET = 10.0;
	private static final double DRIFT = 0.1;
	
	private String filenameAq;
	private String filenameWww;
	private String filenameStrg;
	private int wlsSrid;
	private GeometryFactory factory;
	private Map<Integer, CoordinateTransformer> transformers = new HashMap<>();
	
	private RoadNet roadNet;
	private GeoToNormConverter geoConverter;
	private GeoToRouteConverter geoToRouteConverter;
	private NormToGeoConverter normToGeoConverter;
	
	private List<TccPointFeature> features;
	private List<TccPointFeatureGroup> featureGroups;
	private Map<String, SdbbyStrategy> strategyMap;
	private Map<String, SdbbyRoute> routeMap;
	
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
		normToGeoConverter = new NormToGeoConverter(roadNet);
		factory = new GeometryFactory(new PrecisionModel(), wlsSrid);
		
		readAqs();
		readWWWs();
		readRoutes();
		readStrategies();
	}

	/**
	 * 
	 * Reads route configuration
	 * 
	 */
	public void readRoutes() {		 // NOSONAR
		LOGGER.info("reading route file: " + filenameStrg);
		SdbbyRoute currentRoute = null;
		SortedMap<Integer, Coordinate> currentCoors = new TreeMap<>();
		Properties properties = new Properties();
		properties.setProperty(CsvDataStore.SEPARATOR_KEY, "\t");
		properties.setProperty( CsvDataStore.CHARSET_KEY, StandardCharsets.ISO_8859_1.name() );
		DataStore store = new CsvDataStore(null, filenameStrg, properties);
		DataReader reader = store.getReader();
		try {
			int pos = 0;
			int line = 0;
			while (reader.hasNext()) {
				line++;
				Data record = reader.next();
				String id = record.getMember("ID").getAsString();
				String name = record.getMember("NAME").getAsString();
				String type = record.getMember("TYPE").getAsString();
				if ("ROUTE".equals(type)) {
					if (currentRoute != null) {
						createLineGeometry(currentRoute, currentCoors);
					}
					currentCoors.clear();
					currentRoute = new SdbbyRoute();
					currentRoute.setId(id);
					currentRoute.setName(name);
					pos = 0;
					routeMap.put(id, currentRoute);
				}
				if ("ROUTE+".equals(type) || "ROUTE".equals(type)) {
					++pos;
					Coordinate c = createCoordinate(record);
					if (c != null) {
						currentCoors.put(pos, c);
					} else {
						String coords = record.getMember("COORDINATE").getAsString();
					    if( coords == null ) {
							throw new IllegalArgumentException("SdbbyFeatureSource.init: no coordinate in line " + line );			
					    }
					    
					    Coordinate coor = createCoordinate( line, coords );
					    currentCoors.put( pos, coor );
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
				createLineGeometry(currentRoute, currentCoors );
			}
		} catch (FactoryException | TransformException | Fault e) {
			LOGGER.fatal("cannot read: " + e);
		} finally {
			if ( reader != null ) {
				reader.close();
			}
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
			while (reader.hasNext()) {      // NOSONAR
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
					try {
						Geometry triggerGeometry = createPointGeometry(wlsSrid, triggerCoor.x, triggerCoor.y);
						GeoLocation triggerLocation = new GeoLocation(id, name, triggerGeometry, wlsSrid, triggerGeometry.getEnvelopeInternal());
						currentStrategy.setTrigger(triggerLocation);
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

	private void readWWWs() {
		LOGGER.info("reading WWW file: " + filenameWww);
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
					try {
						Geometry geometry = createPointGeometry(srid, x, y);
						Integer bearing = getBearing(id, geometry);
						if (bearing != null) {
							properties.add(new Property(WINKEL_PROPERTY, Integer.toString(bearing)));
						}
						properties.add(new Property(AQTYPE_PROPERTY, "WWW"));
						properties.add(new Property(SEQNUM_PROPERTY, Integer.toString(++n)));						
						TccPointFeature wwwFeature = new TccPointFeature(id, name, geometry, srid, properties);
						currentFeatureGroup.getPointFeatures().add(wwwFeature);
						features.add(wwwFeature);
					} catch (FactoryException | TransformException e) {
						LOGGER.fatal("cannot create WWW " + id + ": " + e);
					}
				}
			}
			setGeometry(currentFeatureGroup);
		} catch( Throwable ex )  {
			LOGGER.error( ex.toString() );
			LOGGER.fatal( CallStack.getStackTraceAsString( ex ) );
		} finally {
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
	
	private void createLineGeometry(SdbbyRoute route, SortedMap<Integer, Coordinate> currentCoors, SortedMap<Integer,String> currentRoadElems) throws Fault {
		if (currentCoors.isEmpty()) {
			createLineGeometryByRoadElems(route, currentRoadElems);
		} else {
			createLineGeometryByCoors(route, currentCoors); 
		}
	}

	private void createLineGeometryByCoors(SdbbyRoute route, SortedMap<Integer, Coordinate> currentCoors) throws Fault {
		Coordinate[] coordinates = currentCoors.values().toArray(new Coordinate[0]);
		LineString line = factory.createLineString(coordinates);
		
		GeoLocation geo = new GeoLocation(route.getId(), route.getName(), line, wlsSrid, line.getEnvelopeInternal());
		LinearLocation linloc = geoToRouteConverter.createLinearLocation(geo, null, null);
		if (linloc == null) {
			throw new Fault("cannot convert route to linear location: " + route.getId());
		}
		Geometry geometry = normToGeoConverter.convertToGeometry(linloc, null);
		route.setGeometry(geometry);
		route.setSrid(wlsSrid);
		route.setLinearLocation(linloc);
	}
	
	private void createLineGeometryByRoadElems(SdbbyRoute route, SortedMap<Integer, String> roadElems) throws Fault {
		SortedMap<Integer, Coordinate> coors = new TreeMap<>();
		int pos = 0;
		for(String reId : roadElems.values()) {
			RoadElement re = roadNet.getRoadElement(reId);
			if (re == null) {
				throw new Fault("invalid road element: " + reId);
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
				throw new Fault("cannot create point for road element: " + reId);
			}
			Coordinate[] coordinates = CoordinateConverter.toJtsCoordinates(point.getCoordinates().toArray(new de.heuboe.geo.Coordinate[0]));
			coors.put(pos, coordinates[0]);
			++pos;
		}
		createLineGeometryByCoors(route, coors);
	}

	private Coordinate createCoordinate(Data record) throws FactoryException, TransformException {
		try {
			int srid = record.getMember("SRID").getAsInt();
			String xStr = record.getMember("X").getAsString().trim();
			double x = Double.parseDouble(xStr.replace(',', '.'));
			String yStr = record.getMember("Y").getAsString().trim();
			double y = Double.parseDouble(yStr.replace(',', '.'));
			if (srid != wlsSrid) {
				CoordinateTransformer transformer = transformers.get(srid);
				if (transformer == null) {
					transformer = new CoordinateTransformer(srid, wlsSrid);
					transformers.put(srid, transformer);
				}
				Coordinate[] c = transformer.transform(new Coordinate(x, y));
				return c[0];
			}
			return new Coordinate(x, y);
		} catch(NumberFormatException e) {
			return null;
		}
	}
	
	private Coordinate createCoordinate( int line, String coorString ) throws FactoryException, TransformException {
		int srid = 4326;
		String[] coors = coorString.split( "," );
		if( ( coors == null ) || ( coors.length != 2 ) ) {
			throw new IllegalArgumentException("SdbbyFeatureSource.init: invalid coordinates in line " + line );			
		}
		double x = Double.parseDouble( coors[0] );
		double y = Double.parseDouble( coors[1] );
		
		CoordinateTransformer transformer = transformers.get(srid);     // NOSONAR
		if (transformer == null) {
			transformer = new CoordinateTransformer(srid, wlsSrid);
			transformers.put(srid, transformer);
		}
		Coordinate[] c = transformer.transform(new Coordinate(x, y));
		return c[0];
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
		feature.getProperties().add(new Property(FAHRTRICHTUNG_PROPERTY, fahrtr));
	}
	
	private void readAqsFromConfigService() {
		List<CfgAq> cfgAqs = configService.getAllAQs( null );
		for( CfgAq aq : cfgAqs ) {
			
			String id = aq.getId();
			
			String name = aq.getName();
			List<Property> properties = new ArrayList<>();
			
			String road = aq.getLocation().getRoadId();
			properties.add(new Property(BABNR_PROPERTY, road));
			
			AqType type = aq.getType();
			properties.add(new Property(AQTYPE_PROPERTY, type.name()));
			
			// Kilometrierung/Fahrtrichtung ergänzen
			
			double lat = aq.getLocation().getLatitude();
			double lon = aq.getLocation().getLongitude();
			
			if (!id.isEmpty() && lat > 0. && lon > 0.) {
				try {
					Geometry geometry = createPointGeometry(CoordinateTransformer.WGS84_ID, lat, lon);
					Integer bearing = getBearing( id, geometry);
					if (bearing != null) {
						properties.add(new Property(WINKEL_PROPERTY, Integer.toString(bearing)));
					}
					TccPointFeature feature = new TccPointFeature(id, name, geometry, wlsSrid, properties);
					features.add(feature);
				} catch (FactoryException | TransformException e) {
					LOGGER.fatal("cannot create AQ " + id + ": " + e);
				}
			}
		}
	}

	private void readAqs() {
		String[] filenames = filenameAq.split(";");
		for(String filename : filenames) {
			LOGGER.info("reading AQ file: " + filename);
			readAq(filename);
		}
		
		
		readAqsFromConfigService(); 
	}
	
	private void readAq(String filename) {
		Properties props = new Properties();
		props.setProperty(CsvDataStore.SEPARATOR_KEY, "\t");

		DataStore store = new CsvDataStore(null,filename,props);
		DataReader reader = store.getReader((Filter)null);		
		String suffix = null;
		Set<String> usedAqIds = new HashSet<>();
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
					} catch (FactoryException | TransformException e) {
						LOGGER.fatal("cannot create AQ " + id + ": " + e);
					}
					suffix = null;
				}
			}
		} finally {
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

	private String getOrientation(Geometry geometry) {     // NOSONAR
		Coordinate[] coordinates = geometry.getCoordinates();
		if (coordinates.length == 0) {
			return null;
		}
		if (coordinates.length == 1) {
			Integer b = getBearing("ID", geometry);
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

	public void setSrid(int srid) {
		this.wlsSrid = srid;
	}

	public void setRoadNet(RoadNet roadNet) {
		this.roadNet = roadNet;
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
		} catch (ClassNotFoundException e) {
			LOGGER.error("sdbbyFeatureSource.getLocations: " + e);
			return null;
		}
		//if (clazz.isAssignableFrom(TccPointFeature.class)) {
		//	return new ArrayList<Location>(features);
		//}
		if (clazz.isAssignableFrom(TccPointFeatureGroup.class)) {
			return new ArrayList<>(featureGroups);
		}
		if (clazz.isAssignableFrom(SdbbyStrategy.class)) {
			return new ArrayList<>(strategyMap.values());
		}
		return null;    // NOSONAR
	}
	
	public List<TccPointFeature> getTccPointFeatures() {
		return features;
	}

    public List<SdbbyStrategy> getStrategies() {
        return strategyMap.values().stream().collect( Collectors.toList() );
    }
	
}
