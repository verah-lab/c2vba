package de.heuboe.datex2.shp.writer.shapefile;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import de.heuboe.data.DataStore;
import de.heuboe.data.DataWriter;
import de.heuboe.data.Factory;
import de.heuboe.data.Type;
import de.heuboe.data.Type.Code;
import de.heuboe.data.shp.ShpDataStore;
import de.heuboe.data.shp.ShpFactory;
import de.heuboe.datex2.base.D2CoordTrans;
import de.heuboe.datex2.measure.D2MeasureLoc;
import de.heuboe.datex2.shp.writer.shapefile.D2ShpRdsTmcSupply.RdsSegment;
import de.heuboe.datex2.shp.writer.shapefile.D2ShpRdsTmcSupply.RdsTmcPoint;
import de.heuboe.geo.Coordinate;
import de.heuboe.geo.Geometry;
import de.heuboe.geo.data.GeoData;
import de.heuboe.geo.utils.DefaultGeometryFactory;
import de.heuboe.geo.utils.GeometryUtils;
import de.heuboe.log.Logger;
import de.heuboe.util.CallStack;

public class D2RdsTmcShpWriter
{
	public enum Direction
	{
		POSITIVE,
		NEGATIVE
	}
	
	public static class D2ShpConfig
	{
		private String shapefilePath;
		private String locSrvAddress;

		public String getShapefilePath()
		{
			return shapefilePath;
		}
		public void setShapefilePath(String shapefilePath)
		{
			this.shapefilePath = shapefilePath;
		}
		public String getLocSrvAddress()
		{
			return locSrvAddress;
		}
		public void setLocSrvAddress(String locSrvAddress)
		{
			this.locSrvAddress = locSrvAddress;
		}
	}
	
	private static final Logger LOGGER = Logger.getLogger( D2RdsTmcShpWriter.class );
	private static final String[] POINT_MEMBER_NAMES = { "Date", 
												   "Version", 
												   "LocCode", 
												   "Type", 
												   "PosOut", 
												   "NegOut", 
												   "RoadName", 
												   "LocName", 
												   "LocName2", 
												   "DispName" };
	private static final Code[] POINT_MEMBER_TYPE_CODES = { Code.DATE, 
													  Code.STRING, 
													  Code.INT, 
													  Code.STRING, 
													  Code.STRING, 
													  Code.STRING, 
													  Code.STRING, 
													  Code.STRING, 
													  Code.STRING, 
													  Code.STRING };
	private static final Integer[] POINT_MEMBER_LENGTHS = { 	-1, 
														20, 
														-1, 
														20, 
														1, 
														1, 
														30, 
														60, 
														60, 
														120 };
	
	private static final String[] LINE_MEMBER_NAMES = { "Created",
														"Updated",	
													    "Version", 
													    "PrimaryLoc", 
													    "SecondaryLoc", 
													    "LocId", 
													    "Name", 
													    "FreeTravelTime",
													    "LossTime",
													    "State",
													    "DispName" };
	
	private static final Code[] LINE_MEMBER_TYPE_CODES = { Code.DATE, 
														   Code.STRING,
														   Code.STRING, 
														   Code.INT, 
														   Code.INT, 
														   Code.INT, 
														   Code.STRING, 
														   Code.INT, 
														   Code.INT, 
														   Code.INT, 
														   Code.STRING };
	
	private static final Integer[] LINE_MEMBER_LENGTHS = { 	-1, 
															20, 
															20, 
															-1, 
															-1, 
															-1, 
															120, 
															-1, 
															-1, 
															-1, 
															120 };
	
	
	private static final String GEOMETRY_POINT_TYPE = "POINT";
	private static final String GEOMETRY_LINE_TYPE = "POLYLINE";
	
	private final static Properties POINT_PROPS = new Properties();
	private final static Properties LINE_PROPS = new Properties();
	
	private D2CoordTrans ct;
	private LocationManager lm = null;
	
	static
	{
		new ShpFactory();
		
		{
			POINT_PROPS.put(ShpDataStore.GEOTYPE_KEY, GEOMETRY_POINT_TYPE);
			POINT_PROPS.put(ShpDataStore.CREATE_SPATIAL_INDEX_KEY, "false");
			POINT_PROPS.put(ShpDataStore.SRID_KEY, "4326" );
		}
		
		{
			LINE_PROPS.put(ShpDataStore.GEOTYPE_KEY, GEOMETRY_LINE_TYPE);
			LINE_PROPS.put(ShpDataStore.CREATE_SPATIAL_INDEX_KEY, "false");
			LINE_PROPS.put(ShpDataStore.SRID_KEY, "4326" );
		}
	}
	
	public D2RdsTmcShpWriter()
	{
		ct = new D2CoordTrans();
		
	}
	
	private boolean dirMatch( Direction dir, char alcDir )
	{
		return ( ( dir == Direction.POSITIVE ) && ( alcDir == 'P' ) ) ||
			   ( ( dir == Direction.NEGATIVE ) && ( alcDir == 'N' ) );
	}
	
	private void setValues( int locCode, 
							double x, double y,
							RdsTmcPoint pt, GeoData data)
	{
		data.getMember("Date").setFromDate( new Date() );

		data.getMember("Version").setFromString( pt.getVersion() );
		data.getMember("LocCode").setFromInt( locCode );
		data.getMember("Type").setFromString( pt.getType() );
		data.getMember("PosOut").setFromString( pt.getPosOut() );
		data.getMember("NegOut").setFromString( pt.getNegOut() );
		data.getMember("RoadName").setFromString( pt.getRoadName() );
		data.getMember("LocName").setFromString( pt.getLocName() );
		data.getMember("LocName2").setFromString( pt.getLocName2() );
		data.getMember("Intersection").setFromString( pt.getLocName() );
		data.getMember("DispName").setFromString( pt.getDisplayName() );
		
		data.setGeometry( createGeometry( x, y ) );
	}
	
	private void setValues( RdsSegment rdsSegment, 
							D2MeasureLoc loc,
						    List<Coordinate> coors,
							GeoData data)
	{
		data.getMember("Created").setFromDate( new Date() );
		
		data.getMember("Version").setFromString( rdsSegment.getVersion() );
		data.getMember("PrimaryLoc").setFromInt( rdsSegment.getPrimLoc() );
		data.getMember("SecondaryLoc").setFromInt( rdsSegment.getSecLoc() );
		data.getMember("LocId").setFromInt( rdsSegment.getLocId() );
		data.getMember("Name").setFromString( loc.getLocName()  );
		data.getMember("DispName").setFromString( loc.getLocName() );
		
		data.setGeometry( createGeometry( coors ) );
	}
	
	
	private void writeSegmentShapeFile( D2ShpConfig config,
									    Map<Integer,RdsSegment> rdsSegments,
									    Collection<D2MeasureLoc> locations,
									    String shapefile )
			throws Exception						    
	{
		Set<Integer> locIds = new HashSet<Integer>();
		for( D2MeasureLoc location : locations )
		{
			locIds.add( location.getGeodynId() );
		}
		Map< Integer,List<Coordinate> > loc2Coordinates;
		loc2Coordinates = lm.localizeAll( locIds );
		
		Type type = createLineType( shapefile );
		DataStore store = new ShpDataStore( type, shapefile, LINE_PROPS ); 
		
		DataWriter writer = null;
		try 
		{
			writer = store.getWriter();
			for( D2MeasureLoc location : locations )
			{
				int locId = location.getGeodynId();
				
				List<Coordinate> coors = loc2Coordinates.get( locId );
				
				RdsSegment rdsSegment = rdsSegments.get( locId );
				if( rdsSegment != null )
				{
					GeoData data = (GeoData)type.createData();
					
					setValues( rdsSegment,
							   location,
							   coors, data );
					
					writer.add(data);
				}
			}
		} 
		finally 
		{
			if ( writer != null ) 
			{
				writer.close();
			}
		}
	}
	
	
	private void writeDirPointShapeFile( D2ShpConfig config, 
										 Map<Integer,RdsTmcPoint> rdsTmcPoints,
			   							 Collection<D2MeasureLoc> locations, 
			   							 Direction dir,
			   							 String shapefile )
	{
		Type type = createPointType( shapefile );
		DataStore store = new ShpDataStore( type, shapefile, POINT_PROPS ); 
		
		DataWriter writer = null;
		try 
		{
			writer = store.getWriter();
			Set<Integer> writtenLocs = new HashSet<Integer>();
			for( D2MeasureLoc loc : locations )
			{
				String rdsTblVer = loc.getRdsLocTblVer();
				char alcDir = loc.getRdsDirection();
				if(  ( rdsTblVer != null ) && 
					!rdsTblVer.isEmpty() &&
					dirMatch( dir, alcDir ) 
				  )
				{
					int primLoc = loc.getRdsLocCodePL();
					
					if( ( 
							primLoc != 0 ) && 
							( primLoc != -1 ) && 
							!writtenLocs.contains( primLoc )
						)
					{
						RdsTmcPoint pt = rdsTmcPoints.get( primLoc );
						if( pt != null )
						{
							
							GeoData data = (GeoData)type.createData();
							
							setValues( primLoc, 
									   loc.getEndCoordX(),
									   loc.getEndCoordY(),
									   pt, data );
							
							writer.add(data);
							writtenLocs.add( primLoc );
						}
					}
					
					int secLoc = loc.getRdsLocCodeSL();
					
					if( ( 
							secLoc != 0 ) && 
							( secLoc != -1 ) && 
							!writtenLocs.contains( secLoc )
						)
					{
						RdsTmcPoint pt = rdsTmcPoints.get( secLoc );
						if( pt != null )
						{
							GeoData data = (GeoData)type.createData();
							
							setValues( secLoc, 
									   loc.getStartCoordX(),
									   loc.getStartCoordY(),
									   pt, data );
							
							writer.add(data);
							writtenLocs.add( secLoc );
						}
					}
				}
			}
		} 
		finally 
		{
			if ( writer != null ) 
			{
				writer.close();
			}
		}
	}
	
	private String getRdsTmcVersion( Collection<D2MeasureLoc> locations ) 
	{
		for( D2MeasureLoc loc : locations )
		{
			String rdsTblRef = loc.getRdsLocTblRef();
			String rdsTblVer = loc.getRdsLocTblVer();
			
			if(  ( rdsTblRef != null ) && ( rdsTblVer != null ) )
			{
				return rdsTblRef + ":" + rdsTblVer;
			}
			
		}
		
		return null;
	}
	
	private void initLocationManager( D2ShpConfig config )
	{
		String locSrvAddress = config.getLocSrvAddress();
		if( ( locSrvAddress != null ) && !locSrvAddress.isEmpty() )
		{
			try
			{
				lm = new LocationManager();
				lm.connect( locSrvAddress );
			}
			catch( Exception ex )
			{
				LOGGER.error( "Failed to create shapefiles !");
				LOGGER.error( ex.toString() );
				LOGGER.error( CallStack.getStackTraceAsString( ex ) );
			}
		}
	}	
	
	public void write( D2ShpConfig config, 
					   Collection<D2MeasureLoc> locations )
	{
		try
		{
			initLocationManager( config );			
			
			D2ShpRdsTmcSupply rdsTmcSupply = new D2ShpRdsTmcSupply();
			String rdsTmcVersion = getRdsTmcVersion( locations );
			
			if( rdsTmcVersion == null )
			{
				LOGGER.info( "Locations have no RDS/TMC attributes !" );
				LOGGER.info( "No shapefiles generated !" );
				
				return;
			}
			
			Map<Integer,RdsTmcPoint> rdsTmcPoints = rdsTmcSupply.getRdsTmcPoints( rdsTmcVersion ); 
			Map<Integer,RdsSegment> rdsSegments = rdsTmcSupply.getRdsSegments( rdsTmcVersion );
			
			String filePointBaseName = config.getShapefilePath() + File.separator + "tmcPoints";
			
			writeDirPointShapeFile( config, 
									rdsTmcPoints,
									locations, Direction.POSITIVE,
									filePointBaseName + "Pos.shp" );
			writeDirPointShapeFile( config,  
									rdsTmcPoints,
									locations,
									Direction.NEGATIVE,
									filePointBaseName + "Neg.shp" );
			
			if( lm != null )
			{
				writeSegmentShapeFile( config,
									   rdsSegments,
									   locations,
									   config.getShapefilePath() + File.separator + "rdsSegments.shp" );
			}
		}
		catch( Exception ex )
		{
			LOGGER.error( "Failed to write shapefiles !" );
			LOGGER.error( ex.toString() );
			LOGGER.error( CallStack.getStackTraceAsString( ex ) );
		}
	}
	
	private Geometry createGeometry( double x, double y ) 
	{
		double[] coords = ct.getWGS84CoordinatesFromUtm( x, y );
		Coordinate coordinate = GeometryUtils.create2DCoordinate(coords[0],coords[1]);
		return DefaultGeometryFactory.newPoint(coordinate, 4326);
	}
	
	private Geometry createGeometry( List<Coordinate> coors ) 
	{
		return DefaultGeometryFactory.newPolyline( coors, 4326);
	}
	
	
	private Type createPointType(String typeName) 
	{
		Factory factory = Factory.Singleton.getInstance();
		Map<String,Type> members = new LinkedHashMap<>();
		for ( int i = 0; i < Math.min(POINT_MEMBER_NAMES.length,POINT_MEMBER_TYPE_CODES.length); ++i ) 
		{
			Type type;
			if ( POINT_MEMBER_TYPE_CODES[i] == Code.STRING ) 
			{
				type = factory.getStringType(POINT_MEMBER_LENGTHS[i]);
			} 
			else 
			{
				type = factory.getType(POINT_MEMBER_TYPE_CODES[i]);
			}
			members.put(POINT_MEMBER_NAMES[i], type);
		}
		return factory.getType(typeName, members, "", "");
	}

	private Type createLineType(String typeName) 
	{
		Factory factory = Factory.Singleton.getInstance();
		Map<String,Type> members = new LinkedHashMap<>();
		for ( int i = 0; i < Math.min(LINE_MEMBER_NAMES.length,LINE_MEMBER_TYPE_CODES.length); ++i ) 
		{
			Type type;
			if ( LINE_MEMBER_TYPE_CODES[i] == Code.STRING ) 
			{
				type = factory.getStringType(LINE_MEMBER_LENGTHS[i]);
			} 
			else 
			{
				type = factory.getType(LINE_MEMBER_TYPE_CODES[i]);
			}
			members.put(LINE_MEMBER_NAMES[i], type);
		}
		return factory.getType(typeName, members, "", "");
	}
	
}
