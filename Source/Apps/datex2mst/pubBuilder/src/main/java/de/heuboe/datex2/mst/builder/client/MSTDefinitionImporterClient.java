package de.heuboe.datex2.mst.builder.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.xml.sax.SAXException;

import de.heuboe.datex2.base.util.D2BaseUtil;
import de.heuboe.datex2.config.persistence.Datex2MSTDefinition;
import de.heuboe.datex2.config.persistence.repository.Datex2MSTDefinitionRepository;
import de.heuboe.datex2.push.D2PubSenderMDM;
import de.heuboe.log.Logger;
import de.heuboe.mst.definition.MstDefinition;

/**
 * 
 * Imports MST definition
 * 
 * @author peters
 *
 */
@SpringBootApplication(exclude = { D2PubSenderMDM.class })
@ComponentScan( basePackages = { "de.heuboe.datex2.config.persistence" } )               
public class MSTDefinitionImporterClient
{
	private static final Logger LOGGER = Logger.getLogger( MSTDefinitionImporterClient.class );
	
	@Autowired
	private Datex2MSTDefinitionRepository defRepository;
	
	@Value("${de.heuboe.datex2.mst.definition.import.mstId}")
	private String mstId;
	
	@Value("${de.heuboe.datex2.mst.definition.import.defVersion}")
    private String defVersion;

	@Value("${de.heuboe.datex2.mst.definition.import.d2Version}")
	private String d2Version;
	
	@Value("${de.heuboe.datex2.mst.definition.import.xmlDefFile}")
	private String xmlDefFile;

	@Value("${de.heuboe.datex2.mst.definition.import.schemaFile}")
	private String schemaFile;
	
	@Value("${de.heuboe.datex2.mst.definition.import.schemaCategory}")
	private String schemaCategory;
	
	@Value("${de.heuboe.datex2.mst.definition.import.description:}")
	private String description = "";

	private <T> T getContent(String content, String schemaFile, String packName ) throws JAXBException, SAXException, IOException  {
		LOGGER.info("Read XML content");

		JAXBContext jc;

		InputStream is = IOUtils.toInputStream(content,StandardCharsets.UTF_8);

		jc = JAXBContext.newInstance(packName);
		Unmarshaller um = jc.createUnmarshaller();

		URL schemaURL = MSTConfigImporterClient.class.getClassLoader().getResource(schemaFile);
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = sf.newSchema(schemaURL);
		um.setSchema(schema);

		@SuppressWarnings("unchecked")
		JAXBElement<T> ifs = (JAXBElement<T>) um.unmarshal(is);
		
		is.close();
		
		return ifs.getValue();
	}
	
	private void save() throws IOException, JAXBException, SAXException  {
		String xmlStr = FileUtils.readFileToString( new File( xmlDefFile ), StandardCharsets.UTF_8 );
		MstDefinition mstDefinition = getContent( xmlStr, 
				   					              "schema/mstDefinition.xsd", 
				   					              "de.heuboe.mst.definition" );
		
		Datex2MSTDefinition mstDefintionRecord = new Datex2MSTDefinition();
		mstDefintionRecord.setTime( new Date() );
		mstDefintionRecord.setMstId( mstId );
		mstDefintionRecord.setDefVersion( defVersion );
		mstDefintionRecord.setD2Version( d2Version );
		mstDefintionRecord.setSchemaFile( schemaFile );
		mstDefintionRecord.setSchemaCategory( schemaCategory );
		mstDefintionRecord.setDescription( description );
		mstDefintionRecord.setMstDefinition( mstDefinition );
		
		defRepository.save( mstDefintionRecord );
	}
	
	/**
	 * 
	 * MSTDefinitionImporterClient is executed as command line application
	 * 
	 * @param args	Command line arguments
	 */
	public static void main(String[] args)   
	{
		LOGGER.info( "Start SpringApplication ..." );
		try {
			ConfigurableApplicationContext ctx = SpringApplication.run(MSTDefinitionImporterClient.class,args);
			
			MSTDefinitionImporterClient importer = ctx.getBean( MSTDefinitionImporterClient.class );
			importer.save();
			
			LOGGER.info( "" );
			LOGGER.info("Import successfully finished !");
			LOGGER.info( "" );
		} catch( Throwable ex ) {   // NOSONAR
			LOGGER.error( "" );
			LOGGER.error("Error on import of MST-Definition:");
			LOGGER.error( "" );
			LOGGER.error( ex.toString() );
			ex.printStackTrace();   // NOSONAR
			LOGGER.error( "" );
		}
		D2BaseUtil.exit(-1);
	}
}
