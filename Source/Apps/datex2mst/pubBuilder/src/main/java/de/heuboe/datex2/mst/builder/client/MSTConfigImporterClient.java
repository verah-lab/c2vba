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
import de.heuboe.datex2.config.persistence.Datex2MSTConfig;
import de.heuboe.datex2.config.persistence.repository.Datex2MSTConfigRepository;
import de.heuboe.datex2.push.D2PubSenderMDM;
import de.heuboe.log.Logger;
import de.heuboe.mst.config.MappingConfig;



/**
 * 
 * Imports MST configuration
 * 
 * @author peters
 *
 */
@SpringBootApplication(exclude = { D2PubSenderMDM.class })
@ComponentScan( basePackages = { "de.heuboe.datex2.config.persistence" } )  
public class MSTConfigImporterClient
{
	private static final Logger LOGGER = Logger.getLogger( MSTConfigImporterClient.class );
	
	@Autowired
	private Datex2MSTConfigRepository cfgRepository;
	
	@Value("${de.heuboe.datex2.mst.config.import.d2Version}")
	private String d2Version;

	@Autowired
	@Value("${de.heuboe.datex2.mst.config.import.xmlCfgFile}")
	private String xmlCfgFile;
	
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
		String xmlStr = FileUtils.readFileToString( new File( xmlCfgFile ), StandardCharsets.UTF_8 );
		MappingConfig mc = getContent( xmlStr, 
				   					   "schema/mstConfig.xsd", 
				   					   "de.heuboe.mst.config" );
		
		Datex2MSTConfig mstConfigRecord = new Datex2MSTConfig();
		mstConfigRecord.setTime( new Date() );
		mstConfigRecord.setD2Version( d2Version );
		mstConfigRecord.setMappingConfig( mc );
		
		cfgRepository.save( mstConfigRecord );
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
			ConfigurableApplicationContext ctx = SpringApplication.run(MSTConfigImporterClient.class,args);
			
			MSTConfigImporterClient importer = ctx.getBean( MSTConfigImporterClient.class );
			importer.save();
			
			LOGGER.info( "" );    
			LOGGER.info("Import successfully finished !");
			LOGGER.info( "" );
		} catch( Throwable ex )     // NOSONAR
		{
			LOGGER.error( "" );
			LOGGER.error("Error on import of MST-Configuration:");
			LOGGER.error( "" );
			LOGGER.error( ex.toString() );
			ex.printStackTrace();   // NOSONAR
			LOGGER.error( "" );
		}
		
		D2BaseUtil.exit(-1);
		
	}
}
