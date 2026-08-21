package de.heuboe.tls.ifacewancom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * shallow class conatainging main function to init the application wiht spring boot
 * @author ronald
 *
 */
@SpringBootApplication
@ComponentScan(basePackageClasses= {de.heuboe.tls.ifacewancom.config.JacksonConfig.class})
public class IfaceWANCom {

    /**
     * main entry for the spring application
     * 
     * @param args program parameters
     */
    public static void main( String[] args ) {
        SpringApplication.run( IfaceWANCom.class, args );
    }

}
