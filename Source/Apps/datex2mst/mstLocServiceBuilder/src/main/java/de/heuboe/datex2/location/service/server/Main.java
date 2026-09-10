package de.heuboe.datex2.location.service.server;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import de.heuboe.datex2.config.persistence.D2MstPersistence;
import de.heuboe.datex2.location.service.LocationService;
import de.heuboe.datex2.mst.builder.D2MSTConf;
import de.heuboe.datex2.mst.builder.ws.D2MSTDataProvider;
import de.heuboe.datex2.mst.builder.ws.D2MSTPubService;
import de.heuboe.datex2.mst.builder.ws.Properties;
import de.heuboe.datex2.push.D2PubSenderMDM;
import de.heuboe.log.Logger;
import de.heuboe.mst.config.MappingConfig;
import de.heuboe.mst.definition.MstDefinition;
import de.heuboe.system.CallStack;

/**
 * 
 * Creation of MSt as command line application
 * 
 * @author peters
 *
 */
@SpringBootApplication(exclude = { D2PubSenderMDM.class })
@ComponentScan( basePackages = { "de.heuboe.datex2.location.service.server" } )               
public class Main
{
	private static final Logger LOGGER = Logger.getLogger( Main.class );
	
	@Autowired
	D2MstPersistence d2MstPersistence;
	
	@Autowired
	OpenLRPostProcessor postProcessor;
	
	@Autowired
	D2MSTDataProvider mstDataProvider;
	
	@Autowired
	D2MSTPubService d2MSTPubService;
	
	@Autowired
	LocationService locService;

	@Autowired
	Properties properties;

	/**
	 * 
	 * Main
	 * 
	 * @param args	Command line arguments
	 */
	public static void main(String[] args)
	{
		LOGGER.info( "Start SpringApplication ..." );
		
		ConfigurableApplicationContext ctx = SpringApplication.run(Main.class,args);
		
		Main main = ctx.getBean( Main.class );
		main.createMst();
	}

	
	/**
	 * 
	 * Creates MST
	 * 
	 */
	public void createMst() {
		try
		{
			MappingConfig mc = d2MstPersistence.getMSTConfig( properties.getD2Version() );
			MstDefinition md = d2MstPersistence.getMSTDefinition( properties.getMstId(), 
					                                              properties.getDefVersion() );
			
			MstBuilder mstBuilder = new MstBuilder();
			MstContentExt mstContent = mstBuilder.build( locService, 
					                             	     properties.getD2Version(),
					                             	     properties.getMstId(),
					                             	     properties.getDefVersion(),
					                             	     properties.getMstVersion(),
					                             	     mc, 
					                             	     md, 
					                             	     properties.isObjectIdAsD2SiteId() );
			
			d2MstPersistence.saveMst( mstContent );
			
			mstDataProvider.setData( mstContent );
			
			postProcessor.setSites( mstContent.getSites() );
			
			LOGGER.info( "");
			LOGGER.info( "Start createMSTPublicationFile() ...");
			
			d2MSTPubService.createMSTPublicationFileDef( properties.getMstId(), 
													     properties.getMstVersion(),
													     properties.getDefVersion(),
													     properties.getNatId(),
											     		 D2MSTConf.PREFERRED_ENCODING_ALERTC );

			LOGGER.info( "End of createMSTPublicationFile()");
			LOGGER.info( "");
			
			LOGGER.info( "");
			LOGGER.info( "");
			LOGGER.info( "MST successfully created !");
			LOGGER.info( "");
			LOGGER.info( "");
			
			System.exit(1);
		} catch( Throwable ex ) {   // NOSONAR
			LOGGER.info( "");
			LOGGER.info( "");
			LOGGER.info( "Error creating MST:");
			LOGGER.info( "");
			LOGGER.info( "");
			LOGGER.fatal( ex.toString() );
			LOGGER.fatal( CallStack.getStackTraceAsString( ex ) );
			System.exit( -1 );
		}
		
	}
}
