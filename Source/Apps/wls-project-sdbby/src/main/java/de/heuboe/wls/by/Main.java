package de.heuboe.wls.by;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;

/***
 * 
 * Main
 * 
 * @author peters
 *
 */
@SpringBootApplication
public class Main {

	private static final Logger LOGGER = Logger.getLogger( Main.class );

	public static volatile boolean running = false;    // NOSONAR
	
	/**
	 * 
	 * main
	 * 
	 * @param args         Application arguments
	 */
	public static void main(String[] args) {
		
		try {
			
			ConfigurableApplicationContext ctx = SpringApplication.run(Main.class, args);
            
            RoadShapefileWriter rsfw = ctx.getBean( RoadShapefileWriter.class );
            rsfw.writeShapefile(); 
            
            running = true;
			
			while(true) {   // NOSONAR
			    Thread.sleep(1000);
			}
		
		} catch( Throwable ex ) {   // NOSONAR
			LOGGER.fatal( "Fatal error executing process!");
			LOGGER.fatal( ex.toString() );
			LOGGER.fatal( CallStack.getStackTraceAsString( ex ) );
		}
	}


}
