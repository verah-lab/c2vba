package de.heuboe.by.c2vba.datex2.mst.localisation.server;

import de.heuboe.log.Logger;
import eu.datex2.schema._2._2_0.MultilingualString;
import eu.datex2.schema._2._2_0.MultilingualStringValue;

/**
 * 
 * Util class
 * 
 * @author peters
 *
 */
public final class Util 
{
	private static final Logger LOGGER = Logger.getLogger( Util.class );
	private static final int D2_MLSL = 1024;
	private static final String DE_LANG = "de";
	public static final Long CONN_TIMEOUT = 300000L;		// ms
	public static final Long RECEIVE_TIMEOUT = 300000L;		// ms
	
    private Util() {
    }
	
	/**
	 * 
	 * Converts String to Datex2 MultilingualString
	 * 
	 * @param text String
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
	 * Executes exit()
	 * 
	 * @param code     Exit code
	 */
	public static final void exit( int code )
	{
		// Ensures that asynchronous log message delivery is finished  
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {   // NOSONAR
            LOGGER.error(ex.toString());
        }

		System.exit( code );
	}
	
}
