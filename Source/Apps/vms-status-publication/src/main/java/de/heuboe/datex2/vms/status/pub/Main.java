package de.heuboe.datex2.vms.status.pub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;

/**
 * 
 * Main
 * 
 * @author peters
 * 
 * main() class
 *
 */
@SpringBootApplication
public class Main
{
	private static final Logger LOGGER = Logger.getLogger( Main.class );

	/**
	 * 
	 * main()
	 * 
	 * @param args process parameters
	 * 
	 */
	public static void main(String[] args) 
	{
		Manager manager = null;
		try {
	        SpringApplication springApp = new SpringApplication(Main.class);
	        ConfigurableApplicationContext ctx = springApp.run(args);
	
	        manager = ctx.getBean( Manager.class );
		} catch( Throwable ex )  { 	// NOSONAR
			LOGGER.fatal( "Main initialisation: exception caught:" );
			LOGGER.fatal( ex.toString() );
			LOGGER.fatal( CallStack.getStackTraceAsString( ex ) );
			
			Util.exit( -1 );
		}
        
        run( manager ); 
	}
	
	
	private static void run( Manager manager )
	{
		try
		{
			manager.loop();
		} catch( Throwable ex )  { 	// NOSONAR: do certain necessary tasks
            						//          on process termination
			LOGGER.fatal( "Main loop: exception caught:\n" );
			if( ex.getMessage() != null )
			{
				LOGGER.fatal( ex.getMessage() );
			}
			
			LOGGER.fatal( CallStack.getStackTraceAsString( ex ) );
			
			Util.exit( -1 );
		}
		
		Util.pause();
	}

}
