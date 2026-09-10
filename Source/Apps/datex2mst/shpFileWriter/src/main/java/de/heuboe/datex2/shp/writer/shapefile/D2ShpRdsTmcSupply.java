package de.heuboe.datex2.shp.writer.shapefile;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.heuboe.datex2.base.D2Exception;
import de.heuboe.ddp.Connection;
import de.heuboe.ddp.Container;
import de.heuboe.ddp.DDPException;
import de.heuboe.ddp.DataFilter;
import de.heuboe.ddp.DatakindFilter;
import de.heuboe.ddp.Dataset;
import de.heuboe.log.Logger;

public class D2ShpRdsTmcSupply
{
	public static class RdsSegment
	{
		private String version;
		private int primLoc;
		private int secLoc;
		private int locId;
		private String name;
		
		public String getVersion()
		{
			return version;
		}
		public void setVersion(String version)
		{
			this.version = version;
		}
		public int getPrimLoc()
		{
			return primLoc;
		}
		public void setPrimLoc(int primLoc)
		{
			this.primLoc = primLoc;
		}
		public int getSecLoc()
		{
			return secLoc;
		}
		public void setSecLoc(int secLoc)
		{
			this.secLoc = secLoc;
		}
		public int getLocId()
		{
			return locId;
		}
		public void setLocId(int locId)
		{
			this.locId = locId;
		}
		public String getName()
		{
			return name;
		}
		public void setName(String name)
		{
			this.name = name;
		}
	}
	
	public static class RdsTmcPoint
	{
		private String version;
		private int locCode;
		private String type;
		private String posOut;
		private String negOut;
		private String locName;
		private String locName2;
		private String roadName;
		private String displayName = null;

		public String getVersion()
		{
			return version;
		}
		public void setVersion(String version)
		{
			this.version = version;
		}
		public String getRoadName()
		{
			return roadName;
		}
		public void setRoadName(String roadName)
		{
			this.roadName = roadName;
		}
		public String getDisplayName()
		{
			if( displayName != null )
				return displayName;
			else
				return roadName + ": " + locName + 
					   " (" + locCode+ ", " + type  + ")";
		}
		public void setDisplayName(String displayName)
		{
			this.displayName = displayName;
		}

		public int getLocCode()
		{
			return locCode;
		}
		public void setLocCode(int locCode)
		{
			this.locCode = locCode;
		}
		public String getLocName()
		{
			return locName;
		}
		public void setLocName(String locName)
		{
			this.locName = locName;
		}
		
		public String getType()
		{
			return type;
		}
		public void setType(String type)
		{
			this.type = type;
		}
		public String getPosOut()
		{
			return posOut;
		}
		public void setPosOut(String posOut)
		{
			this.posOut = posOut;
		}
		public String getNegOut()
		{
			return negOut;
		}
		public void setNegOut(String negOut)
		{
			this.negOut = negOut;
		}
		public String getLocName2()
		{
			return locName2;
		}
		public void setLocName2(String locName2)
		{
			this.locName2 = locName2;
		}
	}
	
	private static Logger LOGGER = Logger.getLogger( D2ShpRdsTmcSupply.class );
	private static final String DK_TR_RDS_DEFINITION = "tr_rds_definition";
	private static final String DK_TR_RDS_SEGMENT_LOOKUP = "tr_rds_segment_lookup";
	private static final String COL_RDS_CODE = "rds_code";
	private static final String COL_FIRST_NAME = "first_name";
	private static final String COL_ROAD_NAME = "route_desc";
	private static final String COL_VERSION = "dbid";
	private static final String COL_RDS_CAT = "rds_cat";
	private static final String COL_RDS_TYPE_CODE = "rds_type_code";
	private static final String COL_RDS_SUB_TYPE_CODE = "rds_sub_type_code";
	private static final String COL_SECOND_NAME = "second_name";
	private static final String COL_OUT_POS = "out_pos";
	private static final String COL_OUT_NEG = "out_neg";
	private static final String COL_ID = "id";
	private static final String COL_PRIMARY_LOC = "primary_loc";
	private static final String COL_SECONDARY_LOC = "secondary_loc";
	
	private static Connection connection = null;
	
	public D2ShpRdsTmcSupply()
	{
	}
	
	public static void setConnection( Connection conn )
	{
		connection = conn;
	}
	
	public Map<Integer,RdsSegment> getRdsSegments( String version )
			throws D2Exception
	{
		Map<Integer,RdsSegment> rdsSegments = new HashMap<Integer,RdsSegment>();
		
		DatakindFilter dkFilter = new DatakindFilter( DK_TR_RDS_SEGMENT_LOOKUP );
		dkFilter.setCondition( "dbid = '" + version + "'" );

		try
		{
	        Container[] results = connection.read( new DataFilter(dkFilter) ); 
	        
	        if( results != null )
	        {
	        	for( Container c : results )
	        	{
			        Iterator<Dataset> iterator = c.iterator();
			        while (iterator.hasNext())
			        {
			            Dataset ds = iterator.next();
			            
			            RdsSegment rdsSegment = new RdsSegment();
			            int locId = ds.value( COL_ID ).getInt();
			            rdsSegment.setLocId( locId );
			            rdsSegment.setVersion( ds.value( COL_VERSION ).getString() );
			            rdsSegment.setPrimLoc( ds.value( COL_PRIMARY_LOC ).getInt() );
			            rdsSegment.setSecLoc( ds.value( COL_SECONDARY_LOC ).getInt() );
			        			            
			            rdsSegments.put( locId, rdsSegment );
			        }
	        	}
	        }
		}
		catch( DDPException ex )
		{
			String error = "Fehler beim Lesen der Datenart <" + DK_TR_RDS_SEGMENT_LOOKUP + ">";
			
			LOGGER.error( error );
			LOGGER.error( ex.toString() );
			
			throw new D2Exception( D2Exception.D2_ERR_GENERAL_DDP_EXCEPTION,
									error, ex );
		}
		
		return rdsSegments;
	}	
	
	public Map<Integer,RdsTmcPoint> getRdsTmcPoints( String version )
			throws D2Exception
	{
		Map<Integer,RdsTmcPoint> rdsTmcPoints = new HashMap<Integer,RdsTmcPoint>();
		
		DatakindFilter dkFilter = new DatakindFilter( DK_TR_RDS_DEFINITION );
		dkFilter.setCondition( "dbid = '" + version + "'" );

		try
		{
	        Container[] results = connection.read( new DataFilter(dkFilter) ); 
	        
	        if( results != null )
	        {
	        	for( Container c : results )
	        	{
			        Iterator<Dataset> iterator = c.iterator();
			        while (iterator.hasNext())
			        {
			            Dataset ds = iterator.next();
			            
			            RdsTmcPoint rdsTmcPoint = new RdsTmcPoint();
			            rdsTmcPoint.setLocCode( ds.value( COL_RDS_CODE ).getInt() );
			            rdsTmcPoint.setLocName( ds.value( COL_FIRST_NAME ).getString() );
			            
			            rdsTmcPoint.setRoadName( ds.value( COL_ROAD_NAME ).getString() );
			            rdsTmcPoint.setVersion( ds.value( COL_VERSION ).getString() );
			            		
			            String type = ds.value( COL_RDS_CAT ).getString() + 
			            			  ds.value( COL_RDS_TYPE_CODE ).getString() + "." + 
			            			  ds.value( COL_RDS_SUB_TYPE_CODE ).getString();
			            
			            rdsTmcPoint.setType( type );
			            rdsTmcPoint.setLocName2( ds.value( COL_SECOND_NAME ).getString() );
			  
			            String posOut = ds.value( COL_OUT_POS ).getBoolean() ? "+" : "-";
			            rdsTmcPoint.setPosOut( posOut );
			            
			            String negOut = ds.value( COL_OUT_NEG ).getBoolean() ? "+" : "-";
			            rdsTmcPoint.setNegOut( negOut );

			            rdsTmcPoints.put( rdsTmcPoint.getLocCode(), rdsTmcPoint );
			        }
	        	}
	        }
	        
	        return rdsTmcPoints;
		}
		catch( DDPException ex )
		{
			String error = "Fehler beim Lesen der Datenart <" + DK_TR_RDS_DEFINITION + ">";
			
			LOGGER.error( error );
			LOGGER.error( ex.toString() );
			
			throw new D2Exception( D2Exception.D2_ERR_GENERAL_DDP_EXCEPTION,
									error, ex );
		}
	}

}
