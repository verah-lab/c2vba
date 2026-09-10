package de.heuboe.datex2.shp.writer.shapefile;

import java.io.FileNotFoundException;

import de.heuboe.arg.ArgParser;
import de.heuboe.arg.ArgParserError;
import de.heuboe.arg.Mand;
import de.heuboe.arg.Opt;
import de.heuboe.arg.PropertyFile;
import de.heuboe.datex2.base.D2Exception;
import de.heuboe.ddp.Connection;
import de.heuboe.ddp.DDPException;
import de.heuboe.log.Logger;

/**
 * 
 * Holds process's command line parameter  
 * 
 * 
 * @author peters
 *
 */
public final class Parameter 
{
	private static final Logger LOGGER = Logger.getLogger( Parameter.class );
	
	private static final String DB = "db"; 
	private static final String DBMS = "dbms"; 
	private static final String SERVER = "server"; 
	private static final String USER = "user"; 
	private static final String PASSWD = "passwd"; 
	private static final String ENV = "env"; 
	private static final String SPECIAL = "special"; 
	private static final String JDBC_URL = "JdbcUrl";
	
	private static String argPref = "de.heuboe.datex2.shp.writer.";
	private static String argSystemPref = "de.heuboe.system.";
	
	private String mstId;
	private String mstVersion;
	private String locStatelessServerAddress;
	private String shapefilePath;

	private ArgParser ap; 
	private Connection connection;
	
	
	private static Parameter instance = null;
	
	private String pref( String argName )
	{
		if( DB.equals( argName ) 		
			||             				
			DBMS.equals( argName ) || 
			SERVER.equals( argName ) ||  
			USER.equals( argName ) ||  
			PASSWD.equals( argName ) || 
			SPECIAL.equals( argName ) || 
			ENV.equals( argName )  || 
			JDBC_URL.equals( argName ) )
		{										
			return argSystemPref + argName;
		}

			
		return argPref + argName;
	}
	
	private Parameter( String[] args )
			throws D2Exception
	{
		ap = new ArgParser( "FTPTransfer", "", args.clone() );
		
		try
		{
			String propFile = System.getProperty( argPref + "properties" );
			if( propFile == null )
			{
				LOGGER.info( "Property file not defined: -D" + argPref + "properties" );
				LOGGER.info( "Non-prefixed, simple argument names in .arg file or on command line assumed" );
				argPref = "";
				argSystemPref = "";
			}
			else
			{
				ap.addSource( new PropertyFile( propFile ) );
			}
			
			ap.addArgumentDefinition( new Opt( pref( "program"), "Application name", "d2ShpFileWriter" ) );
			
			ap.addArgumentDefinition( new Opt( pref(ENV), "GeoDyn2 environment" ) );
			ap.addArgumentDefinition( new Opt( pref(DBMS), "DBMS" ) );
			
			ap.addArgumentDefinition( new Opt( pref(SERVER), "DB server name" ) );
			ap.addArgumentDefinition( new Opt( pref("db"), "Database name" ) );
			
			ap.addArgumentDefinition( new Opt( pref(USER), "Database user name" ) );
			ap.addArgumentDefinition( new Opt( pref(PASSWD), "Database user password" ) );
			ap.addArgumentDefinition( new Opt( pref("special"), "Allows multiple instances of this application in one GeoDyn2 environment", "" ) );
			
			ap.addArgumentDefinition( new Mand( pref("mstId"), "MST-ID" ) );
			ap.addArgumentDefinition( new Mand( pref("mstVersion"), "MST-Version" ) );
			ap.addArgumentDefinition( new Mand( pref("locStatelessServerAddress"), "CORBA-NamingService address of locStatelessServer" ) );
			ap.addArgumentDefinition( new Mand( pref("shapefilePath"), "Shapefile path" ) );

						
			ap.parse();
			
			mstId = ap.getValue( pref("mstId") ).getAsString();
			mstVersion = ap.getValue( pref("mstVersion") ).getAsString();
			locStatelessServerAddress = ap.getValue( pref("locStatelessServerAddress") ).getAsString();
			shapefilePath = ap.getValue( pref("shapefilePath") ).getAsString();
			
			connection = createConnection();
		}
		catch( ArgParserError ex )
		{
			String errMsg = "ArgParser exception: " + ex.getMessage();
			LOGGER.error( errMsg );
			
			D2Exception nex = new D2Exception( D2Exception.D2_ERR_GENERAL_PUB_EXCEPTION, 
					                           "Error parsing arguments:\n" + ex.getMessage(), 
					                           ex );
			throw nex;
		}
		catch( FileNotFoundException ex )
		{
			String errMsg = "ArgParser exception: " + ex.getMessage();
			LOGGER.error( errMsg );
			
			D2Exception nex = new D2Exception ( D2Exception.D2_ERR_GENERAL_PUB_EXCEPTION,
												"Error parsing arguments:\n" + ex.getMessage(), 
												ex );
			throw nex;
		}
	}
	
	/**
	 * 
	 * Creates a DDP connection
	 * 
	 * @return DDP connection
	 * @throws DDPException DDP exception
	 */
	public Connection createConnection()
			throws D2Exception
	{
		try
		{
			return new Connection( ap.getValue( pref(DBMS) ).getAsString(),
								   ap.getValue( pref("db") ).getAsString(),
								   ap.getValue( pref(SERVER) ).getAsString(),
								   ap.getValue( pref(USER) ).getAsString(),
								   ap.getValue( pref(PASSWD) ).getAsString(),
								   ap.getValue( pref("program") ).getAsString(),
								   ap.getValue( pref("special") ).getAsString(),
								   ap.getValue( pref(ENV) ).getAsString(),
								   0 );
		}
		catch( DDPException ex )
		{
			throw new D2Exception( D2Exception.D2_ERR_GENERAL_DDP_EXCEPTION, "DDP-Error connecting to database", ex );
		}
	}

	
	/**
	 * 
	 * Creates single instance of class
	 * 
	 * @param args process command line parameters
	 * @return single instance of class
	 * @throws NDWExportException NDW export exception
	 */
	public static Parameter createInstance( String[] args )
			throws D2Exception
	{
		if( instance == null )
		{
			instance = new Parameter( args.clone() );
		}
		
		return instance;
	}

	/**
	 * 
	 * Returns single instance of class
	 * 
	 * @return single instance of class
	 */
	public static Parameter instance()			
	{
		return instance;
	}

	public Connection getConnection() 				
	{
		return connection;
	}

	public String getMstId()
	{
		return mstId;
	}
	
	public String getMstVersion()
	{
		return mstVersion;
	}

	public String getLocStatelessServerAddress()
	{
		return locStatelessServerAddress;
	}

	public String getShapefilePath()
	{
		return shapefilePath;
	}
}
