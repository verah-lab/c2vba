package de.heuboe.datex2.location.service.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import de.heuboe.datex2.location.service.D2MeasurementSite;
import de.heuboe.datex2.location.service.LocationFilter;
import de.heuboe.datex2.location.service.LocationService;
import de.heuboe.datex2.location.service.ServiceException;
import de.heuboe.datex2.location.service.SiteType;
import de.heuboe.datex2.location.service.server.Util.IdGenerator;
import de.heuboe.datex2.location.service.server.Util.SubIdGenerator;
import de.heuboe.datex2.measure.D2MeasureDataSource;
import de.heuboe.datex2.measure.D2MeasureDatakind;
import de.heuboe.datex2.measure.D2MeasureVehClass;
import de.heuboe.log.Logger;
import de.heuboe.mst.config.Column;
import de.heuboe.mst.config.EnumDefinition;
import de.heuboe.mst.config.EnumMappingType;
import de.heuboe.mst.config.EnumValue;
import de.heuboe.mst.config.EnumValueDataType;
import de.heuboe.mst.config.Mapping;
import de.heuboe.mst.config.MappingConfig;
import de.heuboe.mst.config.OutMapping;
import de.heuboe.mst.config.ReferenceType;
import de.heuboe.mst.config.lib.Config;
import de.heuboe.mst.config.lib.Datakind;
import de.heuboe.mst.config.lib.DatakindConfig;
import de.heuboe.mst.definition.MstDefinition;
import de.heuboe.mst.definition.MstEntries;
import de.heuboe.mst.definition.MstEntry;
import de.heuboe.mst.definition.MstFilter;
import de.heuboe.mst.definition.MstObjFilter;
import de.heuboe.mst.definition.MstObjFilters;
import de.heuboe.system.CallStack;

public class MstBuilder
{
	private static final Logger LOGGER = Logger.getLogger( MstBuilder.class );
	private static final String HPA_D2_EXT_REF_TYPE = "HPA";
	
	private Map< SiteType, Sites > siteMap = new HashMap<>();
	private List< DataSource > dataSources = new ArrayList<>();
	
	private IdGenerator locIdGen = new IdGenerator();
	private IdGenerator itemIdGen = new IdGenerator();
	private SubIdGenerator itemIndexGen = new SubIdGenerator();
	
	private Sites createSites( LocationService locService, SiteType siteType, MstFilter mstFilter, boolean objectIdAsD2SiteId )     // NOSONAR
			throws LSBException
	{
		try
		{
			LocationFilter filter = null;
			
			if( mstFilter != null )
			{
				String subObjType = mstFilter.getSubObjType();
				if( ( subObjType != null ) && !subObjType.isEmpty() )
					filter = new LocationFilter( subObjType );
			}
			
			List<D2MeasurementSite> mSites = locService.getDatex2Locations( siteType, filter );
			
			if( mSites != null ) {
			
			
				Set<String> filterIds = new TreeSet<>();
				
				if( mstFilter != null )
				{
					MstObjFilters objFilters = mstFilter.getObjFilters();
					if( objFilters != null )
					{
						List<MstObjFilter> ofs = objFilters.getItem();
						if( ( ofs != null ) && !ofs.isEmpty() )
						{
							for( MstObjFilter of : ofs )
							{
								filterIds.add( of.getId() );
							}
						}
					}
				}
					
				if( !filterIds.isEmpty() )
				{
					List<D2MeasurementSite> unfilteredMSites = new ArrayList<>();
					unfilteredMSites.addAll( mSites );
					mSites.clear();
					
					for( D2MeasurementSite mSite : unfilteredMSites )
					{
						if( filterIds.contains( mSite.getObjId() ) )
						{
							mSites.add( mSite );
						}
					}
				}
				
				return new Sites( siteType, mSites, locIdGen, itemIdGen, itemIndexGen, objectIdAsD2SiteId );
			} else {
				return null;
			}
		}
		catch( ServiceException ex )
		{
			LOGGER.error( CallStack.getStackTraceAsString( ex ) );
			throw new LSBException( LSBException.ERROR_LOC_SERVICE_ERROR, "Error on localisation", ex );
		}
	}
	
	public MstContentExt build( LocationService locService,    // NOSONAR
			                 	String d2Version,
			                 	String mst,
			                 	String defVersion,
			                 	String mstVersion,
			                 	MappingConfig mappingConfig, 
			                 	MstDefinition definition,
			                 	boolean objectIdAsD2SiteId )
			throws LSBException
	{
		LOGGER.info( "Start build():");
		LOGGER.info( "MST-Conf-ID:        " + d2Version );
		LOGGER.info( "MST-Def-Version:    " + defVersion );
		LOGGER.info( "MST-ID:             " + mst );
		LOGGER.info( "MST-Version:        " + mstVersion );
		
		Config config = new Config( mappingConfig );
		
		MstContentExt def = new MstContentExt( d2Version, mst, defVersion, mstVersion );
		def.setDescription( definition.getDescription() );
		
		buildDataSources( config, mappingConfig.getEnumDefinition(), definition );
		for( DataSource src : dataSources )
		{
			SiteType siteType = src.getSiteType();
			Sites sites = siteMap.get( siteType );   // NOSONAR
			if( sites == null )
			{
				sites = createSites( locService, siteType, definition.getFilter(), objectIdAsD2SiteId );
			}
			
			if( sites != null ) {
				siteMap.put( siteType, sites );
				sites.addDataSource( src );
			}
		}
		
		def.setSources( getSources() );
		for( Sites sites : siteMap.values() )
		{
			def.addItems( sites.getItems() );
			def.addLocations( sites.getLocations() );
			def.addSites( sites.getMeasurementSites() );
		}
		
		LOGGER.info( "End of build()");
		return def;
	}
	
	private List<D2MeasureDataSource> getSources()
	{
		List<D2MeasureDataSource> sources = new ArrayList<>();
		for( DataSource dataSource : dataSources )
			sources.add( dataSource.getSrc() );
		
		return sources;
	}
	
	private void buildDataSources( Config config,                       // NOSONAR
								   List<EnumDefinition> enumDefs,	
								   MstDefinition definition )
			throws LSBException
	{
		MstEntries mes = definition.getColumnEntries();
		int srcCount = 1;
		for( MstEntry me : mes.getItem() )
		{
			D2MeasureDataSource src = new D2MeasureDataSource( srcCount++ );
			
			String valCol = me.getColumn() ;
			String valColChar = me.getColumnCharacteristic();
			src.setDbValueCol( valCol );
			src.setValueColCharacteristic( valColChar );
			
			DatakindConfig dkc = config.getDatakindConfig( me.getDatakindName() );
			if( dkc == null )
			{
				throw new LSBException( LSBException.ERROR_MST_DEFINITION, 
										"MST-Definition referenziert unbekannte Datakind '" + 
										me.getDatakindName() + "'" );
			}
			
			Mapping mp = dkc.getMapping( valCol, valColChar );
			if( mp == null )
			{
				throw new LSBException( LSBException.ERROR_MST_DEFINITION, 
										"MST-Definition referenziert unbekannte Column '" + 
										valCol + "/" + valColChar +"'" );
			}
			
			OutMapping omp = mp.getOut();
			src.setD2Value( omp.getValue() );
			src.setD2ValueInner( omp.getValueInner() );
			src.setD2ValueElement( omp.getValueElement() );
			src.setD2ValuePath( omp.getValuePath() );
			src.setD2BasicDataType( omp.getBasicDataType() );	
			src.setDatakind( D2MeasureDatakind.toDatakind( omp.getValueType().value() ) );
			src.setVehClass( D2MeasureVehClass.toVehClass( omp.getVehType().value() ) );
			
			src.setFilter( me.getFilterColumn(), me.getFilterValue().toArray( new String[0]) );
			if( me.getPeriod() != null ) {
				src.setPeriod( me.getPeriod() );
			}
			
			Datakind dk = dkc.getDatakind();
			
			src.setDbDatakind( me.getDatakindName() );
			src.setDbIdCol( dk.getDatakindDesc().getIdColumn() );
			
			Column col = dk.getColumn( valCol );
			src.setDbValidCol( col.getValidColumn() );
			String ivvs = "";
			for( String ivv : col.getInvalidValue() )
			{
				if( !ivvs.isEmpty() )
					ivvs += ";";    // NOSONAR
				ivvs += ivv;        // NOSONAR
			}
			src.setDbInvalidValsRaw( ivvs );
			
			src.setValidInterval( me.getValidInterval() );
			
			Float factor = mp.getFactor();
			if( factor != null )
				src.setFactor( (double)factor );
			else
				src.setFactor( 1.0 );
			
			src.setMappingType( mp.getMappingType().value() );
			
			EnumValueDataType vdt = mp.getValueDataType();
			if( vdt == null )
				vdt = Config.getDefaultValueDataType();
			
			src.setValueDataType( vdt.value() );
			
			String objType = dkc.getDatakind().getDatakindDesc().getObjectType();
			List<String> subTypes = dkc.getDatakind().getDatakindDesc().getSubType();
			
			SiteType siteType = new SiteType( objType );
			if( dk.getDatakindDesc().getReferenceType() == ReferenceType.SERVICE_PROVIDED_EXT_REF ) {
				siteType.setExternalType( HPA_D2_EXT_REF_TYPE );
			}
			
			if( !subTypes.isEmpty() ) {
				siteType.setHasSubSites( true );
			}
			
			DataSource ds = new DataSource( src, siteType, subTypes );
			
			if( mp.getMappingType() == EnumMappingType.PROVIDED_ENUM )
			{
				String enumName = mp.getEnumType();
				if( enumName != null )
				{
					for( EnumDefinition enumDef : enumDefs )
					{
						if( enumDef.getName().equals( enumName ) )
						{
							Map< Integer, String > values = new TreeMap<>();
							for( EnumValue ev : enumDef.getItem() )
							{
								values.put( ev.getValue() , ev.getName() );
							}
							src.setEnumValues( values );		
						}
					}
				}
			}

			
			dataSources.add( ds );
		}
	}

	
}
