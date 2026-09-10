package de.heuboe.c2vba.datex2.join;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

import de.heuboe.util.DirectoryCleaner;


/**
 * 
 * Spring cnofiguration
 * 
 * @author peters
 *
 */
@Configuration
@EnableAutoConfiguration
@EnableKafka
@ComponentScan( basePackages = { "de.heuboe.c2vba.datex2.join",
        						 "de.heuboe.kafka.listener" } )               
public class Config {

	public static final String D2_SCHEMA_FILE = "schema/DATEXIISchema_2_2_3.xsd";
	public static final String D2_PACKAGE = "eu.datex2.schema._2._2_0";
	
	
	@Autowired
	private Properties properties;
	
	@PostConstruct
	void startDirectoryCleaner() {
		
		DirectoryCleaner dc = null;

		String pubFileDir = properties.getPubFileDir();
		int keepInterval = properties.getKeepInterval();
		File dir = new File( pubFileDir );
		dir.mkdirs();
		
		if( !dir.exists() || !dir.isDirectory() )
		{
			throw new RuntimeException( "No access to pubFileDir <" + pubFileDir + "> !" );   // NOSONAR
		}
		
		try
		{
			dc = new DirectoryCleaner( keepInterval, pubFileDir, 10 );
			dc.start();
		} catch( IOException ex ) {
			throw new RuntimeException( "DirectoryCleaner: no access to pubFileDir <" + pubFileDir + "> !" );  // NOSONAR
		}
	}
	
}
