package de.heuboe.datex2.vms.service.uz;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;

import de.heuboe.datex2.vms.service.D2VMSMessage;
import de.heuboe.datex2.vms.service.ObjectKey;
import de.heuboe.datex2.vms.service.VMSDataListener;
import de.heuboe.datex2.vms.service.VMSService;
import de.heuboe.datex2.vms.service.VMSServiceException;
import de.heuboe.datex2.vms.service.VMSTableConfiguration;
import de.heuboe.datex2.vms.service.db.Persistence;
import de.heuboe.datex2.vms.service.table.data.D2VMSTable;
import de.heuboe.datex2.vms.service.table.data.D2VMSUnit;
import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;

/**
 * 
 * Implementation of VMSService
 * 
 * @author peters
 *
 */
@WebService(endpointInterface="de.heuboe.datex2.vms.service.VMSService",
		    serviceName="VMSService",
		    portName="VMSServiceWSPort")
public class UzService implements VMSService
{
	private static final Logger LOGGER = Logger.getLogger( UzService.class );
	public static final int SERVICE_ID = 4712;
	private boolean started = false;
	
	private String svcHost; 
	private int svcPort;
	
	
	private LocationManager locationManager;
	private ImageManager imageManager;
	private ConfigManager cfgManager;
	private DataManager dataManager;

	private Persistence persistence = null;
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param svcHost	Service host
	 * @param svcPort	Service port
	 */
	public UzService( String svcHost, int svcPort ) {
		this.svcHost = svcHost;
		this.svcPort = svcPort;
	}
	
	
	public void setPersistence( Persistence persistence ) {
		this.persistence = persistence;
	}
	
	public void setLocationManager(LocationManager locationManager) {
		this.locationManager = locationManager;
	}

	public void setImageManager(ImageManager imageManager) {
		this.imageManager = imageManager;
	}

	public void setConfigManager(ConfigManager cfgManager) {
		this.cfgManager = cfgManager;
	}
	
	public void setDataManager( DataManager dataManager ) {
		this.dataManager = dataManager;
	}



	
	@Override
	public boolean ping( String clientId )
	{
		LOGGER.info( "Ping received from Client <" + clientId + ">" );
		return true;
	}
	
	
	
	@Override
	public void saveVMSTable( D2VMSTable vmsTable ) throws VMSServiceException
	{
		LOGGER.info( "WS: saveVMSTable()" );
		LOGGER.debug( vmsTable.toString() );
		
		try {
			persistence.saveVMSTable( vmsTable );
		} catch( Throwable ex ) {   // NOSONAR
			LOGGER.error( ex.toString() );
			LOGGER.error( CallStack.getStackTraceAsString( ex ) );
			throw ex;
		}
		
	}
	
	@Override
	public D2VMSTable readVMSTable( String id, String version )
			throws VMSServiceException
	{
		LOGGER.info( "WS: readVMSTable()" );
		LOGGER.info( "id: 		" + id );
		LOGGER.info( "version: 	" + version );
		
		try {
			return persistence.readVMSTable( id, version );
		} catch( VMSServiceException ex ) {
			LOGGER.error( ex.toString() );
			LOGGER.error( CallStack.getStackTraceAsString( ex ) );
			throw ex;
		} catch( Throwable ex ) {  // NOSONAR
			LOGGER.error( ex.toString() );
			LOGGER.error( CallStack.getStackTraceAsString( ex ) );
			throw new VMSServiceException( "Error reading VMS table", ex );
		}
	}	
	
	@Override
	public String getCurrentTableVersion( String tableId )
			throws VMSServiceException
	{
		LOGGER.info( "WS: getCurrentTableVersion(" + tableId + ")" );
		
		String currentVersion = persistence.getCurrentTableVersion( tableId );

		if( currentVersion == null )
		{
			throw new VMSServiceException( VMSServiceException.VMS_SVC_ERR_UNKNOWN_TABLE_ID,
										"VMS-Table-ID <" + tableId + "> unknown." );
		}
			
		return currentVersion;
	}
	
	
	@Override
	public String getNextTableVersion( String tableId )
			throws VMSServiceException
	{
		LOGGER.info( "WS: getNextTableVersion(" + tableId + ")" );

		return persistence.getNextTableVersion( tableId );
	}

	
	@Override
	public String registerDataListener( String tableId, 
									    String tableVersion, 
									    VMSDataListener.Identity identity )
			throws VMSServiceException
	{
		D2VMSTable d2VMSTable = readVMSTable( tableId, tableVersion );
		return dataManager.registerDataListener( d2VMSTable, identity);
	}
	
	@Override
	public List<D2VMSMessage> getVMSMessages( Set<ObjectKey> objKeys )
			throws VMSServiceException
	{
		return dataManager.getMessages( objKeys );
	}
	
	@Override
	public synchronized List<D2VMSUnit> getD2VMSUnits( VMSTableConfiguration configuration ) throws VMSServiceException {
		
		try {
			List<D2VMSUnit> units = cfgManager.getD2VMSUnits( configuration, locationManager ); 
			return units
					.stream()
					.sorted( (u1, u2) -> u1.getInternalId().compareTo( u2.getInternalId() ) )
					.collect( Collectors.toList() );
		} catch( Throwable ex ) { // NOSONAR
			LOGGER.error( "Error in getD2VMSUnits()" );
			LOGGER.error( ex.toString() );
			LOGGER.error( CallStack.getStackTraceAsString( ex ) );
			throw ex;
		}
	}
	
	/**
	 * 
	 * Init service
	 * 
	 * @exception  VMSServiceException Error
	 * 
	 */
	public void init() throws VMSServiceException
	{
		dataManager.startPingTask();
		
		imageManager.registerDataObserver( dataManager );
		imageManager.start();
	}

	public static String getUrl( String svcHost, int svcPort ) {
		String host = "0.0.0.0"; 
		if( ( svcHost != null ) && !svcHost.isEmpty() ) {
			host = svcHost;
		}
		
		return "http://" + host + ":" + svcPort + "/Datex2/VMSPublication/Service";   
	}
	
	/**
	 * 
	 * Start SOAP service
	 * 
	 */
    public void start()
    {
    	if( !started )
    	{
    		
    		String smcURL = getUrl( svcHost, svcPort );
    		Endpoint.publish( smcURL, this );
    		
    		LOGGER.info( "Web service successfully started" );
    		started = true;
    	}
    }


}
