package de.heuboe.datex2.vms.status.pub;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.ws.Endpoint;

import de.heuboe.datex2.vms.service.ObjectKey;
import de.heuboe.datex2.vms.service.VMSServiceException;
import de.heuboe.datex2.vms.status.pub.config.Properties;
import de.heuboe.datex2.vms.service.VMSDataListener;
import de.heuboe.datex2.vms.service.VMSService;
import de.heuboe.log.Logger;


/**
 * 
 * Switching data listener
 * 
 * @author peters
 *
 */
public class DataListener extends TimerTask implements VMSDataListener
{
	private static final Logger LOGGER = Logger.getLogger( DataListener.class );
	
	private Properties properties;
	private VMSDataListener manager;
	
	private String thisId;
	private String address;
	private String addressExt;
	private Date lastPing;
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param properties	Configuration properties
	 * @param address		Own listener address
	 * @param addressExt	Own listener external address
	 * @param manager		VMSDataListener
	 */
	public DataListener( Properties properties, String address, String addressExt, VMSDataListener manager )
	{
		this.properties = properties;
		
		this.address = address;
		this.addressExt = addressExt;
		this.manager = manager;
		
		this.thisId = "VMSPublicationBuilder" + 
		              " [" + properties.getTableId() + "]" +
		              " [" + (new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")).format( new Date() ) + "]";
	}
	
	@Override
	public synchronized void ping( String clientId )
	{
		lastPing = new Date();
		
		LOGGER.info( "Ping from: " + clientId );
		LOGGER.info( "Ping at:   " + lastPing.toString() );
	}
	
	@Override
	public void error( String reason )
	{
		manager.error( reason );
	}

	@Override
	public void notifyChange( List<ObjectKey> objIds )
	{
		manager.notifyChange( objIds );
	}

	/**
	 * 
	 * Start listening to data updates
	 * 
	 * @param vmsService			VMS service
	 * @throws VMSServiceException	Error
	 */
    public void start( VMSService vmsService )
    		throws VMSServiceException
    {
    	LOGGER.info( "Start listener on <" + address + ">" );
		Endpoint.publish( address, this );
		
		String tableVersion = properties.getTableVersion();
		if( ( tableVersion == null ) || tableVersion.isEmpty() )
		{
			tableVersion = vmsService.getCurrentTableVersion( properties.getTableId() );
		}
		
		Identity identity = new Identity( thisId, addressExt );
		vmsService.registerDataListener( properties.getTableId() , 
										 tableVersion, identity );
		
		lastPing = new Date();
		
		Timer timer = new Timer();
		timer.schedule( this, PING_INTERVAL_MS * 2L, PING_INTERVAL_MS * 2L ); 
		
		LOGGER.info( "DataListener successfully registered" );
    }

	@Override
	public synchronized void run()
	{
		Date now = new Date();
		
		if( now.getTime() - lastPing.getTime() > PING_INTERVAL_MS * 2L )
		{
			LOGGER.fatal( "Connection to VMSPublicationService lost !" );
			LOGGER.fatal( "No ping received !" );
			
			manager.error( "No ping received !" );
		}
	}
}
