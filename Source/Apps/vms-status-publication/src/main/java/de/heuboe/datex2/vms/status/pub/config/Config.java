package de.heuboe.datex2.vms.status.pub.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import de.heuboe.datex2.vms.status.pub.Manager;
import de.heuboe.datex2.vms.status.pub.NoSender;
import de.heuboe.datex2.vms.status.pub.Sender;
import de.heuboe.datex2.vms.status.pub.VMSException;
import de.heuboe.datex2.vms.status.pub.check.ContentCheck;
import de.heuboe.datex2.vms.status.pub.check.Persistence;

/**
 * 
 * Spring configuration
 * 
 * @author peters
 *
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan( basePackages = { "de.heuboe.datex2.vms.status.pub.config", 
		                         "de.heuboe.datex2.vms.status.pub.check",
		                         "de.heuboe.kafka.listener" } )               
public class Config {

	@Bean 
	Manager manager( Properties properties, Persistence persistence ) throws VMSException {
		
		Manager manager = new Manager( properties );
		
		if( properties.isCheckData() ) {
			ContentCheck cc = new ContentCheck( persistence, manager.getReader() );
			manager.setUpdateObserver( cc );
		}
		
		return manager;
	}

	@Bean 
	Persistence persistence() {
		return new Persistence();
	}
	
	
	@Bean
	Sender sender( Properties properties ) {
		String rt = properties.getReceiverType();
		if( ( ( rt == null ) || rt.isEmpty() )) {
			return new NoSender();
		} else {
			return new Sender();
		}
	}
}

