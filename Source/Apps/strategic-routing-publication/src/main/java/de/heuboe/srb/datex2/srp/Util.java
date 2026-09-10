package de.heuboe.srb.datex2.srp;

import eu.datex2.schema._2._2_0.srp.MultilingualString;
import eu.datex2.schema._2._2_0.srp.MultilingualStringValue;



/**
 * 
 * @author peters
 *
 * Util class
 *
 */
public final class Util 
{
	private static final int D2_MLSL = 1024;
	private static final String DE_LANG = "de";
	private static final String EN_LANG = "en";

	public static final Long CONN_TIMEOUT = 300000L;		// ms
	public static final String TEST_MODE_FLAG = "de.heuboe.hpa.ftp.download.mode.test";
	
	private Util()
	{
		
	}
	
	/**
	 * 
	 * Executes exit()
	 * 
	 * @param code  exit code
	 */
	@SuppressWarnings("deprecation")
	public static final void exit( int code )
	{
		// Trying to ensure that logging messages are ejected  
		pause();

		de.heuboe.system.System.exit( code );
	}
	
	/**
	 * 
	 * executes Sleep
	 * 
	 * @param ms  milliseconds
	 */
	public static final void sleep( int ms )
	{
		try
		{ 
			Thread.sleep( ms ); 
		} 
		catch( Throwable ex ) // NOSONAR: sleep without exception handling
		{					  // NOSONAR: sleep without exception handling
		}  
	}
	
	/**
	 * 
	 * executes Sleep for 5000 ms
	 * 
	 */
	public static final void pause()
	{
		try
		{ 
			Thread.sleep( 5000 ); 
		} 
		catch( Throwable ex ) // NOSONAR: sleep without exception handling
		{					  // NOSONAR: sleep without exception handling
		}  
	}
	
	/**
	 * 
	 * Throwable to String
	 * 
	 * @param ex   throwable
	 * @return
	 */
	public static String toString( Throwable ex )
	{
		String msg = ex.getMessage();
		if( msg == null )
		{
			msg = Util.toString(ex);
		}
		
		return msg;
	}
	
	/**
	 * 
	 * Converts string to Datex2 MultilingualString
	 * 
	 * @param text string
	 * @return Datex2 MultilingualString
	 */
	public static MultilingualString toD2String( String text )
	{
		MultilingualString mls = new MultilingualString();
		mls.setValues( new MultilingualString.Values() );
		
		int numParts = 1;
		if( text.length() > 0 )
		{
			numParts = (text.length() - 1) / D2_MLSL + 1;
		}
		
		for( int i = 0; i < numParts; i++ )
		{
			MultilingualStringValue mlsv = new MultilingualStringValue();
			mlsv.setLang( DE_LANG );
			mlsv.setValue( text.substring( i * D2_MLSL, 
										   Math.min( (i+1) * D2_MLSL, text.length() ) ) );
		
			mls.getValues().getValue().add( mlsv );
		}
		
		return mls;
	}
	
	/**
	 * 
	 * Set German and English text
	 * 
	 * @param textDe German text
	 * @param textEn English text

	 * @return Datex2 MultilingualString
	 */
	public static MultilingualString toD2String( String textDe, String textEn )
	{
		MultilingualString mls = new MultilingualString();
		mls.setValues( new MultilingualString.Values() );
		
		MultilingualStringValue mlsv = new MultilingualStringValue();
		mlsv.setLang( DE_LANG );
		mlsv.setValue( textDe );
		mls.getValues().getValue().add( mlsv );
		
		mlsv = new MultilingualStringValue();
		mlsv.setLang( EN_LANG );
		mlsv.setValue( textEn );
		mls.getValues().getValue().add( mlsv );

		return mls;
	}
	
	
}
