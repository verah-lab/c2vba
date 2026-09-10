package de.heuboe.datex2.vms.service.uz;

/**
 * 
 * Nicht mehr verwendet !
 * 
 */



/*
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

import de.heuboe.datex2.vms.service.VMSTableConfiguration;
import de.heuboe.datex2.vms.service.VMSServiceException;
import de.heuboe.datex2.vms.service.VMSTableConfiguration.DWiStaMode;
import de.heuboe.datex2.vms.service.VMSTableConfiguration.DWiStaTextArea;
import de.heuboe.datex2.vms.service.VMSTableConfiguration.DWiStaTextAreaImage;
import de.heuboe.datex2.vms.service.VMSTableConfiguration.DWiStaTextAreaLayout;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplay;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplayPart;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplayPart.DisplayPartMode;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplayPart.DisplayPartType;
import de.heuboe.datex2.vms.service.table.data.D2VMSUnit;
import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;
import de.heuboe.nrw.guisvc.cfgClasses.CfgAttribute;
import de.heuboe.nrw.guisvc.cfgClasses.CfgObject;
import de.heuboe.nrw.guisvc.cfgClasses.CfgObjects;
import de.heuboe.nrw.guisvc.cfgClasses.GenConfigError;
import de.heuboe.nrw.guisvc.cfginterface.CfgService;
import de.heuboe.nrw.guisvc.cfgTypes.DisplayType;
import de.heuboe.nrw.guisvc.cfgTypes.LanePos;
import de.heuboe.nrw.guisvc.cfgClasses.ObjReferences;
import de.heuboe.nrw.guisvc.cfgTypes.SignPostType;
import de.heuboe.nrw.guisvc.cfgClasses.Strings;
import de.heuboe.zst.ZstError;
import eu.datex2.schema._2._2_0.LaneEnum;
import eu.datex2.schema._2._2_0.Location;
import eu.datex2.schema._2._2_0.LocationDescriptorEnum;
*/

import java.util.List;
import de.heuboe.datex2.vms.service.VMSServiceException;
import de.heuboe.datex2.vms.service.VMSTableConfiguration;
import de.heuboe.datex2.vms.service.table.data.D2VMSUnit;


public class ConfigServiceNRW extends ConfigManager
{
/*	
	private static final Logger LOGGER = Logger.getLogger( ConfigServiceNRW.class );
	private CfgService cfgService = null;
	
	@SuppressWarnings("serial")
	private static Map< String, DisplayPartType > wzgType2DisplayPartType = new TreeMap<String, DisplayPartType >()
	{{
		put( CFG_SIGN_POST_TYPE_A, DisplayPartType.ASign );
		put( CFG_SIGN_POST_TYPE_B, DisplayPartType.BSign );
		put( CFG_SIGN_POST_TYPE_C, DisplayPartType.CSign );
		put( CFG_SIGN_POST_TYPE_D, DisplayPartType.DSign );
		put( CFG_SIGN_POST_TYPE_E, DisplayPartType.ESign );
		put( CFG_SIGN_POST_TYPE_ZA, DisplayPartType.SSign );
	}};
	
	@SuppressWarnings("serial")
	private static Map< String, DisplayPartType > anzeigePrinzip2DisplayPartType = new TreeMap<String, DisplayPartType >()
	{{
		put( CFG_ATTR_DISPLAY_TYPE_A, DisplayPartType.ASign );
		put( CFG_ATTR_DISPLAY_TYPE_B, DisplayPartType.BSign );
		put( CFG_ATTR_DISPLAY_TYPE_C, DisplayPartType.CSign );
		put( CFG_ATTR_DISPLAY_TYPE_D, DisplayPartType.DSign );
		put( CFG_ATTR_DISPLAY_TYPE_E, DisplayPartType.ESign );
	}};

	public ConfigServiceNRW()
	{
	}
	
	public boolean connect( String url )
	{
		try
		{
			JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
			factory.setServiceClass( CfgService.class );
			factory.setAddress( url );
			
			JAXBDataBinding db = new JAXBDataBinding();
			factory.setDataBinding( db );
				
			cfgService = (CfgService)factory.create();
			
			
			cfgService.getAllObjectTypes( 1 );
			
			LOGGER.info( "Successfully connected to CfgService." );
			return true;
		}
		catch( Throwable ex )
		{
			LOGGER.fatal( "Failed to connect to CfgService" );
			LOGGER.fatal( ex.toString() );
			LOGGER.fatal( CallStack.getStackTraceAsString( ex ) );
			
			return false;
		}
	}
	
	public static final Strings toStrings( Collection<String> ids )
	{
		return new Strings().withItem(ids);
	}
	
	public static  <T> Collection<String> getIds( Collection<T> objects )
			throws GenConfigError
	{
		return getIds( objects, "getId" );
	}	
	
	public static  <T> Collection<String> getIds( Collection<T> objects, String idMethodName )
			throws GenConfigError  
	{
		try
		{
			List<String> ids = new ArrayList<String>();
			for( T obj : objects )
			{
	    		Method method = obj.getClass().getDeclaredMethod( idMethodName, new Class<?> [0] );
	    		Object result = method.invoke( obj, (Object[])null);
				
				ids.add( result.toString() );
			}
			return ids;
		}
		catch( Exception ex )
		{
			throw new GenConfigError( "Internal error: " + ex.getMessage(), ex );
		}
	}
	
	public CfgObjects getAqs( String siteId )
			throws VMSServiceException
	{
		try
		{
			Strings objTypes = new Strings();
			objTypes.getItem().add( "ObjectType.AQ" );
			CfgObjects cfgObjects = cfgService.getObjectsByParent( UzService.SERVICE_ID, siteId , objTypes );
			
			return cfgObjects;
		}
		catch( GenConfigError ex )
		{
			throw new VMSServiceException( VMSServiceException.VMS_SVC_ERR_READ_INFRA, "Error on getAqs()", ex );
		}
	}
	
	public CfgObjects getSites()
			throws VMSServiceException
	{
		try
		{
			ObjReferences orsSite = cfgService.getObjectReferences( UzService.SERVICE_ID, "ObjectType.SBA" );
			
			Collection<String> ids = getIds( orsSite.getItem() );
			return cfgService.getObjectsByIds( UzService.SERVICE_ID, toStrings(ids) );
		}
		catch( GenConfigError ex )
		{
			throw new VMSServiceException( VMSServiceException.VMS_SVC_ERR_READ_INFRA, "Error on getSites()", ex );
		}
	}
	
	public CfgObjects getAqs()
			throws VMSServiceException
	{
		try
		{
			ObjReferences orsSite = cfgService.getObjectReferences( UzService.SERVICE_ID, "ObjectType.AQ" );
			
			Collection<String> ids = getIds( orsSite.getItem() );
			return cfgService.getObjectsByIds( UzService.SERVICE_ID, toStrings(ids) );
		}
		catch( GenConfigError ex )
		{
			throw new VMSServiceException( VMSServiceException.VMS_SVC_ERR_READ_INFRA, "Error on getSites()", ex );
		}
	}
	
	public boolean isDWiSta( CfgObject aq )
	{
		List<CfgAttribute> attrs = aq.getAttributes().getItem();
		for( CfgAttribute attr : attrs )
		{
			if( attr.getName().equals( CFG_ATTR_TYPE_AQ_TYPE ) )
			{
				if( aq.getMyself().getId().startsWith( "WW" ) ) 
				// @@@  remove if( attr.getValue().equals( CFG_ATTR_AQ_DWISTA ) )
					return true;
			}
		}
			
		return false;
	}
	
	public boolean isWWW( CfgObject aq )
	{
		List<CfgAttribute> attrs = aq.getAttributes().getItem();
		for( CfgAttribute attr : attrs )
		{
			if( attr.getName().equals( CFG_ATTR_TYPE_AQ_TYPE ) )
			{
				if( attr.getValue().equals( CFG_ATTR_AQ_WWW ) )
					return true;
			}
		}
			
		return false;
	}
	
	public LanePos getLanePos( CfgObject wzg )
	{
		List<CfgAttribute> attrs = wzg.getAttributes().getItem();
		for( CfgAttribute attr : attrs )
		{
			if( attr.getName().equals( CFG_ATTR_TYPE_LANE ) )
			{
				return LanePos.fromValue( attr.getValue().substring( "LanePos.".length() ) );
			}
		}
		
		return null;
	}
	
	public SignPostType	getSignPostType( CfgObject wzg ) {

		List<CfgAttribute> attrs = wzg.getAttributes().getItem();
		for( CfgAttribute attr : attrs )
		{
			if( attr.getName().equals( CFG_ATTR_TYPE_WZG_TYPE ) )
			{
					return SignPostType.fromValue( attr.getValue().substring( "SignPostType.".length() ) );
			}
		}
		
		return null;
		
	}
	
	public Integer getDeNumber( CfgObject object ) {
		
		List<CfgAttribute> attrs = object.getAttributes().getItem();
		for( CfgAttribute attr : attrs )
		{
			if( attr.getName().equals( CFG_ATTR_TYPE_DE ) )
			{
				String val = attr.getValue();
				String[] parts = val.split( "-" );
				if( ( parts.length == 3 ) && !parts[2].isEmpty() ) {
					
					try {
					    return Integer.parseInt( parts[2] );
					}
					catch( NumberFormatException ex) {
						LOGGER.debug( "Error: Object <" + object.getMyself().getId() + ">" );
					    LOGGER.debug( "Error: Error parsing DE-Nummer: <" + parts[2] + ">");
					}					
				}
			}
		}
		
		return null;
	}
	
	
	public boolean isNonStandardAQ( CfgObject aq, List<CfgObject> wzgs )
	{
		return false;
	}
	
	
	public CfgObjects getWzgs( String parentId )
			throws VMSServiceException
	{
		try
		{
			CfgObjects orsAQ = cfgService.getObjectsByParent( UzService.SERVICE_ID, 
															  parentId, 
															  (new Strings()).withItem( Util.CFG_WZG_TYPE ) );
			return orsAQ;
		}
		catch( GenConfigError ex )
		{
			throw new VMSServiceException( VMSServiceException.VMS_SVC_ERR_READ_INFRA, "Error on getAqs()", ex );
		}
	}

	private List<DWiStaTextArea> getTextAreas( VMSTableConfiguration configuration, List<CfgObject> wzgs ) {
		
		List<DWiStaTextArea> dtas = new ArrayList<>();
		
		Set<Integer> wzgDes = new HashSet<>();
		for( CfgObject wzg: wzgs ) {
			Integer de = getDeNumber( wzg );
			if( de != null ) {
				wzgDes.add( de );
			}
		}
		
		for( DWiStaTextAreaLayout ta : configuration.getDWiStaTextAreaLayouts() ) {
			
			if( ta.isAqTextArea( wzgDes ) ) {
				return ta.getTextAreas();
			}
		}
		
		return dtas;
	}
	
	private boolean wzgIsSingleUnit( VMSTableConfiguration configuration, 
			                         CfgObject aq, 
			                         Set<Integer> dWisTaDes,
			                         CfgObject wzg ) {
		if( !isWWW( aq ) ) {
			if( isDWiSta(aq) ) {
				
				if( (configuration.getdWiStaMode() == DWiStaMode.SINGLE_WZG ) ) {
					return true;
				}
				
				if( (configuration.getdWiStaMode() == DWiStaMode.COMPLETE_TEXTAREA ) ) {
					Integer de = getDeNumber( wzg );
					
					if( de != null ) {
						return !dWisTaDes.contains( de );
					}
				}
			}
		}
		
		return true;
	}
	
	private List<CfgObject> getTextAreaWzgs( DWiStaTextArea ta, List<CfgObject> allWzgs ) {
		
		List<CfgObject> wzgs = new ArrayList<>();
		
		Set<Integer> des = ta.getTextDeList().stream().collect( Collectors.toSet() );
		
		for( CfgObject w : allWzgs ) {
			Integer de = getDeNumber( w );
			if( de != null ) {
				if( des.contains( de ) ) {
					wzgs.add( w );
				}
			}
		}
		
		return wzgs;
	}
	
	private CfgObject getImageWzg( List<CfgObject> allWzgs, int imageDe ) {
		
		for( CfgObject w : allWzgs ) {
			Integer de = getDeNumber( w );
			if( de != null ) {
				if( de == imageDe ) {
					return w;
				}
			}
		}
		
		return null;
	}
	
	
	@Override
	public List<D2VMSUnit> getD2VMSUnits( VMSTableConfiguration configuration, 
										  LocationManager locationManager )
			throws VMSServiceException
	{
		LOGGER.info( "ConfigServiceNRW: getD2VMSUnits()" );
		
		Set<String> filterSites = null;
		Set<String> filterAqs = null;
		
		List<String> filterSiteList = configuration.getSbaIds();
		List<String> filterAqList = configuration.getAqIds();
		if( ( filterSiteList != null ) && !filterSiteList.isEmpty() )
		{
			filterSites = new HashSet<String>( filterSiteList );
		}
		
		if( ( filterAqList != null ) && !filterAqList.isEmpty() )
		{
			filterAqs = new HashSet<String>( filterAqList );
		}
		
		
		locationManager.init();
		List<D2VMSUnit> units = new ArrayList<D2VMSUnit>();
		
		List<CfgObject> aqs = new ArrayList<>();
		if( ( filterSites == null ) || ( filterSites.size() == 0 ) ) {
			aqs.addAll( getAqs().getItem() );
		} else {
			CfgObjects sites = getSites();
			for( CfgObject site : sites.getItem() )
			{
				if( ( filterSites == null ) || ( filterSites.contains( site.getMyself().getId() )) )
				{
					aqs.addAll( getAqs( site.getMyself().getId() ).getItem() );
				}
			}
		}		
		
		for( CfgObject aq : aqs )
		{
			String aqId = aq.getMyself().getId();
			
			if( ( filterAqs == null ) || ( filterAqs.contains( aqId )) ) {
			
				D2VMSUnit unit = new D2VMSUnit();
				unit.setInternalId( aqId );
				
				CfgObjects wzgs = getWzgs( aqId );
				
				boolean complete = isWWW( aq ) 
								   || 
								   ( isDWiSta( aq ) && ( configuration.getdWiStaMode() == DWiStaMode.COMPLETE ) );
				
				if( isWWW( aq ) ) {
					unit.setDescription( "WWW" );
				} else if( isDWiSta( aq ) ) {
					unit.setDescription( "dWiSta" );
				} else {
					unit.setDescription( "AQ" );
				}
				
				if( complete ) 
				{
					D2VMSDisplay display = new D2VMSDisplay();
					
					if( isDWiSta( aq ) ) {
						display.setDisplayType( de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.DisplayType.DWISTA );
					} else {
						display.setDisplayType( de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.DisplayType.WWW );
					}
					
					display.setDescription( aqId );
					display.setInternalId( aqId );
					
					Location location = locationManager.getLocation( Util.CFG_AQ_TYPE, 
							                                         aqId,
																	 configuration.isAddTmcLocations(),
																	 configuration.isAddOpenLRLocations() );
					
					if( location == null ) 
					{
						LOGGER.error( "No location for AQ with ID <" + aqId + ">" );
						continue;
					}
	
					display.setLocation( location );
					
					for( CfgObject wzg : wzgs.getItem() )
					{
						D2VMSDisplayPart displayPart = new D2VMSDisplayPart();
						displayPart.setInternalId( wzg.getMyself().getId() );
						
						Integer de = getDeNumber( wzg );
						if( de != null ) {
							displayPart.setDe( de );
						}
						
						SignPostType spt = getSignPostType( wzg );
						displayPart.setDisplayPartType( getDisplayPartType( spt ) );
						display.getDisplayParts().add( displayPart );
					}
					
					unit.addDisplay( display );
				}
				else if( isDWiSta( aq ) && ( configuration.getdWiStaMode() == DWiStaMode.COMPLETE_TEXTAREA )  )
				{
					// WZGs einer Textarea werden zu einem D2VMSDisplay zusammengefasst
					
					List<DWiStaTextArea> tas = getTextAreas( configuration, wzgs.getItem() );
					int index = 1;
					for( DWiStaTextArea ta : tas ) {
						
						List<CfgObject> taWzgs = getTextAreaWzgs( ta, wzgs.getItem() ); 
						
						D2VMSDisplay display = new D2VMSDisplay();
						
						String descr = "dWiSta Textbereich_" + index;
						String id = descr + " " + aqId;
						
						display.setDescription( descr );
						display.setInternalId( id );
						display.setDisplayPosition( ta.getPosition() );
						display.setDisplayType( de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.DisplayType.DWISTA_TXT_GRP ); 
						
						Location location = locationManager.getLocation( Util.CFG_WZG_TYPE, 
																		 aqId,
								 										 configuration.isAddTmcLocations(),
								 										 configuration.isAddOpenLRLocations()  );
						
						
						List<LaneEnum> lanes = Arrays.asList( LaneEnum.ALL_LANES_COMPLETE_CARRIAGEWAY );
						locationManager.setWzgLanes( aqId, location, lanes, null );
						display.setLocation( location );
						
						for( CfgObject taWzg : taWzgs ) {
							D2VMSDisplayPart displayPart = new D2VMSDisplayPart();
							displayPart.setInternalId( taWzg.getMyself().getId() );
							displayPart.setDisplayPartMode( DisplayPartMode.TEXT );
							displayPart.setDe( getDeNumber( taWzg ) );
							SignPostType spt = getSignPostType( taWzg );
							displayPart.setDisplayPartType( getDisplayPartType( spt ) );
							display.getDisplayParts().add( displayPart );
						}
						
						int i = 0;
						List<DWiStaTextAreaImage> images = ta.getImages();
						for( DWiStaTextAreaImage image : images ) {
							
							CfgObject iObj = getImageWzg( wzgs.getItem(), image.getDe() );
							
							D2VMSDisplayPart displayPart = new D2VMSDisplayPart();
							displayPart.setInternalId( iObj.getMyself().getId() );
							displayPart.setPictogramIndex( i++ );
							displayPart.setDe( getDeNumber( iObj ) );
							displayPart.setDisplayPartMode( DisplayPartMode.IMAGE );
							displayPart.setDisplayPosition( image.getPosition() );
							SignPostType spt = getSignPostType( iObj );
							displayPart.setDisplayPartType( getDisplayPartType( spt ) );
							display.getDisplayParts().add( displayPart );
						}
						
						unit.addDisplay( display );
						
						index++;
					}
				}
				
				
				if( !complete )
				{
					List<DWiStaTextArea> tas = getTextAreas( configuration, wzgs.getItem() );
					Set<Integer> dWiStaDes = tas.stream()
							                    .map( t -> t.getDeList() )
							                    .flatMap(List::stream)
							                    .collect( Collectors.toSet() );
					
					Set<Integer> dWiStaTextDes = tas.stream()
								                    .map( t -> t.getTextDeList() )
								                    .flatMap(List::stream)
								                    .collect( Collectors.toSet() );
					
					for( CfgObject wzg : wzgs.getItem() )
					{
						if( wzgIsSingleUnit( configuration, aq, dWiStaDes, wzg ) ) {
							
							D2VMSDisplay display = new D2VMSDisplay();
							Integer deNr = getDeNumber( wzg );
							if( deNr != null ) {
								display.setDe( deNr );
							}
							
							String wzgId = wzg.getMyself().getId();
							display.setDescription( wzgId );
							display.setInternalId( wzgId );
							
							if( ( deNr != null )  && dWiStaTextDes.contains( deNr ) ) {
								display.setDisplayType( de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.DisplayType.DWISTA_TXT_WZG ); 
							} else {
								display.setDisplayType( de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.DisplayType.WZG ); 
							}
							
							Location location = locationManager.getLocation( Util.CFG_WZG_TYPE, 
																			 aqId,
									 										 configuration.isAddTmcLocations(),
									 										 configuration.isAddOpenLRLocations()  );
							
							
							LanePos lp = getLanePos( wzg );
							
							List<LaneEnum> lanes = null;
							LocationDescriptorEnum ld = null;
							if( lp != null ) {
								lanes = getLanes( lp );
								ld = getLocationDescriptor( lp );
							}
							
							if( isDWiSta( aq ) ) {
								lanes = Arrays.asList( LaneEnum.ALL_LANES_COMPLETE_CARRIAGEWAY );
							}
							
							if( ( lanes == null || lanes.isEmpty() ) && ld == null ) 
							{
								LOGGER.error( "No lane localization for WZG with ID <" + wzgId + ">" );
								continue;
							}
							locationManager.setWzgLanes( aqId, location, lanes, ld );
							
							display.setLocation( location );
							
							D2VMSDisplayPart displayPart = new D2VMSDisplayPart();
							displayPart.setInternalId( wzg.getMyself().getId() );
							
							if( deNr != null ) {
								displayPart.setDe( deNr );
							}

							SignPostType spt = getSignPostType( wzg );
							displayPart.setDisplayPartType( getDisplayPartType( spt ) );
							display.getDisplayParts().add( displayPart );
							
							unit.addDisplay( display );
						}
					}
				}
				
				units.add( unit );
			}
		}
		return units;
	}
	
	
	private static LocationDescriptorEnum toLocationDescriptor( String cfgLane ) 
	{
		return null;
	}
	
	private static List<LaneEnum> toLane( String cfgLane )
	{
		List<LaneEnum> result = new ArrayList<LaneEnum>();
		
		switch( cfgLane )
		{
			case CFG_ATTR_LANE_ABS:
				result.add( LaneEnum.HARD_SHOULDER );
				break;
			case CFG_ATTR_LANE_ABSF1:
				result.add( LaneEnum.HARD_SHOULDER );
				result.add( LaneEnum.LANE_1 );
				break;
			case CFG_ATTR_LANE_F1:
				result.add( LaneEnum.LANE_1 );
				break;
			case CFG_ATTR_LANE_F2:
				result.add( LaneEnum.LANE_2 );
				break;
			case CFG_ATTR_LANE_F3:
				result.add( LaneEnum.LANE_3 );
				break;
			case CFG_ATTR_LANE_F4:
				result.add( LaneEnum.LANE_4 );
				break;
			case CFG_ATTR_LANE_F5:
				result.add( LaneEnum.LANE_5 );
				break;
			case CFG_ATTR_LANE_F1F2:
				result.add( LaneEnum.LANE_1 );
				result.add( LaneEnum.LANE_2 );
				break;
			case CFG_ATTR_LANE_F2F3:
				result.add( LaneEnum.LANE_2 );
				result.add( LaneEnum.LANE_3 );
				break;
			case CFG_ATTR_LANE_F3F4:
				result.add( LaneEnum.LANE_3 );
				result.add( LaneEnum.LANE_4 );
				break;
			case CFG_ATTR_LANE_F4F5:
				result.add( LaneEnum.LANE_4 );
				result.add( LaneEnum.LANE_5 );
				break;
		}
		
		if( result.isEmpty() )
		{
			// return null;
			return Arrays.asList( LaneEnum.ALL_LANES_COMPLETE_CARRIAGEWAY );			
		}
		else 	
			return result;
	}
	
	public static DisplayPartType getDisplayPartType( DisplayType type )
	{
		DisplayPartType dpt = anzeigePrinzip2DisplayPartType.get( "DisplayType." + type.value() );
		if( dpt != null )
		{
			return dpt;
		}
		
		return DisplayPartType.SSign;
	}
	
	public static DisplayPartType getDisplayPartType( SignPostType type )
	{
		DisplayPartType dpt =  wzgType2DisplayPartType.get( "SignPostType." + type.value() );
		if( dpt != null )
		{
			return dpt;
		}
		
		return DisplayPartType.SSign;
	}
	
	public static List<LaneEnum> getLanes( LanePos lanePos )
	{
		return toLane( "LanePos." + lanePos.value() );
	}
	
	public static LocationDescriptorEnum getLocationDescriptor( LanePos lanePos )
	{
		return toLocationDescriptor( "LanePos." + lanePos.value() );
	}
*/	
	
	public boolean connect( String url ) {
		return false;
	}
	
	@Override
	public List<D2VMSUnit> getD2VMSUnits( VMSTableConfiguration configuration, 
										  LocationManager locationManager )
				throws VMSServiceException
	{
		return null; 
	}
}
