package de.heuboe.datex2.mst.builder.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import de.heuboe.datex2.base.util.D2BaseUtil;
import de.heuboe.datex2.mst.builder.ws.D2MSTPubService;
import de.heuboe.datex2.mst.builder.ws.Properties;
import de.heuboe.log.Logger;

@SpringBootApplication
@ComponentScan( basePackages = { "de.heuboe.datex2.mst.builder.ws", 
		                         "de.heuboe.datex2.config.persistence" } )               
public class MSTFileInstanceWriter
{
	private static final Logger LOGGER = Logger.getLogger( MSTFileInstanceWriter.class );
	
	
	public static void main(String[] args)
	{
		LOGGER.info( "Start SpringApplication ..." );
		try {
			ConfigurableApplicationContext ctx = SpringApplication.run(MSTFileInstanceWriter.class,args);
			
			D2MSTPubService fileCreator = ctx.getBean( D2MSTPubService.class );
			Properties props = ctx.getBean( Properties.class );
			fileCreator.createMSTPublicationFileDef( props.getMstId(), 
					                                 props.getMstVersion(), 
					                                 props.getDefVersion(), 
					                                 props.getNatId(), 
					                                 props.getPreferredLocEncoding() );
			
			System.out.println( "" );
			System.out.println("MST XML represenation successfully written to file !");
			System.out.println( "" );
		}
		catch( Throwable ex )
		{
			System.out.println( "" );
			System.out.println("Error creating MST XML represenation:");
			System.out.println( "" );
			System.out.println( ex.toString() );
			ex.printStackTrace();
			System.out.println( "" );
			D2BaseUtil.exit(-1);
			
		}
	}
}
