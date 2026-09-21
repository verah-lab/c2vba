package de.heuboe.geo.manager.control.c2vba.strategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.spatial4j.io.jackson.ShapesAsGeoJSONModule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import de.heuboe.geo.manager.base.config.ConfigBase;
import de.heuboe.geo.manager.base.road.RoadManager;
import de.heuboe.log.Logger;
import de.heuboe.wls.data.roadmap.RouteDefinition;
import de.heuboe.wls.data.sdbby.SdbbyRoute;
import de.heuboe.wls.data.sdbby.SdbbyStrategy;
import de.heuboe.wls.data.wls.ErrorLocation;
import de.heuboe.wls.data.wls.GeoLocation;
import de.heuboe.wls.data.wls.LinearLocation;
import de.heuboe.wls.data.wls.Location;
import de.heuboe.wls.data.wls.PointLocation;
import de.heuboe.wls.iface.Fault;
import de.heuboe.wls.plugin.roadmap.RoadMapPlugin;
import de.heuboe.wls.plugin.roadmap.RoadNetPlugin;
import de.heuboe.wls.roadmap.NormToGeoConverter;
import de.heuboe.wls.utils.GeometryTools;

/**
 * 
 * Adds linear locations to strategy routes
 * 
 * @author peters
 *
 */
public class StrategyLocator  {
    
    private static final Logger LOGGER = Logger.getLogger(StrategyLocator.class);
    private static final String STRATEGY = "Strategy route ";
    private static final String INVALID_COORDINATES = ": invalid coordinates ";
    
    private ObjectMapper objectMapper;
    
    private int wlsSrid;
    private RoadMapPlugin roadMapPlugin;
    private RoadNetPlugin roadNetPlugin;
    @SuppressWarnings("unused")
    private RoadManager roadManager;
    private NormToGeoConverter normToGeoConverter;

    private String fileDirStrategyRoutes;
    private  List<SdbbyStrategy> strategies;  
    private Map<String,LinearLocation> routeLinLocMap = new HashMap<>();

    /**
     * 
     * Constructor
     * 
     * @param roadMapPlugin             RoadMapPlugin
     * @param roadNetPlugin             RoadNetPlugin
     * @param roadManager               RoadManager
     * @param strategies                SdbbyStrategys
     * @param fileDirStrategyRoutes     Strategy route linear location files directory
     */
    public StrategyLocator( RoadMapPlugin roadMapPlugin, 
                            RoadNetPlugin roadNetPlugin, 
                            RoadManager roadManager,
                            List<SdbbyStrategy> strategies,
                            String fileDirStrategyRoutes ) {
        
        
        objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(BasicPolymorphicTypeValidator.builder().build());
        objectMapper.registerModule(new ShapesAsGeoJSONModule() );


        wlsSrid = ConfigBase.getWlsSrid();
        this.roadMapPlugin = roadMapPlugin;
        this.roadNetPlugin = roadNetPlugin;
        this.roadManager = roadManager;
        
        this.normToGeoConverter = new NormToGeoConverter(roadNetPlugin.getRoadNet());
        
        this.fileDirStrategyRoutes = fileDirStrategyRoutes;

        this.strategies = new ArrayList<>();
        for( SdbbyStrategy strategy : strategies ) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream( baos );
                objectOutputStream.writeObject(strategy);
                
                ByteArrayInputStream bais = new ByteArrayInputStream( baos.toByteArray() );
                ObjectInputStream objectInputStream = new ObjectInputStream(bais);
                SdbbyStrategy copy =  (SdbbyStrategy) objectInputStream.readObject();
                
                this.strategies.add( copy );
            } catch (IOException | ClassNotFoundException ex ) {
                throw new RuntimeException( "Fatal error copying strategy <" + strategy.getId() + ">", ex );   // NOSONAR
            }
        }
        
        readRouteLinearLocations();
    }
    
    /**
     * 
     * Add linear locations
     * 
     */
    public void addLinearLocations() {  // NOSONAR
        LOGGER.info( "Add linear locations ..." );
        
        for( SdbbyStrategy strategy : strategies ) {
            for( SdbbyRoute route : strategy.getRoutes() ) {
                if( !createLineGeometryByCoors( route ) ) {
                    LOGGER.error( STRATEGY + route.getId() + ": cannot map coordinates onto road net." );
                }
            }
            
//            // TriggerLocation --> PointRoadReference
//            ComplexRoadReference crr = null;
//            String exMsg = null;
//            try {
//                Location pl = roadMapPlugin.convertLocation( PointLocation.class.getName(), strategy.getTrigger(), null );
//                if( pl instanceof PointLocation ) {
//                    Location rr = roadManager.convertPointLocation2RoadReference( (PointLocation)pl );
//                    if( rr instanceof ComplexRoadReference ) {
//                        crr = (ComplexRoadReference)rr;
//                        strategy.setTrigger( rr );
//                    }
//                }
//            } catch (Fault ex) {
//                exMsg = ex.toString(); 
//            }
//            
//            if( crr == null ) {
//                String error = "Strategy <" + strategy.getId() + ": cannot convert trigger coordinate to road location";
//                if( exMsg != null ) {
//                    error += exMsg;
//                }
//                LOGGER.error( error );
//            }
//            
//            // TriggerDestinationLocation --> PointRoadReference
//            crr = null;
//            exMsg = null;
//            try {
//                Location pl = roadMapPlugin.convertLocation( PointLocation.class.getName(), strategy.getTriggerDestination(), null );
//                if( pl instanceof PointLocation ) {
//                    Location rr = roadManager.convertPointLocation2RoadReference( (PointLocation)pl );
//                    if( rr instanceof ComplexRoadReference ) {
//                        crr = (ComplexRoadReference)rr;
//                        strategy.setTriggerDestination( rr );
//                    }
//                }
//            } catch (Fault ex) {
//                exMsg = ex.toString(); 
//            }
//            
//            if( crr == null ) {
//                String error = "Strategy <" + strategy.getId() + ": cannot convert trigger destination coordinate to road location";
//                if( exMsg != null ) {
//                    error += exMsg;
//                }
//                LOGGER.error( error );
//            }

        }
    }
    
    public List<SdbbyStrategy> getStrategies() {
        return strategies;
    }
    
    
    private boolean createLineGeometryByCoors( SdbbyRoute route ) {
        
        List<Coordinate> coors = Arrays.asList( route.getGeometry().getCoordinates() );
        
        try {
        
            if( coors.size() < 2 ) {
                LOGGER.error( STRATEGY + route.getId() + ": too few coordinates" );
                return false;
            }
            
            Location routeLoc;
            
            LinearLocation ll = routeLinLocMap.get( route.getId() );
            if( ll != null ) {
                routeLoc = ll;
            } else {
                
                if( route.getId().startsWith( "DE") ) {
                    LOGGER.info( "No route for " + route.getId() );
                }
                
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
                route.setLinearLocation( linLoc );
                route.setGeometry( geometry );
            } else {
                LOGGER.error( STRATEGY + route.getId() + ": no route for coordinates of strategy" );
                return false;
            }
        
        } catch( Fault ex ) {
            LOGGER.error( STRATEGY + route.getId() + ": error converting to WlS route: " + ex.toString()  );
            return false;
        }
        
        
        return true;
    }
    
    private void readRouteLinearLocations() {
        ObjectMapper om = new ObjectMapper();

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
                    LinearLocation linLoc = om.readValue( xml, LinearLocation.class );
                    routeLinLocMap.put( id, linLoc );
                } catch (IOException e) {
                    throw new IllegalArgumentException( "Cannot read route location file <" + f.getName() + ">" );
                }
            }
        }
    }


}
