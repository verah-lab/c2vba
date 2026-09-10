package de.heuboe.datex2.vms.service.uz;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import de.heuboe.datex2.vms.service.D2Location;
import de.heuboe.datex2.vms.service.VMSServiceException;
import de.heuboe.datex2.vms.service.VMSTableConfiguration;
import de.heuboe.datex2.vms.service.VMSTableConfiguration.DWiStaMode;
import de.heuboe.datex2.vms.service.VMSTableConfiguration.DWiStaTextArea;
import de.heuboe.datex2.vms.service.VMSTableConfiguration.DWiStaTextAreaLayout;
import de.heuboe.datex2.vms.service.VMSTableConfiguration.WzgPosition;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplay;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.AddressedTrafficFlow;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.DisplayPosition;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.FunctionalType;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplayPart;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplayPart.DisplayPartMode;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplayPart.DisplayPartType;
import de.heuboe.datex2.vms.service.table.data.D2VMSUnit;
import de.heuboe.datex2.vms.service.table.data.D2VMSUnit.UnitType;
import de.heuboe.datex2.vms.service.uz.SiteConfigService.DWiStaLayoutMapping.DWiStaTextGroup;
import de.heuboe.log.Logger;
import de.heuboe.nrw.guisvc.sitecfg.data.SiteConfigError;
import de.heuboe.nrw.ims.SiteConfigServiceIface;
import de.heuboe.sitesconfig.CfgDevice;
import de.heuboe.sitesconfig.CfgQ;
import de.heuboe.sitesconfig.CfgQs;
import de.heuboe.sitesconfig.QKind;
import eu.datex2.schema._2._2_0.LaneEnum;
import eu.datex2.schema._2._2_0.Location;
import eu.datex2.schema._2._2_0.LocationDescriptorEnum;
import eu.datex2.schema._2._2_0.Point;
import eu.datex2.schema._2._2_0._PointExtensionType;


/**
 * 
 * SiteConfigService implementation of ConfigManager
 * 
 * @author peters
 *
 */
public class SiteConfigService extends ConfigManager
{
	private enum ItemType {
		PIKT,
		TEXT,
	}
	
	static class DWiStaLayoutMapping {
		
		static class DWiStaTextGroup {
			
			private int textAreaId;
			private DisplayPosition position;
			private List<CfgDevice> textWzgs = new ArrayList<>();
			private List<CfgDevice> imageWzgs = new ArrayList<>();
			
			private Map<WzgPosition,DisplayPosition> imagePositions = new HashMap<>();
			
			public int getTextAreaId()
			{
				return textAreaId;
			}
			public void setTextAreaId(int textAreaId)
			{
				this.textAreaId = textAreaId;
			}
			public List<CfgDevice> getTextWzgs() throws VMSServiceException
			{
				Map<WzgPosition,CfgDevice> sorted = new TreeMap<>();
				for( CfgDevice twzg : textWzgs ) {
					sorted.put( toWzgPosition(twzg), twzg );
				}
				return new ArrayList<>( sorted.values() );
			}
			
			/**
			 * 
			 * Adds text WZG
			 * 
			 * @param textWzg	Text WZG
			 */
			public void addTextWzg( CfgDevice textWzg )
			{
				this.textWzgs.add( textWzg );
			}
			
			public List<CfgDevice> getImageWzgs() throws VMSServiceException
			{
				Map<WzgPosition,CfgDevice> sorted = new TreeMap<>();
				for( CfgDevice iwzg : imageWzgs ) {
					sorted.put( toWzgPosition(iwzg), iwzg );
				}
				return new ArrayList<>( sorted.values() );
			}
			
			/**
			 * 
			 * Adds image WZG
			 * 
			 * @param imageWzg				Image WZG
			 * @param dp					Display position	
			 * @throws VMSServiceException	Error	
			 */
			public void addImageWzg( CfgDevice imageWzg, DisplayPosition dp ) throws VMSServiceException
			{
				this.imageWzgs.add( imageWzg );
				WzgPosition pos = toWzgPosition( imageWzg );
				this.imagePositions.put( pos, dp );
			}
			public DisplayPosition getPosition()
			{
				return position;
			}
			public void setPosition(DisplayPosition position)
			{
				this.position = position;
			}
			
			/**
			 * 
			 * Returns image position
			 * 
			 * @param imageWzg				WZG
			 * @return						Relative Position on display	
			 * @throws VMSServiceException	Error
			 */
			public DisplayPosition getImagePosition( CfgDevice imageWzg ) throws VMSServiceException {
				WzgPosition pos = toWzgPosition( imageWzg );
				return imagePositions.get( pos );
			}
		}
		
		private Map<Integer,DWiStaTextGroup> textGroups = new HashMap<>();
		
		/**
		 * 
		 * Adds DWiStaText text group
		 * 
		 * @param textAreaId	Text area ID
		 * @param textGroup		Text group
		 */
		public void addTextGroup( int textAreaId, DWiStaTextGroup textGroup ) {
			textGroups.put( textAreaId, textGroup );	
		}
		
		public List<DWiStaTextGroup> getTextGroups() {
			return new ArrayList<>( textGroups.values() );
		}
	}

	private static final Logger LOGGER = Logger.getLogger( SiteConfigService.class );
	private static final String TEMP = "temp/";
	
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
	private static final String SCS_CFG_ATTR_LANE_F1F2_SIMPLE = "F1F2";	
	private static final String SCS_CFG_ATTR_LANE_F2F3_SIMPLE = "F2F3";	
	private static final String SCS_CFG_ATTR_LANE_F3F4_SIMPLE = "F3F4";	
	private static final String SCS_CFG_ATTR_LANE_F4F5_SIMPLE = "F4F5";	
	private static final String SCS_CFG_ATTR_LANE_F1F2 = "F2/F1";	
	private static final String SCS_CFG_ATTR_LANE_F2F3 = "F3/F2";	
	private static final String SCS_CFG_ATTR_LANE_F3F4 = "F4/F3";	
	private static final String SCS_CFG_ATTR_LANE_F4F5 = "F5/F4";	
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
	public static final String SCS_CFG_SIGN_POST_TYPE_PIKT = "PIKT";	
	public static final String SCS_CFG_SIGN_POST_TYPE_TEXT = "TEXT";	
	
	
	
	
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
		wzgType2DisplayPartType.put( SCS_CFG_SIGN_POST_TYPE_PIKT, DisplayPartType.Pikt  );
		wzgType2DisplayPartType.put( SCS_CFG_SIGN_POST_TYPE_TEXT, DisplayPartType.Text  );
	}
	
	private static Map< String, String > wvzType2WzgType = new TreeMap<>();
	
	static
	{
		wvzType2WzgType.put( "WVZA", SCS_CFG_SIGN_POST_TYPE_A );
		wvzType2WzgType.put( "WVZB", SCS_CFG_SIGN_POST_TYPE_B );
		wvzType2WzgType.put( "WVZC", SCS_CFG_SIGN_POST_TYPE_C );
		wvzType2WzgType.put( "WVZD", SCS_CFG_SIGN_POST_TYPE_D );
		wvzType2WzgType.put( "WVZE", SCS_CFG_SIGN_POST_TYPE_E );
		wvzType2WzgType.put( "WVZZA", SCS_CFG_SIGN_POST_TYPE_ZA );
	}

	
	private SiteConfigServiceIface siteCfgService;
	
	/**
	 * 
	 * Constructor 
	 * 
	 * @param siteCfgService SiteConfigService implementation
	 */
	public SiteConfigService( SiteConfigServiceIface siteCfgService ) {
		this.siteCfgService = siteCfgService;
	}
	
	/**
	 * 
	 * Creates SiteConfigService and connects
	 * 
	 * @return			ok
	 */
	public boolean connect()
	{
		return siteCfgService.connect();
	}
	
	private static Integer toLanePos( String cfgLane ) throws VMSServiceException
	{
		switch( cfgLane )
		{
			case SCS_CFG_ATTR_LANE_AS:		
			case SCS_CFG_ATTR_LANE_AS_LOWER:		
				return 13;
			case SCS_CFG_ATTR_LANE_ABS:
			case SCS_CFG_ATTR_LANE_ABS_LOWER:
				return 12;
			case SCS_CFG_ATTR_LANE_ABSF1:
			case SCS_CFG_ATTR_LANE_ABSF1_LOWER:
				return 11;

		    case SCS_CFG_ATTR_LANE_F1:
				return 10;
			case SCS_CFG_ATTR_LANE_F1F2:
			case SCS_CFG_ATTR_LANE_F1F2_SIMPLE:
				return 9;
			case SCS_CFG_ATTR_LANE_F2:
				return 8;
			case SCS_CFG_ATTR_LANE_F2F3:
			case SCS_CFG_ATTR_LANE_F2F3_SIMPLE:
				return 7;
			case SCS_CFG_ATTR_LANE_F3:
				return 6;
			case SCS_CFG_ATTR_LANE_F3F4:
			case SCS_CFG_ATTR_LANE_F3F4_SIMPLE:
				return 5;
			case SCS_CFG_ATTR_LANE_F4:
				return 4;
			case SCS_CFG_ATTR_LANE_F4F5:
			case SCS_CFG_ATTR_LANE_F4F5_SIMPLE:
				return 3;
			case SCS_CFG_ATTR_LANE_F5:
				return 2;
				
			case SCS_CFG_ATTR_LANE_IS:
			case SCS_CFG_ATTR_LANE_IS_LOWER:
				return 1;
				
			default:
				throw new VMSServiceException( VMSServiceException.VMS_SVC_ERR_CFG, "Unknown lane position <" + cfgLane + ">!" ); 
		}
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
			case SCS_CFG_ATTR_LANE_AS:
			case SCS_CFG_ATTR_LANE_ABS:
			case SCS_CFG_ATTR_LANE_ABS_LOWER:
				result.add( LaneEnum.HARD_SHOULDER );
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
				//result.add( LaneEnum.LANE_1 );
				break;
		}
		
		return result;
	}
	
	private static WzgPosition toWzgPosition( CfgDevice wzg ) throws VMSServiceException {
		
		if(  wzg.getType().equals( ItemType.TEXT.name() ) 
				 || 
				 ( wzg.getType().equals( ItemType.PIKT.name() ) )
			  ) {
			WzgPosition p = new WzgPosition();
			Integer lane = toLanePos( wzg.getLane() );
			p.setLane( lane );
			p.setRow( 20 - wzg.getVPos() ); 
			
			return p;
		}

		return null;
	}

	
	private List<CfgQ> getAqs( List<String> aqIds ) throws SiteConfigError {
		return siteCfgService.getAqs( aqIds );
	}
	
	private List<CfgQs> getSites( Set<String> siteIds ) throws SiteConfigError {
		
		List<CfgQs> siteItems = new ArrayList<>();
		
		for( String siteId : siteIds ) {
		
			siteItems.add( siteCfgService.getSiteAqs( siteId ) );
		}
		
		return siteItems; 
	}
	
	private List<CfgDevice> getWzgs( CfgQ aq ) {
		return aq.getDevice();
	}
	
	private boolean isDWiSta( CfgQ aq ) {
		if( aq.getKind() == null ) {
			return false;
		}
		return aq.getKind().name().startsWith( QKind.DWISTA.name() ); 
	}
	
	private boolean isWWWSite( String siteId ) {
		return siteId.contains( "WWW" );
	}
	
	private boolean isWWWPublication( List<String> siteIds ) {
		for( String siteId : siteIds ) {
			if( isWWWSite( siteId ) ) {
				return true;
			}
		}
		
		return false;
	}

	private boolean isWWW( CfgQ aq, List<String> siteIds ) {  // NOSONAR
		
		return isWWWPublication( siteIds );
	}
	
	private String getSignPostType( CfgDevice wzg ) {
		
		String t = wzg.getType();
		if( t.equals( SCS_CFG_SIGN_POST_TYPE_PIKT ) ) { 
			return SCS_CFG_SIGN_POST_TYPE_PIKT;
		}
		if( t.equals( SCS_CFG_SIGN_POST_TYPE_TEXT ) ) { 
			return SCS_CFG_SIGN_POST_TYPE_TEXT;
		}
		
		if( wzg.getWzgType() != null ) {
			return wzg.getWzgType();
		}
		
		return wvzType2WzgType.get( wzg.getType() );
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
	
	private boolean wzgIsSingleUnit( VMSTableConfiguration configuration, CfgQ aq, List<String> siteIds ) 
	{
		if (!isWWW(aq, siteIds))  			// NOSONAR
		{
			if (isDWiSta(aq))		// NOSONAR
			{

				if ((configuration.getdWiStaMode() == DWiStaMode.SINGLE_WZG))
				{
					return true;
				}

				if ((configuration.getdWiStaMode() == DWiStaMode.COMPLETE_TEXTAREA))
				{
					return false;
				}
			}
		}

		return true;
	}
	
	private String getLanePos( CfgDevice wzg )
	{
		return wzg.getLane();
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
	
	
	private DWiStaLayoutMapping getDWistaLayout( DWiStaTextAreaLayout dWiStaLayout,      // NOSONAR
			                                     List<CfgDevice> wzgs ) throws VMSServiceException {

		List<WzgPosition> textPositions = new ArrayList<>(); 
		List<WzgPosition> imagePostions = new ArrayList<>(); 	
		Map<WzgPosition, CfgDevice> wzgMap = new HashMap<>();
		
		for( CfgDevice wzg: wzgs ) {
			
			WzgPosition p = toWzgPosition( wzg );
			
			if( wzg.getType().equals( ItemType.PIKT.name() ) ) {
				imagePostions.add( p );
				wzgMap.put(p, wzg );
			} else if( wzg.getType().equals( ItemType.TEXT.name() ) ) {
				textPositions.add( p );
				wzgMap.put(p, wzg );
			} else {
				LOGGER.error( "DWiSta: non PIKT or -TEXT Wzg <" + wzg.getId() + ">!" );
			}
		}
		
		
		Map< Integer, List<WzgPosition> > lane2TextAreas = new TreeMap<>();
		Map< Integer, List<WzgPosition> > lane2ImageAreas = new TreeMap<>();

		for( WzgPosition tp : textPositions ) {
			lane2TextAreas.computeIfAbsent( tp.getLane(), k -> new ArrayList<>() ).add( tp );
		}
		for( WzgPosition ip : imagePostions ) {
			lane2ImageAreas.computeIfAbsent( ip.getLane(), k -> new ArrayList<>() ).add( ip );
		}
		
		List<DWiStaTextArea> textAreas = dWiStaLayout.getTextAreas();
		
		if( lane2TextAreas.size() != textAreas.size() ) {
			return null;
		}
		

		Iterator< List<WzgPosition> > laneIter = lane2TextAreas.values().iterator();
		Iterator<DWiStaTextArea> taIter = textAreas.iterator();
		
		while( laneIter.hasNext() ) {
			List<WzgPosition> lPos = laneIter.next();
			DWiStaTextArea ta = taIter.next();
		
			if( lPos.size() != ta.getNumLines() ) {
				return null;
			}
		}

		// Vereinfachung: 
		//	  Für die erste TextArea gibt es eine Image-Gruppe links davon  
		//	  Für die letzte TextArea gibt es eine Image-Gruppe rechts davon 
		
		final DWiStaTextGroup textGroup1 = new DWiStaTextGroup();
		final DWiStaTextGroup textGroupN = new DWiStaTextGroup();
		
		{ // NOSONAR
			boolean firstTAHasImages = false;
			DWiStaTextArea ta1 = textAreas.get(0);
			int lane1 = lane2TextAreas.keySet().iterator().next();
			List<WzgPosition> ipos = null;
			
			for( Entry< Integer, List<WzgPosition> > entry : lane2ImageAreas.entrySet() ) {   // NOSONAR
				int l = entry.getKey();
				if( l >= lane1 ) {
					break;
				}
				ipos = entry.getValue();
				if( ta1.getImages().size() == ipos.size() ) {
					firstTAHasImages = true;
					break;
				}
			}
			
			if( !firstTAHasImages ) {
				return null;
			}
			
			textGroup1.setTextAreaId( ta1.getId() );
			List<WzgPosition> poss = lane2TextAreas.values().iterator().next();
			for( WzgPosition pos : poss ) {
				textGroup1.addTextWzg( wzgMap.get( pos ) );
			}
			
			textGroup1.setPosition( ta1.getPosition() );
			
			for( WzgPosition i : ipos ) {
				textGroup1.addImageWzg( wzgMap.get(i), DisplayPosition.LEFT );
			}
		}

		if( textAreas.size() > 1 ) {
		
			boolean lastTAHasImages = false;
			DWiStaTextArea taN = textAreas.get( textAreas.size() - 1 );
			int laneN = 0;
			List<WzgPosition> ipos = null;
			
			Iterator<Integer> iter = lane2TextAreas.keySet().iterator();
			while( iter.hasNext() ) {
				laneN = iter.next();
			}

			for( Entry< Integer, List<WzgPosition> > entry : lane2ImageAreas.entrySet() ) {  // NOSONAR
				int l = entry.getKey();
				if( l <= laneN ) {
					continue;
				}
				ipos = entry.getValue();
				if( taN.getImages().size() == ipos.size() ) {
					lastTAHasImages = true;
					break;
				}
			}
			
			if( !lastTAHasImages ) {
				return null;
			}
			
			textGroupN.setTextAreaId( taN.getId() );
			
			List<WzgPosition> poss = new ArrayList<>();
			Iterator< List<WzgPosition> > iterList = lane2TextAreas.values().iterator();
			while( iterList.hasNext() ) {
				poss = iterList.next();
			}
			for( WzgPosition pos : poss ) {
				textGroupN.addTextWzg( wzgMap.get( pos ) );
			}
			
			textGroupN.setPosition( taN.getPosition() );
			
			for( WzgPosition i : ipos ) {
				textGroupN.addImageWzg( wzgMap.get(i), DisplayPosition.RIGHT );
			}
		}
		
		DWiStaLayoutMapping mapping = new DWiStaLayoutMapping();
		mapping.addTextGroup( textGroup1.getTextAreaId(), textGroup1 );
		mapping.addTextGroup( textGroupN.getTextAreaId(), textGroupN );
		
		return mapping;	
	}
	
	private DWiStaLayoutMapping getDWistaLayout( VMSTableConfiguration configuration, 
            									 List<CfgDevice> wzgs ) throws VMSServiceException {

		for( DWiStaTextAreaLayout ta : configuration.getDWiStaTextAreaLayouts() ) {
		
			DWiStaLayoutMapping mapping = getDWistaLayout( ta , wzgs);
			if( mapping != null ) {
				return mapping;
			}
		}
		
		return null;
	}

	private Point copyD2LocationPoint( Location location ) {
		Point point = (Point)location;

		Point copy = new Point();
		copy.setAlertCPoint( point.getAlertCPoint() );
		copy.setPointByCoordinates( point.getPointByCoordinates() );
		_PointExtensionType pe = new _PointExtensionType();
		pe.setOpenlrExtendedPoint( point.getPointExtension().getOpenlrExtendedPoint() );
		copy.setPointExtension( pe ); 
		
		return copy;
	}
	
	@Override
	public List<D2VMSUnit> getD2VMSUnits( VMSTableConfiguration configuration,     // NOSONAR
										  LocationManager locationManager) throws VMSServiceException
	{
		LOGGER.info( "ConfigServiceNRW: getD2VMSUnits()" );
		
		Map<String,Location> aqId2Location = new HashMap<>();
		
		Set<String> filterSites = null;
		Set<String> filterAqs = null;
		
		List<String> filterSiteList = configuration.getSbaIds();
		List<String> filterAqList = configuration.toAqIds();
		if( ( filterSiteList != null ) && !filterSiteList.isEmpty() )
		{
			filterSites = new HashSet<>( filterSiteList );
		}
		
		if( ( filterAqList != null ) && !filterAqList.isEmpty() )
		{
			filterAqs = new HashSet<>( filterAqList );
		}
		
		
		locationManager.init();
		List<D2VMSUnit> units = new ArrayList<>();
		
		List<CfgQ> aqs = new ArrayList<>();
		try {
		
			if( ( filterSites == null ) || ( filterSites.isEmpty() ) ) {
				
				List<String> lst = Arrays.asList();
				if( filterAqs != null ) {
					lst = filterAqs.stream().collect( Collectors.toList() ); 
					aqs.addAll( getAqs( lst ) );
				}	
			} else {
				List<CfgQs> siteAqss = getSites( filterSites );
				for( CfgQs siteAqs : siteAqss )
				{
					aqs.addAll( siteAqs.getQ() );
				}
			}	
		} catch( SiteConfigError ex ) {
			LOGGER.error( "SiteConfigService::getD2VMSUnits(), error retrieving sites: " + ex.toString() );
			throw new VMSServiceException( "SiteConfigError", ex );
		}
		
		for( CfgQ aq : aqs )   // NOSONAR
		{
			String aqId = aq.getId();
			
			if( ( filterAqs == null ) || ( filterAqs.contains( aqId )) ) {
			
				D2VMSUnit unit = new D2VMSUnit();
				unit.setInternalId( aqId );
				
				List<CfgDevice> wzgs = getWzgs( aq );
				
				boolean complete = isWWW( aq, filterSiteList ) 
								   || 
								   ( 
									  isDWiSta( aq ) 
									  && 
									  (
											  ( configuration.getdWiStaMode() == DWiStaMode.COMPLETE ) 
											  ||
											  ( configuration.getdWiStaMode() == DWiStaMode.COMPLETE_TEXTAREA )
									  )
								    );
				
				DWiStaLayoutMapping dWiStaMapping = null;
				if( isDWiSta( aq ) ) {
					dWiStaMapping = getDWistaLayout( configuration, wzgs );
					
					if( dWiStaMapping == null ) {
						LOGGER.error( "Cannot map layout of dWiSta with ID <" + aqId + ">" );
						continue;
					}
				}
				
				if( isWWW( aq, filterSiteList ) ) {
					unit.setUnitType( UnitType.WWW );
					unit.setDescription( "WWW" );
				} else if( isDWiSta( aq ) ) {
					unit.setUnitType( UnitType.DWISTA );
					unit.setDescription( "dWiSta" );
				} else {
					unit.setUnitType( UnitType.AQ );
					unit.setDescription( "AQ" );
				}
				
				if( complete ) 
				{
					D2VMSDisplay display = new D2VMSDisplay();
					display.setFunctionalType( FunctionalType.OVERVIEW );
					
					if( isDWiSta( aq ) ) {
						display.setDisplayType( de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.DisplayType.DWISTA );
					} else {
						display.setDisplayType( de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.DisplayType.WWW );
					}
					
					display.setDescription( aqId );
					display.setInternalId( aqId );
					
					String locId = configuration.toLocationId( aqId );
					Location location = locationManager.getLocation( Util.CFG_AQ_TYPE, 
							                                         locId,
																	 configuration.isAddTmcLocations(),
																	 configuration.isAddOpenLRLocations() );
					
					if( location == null ) 
					{
						LOGGER.error( "No location for AQ with ID <" + aqId +  ">" );
						continue;
					}
					
					List<LaneEnum> lanes = Arrays.asList( LaneEnum.ALL_LANES_COMPLETE_CARRIAGEWAY );
					locationManager.setWzgLanes( aqId, location, lanes, null );
	
					display.setLocation( location );
					
					for( CfgDevice wzg : wzgs )
					{
						D2VMSDisplayPart displayPart = new D2VMSDisplayPart();
						displayPart.setInternalId( wzg.getId() );
						
						String spt = getSignPostType( wzg );
						displayPart.setDisplayPartType( getDisplayPartType( spt ) );
						displayPart.setDe( wzg.getDe() );
						display.getDisplayParts().add( displayPart );
					}
					
					unit.addDisplay( display );
				}
				
				if( isDWiSta( aq ) && ( configuration.getdWiStaMode() == DWiStaMode.COMPLETE_TEXTAREA )  )
				{
					// WZGs einer Textarea werden zu einem D2VMSDisplay zusammengefasst
					
					List<DWiStaTextGroup> tgs = dWiStaMapping.getTextGroups();
					int index = 1;
					for( DWiStaTextGroup tg : tgs ) {
						
						List<CfgDevice> taWzgs = tg.getTextWzgs(); 
						
						D2VMSDisplay display = new D2VMSDisplay();
						display.setFunctionalType( FunctionalType.FUNCTIONAL_DETAIL );
						
						if( tg.getPosition() == DisplayPosition.LEFT ) {
							display.setAddressedTrafficFlow( AddressedTrafficFlow.MAIN_CARRIAGEWAY_FOLLOWING_TRAFFIC );
						} else {
							display.setAddressedTrafficFlow( AddressedTrafficFlow.RIGHT_DIRECTION_FOLLOWING_TRAFFIC );
						}
						String descr = "dWiSta Textbereich_" + index;
						String id = descr + " " + aqId;
						
						display.setDescription( descr );
						display.setInternalId( id );
						display.setDisplayPosition( tg.getPosition() );
						display.setDisplayType( de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.DisplayType.DWISTA_TXT_GRP ); 
						
						String locId = configuration.toLocationId( aqId );
						Location location = locationManager.getLocation( Util.CFG_WZG_TYPE, 
								                                         locId,
								 										 configuration.isAddTmcLocations(),
								 										 configuration.isAddOpenLRLocations()  );
						
						
						List<LaneEnum> lanes = Arrays.asList( LaneEnum.ALL_LANES_COMPLETE_CARRIAGEWAY );
						locationManager.setWzgLanes( aqId, location, lanes, null );
						display.setLocation( location );
						
						for( CfgDevice taWzg : taWzgs ) {
							D2VMSDisplayPart displayPart = new D2VMSDisplayPart();
							displayPart.setInternalId( taWzg.getId() );
							displayPart.setDisplayPartMode( DisplayPartMode.TEXT );
							String spt = getSignPostType( taWzg );
							displayPart.setDisplayPartType( getDisplayPartType( spt ) );
							displayPart.setDe( taWzg.getDe() );
							display.getDisplayParts().add( displayPart );
						}
						
						int i = 0;
						List<CfgDevice> images = tg.getImageWzgs();
						for( CfgDevice image : images ) {
							
							D2VMSDisplayPart displayPart = new D2VMSDisplayPart();
							displayPart.setInternalId( image.getId() );
							displayPart.setPictogramIndex( i++ );
							displayPart.setDisplayPartMode( DisplayPartMode.IMAGE );
							displayPart.setDisplayPosition( tg.getImagePosition(image) );
							String spt = getSignPostType( image );
							displayPart.setDisplayPartType( getDisplayPartType( spt ) );
							displayPart.setDe( image.getDe() );
							display.getDisplayParts().add( displayPart );
						}
						
						unit.addDisplay( display );
						
						index++;
					}
				}
				
				
				if( !complete )
				{
					boolean hasLocation = true;
					for( CfgDevice wzg : wzgs )    // NOSONAR
					{
						if( wzgIsSingleUnit( configuration, aq, filterSiteList ) ) {
							
							D2VMSDisplay display = new D2VMSDisplay();
							display.setFunctionalType( FunctionalType.DETAIL );
							
							String wzgId = wzg.getId();
							display.setDescription( wzgId );
							display.setInternalId( wzgId );
							
							display.setDisplayType( de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.DisplayType.WZG ); 
							
							String locId = configuration.toLocationId( aqId );
							String partId = siteCfgService.getPartId( locId );
							
							Location location = aqId2Location.get( aqId );
							if( ( location == null ) && hasLocation ) {
								
								List<D2Location> d2Locations = locationManager.getLocations( Util.CFG_WZG_TYPE, 
		                                													 new HashSet<>( Arrays.asList( partId ) ),
		                                													 configuration.isAddTmcLocations(),
		                                													 configuration.isAddOpenLRLocations()  );

								if( ( d2Locations != null ) && !d2Locations.isEmpty() ) {
									location = d2Locations.get(0).getLocation();
								}
								
								if( location != null ) { 
									unit.setDescription( d2Locations.get(0).getDescription() );
									aqId2Location.put( aqId, location );
								} else {
									hasLocation = false;
								}
							} else {
								location = copyD2LocationPoint( location );
							}
							
							if( location == null ) 
							{
								LOGGER.error( "No location for AQ with ID <" + aqId +  ">" );
								continue;
							}
							
							String lp = getLanePos( wzg );
							
							LocationDescriptorEnum ld = toLocationDescriptor( lp );

							List<LaneEnum> lanes = null;
							if( lp != null ) {
								lanes = toLanes( lp );
							}
							
							if( isDWiSta( aq ) ) {
								lanes = Arrays.asList( LaneEnum.ALL_LANES_COMPLETE_CARRIAGEWAY );
								ld = null;
							} else if( ( lanes == null || lanes.isEmpty() ) && ld == null ) {
								LOGGER.error( "No lane localization for WZG with ID <" + wzgId + ">" );
								continue;
							}
							
							locationManager.setWzgLanes( aqId, location, lanes, ld );
							
							display.setLocation( location );
							
							D2VMSDisplayPart displayPart = new D2VMSDisplayPart();
							displayPart.setInternalId( wzg.getId() );
							
							String spt = getSignPostType( wzg );
							displayPart.setDisplayPartType( getDisplayPartType( spt ) );
							display.getDisplayParts().add( displayPart );
							display.setDe( wzg.getDe() );
							
							unit.addDisplay( display );
						}
					}
				}
				
				units.add( unit );
			}
		}
		return units;
	}

	/**
	 * 
	 * Writes AQs and WZGs Json files.
	 * Used to compare GRPC and SOAP versions of infrastructure
	 * 
	 * @param aqs
	 * @param filterAqs
	 */
	@SuppressWarnings("unused")
	private void writeInfraObjects( List<CfgQ> aqs, Set<String> filterAqs ) {
	    ObjectMapper objectMapper = new ObjectMapper();
	    ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
	    
	    String subDir = "wzgSoap";
	    if( siteCfgService instanceof SiteConfigServiceGrpc ) {
	    	subDir = "wzgGrpc";
	    }
	    
	    (new File( TEMP  + subDir )).mkdirs();
	    
		for( CfgQ aq : aqs )   // NOSONAR
		{
			if( ( filterAqs == null ) || ( filterAqs.contains( aq.getId() )) ) {
			
	            try {
		            String aqStr = objectWriter.writeValueAsString( aq );
					FileUtils.write( new File( TEMP + subDir + "/AQ_" + aq.getId() + ".txt" ), aqStr );
					List<CfgDevice> wzgs = getWzgs( aq );
					for( CfgDevice wzg : wzgs ) {
			            String wzgStr = objectWriter.writeValueAsString( wzg );
						FileUtils.write( new File( TEMP + subDir + '/' + aq.getId() + "__" + wzg.getId() + ".txt" ), wzgStr );
					}
				} catch (IOException e) {
					//
				}
			}
		}
		
		
	}
}
