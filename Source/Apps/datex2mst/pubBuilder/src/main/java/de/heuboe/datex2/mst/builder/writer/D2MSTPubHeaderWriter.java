package de.heuboe.datex2.mst.builder.writer;

import java.util.Date;

import de.heuboe.datex2.base.util.D2BaseUtil;
import de.heuboe.datex2.exception.D2ExceptionBase;
import de.heuboe.datex2.mst.builder.D2MSTConf;

/**
 * 
 * Erstellt den MST-XML-Content (als String) des Headers der MeasurementSite-Publication
 * 
 * @author peters
 *
 */
public class D2MSTPubHeaderWriter
{
	private D2MSTPubHeaderWriter() {
	}
	
	
	private static String replace( String src, String var, String val )
	{
		return src.replaceAll( var, val );
	}
	
	/**
	 * 
	 * Creates header of MST publication
	 * 
	 * @param conf				Configuration
	 * @param mstName			MST name
	 * @param mstDescr			MST description
	 * @param mstVersion		MST version
	 * @param pubTime			Publication time
	 * @param country			DATEX-II country code
	 * @param natId				National identifier
	 * @param lang				DATEX-II language code	
	 * @return					Header of MST publication
	 * @throws D2ExceptionBase	Error
	 */
	public static String getMSTHeader( 	D2MSTConf conf,  
										String mstName,
										String mstDescr,
										String mstVersion,
										Date pubTime,
										String country,
										String natId,
										String lang )
			throws D2ExceptionBase
	{
		String mst = getMST( conf );	
		mst = replace( mst, "@@mstId", mstName );
		mst = replace( mst, "@@mstVersion", mstVersion );
		
		String _header = header;   										// NOSONAR
		
		_header = replace( _header, "@@MST", mst );
		
		_header = replace( _header, "@@mstId", mstName );
		_header = replace( _header, "@@mstDescr", mstDescr );
		_header = replace( _header, "@@mstVersion", mstVersion );
		
		String _pubTime = D2BaseUtil.toXMLDateTime( pubTime );			// NOSONAR	
		_header = replace( _header, "@@pubTime", _pubTime );
		
		_header = replace( _header, "@@country", country );
		_header = replace( _header, "@@natId", natId );
		
		_header = replace( _header, "@@lang", lang );
		
		_header = replace( _header, "@@d2Version", conf.getD2SchemaModelBaseVersion() );
		_header = replace( _header, "@@d2SchemaNameSpace", conf.getD2SchemaNameSpace() );
		_header = replace( _header, "@@d2SchemaFile", conf.getD2SchemaFile() );
		
		return _header;
	}
	
	public static String getMSTTail()
	{
		return tail;
	}
	
	private static String getMST( D2MSTConf conf )
	{
		if( conf.getModelBaseVersionNumber() == 1 ) {
			return mst;
		} else {
			return mst2;
		}
	}

	private static String mst = 
		
		"<measurementSiteTable id=\"@@mstId\">\n" +
		"    <measurementSiteTableReference>@@mstDescr</measurementSiteTableReference>\n" +
		"    <measurementSiteTableVersion>@@mstVersion</measurementSiteTableVersion>\n";

	private static String mst2 = 
		
		"<measurementSiteTable id=\"@@mstId\" version=\"@@mstVersion\">\n" +
		"    <measurementSiteTableIdentification>@@mstDescr</measurementSiteTableIdentification>\n";
	
	
	private static String header = 
		
	"<d2LogicalModel " +
    "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" modelBaseVersion=\"@@d2Version\" \n" + 
    "xmlns=\"@@d2SchemaNameSpace\" \n" +
   	"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" + 
	"xsi:schemaLocation=\"@@d2SchemaNameSpace @@d2SchemaFile\">" +
	"<exchange xmlns=\"@@d2SchemaNameSpace\">\n" +
	"    <supplierIdentification>\n" +
	"    <country>@@country</country>\n" +
	"    <nationalIdentifier>@@natId</nationalIdentifier>\n" +
	"    </supplierIdentification>\n" +
	"</exchange>" +
	"<payloadPublication xsi:type=\"MeasurementSiteTablePublication\" lang=\"@@lang\">\n" +
	"<publicationTime>@@pubTime</publicationTime>\n" +
	"<publicationCreator>\n" +
	"    <country>@@country</country>\n" +
	"    <nationalIdentifier>@@natId</nationalIdentifier>\n" +
	"</publicationCreator>\n" +
	"<headerInformation>\n" +
	"    <confidentiality>noRestriction</confidentiality>\n" +
	"    <informationStatus>real</informationStatus>\n" +
	"</headerInformation>\n\n" +
	"@@MST";
					
	private static String tail = "\n</measurementSiteTable>\n</payloadPublication>\n</d2LogicalModel>\n";
}
