package de.heuboe.datex2.mst.builder.writer;

import java.util.List;
import java.util.Map;

import de.heuboe.datex2.exception.D2ExceptionBase;
import de.heuboe.datex2.measure.D2MeasureDataSource;
import de.heuboe.datex2.measure.D2MeasureItem;
import de.heuboe.datex2.measure.D2MeasureVehClass;
import de.heuboe.datex2.mst.builder.D2MSTConf;
import de.heuboe.datex2.mst.builder.D2MSTPubException;
import de.heuboe.datex2.mst.builder.D2MSTPubLaneType;

/**
 * 
 * Erstellt den MST-XML-Content (als String) der MeasurementSpecificCharacteristics einer MeasurementSite (aus den zugehörigen D2MeasureItem)
 * 
 * @author peters
 *
 */
public class D2MSTPubItemWriter
{
	public static String getContent( D2MSTConf conf,
									 List< D2MeasureItem > items,
									 Map< Integer, D2MeasureDataSource > dataSources )
			throws D2ExceptionBase
	{
		String allItemContent = "";
		
		int index = 1;
		for( D2MeasureItem item : items )
		{
			int dkRef = item.getDkRef();
			D2MeasureDataSource ds = dataSources.get( dkRef );
			
			if( ds == null )
			{
				throw new D2MSTPubException( D2MSTPubException.D2_ERR_NO_DATA_SOURCE_FOR_ITEM,
   					 						 "Keine Data-Source fuer Item '" + item.toString() + "'" ); 
			}
			
			String content = getMSC( conf );
			
			content = content.replace( "@@index", "" + index );
			
			int itemPeriod = item.getPeriod();
			int srcPeriod = ds.getPeriod();
			int period = itemPeriod;
			if( ( srcPeriod != -1 ) && ( srcPeriod != 0 ) )
				period = srcPeriod;
			
			content = content.replace( "@@period", "" + period );
			
			String lanes = item.getLane();
			String L = lane;
			if( lanes.length() > 0 )
			{
				if( !conf.useMVTemplateFactory() )
				{
					char l1 = lanes.charAt( 0 );
					D2MSTPubLaneType lt = D2MSTPubLocWriter.convertLaneCode( l1 );
					L = L.replace( "@@lane", lt.toString() );
				}
				else
				{
					L = lane;	
					L = L.replace( "@@lane", lanes );
				}
			}
			else
				L = "";
			
			content = content.replace( "@@LANE", L );
			
			
			
			content = content.replace( "@@valueType", "" + ds.getDatakind().toString() );
			
			String vc = vehicleCharacteristics;
			if( ( ds.getVehClass() == null ) || ds.getVehClass().equals( D2MeasureVehClass.other ) )
				vc = "";
			else		
				vc = vc.replace( "@@vt", "" + ds.getVehClass() );
			
			content = content.replace( "@@VC", vc );
			
			index += 1;
			
			allItemContent += content;			
		}
		
		return allItemContent;
	}		

	private static String lane =
	
		"                <specificLane>@@lane</specificLane>\n";
	
	private static String getMSC( D2MSTConf conf )
	{
		if( conf.getModelBaseVersionNumber() == 1 )
			return msc;
		else
			return msc2;
	}
	
	private static String msc = 
		"        <measurementSpecificCharacteristics index=\"@@index\">\n" + 
		"            <period>@@period</period>\n" + 
					 "@@LANE" +
		"            <specificMeasurementValueType>@@valueType</specificMeasurementValueType>\n" +
					 "@@VC" +
		"        </measurementSpecificCharacteristics>\n";
	
	private static String msc2 = 
		"        <measurementSpecificCharacteristics index=\"@@index\">\n" + 
		"            <measurementSpecificCharacteristics>\n" + 
		"                <period>@@period</period>\n" + 
						 "@@LANE" +
		"                <specificMeasurementValueType>@@valueType</specificMeasurementValueType>\n" +
						 "@@VC" +
		"            </measurementSpecificCharacteristics>\n" + 
		"        </measurementSpecificCharacteristics>\n";
	
	private static String vehicleCharacteristics = 
		"                <specificVehicleCharacteristics>\n" + 	
	    "                    <vehicleType>@@vt</vehicleType>\n" + 
		"                </specificVehicleCharacteristics>\n";	
	
}
