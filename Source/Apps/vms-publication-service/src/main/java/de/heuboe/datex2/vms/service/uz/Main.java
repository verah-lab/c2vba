package de.heuboe.datex2.vms.service.uz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import de.heuboe.datex2.vms.service.config.Properties;
import de.heuboe.datex2.vms.service.db.Persistence;
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
@ComponentScan( basePackages = { "de.heuboe.datex2.vms.service.config" } )               
public class Main 
{
	private static final Logger LOGGER = Logger.getLogger( Main.class );

	private static void loop() {
		while( true ) {  // NOSONAR
			try {
				Thread.sleep( 10L );
			} catch (InterruptedException e) {
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
			LOGGER.info( "SpringApplication started" );
			
	        Persistence persistence = ctx.getBean( Persistence.class );
	        LocationManager lsocationManager = ctx.getBean( LocationManager.class );
	        ImageManager imageManager = ctx.getBean( ImageManager.class );
	        ConfigManager configManager = ctx.getBean( "MAIN", ConfigManager.class );
	        DataManager dataManager = ctx.getBean( DataManager.class );
	        
	        Properties properties = ctx.getBean( Properties.class );
			
			LOGGER.info( "Create UzService ..." );
			UzService uzService = new UzService( properties.getServiceHost(), properties.getServicePort() );
			uzService.setPersistence( persistence );
			uzService.setLocationManager( lsocationManager );
			uzService.setImageManager( imageManager );
			uzService.setConfigManager( configManager );
			uzService.setDataManager( dataManager );
			uzService.init();
			uzService.start();
			LOGGER.info( "UzService started" );
			
			loop();
		}
		catch( Throwable ex )  // NOSONAR
		{
			LOGGER.fatal( "Error initialising:" );
			LOGGER.fatal( ex.toString() );
			LOGGER.fatal( CallStack.getStackTraceAsString( ex ) );
			LOGGER.fatal( "Process will be terminated !" );
			
			Util.exit(-1);
		}
	}
	
	
}
