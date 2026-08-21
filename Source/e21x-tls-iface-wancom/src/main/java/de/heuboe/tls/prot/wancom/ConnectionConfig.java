package de.heuboe.tls.prot.wancom;

import de.heuboe.tls.iface.iface.TimeSyncGenerator;
import de.heuboe.tls.iface.iface.TimeSyncMode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A bean that provides all data needed to set up a client or server configuration
 * for wancom.
 * 
 * @author ralfz, ronald
 *
 */
@Getter
@Setter
@ToString
public class ConnectionConfig {

	private Boolean client;
	
	private Integer tcpPort;
	private String serverHost;
	private String serverHostB;
	
	private Short osi2Port;
	private Short osi2Address;
	
	private Boolean secureConnection;
	private Boolean clientAuthentication;
	
//	private Integer helloDelay
//	private Integer helloTimeout
//
//	private Integer receiptCount
//	private Integer receiptDelay
//	private Integer receiptTimeout
//
	private Integer reconnectDelay;
	private Integer connectDelay;
//	private Integer connectDuration

	private Boolean keepAliveDambach;
	
	/**
	 * keep track of selected mode for time synchronization
	 */
	private TimeSyncMode timeSyncMode = TimeSyncMode.WALLTIME;
	
	/**
	 * in case TimeSyncMode.USERDELIVERED was chosen, generator will be an object delivering a time sync telegram
	 */
	TimeSyncGenerator timeSyncGenerator = null;
	
	private String logFile;
	private Integer logFileSize;
	private Integer logFileRotate;
	
	public String getLogFile() {
		return logFile;
	}
	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}
	public Integer getLogFileSize() {
		return logFileSize;
	}
	public void setLogFileSize(Integer logFileSize) {
		this.logFileSize = logFileSize;
	}
	public Integer getLogFileRotate() {
		return logFileRotate;
	}
	public void setLogFileRotate(Integer logFileRotate) {
		this.logFileRotate = logFileRotate;
	}
	
	/**
	 * select how timeSync has to be done
	 * @param mode the mode to select for subsequent time syncs
	 */
        public void setTimeSyncMode( TimeSyncMode mode ) {
                this.timeSyncMode = mode;
        }
	
	/**
	 * set generator for time sync telegrams for the mode USERDELIVERED
	 * @param generator the generator of a time sync telegram 
	 */
        public void setTimeSyncGenerator( TimeSyncGenerator generator ) {
                this.timeSyncGenerator = generator;
        }

        public TimeSyncMode getTimeSyncMode() {
                return timeSyncMode;
        }

        public TimeSyncGenerator getTimeSyncGenerator() {
                return timeSyncGenerator;
        }

//        @Override
//        public String toString() 
//                return "ConnectionConfig [client=" + client + ", tcpPort=" + tcpPort + ", serverHost=" + serverHost + ", serverHostB=" + serverHostB
//                                + ", osi2Port=" + osi2Port + ", osi2Address=" + osi2Address + ", secureConnection=" + secureConnection
//                                + ", clientAuthentication=" + clientAuthentication + ", helloDelay=" + helloDelay + ", helloTimeout=" + helloTimeout
//                                + ", receiptCount=" + receiptCount + ", receiptDelay=" + receiptDelay + ", receiptTimeout=" + receiptTimeout
//                                + ", reconnectDelay=" + reconnectDelay + ", connectDelay=" + connectDelay + ", connectDuration=" + connectDuration
//                                + ", timeSyncMode=" + timeSyncMode + ", timeSyncGenerator=" + timeSyncGenerator + ", logFile=" + logFile + ", logFileSize="
//                                + logFileSize + ", logFileRotate=" + logFileRotate + "]"
//        
}
