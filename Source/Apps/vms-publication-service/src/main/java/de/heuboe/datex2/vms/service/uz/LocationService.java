package de.heuboe.datex2.vms.service.uz;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.DefaultCoordinateOperationFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import de.heuboe.datex2.vms.service.D2Location;
import de.heuboe.datex2.vms.service.ObjectKey;
import de.heuboe.datex2.vms.service.VMSServiceException;
import de.heuboe.datex2.wls.OpenLRLocalizer;
import de.heuboe.geo.utils.CRSFactory;
import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;
import de.heuboe.wls.data.tcc.TccPointFeature;
import de.heuboe.wls.data.tmc.TmcRefLocation;
import de.heuboe.wls.data.tmc.TmcRefPoint;
import de.heuboe.wls.data.wls.ConvertLocationsRequest;
import de.heuboe.wls.data.wls.ConvertLocationsResponse;
import de.heuboe.wls.data.wls.DataVersionInfo;
import de.heuboe.wls.data.wls.Direction;
import de.heuboe.wls.data.wls.ErrorLocation;
import de.heuboe.wls.data.wls.GeoLocation;
import de.heuboe.wls.data.wls.GeometryFeature;
import de.heuboe.wls.data.wls.GetCapabilitiesRequest;
import de.heuboe.wls.data.wls.GetDataVersionInfoRequest;
import de.heuboe.wls.data.wls.GetDataVersionInfoResponse;
import de.heuboe.wls.data.wls.GetLocationsRequest;
import de.heuboe.wls.data.wls.GetLocationsResponse;
import de.heuboe.wls.data.wls.NormalisedLocation;
import de.heuboe.wls.data.wls.ParameterId;
import de.heuboe.wls.data.wls.PointLocation;
import de.heuboe.wls.data.wls.Property;
import de.heuboe.wls.data.wls.TmcLocationTableVersion;
import de.heuboe.wls.iface.Fault;
import de.heuboe.wls.iface.WebLocationServer;
import de.heuboe.wls.util.WlsException;
import de.heuboe.wls.util.WlsWebServiceClient;
import eu.datex2.schema._2._2_0.AlertCDirection;
import eu.datex2.schema._2._2_0.AlertCDirectionEnum;
import eu.datex2.schema._2._2_0.AlertCLocation;
import eu.datex2.schema._2._2_0.AlertCMethod4Point;
import eu.datex2.schema._2._2_0.AlertCMethod4PrimaryPointLocation;
import eu.datex2.schema._2._2_0.CarriagewayEnum;
import eu.datex2.schema._2._2_0.Location;
import eu.datex2.schema._2._2_0.OffsetDistance;
import eu.datex2.schema._2._2_0.Point;
import eu.datex2.schema._2._2_0.PointByCoordinates;
import eu.datex2.schema._2._2_0.PointCoordinates;

public class LocationService extends LocationManager
{
	private static class WGS84Point
	{
		private double x;
		private double y;
		
		public WGS84Point( double x, double y )    
		{                                            
			this.x = x;
			this.y = y;
		}
		
		public double x()							
		{                                               
			return x;
		}
		
		public double y()							
		{                                             
			return y;
		}
	}
	
	private static final Logger LOGGER = Logger.getLogger( LocationService.class );
	private int wlsSrid = -1;
	
	private MathTransform transform = null;
	private WebLocationServer wls;
	private OpenLRLocalizer openLRLocalizer = null;
	private String lclVersion = "16.0";
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param wlsSrid	SRID of WLS object geometries
	 */
	public LocationService( int wlsSrid, boolean useCarriagewayAttr )
	{
		super( useCarriagewayAttr );
		this.wlsSrid = wlsSrid;
	}
	
	@Override
	public void init()
	{
		locidWithoutTMCLoc.clear();
		locidWithoutNormLoc.clear();
	}

	/////////////////////////////////////////////////////////////////
	// Coordinate transformation
	/////////////////////////////////////////////////////////////////
	
	private int getSRID( Integer srid )
	{
		if( ( srid == null ) || ( srid == 0 ) )
		{
			return wlsSrid;
		}
		
		return srid;
	}
	
	private void createTransform( int refSysKey )
	{
		try
		{
			int key = refSysKey;
			if( key == 0 ) 
			{
				key = wlsSrid;
			}
			
			CoordinateReferenceSystem wgs84CRS = DefaultGeographicCRS.WGS84;
			CoordinateReferenceSystem gkCRS = CRSFactory.getCRS( "epsg:" + key );
			
			DefaultCoordinateOperationFactory trFactory = new DefaultCoordinateOperationFactory();
			CoordinateOperation operation = trFactory.createOperation(gkCRS, wgs84CRS);
			
			transform = operation.getMathTransform();   
		}
		catch( FactoryException ex ) 
		{
			LOGGER.error( "Unable to init GeoTools !" );
			LOGGER.error( ex.toString(), ex );
			Util.exit( -1 );
		}
	}
	
	
	private MathTransform getTransform( int refSysKey )
	{
		if( transform == null )
		{
			createTransform( refSysKey );
		}
		
		return transform;
	}
	
	
	public boolean connect( String url )
	{
		if( ( url == null ) || url.isEmpty() )
		{
			return false;
		}
		
		try
		{
			JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
			factory.setServiceClass( WebLocationServer.class );
			factory.setAddress( url );
			factory.setWsdlURL( url );
			
			String ctxPackages = WlsWebServiceClient.getWlsJAXBContext();
			JAXBContext context = javax.xml.bind.JAXBContext.newInstance( ctxPackages );
																		   
			JAXBDataBinding dataBinding = new JAXBDataBinding( context );
			factory.setDataBinding( dataBinding );
			
			wls = (WebLocationServer)factory.create();
	
	        Client client = ClientProxy.getClient(wls);
	        
	        if (client != null) 
	        {
				HTTPConduit conduit = (HTTPConduit) client.getConduit();
				HTTPClientPolicy policy = new HTTPClientPolicy();
				policy.setConnectionTimeout( Util.CONN_TIMEOUT );
				policy.setReceiveTimeout( Util.CONN_TIMEOUT );
				conduit.setClient(policy);
	        }
	        
	        GetCapabilitiesRequest input = new GetCapabilitiesRequest();
	        wls.getCapabilities( input );
	        
			GetDataVersionInfoResponse r = wls.getDataVersionInfo( new GetDataVersionInfoRequest() );
			DataVersionInfo dvi = r.getDataVersionInfo();
			List<TmcLocationTableVersion> tmcVersions = dvi.getSupportedTmcVersion();
			if( ( tmcVersions != null ) && !tmcVersions.isEmpty() ) {
				lclVersion = tmcVersions.get(0).getTableVersion();
				int pos = lclVersion.indexOf( ":" );
				if( pos != -1 ) {
					lclVersion = lclVersion.substring( pos + 1 );
				}
			}
	        
			LOGGER.info( "Successfully connected to WLS." );
	        return true;
		}
		catch( JAXBException | WlsException | Fault ex )
		{
			LOGGER.fatal( "Failed to connect to WLS" );
			LOGGER.fatal( ex.toString() );
			LOGGER.fatal( CallStack.getStackTraceAsString( ex ) );
			
			return false;
		}
	}
	
	@Override
	public Location getLocation( String objType, String objId, 
								 boolean addTmcLocation, 
								 boolean addOpenLRLocation )
			throws VMSServiceException
	{
		List<D2Location> locations = getLocations( objType, 
												   new TreeSet<>( Arrays.asList( objId ) ),
												   addTmcLocation,
												   addOpenLRLocation );
		
		if( ( locations == null ) || locations.isEmpty() )
			return null;
		
		return  locations.get(0).getLocation();
	}
	
	@Override
	public D2Location getD2Location( String objType, 
			                          String objId, 
								 	 boolean addTmcLocation, 
								 	 boolean addOpenLRLocation )
			throws VMSServiceException
	{
		List<D2Location> locations = getLocations( objType, 
												   new TreeSet<>( Arrays.asList( objId ) ),
												   addTmcLocation,
												   addOpenLRLocation );
		
		if( ( locations == null ) || locations.isEmpty() )
			return null;
		
		return locations.get(0);
	}

	
	private String toWLSType( String objType )
	{
		return TccPointFeature.class.getName();
	}
	
	protected String toLocId( String objId )
	{
		// ### ToDo
		return objId;
	}
	
	private String toObjId( String locId )
	{
		// ### ToDo
		return locId;
	}
	
	@SuppressWarnings("unchecked")
	private <T extends NormalisedLocation> Map< String, NormalisedLocation > getNormalisedLocations( 
										Class<T> clasz,
										Collection<de.heuboe.wls.data.wls.Location> locations,
										boolean showError )
			throws Fault
	{
		Map< String, NormalisedLocation > points = new TreeMap<>();
	
		ConvertLocationsRequest request = new ConvertLocationsRequest();
		request.withSource( locations );
		request.setTargetType( clasz.getName() );
		
		ConvertLocationsResponse response = wls.convertLocations( request );	
		
		List<de.heuboe.wls.data.wls.Location> lls = response.getLocations();
		
		for( de.heuboe.wls.data.wls.Location ll : lls )
		{
			if( ll.getClass() == clasz )
			{
				points.put( ll.getId(), (T)ll );
			}
			else 
			{
				if( showError ) {
					LOGGER.warn( "No " + clasz.getSimpleName() + " for ID <" + ll.getId() + ">" );
					if( ll instanceof ErrorLocation ) {
						LOGGER.warn( "    " + ((ErrorLocation)ll).getMessage() );
					}
				}
			}
		}
		
		return points;
	}
	
	private Set<String> locidWithoutNormLoc = new HashSet<>();
	
	private void noNormLocMsg( Class<?> clasz, String locId, Coordinate coor,
							   List<de.heuboe.wls.data.wls.Location> lls ) {
		if( !locidWithoutNormLoc.contains( locId ) ) 
		{
			LOGGER.warn( "No " + clasz.getSimpleName() + " for ID <" + locId + ">" );
			if( ( lls != null ) && !lls.isEmpty() && ( lls.get(0) instanceof ErrorLocation ) ) {
				LOGGER.warn( "    " + ((ErrorLocation)lls.get(0)).getMessage() );
				LOGGER.warn( "    (" + coor.x + "," + coor.y + ")" );
			}
			locidWithoutNormLoc.add( locId );
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private <T extends NormalisedLocation> NormalisedLocation getNormalisedLocation( Class<T> clasz, String locId, 
																					 Coordinate coor )
			throws Fault
	{
		GeometryFactory factory = new GeometryFactory( new PrecisionModel() );
		org.locationtech.jts.geom.Point pt = factory.createPoint( coor );
		
		GeoLocation geol = new GeoLocation().withId(locId).withName(locId).withGeometry( pt );
		
		ConvertLocationsRequest request = new ConvertLocationsRequest();
		request.withSource( geol );
		request.setTargetType( clasz.getName() );
		
		ConvertLocationsResponse response = wls.convertLocations( request );	
		
		List<de.heuboe.wls.data.wls.Location> lls = response.getLocations();
		
		if( ( lls != null ) && !lls.isEmpty() && lls.get(0).getClass() == clasz )
		{
			return (T)lls.get(0);
		}
		else 
		{
			noNormLocMsg( clasz, locId, coor, lls );
		}
		
		return null;
	}
	
	
	private Coordinate getCoordinate( de.heuboe.wls.data.wls.Location loc ) {
		if( loc instanceof GeometryFeature )
		{
			GeometryFeature gf = (GeometryFeature)loc;

			if( !(
					( gf.getGeometry() == null ) 
					|| 
					( gf.getGeometry().getCoordinates() == null ) 
					||
					( gf.getGeometry().getCoordinates().length == 0 )
				)
			  )
			{
				return gf.getGeometry().getCoordinates()[0];
			}
		}
		
		return null;
	}
	
	private WGS84Point getWGS84Coordinate( de.heuboe.wls.data.wls.Location loc )
	{
		Coordinate coor = getCoordinate( loc );
		if( coor != null ) 
		{
			int refSysKey = getSRID( null );
			try 
			{
				double[] sourceCoor = new double[3];
				sourceCoor[0] = coor.x;
				sourceCoor[1] = coor.y;
				sourceCoor[2] = 0; 			// z-value is 0
				
				double[] targetCoor = new double[3];
				getTransform( refSysKey ).transform(sourceCoor, 0, targetCoor, 0, 1);
				
				return new WGS84Point( targetCoor[0], targetCoor[1] );
			} 
			catch (TransformException ex) 
			{
				LOGGER.error( ex.toString(), ex );
				return null;
			}
		}
		else
		{
			return null;
		}
	}
	
	private Set<String> locidWithoutTMCLoc = new HashSet<>();
	
	private void noTMCLocMsg( de.heuboe.wls.data.wls.Location loc, Exception ex, WGS84Point wgs84Point ) {
		if( !locidWithoutTMCLoc.contains( loc.getId() ) ) 
		{
			LOGGER.warn( "No AlertC representation for location with ID <" +  loc.getId() + ">" );
			if( ex != null ) 
			{
				LOGGER.warn( ex.toString(), ex );
			}
			else
			{
				if( wgs84Point != null ) 
				{
					LOGGER.warn( "    (" + wgs84Point.y + "," + wgs84Point.x + ")" );
				}
			}
			
			locidWithoutTMCLoc.add( loc.getId() );
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T extends TmcRefLocation> T  getTmcLocation( Class<T> clasz, 
														  de.heuboe.wls.data.wls.Location loc,
														  WGS84Point wgs84Point )
	{
		try
		{
			ConvertLocationsRequest clRequest = new ConvertLocationsRequest();
			clRequest.setTargetType( clasz.getName() );
			clRequest.withSource( loc );
			ConvertLocationsResponse clResponse = wls.convertLocations( clRequest );
			List<de.heuboe.wls.data.wls.Location> tmcLocs = clResponse.getLocations();
			
			if( ( tmcLocs == null ) || ( tmcLocs.isEmpty() ) )
			{
				noTMCLocMsg( loc, null, wgs84Point );
				return null;
			}
			
			de.heuboe.wls.data.wls.Location tmcLoc = tmcLocs.get(0);
			if( tmcLoc.getClass() ==  clasz  )
			{
				return (T)tmcLoc;
			}
			else 
			{
				noTMCLocMsg( loc, null, wgs84Point );
				return null;
			}
		}
		catch( Fault ex )  
		{
			noTMCLocMsg( loc, ex, wgs84Point );
			return null;
		}
	}
	
	
	private AlertCDirectionEnum toD2Direction( Direction dir )
	{
		switch( dir )
		{
			case NEGATIVE:
				return AlertCDirectionEnum.NEGATIVE;
			case POSITIVE:
				return AlertCDirectionEnum.POSITIVE;
			case BOTH:
				return AlertCDirectionEnum.BOTH;
			case NONE:
			default:	
				return AlertCDirectionEnum.UNKNOWN;
		}
	}
	
	protected void setBearing( PointByCoordinates pbc, String bearing )
	{
		long b = Math.round( Double.parseDouble( bearing ) );
		long degree = Math.min( 359L, Math.max( 0L, 360L - b ) );
		pbc.setBearing( BigInteger.valueOf( degree ) );
	}
	
	protected void setBearing( PointByCoordinates pbc,  long bearing )
	{
		long degree = Math.min( 359L, Math.max( 0L, 360L - bearing ) );
		pbc.setBearing( BigInteger.valueOf( degree ) );
	}

	protected GetLocationsRequest createGetLocationsRequest( String objType, Set<String> objIds )  {
		
		GetLocationsRequest request = new GetLocationsRequest();
		request.setLocationType( toWLSType( objType ) );
		
		if( objIds.size() == 1 ) 
		{
			String objId = objIds.iterator().next();
			ParameterId pi = new ParameterId( toLocId( objId ) );
			request.setParameter( pi );
		}
		
		return request;
	}
	
	
	protected CarriagewayEnum toCarriageway( String cw ) {
		switch( cw ) {
			case "CONNECTING_CARRIAGEWAY":
				return CarriagewayEnum.MAIN_CARRIAGEWAY;
			case "ENTRY_SLIP_ROAD":
				return CarriagewayEnum.ENTRY_SLIP_ROAD;
			case "EXIT_SLIP_ROAD":
				return CarriagewayEnum.EXIT_SLIP_ROAD;
			case "PARALLEL_CARRIAGEWAY":
				return CarriagewayEnum.PARALLEL_CARRIAGEWAY;
			case "SINGLE_LANE":
			case "MAIN_CARRIAGEWAY":
			default:	
		    	return CarriagewayEnum.MAIN_CARRIAGEWAY;
		}
	}
	
	protected void registerCarriageway( de.heuboe.wls.data.wls.Location location ) {
		
		if( location instanceof TccPointFeature )
		{
			TccPointFeature tpf = (TccPointFeature)location;
			Optional<Property> prop = tpf.getProperties()
								    	.stream()
								    	.filter( p -> p.getName().equals("CARRIAGEWAY" ) )
								    	.findFirst();
			if( prop.isPresent() ) {
				objId2Carriageway.put( location.getId(), toCarriageway( prop.get().getValue() ) );
			}
		}
	}
	
	protected void addBearing( de.heuboe.wls.data.wls.Location location, PointByCoordinates pbc ) {
		
		if( location instanceof TccPointFeature )
		{
			TccPointFeature tpf = (TccPointFeature)location;
			tpf.getProperties()
					    .stream()
					    .filter( p -> p.getName().equals("WINKEL" ) )
					    .findFirst()
					    .ifPresent( prop -> setBearing( pbc, prop.getValue() ) ); 
		}
	}
	
	@Override
	public List<D2Location> getLocations( String objType,              // NOSONAR
			 							  Set<String> objIds,
			 							  boolean addTmcLocation,
			 							  boolean addOpenLRLocation )
			throws VMSServiceException	 									
    {
		try
		{
			GetLocationsRequest request = createGetLocationsRequest( objType, objIds );
			GetLocationsResponse response = wls.getLocations( request );
		
			List<de.heuboe.wls.data.wls.Location> locations = response.getLocations();
			Map< String, de.heuboe.wls.data.wls.Location > wlsLocations = new HashMap<>();
			for( de.heuboe.wls.data.wls.Location location : locations )
			{
				if( objIds.contains( toObjId( location.getId() ) ) ) {
					registerCarriageway( location );					
					wlsLocations.put( location.getId(),  location );
				}
			}
			
			Map< String, NormalisedLocation > points = getNormalisedLocations( PointLocation.class,
																			   wlsLocations.values(),
																			   false );
			
			List<D2Location> d2Locations = new ArrayList<>();
			for( String objId : objIds )
			{
				Point d2Location = new Point();
				
				de.heuboe.wls.data.wls.Location location = wlsLocations.get( toLocId( objId ) );
				
				if( location == null )
				{
					continue;
				}
				
				WGS84Point wgs84Point = getWGS84Coordinate( location );
				if( wgs84Point != null )
				{
					PointByCoordinates pbc = new PointByCoordinates();
					PointCoordinates pc = new PointCoordinates();

					pc.setLatitude((float) wgs84Point.y() );
					pc.setLongitude((float) wgs84Point.x() );

					pbc.setPointCoordinates(pc);
					d2Location.setPointByCoordinates( pbc );
					
					addBearing( location, pbc );
				}
				
				NormalisedLocation pl = points.get( toLocId( objId ) );
				
				if( ( pl == null ) && addTmcLocation )
				{
					Coordinate coor = getCoordinate( location );
					if( coor != null )
					{
						pl = getNormalisedLocation( PointLocation.class, location.getId(), coor );
						if( pl != null )
						{
							points.put( toLocId( objId ), pl );
						}
					}
				}
				
				if( addTmcLocation && ( pl != null ) )
				{
					TmcRefPoint tmcLoc = getTmcLocation( TmcRefPoint.class, pl, wgs84Point );
					
					
					if( tmcLoc != null )
					{
						AlertCMethod4Point alcPoint = new AlertCMethod4Point();
						
						alcPoint.setAlertCLocationCountryCode( "D" );
						alcPoint.setAlertCLocationTableNumber( "01" );
						
						alcPoint.setAlertCLocationTableVersion( lclVersion );
						String version = tmcLoc.getVersion();
						if( version != null )
						{
							int pos =version.indexOf( ':' );
							if( pos != -1 )
							{
								alcPoint.setAlertCLocationTableVersion( version.substring( pos + 1 ) );
							}
						}
						
						AlertCMethod4PrimaryPointLocation alcPrimary = new AlertCMethod4PrimaryPointLocation();
						
						AlertCLocation alcLoc = new AlertCLocation();
						long locId = Long.parseLong( tmcLoc.getLocationId() );
						alcLoc.setSpecificLocation( BigInteger.valueOf( locId ) );
						alcPrimary.setAlertCLocation( alcLoc );
						
						OffsetDistance od = new OffsetDistance();
						od.setOffsetDistance( BigInteger.valueOf( Math.round( tmcLoc.getDistance() ) ) );
						alcPrimary.setOffsetDistance( od );
						
						AlertCDirection alcDir = new AlertCDirection();
						alcDir.setAlertCDirectionCoded( toD2Direction( tmcLoc.getDirection() ) ); 
						alcPoint.setAlertCDirection( alcDir ); 
						
						alcPoint.setAlertCMethod4PrimaryPointLocation( alcPrimary );
						
						d2Location.setAlertCPoint( alcPoint );
					}
				}
				
				D2Location d2l = new D2Location();
				d2l.setLocation( d2Location );
				d2l.setObjKey( new ObjectKey( objType, objId  ) );
				d2l.setDescription( getName( location ) );
				d2Locations.add( d2l );
			}
			
			if( addOpenLRLocation && ( points.size() > 0 ) ) 
			{
				if( openLRLocalizer == null ) 
				{
					openLRLocalizer = new OpenLRLocalizer( wls ); 
				}
				
				
				Map<String,Location> locationMap = new HashMap<>();
				Map<String,D2Location> d2LocationMap = new HashMap<>();
				for( D2Location d2Location: d2Locations )
				{
					locationMap.put( toLocId( d2Location.getObjKey().getId() ), 
							         d2Location.getLocation() );
					d2LocationMap.put( d2Location.getObjKey().getId(), d2Location );
				}
				
				List<de.heuboe.wls.data.wls.Location> locs = new ArrayList<>( );
				for( NormalisedLocation point : points.values() )
				{
					locs.add( point );
				}
				Map<String,Location> completedLocations = openLRLocalizer.addOpenLRLocations( locs, locationMap );
				
				Iterator<Map.Entry<String,Location>> iter = completedLocations.entrySet().iterator();
				while (iter.hasNext()) {
				    Map.Entry<String,Location> item = iter.next();
				    String objId = toObjId( item.getKey() );
				    Location location = item.getValue();
				    D2Location d2l = d2LocationMap.get( objId );
				    if( d2l != null ) {
				    	d2l.setLocation( location );
				    }
				}
			}
			
			return d2Locations;
		} catch( Fault | JAXBException | WlsException ex ) {
			throw new VMSServiceException( VMSServiceException.VMS_SVC_ERR_WLS, "Error in WLS.getLocations()", ex );
		}
    }
	
    public static String getProperty( TccPointFeature geoFeature, String name ) {
        List<Property> props = geoFeature.getProperties();
        for( Property prop : props ) {
            if( prop.getName().equals(name) ) {
                return prop.getValue();
            }
        }
        
        return null;
    }
	
	private String getName( de.heuboe.wls.data.wls.Location location  )  {
		
		if( !( location instanceof TccPointFeature ) ) {
			return null;
		}
		
		TccPointFeature geoFeature = (TccPointFeature)location;
		
		String name = geoFeature.getName();
		
		String uz = getProperty( geoFeature, "UZ" );
		if( uz != null ) {
			name += "; " + uz;
		}
		
		String road = getProperty( geoFeature, "Road" );
		String target = getProperty( geoFeature, "Target" );
		String compassDir = getProperty( geoFeature, "Direction" );
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

		String km = getProperty( geoFeature, "Kilometer" );
		if( km != null ) {
			name += "; Km=" + km;
		}

		String knNr = getProperty( geoFeature, "Knotennummer" );
		if( knNr != null ) {
			name += "; KnNr=" + knNr;
		}
		
		return name;
	}


}
