package de.heuboe.datex2.mst.builder.writer;

import java.util.List;
import java.util.Set;

import de.heuboe.datex2.base.data.D2CoordTrans;
import de.heuboe.datex2.base.data.D2Coordinate;
import de.heuboe.datex2.base.util.D2BaseUtil;
import de.heuboe.datex2.exception.D2ExceptionBase;
import de.heuboe.datex2.measure.D2MeasureLoc;
import de.heuboe.datex2.mst.builder.D2MSTConf;
import de.heuboe.datex2.mst.builder.D2MSTPubLaneType;
import de.heuboe.datex2.mst.builder.D2MSTPubXMLMan;


/**
 * 
 * Erstellt den MST-XML-Content (als String) der Location einer MeasurementSite
 * 
 * @author peters
 *
 */
public class D2MSTPubLocWriter 
{
	private static final long THOUSAND = 1000L;
	
	public static String getContent( D2MSTConf conf,
									 D2MeasureLoc loc, 
									 String carriageway,
									 Set<String> lanes )
			throws D2ExceptionBase
	{
		String cw = carriageway;
		
		if( ( cw == null ) || cw.isEmpty() ) {
			cw = "mainCarriageway";
		}
		
		String locContent = "";
		
		int lrmi = 4;

		int sli = loc.getRdsLocCodeSL();
		int pli = loc.getRdsLocCodePL(); 
		
		if( ( sli == 0 ) || ( sli == pli ) ) {
			lrmi = 3;
			if( loc.getRdsPLDist() <= 0 ) {
				lrmi = 1;
			}
		}
		
        String dbid = loc.getRdsLocTblRef();		
    	List<D2Coordinate> coors = loc.getCoordinates();
        
    	if( 
            ( dbid.length() == 0 ) 				// no AlertC location
        	|| 
            conf.getPreferredLocEncoding().equals( D2MSTConf.PREFERRED_ENCODING_COORDINATE )
          )	
        {
        	if( ( coors != null ) && ( coors.size() > 1 ) )
        		locContent = itineraryLocation;
        	else	
        		locContent = singlePoint;
        }
		else
		{
			if( lrmi == 3 )
			{
				locContent = pointOffs;
			}
			else if( lrmi == 1 )
			{
				locContent = point;
			}
			else
			{
				locContent = linearOffs;
			}
		}
		
		locContent = replaceParams( conf, locContent, loc, cw, lanes, lrmi );
		
		return locContent;
	}	
	
	public static D2MSTPubLaneType convertLaneCode( char lc )
	{
		if( lc == 'A' )
			return D2MSTPubLaneType.allLanesCompleteCarriageway;
		if( lc == 'H' )
			return D2MSTPubLaneType.hardShoulder;
		if( lc == '1' )
			return D2MSTPubLaneType.lane1;
		if( lc == '2' )
			return D2MSTPubLaneType.lane2;
		if( lc == '3' )
			return D2MSTPubLaneType.lane3;
		if( lc == '4' )
			return D2MSTPubLaneType.lane4;
		if( lc == '5' )
			return D2MSTPubLaneType.lane5;
		if( lc == '6' )
			return D2MSTPubLaneType.lane6;
		if( lc == '7' )
			return D2MSTPubLaneType.lane7;
		if( lc == '8' )
			return D2MSTPubLaneType.lane8;
		if( lc == '9' )
			return D2MSTPubLaneType.lane9;
			
        return D2MSTPubLaneType.unknown;			
	}
	
	private static String replaceParams( D2MSTConf conf,
										 String locContent, 
										 D2MeasureLoc loc, 
										 String carriageway,
										 Set<String> lanes, int lrmi )
			throws D2ExceptionBase 
	{
        String dbid = loc.getRdsLocTblRef();			   
		
        if( dbid.length() != 0 )
        {
	        String dbid_c = dbid.substring( 0, 1 );
	        String dbid_n = dbid.substring( 1 );
	        
			locContent = locContent.replace( LTR_C, dbid_c );
			locContent = locContent.replace( LTR_N, dbid_n );
			
			String rdsDir;
			char dir = loc.getRdsDirection();
			
			if( (dir == 'P') || (dir == 'p') )
				rdsDir = positive;
			else if( (dir == 'N') || (dir == 'n') )
				rdsDir = negative;
			else if( (dir == 'B') || (dir == 'b') )
				rdsDir = both;
			else
				rdsDir = unknown;
			
			locContent = locContent.replace( RDI, rdsDir );
			
			locContent = locContent.replace( LTV, loc.getRdsLocTblVer() );
			locContent = locContent.replace( PL, "" + loc.getRdsLocCodePL() );
			locContent = locContent.replace( SL, "" + loc.getRdsLocCodeSL() );
			locContent = locContent.replace( DPL, "" + loc.getRdsPLDist() );
			locContent = locContent.replace( DSL, "" + loc.getRdsSLDist() );
			
			// No AlertC names provided yet !
			String primName = "";
			String secName = "";
			
			String pnContent = alcLocNamePN;
			String snContent = alcLocNameSN;
				
			pnContent = pnContent.replace( PN, primName );
			snContent = snContent.replace( SN, secName );
			
			String valuesElementB = "";
			String valuesElementE = "";
			if( conf.getModelBaseVersionNumber() > 1 )
			{
				valuesElementB = "<values>";
				valuesElementE = "</values>";
        	}
			
			pnContent = pnContent.replace( VALS_B, valuesElementB );
			pnContent = pnContent.replace( VALS_E, valuesElementE );
			snContent = snContent.replace( VALS_B, valuesElementB );
			snContent = snContent.replace( VALS_E, valuesElementE );
			
			if( primName.isEmpty() )
				locContent = locContent.replace( ALC_PN, "" );
			else
				locContent = locContent.replace( ALC_PN, pnContent );

			if( secName.isEmpty() )
				locContent = locContent.replace( ALC_SN, "" );
			else
				locContent = locContent.replace( ALC_SN, snContent );
        }		
		
        if( !carriageway.isEmpty() ) 
        {
			String _lanes = getLanes( conf );
			
			String allLanes = "";
			
			if( !conf.useMVTemplateFactory() )
			{
				for( String lane : lanes )
				{
					if( lane.length() > 0 )
					{
						char lc = lane.charAt(0);
						D2MSTPubLaneType lt = convertLaneCode( lc );
						
						String _lane = getLane( conf );
						_lane = _lane.replaceAll( "@@lane", lt.toString() );
						
						allLanes += _lane;
					}
				}
			}
			else
			{
				for( String lc : lanes )
				{
					String _lane = getLane( conf );
					_lane = _lane.replaceAll( "@@lane", lc );
					allLanes += _lane;
				}
			}
			
			_lanes = _lanes.replace( "@@carriageway", carriageway );
			_lanes = _lanes.replace( "@@lanes", allLanes );
			
			locContent = locContent.replace( "@@LANES", _lanes );
        }
        else 
        {
        	locContent = locContent.replace( "@@LANES", "" );
        }
        	
        	
		
		String _extRef = extRef;
		String extRefSys = loc.getD2ExtRefSystem();
		if( ( extRefSys == null ) || extRefSys.isEmpty() )
			_extRef = "";
		else
		{
			_extRef = _extRef.replace( "@@extRefSystem", extRefSys );
			_extRef = _extRef.replace( "@@extRefLocCode", loc.getD2ExtLocCode() );
		}
		
		locContent = locContent.replace( "@@EXT_REF", _extRef );

		
		List<D2Coordinate> coordinates = loc.getCoordinates();
		if( ( coordinates != null ) && ( coordinates.size() > 0 ) )
		{
			String points = "";
			int index = 1;
			for( D2Coordinate coordinate : coordinates )
			{
				String ip = itineraryPoint;
				String co = COORD;
				
				co = co.replaceAll( "@@coordx", "" + coordinate.getX() );
				co = co.replaceAll( "@@coordy", "" + coordinate.getY() );
				
				if( loc.getBearing() >= 0 ) {
					String b = bearing;
					b = b.replaceAll( "@@bearing", "" + loc.getBearing() );
					co = co.replaceAll( "@@BEARING", b );
				} else {
					co = co.replaceAll( "@@BEARING", "" );
				}
				
				ip = ip.replaceAll( "@@INDEX", "" + index );
				ip = ip.replaceAll( "@@COOR", "" + co );
				
				
				points += ip;
				
				index++;
			}
			locContent = locContent.replaceAll( "@@IT_POINTS", points );
		}

		
		if( ( lrmi == 3 ) || ( lrmi == 1 ) )
		{
			if( 
				( Math.abs( loc.getStartCoordX() ) < THOUSAND )
				&&
				( Math.abs( loc.getStartCoordY() ) < THOUSAND )
			  )
			{
				if( 
					( Math.abs( loc.getStartCoordX() - (-2.0) ) < D2BaseUtil.EPSILON )
					&&	
					( Math.abs( loc.getStartCoordY() - (-2.0) ) < D2BaseUtil.EPSILON )
				  )
				{
					// Value -2 indicates invalid value
					locContent = locContent.replaceAll( "@@COOR", "" );
				}
				else
				{
					// WGS84
					
					String _coord = COORD;
					_coord = _coord.replaceAll( "@@coordx", "" + loc.getStartCoordX() );
					_coord = _coord.replaceAll( "@@coordy", "" + loc.getStartCoordY() );
					
					if( loc.getBearing() >= 0 ) {
						String b = bearing;
						b = b.replaceAll( "@@bearing", "" + loc.getBearing() );
						_coord = _coord.replaceAll( "@@BEARING", b );
					} else {
						_coord = _coord.replaceAll( "@@BEARING", "" );
					}
					
					locContent = locContent.replaceAll( "@@COOR", _coord );
				}
			}
			else
			{
				// Warning: CT transforms from SRID=25832 (Hessen)! 
				double[] cos = CT.getWGS84CoordinatesFromUtm( loc.getStartCoordX(), loc.getStartCoordY() );			
				
				if( ( cos != null ) && ( cos.length >= 2 ) )
				{
					String _coord = COORD;
					_coord = _coord.replaceAll( "@@coordx", "" + cos[0] );
					_coord = _coord.replaceAll( "@@coordy", "" + cos[1] );
					
					if( loc.getBearing() >= 0 ) {
						String b = bearing;
						b = b.replaceAll( "@@bearing", "" + loc.getBearing() );
						_coord = _coord.replaceAll( "@@BEARING", b );
					} else {
						_coord = _coord.replaceAll( "@@BEARING", "" );
					}
					
					locContent = locContent.replaceAll( "@@COOR", _coord );
				}
				else
					locContent = locContent.replaceAll( "@@COOR", "" );
			}
		}
		else
		{
			locContent = locContent.replaceAll( "@@COOR", "" );
		}
		
		return locContent;
	}
	
	private static D2CoordTrans CT = new D2CoordTrans();			
	
	private static final String VALS_B 	= "@@VALS_B";
	private static final String VALS_E 	= "@@VALS_E";
	
	private static final String LTR_C 	= "@@LTR_C";
	private static final String LTR_N 	= "@@LTR_N";
	private static final String LTV		= "@@LTV";
	private static final String RDI		= "@@RDI";
	
	private static final String DPL		= "@@DPL";
	private static final String DSL		= "@@DSL";
	
	private static final String PN		= "@@@PN";
	private static final String SN		= "@@@SN";
	
	private static final String ALC_PN	= "@@@ALC_PN";
	private static final String ALC_SN	= "@@@ALC_SN";

	private static final String PL		= "@@@PL";
	private static final String SL		= "@@@SL";
	
	private static String negative 	= "negative";
	private static String positive	= "positive";
	private static String both 		= "both";
	private static String unknown 	= "unknown";
	
	
	
	
    private static String xsdPref = D2MSTPubXMLMan.getD2xsdNSPref();
    
    
	private static final String singlePoint = 
		
		"    <measurementSiteLocation " + xsdPref + "type=\"Point\">\n"+
		"@@EXT_REF" +
		"@@LANES" + 		
		"@@COOR" + 
		"    </measurementSiteLocation>\n";
	
	private static final String itineraryPoint =
			
	"    <locationContainedInItinerary index=\"@@INDEX\">\n" +
    "        <location xsi:type=\"Point\">\n" +
	"    @@COOR" + 
    "        </location>\n" +
    "    </locationContainedInItinerary>\n";
			
	private static final String itineraryLocation =
	
	"<measurementSiteLocation xsi:type=\"ItineraryByIndexedLocations\">\n" +  
	"@@IT_POINTS" + 
	"</measurementSiteLocation>\n";
	
	
	private static final String alcLocNamePN =
	"                    <alertCLocationName>@@VALS_B<value lang=\"de\">@@@PN</value>@@VALS_E</alertCLocationName>\n";
	
	private static final String alcLocNameSN =
	"                    <alertCLocationName>@@VALS_B<value lang=\"de\">@@@SN</value>@@VALS_E</alertCLocationName>\n";
	
	
	private static final String linearOffs =
		
	"    <measurementSiteLocation " + xsdPref + "type=\"Linear\">\n"+
    "@@LANES" +
    "        <alertCLinear " + xsdPref + "type=\"AlertCMethod4Linear\">\n"+
    "            <alertCLocationCountryCode>@@LTR_C</alertCLocationCountryCode>\n"+
    "            <alertCLocationTableNumber>@@LTR_N</alertCLocationTableNumber>\n"+
    "            <alertCLocationTableVersion>@@LTV</alertCLocationTableVersion>\n"+
    "            <alertCDirection>\n"+
	"                <alertCDirectionCoded>@@RDI</alertCDirectionCoded>\n"+
	"            </alertCDirection>\n"+
	"            <alertCMethod4PrimaryPointLocation>\n"+
	"                <alertCLocation>\n"+
	"@@@ALC_PN" +
	"                    <specificLocation>@@@PL</specificLocation>\n"+
	"                </alertCLocation>\n"+
	"                <offsetDistance>\n"+
	"                    <offsetDistance>@@DPL</offsetDistance>\n"+
	"                </offsetDistance>\n"+
	"            </alertCMethod4PrimaryPointLocation>\n"+
	"            <alertCMethod4SecondaryPointLocation>\n"+
	"                <alertCLocation>\n"+
	"@@@ALC_PN" +
	"                    <specificLocation>@@@SL</specificLocation>\n"+
	"                </alertCLocation>\n"+
	"                <offsetDistance>\n"+
	"                    <offsetDistance>@@DSL</offsetDistance>\n"+
	"                </offsetDistance>\n"+
	"            </alertCMethod4SecondaryPointLocation>\n"+
    "        </alertCLinear>\n"+
	"    </measurementSiteLocation>\n";
	
	private static final String COORD =
		
	"            <pointByCoordinates>\n" +
	"@@BEARING" + 
	"                <pointCoordinates>\n" +
   	"                    <latitude>@@coordy</latitude>\n" +
	"                    <longitude>@@coordx</longitude>\n" +
	"                </pointCoordinates>\n" +
	"            </pointByCoordinates>\n";	
	
	private static final String bearing = 
			
    "                <bearing>@@bearing</bearing>";
	
	
	private static final String point = 
			
	"    <measurementSiteLocation " + xsdPref + "type=\"Point\">\n"+
	"@@LANES" +
	"        <alertCPoint " + xsdPref + "type=\"AlertCMethod2Point\">\n"+
	"            <alertCLocationCountryCode>@@LTR_C</alertCLocationCountryCode>\n"+
	"            <alertCLocationTableNumber>@@LTR_N</alertCLocationTableNumber>\n"+
	"            <alertCLocationTableVersion>@@LTV</alertCLocationTableVersion>\n"+
	"            <alertCDirection>\n"+
	"                <alertCDirectionCoded>@@RDI</alertCDirectionCoded>\n"+
	"            </alertCDirection>\n"+
	"            <alertCMethod2PrimaryPointLocation>\n"+
	"                <alertCLocation>\n"+
	"@@@ALC_PN" +
	"                    <specificLocation>@@@PL</specificLocation>\n"+
	"                </alertCLocation>\n"+
	"            </alertCMethod2PrimaryPointLocation>\n"+
	"        </alertCPoint>\n"+
	"@@COOR" + 
	"    </measurementSiteLocation>\n";
	
	
	private static final String pointOffs = 
		
	"    <measurementSiteLocation " + xsdPref + "type=\"Point\">\n"+
	"@@LANES" +
	"        <alertCPoint " + xsdPref + "type=\"AlertCMethod4Point\">\n"+
	"            <alertCLocationCountryCode>@@LTR_C</alertCLocationCountryCode>\n"+
	"            <alertCLocationTableNumber>@@LTR_N</alertCLocationTableNumber>\n"+
	"            <alertCLocationTableVersion>@@LTV</alertCLocationTableVersion>\n"+
	"            <alertCDirection>\n"+
	"                <alertCDirectionCoded>@@RDI</alertCDirectionCoded>\n"+
	"            </alertCDirection>\n"+
	"            <alertCMethod4PrimaryPointLocation>\n"+
	"                <alertCLocation>\n"+
	"@@@ALC_PN" +
	"                    <specificLocation>@@@PL</specificLocation>\n"+
	"                </alertCLocation>\n"+
	"                <offsetDistance>\n"+
	"                    <offsetDistance>@@DPL</offsetDistance>\n"+
	"                </offsetDistance>\n"+
	"            </alertCMethod4PrimaryPointLocation>\n"+
	"        </alertCPoint>\n"+
	"@@COOR" + 
	"    </measurementSiteLocation>\n";
	
	private static String getLanes( D2MSTConf conf )
	{
		if( conf.getModelBaseVersionNumber() == 1 )
			return lanes;
		else
			return lanes2;
	}
	
	private static final String lanes =
		
	"    <supplementaryPositionalDescription>\n" +
	"@@lanes" +
	"    </supplementaryPositionalDescription>\n";

	private static final String lanes2 =
		
		"        <supplementaryPositionalDescription>\n" +
		"            <affectedCarriagewayAndLanes>\n" + 
		"            <carriageway>@@carriageway</carriageway>\n" + 
					 "@@lanes" +
		"            </affectedCarriagewayAndLanes>\n" + 
		"        </supplementaryPositionalDescription>\n";
	
	
	private static String getLane( D2MSTConf conf )
	{
		if( conf.getModelBaseVersionNumber() == 1 )
			return lane;
		else
			return lane2;
	}
	
	
	private static final String lane =
	
	"        <lanes>@@lane</lanes>\n";
	
	private static final String lane2 =
		
	"        <lane>@@lane</lane>\n";
	
	
	private static final String extRef =
			
			"        <externalReferencing>\n" +
			"            <externalLocationCode>@@extRefLocCode</externalLocationCode>\n" + 
			"            <externalReferencingSystem>@@extRefSystem</externalReferencingSystem>\n" + 
			"        </externalReferencing>\n";
	
	
}	

