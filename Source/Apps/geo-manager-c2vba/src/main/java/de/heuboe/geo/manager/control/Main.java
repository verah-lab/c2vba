package de.heuboe.geo.manager.control;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import de.heuboe.base.log.control.LogControlManager;
import de.heuboe.geo.manager.base.road.NetLevelSystemControl;
import de.heuboe.geo.manager.base.road.RouteManager;
import de.heuboe.hbmonitor.HbMonitor;
import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;
import de.heuboe.wls.map.supply.MapActivationPostProcessing;
import de.heuboe.wls.map.supply.MapContextManager;
import de.heuboe.wls.srv.manager.WebLocationServiceManager;


/**
 * 
 * This is the Spring configuration class
 * 
 * @author peters
 *
 */
@SpringBootApplication
public class Main {
    
    private static final Logger LOGGER = Logger.getLogger( Main.class );

    /**
     * Accessed by Spring ReadyController
     */
    public static volatile boolean running = false;    // NOSONAR

	
	private static void loop() throws InterruptedException {
	    
		while( true ) {							// NOSONAR: Main loop
			try {
				Thread.sleep(1000);
				
			} catch (InterruptedException ex ) {
				LOGGER.error( "InterruptedException in main loop" );
				LOGGER.error( CallStack.getStackTraceAsString( ex ) );
				throw ex;
			}
		}
		
	}
	
	/**
     * 
     * Sets route system implementation
     * 
     */
    public static void setRouteSystem() {
        RouteManager.netLevelSystem = new NetLevelSystemControl();
    }

	
	/**
	 * 
	 * main method
	 * 
	 * @param args parameters
	 */
	public static void main(String[] args) {  // NOSONAR
		
		try {
		    
		    setRouteSystem();
		    
		    
            SpringApplication springApp = new SpringApplication(Main.class);
		    springApp.setAdditionalProfiles( "NO_OSM", "NO_DEFAULT_INFRA" );
		    ConfigurableApplicationContext ctx = springApp.run(args);  
		    
			WebLocationServiceManager wlsm = ctx.getBean( WebLocationServiceManager.class );
			wlsm.setPrimaryCtx( ctx );
			
			MapActivationPostProcessing wapa = ctx.getBean( MapActivationPostProcessing.class );
            MapContextManager mcm = ctx.getBean( MapContextManager.class );
            wlsm.setMapContextManager( mcm );
            wlsm.firstInit( wapa, true );
            
            HbMonitor.setRunning();
			running = true;
			(new LogControlManager()).start();

			loop();
		} catch( Throwable ex ) {    // NOSONAR: Log all errors!
			LOGGER.fatal( "Fatal error initialising process!");
			LOGGER.fatal( ex.toString() );
			LOGGER.fatal( CallStack.getStackTraceAsString( ex ) );
			System.exit(-1);
		}
	}


}
