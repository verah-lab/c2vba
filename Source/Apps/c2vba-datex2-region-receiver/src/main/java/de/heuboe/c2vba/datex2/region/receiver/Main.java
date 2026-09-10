package de.heuboe.c2vba.datex2.region.receiver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import de.heuboe.datex2.pull.PullClientAutoConfiguration;
import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;


/**
 * 
 * main() class
 *
 * @author peters
 * 
 */
@SpringBootApplication(exclude = { PullClientAutoConfiguration.class })
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
			ConfigurableApplicationContext ctx = SpringApplication.run(Main.class,args);
			
			Manager manager = ctx.getBean( Manager.class );
			
			manager.start();
			
			LOGGER.info( "c2vba-datex2-region-receiver started" );
			
			loop();
		}
		catch( Throwable ex )  // NOSONAR
		{
			LOGGER.fatal( "Error executing:" );
			LOGGER.fatal( ex.toString() );
			LOGGER.fatal( CallStack.getStackTraceAsString( ex ) );
			LOGGER.fatal( "Process will be terminated !" );
			System.exit(-1);
		}
	}
	
	
}
