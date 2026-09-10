package de.heuboe.datex2.vms.status.pub;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import de.heuboe.datex2.vms.service.NumericalValueType;
import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;
import eu.datex2.schema._2._2_0.MultilingualString;
import eu.datex2.schema._2._2_0.MultilingualStringValue;
import eu.datex2.schema._2._2_0.VmsDatexPictogramEnum;



/**
 * 
 * @author peters
 *
 * Util class
 *
 */
public final class Util 
{
	private static final Logger LOGGER = Logger.getLogger( Util.class );
	private static final int D2_MLSL = 1024;
	private static String DE_LANG = "de";

	public static final Long CONN_TIMEOUT = 300000L;		// ms
	public static final String TEST_MODE_FLAG = "de.heuboe.datex2.vms.status.pub.mode.test";
	
	@SuppressWarnings("serial")
	private static final Map< Integer, NumericalValueType > tlsCode2NumericalValueType = new HashMap< Integer, NumericalValueType >()
	{{
        put( 20, NumericalValueType.SPEED );
        put( 21, NumericalValueType.SPEED );
        put( 22, NumericalValueType.SPEED );
        put( 23, NumericalValueType.SPEED );
        put( 24, NumericalValueType.SPEED );
        put( 25, NumericalValueType.SPEED );
        put( 26, NumericalValueType.SPEED );
        put( 27, NumericalValueType.SPEED );
        put( 28, NumericalValueType.SPEED );
        put( 29, NumericalValueType.SPEED );
        put( 30, NumericalValueType.SPEED );
        put( 33, NumericalValueType.SPEED );
        put( 34, NumericalValueType.SPEED );
        put( 35, NumericalValueType.SPEED );
        put( 36, NumericalValueType.SPEED );
        put( 40, NumericalValueType.SPEED );
        put( 41, NumericalValueType.SPEED );
        put( 42, NumericalValueType.SPEED );
        put( 43, NumericalValueType.SPEED );
        put( 44, NumericalValueType.SPEED );
        put( 45, NumericalValueType.SPEED );
        put( 46, NumericalValueType.SPEED );
        put( 47, NumericalValueType.SPEED );
        put( 48, NumericalValueType.SPEED );
        put( 49, NumericalValueType.SPEED );
        put( 50, NumericalValueType.SPEED );
        put( 54, NumericalValueType.SPEED );
        put( 55, NumericalValueType.SPEED );
        put( 56, NumericalValueType.SPEED );
        put( 57, NumericalValueType.SPEED );
        put( 121, NumericalValueType.SPEED );
        put( 122, NumericalValueType.SPEED );
        put( 123, NumericalValueType.SPEED );
        put( 124, NumericalValueType.SPEED );
        put( 125, NumericalValueType.SPEED );
        put( 126, NumericalValueType.SPEED );
        put( 127, NumericalValueType.SPEED );
        put( 128, NumericalValueType.SPEED );
        put( 129, NumericalValueType.SPEED );
        put( 130, NumericalValueType.SPEED );
        put( 131, NumericalValueType.SPEED );
        put( 141, NumericalValueType.SPEED );
        put( 142, NumericalValueType.SPEED );
        put( 143, NumericalValueType.SPEED );
        put( 144, NumericalValueType.SPEED );
        put( 145, NumericalValueType.SPEED );
        put( 146, NumericalValueType.SPEED );
        put( 147, NumericalValueType.SPEED );
        put( 148, NumericalValueType.SPEED );
        put( 149, NumericalValueType.SPEED );
        put( 150, NumericalValueType.SPEED );
        put( 151, NumericalValueType.SPEED );
	}};
	
	@SuppressWarnings("serial")
	private static final Map< Integer, Double > tlsCode2NumericalValue = new HashMap< Integer, Double >()
	{{
        put( 20, 20.0 );
        put( 21, 30.0 );
        put( 22, 40.0 );
        put( 23, 50.0 );
        put( 24, 60.0 );
        put( 25, 70.0 );
        put( 26, 80.0 );
        put( 27, 90.0 );
        put( 28, 100.0 );
        put( 29, 110.0 );
        put( 30, 120.0 );
        put( 33, 130.0 );
        put( 34, 140.0 );
        put( 35, 150.0 );
        put( 36, 160.0 );
        put( 40, 20.0 );
        put( 41, 30.0 );
        put( 42, 40.0 );
        put( 43, 50.0 );
        put( 44, 60.0 );
        put( 45, 70.0 );
        put( 46, 80.0 );
        put( 47, 90.0 );
        put( 48, 100.0 );
        put( 49, 110.0 );
        put( 50, 120.0 );
        put( 54, 130.0 );
        put( 55, 140.0 );
        put( 56, 150.0 );
        put( 57, 160.0 );
        put( 121, 30.0 );
        put( 122, 40.0 );
        put( 123, 50.0 );
        put( 124, 60.0 );
        put( 125, 70.0 );
        put( 126, 80.0 );
        put( 127, 90.0 );
        put( 128, 100.0 );
        put( 129, 110.0 );
        put( 130, 120.0 );
        put( 131, 130.0 );
        put( 141, 30.0 );
        put( 142, 40.0 );
        put( 143, 50.0 );
        put( 144, 60.0 );
        put( 145, 70.0 );
        put( 146, 80.0 );
        put( 147, 90.0 );
        put( 148, 100.0 );
        put( 149, 110.0 );
        put( 150, 120.0 );
        put( 151, 130.0 );
		
	}};
	
	
	@SuppressWarnings("serial")
	private static final Map< Integer, String > tlsCode2PictogramType = new HashMap< Integer, String >()
	{{
        put( -1, "blankVoid" );
        put( 0, "other" );
        put( 1, "otherDangers" ); 				// Gefahrenstelle  
        put( 2, "unevenRoad" ); 				// Unebene Fahrbahn  
        put( 3, "slipperyRoad" ); 				// Schnee- oder Eisglätte  
        put( 4, "slipperyRoad" ); 				// Schleudergefahr bei Nässe oder Schmutz  
        put( 5, "narrowLanesAead" ); 			// verengte Fahrbahn 
        put( 6, "narrowLanesAead" ); 			// einseitig (rechts) verengte Fahrbahn 
        put( 7, "narrowLanesAead" ); 			// einseitig (links) verengte Fahrbahn 
        put( 8, "roadworks" ); 					// Baustelle 
        put( 9, "trafficCongestion" ); 			// Stau
        put( 10, "twoWayTraffic" ); 			// Gegenverkehr
        put( 11, "snow" ); 						// Schneefall
        put( 12, "lightSignals" ); 				// Lichtzeichenanlage
        put( 20, "maximumSpeedLimitedToTheFigureIndicated" ); // Zulässige Höchstgeschwindigkeit
        put( 21, "maximumSpeedLimitedToTheFigureIndicated" ); // Zulässige Höchstgeschwindigkeit
        put( 22, "maximumSpeedLimitedToTheFigureIndicated" ); // Zulässige Höchstgeschwindigkeit
        put( 23, "maximumSpeedLimitedToTheFigureIndicated" ); // Zulässige Höchstgeschwindigkeit
        put( 24, "maximumSpeedLimitedToTheFigureIndicated" ); // Zulässige Höchstgeschwindigkeit
        put( 25, "maximumSpeedLimitedToTheFigureIndicated" ); // Zulässige Höchstgeschwindigkeit
        put( 26, "maximumSpeedLimitedToTheFigureIndicated" ); // Zulässige Höchstgeschwindigkeit
        put( 27, "maximumSpeedLimitedToTheFigureIndicated" ); // Zulässige Höchstgeschwindigkeit
        put( 28, "maximumSpeedLimitedToTheFigureIndicated" ); // Zulässige Höchstgeschwindigkeit
        put( 29, "maximumSpeedLimitedToTheFigureIndicated" ); // Zulässige Höchstgeschwindigkeit
        put( 30, "maximumSpeedLimitedToTheFigureIndicated" ); // Zulässige Höchstgeschwindigkeit
        put( 31, "overtakingProhibited" ); 		// Überholverbot für Kraftfahrzeuge aller Art
        put( 32, "overtakingByGoodsVehiclesProhibited" ); // Überholverbot für Lkw
        put( 33, "maximumSpeedLimitedToTheFigureIndicated" ); // Zulässige Höchstgeschwindigkeit
        put( 34, "maximumSpeedLimitedToTheFigureIndicated" ); // Zulässige Höchstgeschwindigkeit
        put( 35, "maximumSpeedLimitedToTheFigureIndicated" ); // Zulässige Höchstgeschwindigkeit
        put( 36, "maximumSpeedLimitedToTheFigureIndicated" ); // Zulässige Höchstgeschwindigkeit
        put( 38, "other" ); 					// Verkehrsverbot bei Smog oder zur Verminderung 
        put( 39, "noEntry" ); 					// Verbot für Fahrzeuge aller Art 
        put( 40, "endOfSpeedLimit" ); 			// Ende der zulässigen Höchstgeschwindigkeit
        put( 41, "endOfSpeedLimit" ); 			// Ende der zulässigen Höchstgeschwindigkeit
        put( 42, "endOfSpeedLimit" ); 			// Ende der zulässigen Höchstgeschwindigkeit
        put( 43, "endOfSpeedLimit" ); 			// Ende der zulässigen Höchstgeschwindigkeit
        put( 44, "endOfSpeedLimit" ); 			// Ende der zulässigen Höchstgeschwindigkeit
        put( 45, "endOfSpeedLimit" ); 			// Ende der zulässigen Höchstgeschwindigkeit
        put( 46, "endOfSpeedLimit" ); 			// Ende der zulässigen Höchstgeschwindigkeit
        put( 47, "endOfSpeedLimit" ); 			// Ende der zulässigen Höchstgeschwindigkeit
        put( 48, "endOfSpeedLimit" ); 			// Ende der zulässigen Höchstgeschwindigkeit
        put( 49, "endOfSpeedLimit" ); 			// Ende der zulässigen Höchstgeschwindigkeit
        put( 50, "endOfSpeedLimit" ); 			// Ende der zulässigen Höchstgeschwindigkeit
        put( 51, "endOfProhibitionOfOvertaking" ); // Ende Überholverbot für Kraftfahrzeuge aller Art
        put( 52, "endOfProhibitionOfOvertakingForGoodsVehicles" ); // Ende Überholverbot für Lkw
        put( 53, "other" ); // Ende sämtlicher Streckenverbote
        put( 54, "endOfSpeedLimit" ); // Ende der zulässigen Höchstgeschwindigkeit
        put( 55, "endOfSpeedLimit" ); // Ende der zulässigen Höchstgeschwindigkeit
        put( 56, "endOfSpeedLimit" ); // Ende der zulässigen Höchstgeschwindigkeit
        put( 57, "endOfSpeedLimit" ); // Ende der zulässigen Höchstgeschwindigkeit
        put( 61, "other" ); // gelbes Blinklicht
        put( 62, "trafficCongestion" ); // Zustatztext STAU
        put( 63, "queue" ); // Zustatztext STAUGEFAHR
        put( 64, "fog" ); // Zustatztext NEBEL
        put( 65, "rain" ); // Zustatztext NÄSSE
        put( 66, "accident" ); // Zustatztext UNFALL
        put( 67, "other" ); // Zustatztext SICHT
        put( 68, "other" ); // Zustatztext SMOG
        put( 69, "other" ); // Zustatztext ROLLSPLIT
        put( 70, "other" ); // Zustatztext MÄHARBEITEN
        put( 71, "other" ); // Zustatztext Ozon
        put( 72, "other" ); // Zustatztext Lärmschutz
        put( 74, "other" ); // Zustatztext 600m
        put( 78, "other" ); // Zustatztext 200m
        put( 79, "other" ); // Zustatztext 300m
        put( 80, "other" ); // Zustatztext 400m
        put( 81, "other" ); // Zustatztext 500m
        put( 82, "other" ); // Zustatztext 1000m
        put( 83, "other" ); // Zustatztext 1500m
        put( 84, "other" ); // Zustatztext 2000m
        put( 85, "other" ); // Zustatztext 2500m
        put( 86, "other" ); // Zustatztext 3000m
        put( 87, "other" ); // Zustatztext 4000m
        put( 88, "other" ); // Zustatztext 5000m
        put( 89, "other" ); // Zustatztext  nach … m
        put( 91, "other" ); // Zustatztext auf 500m
        put( 92, "other" ); // Zustatztext auf 1000m
        put( 93, "other" ); // Zustatztext auf 1500m
        put( 94, "other" ); // Zustatztext auf 2000m
        put( 95, "other" ); // Zustatztext auf 2500m
        put( 96, "other" ); // Zustatztext auf 3000m
        put( 97, "other" ); // Zustatztext auf 4000m
        put( 98, "other" ); // Zustatztext auf 5km
        put( 99, "other" ); // Zustatztext auf … m
        put( 100, "other" ); // Zustatztext 2,8t
        put( 101, "other" ); // Zustatztext 4t
        put( 102, "other" ); // Zustatztext 7,5t
        put( 103, "other" ); // Gefahr unerwarteter Glatteisbildung (Piktogramm) 
        put( 104, "other" ); // nur Lkw (Piktogramm) 
        put( 105, "other" ); // nur Pkw (Piktogramm) 
        put( 106, "queue" ); // Staugefahr (Piktogramm) 
        put( 107, "other" ); // rot
        put( 108, "other" ); // gelb
        put( 109, "other" ); // grün
        put( 110, "other" ); // rot/gelb
        put( 111, "laneClosed" ); // Fahrstreifen gesperrt = diagonales Kreuz rot 
        put( 112, "laneOpen" ); // Fahrstreifen halten = Pfeil nach unten grün
        put( 113, "laneDeviationToLeft" ); // Fahrstreifen wechseln = gelber Pfeil nach links 
        put( 114, "laneDeviationToRight" ); // Fahrstreifen  wechseln  =  gelber  Pfeil  nach  rechts unten
        put( 115, "other" ); // 1 Fahrzeug bei Grün
        put( 116, "other" ); // 2 Fahrzeuge bei Grün
        put( 121, "advisorySpeed" ); // Richtgeschwindigkeit
        put( 122, "advisorySpeed" ); // Richtgeschwindigkeit
        put( 123, "advisorySpeed" ); // Richtgeschwindigkeit
        put( 124, "advisorySpeed" ); // Richtgeschwindigkeit
        put( 125, "advisorySpeed" ); // Richtgeschwindigkeit
        put( 126, "advisorySpeed" ); // Richtgeschwindigkeit
        put( 127, "advisorySpeed" ); // Richtgeschwindigkeit
        put( 128, "advisorySpeed" ); // Richtgeschwindigkeit
        put( 129, "advisorySpeed" ); // Richtgeschwindigkeit
        put( 130, "advisorySpeed" ); // Richtgeschwindigkeit
        put( 131, "advisorySpeed" ); // Richtgeschwindigkeit
        put( 141, "endOfAdvisorySpeed" ); // Ende der Richtgeschwindigkeit 
        put( 142, "endOfAdvisorySpeed" ); // Ende der Richtgeschwindigkeit 
        put( 143, "endOfAdvisorySpeed" ); // Ende der Richtgeschwindigkeit 
        put( 144, "endOfAdvisorySpeed" ); // Ende der Richtgeschwindigkeit 
        put( 145, "endOfAdvisorySpeed" ); // Ende der Richtgeschwindigkeit 
        put( 146, "endOfAdvisorySpeed" ); // Ende der Richtgeschwindigkeit 
        put( 147, "endOfAdvisorySpeed" ); // Ende der Richtgeschwindigkeit 
        put( 148, "endOfAdvisorySpeed" ); // Ende der Richtgeschwindigkeit 
        put( 149, "endOfAdvisorySpeed" ); // Ende der Richtgeschwindigkeit 
        put( 150, "endOfAdvisorySpeed" ); // Ende der Richtgeschwindigkeit 
        put( 151, "endOfAdvisorySpeed" ); // Ende der Richtgeschwindigkeit 
        put( 200, "other" ); // Pfeil nach rechts
        put( 201, "other" ); // anlagenspezifische WVZ-Codes 0 - 19 
        put( 202, "other" ); // anlagenspezifische WVZ-Codes 0 - 19 
        put( 203, "other" ); // anlagenspezifische WVZ-Codes 0 - 19 
        put( 204, "other" ); // anlagenspezifische WVZ-Codes 0 - 19 
        put( 205, "other" ); // anlagenspezifische WVZ-Codes 0 - 19 
        put( 206, "other" ); // anlagenspezifische WVZ-Codes 0 - 19 
        put( 207, "other" ); // anlagenspezifische WVZ-Codes 0 - 19 
        put( 208, "other" ); // anlagenspezifische WVZ-Codes 0 - 19 
        put( 209, "other" ); // anlagenspezifische WVZ-Codes 0 - 19 
        put( 210, "other" ); // anlagenspezifische WVZ-Codes 0 - 19 
        put( 211, "other" ); // anlagenspezifische WVZ-Codes 0 - 19 
        put( 212, "other" ); // anlagenspezifische WVZ-Codes 0 - 19 
        put( 213, "other" ); // anlagenspezifische WVZ-Codes 0 - 19 
        put( 214, "other" ); // anlagenspezifische WVZ-Codes 0 - 19 
        put( 215, "other" ); // anlagenspezifische WVZ-Codes 0 - 19 
        put( 216, "other" ); // anlagenspezifische WVZ-Codes 0 - 19 
        put( 217, "other" ); // anlagenspezifische WVZ-Codes 0 - 19 
        put( 218, "other" ); // anlagenspezifische WVZ-Codes 0 - 19 
        put( 219, "other" ); // anlagenspezifische WVZ-Codes 0 - 19 
        put( 220, "other" ); // Schrankenantrieb abgestellt 
        put( 221, "other" ); // Schranke in Nullstellung
        put( 222, "other" ); // Schranke in Sperrstellung 
        put( 223, "other" ); // Schranke in Leitstellung oder MLK einschalten
        put( 241, "other" ); // Prisma Seite 1 
        put( 242, "other" ); // Prisma Seite 2
        put( 243, "other" ); // Prisma Seite 3
        put( 244, "other" ); // Prisma Seite 4
        put( 245, "other" ); // undefinierte Stellung (nur Meldung)
        put( 251, "other" ); // Wartungsstellung (Rollo geschlossen) 
	}};
	
    String[] wvzToDatexEnum = {
        };
	
	
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
	
	public static final String getThisHostNamne()
	{
		try
		{
			return InetAddress.getLocalHost().getHostName();
		}
		catch( UnknownHostException ex )
		{
			LOGGER.fatal( "Error in getThisHostNamne()" );
			LOGGER.fatal( ex.toString() );
			LOGGER.fatal( CallStack.getStackTraceAsString( ex ) );
			
			Util.exit(-1);
			return null;
		}
	}
	
	/**
	 * 
	 * Throwable to String
	 * 
	 * @param ex   Throwable
	 * @return	   Error description 	 	
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
	
	public static Integer toInteger( String value )
	{
		try
		{
			return Integer.parseInt( value );
		}
		catch( NumberFormatException ex )
		{
			return null;
		}
	}
	
	public static VmsDatexPictogramEnum tlsCode2VmsPictogramType( int tlsCode )
	{
		String typeName = tlsCode2PictogramType.get( tlsCode );
		
		if( typeName == null )
			return VmsDatexPictogramEnum.OTHER;
					
		return VmsDatexPictogramEnum.fromValue( typeName );
	}
	
	public static NumericalValueType tlsCode2NumericalValueType( int tlsCode )
	{
		return tlsCode2NumericalValueType.get( tlsCode );
	}
	
	public static Double tlsCode2NumericalValue( int tlsCode )
	{
		return tlsCode2NumericalValue.get( tlsCode );
	}
	
}
