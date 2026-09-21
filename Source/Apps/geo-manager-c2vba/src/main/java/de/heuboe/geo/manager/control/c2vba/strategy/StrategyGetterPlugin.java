package de.heuboe.geo.manager.control.c2vba.strategy;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import de.heuboe.wls.data.sdbby.SdbbyStrategy;
import de.heuboe.wls.data.wls.GeoLocation;
import de.heuboe.wls.utils.GeometryLocationCache;
import de.heuboe.wls.utils.GeometryLocationGetterPlugin;


/**
 * 
 * GeoFeature plugin for strategy routes as GeoFeatures
 * 
 * @author peters
 *
 */
public class StrategyGetterPlugin extends GeometryLocationGetterPlugin<SdbbyStrategy> {
    
    /**
     * 
     * Constructor
     * 
     * @param strategies    Strategies
     */
    public StrategyGetterPlugin( List<SdbbyStrategy> strategies ) {
        setMultiType( true ); 
        setTypeName( SdbbyStrategy.class.getName() );
        
        GeometryFactory gf = new GeometryFactory(new PrecisionModel());
        
        GeometryLocationCache<SdbbyStrategy> locationCache = 
                new GeometryLocationCache<SdbbyStrategy>( SdbbyStrategy.class.getName(), strategies ) {
                        @Override
                        public Geometry toGeometry(SdbbyStrategy strategy ) {
                            List<Geometry> geoms = new ArrayList<>();
                            strategy.getRoutes().forEach( r -> geoms.add(r.getGeometry() ) );
                            if( strategy.getTrigger() instanceof GeoLocation ) {
                                geoms.add( ((GeoLocation)strategy.getTrigger()).getGeometry() );
                            }
                            if( strategy.getTriggerDestination() instanceof GeoLocation ) {
                                geoms.add( ((GeoLocation)strategy.getTriggerDestination()).getGeometry() );
                            }
                            return gf.createGeometryCollection( geoms.toArray( new Geometry[0] )  );
                        }
       };

        setLocationCache( locationCache );
    }
    
}
