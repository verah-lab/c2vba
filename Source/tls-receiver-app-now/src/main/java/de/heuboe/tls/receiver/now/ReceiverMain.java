package de.heuboe.tls.receiver.now;

import de.heuboe.tls.receiver.now.config.ConfigVMIS2Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import de.heuboe.vmis2.id.autoconfig.IDGeneratorAutoConfiguration;

/**
 * the main class with the main method - mainly for initialisation by spring
 * @author ronald
 *
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan( basePackageClasses = {
        de.heuboe.tls.receiver.core.ConfigHook.class,
        ConfigVMIS2Config.class,
        de.heuboe.now.receiveconverter.InitAllInit.class
        } )
@EnableAutoConfiguration( exclude = { MongoAutoConfiguration.class, IDGeneratorAutoConfiguration.class } )
public class ReceiverMain {

    /**
     * main entry for the spring application
     * @param args program parameters
     */
    public static void main( String[] args ) {
        SpringApplication.run( ReceiverMain.class, args );
    }
    
}
