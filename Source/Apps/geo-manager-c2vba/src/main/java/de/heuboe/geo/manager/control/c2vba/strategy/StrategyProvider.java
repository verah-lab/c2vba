package de.heuboe.geo.manager.control.c2vba.strategy;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import de.heuboe.data.Data;
import de.heuboe.data.DataReader;
import de.heuboe.data.DataStore;
import de.heuboe.data.csv.CsvDataStore;
import de.heuboe.geo.manager.base.config.ConfigBase;
import de.heuboe.geo.manager.map.importer.MapSupplyProviderImporter;
import de.heuboe.geo.manager.map.importer.MapSupplyProviderTable;
import de.heuboe.log.Logger;
import de.heuboe.wls.data.sdbby.SdbbyRoute;
import de.heuboe.wls.data.sdbby.SdbbyStrategy;
import de.heuboe.wls.data.wls.GeoLocation;
import de.heuboe.wls.map.supply.MapDataVersion.Version;
import de.heuboe.wls.map.supply.MapSupply;
import de.heuboe.wls.srv.impl.WlsEnvironment;
import de.heuboe.wls.util.WlsException;
import de.heuboe.wls.utils.CoordinateTransformer;

/**
 * 
 * Reads strategies
 * 
 * @author peters
 *
 */
public class StrategyProvider extends MapSupplyProviderTable<SdbbyStrategy> {
    
    /**
     * 
     * Constructor
     * 
     * @throws WlsException     Error
     */
    
    /**
     * 
     * Constructor
     * 
     * @param wlsEnvironment    WLS environment
     * @param mapSupply         Map supply
     * @param type              Component type
     * @param mapDataVersion    Map version
     * @param fileName          Strategy routes file
     * @throws WlsException     Error
     */
    public StrategyProvider( WlsEnvironment wlsEnvironment, 
                             MapSupply mapSupply,      
                             String type, 
                             Version mapDataVersion, 
                             String fileName
                            ) throws WlsException {
        
        super( wlsEnvironment, type, mapDataVersion);
        this.mapSupply = mapSupply;
        
        try {
            this.wlsSrid = ConfigBase.getWlsSrid();
            this.factory = new GeometryFactory(new PrecisionModel(), wlsSrid);
            this.transformer = new CoordinateTransformer( CoordinateTransformer.WGS84_ID, wlsSrid );
            this.fileName = fileName;
        } catch (FactoryException ex ) {
            throw new WlsException( "Cannot create CoordinateTransformer " + CoordinateTransformer.WGS84_ID + " --> " + wlsSrid, ex) ;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(StrategyProvider.class);
    private static final String STRATEGY = "Strategy ";
    private static final String STRATEGIE = "STRATEGIE";
   
    private String fileName;
    
    private CoordinateTransformer transformer;
    private GeometryFactory factory;
    
    private int wlsSrid; 
    private Map<String, SdbbyStrategy> strategyMap = new HashMap<>();
    private Map<String, SdbbyRoute> routeMap = new HashMap<>();
    
    private boolean hasCoordinate( Data record ) {
        int srid = record.getMember("SRID").getAsInt();
        String xStr = record.getMember("X").getAsString().trim();
        String yStr = record.getMember("Y").getAsString().trim();
        
        return ( srid != 0 ) && !xStr.isEmpty() && !yStr.isEmpty();
    }

    private Coordinate createCoordinate( String id, int line, Data record) {
        
        try { 
            String xStr = record.getMember("X").getAsString().trim();
            double x = Double.parseDouble(xStr.replace(',', '.'));
            String yStr = record.getMember("Y").getAsString().trim();
            double y = Double.parseDouble(yStr.replace(',', '.'));
            Coordinate[] c = transformer.transform(new Coordinate(x, y));
            return c[0];
        } catch( TransformException | NumberFormatException ex ) {
            LOGGER.error( STRATEGY + id + ", line " + line + " error converting coordinate: " + ex.toString()  );
        }
        
        return null;
    }

    
    /**
     * 
     * Reads route configuration
     * @param fileName File with strategy routes definitions
     * 
     * @exception WlsException Error
     */
    public void readRoutes( String fileName ) throws WlsException {        // NOSONAR
        LOGGER.info("reading route file: " + fileName);
        SdbbyRoute currentRoute = null;
        SortedMap<Integer, Coordinate> currentCoors = new TreeMap<>();
        SortedMap<Integer, String> currentRoadElems = new TreeMap<>();
        Properties properties = new Properties();
        properties.setProperty(CsvDataStore.SEPARATOR_KEY, "\t");
        properties.setProperty( CsvDataStore.CHARSET_KEY, StandardCharsets.ISO_8859_1.name() );
        DataStore store = new CsvDataStore(null, fileName, properties);
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
                        if( !createLineGeometry(currentRoute, currentCoors ) ) {  // NOSONAR
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
                if( !createLineGeometry(currentRoute, currentCoors ) ) {  // NOSONAR
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

    private SdbbyRoute copySdbbyRoute(SdbbyRoute r) {       
        return new SdbbyRoute(r.getId(), r.getName(), r.getGeometry(), r.getSrid(), r.getLinearLocation(), r.isMainRoute(), r.isRecommended());
    }

    /**
     * 
     * Reads strategy routes definitions
     * 
     * @param fileName File with strategy routes definitions
     * 
     * @exception WlsException Error
     */
    public void readStrategies( String fileName ) {         
        LOGGER.info("reading strategy file: " + fileName);
        SdbbyStrategy currentStrategy = null;
        Properties properties = new Properties();
        properties.setProperty(CsvDataStore.SEPARATOR_KEY, "\t");
        properties.setProperty( CsvDataStore.CHARSET_KEY, StandardCharsets.ISO_8859_1.name() );
        DataStore store = new CsvDataStore(null, fileName, properties);
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
                        Geometry triggerGeometry = createPointGeometry( triggerCoor.x, triggerCoor.y);
                        GeoLocation triggerLocation = new GeoLocation(id, name, triggerGeometry, wlsSrid, triggerGeometry.getEnvelopeInternal());
                        currentStrategy.setTrigger(triggerLocation);
                        
                        // Trigger destination
                        triggerGeometry = createPointGeometry( triggerCoorDest.x, triggerCoorDest.y);
                        triggerLocation = new GeoLocation(id, name, triggerGeometry, wlsSrid, triggerGeometry.getEnvelopeInternal());
                        currentStrategy.setTriggerDestination( triggerLocation );

                    } catch ( TransformException ex ) {
                        LOGGER.fatal("Cannot create trigger location for strategy " + id);
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

    private Geometry createPointGeometry( double x, double y) throws TransformException {
        return factory.createPoint(new Coordinate(x, y));
    }
    
    private boolean createLineGeometry( SdbbyRoute route, 
                                        SortedMap<Integer, Coordinate> currentCoors ) throws WlsException {
        if (currentCoors.isEmpty()) {
            throw new WlsException("No route coordinates provided");
        } else {
            return createLineGeometryByCoors(route, currentCoors); 
        }
    }

    private boolean createLineGeometryByCoors( SdbbyRoute route, SortedMap<Integer, Coordinate> coorMap ) {
        
        if( coorMap.size() < 2 ) {
            LOGGER.error( STRATEGY + route.getId() + ": too few coordinates" );
            return false;
        }
        List<Coordinate> coors = new ArrayList<>( coorMap.values() );
        Coordinate[] cos = coors.toArray( new Coordinate[0] );
        Geometry geometry = factory.createLineString(cos );
        route.setGeometry(geometry);
        route.setSrid( wlsSrid );
        return true;
    }

    @Override
    public MapSupplyProviderImporter<SdbbyStrategy> createImporter( MapSupply mapSupply, 
                                                                    Version version,
                                                                    Map<String,String> props, 
                                                                    String supplySource ) throws WlsException {
        return new StrategyProvider( wlsEnvironment, mapSupply, type, version, supplySource );
    }

    @Override
    public String getMapSupplyErrorMsg() {
        return "Fehler beim Lesen der Strategien.";
    }

    @Override
    public boolean importMapData() {
        try {
            readRoutes( fileName );
            readStrategies( fileName );
            
            records = strategyMap.values().stream().collect( Collectors.toList() );
            
            return true;
        } catch (WlsException ex ) {
            LOGGER.error( "Error reading strategies: " + ex.toString() );
        }

        return false;
    }

    @Override
    public String getRecordTypeName() {
        return "Strategien";
    }
}    
