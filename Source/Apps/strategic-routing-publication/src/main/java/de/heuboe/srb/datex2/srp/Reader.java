package de.heuboe.srb.datex2.srp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import de.heuboe.c2vba.data.StrategyState;
import de.heuboe.c2vba.data.StrategyStates;
import de.heuboe.datex2.base.ProfileConverter;
import de.heuboe.datex2.wls.AlertCLocalizerD2Profile;
import de.heuboe.datex2.wls.AlertCLocalizerD2Profile.TmcDirectionResolver;
import de.heuboe.datex2.wls.OpenLRLocalizerD2Profile;
import de.heuboe.log.Logger;
import de.heuboe.sdbby.service.data.StrategyItem;
import de.heuboe.sdbby.service.data.StrategyRequest;
import de.heuboe.sdbby.service.data.StrategySet;
import de.heuboe.sdbby.service.iface.SdbbyService;
import de.heuboe.srb.datex2.srp.LocationBuilder.WGS84Point;
import de.heuboe.system.CallStack;
import de.heuboe.util.JAXBUtil;
import de.heuboe.wls.data.openlr.OpenLRLocation;
import de.heuboe.wls.data.openlr.OpenLRLocationXml;
import de.heuboe.wls.data.roadmap.RouteDefinition;
import de.heuboe.wls.data.roadmap.RouteDetails;
import de.heuboe.wls.data.sdbby.SdbbyRoute;
import de.heuboe.wls.data.sdbby.SdbbyStrategy;
import de.heuboe.wls.data.wls.ConvertLocationsRequest;
import de.heuboe.wls.data.wls.ConvertLocationsResponse;
import de.heuboe.wls.data.wls.DataVersionInfo;
import de.heuboe.wls.data.wls.GeoLocation;
import de.heuboe.wls.data.wls.GetDataVersionInfoRequest;
import de.heuboe.wls.data.wls.GetDataVersionInfoResponse;
import de.heuboe.wls.data.wls.GetLocationsRequest;
import de.heuboe.wls.data.wls.GetLocationsResponse;
import de.heuboe.wls.data.wls.LinearLocation;
import de.heuboe.wls.data.wls.Parameter;
import de.heuboe.wls.data.wls.ParameterFlags;
import de.heuboe.wls.data.wls.PointLocation;
import de.heuboe.wls.data.wls.TmcLocationTableVersion;
import de.heuboe.wls.iface.Fault;
import de.heuboe.wls.iface.WebLocationServer;
import de.heuboe.wls.util.WlsException;
import de.heuboe.wls.utils.CoordinateTransformer;
import de.heuboe.wls.utils.GeometryTools;
import eu.datex2.schema._2._2_0.OpenlrPointLocationReference;
import eu.datex2.schema._2._2_0.srp.AlertCArea;
import eu.datex2.schema._2._2_0.srp.AlertCLocation;
import eu.datex2.schema._2._2_0.srp.Area;
import eu.datex2.schema._2._2_0.srp.Itinerary;
import eu.datex2.schema._2._2_0.srp.ItineraryByIndexedLocations;
import eu.datex2.schema._2._2_0.srp.Linear;
import eu.datex2.schema._2._2_0.srp.Location;
import eu.datex2.schema._2._2_0.srp.OpenlrExtendedPoint;
import eu.datex2.schema._2._2_0.srp.OpenlrGeoCoordinate;
import eu.datex2.schema._2._2_0.srp.OpenlrLineLocationReference;
import eu.datex2.schema._2._2_0.srp.Point;
import eu.datex2.schema._2._2_0.srp.PointByCoordinates;
import eu.datex2.schema._2._2_0.srp.PointCoordinates;
import eu.datex2.schema._2._2_0.srp.Route;
import eu.datex2.schema._2._2_0.srp.StrategicRouteManagement;
import eu.datex2.schema._2._2_0.srp._LocationContainedInItinerary;
import openlr.PhysicalFormatException;
import openlr.datex2.Datex2Location;
import openlr.datex2.OpenLRDatex2Decoder;
import openlr.datex2.impl.LocationReferenceImpl;
import openlr.rawLocRef.RawLocationReference;
import openlr.xml.OpenLRXMLEncoder;
import openlr.xml.OpenLRXmlWriter;
import openlr.xml.generated.OpenLR;
import openlr.xml.impl.LocationReferenceXmlImpl;


/**
 * 
 * Strategy reader
 * 
 * @author peters
 *
 */
public class Reader
{
	private static final Logger LOGGER = Logger.getLogger( Reader.class );
	
	private WebLocationServer wlsService; 
	
	private TmcDirectionResolver tmcDirectionResolver;
	private LocationBuilder locationBuilder;	
	private SdbbyService sdbbyService;
	private Map<String,SdbbyStrategy> strategyLocations = new HashMap<>();
	private Map<String,StrategicRouteManagement> strategicRouteManagements = new HashMap<>();
	
	private String strategyRoutesFilePath;
	private List<String> routeEncoding;
	
	private String wlsLocationDir;
	
	private OpenLRLocalizerD2Profile<Location> openLRLocalizer;
	private AlertCLocalizerD2Profile<Location> alertCLocalizer;
	
	@Autowired
	private ProfileConverter< eu.datex2.schema._2._2_0.srp.OpenlrPointLocationReference,
	                          eu.datex2.schema._2._2_0.OpenlrPointLocationReference> profileConverterOpenLr;
	
	private String lclVersion = "20.0";
	
	private boolean routeCoordinatesWritten = false;
	
	private boolean convert2BaseSchema;
	
	
	/**
	 * 
	 * Strategy object
	 * 
	 * @author peters
	 *
	 */
	public class D2Strategy
	{
		private String id;
		private String name;
		
		private StrategicRouteManagement strategicRouteManagement = null;
		
		public StrategicRouteManagement getStrategicRouteManagement() {
			return strategicRouteManagement;
		}

		public void setStrategicRouteManagement(StrategicRouteManagement strategicRouteManagement) {
			this.strategicRouteManagement = strategicRouteManagement;
		}

		private Location location;
		
		private Location triggerLocation;
		private String triggerDescription;
		
		private Location triggerDestination;
		
		private String originalRouteName;
		private String alternativeRouteName;
		private Itinerary originalRoute;
		private Itinerary alternativeRoute;
		
		public void setId( String id )
		{
			this.id = id;
		}
		
		public String getId()
		{
			return id;
		}
		
		public void setName( String name )
		{
			this.name = name;
		}
		
		public String getName()
		{
			return name;
		}
		
		public Location getLocation()
		{
			return location;
		}

		public void setLocation(Location location)
		{
			this.location = location;
		}
		
		public Location getTriggerLocation()
		{
			return triggerLocation;
		}

		public void setTriggerLocation( Location triggerLocation)
		{
			this.triggerLocation = triggerLocation;
		}

		public Itinerary getOriginalRoute()
		{
			return originalRoute;
		}

		public void setOriginalRoute(Itinerary originalRoute)
		{
			this.originalRoute = originalRoute;
		}

		public Itinerary getAlternativeRoute()
		{
			return alternativeRoute;
		}
		public void setAlternativeRoute(Itinerary alternativeRoute)
		{
			this.alternativeRoute = alternativeRoute;
		}
		public String getOriginalRouteName()
		{
			return originalRouteName;
		}

		public void setOriginalRouteName(String originalRouteName)
		{
			this.originalRouteName = originalRouteName;
		}

		public String getTriggerDescription()
		{
			return triggerDescription;
		}

		public void setTriggerDescription(String triggerDescription)
		{
			this.triggerDescription = triggerDescription;
		}
		
		public String getAlternativeRouteName()
		{
			return alternativeRouteName;
		}

		public void setAlternativeRouteName(String alternativeRouteName)
		{
			this.alternativeRouteName = alternativeRouteName;
		}

		public Location getTriggerDestination() {
			return triggerDestination;
		}

		public void setTriggerDestination(Location triggerDestination) {
			this.triggerDestination = triggerDestination;
		}
		
	}
	
	/**
	 * 
	 * Constructor 
	 * 
	 * @param wlsService				WLS
	 * @param sdbbyService				SDBBY service
	 * @param lclVersion				LCL version
	 * @param strategyRoutesFilePath	Coordinate files of strategy routes are saved here
	 * @param wlsLocationDir			WLS LinearLocation files of strategy routes are saved here	
	 * @param routeEncoding				Location encodings
	 * @param convert2BaseSchema		true: use DATEX-II base schema
	 * @throws SRPException				Error
	 */
	public Reader( WebLocationServer wlsService, 
			       SdbbyService sdbbyService, 
			       String lclVersion, 
			       String strategyRoutesFilePath,
			       String wlsLocationDir,
			       List<String> routeEncoding,
			       boolean convert2BaseSchema )
			throws SRPException
	{
		this.wlsService = wlsService; 
		this.sdbbyService = sdbbyService;
		this.lclVersion = lclVersion;
		this.strategyRoutesFilePath = strategyRoutesFilePath;
		this.wlsLocationDir = wlsLocationDir;
		this.routeEncoding = routeEncoding;
		this.convert2BaseSchema = convert2BaseSchema;
		
		locationBuilder = new LocationBuilder();
		
		try
		{
			tmcDirectionResolver = new SrpTmcDirectionResolver( wlsService );

			readStrategyLocations();
			
			if( ( lclVersion == null ) || lclVersion.isEmpty() ) {
				GetDataVersionInfoResponse r = wlsService.getDataVersionInfo( new GetDataVersionInfoRequest() );
				DataVersionInfo dvi = r.getDataVersionInfo();
				List<TmcLocationTableVersion> tmcVersions = dvi.getSupportedTmcVersion();
				if( ( tmcVersions != null ) && !tmcVersions.isEmpty() ) {
					lclVersion = tmcVersions.get(0).getTableVersion();
				}
			}
		} catch( JAXBException | Fault ex ) {
			throw new SRPException( SRPException.ERROR_SVC, "Cannot connect to WLS", ex );
		}
	}
	
	/**
	 * 
	 * Init
	 * 
	 * @throws SRPException Error
	 */
	public void init() throws SRPException {
		readOpenLrLocations();
	}
	
	//  OpenLR-Locations der Strategie-Routen werden in WLS-LinearLocations konvertiert und in Dateien gespeichert
	//  Dateien gehören zur Versorgung von WLS und GeoManager
	@SuppressWarnings("unused")
	private void openLrLocationsToWlsLocations() {     // NOSONAR
		
	    ObjectMapper objectMapper = new ObjectMapper();
	    ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
		OpenLRXMLEncoder xmlEncoder = new OpenLRXMLEncoder();
		OpenLRXmlWriter writerXml = null;
	    try {
	    	writerXml = new OpenLRXmlWriter();
		} catch (IOException ex ) {
			LOGGER.error( "Cannot instantiate OpenLRXmlWriter: " + ex.toString() );
		}
		
		for( StrategicRouteManagement srm : strategicRouteManagements.values() ) {
			String srmName = srm.getNameOfRouteManagement().getValues().getValue().get(0).getValue();

			int numAlternativeRoutes = 0;
			for( Route route : srm.getRoute() ) {
				if( !Boolean.TRUE.equals(route.isOriginalRoute()) ) {
					numAlternativeRoutes++;
				}
			}			
			
			int countAlternativeRoute = 0;
			for( Route route : srm.getRoute() ) {
				String routeName = srmName + " " + route.getNameOfRoute().getValues().getValue().get(0).getValue();
				String routeLocFileName = srmName;
				if( Boolean.TRUE.equals(route.isOriginalRoute()) ) {
					routeLocFileName += ".M";
				} else {
					countAlternativeRoute++;
					routeLocFileName += ".A";
					if( numAlternativeRoutes > 1 ) {
						routeLocFileName += "" + countAlternativeRoute;
					}
				}
				
				LOGGER.info( routeLocFileName );
				
				ItineraryByIndexedLocations ibils = (ItineraryByIndexedLocations)route.getItinerary();
				
				List<de.heuboe.wls.data.wls.Location> routePls = new ArrayList<>();
				
				for( _LocationContainedInItinerary lcii : ibils.getLocationContainedInItinerary() ) {
					
					PointLocation pl = null;
					
					Point point = (Point) lcii.getLocation();
					OpenlrExtendedPoint olep = point.getPointExtension().getOpenlrExtendedPoint();
					
					try {
						OpenlrPointLocationReference olplr = profileConverterOpenLr.convertS( olep.getOpenlrPointLocationReference() );
						olplr.setOpenlrGeoCoordinate( null );
						Datex2Location d2Loc = new Datex2Location( olplr );
						LocationReferenceImpl lri = new LocationReferenceImpl( "1", d2Loc, 1 );
						OpenLRDatex2Decoder decoder = new OpenLRDatex2Decoder();

						RawLocationReference rlr = decoder.decodeData( lri );
						
						LocationReferenceXmlImpl xmlLr = (LocationReferenceXmlImpl) xmlEncoder.encodeData(rlr);
						OpenLR data = (OpenLR) xmlLr.getLocationReferenceData();
						ByteArrayOutputStream oStrm = new ByteArrayOutputStream();
						writerXml.saveOpenLRXML(data, oStrm, false);
						
						OpenLRLocation wlsLoc = new OpenLRLocationXml( "1", "1", null, oStrm.toString() );
						ConvertLocationsRequest request = new ConvertLocationsRequest();
						request.setTargetType( PointLocation.class.getName() );
						request.getSource().add( wlsLoc );
						ConvertLocationsResponse response = wlsService.convertLocations( request );
						
						de.heuboe.wls.data.wls.Location rpl = response.getLocations().get(0);
						if( rpl instanceof PointLocation ) {
							pl = (PointLocation)rpl;
						} else {
							LOGGER.warn( "    Cannot convert from OpenLR" );
						}
						
					} catch (PhysicalFormatException | Fault | SAXException | JAXBException ex ) {
						LOGGER.error( ex.toString() );
						LOGGER.error( CallStack.getStackTraceAsString(ex) );
					}
					
					if( pl == null ) {
						
						OpenlrGeoCoordinate geoCoor = olep.getOpenlrPointLocationReference().getOpenlrGeoCoordinate();
						if( geoCoor!= null ) {
							PointCoordinates pc = geoCoor.getOpenlrCoordinate();
							if( pc != null ) {
								try {
									Coordinate coor = new Coordinate( pc.getLatitude(), pc.getLongitude() );
									
									LOGGER.info( "    (" + pc.getLatitude() + "," + pc.getLongitude() + ")" );
									
									ConvertLocationsRequest request = new ConvertLocationsRequest();
									request.setTargetType( PointLocation.class.getName() );
									GeoLocation geol = new GeoLocation( "1", "1", GeometryTools.createPoint( coor ), CoordinateTransformer.WGS84_ID , null );
									request.getSource().add( geol );
									ConvertLocationsResponse response;
										response = wlsService.convertLocations( request );
									de.heuboe.wls.data.wls.Location rpl = response.getLocations().get(0);
									if( rpl instanceof PointLocation ) {
										pl = (PointLocation)rpl;
									}
								} catch( Fault ex ) {
									LOGGER.error( "    Cannot convert OpenLR coordinate location of <" + routeName + "> to WLS location: " + ex.toString() );
								}
							}
						}
					}
					
					if( pl != null ) {
						boolean add = true;
						if( !routePls.isEmpty() ) {
							PointLocation prePl = (PointLocation)routePls.get( routePls.size() - 1 );
							if( prePl.getElement().getId().equals( pl.getElement().getId() ) && 
								( Math.abs( prePl.getFromOffset() - pl.getFromOffset() ) < 0.1 ) 	) {
								add = false;
							}
						}
						if( add ) {
							LOGGER.info( "    " + pl.getElement().getId() + ": " + pl.getFromOffset() );
							routePls.add( pl );
						}
					} else {
						LOGGER.error( "    Cannot map OpenLR point location of <" + routeName + ">" );
					}
				}
				if( routePls.size() >= 2  ) {
					
					try {
						List<de.heuboe.wls.data.wls.Location> vias = routePls.subList( 1, routePls.size() - 1 );
			            RouteDefinition rd = new RouteDefinition( routeLocFileName, routeLocFileName, routePls.get(0), routePls.get( routePls.size() - 1 ), vias );
			            
						ConvertLocationsRequest request = new ConvertLocationsRequest();
						request.setTargetType( RouteDetails.class.getName() );
						request.getSource().add( rd );
						
						ConvertLocationsResponse response = wlsService.convertLocations( request );
						de.heuboe.wls.data.wls.Location routeLoc = response.getLocations().get(0); 
			            if( routeLoc instanceof RouteDetails ) {
			                LinearLocation linLoc = ((RouteDetails)routeLoc).getLinLoc();
			                String linLocString = objectWriter.writeValueAsString( linLoc );
			                FileUtils.write( new File( wlsLocationDir + File.separator + routeLocFileName  ), linLocString, StandardCharsets.UTF_8 );
			            } else {
			                LOGGER.error( "No road net route for strategy route <" + routeName + ">" );
			            }
		        
					} catch( Fault | IOException ex ) {
						LOGGER.error( "Cannot create WLS route for <" + routeName + ">: " + ex.toString() );
					}
					
				} else {
	                LOGGER.error( "Cannot map OpenLR location of <" + routeName + "> to road net location" );
				}
			}
			
		}
	}
	
	private void readStrategyLocations()
			throws JAXBException, Fault 
	{
        
        GetLocationsRequest request = new GetLocationsRequest();
        request.setLocationType( SdbbyStrategy.class.getName()  );
		Parameter pFlags = new ParameterFlags( Arrays.asList(  "COMPLETE" ) ); 
		request.setParameter( pFlags );
        GetLocationsResponse response = wlsService.getLocations( request );
        
        List<de.heuboe.wls.data.wls.Location> locations = response.getLocations();
        for( de.heuboe.wls.data.wls.Location location : locations )
        {
        	if( location instanceof SdbbyStrategy )
        	{
        		SdbbyStrategy strategyLocation = (SdbbyStrategy)location;
        		strategyLocations.put( location.getId(), strategyLocation );
        	}
        }
        
    	openLRLocalizer = new OpenLRLocalizerD2Profile<>( wlsService, 
    			                                          Publisher.SCHEMA_FILE, 
    			                                          Publisher.D2_PACKAGE, 
    			                                          Location.class );
    	
    	alertCLocalizer = new AlertCLocalizerD2Profile<>( wlsService, 
                                                          tmcDirectionResolver,
														  lclVersion,
										                  Publisher.SCHEMA_FILE, 
										                  Publisher.D2_PACKAGE, 
										                  Location.class );  
	}
	
	private Location addOpenLR( de.heuboe.wls.data.wls.Location point, 
							    Location d2Location )
	{
		try
		{
			Map< String, Location > d2lm = new HashMap<>();
			d2lm.put( point.getId(), d2Location  );
	
			return openLRLocalizer.addOpenLRLocations( point, d2Location );
		} catch( WlsException ex ) {
			LOGGER.error( "Failed to add OpenLR location representation." );
			LOGGER.error( ex.toString() );
			LOGGER.error( CallStack.getStackTraceAsString( ex ) );
			return null;
		}
	}
	
	private Location addAlertCPoint( de.heuboe.wls.data.wls.Location point,
							   		Location d2Location)
	{
		try
		{
			Map<String, Location> d2lm = new HashMap<>();
			d2lm.put(point.getId(), d2Location);

			return alertCLocalizer.addAlertCPointLocation(point, d2Location);
		} catch (WlsException ex) {
			LOGGER.error("Failed to add AlertC location representation.");
			LOGGER.error(ex.toString());
			LOGGER.error(CallStack.getStackTraceAsString(ex));
			return null;
		}
	}
	
	
	private void setLocation( SdbbyStrategy strategyLocation, Location d2Location )
	
	{
		// ### Assume AlertC area
		
		Area area = (Area)d2Location;
		area.setAreaExtension( null ); 
		
		AlertCArea alertCArea = area.getAlertCArea(); 
		alertCArea.setAlertCLocationTableVersion( lclVersion );
		
		AlertCLocation alertCLocation = alertCArea.getAreaLocation();
		
		Integer ac = Integer.getInteger( strategyLocation.getAreaCode() );
		if( ac != null )
		{
			alertCLocation.setSpecificLocation( BigInteger.valueOf( ac ) );
		}

		alertCArea.setAreaLocation( alertCLocation );
	}
	
	private Location getTriggerLocation( de.heuboe.wls.data.wls.Location triggerLocation ) 
			throws SRPException					     
	{
		
		List<WGS84Point> points = locationBuilder.getCoordinates( triggerLocation );	
		if( points == null )
		{
			throw new SRPException( SRPException.ERROR_LOC, "No coordinates for trigger location" );
		}
		
		Point point = new Point();
		
		PointByCoordinates pbc = new PointByCoordinates();
		PointCoordinates pc = new PointCoordinates();

		pc.setLatitude((float) points.get(0).y() );
		pc.setLongitude((float) points.get(0).x() );

		pbc.setPointCoordinates(pc);
		point.setPointByCoordinates( pbc );
		
		if( routeEncoding.contains( Properties.ROUTE_ENCODING_OPENLR ) )
		{
			Location pointExt = addOpenLR( triggerLocation, point ); 
			if( pointExt != null ) 
			{
				if( !routeEncoding.contains( Properties.ROUTE_ENCODING_COORD ) ) {
					((Point)pointExt).setPointByCoordinates( null );
				}
				point = (Point)pointExt;
			}
		}
		
		if( routeEncoding.contains( Properties.ROUTE_ENCODING_ALERTC ) )
		{
			Location pointExt = addAlertCPoint( triggerLocation, point ); 
			if( pointExt != null ) 
			{
				point = (Point)pointExt;
			}		
		}
		
		return point;
	}
	
	private Itinerary toItinerary( SdbbyRoute route, Publisher publisher )
			throws SRPException, WlsException
	{
		ItineraryByIndexedLocations itinerary = new ItineraryByIndexedLocations();
		boolean addedOpenLR = false;

		Location linearLocation = publisher.getLinearLocation();
		if( routeEncoding.contains( Properties.ROUTE_ENCODING_OPENLR ) )
		{
			Location completedLoc = openLRLocalizer.addOpenLRLocations( route.getLinearLocation(), 
														                linearLocation );
			if( completedLoc != null )
			{
				linearLocation = completedLoc;
				
				if( convert2BaseSchema ) {
					OpenlrLineLocationReference ollr =  ((Linear)linearLocation).getLinearExtension().getOpenlrExtendedLinear().getFirstDirection();
					if( ollr != null ) {
						((Linear)linearLocation).getLinearExtension().getOpenlrExtendedLinear().setOpenlrLineLocationReference( ollr );
						((Linear)linearLocation).getLinearExtension().getOpenlrExtendedLinear().setFirstDirection( null );
					}
				}
			
				_LocationContainedInItinerary  lcii = new _LocationContainedInItinerary();
				
				lcii.setLocation( linearLocation );
				lcii.setIndex( 0 );
				itinerary.getLocationContainedInItinerary().add( lcii );
				
				addedOpenLR = true;
			}
		}
		if( routeEncoding.contains( Properties.ROUTE_ENCODING_ALERTC ) )
		{
			alertCLocalizer.addAlertCLinearLocations( route.getLinearLocation(), linearLocation );
		}
		
		if( routeEncoding.contains( Properties.ROUTE_ENCODING_COORD ) || !addedOpenLR )
		{
			// Location as itinerary of coordinates
			
			List<WGS84Point> wgs84Points = locationBuilder.getCoordinates(route);
		
			int index = 1;
			for( WGS84Point wgs84Point : wgs84Points )
			{
				_LocationContainedInItinerary  lcii = new _LocationContainedInItinerary();
			
				Point point = new Point();
				
				PointByCoordinates pbc = new PointByCoordinates();
				PointCoordinates pc = new PointCoordinates();
	
				pc.setLatitude((float) wgs84Point.y() );
				pc.setLongitude((float) wgs84Point.x() );
	
				pbc.setPointCoordinates(pc);
				point.setPointByCoordinates( pbc );
				
				lcii.setLocation( point );
				lcii.setIndex( index );
				itinerary.getLocationContainedInItinerary().add( lcii );
				
				index++;
			}
		
		}
		
		return itinerary;
	}
	
	private List<WGS84Point> toCoordinates( LinearLocation linLoc ) throws Fault, SRPException {
		
		ConvertLocationsRequest request = new ConvertLocationsRequest();
		request.setTargetType( GeoLocation.class.getName() );
		request.getSource().add( linLoc );
		
		ConvertLocationsResponse response = wlsService.convertLocations( request );
		if( ( response != null ) && 
		    ( response.getLocations() != null ) &&
		    !response.getLocations().isEmpty() &&
		    ( response.getLocations().get(0) instanceof GeoLocation ) ) {
				GeoLocation geol = (GeoLocation)response.getLocations().get(0);
				
				return locationBuilder.getCoordinates( geol.getGeometry(), geol.getSrid() );
		} 
		
		return new ArrayList<>();
	}
	
	
	/**
	 * 
	 * Writes strategies to file
	 * 
	 * @param srfp			File name	
	 * @param strategies	Strategies
	 */
	public void writeStrategyRoutesToFile( String srfp, List<StrategyItem> strategies ) {    // NOSONAR
		
		for( StrategyItem si : strategies )
		{
			String suffix = "M";

		    SdbbyStrategy strategyLocation = strategyLocations.get( si.getId() );
			if( strategyLocation == null )
			{
				LOGGER.error( "No location for strategy <" + si.getId() + ">" );   // NOSONAR
				continue;
			}
			
			for( SdbbyRoute route : strategyLocation.getRoutes() )
			{
				LinearLocation linLoc = route.getLinearLocation();
				
				if( !route.isMainRoute() ) {
					suffix = "A";
				}
				
				String fileName = srfp + "/Strategy_" + si.getId() + "_" + suffix + ".txt";
				
				try {
					List<WGS84Point>  pts = toCoordinates( linLoc );  
							
					if( ( pts == null ) || pts.isEmpty() ) {
						LOGGER.error( "No location for strategy <" + si.getId() + ">, route '" + suffix + "'" );
						continue;
					}
					
					List<String> lines = new ArrayList<>();
					lines.add( si.getId() + ";" + suffix );
					for( WGS84Point pt : pts ) {
						lines.add( pt.y() + ";" +  pt.x() );
					}
				
					FileUtils.writeLines( new File( fileName ), lines);
				} catch ( IOException | Fault | SRPException ex ) {
					LOGGER.error( "Cannot convert route <" + si.getId() + " (" + suffix + ")" + "> to coordinates.");
					LOGGER.error( ex.toString() );
				}
			}
			
			
		}		
		
	}
	
	
	/**
	 * 
	 * Returns active strategies
	 * 
	 * @param publisher publisher
	 * @return strategyStates StrategyStates
	 * @throws SRPException Error
	 * @throws WlsException Error 
	 */
	public List<D2Strategy> getActiveStrategies( Publisher publisher, StrategyStates strategyStates )  // NOSONAR
			throws SRPException, WlsException
	{
		List<StrategyItem> strategies;
		if( strategyStates != null ) {
			strategies = new ArrayList<>();
			for( StrategyState strategyState : strategyStates.getStrategyStateList() ) {
				StrategyItem si = new StrategyItem( strategyState.getId(), strategyState.getActive() );
				strategies.add( si );
			}
		} else {
			StrategyRequest request = new StrategyRequest();
			StrategySet strategySet = sdbbyService.queryStrategies( request );	
			strategies = strategySet.getStrategySet();
		}
		
		if( !routeCoordinatesWritten ) {
			String srfp = strategyRoutesFilePath;
			if( ( srfp != null ) && !srfp.isEmpty() ) {
				File dir = new File( srfp );
				if( !dir.exists() ) {  
					if( !dir.mkdirs() ) {   // NOSONAR
						LOGGER.error( "Cannot create directory <" + srfp + "> to hold strategy route coordinate files. Terminate process!" );
						Util.sleep( 5000 );
						System.exit(-1);
					}
				}
				writeStrategyRoutesToFile( srfp, strategies );
			}
			
			routeCoordinatesWritten = true;
		}
		
		Set<String> sIds = new HashSet<>();
		List<D2Strategy> ss = new ArrayList<>();
		for( StrategyItem si : strategies )
		{
			if( !sIds.contains( si.getId() ) )
			{
				sIds.add( si.getId() );
				if( si.isActive() )
				{
					LOGGER.debug( "Active Strategy: " + si.getId() );
					
					D2Strategy d2Strategy = new D2Strategy();
					d2Strategy.setId( si.getId() );
					
					String sKey = StrategyConfig.getC2VBAStrategyIdMainPart( si.getId() );
					StrategicRouteManagement srm = strategicRouteManagements.get( sKey );
					
					SdbbyStrategy strategyLocation = strategyLocations.get( si.getId() );
					if( strategyLocation == null )
					{
						LOGGER.error( "No location for strategy <" + si.getId() + ">" );
						continue;
					}
					Location d2Location = publisher.getAreaLocation();
					setLocation( strategyLocation, d2Location );
					d2Strategy.setLocation( d2Location );

					if( srm != null ) {
						d2Strategy.setStrategicRouteManagement( srm );
					} else {
						Location d2tl = getTriggerLocation( strategyLocation.getTrigger() );
						d2Strategy.setTriggerLocation( d2tl );
						Location d2td = getTriggerLocation( strategyLocation.getTriggerDestination() );
						d2Strategy.setTriggerDestination( d2td );
						
						d2Strategy.setName( strategyLocation.getName() );
						for( SdbbyRoute route : strategyLocation.getRoutes() )
						{
							if( route.isMainRoute() ) {
								d2Strategy.setOriginalRoute( toItinerary( route, publisher ) );
								if( strategyLocation.getRoutes().size() > 1 ) {
									d2Strategy.setOriginalRouteName( "Hauptroute" );
								} else {
									d2Strategy.setOriginalRouteName( route.getName() );
								}
							} else {
								d2Strategy.setAlternativeRouteName( route.getName() );
								d2Strategy.setAlternativeRoute( toItinerary( route, publisher ) );
							}
						}
					}
					ss.add( d2Strategy );
				}
			}
		}		
		return ss;
	}
	
	private boolean isOpenLrProvidedStrategy( String id ) {
		return StrategyConfig.isC2VBAStrategy( id );
	}
	
	private void readOpenLrLocations() throws SRPException {
		
		LOGGER.info( "Reading OpenLr location files:" );
		
		JAXBUtil jaxbUtil;
		try {
			jaxbUtil = new JAXBUtil( Publisher.SCHEMA_FILE, Publisher.D2_PACKAGE, 
					                 "http://datex2.eu/schema/2/2_0", new HashMap<>(), true );
			for( String strategyId : strategyLocations.keySet() ) {
				if( isOpenLrProvidedStrategy( strategyId ) ) {
					
					String sid = StrategyConfig.getC2VBAStrategyIdMainPart( strategyId );
					
					String fileName = "openLr/" + sid + ".xml";
					LOGGER.info( "     " + fileName );
					InputStream is = getClass().getClassLoader().getResourceAsStream( fileName );	
					
					if( is == null ) {
						LOGGER.warn( "       file not found" );
					}
					
					String openLrXml = IOUtils.toString( is );
					StrategicRouteManagement srm = jaxbUtil.getObject( openLrXml );
					
					strategicRouteManagements.put( sid, srm );
				}
				
			}
		} catch (JAXBException | IOException | NullPointerException ex ) {
			LOGGER.error( ex.toString() );
			LOGGER.error( CallStack.getStackTraceAsString(ex) );
			throw new SRPException( SRPException.ERROR_IO, "Error reading OpenLR locations", ex );
		}
	
		if( ( wlsLocationDir != null ) && !wlsLocationDir.isBlank() ) {
			openLrLocationsToWlsLocations();
		}
	}

	
	

}
