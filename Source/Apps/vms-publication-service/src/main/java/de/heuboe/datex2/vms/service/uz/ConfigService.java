package de.heuboe.datex2.vms.service.uz;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

import de.heuboe.datex2.vms.service.VMSTableConfiguration;
import de.heuboe.datex2.vms.service.VMSServiceException;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplay;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplayPart;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplayPart.DisplayPartType;
import de.heuboe.datex2.vms.service.table.data.D2VMSUnit;
import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;
import de.heuboe.zst.AQType;
import de.heuboe.zst.CfgAttribute;
import de.heuboe.zst.CfgObject;
import de.heuboe.zst.CfgObjects;
import de.heuboe.zst.CfgService;
import de.heuboe.zst.DisplayType;
import de.heuboe.zst.LanePos;
import de.heuboe.zst.ObjReferences;
import de.heuboe.zst.SignPostType;
import de.heuboe.zst.Strings;
import de.heuboe.zst.ZstError;
import eu.datex2.schema._2._2_0.LaneEnum;
import eu.datex2.schema._2._2_0.Location;
import eu.datex2.schema._2._2_0.LocationDescriptorEnum;


public class ConfigService extends ConfigManager
{
	private static final Logger LOGGER = Logger.getLogger( ConfigService.class );
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

	public ConfigService()
	{
	}
	
	public boolean connect( String url )
	{
		try
		{
			JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
			factory.setServiceClass( CfgService.class );
			factory.setAddress( url );
			factory.setWsdlLocation( "cfgService.wsdl" );
			
			JAXBDataBinding db = new JAXBDataBinding();
			factory.setDataBinding( db );
				
			cfgService = (CfgService)factory.create();
			
			
			cfgService.getAllObjectTypes( 1 );
			
			LOGGER.info( "Sucessfully connected to CfgService." );
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
	
	/**
	 * 
	 * Bestimmt die Liste der Werte, die die Objekte <objects>
	 * bei Aufruf von "getId" zurückgeben.
	 * 
	 * @param objects			Objekte
	 * @return					Werte-Liste
	 * @throws ZstError			Service-Exception
	 */
	public static  <T> Collection<String> getIds( Collection<T> objects )
			throws ZstError
	{
		return getIds( objects, "getId" );
	}	
	
	/**
	 * 
	 * Bestimmt die Liste der Werte, die die Objekte <objects>
	 * bei Aufruf von <idMethodName> zurückgeben.
	 * 
	 * @param objects			Objekte
	 * @param idMethodName		Methodenname
	 * @return					Werte-Liste
	 * @throws ZstError			Service-Exception
	 */
	public static  <T> Collection<String> getIds( Collection<T> objects, String idMethodName )
			throws ZstError  
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
			throw new ZstError( "Internal error: " + ex.getMessage(), ex );
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
		catch( ZstError ex )
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
		catch( ZstError ex )
		{
			throw new VMSServiceException( VMSServiceException.VMS_SVC_ERR_READ_INFRA, "Error on getSites()", ex );
		}
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
		catch( ZstError ex )
		{
			throw new VMSServiceException( VMSServiceException.VMS_SVC_ERR_READ_INFRA, "Error on getAqs()", ex );
		}
	}
	
	@Override
	public List<D2VMSUnit> getD2VMSUnits( VMSTableConfiguration filter, 
										  LocationManager locationManager )
			throws VMSServiceException
	{
		LOGGER.info( "ConfigService: getD2VMSUnits()" );
		
		Set<String> filterSites = null;
		Set<String> filterAqs = null;
		
		List<String> filterSiteList = filter.getSbaIds();
		List<String> filterAqList = filter.toAqIds();
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
		CfgObjects sites = getSites();
		
		for( CfgObject site : sites.getItem() )
		{
			if( ( filterSites == null ) || ( filterSites.contains( site.getMyself().getId() )) )
			{
				CfgObjects aqs = getAqs( site.getMyself().getId() );
				for( CfgObject aq : aqs.getItem() )
				{
					String aqId = aq.getMyself().getId();
					
					if( ( filterAqs == null ) || ( filterAqs.contains( aqId )) ) {
					
						D2VMSUnit unit = new D2VMSUnit();
						unit.setInternalId( aqId );
						
						CfgObjects wzgs = getWzgs( aqId );
						
						if( isWWW( aq ) )
						{
							D2VMSDisplay display = new D2VMSDisplay();
							
							display.setDisplayType( de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.DisplayType.WWW );
							display.setDescription( aqId );
							display.setInternalId( aqId );
							
							Location location = locationManager.getLocation( Util.CFG_AQ_TYPE, 
									                                         aqId,
																			 filter.isAddTmcLocations(),
																			 filter.isAddOpenLRLocations() );
							
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
								SignPostType spt = getSignPostType( wzg );
								displayPart.setDisplayPartType( getDisplayPartType( spt ) );
								display.getDisplayParts().add( displayPart );
							}
							
							unit.addDisplay( display );
						}
						else if( isNonStandardAQ( aq, wzgs.getItem() ) )
						{
							// Only one WZG
						}
						else
						{
							for( CfgObject wzg : wzgs.getItem() )
							{
								D2VMSDisplay display = new D2VMSDisplay();
								
								String wzgId = wzg.getMyself().getId();
								display.setDescription( wzgId );
								display.setInternalId( wzgId );
								display.setDisplayType( de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.DisplayType.WZG ); 
								
								Location location = locationManager.getLocation( Util.CFG_WZG_TYPE, 
																				 aqId,
										 										 filter.isAddTmcLocations(),
										 										 filter.isAddOpenLRLocations()  );
								
								
								LanePos lp = getLanePos( wzg );
								List<LaneEnum> lanes = null;
								LocationDescriptorEnum ld = null;
								if( lp != null ) {
									lanes = getLanes( lp );
									ld = getLocationDescriptor( lp );
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
								SignPostType spt = getSignPostType( wzg );
								displayPart.setDisplayPartType( getDisplayPartType( spt ) );
								display.getDisplayParts().add( displayPart );
								
								unit.addDisplay( display );
							}
						}
						
						units.add( unit );
					}
				}
			} 
		}
		return units;
	}
	
	
	private static LocationDescriptorEnum toLocationDescriptor( String cfgLane ) 
	{
/*		
		switch( cfgLane )
		{
			case CFG_ATTR_LANE_AS:
				return LocationDescriptorEnum.ON_THE_RIGHT;
			case CFG_ATTR_LANE_IS:
				return LocationDescriptorEnum.ON_THE_LEFT;
			default:
*/			
				return null;
		//}
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
	
	
	public static List<LaneEnum> getLanes( LanePos lanePos )
	{
		return toLane( "LanePos." + lanePos.value() );
	}
	
	public static LocationDescriptorEnum getLocationDescriptor( LanePos lanePos )
	{
		return toLocationDescriptor( "LanePos." + lanePos.value() );
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
	
	
	public static boolean isWWW( AQType type )
	{
		return ( type == AQType.WWWQ );
	}
	
	public static boolean isWWW( CfgObject aq )
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

	
	
}
