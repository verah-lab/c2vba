package de.heuboe.c2vba.datex2.region.receiver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaNull;

import de.heuboe.c2vba.data.DatexIIContent;
import de.heuboe.c2vba.data.pojo.PDatexIIContent;
import de.heuboe.c2vba.datex2.kafka.Producer;
import de.heuboe.c2vba.datex2.region.receiver.RegionConfigProps.RegionConfig;
import de.heuboe.datex2.consumer.Datex2SoapConnectorFactory;
import de.heuboe.datex2.schema.SchemaUtil;
import de.heuboe.kafka.listener.KafkaException;
import de.heuboe.kafka.listener.KafkaManager;
import de.heuboe.kafka.producer.KafkaProducerProto;
import de.heuboe.util.DirectoryCleaner;
import de.heuboe.util.JAXBUtil;
import eu.datex2.schema._2._2_0.D2LogicalModel;


/**
 * 
 * Spring configuration
 * 
 * @author peters
 *
 */
@Configuration
@EnableAutoConfiguration
@EnableKafka
@EnableConfigurationProperties(value = RegionConfigProps.class)
@ComponentScan( basePackages = { "de.heuboe.c2vba.datex2.region.receiver",
        						 "de.heuboe.kafka.listener" } )               
public class Config {
	
	private static List<DirectoryCleaner> dcs = new ArrayList<>();
	
	@Bean 
	String generalD2Schema( Properties props ) {
		String schema = "schema/" + SchemaUtil.DATEX2_SCHEMA_2_2_3_FILE;
		String gd2s = props.getGeneralD2Schema();
		
		if( gd2s != null && !gd2s.isEmpty() ) {
			schema = gd2s; 
		}
		
		return schema;
	}
	
	@Bean
	JAXBUtil jaxbUtil( String generalD2Schema ) throws JAXBException {
		return new JAXBUtil( generalD2Schema, 
				 			 "eu.datex2.schema._2._2_0", 
				 			 SchemaUtil.DATEX2_SCHEMA_NS,
				 			 false );
	}
	
    @Autowired
    private KafkaTemplate< String, PDatexIIContent> kafkaTemplateD2Content;    	 // NOSONAR
    @Autowired
    private KafkaTemplate< String, KafkaNull > kafkaTemplateNull;				 // NOSONAR
	
	@Bean
	List< Producer<D2LogicalModel> > producers( RegionConfigProps regionConfigProps, 
			                                    Properties properties,
			                                    KafkaManager kafkaManager ) throws KafkaException {
	
		List< Producer<D2LogicalModel> > producers = new ArrayList<>();
		
		for( RegionConfig rc : regionConfigProps.getRegionConfigs() ) {
			String name = rc.getName();
			String topic = rc.getTopic();
			KafkaProducerProto<DatexIIContent,PDatexIIContent> kpp = new KafkaProducerProto<>( DatexIIContent.class, topic );

			kpp.setKafkaTemplate( kafkaTemplateD2Content );
			kpp.setKafkaTemplateNull( kafkaTemplateNull );
			
			Producer<D2LogicalModel> producer = new Producer<>( name, kpp );
			producer.init( kafkaManager, properties.getKeyStockReadTimeout() );
			
			producers.add( producer );
		}
		
		return producers;
	}

	@Bean
	Datex2SoapConnectorFactory datex2SoapConnectorFactory() {
		return new 	Datex2SoapConnectorFactory();
	}
	
	@Bean
	Manager manager() {
		return new Manager();
	}

	/**
	 * 
	 * Build valid file system path
	 * 
	 * @param fileName   File name
	 * @return			 Valid file system path
	 */
	public static String removeInvalidFileChars( String fileName ) {
		return fileName.replaceAll("[\\\\/:*?\"<>|]", "");
	}
	
	/**
	 * 
	 * Start directory cleaner
	 * 
	 * @param pubFileDir   		Directory
	 * @param rn   				Sub directory
	 * @param keepInterval   	Keep interval
	 * @return Full name of folder to clean			 
	 */
	public static String startDirectoryCleaner( String pubFileDir, String rn, int keepInterval ) {
		
		String dirName = pubFileDir + File.separator + removeInvalidFileChars( rn ); 
		File dir = new File( dirName );
		dir.mkdirs();
		
		if( !dir.exists() || !dir.isDirectory() )
		{
			throw new RuntimeException( "No access to pubFileDir <" + pubFileDir + "> !" );   // NOSONAR
		}
		
		try
		{
			DirectoryCleaner dc = new DirectoryCleaner( keepInterval, dirName, 10 );
			dcs.add( dc );
			dc.start();
		} catch( IOException ex ) {
			throw new RuntimeException( "DirectoryCleaner: no access to pubFileDir <" + pubFileDir + "> !" ); // NOSONAR
		}
		
		return dirName;
	}
}
