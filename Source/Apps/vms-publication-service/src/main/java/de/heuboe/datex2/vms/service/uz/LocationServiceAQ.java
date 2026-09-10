package de.heuboe.datex2.vms.service.uz;

import java.util.Optional;
import java.util.Set;

import de.heuboe.wls.data.geofeature.GeoFeature;
import de.heuboe.wls.data.roadmap.RoadGeoFeature;
import de.heuboe.wls.data.wls.GetLocationsRequest;
import de.heuboe.wls.data.wls.ParameterId;
import de.heuboe.wls.data.wls.ParameterList;
import de.heuboe.wls.data.wls.ParameterType;
import de.heuboe.wls.data.wls.Property;
import eu.datex2.schema._2._2_0.PointByCoordinates;

/**
 * 
 * Anzeigequerschnitte sind GeoFeature vom Typ "AQ"
 * (Sie verfügen u.U. über die Property CARRIAGEWAY, das Fahrtrichtungsattribut heißt "BEARING")
 * 
 * @author peters
 *
 */
public class LocationServiceAQ extends LocationService
{
	/**
	 * 
	 * Constructor
	 * 
	 * @param wlsSrid				SRID of WLS object geometries
	 * @param useCarriagewayAttr	true: create DATEX-II location carriageway attribute
	 */
	public LocationServiceAQ( int wlsSrid, boolean useCarriagewayAttr ) {
		super( wlsSrid, useCarriagewayAttr );
	}
	
	
	@Override
	protected GetLocationsRequest createGetLocationsRequest( String objType, Set<String> objIds )  {
		
		GetLocationsRequest request = new GetLocationsRequest();
		request.setLocationType( GeoFeature.class.getName() );
		
		ParameterList pList = new ParameterList();
		ParameterType p = new ParameterType("AQ");
		pList.getParameters().add( p );

		if( objIds.size() == 1 ) 
		{
			String objId = objIds.iterator().next();
			pList.getParameters().add( new ParameterId( toLocId( objId ) ) );
		}
		request.setParameter( pList );
		
		return request;
	}
	
	@Override
	protected void registerCarriageway( de.heuboe.wls.data.wls.Location location ) {
		
		if( location instanceof GeoFeature )
		{
			GeoFeature tpf = (GeoFeature)location;
			Optional<Property> prop = tpf.getProperties()
								    	.stream()
								    	.filter( p -> p.getName().equals("CARRIAGEWAY" ) )
								    	.findFirst();
			if( prop.isPresent() ) {
				objId2Carriageway.put( location.getId(), toCarriageway( prop.get().getValue() ) );
			}
		}
	}
	
	@Override
	protected void addBearing( de.heuboe.wls.data.wls.Location location, PointByCoordinates pbc ) {
		
		if (location instanceof RoadGeoFeature) {
			RoadGeoFeature rgf = (RoadGeoFeature) location;
			double heading = rgf.getHeading();
			if (heading >= 0.0) {
				setBearing(pbc, (long) heading);
				return;
			}
		} 
		
		if (location instanceof GeoFeature) {
			GeoFeature tpf = (GeoFeature) location;
			tpf.getProperties().stream().filter(p -> p.getName().equals("BEARING")).findFirst()
					.ifPresent(prop -> setBearing(pbc, prop.getValue()));
		}
	}
	

}
