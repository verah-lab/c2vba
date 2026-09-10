package de.heuboe.wls.by;

import java.util.List;

import org.locationtech.jts.geom.Geometry;

import de.heuboe.wls.data.tcc.TccPointFeature;
import de.heuboe.wls.utils.GeometryLocationCache;
import de.heuboe.wls.utils.GeometryLocationGetterPlugin;

/**
 * 
 * TccPointFeaturePlugin
 * 
 * @author peters
 *
 */
public class TccPointFeaturePlugin extends GeometryLocationGetterPlugin<TccPointFeature> {
	
	private SdbbyFeatureSource source;
	
	/**
	 * 
	 * Initializatiom
	 * 
	 */
	public void init() {
		
		setTypeName( TccPointFeature.class.getName() );
		List<TccPointFeature> tccPointFeatures = source.getTccPointFeatures();
		
		GeometryLocationCache<TccPointFeature> locationCache = 
				
				new GeometryLocationCache<TccPointFeature>(  TccPointFeature.class.getName(), tccPointFeatures ) {

					@Override
					public Geometry toGeometry(TccPointFeature location) {
						return location.getGeometry();
					}	
			
				};
		
		setLocationCache( locationCache );
	}

	public SdbbyFeatureSource getSource() {
		return source;
	}

	public void setSource(SdbbyFeatureSource source) {
		this.source = source;
	}
}
