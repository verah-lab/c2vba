package de.heuboe.c2vba.datex2.table.update.mst;

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
import de.heuboe.datex2.base.ProfileConverter;
import de.heuboe.datex2.schema.SchemaUtil;
import de.heuboe.log.Logger;
import de.heuboe.util.JAXBUtil;
import eu.datex2.schema._2._2_0.mst.D2LogicalModel;
import eu.datex2.schema._2._2_0.mst.MeasurementSiteRecord;
import eu.datex2.schema._2._2_0.mst.MeasurementSiteTable;
import eu.datex2.schema._2._2_0.mst.MeasurementSiteTablePublication;

public class MstJoiner extends TableJoinerImpl<D2LogicalModel> {
	
	private static final Logger LOGGER = Logger.getLogger( MstJoiner.class );
	
	private static final String MST_SCHEMA_FILE = "schema/MeasurementSite_2017_01-00-00.xsd";
	private static final String MST_PACKAGE = "eu.datex2.schema._2._2_0.mst";
	private static final String MST_BASE_FILE_LVE = "/d2Template/emptyMstFG1.xml";
	private static final String MST_BASE_FILE_UFD = "/d2Template/emptyMstFG3.xml";
	
	private String type;
	
	private D2LogicalModel emptyTable; 
	
	public MstJoiner( String type ) throws JAXBException {
		
		this.type = type;
		
		jaxbUtil = new JAXBUtil( MST_SCHEMA_FILE, 
								 MST_PACKAGE, 
	            				 SchemaUtil.DATEX2_SCHEMA_NS,
	            				 false );
		
		profileConverter = 
				  new ProfileConverter<>( MST_SCHEMA_FILE, D2LogicalModel.class,
	                                    SchemaUtil.DATEX2_SCHEMA_2_2_3_FILE, 
	                                    eu.datex2.schema._2._2_0.D2LogicalModel.class);
		
		String ef = MST_BASE_FILE_LVE;
		if( type.equals( "UFD" ) ) {
			ef = MST_BASE_FILE_UFD;
		}
		
		try ( Scanner scanner = new Scanner( MstJoiner.class.getResourceAsStream( ef ), StandardCharsets.UTF_8) ) {
			String esp = scanner.useDelimiter("\\A").next();
			emptyTable = jaxbUtil.getObject( esp );
		}

	}
	
	@Override
	public JoinResult<D2LogicalModel> joinTables(  D2LogicalModel table1, 
										           D2LogicalModel table2, 
										           String versionMode1,
										           String versionMode2,
										           String resultFile ) 
			             throws JAXBException, IOException {
		
		LOGGER.info( "Join table of type '" + type + "'" );

		D2LogicalModel table = emptyTable;
		
		MeasurementSiteTablePublication mstp = (MeasurementSiteTablePublication)(table.getPayloadPublication());
		mstp.setPublicationTime( new Date() );
		MeasurementSiteTable t = mstp.getMeasurementSiteTable().get(0);
		String tableId = t.getId();
		
		t.setId( "D2-C2VBA-" + type + "-Table" );
		
		MeasurementSiteTablePublication mstp1 = (MeasurementSiteTablePublication)(table1.getPayloadPublication());
		MeasurementSiteTable t1 = mstp1.getMeasurementSiteTable().get(0);
		MeasurementSiteTablePublication mstp2 = (MeasurementSiteTablePublication)table2.getPayloadPublication();
		MeasurementSiteTable t2 = mstp2.getMeasurementSiteTable().get(0);

		t.getMeasurementSiteRecord().clear();
		for( MeasurementSiteRecord ms : t1.getMeasurementSiteRecord() ) {
			t.getMeasurementSiteRecord().add( ms );
		}
		for( MeasurementSiteRecord ms : t2.getMeasurementSiteRecord() ) {
			t.getMeasurementSiteRecord().add( ms );
		}
		
		String version1 = t1.getVersion();
		String version2 = t2.getVersion();
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
