package de.heuboe.datex2.vms.service.uz;

import java.util.List;

import de.heuboe.datex2.vms.service.VMSTableConfiguration;
import de.heuboe.datex2.vms.service.VMSServiceException;
import de.heuboe.datex2.vms.service.table.data.D2VMSUnit;


/**
 * 
 * Builds D2VMSUnits from infrastructure/configuration
 * 
 * @author peters
 *
 */
public abstract class ConfigManager 
{
	public static final String CFG_ATTR_TYPE_AQ_TYPE = "AttributeType.AQTyp";
	public static final String CFG_ATTR_TYPE_WZG_TYPE = "AttributeType.WZGTyp";
	public static final String CFG_ATTR_TYPE_LANE = "AttributeType.Fahrstreifenbezug";	
	public static final String CFG_ATTR_TYPE_DE = "AttributeType.NodeNumDe";	
	
	public static final String CFG_ATTR_TYPE_DISPLAY_TYPE = "AttributeType.Anzeigeprinzip";	

	public static final String CFG_ATTR_AQ_WWW = "AQType.WWWQ";
	public static final String CFG_ATTR_AQ_DWISTA = "AQType.DWISTA";

	public static final String CFG_ATTR_DISPLAY_TYPE_A = "DisplayType.A";	
	public static final String CFG_ATTR_DISPLAY_TYPE_B = "DisplayType.B";	
	public static final String CFG_ATTR_DISPLAY_TYPE_C = "DisplayType.C";	
	public static final String CFG_ATTR_DISPLAY_TYPE_D = "DisplayType.D";	
	public static final String CFG_ATTR_DISPLAY_TYPE_E = "DisplayType.E";	

	public static final String CFG_SIGN_POST_TYPE_A = "SignPostType.WzgA";	
	public static final String CFG_SIGN_POST_TYPE_B = "SignPostType.WzgB";	
	public static final String CFG_SIGN_POST_TYPE_C = "SignPostType.WzgC";	
	public static final String CFG_SIGN_POST_TYPE_D = "SignPostType.WzgD";	
	public static final String CFG_SIGN_POST_TYPE_E = "SignPostType.WzgE";	
	public static final String CFG_SIGN_POST_TYPE_ZA = "SignPostType.WzgZA";	
	
	public static final String CFG_ATTR_LANE_AS = "LanePos.AS";	
	public static final String CFG_ATTR_LANE_ABS = "LanePos.AbS";	
	public static final String CFG_ATTR_LANE_ABSF1 = "LanePos.AbSF1";	
	public static final String CFG_ATTR_LANE_F1 = "LanePos.F1";	
	public static final String CFG_ATTR_LANE_F2 = "LanePos.F2";	
	public static final String CFG_ATTR_LANE_F3 = "LanePos.F3";	
	public static final String CFG_ATTR_LANE_F4 = "LanePos.F4";	
	public static final String CFG_ATTR_LANE_F5 = "LanePos.F5";	
	public static final String CFG_ATTR_LANE_F1F2 = "LanePos.F1F2";	
	public static final String CFG_ATTR_LANE_F2F3 = "LanePos.F2F3";	
	public static final String CFG_ATTR_LANE_F3F4 = "LanePos.F3F4";	
	public static final String CFG_ATTR_LANE_F4F5 = "LanePos.F4F5";	
	public static final String CFG_ATTR_LANE_IS = "LanePos.Is";	
	
	/**
	 * 
	 * Builds D2VMSUnits from infrastructure/configuration
	 * 
	 * @param configuration			VMS configuration
	 * @param locationManager		Location manager	
	 * @return						D2VMSUnits			
	 * @throws VMSServiceException	Error
	 */
	public abstract List<D2VMSUnit> getD2VMSUnits( VMSTableConfiguration configuration, 
			   					                   LocationManager locationManager ) throws VMSServiceException;
	
}
