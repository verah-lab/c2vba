package de.heuboe.srb.datex2.srp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
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
@ComponentScan( basePackages = { "de.heuboe.srb.datex2.srp" } )               
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
		try
		{
			LOGGER.info( "Start SpringApplication ..." );
			ConfigurableApplicationContext ctx = SpringApplication.run(Main.class,args);
			LOGGER.info( "strategicRoutingPublication started" );
			
			Manager manager = ctx.getBean( Manager.class );

			StrategyStatesReceiver sssr = ctx.getBean( StrategyStatesReceiver.class );
			sssr.setConsumer( manager );
			sssr.start();
			
			manager.loop();
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
	
