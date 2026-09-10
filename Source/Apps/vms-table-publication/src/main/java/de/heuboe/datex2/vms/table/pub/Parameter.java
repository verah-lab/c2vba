package de.heuboe.datex2.vms.table.pub;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import de.heuboe.arg.ArgParser;
import de.heuboe.arg.ArgParserError;
import de.heuboe.arg.Flag;
import de.heuboe.arg.Mand;
import de.heuboe.arg.Opt;
import de.heuboe.arg.OptMany;
import de.heuboe.arg.PropertyFile;
import de.heuboe.data.Data;
import de.heuboe.datex2.vms.service.VMSTableConfiguration.DWiStaMode;
import de.heuboe.datex2.vms.service.VMSTableConfiguration.DWiStaTextArea;
import de.heuboe.datex2.vms.service.VMSTableConfiguration.DWiStaTextAreaImage;
import de.heuboe.datex2.vms.service.VMSTableConfiguration.DWiStaTextAreaLayout;
import de.heuboe.datex2.vms.service.table.data.D2VMSDisplay.DisplayPosition;
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
	
	private static final String JDBC_URL = "JdbcUrl";
	
	private static String argPref = "de.heuboe.datex2.vms.table.pub.";
	private static String argSystemPref = "de.heuboe.system.";

	private String tableId = ""; 
	private String tableName = ""; 
	private String tableVersion = ""; 
	private List<String> filterVBAs = new ArrayList<>();
	private List<String> filterAQs = new ArrayList<>();
	private DWiStaMode dWiStaMode = null;
	private List<DWiStaTextAreaLayout> dWiStaTextAreaLayouts = new ArrayList<>();
	private String cfgFile = "";
	
	private long connTimeout = 60L * 1000L;			// ms: 1 minute
	private String fileDir;
	private String sender;
	
	private String vmsServiceUrl;
	private String d2SchemaLocation;
	
	private boolean addTmcLocations;
	private boolean addOpenLRLocations;

	private ArgParser ap; 
	
	
	private static Parameter instance = null;
	
	private String pref( String argName )
	{
		if( JDBC_URL.equals( argName ) )
		{										// NOSONAR: clearly arranged
			return argSystemPref + argName;
		}

			
		return argPref + argName;
	}
	
	private DisplayPosition parsePosition( String posStr ) throws VMSException {
		
		String p = posStr.trim();
		if( !p.startsWith( "POS" ) ) {
			throw new VMSException( VMSException.ERROR_ARG_PARSER, "Invalid dWiStaTextAreaLayout definition <" + posStr + ">" );
		}
		
		p = p.substring( 3 ).trim();
		if( !p.startsWith( "(" ) || !p.endsWith( ")" ) ) {
			throw new VMSException( VMSException.ERROR_ARG_PARSER, "Invalid dWiStaTextAreaLayout definition <" + posStr + ">" );
		}
		
		p = p.substring( 1, p.length() - 1 );
		
		try {
			return DisplayPosition.valueOf( p );
		} catch( Throwable ex ) {
			throw new VMSException( VMSException.ERROR_ARG_PARSER, "Invalid dWiStaTextAreaLayout definition <" + posStr + ">" );
		}
	}
	
	private Integer parseTextItem( String textDes ) throws VMSException {
		String t = textDes.trim();
		if( !t.startsWith( "TEXT" ) ) {
			throw new VMSException( VMSException.ERROR_ARG_PARSER, "Invalid dWiStaTextAreaLayout definition <" + textDes + ">" );
		}
		
		t = t.substring( 4 ).trim();
		if( !t.startsWith( "(" ) || !t.endsWith( ")" ) ) {
			throw new VMSException( VMSException.ERROR_ARG_PARSER, "Invalid dWiStaTextAreaLayout definition <" + textDes + ">" );
		}
		
		t = t.substring( 1, t.length() - 1 );
		
		try {
			return Integer.parseInt( t.trim() );
		} catch( Throwable ex ) {
			throw new VMSException( VMSException.ERROR_ARG_PARSER, "Invalid dWiStaTextAreaLayout definition <" + textDes + ">" );
		}
	}
	
	private DWiStaTextAreaImage parseImageItem( String imageStr, int row ) throws VMSException {
		
		String i = imageStr.trim();
		if( !i.startsWith( "IMAGE" ) ) {
			throw new VMSException( VMSException.ERROR_ARG_PARSER, "Invalid dWiStaTextAreaLayout definition <" + imageStr + ">" );
		}
		
		i = i.substring( 5 ).trim();
		if( !i.startsWith( "(" ) || !i.endsWith( ")" ) ) {
			throw new VMSException( VMSException.ERROR_ARG_PARSER, "Invalid dWiStaTextAreaLayout definition <" + imageStr + ">" );
		}
		
		i = i.substring( 1, i.length() - 1 );
		
		
		try {
			DisplayPosition dp = DisplayPosition.valueOf( i.trim() );
			
			return new DWiStaTextAreaImage( row, dp );
			
		} catch( Throwable ex ) {
			throw new VMSException( VMSException.ERROR_ARG_PARSER, "Invalid dWiStaTextAreaLayout definition <" + imageStr + ">" );
		}
	}
	
	
	
	private DWiStaTextAreaLayout parseTextAreaLayout( String tal ) throws VMSException {
	
		// { POS(LEFT) / TEXT(3) / IMAGE(TOP_LEFT) / IMAGE(BOTTOM_LEFT) } | { POS(RIGHT) / TEXT(3) / IMAGE(TOP_RIGHT) / IMAGE(BOTTOM_RIGHT) }

		String[] textAreas = tal.split( "\\|" ); 
		if( ( textAreas == null ) || ( textAreas.length == 0 ) ) {
			throw new VMSException( VMSException.ERROR_ARG_PARSER, "Invalid dWiStaTextAreaLayout definition <" + tal + ">" );
		}
		
		List<DWiStaTextArea> dWistaTas = new ArrayList<>();
		
		int taIndex = 1;
		for( String ta : textAreas ) {
			String textArea = ta.trim();
			if( !textArea.startsWith( "{" ) || !textArea.endsWith( "}" ) ) {
				throw new VMSException( VMSException.ERROR_ARG_PARSER, "Invalid dWiStaTextAreaLayout definition <" + tal + ">" );
			}
			
			textArea = textArea.substring( 1, textArea.length() - 1 );

			String[] parts = textArea.split( "/" ); 
			if( ( parts == null ) || ( parts.length < 2 ) ) {
				throw new VMSException( VMSException.ERROR_ARG_PARSER, "Invalid dWiStaTextAreaLayout definition <" + tal + ">" );
			}
			
			DisplayPosition dp = null;
			Integer numLines = null;
			List<DWiStaTextAreaImage> images = new ArrayList<>();
			
			int row = 1;
			for( String p : parts ) {
				String part = p.trim();
				if( part.startsWith( "POS" ) ) {
					dp = parsePosition( part );
				} else if( part.startsWith( "TEXT" ) ) {
					numLines = parseTextItem( part );
				} else if( part.startsWith( "IMAGE" ) ) {
					DWiStaTextAreaImage image = parseImageItem( part, row );
					images.add( image );
					row++;
				} else {
					throw new VMSException( VMSException.ERROR_ARG_PARSER, "Invalid dWiStaTextAreaLayout definition <" + tal + ">" );
				}
			}
			
			if( dp == null ) {
				throw new VMSException( VMSException.ERROR_ARG_PARSER, "Invalid dWiStaTextAreaLayout definition <" + tal + ">" );
			}
			if( numLines == null ) {
				throw new VMSException( VMSException.ERROR_ARG_PARSER, "Invalid dWiStaTextAreaLayout definition <" + tal + ">" );
			}
			
			DWiStaTextArea dWistaTa = new DWiStaTextArea( taIndex,
					                                      dp, 
					                                      numLines, 
					                                      images );
			dWistaTas.add( dWistaTa );
			
			taIndex++;
		}
		
		return new DWiStaTextAreaLayout (dWistaTas );
	}
	
	private Parameter( String[] args )
			throws VMSException
	{
		ap = new ArgParser( "FTPTransfer", "", args.clone() );
		
		try
		{
			String propFile = System.getProperty( "de.heuboe.datex2.vms.table.pub.properties" );
			if( propFile == null )
			{
				LOGGER.info( "Property file not defined: -Dde.heuboe.hpa.ftp.download.properties" );
				LOGGER.info( "Non-prefixed, simple argument names in .arg file or on command line assumed" );
				argPref = "";
				argSystemPref = "";
			}
			else
			{
				ap.addSource( new PropertyFile( propFile ) );
			}
			
			ap.addArgumentDefinition( new Opt( pref( "program"), "Application name", "FTPTransfer" ) );
			
			ap.addArgumentDefinition( new Mand( pref("tableId"), "Unit table id" ) );
			ap.addArgumentDefinition( new Opt( pref("tableName"), "Unit table id" ) );
			ap.addArgumentDefinition( new Mand( pref("tableVersion"), "Unit table version" ) );
			ap.addArgumentDefinition( new OptMany( pref("filterVBA"), "ID of VBA VMS table publication is restricted to" ) );
			ap.addArgumentDefinition( new OptMany( pref("filterAQ"), "ID of AQ VMS table publication is restricted to" ) );
			ap.addArgumentDefinition( new Opt( pref("cfgFile"), "Configuration file (extends configuration provided by system)" ) );
			ap.addArgumentDefinition( new Opt( pref("sender"), "Datex2 <nationalIdentifier>", "" ) );
			
			ap.addArgumentDefinition( new Opt( pref("connTimeout"), "Timout of web service connection", 1800000 ) );
			
			ap.addArgumentDefinition( new Opt( pref("d2SchemaLocation" ), "DATEX-II schema location", "" ) );

			ap.addArgumentDefinition( new Mand( pref("fileDir"), "Publication file directory" ) );
			ap.addArgumentDefinition( new Mand( pref("vmsServiceUrl"), "Url of VMS-Service" ) );

			ap.addArgumentDefinition( new Flag( pref("addTmcLocations" ), "RDS/TMC locations shall be added for VMS units" ) );
			ap.addArgumentDefinition( new Flag( pref("addOpenLRLocations" ), "OpenLR locations shall be added for VMS units" ) );
			ap.addArgumentDefinition( new Opt( pref("dWiStaMode" ), "DWiStaMode" ) );
			ap.addArgumentDefinition( new OptMany( pref("dWiStaDeTextAreaLayout" ), "..." ) );
			
			ap.parse();

			tableId = ap.getValue( pref("tableId") ).getAsString();
			tableName = ap.getValue( pref("tableName") ).getAsString();
			tableVersion = ap.getValue( pref("tableVersion") ).getAsString();
			
			Data fd = ap.getValue( pref("filterVBA") );
			if( fd != null )
			{
				int num = fd.size();
				for( int i = 0; i < num; i++ )
				{
					filterVBAs.add( fd.get(i).getAsString() );
				}
			}
			
			Data fa = ap.getValue( pref("filterAQ") );
			if( fa != null )
			{
				int num = fa.size();
				for( int i = 0; i < num; i++ )
				{
					filterAQs.add( fa.get(i).getAsString() );
				}
			}
			
			
			cfgFile = ap.getValue( pref("cfgFile") ).getAsString();
			
			this.sender = ap.getValue( pref("sender") ).getAsString();
			
			this.connTimeout= ap.getValue( pref("connTimeout") ).getAsLong();
			fileDir = ap.getValue( pref("fileDir") ).getAsString();
			vmsServiceUrl = ap.getValue( pref("vmsServiceUrl") ).getAsString();
			
			d2SchemaLocation = ap.getValue( pref("d2SchemaLocation") ).getAsString();
			addTmcLocations = ap.getValue( pref("addTmcLocations") ).getAsBoolean();
			addOpenLRLocations = ap.getValue( pref("addOpenLRLocations") ).getAsBoolean();
			
			String dm = ap.getValue( pref("dWiStaMode") ).getAsString();
			if( ( dm != null ) && !dm.isEmpty() ) {
				dWiStaMode = DWiStaMode.valueOf( dm );
			}
			
			Data tas = ap.getValue( pref("dWiStaDeTextAreaLayout") );
			if( tas != null )
			{
				int num = tas.size();
				for( int i = 0; i < num; i++ )
				{
					dWiStaTextAreaLayouts.add( parseTextAreaLayout( tas.get(i).getAsString() ) );
				}
			}
		}
		catch( ArgParserError | FileNotFoundException  ex )
		{
			String errMsg = "ArgParser exception: " + ex.getMessage();
			LOGGER.error( errMsg );
			
			throw new VMSException( VMSException.ERROR_ARG_PARSER, 
                    				"Error parsing arguments:\n" + ex.getMessage(), 
                    				ex );
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
			throws VMSException
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

	public long getConnTimeout()
	{
		return connTimeout;
	}
	
	public String getFileDir()
	{
		return fileDir;
	}
	
	public String getVmsServiceUrl()
	{
		return vmsServiceUrl;
	}
	
	public String getTableId()
	{
		return tableId;
	}

	public String getTableName()
	{
		return tableName;
	}

	public String getTableVersion()
	{
		return tableVersion;
	}
	
	
	public List<String> getFilterVBAs()
	{
		return filterVBAs;
	}
	
	public List<String> getFilterAQs()
	{
		return filterAQs;
	}
	
	public String getSender()
	{
		return sender;
	}

	public String getD2SchemaLocation()
	{
		return d2SchemaLocation;
	} 

	public String getCfgFile()
	{
		return cfgFile;
	}
	
	public boolean addTmcLocations()
	{
		return addTmcLocations;
	}

	public boolean addOpenLRLocations()
	{
		return addOpenLRLocations;
	}

	public DWiStaMode getDWiStaMode()
	{
		return dWiStaMode;
	}

	public List<DWiStaTextAreaLayout> getdWiStaTextAreaLayouts()
	{
		return dWiStaTextAreaLayouts;
	}
}
