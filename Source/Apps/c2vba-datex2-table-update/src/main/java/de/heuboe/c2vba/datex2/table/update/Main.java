package de.heuboe.c2vba.datex2.table.update;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

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
			LOGGER.info( "c2vba-datex2-table-update started" );
			
			Manager manager = ctx.getBean( Manager.class );
			manager.execute();
			
			LOGGER.info( "" );
			LOGGER.info( "c2vba-datex2-table-update finished" );
			System.exit(0);
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
