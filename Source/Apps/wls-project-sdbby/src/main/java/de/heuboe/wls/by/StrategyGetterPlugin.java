package de.heuboe.wls.by;

import java.util.ArrayList;
import java.util.List;

import de.heuboe.wls.data.geofeature.GeoFeature;
import de.heuboe.wls.data.sdbby.SdbbyRoute;
import de.heuboe.wls.data.sdbby.SdbbyStrategy;
import de.heuboe.wls.geofeature.GeoFeatureGetterPlugin;
import de.heuboe.wls.utils.GeometryFeatureCache;


/**
 * 
 * GeoFeature plugin for SdbbyRoutes
 * 
 * @author peters
 *
 */
public class StrategyGetterPlugin extends GeoFeatureGetterPlugin {
	
  	private SdbbyFeatureSource source;
	
  	/**
  	 * 
  	 * Converts SdbbyRoutes to GeoFeature
  	 * 
  	 */
	public void init() {
		
		List<SdbbyStrategy> strategies = source.getStrategies();
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
	
	public SdbbyFeatureSource getSource() {
		return source;
	}

	public void setSource(SdbbyFeatureSource source) {
		this.source = source;
	}

}
