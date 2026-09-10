package de.heuboe.by.c2vba.datex2.mst.localisation.server;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.DefaultCoordinateOperationFactory;
import org.locationtech.jts.geom.Coordinate;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;

import de.heuboe.datex2.location.service.LocationFilter;
import de.heuboe.datex2.wls.OpenLRLocalizer;
import de.heuboe.log.Logger;
import de.heuboe.wls.data.geofeature.GeoFeature;
import de.heuboe.wls.data.wls.GetLocationsRequest;
import de.heuboe.wls.data.wls.GetLocationsResponse;
import de.heuboe.wls.data.wls.Location;
import de.heuboe.wls.data.wls.ParameterType;
import de.heuboe.wls.geofeature.GeoFeatureUtil;
import de.heuboe.wls.iface.WebLocationServer;
import de.heuboe.wls.util.WlsException;
import eu.datex2.schema._2._2_0.AlertCDirection;
import eu.datex2.schema._2._2_0.AlertCDirectionEnum;
import eu.datex2.schema._2._2_0.AlertCLocation;
import eu.datex2.schema._2._2_0.AlertCMethod2Point;
import eu.datex2.schema._2._2_0.AlertCMethod2PrimaryPointLocation;
import eu.datex2.schema._2._2_0.AlertCMethod4Point;
import eu.datex2.schema._2._2_0.AlertCMethod4PrimaryPointLocation;
import eu.datex2.schema._2._2_0.GroupOfLocations;
import eu.datex2.schema._2._2_0.ItineraryByIndexedLocations;
import eu.datex2.schema._2._2_0.OffsetDistance;
import eu.datex2.schema._2._2_0.Point;
import eu.datex2.schema._2._2_0.PointByCoordinates;
import eu.datex2.schema._2._2_0.PointCoordinates;
import eu.datex2.schema._2._2_0._LocationContainedInItinerary;

/**
 * 
 * Mfs access encapsulation 
 * 
 * @author peters
 *
 */
public class WlsClient 
{
	private static final Logger LOGGER = Logger.getLogger( WlsClient.class );
	
	private static final String GEODYN_ID = "GeoDynId";
    private static final String HEADING  = "Heading";
	
	private static final String LCL_COUNTRY_CODE = "D";	
	private static final String LCL_TABLE_NUMBER = "01";	
	private String lclVersion = "18.0";

    private static final String ALC_PRIM_LOC = "AlertCPrimaryLocation";
    private static final String ALC_PRIM_LOC_NAME = "AlertCPrimaryLocationName";
    private static final String ALC_DIR = "AlertCDirection";
    private static final String ALC_DIST = "AlertCDistance";
    private static final String D2_CARRIAGEWAY = "Datex2Carriageway";
	
	@Autowired
	private WebLocationServer webLocationServer;
	
	@Autowired
    private OpenLRLocalizer openLRLocalizer = null;
	
	private Map<String, eu.datex2.schema._2._2_0.Location> locId2OpenLRLocations = new HashMap<>();
	
	private boolean addOpenLRLocation;
	
	private static final int SRID_DEFAULT = 31463;
	private static MathTransform transform = null;
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param addOpenLRLocation  true: add OpenLR locations
	 */
    public WlsClient( boolean addOpenLRLocation) {
		this.addOpenLRLocation = addOpenLRLocation;
	}

	private int getSRID(Integer srid) {
        if (srid == null) {
            return SRID_DEFAULT;
        }

        return srid;
    }
	
	private void createTransform( int refSysKey )
	{
		try {
			int key = refSysKey;
			if( key == 0 ) 
			{
				key = SRID_DEFAULT;
			}
			
			// reference system is WGS84
			CoordinateReferenceSystem wgs84CRS = DefaultGeographicCRS.WGS84;
			
			CoordinateReferenceSystem gkCRS = CRS.decode("EPSG:" + key );
			
			// Retrieve transform operation
			DefaultCoordinateOperationFactory trFactory = new DefaultCoordinateOperationFactory();
			CoordinateOperation operation = trFactory.createOperation(gkCRS,
					wgs84CRS);
			
			transform = operation.getMathTransform();   // NOSONAR:
		} catch( FactoryException ex ) {
			LOGGER.error( "Unable to init GeoTools !" );
			LOGGER.error( ex.toString(), ex );
			Util.exit( -1 );
		}
	}
	
	
	private MathTransform getTransform( int refSysKey )
	{
		if( transform == null ) {
			createTransform( refSysKey );
		}
		
		return transform;
	}
	
	private static class WGS84Point
	{
		private double x;
		private double y;
		
		public WGS84Point( double x, double y )    // NOSONAR:
		{                                          // NOSONAR:   
			this.x = x;
			this.y = y;
		}
		
		public double x()							// NOSONAR:
		{                                           // NOSONAR:    
			return x;
		}
		
		public double y()							// NOSONAR:
		{                                           // NOSONAR:  
			return y;
		}
	}
	
	private WGS84Point toWGS84Coordinates( int refSysKey, double x, double y ) {
		try {
			// Convert coordinate
			double[] sourceCoor = new double[3];
			sourceCoor[0] = x;
			sourceCoor[1] = y;
			sourceCoor[2] = 0; // z-value is 0
			
			double[] targetCoor = new double[3];
			getTransform( refSysKey ).transform(sourceCoor, 0, targetCoor, 0, 1);
			
			return new WGS84Point( targetCoor[0], targetCoor[1] );
		} catch (TransformException ex) {
			LOGGER.error( ex.toString(), ex );
			return null;
		}
	}
	
	
	private void addItineraryPoint( ItineraryByIndexedLocations it, PointByCoordinates coorPoint ) {
	    
		_LocationContainedInItinerary lcii = new _LocationContainedInItinerary();
		lcii.setIndex( it.getLocationContainedInItinerary().size() );
		Point point = new Point();
		point.setPointByCoordinates( coorPoint );
		lcii.setLocation( point );
		it.getLocationContainedInItinerary().add( lcii );
	}

	private boolean addCoordinateLocation( ItineraryByIndexedLocations it, 
										   int refSysKey,
									       double x, double y, double bearing ) {
	    
		PointByCoordinates pbc = new PointByCoordinates();
		PointCoordinates pc = new PointCoordinates();

		WGS84Point wgs84 = toWGS84Coordinates( refSysKey, x, y);
		
		if( wgs84 == null ) {
		    return false;
		}
		
		pc.setLatitude((float) wgs84.y());
		pc.setLongitude((float) wgs84.x());

		pbc.setPointCoordinates(pc);
		if( bearing > -0.2 ) {
		    pbc.setBearing( BigInteger.valueOf( (long)bearing ));
		}

		addItineraryPoint(it, pbc);
		
		return true;
	}	
	
	private GroupOfLocations toD2CoordinateLocation( int srid,
													 Coordinate[] coors,
													 double bearing ) {
		ItineraryByIndexedLocations inils = new ItineraryByIndexedLocations();
		for( Coordinate coor : coors )
		{
			if( !addCoordinateLocation( inils, srid, coor.x, coor.y, bearing ) ) {
			    return null;
			}
		}
		
		return inils;
	}
	
	private AlertCDirectionEnum getAlertCDirection( String dir ) {
	    if( dir.equals( "-1" ) ) {
	        return AlertCDirectionEnum.NEGATIVE; 
	    } else {
            return AlertCDirectionEnum.POSITIVE;
	    }
	} 
	
	
	private void toD2OpenLRLocation( List<Location> locations ) throws WlsException {
		locations.forEach( l -> locId2OpenLRLocations.put( l.getId(), new Point() ) );
		locId2OpenLRLocations = openLRLocalizer.addOpenLRLocations( locations, locId2OpenLRLocations );
	}
	
	private GroupOfLocations toD2AlertCLocation( GeoFeature geoFeature ) {
		
	    String dir = GeoFeatureUtil.getProperty( geoFeature, ALC_DIR );
	    String primLoc = GeoFeatureUtil.getProperty( geoFeature, ALC_PRIM_LOC );
        String primLocName = GeoFeatureUtil.getProperty( geoFeature, ALC_PRIM_LOC_NAME );
        String distance = GeoFeatureUtil.getProperty( geoFeature, ALC_DIST );
        
        if( primLoc != null ) {

            BigInteger pLocCode = BigInteger.valueOf( Long.parseLong( primLoc ) );
            AlertCDirection alcDir = new AlertCDirection();
            AlertCDirectionEnum alcDirVal = getAlertCDirection( dir );
            alcDir.setAlertCDirectionCoded( alcDirVal );
            
            Point point = new Point();
            if( distance == null ) {
                AlertCMethod2Point alcPoint = new AlertCMethod2Point();
                alcPoint.setAlertCLocationCountryCode( LCL_COUNTRY_CODE );
                alcPoint.setAlertCLocationTableNumber( LCL_TABLE_NUMBER );
                alcPoint.setAlertCLocationTableVersion( lclVersion );
                alcPoint.setAlertCDirection( alcDir );
    
                AlertCMethod2PrimaryPointLocation primary = new AlertCMethod2PrimaryPointLocation();
                AlertCLocation pLoc = new AlertCLocation();
                pLoc.setSpecificLocation( pLocCode ); 
                if( primLocName !=null ) {
                    pLoc.setAlertCLocationName( Util.toD2String( primLocName ) );
                }
                primary.setAlertCLocation( pLoc );
    
                alcPoint.setAlertCMethod2PrimaryPointLocation( primary );
                point.setAlertCPoint( alcPoint );
            } else {
                
                AlertCMethod4Point alcPoint = new AlertCMethod4Point();
                alcPoint.setAlertCLocationCountryCode( LCL_COUNTRY_CODE );
                alcPoint.setAlertCLocationTableNumber( LCL_TABLE_NUMBER );
                alcPoint.setAlertCLocationTableVersion( lclVersion );
                alcPoint.setAlertCDirection( alcDir );
                
                AlertCMethod4PrimaryPointLocation primary = new AlertCMethod4PrimaryPointLocation();
                AlertCLocation pLoc = new AlertCLocation();
                pLoc.setSpecificLocation( pLocCode ); 
                if( primLocName !=null ) {
                    pLoc.setAlertCLocationName( Util.toD2String( primLocName ) );
                }
                primary.setAlertCLocation( pLoc );
    
                Double d = Double.parseDouble( distance );
                OffsetDistance pOffset = new OffsetDistance();
                pOffset.setOffsetDistance( BigInteger.valueOf( d.longValue() ) );
                primary.setOffsetDistance( pOffset );
    
                alcPoint.setAlertCMethod4PrimaryPointLocation( primary );
                point.setAlertCPoint( alcPoint );
            }
            
    		
    		return point;
        }
        
        return null;
	}
	
    private void addD2Locations( LocationObject locObject, GeoFeature geoFeature ) {

        GroupOfLocations agol = toD2AlertCLocation( geoFeature );
        if( agol != null ) {
            locObject.setAlertCLocation( agol );
        }
        
        if( addOpenLRLocation ) {
        	eu.datex2.schema._2._2_0.Location d2Location = locId2OpenLRLocations.get( geoFeature.getId() );
        	if( d2Location != null ) {
                locObject.setOpenLRLocation( d2Location );
        	}
        }
        
        String cw = GeoFeatureUtil.getProperty( geoFeature, D2_CARRIAGEWAY );
        if( cw != null ) {
            locObject.setCarriageway( cw );
        }

        String heading = GeoFeatureUtil.getProperty( geoFeature, HEADING );
        double bearing = -1.0;
        if( ( heading != null ) && !heading.isEmpty() ) {
            bearing = Double.parseDouble( heading );
        }
        GroupOfLocations gol = toD2CoordinateLocation( getSRID( geoFeature.getSrid()), geoFeature.getGeometry().getCoordinates(), bearing );
        if (gol != null) {
            locObject.setCoordinateLocation(gol);
        }
    }	
	
	
	
	/**
	 * 
	 * Returns objects of type <locType>, filtered by <filter>
	 *          
	 * 
	 * @param filter        object filter
	 * @param locType       location type
	 * @param ids			Location IDs
	 * @return              location objects
	 * @throws SvcException exception
	 */
	public Collection<LocationObject> getLocationObjects( LocationFilter filter, String locType, Set<String> ids )
			throws SvcException									
	{
		try
		{
			GetLocationsRequest request = new GetLocationsRequest();
            request.setLocationType( GeoFeature.class.getName() );
			
			de.heuboe.wls.data.wls.Parameter param =  new ParameterType( locType );
            request.setParameter( param );
			
			GetLocationsResponse response = webLocationServer.getLocations( request );
		
			List<Location> locations = response.getLocations();
			
			if( ( ids != null ) && !ids.isEmpty() ) {
				locations = locations.stream().filter( l -> ids.contains( l.getId() ) ).collect( Collectors.toList() );
			}
			
	        if( addOpenLRLocation ) {
	        	toD2OpenLRLocation( locations );
	        }
			
			List<LocationObject> locObjects = new ArrayList<>();
			for( Location location : locations ) {
			    
			    GeoFeature geoFeature = (GeoFeature)location;
			    
				LocationObject locObject = new LocationObject();
				locObject.setId( location.getId() );
				
				locObject.setName( getName( geoFeature ) );
				
				String geoDynId = GeoFeatureUtil.getProperty( geoFeature, GEODYN_ID );
				if( geoDynId != null ) {
				    locObject.setGeoDynId( Integer.parseInt( geoDynId ) );
				}
				
				addD2Locations( locObject, geoFeature );			
				
				locObjects.add( locObject );
			}
			
			return locObjects;
		} catch (de.heuboe.wls.iface.Fault | WlsException ex ) {
			throw new SvcException( SvcException.ERROR_WLS_MFS, "Error in getLocationObjects()", ex );
		}
	}	
	
	private String getName( GeoFeature geoFeature  )  {
		String name = geoFeature.getName();
		
		String uz = GeoFeatureUtil.getProperty( geoFeature, "UZ" );
		if( uz != null ) {
			name += "; " + uz;
		}
		
		String road = GeoFeatureUtil.getProperty( geoFeature, "Road" );
		String target = GeoFeatureUtil.getProperty( geoFeature, "Target" );
		String compassDir = GeoFeatureUtil.getProperty( geoFeature, "Direction" );
		if( target != null ) {
			name += "; " + target;
			if( compassDir != null ) {
				name += " (" + compassDir + ")";
			}
		} else if( road != null ) {
			name += ", " + road;
			if( ( compassDir != null ) && !compassDir.isEmpty() ) {
				name += " (" + compassDir + ")";
			}
		}

		String km = GeoFeatureUtil.getProperty( geoFeature, "Kilometer" );
		if( km != null ) {
			name += "; Km=" + km;
		}

		String knNr = GeoFeatureUtil.getProperty( geoFeature, "Knotennummer" );
		if( knNr != null ) {
			name += "; KnNr=" + knNr;
		}
		
		
		return name;
	}
	
}
