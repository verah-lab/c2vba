package de.heuboe.geo.manager.control.c2vba.strategy;

import java.util.ArrayList;
import java.util.List;

import de.heuboe.wls.data.geofeature.GeoFeature;
import de.heuboe.wls.data.sdbby.SdbbyStrategy;
import de.heuboe.wls.data.wls.GeoLocation;
import de.heuboe.wls.geofeature.GeoFeatureGetterPlugin;
import de.heuboe.wls.utils.GeometryFeatureCache;


/**
 * 
 * GeoFeature plugin for strategy routes as GeoFeatures
 * 
 * @author peters
 *
 */
public class StrategyTriggerGetterPlugin extends GeoFeatureGetterPlugin {
    
    /**
     * 
     * Constructor
     * 
     * @param strategies    Strategies
     */
    public StrategyTriggerGetterPlugin( List<SdbbyStrategy> strategies ) {
        setTypeName( StrategyTrigger.class.getName() );
        init( strategies );
    }
    
    
	
  	/**
  	 * 
  	 * Converts SdbbyRoutes to GeoFeature
  	 * 
  	 */
	private void init ( List<SdbbyStrategy> strategies ) {
		
        List<GeoFeature> routeTrigger = new ArrayList<>();
        for( SdbbyStrategy strategy : strategies ) {
            GeoFeature gf = new GeoFeature( strategy.getId(), 
                                            strategy.getName(),
                                            ((GeoLocation)strategy.getTrigger()).getGeometry(), 
                                            ((GeoLocation)strategy.getTrigger()).getSrid(), 
                                            "StrategyTrigger", 
                                            new ArrayList<>() );
            routeTrigger.add( gf );
        }
		
	    setLocationCache( new GeometryFeatureCache<>( "StrategyTrigger", routeTrigger ) );
	}

}
