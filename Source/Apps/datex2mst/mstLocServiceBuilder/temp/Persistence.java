package de.heuboe.datex2.location.service.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import de.heuboe.datex2.base.D2Coordinate;
import de.heuboe.datex2.measure.D2MeasureDataSource;
import de.heuboe.datex2.measure.D2MeasureItem;
import de.heuboe.datex2.measure.D2MeasureLoc;
import de.heuboe.ddp.Connection;
import de.heuboe.ddp.Container;
import de.heuboe.ddp.DDPException;
import de.heuboe.ddp.DataFilter;
import de.heuboe.ddp.DatakindFilter;
import de.heuboe.ddp.Dataset;
import de.heuboe.ddp.Modification;
import de.heuboe.log.Logger;
import de.heuboe.mst.config.MappingConfig;
import de.heuboe.mst.config.lib.MstContent;
import de.heuboe.mst.definition.MstDefinition;

public class Persistence
{
	private static final Logger LOGGER = Logger.getLogger( Persistence.class );
	private Connection connection = null;
	private static Persistence instance = null;
	
	private static final String DK_MST 				= "datex2MST";
	private static final String DK_MST_SRC 			= "datex2MSTDataSource";
	private static final String DK_MST_SRC_EM 		= "datex2MSTDataSourceEnumMapping";
	private static final String DK_MST_SRC_FILTER 	= "datex2MSTDataSourceFilter";
	private static final String DK_MST_LOC 			= "datex2MSTLocation";
	private static final String DK_MST_COOR 		= "datex2MSTCoordinate";
	private static final String DK_MST_ITEM 		= "datex2MSTItem";
	private static final String DK_MST_CFG 			= "datex2MSTConfig";
	private static final String DK_MST_DEF 			= "datex2MSTDefinition";
	private static final String DK_MST_DEF_DETAIL   = "datex2MSTDefinitionDetail";
	
	private static final String COL_TIME 				= "time";
	private static final String COL_DEF_PART 			= "defPart";
	private static final String COL_CFG_PART			= "defPart";
	private static final String COL_MST					= "mst";
	private static final String COL_VERSION				= "version";
	private static final String COL_ID					= "id";
	
	private static final String COL_MST_DESCRIPTION		= "description";

	private static final String COL_MST_D2VERSION		= "d2Version";
	private static final String COL_MST_DEFVERSION		= "defVersion";

	private static final String COL_MST_ACTIVE			= "active";
	
	
	
	private static final String COL_SRC_DATAKIND				= "datakind";
	private static final String COL_SRC_VALUECOL     			= "valueCol";  
	private static final String COL_SRC_VALUECOLCHARACTERISTIC  = "valueColCharacteristic";   

	private static final String COL_SRC_IDCOLUMN      			= "idColumn";
	private static final String COL_SRC_VALIDCOLUMN     		= "validColumn";
	private static final String COL_SRC_INVALIDVALUES     		= "invalidValues";

	private static final String COL_SRC_D2VEHTYPE				= "d2VehType"; 
	private static final String COL_SRC_D2DATATYPE				= "d2DataType";
	private static final String COL_SRC_D2BASICDATATYPE    		= "d2BasicDataType";
	private static final String COL_SRC_D2VALUEELEMENT     		= "d2ValueElement";
	private static final String COL_SRC_D2VALUE     			= "d2Value";
	private static final String COL_SRC_D2VALUEINNER      		= "d2ValueInner";
	private static final String COL_SRC_D2VALUEPATH     		= "d2ValuePath";

	private static final String COL_SRC_MAPPINGTYPE         	= "mappingType";
	private static final String COL_SRC_FACTOR					= "factor";
	private static final String COL_SRC_VALUEDATATYPE			= "valueDataType";

	private static final String COL_SRC_VALIDINTERVAL     		= "validInterval";   
	
	private static final String COL_SRC_EM_VALUE     			= "value";   
	private static final String COL_SRC_EM_NAME     			= "name";   
	
	private static final String COL_SRC_FILTER_COL     			= "filterCol";   
	private static final String COL_SRC_FILTER_VALUES   			= "filterValues";   
	private static final String COL_SRC_FILTER_PERIOD 			= "period";   
	
	private static final String COL_LOC_D2ID	   			= "d2Id";		
	private static final String COL_LOC_LOCNAME	   			= "locName";		
	private static final String COL_LOC_EQUIPMENT	   		= "equipment";

	private static final String COL_LOC_RDSLOCTBLREF 		= "rdsLocTblRef";
	private static final String COL_LOC_RDSLOCTBLVER 		= "rdsLocTblVer";

	private static final String COL_LOC_RDSPRIMLOCCODE     	= "rdsPrimLocCode";
	private static final String COL_LOC_RDSSECLOCCODE       = "rdsSecLocCode";

	private static final String COL_LOC_RDSDIRECTION   		= "rdsDirection";
	private static final String COL_LOC_RDSPRIMLOCDIST    	= "rdsPrimLocDist";
	private static final String COL_LOC_RDSSECLOCDIST   	= "rdsSecLocDist";

	private static final String COL_LOC_STARTCOORDX         = "startCoordX";   
	private static final String COL_LOC_STARTCOORDY         = "startCoordY";   

	private static final String COL_LOC_ENDCOORDX           = "endCoordX";   
	private static final String COL_LOC_ENDCOORDY           = "endCoordY";   
	private static final String COL_LOC_BEARING 			= "bearing"; 
	private static final String COL_LOC_GEODYNID			= "geoDynId";
	
	private static final String COL_LOC_D2_EXT_REF_SYSTEM   = "d2ExtRefSystem";
	private static final String COL_LOC_D2_EXT_LOC_CODE		= "d2ExtLocCode";
	
	private static final String COL_COOR_INDEX				= "index";
	private static final String COL_COOR_X					= "x";
	private static final String COL_COOR_Y					= "y";
	

	
	private static final String COL_ITEM_INDEX			= "index";
	
	private static final String COL_ITEM_NAME 			= "name";
	private static final String COL_ITEM_CARRIAGEWAY	= "carriageway";
	private static final String COL_ITEM_LANE			= "lane";
	private static final String COL_ITEM_PERIOD         = "period";
	   	
	private static final String COL_ITEM_DKREF         	= "dkRef";		
	private static final String COL_ITEM_DKID          	= "dkId";
	private static final String COL_ITEM_DKKEY          = "dkKey";
	private static final String COL_ITEM_LOCID		   	= "locId";
	
	
	private Persistence( Connection connection )
			throws LSBException
	{
		try
		{
			this.connection = connection;
			connection.setAutoCommit( false );
		}
		catch( DDPException ex )
		{
			LOGGER.error( "Error initialising class 'Persistence'" );
			throw new LSBException( LSBException.ERROR_DB_READ, ex.toString(), ex );
		}
	}
	
	
	private <T> T getContent( String content,
							  String schemaFile,
							  String packName,
							  Class<T> clasz )
			throws LSBException
	{
		try
		{
			LOGGER.debug( "Start of  getContent()" );
			
			JAXBContext jc;

			InputStream is = IOUtils.toInputStream( content );			
			try 
			{
				jc = JAXBContext.newInstance( packName );
				Unmarshaller um = jc.createUnmarshaller();
				
	       	 	URL schemaURL = getClass().getClassLoader().getResource( schemaFile );
		        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		        Schema schema = sf.newSchema( schemaURL );
				um.setSchema( schema );
		
				@SuppressWarnings("unchecked")
				JAXBElement<T> ifs = (JAXBElement<T>)um.unmarshal(is); 
				return ifs.getValue();
			}
			catch (JAXBException | SAXException ex ) 
			{
				try
				{
					if( is != null )
						is.close();
				}
				catch( IOException iex ) {}
				
				throw new LSBException( LSBException.ERROR_XML, 
										ex.toString(),
									    ex );
			}
		}
		finally
		{
			LOGGER.debug( "End of  getContent()" );
		}
	}
	
	
	/**
	 * 
	 * Creates single instance of this class
	 * 
	 * @param connection database connection 
	 * @throws NDWExportExceptionFatal NDW export exception
	 */
	public static void createInstance( Connection connection )
			throws LSBException
	{
		if( instance == null )
		{
			instance = new Persistence( connection );
		}
	}
	
	/**
	 * 
	 * Returns single instance of this class
	 * 
	 * @return single instance of this class
	 */
	public static Persistence instance()
	{
		return instance;
	}
	
	public synchronized MappingConfig getMSTConfig( String d2Version )
			throws LSBException
	{
		try
		{
			DatakindFilter dkFilter = new DatakindFilter( DK_MST_CFG );

			dkFilter.setCondition( "d2Version = '" + d2Version + "'" );
			dkFilter.setOrder( "count" );
	
	        Container[] results = connection.read( new DataFilter(dkFilter) ); 
	
	        String def = "";
	        Iterator<Dataset> iterator = results[0].iterator();
	        while (iterator.hasNext())
	        {
	            Dataset ds = iterator.next();
	            def += ds.value( COL_CFG_PART ).getString();
	        }
	        
	        StringBuffer xmlStr = new StringBuffer( def );
	        
	        return getContent( xmlStr.toString(), 
	        				   "schema/mstConfig.xsd", 
	        				   "de.heuboe.mst.config",
	        				   MappingConfig.class );
	        
	        // return javax.xml.bind.JAXB.unmarshal( new StreamSource( new StringReader( xmlStr.toString() ) ), MappingConfig.class );
		}
		catch( DDPException ex )
		{
			LOGGER.error( "Error reading '" + DK_MST_CFG + "'" );
			throw new LSBException( LSBException.ERROR_DB_READ, ex.toString(), ex );
		}
	}

	public synchronized MstDefinition getMSTDefinition( String d2Version, 
										   				String mst, 
										   				String defVersion )
			throws LSBException
	{
		try
		{
			DatakindFilter dkFilter = new DatakindFilter( DK_MST_DEF_DETAIL );

			//String d2VersionC = "(d2Version = '" + d2Version + "')";
			String mstC = "(mst = '" + mst + "')";
			String versionC = "(defVersion = '" + defVersion + "')";
			
			dkFilter.setCondition(  /*d2VersionC + " and " +*/ mstC + " and " + versionC );
			dkFilter.setOrder( "count" );
	
	        Container[] results = connection.read( new DataFilter(dkFilter) ); 
	
	        String def = "";
	        Iterator<Dataset> iterator = results[0].iterator();
	        while (iterator.hasNext())
	        {
	            Dataset ds = iterator.next();
	            def += ds.value( COL_DEF_PART ).getString();
	        }
	        
	        StringBuffer xmlStr = new StringBuffer( def );
	        
	        return getContent( xmlStr.toString(), 
	        				   "schema/mstDefinition.xsd", 
	        				   "de.heuboe.mst.definition",
	        				   MstDefinition.class );
	        
	        // return javax.xml.bind.JAXB.unmarshal( new StreamSource( new StringReader( xmlStr.toString() ) ), MstDefinition.class );
		}
		catch( DDPException ex )
		{
			LOGGER.error( "Error reading '" + DK_MST_DEF + "'" );
			throw new LSBException( LSBException.ERROR_DB_READ, ex.toString(), ex );
		}
	}
	
	public synchronized void saveMst( MstContent def )
			throws DDPException
	{
		LOGGER.info( "");
		LOGGER.info( "Start saveMst() ...");
		
		Date now = new Date();
		
		Modification mod = new Modification();
		
		// erase existing records
		String condition = "mst = '" + def.getMst() + "'";
		condition += " and (version = '" +  def.getVersion() + "')";
		mod.erase( DK_MST_SRC, condition );
		mod.erase( DK_MST_LOC, condition );
		mod.erase( DK_MST_COOR, condition );
		mod.erase( DK_MST_ITEM, condition );
		
		Container dataSourcesC = new Container();
		Container dataSourcesEMC = new Container();
		Container dataSourcesFilter = new Container();
		Container locationsC = new Container();
		Container coorC = new Container();
		Container itemsC = new Container();
		Container mstC = new Container();

		List<D2MeasureDataSource>  dataSources = def.getSources();
		for( D2MeasureDataSource dataSource : dataSources )
		{
			Dataset ds = new de.heuboe.ddp.Dataset( connection.getDatakind( DK_MST_SRC ) );
			ds.value( COL_TIME ).setDate( now );
			ds.value( COL_ID ).setInt( dataSource.getId() );
			ds.value( COL_MST ).setString( def.getMst() );
			ds.value( COL_VERSION ).setString( def.getVersion() );
			
			ds.value( COL_SRC_DATAKIND ).setString( dataSource.getDbDatakind() );
			ds.value( COL_SRC_VALUECOL ).setString( dataSource.getDbValueCol() );
			ds.value( COL_SRC_VALUECOLCHARACTERISTIC ).setString( dataSource.getValueColCharacteristic() ); 
			ds.value( COL_SRC_IDCOLUMN ).setString( dataSource.getDbIdCol() );
			ds.value( COL_SRC_VALIDCOLUMN ).setString( dataSource.getDbValidCol() );
			ds.value( COL_SRC_INVALIDVALUES ).setString( dataSource.getDbInvalidValsRaw() );
			ds.value( COL_SRC_D2VEHTYPE ).setString( dataSource.getVehClass().toString() );

			ds.value( COL_SRC_D2DATATYPE ).setString( dataSource.getDatakind().toString() );
			ds.value( COL_SRC_D2BASICDATATYPE ).setString( dataSource.getD2BasicDataType() );
			ds.value( COL_SRC_D2VALUEELEMENT ).setString( dataSource.getD2ValueElement() );
			ds.value( COL_SRC_D2VALUE ).setString( dataSource.getD2Value() );
			ds.value( COL_SRC_D2VALUEINNER ).setString( dataSource.getD2ValueInner() );

			ds.value( COL_SRC_D2VALUEPATH ).setString( dataSource.getD2ValuePath() );
			ds.value( COL_SRC_MAPPINGTYPE ).setString( dataSource.getMappingType() );
			ds.value( COL_SRC_FACTOR ).setDouble( dataSource.getFactor() );
			ds.value( COL_SRC_VALUEDATATYPE ).setString( dataSource.getValueDataType() );
			ds.value( COL_SRC_VALIDINTERVAL ).setInt( dataSource.getValidInterval() );
				
			dataSourcesC.add( ds );
			
			Map<Integer,String> values = dataSource.getEnumValues();
			if( values != null )
			{
				Iterator<Map.Entry< Integer, String > > it = values.entrySet().iterator();
			    while( it.hasNext() ) 
			    {
			    	Map.Entry< Integer, String > e = it.next();
			    	Integer value = e.getKey();
			    	String name = e.getValue();
				
					Dataset eds = new de.heuboe.ddp.Dataset( connection.getDatakind( DK_MST_SRC_EM ) );
					eds.value( COL_TIME ).setDate( now );
					eds.value( COL_ID ).setInt( dataSource.getId() );
					eds.value( COL_MST ).setString( def.getMst() );
					eds.value( COL_VERSION ).setString( def.getVersion() );
					
					eds.value( COL_SRC_EM_VALUE ).setInt( value );
					eds.value( COL_SRC_EM_NAME ).setString( name );
					
					dataSourcesEMC.add( eds );
			    }
			}
			
			String filterCol = dataSource.getDbFilterCol();
			if( 
					( ( filterCol != null ) && !filterCol.isEmpty() )
					||
					( dataSource.getPeriod() >= 0 )
			  )
			{
				Dataset eds = new de.heuboe.ddp.Dataset( connection.getDatakind( DK_MST_SRC_FILTER ) );
				eds.value( COL_TIME ).setDate( now );
				eds.value( COL_ID ).setInt( dataSource.getId() );
				eds.value( COL_MST ).setString( def.getMst() );
				eds.value( COL_VERSION ).setString( def.getVersion() );
				
				eds.value( COL_SRC_FILTER_COL ).setString( filterCol );
				eds.value( COL_SRC_FILTER_PERIOD ).setInt( dataSource.getPeriod() );

				String fvs = "";
				List<String> filterValues = dataSource.getDbFilterVals();
				if( filterValues != null )
				{
					for( String filterValue : filterValues )
					{
						if( !fvs.isEmpty() )
							fvs += ",";
						fvs += filterValue;
					}
				}
				eds.value( COL_SRC_FILTER_VALUES ).setString( fvs );
				
				dataSourcesFilter.add( eds );
			}
		}
		
		List<D2MeasureLoc>  locations = def.getLocations();
		for( D2MeasureLoc location : locations )
		{
			Dataset ds = new de.heuboe.ddp.Dataset( connection.getDatakind( DK_MST_LOC ) );
			ds.value( COL_TIME ).setDate( now );
			ds.value( COL_ID ).setInt( location.getId() );
			ds.value( COL_MST ).setString( def.getMst() );
			ds.value( COL_VERSION ).setString( def.getVersion() );
			
			ds.value( COL_LOC_D2ID ).setString( location.getD2Id() );		
			ds.value( COL_LOC_LOCNAME ).setString( location.getLocName() );		
			ds.value( COL_LOC_EQUIPMENT ).setString( location.getEquipment() );

			ds.value( COL_LOC_RDSLOCTBLREF ).setString( location.getRdsLocTblRef() );
			ds.value( COL_LOC_RDSLOCTBLVER ).setString( location.getRdsLocTblVer() );

			ds.value( COL_LOC_RDSPRIMLOCCODE ).setInt( location.getRdsLocCodePL() );
			ds.value( COL_LOC_RDSSECLOCCODE ).setInt( location.getRdsLocCodeSL() );

			ds.value( COL_LOC_RDSDIRECTION ).setChar( location.getRdsDirection() );
			ds.value( COL_LOC_RDSPRIMLOCDIST ).setInt( location.getRdsPLDist() );
			ds.value( COL_LOC_RDSSECLOCDIST ).setInt( location.getRdsSLDist() );

			ds.value( COL_LOC_STARTCOORDX ).setDouble( location.getStartCoordX() );  
			ds.value( COL_LOC_STARTCOORDY ).setDouble( location.getStartCoordY() );   

			
			ds.value( COL_LOC_ENDCOORDX ).setDouble( location.getEndCoordX() );  
			ds.value( COL_LOC_ENDCOORDY ).setDouble( location.getEndCoordY() ); 
			
			if( connection.getDatakind( DK_MST_LOC ).getColumn( COL_LOC_BEARING ) != null ) {
				ds.value( COL_LOC_BEARING ).setDouble( location.getBearing() ); 
			}

			ds.value( COL_LOC_D2_EXT_REF_SYSTEM ).setString( location.getD2ExtRefSystem() );
			ds.value( COL_LOC_D2_EXT_LOC_CODE ).setString( location.getD2ExtLocCode() );
			
			

			ds.value( COL_LOC_GEODYNID ).setInt( location.getGeodynId() );
			
			List<D2Coordinate> coors = location.getCoordinates();
			
			if( ( coors != null ) && ( coors.size() > 0 ) )
			{
				int index = 1;
				for( D2Coordinate coor : coors )
				{
					Dataset dsc = new de.heuboe.ddp.Dataset( connection.getDatakind( DK_MST_COOR ) );
					dsc.value( COL_TIME ).setDate( now );
					dsc.value( COL_ID ).setInt( location.getId() );
					dsc.value( COL_MST ).setString( def.getMst() );
					dsc.value( COL_VERSION ).setString( def.getVersion() );
					
					dsc.value( COL_COOR_INDEX ).setInt( index );
					dsc.value( COL_COOR_X ).setDouble( coor.getX() );
					dsc.value( COL_COOR_Y ).setDouble( coor.getY() );
					
					coorC.add( dsc );
					
					index++;
				}
			}
				
			locationsC.add( ds );
		}
		
		List<D2MeasureItem>  items = def.getItems();
		for( D2MeasureItem item : items )
		{
			Dataset ds = new de.heuboe.ddp.Dataset( connection.getDatakind( DK_MST_ITEM ) );
			ds.value( COL_TIME ).setDate( now );
			ds.value( COL_ID ).setInt( item.getId() );
			ds.value( COL_MST ).setString( def.getMst() );
			ds.value( COL_VERSION ).setString( def.getVersion() );
			
			ds.value( COL_ITEM_INDEX ).setInt( item.getIndex() );
			ds.value( COL_ITEM_NAME ).setString( item.getName() );
			ds.value( COL_ITEM_CARRIAGEWAY ).setString( item.getCarriagway() );
			ds.value( COL_ITEM_LANE ).setString( item.getLane() );
			ds.value( COL_ITEM_PERIOD ).setInt( item.getPeriod() );
			   	
			ds.value( COL_ITEM_DKREF ).setInt( item.getDkRef() );	
			ds.value( COL_ITEM_DKID ).setInt( item.getDkId_() );
			ds.value( COL_ITEM_DKKEY ).setString( item.getDkKey() );
			ds.value( COL_ITEM_LOCID ).setInt( item.getLocId() );
				
			itemsC.add( ds );
		}

		Dataset ds = new de.heuboe.ddp.Dataset( connection.getDatakind( DK_MST ) );
		ds.value( COL_TIME ).setDate( now );
		ds.value( COL_MST ).setString( def.getMst() );
		ds.value( COL_VERSION ).setString( def.getVersion() );
		ds.value( COL_MST_DESCRIPTION ).setString( def.getDescription() );
		ds.value( COL_MST_D2VERSION ).setString( def.getD2Version() );
		ds.value( COL_MST_DEFVERSION ).setString( def.getDefVersion() );

		ds.value( COL_MST_ACTIVE ).setBoolean( false );
		
		mstC.add( ds );
		
		mod.put( mstC );
		mod.put( dataSourcesC );
		if( dataSourcesEMC.size() > 0 )
			mod.put( dataSourcesEMC );
		if( dataSourcesFilter.size() > 0 )
			mod.put( dataSourcesFilter );
		mod.put( locationsC );
		mod.put( itemsC );
		if( coorC.size() > 0 )
			mod.put( coorC );
		
		connection.write( mod );
		connection.commit();
		
		LOGGER.info( "End of saveMst()");
		LOGGER.info( "");
	}
}
