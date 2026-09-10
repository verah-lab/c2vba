package de.heuboe.srb.datex2.srp;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import de.heuboe.c2vba.data.StrategyStates;
import de.heuboe.datex2.exception.D2Exception;
import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;
import de.heuboe.util.DirectoryCleaner;
import de.heuboe.wls.util.WlsException;
import eu.datex2.schema._2._2_0.srp.D2LogicalModel;

/**
 * 
 * Implements process logic
 *
 * @author peters
 *
 */
public class Manager implements StrategyStatesReceiver.Consumer
{
	private static final Logger LOGGER = Logger.getLogger( Manager.class );

	private PublicationMode pubMode;
	private int pubInterval;
	private long checkInterval;
	private String fileDir;
	
	private String version = "1";
	
	private Builder builder;
	private Sender sender;
	
	private Date lastSnapshot = null;
	
	private DirectoryCleaner dc; 
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param builder          	Builder
	 * @param sender         	Sender
	 * @param fileDir           Publication file directory  
	 * @param publicationMode   Publication mode  
	 * @param pubInterval       Publication interval 
	 * @param checkInterval     Check for  
	 * @throws SRPException    	Exception
	 * @throws IOException     	I/O error
	 */
	public Manager( Builder builder, 
			        Sender sender, 
			        String fileDir, 
			        PublicationMode publicationMode, 
			        int pubInterval,
			        int checkInterval )
			throws IOException, SRPException
	{
		this.sender = sender;
		this.builder = builder;
		this.pubMode = publicationMode;
		
		File dir = new File( fileDir );
		if( !dir.exists() && !dir.mkdirs() )
		{
			throw new SRPException( SRPException.ERROR_IO, 
									"Cannot create fileDir <" + fileDir + "> !" );
		}
		
		if( !dir.isDirectory() )
		{
			throw new SRPException( SRPException.ERROR_IO, 
									"fileDir <" + fileDir + "> is no directory !" );
		}
		
		this.fileDir = fileDir;
		try{
			dc = new DirectoryCleaner( fileDir );
			dc.start();
		} catch( IOException ex ) {
			throw new SRPException( SRPException.ERROR_IO, 
								    "Exception on creating DirectoryCleaner !", ex  );
			
		}
		
		this.pubInterval = pubInterval;
		this.checkInterval = checkInterval;
	}
	
	
	
	/**
	 * 
	 * Waits until next import time
	 * 
	 */
	public void waitForNextCycle()
	{
		long now = (new Date()).getTime();
		long next = now / checkInterval * checkInterval + checkInterval;
		
		LOGGER.debug( "Waiting for next period ... ( until " + (new Date(next)).toString() + " )" );

		Date nextDate = new Date( next );
		while( (new Date()).before( nextDate ) ) {
			Util.sleep( 10 );
		}
	}
	
	
	private synchronized void writePublication( Date now, boolean onChange, boolean snapshot, StrategyStates strategyStates )
			throws SRPException, WlsException
	{
		D2LogicalModel d2lm = builder.buildPublication( now, onChange, snapshot, strategyStates );
		
		if( d2lm == null )
		{
			LOGGER.debug( "" );
			LOGGER.debug( "No data change: no publication created." );
			LOGGER.debug( "" );
			
			return;
		}
		
		if( snapshot && ( pubMode == PublicationMode.OnUpdateCyclicSnapshot ) )
		{
			LOGGER.info( "Snapshot publication" );
		}
		
		String fileName = fileDir + 
						  "/D2SRPPub_" + 
						  version + 
						  "_" + now.getTime() + 
						  ".xml";
		
		try
		{
			LOGGER.info( "Write publication to <" + fileName +  ">" );
			FileUtils.write( new File(fileName), builder.toString( d2lm ) );
		} catch( IOException ex ) {
			try {
				sender.send( now, pubInterval, true, ex.toString(), null, null );
			} catch( D2Exception dex ) {
				LOGGER.fatal( "Error sending publication !");
				LOGGER.fatal( dex.toString() );
				LOGGER.fatal( CallStack.getStackTraceAsString( dex ) );
				
				Util.exit(-1);
			}
			
			throw new SRPException( SRPException.ERROR_IO, ex.toString(), ex);
		}
		
		try {
			LOGGER.info( "Send publication ..." );
			
			sender.send( now, pubInterval, false, "", d2lm, fileName );
		} catch( D2Exception dex ) {
			LOGGER.fatal( "Error sending publication !");
			LOGGER.fatal( dex.toString() );
			LOGGER.fatal( CallStack.getStackTraceAsString( dex ) );
			
			Util.exit(-1);
		}
		
	}

	
	/**
	 * 
	 * Loop
	 * 
	 * @throws SRPException	Exception
	 * @throws WlsException WLS error
	 */
	public void loop() 
	          throws SRPException, WlsException
	{
	    boolean once = false;
	    String onceStr = System.getProperty( Util.TEST_MODE_FLAG );
	    if( onceStr != null )
	    {
	        once = true;
	    }
	    
		while( true )
		{
			waitForNextCycle();
			
			synchronized( this ) {
				long n = (new Date()).getTime() / checkInterval * checkInterval;
				Date now = new Date( n );
				
				boolean snapshot = true;
				boolean onChange = false;
				
				if( lastSnapshot != null ) {    // NOSONAR
					long t = lastSnapshot.getTime();
					if( n < t + pubInterval * 1000L ) {
						onChange = true;
					}
				}
				
				if( ( pubMode != PublicationMode.CyclicComplete ) || !onChange ) {
				
					if( ( pubMode == PublicationMode.OnUpdateCyclicSnapshot ) && onChange ){
						snapshot = false; 
					}
					
					writePublication( now, onChange, snapshot, null );
					
					if( !onChange ) {
						long piMs = pubInterval * 1000L;
						long next = (new Date()).getTime() / piMs * piMs + piMs;
						LOGGER.info( "Waiting for next period ... ( until " + (new Date(next)).toString() + " )" );
	
						lastSnapshot = now;
					}
					
					if( once ) {
					    return;
					}
				}
			}
		}
	}

	@Override
	public void consume(StrategyStates strategyStates) {
		try {
			writePublication( new Date(), true, ( pubMode != PublicationMode.OnUpdateCyclicSnapshot ), strategyStates );
		} catch (SRPException | WlsException ex ) {
			LOGGER.fatal( ex.toString() );
			LOGGER.fatal( CallStack.getStackTraceAsString( ex ) );
			LOGGER.fatal( "Process will be terminated !" );
			System.exit(-1);
		}
	}
}
