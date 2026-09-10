package de.heuboe.c2vba.datex2.table.update;

import javax.xml.bind.JAXBException;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import de.heuboe.c2vba.datex2.table.update.mst.MstJoiner;
import de.heuboe.c2vba.datex2.table.update.vms.VmsTableJoiner;
import de.heuboe.datex2.base.config.RemoteConfig;
import de.heuboe.datex2.push.PushClient;


/**
 * 
 * Spring configuration
 * 
 * @author peters
 *
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan( basePackages = { "de.heuboe.c2vba.datex2.table.update" } )               
public class Config {
	
	@Bean
	PushClient pushClient( RemoteConfig rc ) {
		return new PushClient( rc );
	}
	
	@Bean
	Manager manager() {
		return new Manager();
	}
	
	@Bean
	MstJoiner mstJoiner( Properties props ) throws JAXBException {
		return new MstJoiner( props.getType() );
	}

	@Bean
	VmsTableJoiner vmsTableJoiner( Properties props ) throws JAXBException {
		return new VmsTableJoiner( props.getType() );
	}
}
