package de.heuboe.c2vba.datex2.table.update.vms;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Scanner;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;

import de.heuboe.c2vba.datex2.table.update.JoinResult;
import de.heuboe.c2vba.datex2.table.update.TableJoinerImpl;
import de.heuboe.c2vba.datex2.table.update.XMLFormatter;
import de.heuboe.c2vba.datex2.table.update.mst.MstJoiner;
import de.heuboe.datex2.base.ProfileConverter;
import de.heuboe.datex2.schema.SchemaUtil;
import de.heuboe.log.Logger;
import de.heuboe.util.JAXBUtil;
import eu.datex2.schema._2._2_0.vms.D2LogicalModel;
import eu.datex2.schema._2._2_0.vms.VmsTablePublication;
import eu.datex2.schema._2._2_0.vms.VmsUnitRecord;
import eu.datex2.schema._2._2_0.vms.VmsUnitTable;

public class VmsTableJoiner extends TableJoinerImpl<D2LogicalModel> {

	private static final Logger LOGGER = Logger.getLogger( VmsTableJoiner.class );
	
	private static final String VMS_SCHEMA_FILE = "schema/DATEXIISchema_2_2_3_vms.xsd";
	private static final String VMS_PACKAGE = "eu.datex2.schema._2._2_0.vms";
	private static final String VMS_BASE_FILE_SBA = "/d2Template/emptyVmstSBA.xml";
	private static final String VMS_BASE_FILE_WWW = "/d2Template/emptyVmstWWW.xml";
	
	private String type;
	
	private D2LogicalModel emptyTable; 
	
	public VmsTableJoiner( String type ) throws JAXBException {
		
		this.type = type;
		
		jaxbUtil = new JAXBUtil( VMS_SCHEMA_FILE, 
				                 VMS_PACKAGE, 
	            				 SchemaUtil.DATEX2_SCHEMA_NS,
	            				 false );
		
		profileConverter = 
				  new ProfileConverter<>( VMS_SCHEMA_FILE, D2LogicalModel.class,
	                                      SchemaUtil.DATEX2_SCHEMA_2_2_3_FILE, 
	                                      eu.datex2.schema._2._2_0.D2LogicalModel.class);
		
		String ef = VMS_BASE_FILE_SBA;
		if( type.equals( ""  ) ) {
			ef = VMS_BASE_FILE_WWW;
		}
		
		try ( Scanner scanner = new Scanner( MstJoiner.class.getResourceAsStream( ef ), StandardCharsets.UTF_8) ) {
			String esp = scanner.useDelimiter("\\A").next();
			emptyTable = jaxbUtil.getObject( esp );
		}

	}
	
	@Override
	public JoinResult<D2LogicalModel> joinTables( D2LogicalModel table1, 
			                                      D2LogicalModel table2, 
			                                      String versionMode1,
			                                      String versionMode2,
			                                      String resultFile ) 
			     throws JAXBException, IOException {
		
		LOGGER.info( "Join table of type '" + type + "'" );

		D2LogicalModel table = emptyTable;
		
		VmsTablePublication vtp = (VmsTablePublication)(table.getPayloadPublication());
		vtp.setPublicationTime( new Date() );
		VmsUnitTable t = vtp.getVmsUnitTable().get(0);
		String tableId = t.getId();
		
		t.setId( "D2-C2VBA-" + type + "-Table" );
		t.setVmsUnitTableIdentification( "D2-C2VBA-" + type + "-Table" );
		
		VmsTablePublication mstp1 = (VmsTablePublication)(table1.getPayloadPublication());
		VmsUnitTable t1 = mstp1.getVmsUnitTable().get(0);
		VmsTablePublication mstp2 = (VmsTablePublication)table2.getPayloadPublication();
		VmsUnitTable t2 = mstp2.getVmsUnitTable().get(0);

		t.getVmsUnitRecord().clear();
		for( VmsUnitRecord unit : t1.getVmsUnitRecord() ) {
			t.getVmsUnitRecord().add( unit );
		}
		for( VmsUnitRecord ms : t2.getVmsUnitRecord() ) {
			t.getVmsUnitRecord().add( ms );
		}
		
		String version1 = t1.getVersion();
		String version2 = t2.getVersion();
		
		if( versionMode1.equals( "ID" ) ) {
			version1 = t1.getId() + "/" + t1.getVersion();
		}
		if( versionMode2.equals( "ID" ) ) {
			version2 = t2.getId() + "-" + t2.getVersion();
		}
		
		String tableVersion = version1 + "_" + version2;
		t.setVersion( tableVersion );
		
		if( ( resultFile != null ) && !resultFile.isEmpty() ) {
			String rs = resultFile.replace( "<version>", tableVersion );
			String content = jaxbUtil.getDocument( table, "d2LogicalModel", D2LogicalModel.class);
			
			content = XMLFormatter.formatXML( content );
			LOGGER.info( "Write join table to file '" + rs + "'" );
			FileUtils.writeStringToFile( new File(rs), content, StandardCharsets.UTF_8 );
		}
		
		return new JoinResult<>( tableId, "", tableVersion, table );
	}
	
}
