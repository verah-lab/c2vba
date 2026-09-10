package de.heuboe.wls.by;


import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.heuboe.base.log.control.LogControlManager;
import de.heuboe.hbmonitor.HbMonitor;
import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;

public class Runner 
{
    private static Logger LOGGER = Logger.getLogger(Runner.class); 
    
    public Runner( String[] args ) {
        ClassPathXmlApplicationContext context = null;
        try {
            context = new ClassPathXmlApplicationContext( args[0] );
            
            HbMonitor.setRunning();
            
            (new LogControlManager()).start();
            
            while(true) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            }
        }
        catch( Throwable ex ) {
            context.close();
            LOGGER.error( ex.toString() );
            LOGGER.error( CallStack.getStackTraceAsString( ex ) );
        } finally {
            System.exit(-1);
        }
        
    }
    
    public static void main( String[] args )
    {
        new Runner( args );
    }
}
