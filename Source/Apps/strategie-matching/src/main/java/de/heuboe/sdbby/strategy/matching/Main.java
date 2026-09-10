package de.heuboe.sdbby.strategy.matching;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;


/**
 * 
 * main() class
 *
 * @author peters
 * 
 */
@SpringBootApplication
@ComponentScan( basePackages = { "de.heuboe.sdbby.strategy.matching" } )               
public class Main 
{
	private static final Logger LOGGER = Logger.getLogger( Main.class );

	private static void loop() {
		while( true ) {  // NOSONAR
			try {
				Thread.sleep( 10L );
			} catch (InterruptedException e) { // NOSONAR
				break;
			}
		}
		LOGGER.fatal( "Main loop ended unexpectedly" );
		LOGGER.fatal( "Process will be terminated !" );
	}
	
	/**
	 * 
	 * main()
	 * 
	 * @param args process parameters
	 * 
	 */
	public static void main(String[] args) 
	{
		try
		{
			LOGGER.info( "Start SpringApplication ..." );
			SpringApplication.run(Main.class,args);
			LOGGER.info( "strategie-matching started" );
			
			loop();
		}
		catch( Throwable ex )  // NOSONAR
		{
			LOGGER.fatal( "Error initialising:" );
			LOGGER.fatal( ex.toString() );
			LOGGER.fatal( CallStack.getStackTraceAsString( ex ) );
			LOGGER.fatal( "Process will be terminated !" );
			System.exit(-1);
		}
	}
}
