package de.heuboe.datex2.mst.builder.writer;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.heuboe.datex2.base.util.D2BaseUtil;
import de.heuboe.datex2.exception.D2ExceptionBase;
import de.heuboe.datex2.measure.D2MeasureDataSource;
import de.heuboe.datex2.measure.D2MeasureItem;
import de.heuboe.datex2.measure.D2MeasureLoc;
import de.heuboe.datex2.mst.builder.D2MSTConf;
import de.heuboe.datex2.mst.builder.D2MSTPubXMLMan;


/**
 * 
 * Erstellt den MST-XML-Content (als String) einer MeasurementSite aus den Bestandteilen D2MeasureLoc und D2MeasureItems 
 * 
 * @author peters
 *
 */
public class D2MSTPubRecordWriter
{
	public static String getContent( D2MSTConf conf,
									 String mstVersion,
									 D2MeasureLoc loc, 
									 List< D2MeasureItem > items, 
			                         Map< Integer, D2MeasureDataSource > dataSources,
			                         Set< String > namesSoFar )
			throws D2ExceptionBase
	{
		String content = getRecord(conf);
		
		String eq = loc.getEquipment();
		if( ( eq != null ) && !eq.isEmpty() )
		{
			String equipmentContent = getEquipmentTypeUsed( conf );
			equipmentContent = equipmentContent.replaceAll( "@@et", eq );
			content = content.replaceAll( "@@ET", equipmentContent ); 
		}
		else
		{
			content = content.replaceAll( "@@ET", "" ); 			
		}
		
		String idLocNanme = loc.getLocName();	
		
		if( namesSoFar.contains( idLocNanme ) )
			idLocNanme = idLocNanme + "_" + loc.getId();
		
		String d2Id = "R" + D2BaseUtil.createXSD_ID( loc.getLocName() );
		if( conf.useMVTemplateFactory() )
			d2Id = loc.getD2Id();
		
		content = content.replaceAll( "@@msId", d2Id ); 
		content = content.replaceAll( "@@msVersion", mstVersion ); 
		content = content.replaceAll( "@@msName", D2BaseUtil.toXML( loc.getLocName() ) ); 
		content = content.replaceAll( "@@lang", D2MSTPubXMLMan.getLanguage() ); 
		
		String itemContent = D2MSTPubItemWriter.getContent( conf,
															items, 
															dataSources );
		content = content.replaceAll( "@@SC", itemContent );
		
		Set<String> carriageways = new HashSet<String>();
		Set<String> lanes = new TreeSet<String>();
		for( D2MeasureItem item : items )
		{
			String lane = item.getLane();
			
			carriageways.add( item.getCarriageway() );
			
			if( conf.useMVTemplateFactory() )
				lanes.add( lane );
			else
			{
				int i;
				for( i = 0; i < lane.length(); i++ )
					lanes.add( new String( lane.substring( i, i + 1 ) ) );
			}
		}
		
		String carriageway = "";
		if( carriageways.size() == 1 ) 
		{
			carriageway = carriageways.iterator().next();
		}
		
		String locContent = D2MSTPubLocWriter.getContent( conf, loc, carriageway, lanes );
		content = content.replaceAll( "@@LOC", locContent );
		
		return content;
	}
	
	private static String getRecord( D2MSTConf conf )
	{
		if( conf.getModelBaseVersionNumber() == 1 )
			return record;
		else
		{
			if( conf.getD2SchemaCategory().equals( D2MSTConf.SCHEMA_CATEGORY_MDM ) )
				return record2_mdm;
			else	
				return record2;
		}
	}
	
	private static String record = 
		"<measurementSiteRecord id=\"@@msId\">\n" +
	    "@@ET" + 
		"    <measurementSiteName>\n" +
		"        <value lang=\"@@lang\">@@msName</value>\n" +
		"    </measurementSiteName>\n" +
		"@@SC" + 
		"@@LOC" +
		"</measurementSiteRecord>\n";
	
	private static String record2 = 
		"<measurementSiteRecord id=\"@@msId\" version=\"@@msVersion\">\n" +
	    "@@ET" + 
		"    <measurementSiteName>\n" +
		"    <values>\n" + 
		"        <value lang=\"@@lang\">@@msName</value>\n" +
		"    </values>\n" + 
		"    </measurementSiteName>\n" +
		"@@SC" + 
		"@@LOC" +
		"</measurementSiteRecord>\n";
	
	private static String record2_mdm = 
			"<measurementSiteRecord id=\"@@msId\" version=\"@@msVersion\">\n" +
		    "@@ET" + 
			"    <measurementSiteIdentification>@@msName</measurementSiteIdentification>\n" +
			"@@SC" + 
			"@@LOC" +
			"</measurementSiteRecord>\n";
	
	
	private static String getEquipmentTypeUsed( D2MSTConf conf )
	{
		if( conf.getModelBaseVersionNumber() == 1 )
			return equipmentTypeUsed;
		else
			return equipmentTypeUsed2;
	}
	
	private static String equipmentTypeUsed = 
		"    <measurementEquipmentTypeUsed>\n" +	
		"        <value lang=\"@@lang\">@@et</value>\n" +
		"    </measurementEquipmentTypeUsed>\n";
	
	private static String equipmentTypeUsed2 = 
		"    <measurementEquipmentTypeUsed>\n" +
		"        <values>\n" + 
		"            <value lang=\"@@lang\">@@et</value>\n" +
		"        </values>\n" + 
		"    </measurementEquipmentTypeUsed>\n";	
}		