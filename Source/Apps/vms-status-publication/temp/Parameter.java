package de.heuboe.datex2.vms.status.pub;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.heuboe.arg.ArgParser;
import de.heuboe.arg.ArgParserError;
import de.heuboe.arg.Flag;
import de.heuboe.arg.Mand;
import de.heuboe.arg.Opt;
import de.heuboe.arg.OptMany;
import de.heuboe.arg.ParseException;
import de.heuboe.data.Data;
import de.heuboe.datex2.vms.service.VMSPublicationMode;
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
	
	private String vmsServiceUrl;
	private int vmsServiceListenerPort;
	private String vmsServiceListenerHost;
	
	private String tableId;
	private String tableVersion;
	
	private long connTimeout = 60L * 1000L;			// ms: 1 minute
	private String fileDir;

	private String mdmAuthenticationFile;
	private String ftpUploadConfigFile;
	
	private String pubInstance;	
	private int pubInterval;
	private long checkInterval;
	private VMSPublicationMode pubMode;
	
	private String sender;
	
	private String d2ReceiverConnPropFile;
	private String d2ReceiverURL;
	private String d2ReceiverUser; 
	private String d2ReceiverPassword; 
	private boolean d2ReceiverUseGzip;
	private String d2ReceiverProxyServer;
	private int d2ReceiverProxyPort;
	private String d2ReceiverTrustStoreFile;
	private String d2ReceiverTrustStorePassword;
	
	private String d2SchemaLocation;
	
	private boolean checkData = false;
	private String datex2Target;
	
	private ArgParser ap; 
	
	private Set<String> switchingDatakindNames = new HashSet<>();
	
	
	
	private static Parameter instance = null;
	
	
	private Parameter( String[] args )
			throws VMSException
	{
		ap = new ArgParser( "vmsStatusPublication", "", args.clone() );
		
		try
		{
			ap.addArgumentDefinition( new Mand( "vmsServiceUrl", "URL des VmsPublicationService", "" ) );
			ap.addArgumentDefinition( new Opt( "vmsServiceListenerHost", "Host des VmsPublicationService-Listeners", "" ) );
			ap.addArgumentDefinition( new Mand( "vmsServiceListenerPort", "Port des VmsPublicationService-Listeners", "4747" ) );
			ap.addArgumentDefinition( new Opt( "connTimeout", "Timout von WebService-Verbindungen", 1800000 ) );
			
			ap.addArgumentDefinition( new Mand( "tableId", "ID des VMSUnitTable" ) );
			ap.addArgumentDefinition( new Opt( "tableVersion", "Version des VMSUnitTable" ) );
			
			ap.addArgumentDefinition( new Mand( "fileDir", "Ablageverzeichnis der Publikationen" ) );

			ap.addArgumentDefinition( new Mand( "pubInstance", "ID der Instanz" ) );

			ap.addArgumentDefinition( new Opt( "pubMode", "Publikationsmodus: CyclicComplete, CyclicOnChangeComplete, or OnChangeUpdatesOnly",  "CyclicComplete" ) );
			ap.addArgumentDefinition( new Mand( "pubInterval", "Publicationszyklus (s)" ) );
			ap.addArgumentDefinition( new Opt( "checkInterval", "Nur für pubMode=OnUpdateCyclicSnapshot verwendet: in diesem Zyklus werden Änderungen an den Strategien geprüft (ms)", 5000 ) );
			ap.addArgumentDefinition( new Opt( "datex2Target", "Target eines Push-Empfaengers der Publikationen", "" ) );
			
			ap.addArgumentDefinition( new Opt( "mdmAuthenticationFile", "Zustellung an MDM: Verbindungs-/Authentifizierungs-Parameter", "" ) );
			ap.addArgumentDefinition( new Opt( "ftpUploadConfigFile", "Konfiguration Ftp-Upload", "" ) );

			ap.addArgumentDefinition( new Opt( "d2ReceiverConnPropFile", "Verbindungsparameter eines Push-Empfaengers der Publikationen", "" ) );
			ap.addArgumentDefinition( new Opt( "d2ReceiverURL", "URL eines Push-Empfaengers der Publikationen", "" ) );
			ap.addArgumentDefinition( new Opt( "d2ReceiverUser", "User-Name zur URL eines Push-Empfaengers der Publikationen", "" ) ); 
			ap.addArgumentDefinition( new Opt( "d2ReceiverPassword", "Passwort zur URL eines Push-Empfaengers der Publikationen", "" ) ); 
			ap.addArgumentDefinition( new Opt( "d2ReceiverUseGzip", "URL eines Push-Empfaengers der Publikationen", "" ) );
			ap.addArgumentDefinition( new Opt( "d2ReceiverProxyServer", "Proxy-Server zur URL eines Push-Empfaengers der Publikationen", "" ) );
			ap.addArgumentDefinition( new Opt( "d2ReceiverProxyPort", "Proxy-Port zur URL eines Push-Empfaengers der Publikationen", "" ) );
			ap.addArgumentDefinition( new Opt( "d2ReceiverTrustStoreFile", "Trust-Store-File zur https-URL eines Push-Empfaengers der Publikationen", "" ) );
			ap.addArgumentDefinition( new Opt( "d2ReceiverTrustStorePassword", "Trust-Store-Password zur https-URL eines Push-Empfaengers der Publikationen", "" ) );

			ap.addArgumentDefinition( new Opt( "d2SchemaLocation", "DATEX-II schema location", "" ) );

			ap.addArgumentDefinition( new Flag( "checkData", "DATEX-II-Daten werden mit den Schalt/Fehler-Daten aus der Datenbank verglichen. Nur zu Testzwecken." ) );
			
			ap.addArgumentDefinition( new Opt( "sender", "Datex2 <nationalIdentifier>", "" ) );
			
			ap.addArgumentDefinition( new OptMany( "switchingDatakindName", "Nur im Testmodus verwendet: Name der Schaltdatenart, etwa DaWVZWechseltextIst", "" ) );
						
			ap.parse();
		
			this.tableId = ap.getValue( "tableId" ).getAsString();
			this.tableVersion = ap.getValue( "tableVersion" ).getAsString();
			
			this.connTimeout= ap.getValue( "connTimeout" ).getAsLong();
			this.fileDir = ap.getValue( "fileDir" ).getAsString();

			this.checkInterval = ap.getValue( "checkInterval" ).getAsLong();
			this.pubInterval = ap.getValue( "pubInterval" ).getAsInt();
			this.pubMode = VMSPublicationMode.valueOf( ap.getValue( "pubMode" ).getAsString() );
			this.datex2Target = ap.getValue( "datex2Target" ).getAsString();
			
			if( ( pubMode == VMSPublicationMode.OnUpdateCyclicSnapshot ) || 
			    ( pubMode == VMSPublicationMode.OnUpdate ) )	
			{
				if( ( ( (long)pubInterval * 1000L ) % checkInterval ) != 0 )
				{
					ParseException  pe = new ParseException( "<pubInterval> has to be an integral multiple of <checkInterval> !" );
					throw new ArgParserError( Arrays.asList(pe) );
				}
				if( ( datex2Target == null ) || datex2Target.isEmpty() )
				{
					ParseException  pe = new ParseException( "No DATEX II target provided !" );
					throw new ArgParserError( Arrays.asList(pe) );
				}
			}
			
			this.pubInstance = ap.getValue( "pubInstance" ).getAsString();
			
			this.mdmAuthenticationFile = ap.getValue( "mdmAuthenticationFile" ).getAsString();
			this.ftpUploadConfigFile = ap.getValue( "ftpUploadConfigFile" ).getAsString();
			
			this.d2ReceiverConnPropFile = ap.getValue( "d2ReceiverConnPropFile" ).getAsString();
			if( ( d2ReceiverConnPropFile == null ) || d2ReceiverConnPropFile.isEmpty() )
			{
				this.d2ReceiverURL = ap.getValue( "d2ReceiverURL" ).getAsString();
				this.d2ReceiverUser = ap.getValue( "d2ReceiverUser").getAsString(); 
				this.d2ReceiverPassword = ap.getValue( "d2ReceiverPassword" ).getAsString();
				this.d2ReceiverUseGzip = ap.getValue( "d2ReceiverUseGzip" ).getAsBoolean();
				this.d2ReceiverProxyServer = ap.getValue( "d2ReceiverProxyServer" ).getAsString();
				this.d2ReceiverProxyPort = ap.getValue( "d2ReceiverProxyPort" ).getAsInt();
				this.d2ReceiverTrustStoreFile = ap.getValue( "d2ReceiverTrustStoreFile" ).getAsString();
				this.d2ReceiverTrustStorePassword = ap.getValue( "d2ReceiverTrustStorePassword" ).getAsString();
			}
			
			this.d2SchemaLocation = ap.getValue( "d2SchemaLocation" ).getAsString();
					
			this.vmsServiceUrl = ap.getValue( "vmsServiceUrl" ).getAsString();
			this.vmsServiceListenerPort = ap.getValue( "vmsServiceListenerPort" ).getAsInt();
			this.vmsServiceListenerHost = ap.getValue( "vmsServiceListenerHost" ).getAsString();

			this.sender	= ap.getValue( "sender" ).getAsString();	
			
			this.checkData = ap.getValue( "checkData" ).getAsBoolean();
			
			Data sdns = ap.getValue( "switchingDatakindName" );
			if( sdns != null )
			{
				int num = sdns.size();
				for( int i = 0; i < num; i++ )
				{
					switchingDatakindNames.add( sdns.get(i).getAsString() );
				}
			}
		} catch( ArgParserError ex ) {
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
	 * @throws VMSException error
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
	
	public String getMdmAuthenticationFile()
	{
		return mdmAuthenticationFile;
	}
	
	public String getD2ReceiverURL()
	{
		return d2ReceiverURL;
	}
	
	public String getFtpUploadConfigFile()
	{
		return ftpUploadConfigFile;
	}
	
	public int getPubInterval()
	{
		return pubInterval;
	}
	
	public long getCheckInterval()
	{
		return checkInterval;
	}
	
	public String getVMSServiceUrl()
	{
		return vmsServiceUrl;
	}
	
	public VMSPublicationMode getPubMode()
	{
		return pubMode;
	}
	
	public String getPubInstance()
	{
		return pubInstance;
	}
	
	public String getTableId()
	{
		return tableId;
	}
	
	public String getTableVersion()
	{
		return tableVersion;
	}
	
	public String getDatex2Target()
	{
		return datex2Target;
	}
	
	public int getVmsServiceListenerPort()
	{
		return vmsServiceListenerPort;
	}

	public String getVmsServiceListenerHost()
	{
		return vmsServiceListenerHost;
	}
	
	public String getSender()
	{
		return sender;
	}
	
	public String getD2ReceiverPassword()
	{
		return d2ReceiverPassword;
	}

	public String getD2ReceiverProxyServer()
	{
		return d2ReceiverProxyServer;
	}

	public int getD2ReceiverProxyPort()
	{
		return d2ReceiverProxyPort;
	}

	public String getD2ReceiverTrustStoreFile()
	{
		return d2ReceiverTrustStoreFile;
	}

	public String getD2ReceiverTrustStorePassword()
	{
		return d2ReceiverTrustStorePassword;
	}
	
	public String getD2ReceiverUser()
	{
		return d2ReceiverUser;
	}
	
	public boolean d2ReceiverUseGzip()  // NOSONAR
	{
		return d2ReceiverUseGzip;
	}
	
	public String getD2ReceiverConnPropFile()
	{
		return d2ReceiverConnPropFile;
	}
	
	public boolean checkData()     // NOSONAR
	{
		return checkData;
	}
	
	public String getD2SchemaLocation()
	{
		return d2SchemaLocation;
	}
	
	public Set<String> getSwitchingDatakindNames()
	{
		return switchingDatakindNames;
	}
}
