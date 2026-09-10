package de.heuboe.datex2.vms.table.pub;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;




import de.heuboe.hbmonitor.HbMonitor;
import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;
import de.heuboe.system.CallStack.DetailLevel;
import eu.datex2.schema._2._2_0.vms.D2LogicalModel;

/**
 * 
 * @author peters
 * 
 * main() class
 *
 */
public class Main
{
	@SuppressWarnings("unused")
	private static final Logger LOGGER_VMS = Logger.getLogger( "de.heuboe.datex2.vms" );
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
			Main ec = new Main();
			ec.init(args);
			
			HbMonitor.setRunning();
			
			ec.buildPub();
		}
		catch( Throwable ex )     // NOSONAR
		{
			LOGGER.fatal( "Error initialising:" );
			LOGGER.fatal( ex.toString() );
			LOGGER.fatal( CallStack.getStackTraceAsString( ex, "", DetailLevel.FULL ) );
			LOGGER.fatal( "Process will be terminated !" );
			
			Util.exit(-1);
		}
		
		Util.exit( 0 );
	}
	
	/**
	 * 
	 * Creates manager object
	 * 
	 * @param args				process parameters
	 * @return					manager object
	 * @throws VMSException		exception	
	 */
	public void init( String[]  args )
			throws VMSException
	{
		Parameter.createInstance( args );
	}
	
	public void buildPub()
			throws VMSException
	{
		String fileDir = Parameter.instance().getFileDir();
		
		File dir = new File( fileDir );
		if( !dir.exists() ) {
			if( !dir.mkdirs() ) {  // NOSONAR
				throw new VMSException( VMSException.ERROR_IO, 
										"Cannot create fileDir <" + fileDir + "> !" );
			}
		}
		
		if( !dir.isDirectory() ) {
			throw new VMSException( VMSException.ERROR_IO, 
									"fileDir <" + fileDir + "> is no directory !" );
		}
		
		Publisher publisher = new Publisher();
		publisher.init();
		
		LOGGER.info( "" );
		D2LogicalModel pub = publisher.getPublication();
		LOGGER.info( "" );
		String pubString = publisher.toString( pub );
		LOGGER.info( "" );
		
		String fileName = fileDir + 
						  "/D2VMTPub_" + 
						  Parameter.instance().getTableId() + "_" + 
						  publisher.getTableVersion() + 
						  ".xml";
		
		try
		{
			LOGGER.info( "Write publication to file <" + fileName + ">" );
			FileUtils.write( new File(fileName), pubString );
		}
		catch( IOException ex )
		{
			throw new VMSException( VMSException.ERROR_XML, ex.toString(), ex);
		}
		
		LOGGER.info( "Publication successfully generated !" );
	}
}
