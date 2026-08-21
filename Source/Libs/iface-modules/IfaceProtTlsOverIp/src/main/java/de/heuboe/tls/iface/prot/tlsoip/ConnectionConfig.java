package de.heuboe.tls.iface.prot.tlsoip;

import de.heuboe.tls.iface.iface.TimeSyncGenerator;
import de.heuboe.tls.iface.iface.TimeSyncMode;
import lombok.Data;

/**
 * A bean that provides all data needed to set up a client or server configuration
 * for tls over ip.
 * 
 * @author ralfz, ronald
 *
 */
@Data public class ConnectionConfig {
    
    private String id;
	
	private Boolean active; // defaults to true. i.e. an config must explicitly say active is false!

	private Boolean client;
	
	private Integer tcpPort;
	private Integer tcpPortB;
	private String serverHost;
	private String serverHostB;
	
	private Short osi2Port;
	private Short osi2Address;
	
	private Boolean secureConnection;
	private Boolean clientAuthentication;
	
	private Integer helloDelay;
	private Integer helloTimeout;

	private Integer receiptCount;
	private Integer receiptDelay;
	private Integer receiptTimeout;
	private Integer receiptGraceTime;

	private Integer reconnectDelay;
	private Integer connectDelay;
	private Integer connectDuration;
	
	/**
	 * keep track of selected mode for time synchronization
	 */
	private TimeSyncMode timeSyncMode = TimeSyncMode.WALLTIME;
	
	/**
	 * in case TimeSyncMode.USERDELIVERED was chosen, generator will be an object delivering a time sync telegram
	 */
	private TimeSyncGenerator timeSyncGenerator = null;
	
	private String logFile;
	private Integer logFileSize;
	private Integer logFileRotate;
	
	/**
	 * i want same behaviour as with the other fields. but if absent, the value shall be true
	 * null fields may be overwritten by default values
	 * @return real value or true if the field is absent
	 */
	@SuppressWarnings( "unused" )
	public Boolean getActive() {
		if ( null == active ) {
			return true;
		}
		return active;
	}

}

