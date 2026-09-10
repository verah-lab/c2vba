package de.heuboe.datex2.mst.builder.ws;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import de.heuboe.datex2.mst.builder.D2MSTConf;
import de.heuboe.datex2.mst.builder.D2MSTDataReader;
import de.heuboe.datex2.mst.builder.D2MSTDataReceiver;
import de.heuboe.datex2.mst.builder.D2MSTPubPostProcessor;
import de.heuboe.datex2.mst.builder.D2MSTPublisherFile;
import de.heuboe.datex2.push.D2Publisher;

/**
 * 
 * Spring configuration
 * 
 * @author peters
 *
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan( basePackages = { "de.heuboe.datex2.mst.builder.ws", 
                                 "de.heuboe.datex2.config.persistence" } )               
public class Config {
	
	@Bean
	D2MSTConf mstConf( Properties props ) {
		return new D2MSTConf( true,
				              props.getPubFilePath() + File.separator + "err",
		                      props.getSchemaPath(),
		                      props.getSchemaFile(),
		                      props.getSchemaModelBaseVersion(),
		                      props.getSchemaNameSpace(),
		                      props.getSchemaCategory(),
		                      props.getPreferredLocEncoding(),
		                      true,
		                      true );
	}
	
	@Bean
	D2Publisher d2Publisher( Properties props, 
			                 D2MSTConf conf, 
			                 @Autowired(required = false) D2MSTPubPostProcessor postProcessor ) {
		D2MSTPublisherFile publisher = new D2MSTPublisherFile( conf,
										 props.getPubFilePath(), 
						                 props.getMstName(), 
						                 null,
						                 false );
		
		if( postProcessor != null ) {
			publisher.setPostProcessor( postProcessor );
		}
		
		return publisher;
	}	
	
	@Bean
	D2MSTDataProvider mstDataProvider() {
		return new D2MSTDataProvider();
	}
	
	@Bean
	D2MSTDataReceiver d2MSTDataReceiver( Properties props, D2MSTConf d2MSTConf, D2MSTDataReader dataReader, D2Publisher publisher ) {
		return new D2MSTDataReceiver( d2MSTConf,
									  dataReader,
									  props.getPubFilePath(),
									  props.getCountry(),
									  props.getNatId(),
									  props.getLang(), 
									  publisher );
	}
	
	@Bean
	D2MSTPubService d2MSTPubServiceServer( D2MSTDataReceiver d2MSTDataReceiver ) {
		return new D2MSTPubService( d2MSTDataReceiver );
	}
}
