package de.heuboe.datex2.vms.service.uz;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;

import de.heuboe.datex2.vms.service.D2Location;
import de.heuboe.datex2.vms.service.VMSServiceException;
import de.heuboe.datex2.vms.service.VMSTableConfiguration;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplay;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.FunctionalType;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplayPart;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplayPart.DisplayPartType;
import de.heuboe.datex2.vms.service.table.data.D2VMSUnit;
import de.heuboe.datex2.vms.service.table.data.D2VMSUnit.UnitType;
import de.heuboe.log.Logger;
import eu.datex2.schema._2._2_0.LaneEnum;
import eu.datex2.schema._2._2_0.Location;
import eu.datex2.schema._2._2_0.LocationDescriptorEnum;
import eu.datex2.schema._2._2_0.Point;
import eu.datex2.schema._2._2_0._PointExtensionType;
import eu.vmis_ehe.vmis2.configservice.CfgAq;
import eu.vmis_ehe.vmis2.configservice.CfgWzg;
import eu.vmis_ehe.vmis2.configservice.CfgWzg.WzgType;


/**
 * 
 * ConfigService implementation of ConfigManager
 * Provides only SBAs
 * 
 * @author peters
 *
 */
public class ConfigManagerSBANonSite extends ConfigManager
{
	private static final Logger LOGGER = Logger.getLogger( ConfigManagerSBANonSite.class );
	
	private static final String SCS_CFG_ATTR_LANE_AS = "AS";	
	private static final String SCS_CFG_ATTR_LANE_AS_LOWER = "As";	
	private static final String SCS_CFG_ATTR_LANE_ABS_LOWER = "AbS";	
	private static final String SCS_CFG_ATTR_LANE_ABSF1_LOWER = "AbSF1";	
	private static final String SCS_CFG_ATTR_LANE_ABS = "PS";	
	private static final String SCS_CFG_ATTR_LANE_ABSF1 = "F1/PS";	
	private static final String SCS_CFG_ATTR_LANE_F1 = "F1";	
	private static final String SCS_CFG_ATTR_LANE_F2 = "F2";	
	private static final String SCS_CFG_ATTR_LANE_F3 = "F3";	
	private static final String SCS_CFG_ATTR_LANE_F4 = "F4";	
	private static final String SCS_CFG_ATTR_LANE_F5 = "F5";	
	private static final String SCS_CFG_ATTR_LANE_F6 = "F6";	
	private static final String SCS_CFG_ATTR_LANE_F7 = "F7";	
	private static final String SCS_CFG_ATTR_LANE_F8 = "F8";	
	private static final String SCS_CFG_ATTR_LANE_F9 = "F9";	
	private static final String SCS_CFG_ATTR_LANE_F1F2_SIMPLE = "F1F2";	
	private static final String SCS_CFG_ATTR_LANE_F2F3_SIMPLE = "F2F3";	
	private static final String SCS_CFG_ATTR_LANE_F3F4_SIMPLE = "F3F4";	
	private static final String SCS_CFG_ATTR_LANE_F4F5_SIMPLE = "F4F5";	
	private static final String SCS_CFG_ATTR_LANE_F5F6_SIMPLE = "F5F6";	
	private static final String SCS_CFG_ATTR_LANE_F6F7_SIMPLE = "F6F7";	
	private static final String SCS_CFG_ATTR_LANE_F7F8_SIMPLE = "F7F8";	
	private static final String SCS_CFG_ATTR_LANE_F8F9_SIMPLE = "F8F9";	
	private static final String SCS_CFG_ATTR_LANE_F1F2 = "F2/F1";	
	private static final String SCS_CFG_ATTR_LANE_F2F3 = "F3/F2";	
	private static final String SCS_CFG_ATTR_LANE_F3F4 = "F4/F3";	
	private static final String SCS_CFG_ATTR_LANE_F4F5 = "F5/F4";	
	private static final String SCS_CFG_ATTR_LANE_F5F6 = "F6/F5";	
	private static final String SCS_CFG_ATTR_LANE_F6F7 = "F7/F6";	
	private static final String SCS_CFG_ATTR_LANE_F7F8 = "F8/F7";	
	private static final String SCS_CFG_ATTR_LANE_F8F9 = "F9/F8";	
	private static final String SCS_CFG_ATTR_LANE_IS_LOWER = "Is";	
	private static final String SCS_CFG_ATTR_LANE_IS = "IS";	
	
	public static final String SCS_CFG_SIGN_POST_TYPE_A_LOWER = "WzgA";	
	public static final String SCS_CFG_SIGN_POST_TYPE_B_LOWER = "WzgB";	
	public static final String SCS_CFG_SIGN_POST_TYPE_C_LOWER = "WzgC";	
	public static final String SCS_CFG_SIGN_POST_TYPE_D_LOWER = "WzgD";	
	public static final String SCS_CFG_SIGN_POST_TYPE_E_LOWER = "WzgE";	
	public static final String SCS_CFG_SIGN_POST_TYPE_ZA_LOWER = "WzgZA";	
	public static final String SCS_CFG_SIGN_POST_TYPE_A = "WZG_A";	
	public static final String SCS_CFG_SIGN_POST_TYPE_B = "WZG_B";	
	public static final String SCS_CFG_SIGN_POST_TYPE_C = "WZG_C";	
	public static final String SCS_CFG_SIGN_POST_TYPE_D = "WZG_D";	
	public static final String SCS_CFG_SIGN_POST_TYPE_E = "WZG_E";	
	public static final String SCS_CFG_SIGN_POST_TYPE_ZA = "WZG_ZA";	
	public static final String SCS_CFG_SIGN_POST_TYPE_TEXT = "TEXT";	
	public static final String SCS_CFG_SIGN_POST_TYPE_LSA = "LSA";	
	public static final String SCS_CFG_SIGN_POST_TYPE_DLZ = "DLZ";	
	
	
	private static Map< String, DisplayPartType > wzgType2DisplayPartType = new TreeMap<>();
	
	static
	{
		wzgType2DisplayPartType.put( SCS_CFG_SIGN_POST_TYPE_A_LOWER, DisplayPartType.ASign );
		wzgType2DisplayPartType.put( SCS_CFG_SIGN_POST_TYPE_B_LOWER, DisplayPartType.BSign );
		wzgType2DisplayPartType.put( SCS_CFG_SIGN_POST_TYPE_C_LOWER, DisplayPartType.CSign );
		wzgType2DisplayPartType.put( SCS_CFG_SIGN_POST_TYPE_D_LOWER, DisplayPartType.DSign );
		wzgType2DisplayPartType.put( SCS_CFG_SIGN_POST_TYPE_E_LOWER, DisplayPartType.ESign );
		wzgType2DisplayPartType.put( SCS_CFG_SIGN_POST_TYPE_ZA_LOWER, DisplayPartType.SSign );

		wzgType2DisplayPartType.put( SCS_CFG_SIGN_POST_TYPE_A, DisplayPartType.ASign );
		wzgType2DisplayPartType.put( SCS_CFG_SIGN_POST_TYPE_B, DisplayPartType.BSign );
		wzgType2DisplayPartType.put( SCS_CFG_SIGN_POST_TYPE_C, DisplayPartType.CSign );
		wzgType2DisplayPartType.put( SCS_CFG_SIGN_POST_TYPE_D, DisplayPartType.DSign );
		wzgType2DisplayPartType.put( SCS_CFG_SIGN_POST_TYPE_E, DisplayPartType.ESign );
		wzgType2DisplayPartType.put( SCS_CFG_SIGN_POST_TYPE_ZA, DisplayPartType.SSign );
		wzgType2DisplayPartType.put( SCS_CFG_SIGN_POST_TYPE_TEXT, DisplayPartType.Text  );
	}
	
	private static Map< String, String > wvzType2WzgType = new TreeMap<>();
	
	static
	{
		wvzType2WzgType.put( "A", SCS_CFG_SIGN_POST_TYPE_A );
		wvzType2WzgType.put( "B", SCS_CFG_SIGN_POST_TYPE_B );
		wvzType2WzgType.put( "C", SCS_CFG_SIGN_POST_TYPE_C );
		wvzType2WzgType.put( "D", SCS_CFG_SIGN_POST_TYPE_D );
		wvzType2WzgType.put( "E", SCS_CFG_SIGN_POST_TYPE_E );
	}


	
	@Autowired
	private ConfigServiceSBANonSite configService;
	private Set<String> siteAqs;
	
	
	/**
	 * 
	 * Constructor 
	 * 
	 * @param configService 	(VMIS2) ConfigSiteConfigService implementation
	 * @param siteAqs			
	 */
	public ConfigManagerSBANonSite( ConfigServiceSBANonSite configService, List<String> siteAqs ) {
		this.configService = configService;
		this.siteAqs = new HashSet<>( siteAqs );
	}
	
	
	/**
	 * 
	 * Connecting
	 * 
	 * @return			true
	 */
	public boolean connect()
	{
		// No explicit connecting required
		return true;
	}
	
	private static List<LaneEnum> toLanes( String cfgLane )
	{
		List<LaneEnum> result = new ArrayList<>();
		
		switch( cfgLane )
		{
			case SCS_CFG_ATTR_LANE_F2:
				result.add( LaneEnum.LANE_2 );
				break;
			case SCS_CFG_ATTR_LANE_F3:
				result.add( LaneEnum.LANE_3 );
				break;
			case SCS_CFG_ATTR_LANE_F4:
				result.add( LaneEnum.LANE_4 );
				break;
			case SCS_CFG_ATTR_LANE_F5:
				result.add( LaneEnum.LANE_5 );
				break;
			case SCS_CFG_ATTR_LANE_F6:
				result.add( LaneEnum.LANE_6 );
				break;
			case SCS_CFG_ATTR_LANE_F7:
				result.add( LaneEnum.LANE_7 );
				break;
			case SCS_CFG_ATTR_LANE_F8:
				result.add( LaneEnum.LANE_8 );
				break;
			case SCS_CFG_ATTR_LANE_F9:
				result.add( LaneEnum.LANE_9 );
				break;
			case SCS_CFG_ATTR_LANE_F1F2:
			case SCS_CFG_ATTR_LANE_F1F2_SIMPLE:
				result.add( LaneEnum.LANE_1 );
				result.add( LaneEnum.LANE_2 );
				break;
			case SCS_CFG_ATTR_LANE_F2F3:
			case SCS_CFG_ATTR_LANE_F2F3_SIMPLE:
				result.add( LaneEnum.LANE_2 );
				result.add( LaneEnum.LANE_3 );
				break;
			case SCS_CFG_ATTR_LANE_F3F4:
			case SCS_CFG_ATTR_LANE_F3F4_SIMPLE:
				result.add( LaneEnum.LANE_3 );
				result.add( LaneEnum.LANE_4 );
				break;
			case SCS_CFG_ATTR_LANE_F4F5:
			case SCS_CFG_ATTR_LANE_F4F5_SIMPLE:
				result.add( LaneEnum.LANE_4 );
				result.add( LaneEnum.LANE_5 );
				break;
			case SCS_CFG_ATTR_LANE_F5F6:
			case SCS_CFG_ATTR_LANE_F5F6_SIMPLE:
				result.add( LaneEnum.LANE_5 );
				result.add( LaneEnum.LANE_6 );
				break;
			case SCS_CFG_ATTR_LANE_F6F7:
			case SCS_CFG_ATTR_LANE_F6F7_SIMPLE:
				result.add( LaneEnum.LANE_6 );
				result.add( LaneEnum.LANE_7 );
				break;
			case SCS_CFG_ATTR_LANE_F7F8:
			case SCS_CFG_ATTR_LANE_F7F8_SIMPLE:
				result.add( LaneEnum.LANE_7 );
				result.add( LaneEnum.LANE_8 );
				break;
			case SCS_CFG_ATTR_LANE_F8F9:
			case SCS_CFG_ATTR_LANE_F8F9_SIMPLE:
				result.add( LaneEnum.LANE_8 );
				result.add( LaneEnum.LANE_9 );
				break;
			case SCS_CFG_ATTR_LANE_AS:
			case SCS_CFG_ATTR_LANE_ABS:
			case SCS_CFG_ATTR_LANE_ABS_LOWER:
				result.add( LaneEnum.HARD_SHOULDER );
				break;
			case SCS_CFG_ATTR_LANE_IS:
			case SCS_CFG_ATTR_LANE_IS_LOWER:
				result.add( LaneEnum.LEFT_LANE );
				break;
			case SCS_CFG_ATTR_LANE_ABSF1:
			case SCS_CFG_ATTR_LANE_ABSF1_LOWER:
				result.add( LaneEnum.HARD_SHOULDER );
				result.add( LaneEnum.LANE_1 );
				break;
			case SCS_CFG_ATTR_LANE_F1:
				result.add( LaneEnum.LANE_1 );
				break;
			default:	
				if( cfgLane != null && !cfgLane.isEmpty() ) {
					LOGGER.warn( "Unknown lane: " + cfgLane );
				}
				break;
		}
		
		return result;
	}
	
	private List<CfgWzg> getWzgs( CfgAq aq ) {
		return aq.getWzgs().getWzgsList();
	}
	
	private String getSignPostType( CfgWzg wzg ) {
		
		int typeNumber = wzg.getTypeValue();
		if( typeNumber == 17 ) {
			return SCS_CFG_SIGN_POST_TYPE_TEXT;	
		} else if( typeNumber == 18 ) {
			return SCS_CFG_SIGN_POST_TYPE_LSA;	
		} else if( typeNumber == 19 ) {
			return SCS_CFG_SIGN_POST_TYPE_DLZ;	
		}
		
		if( ( wzg.getType() != null ) && ( wzg.getType() != WzgType.UNKNOWN ) && ( wzg.getType() !=  WzgType.UNRECOGNIZED ) ) {
			return wzg.getType().name();
		}
		
		LOGGER.warn("No type in configuration for WZG " + wzg.getId() );
		return wvzType2WzgType.get( wzg.getDisplayType().name() );
	}
	
	private static DisplayPartType getDisplayPartType( String type )
	{
		DisplayPartType dpt =  wzgType2DisplayPartType.get( type );
		if( dpt != null )
		{
			return dpt;
		}
		
		return DisplayPartType.SSign;
	}
	
	private String getLanePos( CfgWzg wzg )
	{
		return wzg.getLanePos().getDescription();
	}
	
	private static LocationDescriptorEnum toLocationDescriptor( String cfgLane ) 
	{
		if( cfgLane == null ) {
			return null;
		}
		
		switch( cfgLane )
		{
			case SCS_CFG_ATTR_LANE_AS:
			case SCS_CFG_ATTR_LANE_AS_LOWER:
				return LocationDescriptorEnum.ON_THE_RIGHT;
			case SCS_CFG_ATTR_LANE_IS:
			case SCS_CFG_ATTR_LANE_IS_LOWER:
				return LocationDescriptorEnum.ON_THE_LEFT;
			default:
				return null;
		}
	}
	
	private int getDe( String wzgId ) {
		
		int pos = wzgId.lastIndexOf( "_" );
		
		if( pos != -1 ) {
			String dePart = wzgId.substring( pos );
			pos = dePart.indexOf( "." );
			if( pos != -1 ) {
				dePart = dePart.substring( 0, pos );
				if( NumberUtils.isDigits( dePart ) ) {
					return Integer.parseInt( dePart );
				}
			}
		}
		
		return -1;
	}
	
	@Override
	public List<D2VMSUnit> getD2VMSUnits( VMSTableConfiguration configuration,     // NOSONAR
										  LocationManager locationManager) throws VMSServiceException
	{
		LOGGER.info( "ConfigServiceNRW: getD2VMSUnits()" );
		
		locationManager.init();
		List<D2VMSUnit> units = new ArrayList<>();
		
		List<CfgAq> aqs = configService.getSbaAqs( siteAqs );
		Set<String> lanePoss = new HashSet<>(); 
		
		int noLaneCount = 0;
		int noTypeCount = 0;
		List<String> aqWithoutWzgs = new ArrayList<>();
		List<String> aqWithoutLoc= new ArrayList<>();
		
		for( CfgAq aq : aqs )   // NOSONAR
		{
			String aqId = aq.getId();
			
			D2VMSUnit unit = new D2VMSUnit();
			unit.setInternalId( aqId );
			
			List<CfgWzg> wzgs = getWzgs( aq );
			
			unit.setUnitType( UnitType.AQ );
			
			String locId = configuration.toLocationId( aqId );
			Location location = null;

			D2Location d2Location = locationManager.getD2Location( Util.CFG_WZG_TYPE, 
					                                			   locId,
					                                			   configuration.isAddTmcLocations(),
					                                			   configuration.isAddOpenLRLocations()  );
			if( d2Location == null ) 
			{
				LOGGER.warn( "No location for AQ with ID <" + aqId +  ">" );
				aqWithoutLoc.add( aqId );
				continue;
			}

			unit.setDescription( d2Location.getDescription() );
			location = d2Location.getLocation();
			
			for( CfgWzg wzg : wzgs )    // NOSONAR
			{
				location = copyD2LocationPoint( location );
				
				D2VMSDisplay display = new D2VMSDisplay();
				display.setFunctionalType( FunctionalType.DETAIL );
				
				String wzgId = wzg.getId();
				display.setDescription( wzgId );
				display.setInternalId( wzgId );
				
				display.setDisplayType( de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.DisplayType.WZG ); 
				
				String lp = getLanePos( wzg );
				lanePoss.add( lp );
				
				if( ( lp == null ) || lp.isEmpty() ) {
					LOGGER.warn( "No lane in configuration for WZG " + wzg.getId() ); 
					noLaneCount++;
					continue;
				}
				
				LocationDescriptorEnum ld = toLocationDescriptor( lp );

				List<LaneEnum> lanes = null;
				if( lp != null ) {
					lanes = toLanes( lp );
				}
				
				if( ( lanes == null || lanes.isEmpty() ) && ld == null ) {
					LOGGER.warn( "No lane localization for WZG with ID <" + wzgId + ">" );
					continue;
				}
				
				locationManager.setWzgLanes( aqId, location, lanes, ld );
				
				display.setLocation( location );
				
				D2VMSDisplayPart displayPart = new D2VMSDisplayPart();
				displayPart.setInternalId( wzg.getId() );
				
				String spt = getSignPostType( wzg );
				
				if( spt == null ) {
					LOGGER.warn( "No type for WZG with ID <" + wzgId + ">" );
					noTypeCount++;
					continue;
				}
				
				displayPart.setDisplayPartType( getDisplayPartType( spt ) );
				display.getDisplayParts().add( displayPart );
				
				int de = getDe( wzg.getId() );
				display.setDe( de );
				
				unit.addDisplay( display );
			}
			
			if( unit.getDisplays().isEmpty() ) {
				LOGGER.warn( "No valid WZG for AQ with ID <" + aqId +  ">" );
				aqWithoutWzgs.add( aqId );
				continue;
				
			}
			units.add( unit );
		}
		
		
		LOGGER.warn( "# WZGs without lane: " + noLaneCount );
		LOGGER.warn( "# WZGs without type: " + noTypeCount );
		
		LOGGER.info( "# SBA-AQs ConfigService: " + aqs.size() );
		LOGGER.info( "# AQs without WZG:       " + aqWithoutWzgs.size()  );
		LOGGER.info( "# AQs without Location:  " + aqWithoutLoc.size()  );
		LOGGER.info( "# VMS-Units:             " + units.size() );

		if( !aqWithoutWzgs.isEmpty() ) {
			LOGGER.warn( "" );
			LOGGER.warn( "AQs without WZG:"  );
			for( String aq : aqWithoutWzgs ) {
				LOGGER.warn( "  " + aq );
			}
			LOGGER.warn( "" );
		}
		
		if( !aqWithoutLoc.isEmpty() ) {
			LOGGER.warn( "" );
			LOGGER.warn( "AQs without Location:" );
			for( String aq : aqWithoutLoc ) {
				LOGGER.warn( "  " + aq );
			}
			LOGGER.warn( "" );
		}

		
		return units;
	}
	
	private Point copyD2LocationPoint( Location location ) {
		Point point = (Point)location;

		Point copy = new Point();
		copy.setAlertCPoint( point.getAlertCPoint() );
		copy.setPointByCoordinates( point.getPointByCoordinates() );
		if( point.getPointExtension() != null ) {
			_PointExtensionType pe = new _PointExtensionType();
			pe.setOpenlrExtendedPoint( point.getPointExtension().getOpenlrExtendedPoint() );
			copy.setPointExtension( pe ); 
		}
		
		return copy;
	}
	
}
