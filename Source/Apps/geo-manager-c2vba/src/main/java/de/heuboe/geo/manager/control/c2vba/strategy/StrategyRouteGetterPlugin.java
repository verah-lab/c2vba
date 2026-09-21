package de.heuboe.geo.manager.control.c2vba.strategy;

import java.util.ArrayList;
import java.util.List;

import de.heuboe.wls.data.geofeature.GeoFeature;
import de.heuboe.wls.data.sdbby.SdbbyRoute;
import de.heuboe.wls.data.sdbby.SdbbyStrategy;
import de.heuboe.wls.geofeature.GeoFeatureGetterPlugin;
import de.heuboe.wls.utils.GeometryFeatureCache;


/**
 * 
 * GeoFeature plugin for strategy routes as GeoFeatures
 * 
 * @author peters
 *
 */
public class StrategyRouteGetterPlugin extends GeoFeatureGetterPlugin {
    
    /**
     * 
     * Constructor
     * 
     * @param strategies    Strategies
     */
    public StrategyRouteGetterPlugin( List<SdbbyStrategy> strategies ) {
        setTypeName( SdbbyRoute.class.getName() );
        init( strategies );
    }
    
    
	
  	/**
  	 * 
  	 * Converts SdbbyRoutes to GeoFeature
  	 * 
  	 */
	private void init ( List<SdbbyStrategy> strategies ) {
		
		List<GeoFeature> routeLocs = new ArrayList<>();
		for( SdbbyStrategy strategy : strategies ) {
		    for( SdbbyRoute route : strategy.getRoutes() ) {
		        GeoFeature gf = new GeoFeature( route.getId(), route.getName(),
		                                        route.getGeometry(), route.getSrid(), 
		                                        "SdbbyRoute", new ArrayList<>() );
		        routeLocs.add( gf );
		    }
		}
		
	    setLocationCache( new GeometryFeatureCache<>( "SdbbyRoute", routeLocs ) );
	}

}
