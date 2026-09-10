package  de.heuboe.datex2.vms.status.pub.config;


import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import de.heuboe.datex2.vms.service.VMSPublicationMode;


/**
 * A class that maps external configuration properties.
 */
@Configuration
@Validated
@ConfigurationProperties("de.heuboe.datex2.vms.status.pub")
public class Properties {
  
	// "URL des VmsPublicationService"
	@NotNull
	private String vmsServiceUrl;
	
	// Host des VmsPublicationService-Listeners
	private String vmsServiceListenerHost;
	
	// (Externer) Host des VmsPublicationService-Listeners
	private String vmsServiceListenerHostExt;
	
	// Port des VmsPublicationService-Listeners
	@NotNull
	private int vmsServiceListenerPort;
	
	// Externer Port des VmsPublicationService-Listeners
	// (vmsServiceListenerPort wenn nicht angegeben)
	private int vmsServiceListenerPortExt = 0;
	
	// Timout von WebService-Verbindungen
	private int connTimeout = 5000;
	
	// ID des VMSUnitTable
	@NotNull
	private String tableId;
	
	// Version des VMSUnitTable
	private String tableVersion;
	
	// Ablageverzeichnis der Publikationen
	@NotNull
	private String fileDir;

	// ID der Instanz
	@NotNull
	private String pubInstance;

	// Publikationsmodus: CyclicComplete, CyclicOnChangeComplete, or OnChangeUpdatesOnly
	private VMSPublicationMode pubMode;
	
	// Publicationszyklus (s)
	@NotNull
	private int pubInterval;
	
	// Nur für pubMode=OnUpdateCyclicSnapshot verwendet: in diesem Zyklus werden Änderungen an den Strategien geprüft (ms)
	private int checkInterval = 5000;
	
	// Target eines Push-Empfaengers der Publikationen
	private String datex2Target;
	
	// Zustellung an MDM: Verbindungs-/Authentifizierungs-Parameter
	private String receiverType;

	// Kafka topic
	private String kafkaTopic;
	
	private boolean writePictogramUrl = true;


	public String getKafkaTopic() {
		return kafkaTopic;
	}

	public void setKafkaTopic(String kafkaTopic) {
		this.kafkaTopic = kafkaTopic;
	}

	
	// DATEX-II schema location
	private String d2SchemaLocation;

	// true: DATEX-II-Daten werden mit den Schalt/Fehler-Daten aus der Datenbank verglichen. Nur zu Testzwecken.
	private boolean checkData = false;
	
	// Datex2 'nationalIdentifier'
	private String sender;
	
	// Nur im Check-Modus verwendet: Name der Schaltdatenart, etwa DaWVZWechseltextIst
	private String switchingDatakindName;

	public String getVmsServiceUrl() {
		return vmsServiceUrl;
	}

	public void setVmsServiceUrl(String vmsServiceUrl) {
		this.vmsServiceUrl = vmsServiceUrl;
	}

	public String getVmsServiceListenerHost() {
		return vmsServiceListenerHost;
	}

	public void setVmsServiceListenerHost(String vmsServiceListenerHost) {
		this.vmsServiceListenerHost = vmsServiceListenerHost;
	}

	public int getVmsServiceListenerPort() {
		return vmsServiceListenerPort;
	}

	public void setVmsServiceListenerPort(int vmsServiceListenerPort) {
		this.vmsServiceListenerPort = vmsServiceListenerPort;
	}

	public int getConnTimeout() {
		return connTimeout;
	}

	public void setConnTimeout(int connTimeout) {
		this.connTimeout = connTimeout;
	}

	public String getTableId() {
		return tableId;
	}

	public void setTableId(String tableId) {
		this.tableId = tableId;
	}

	public String getTableVersion() {
		return tableVersion;
	}

	public void setTableVersion(String tableVersion) {
		this.tableVersion = tableVersion;
	}

	public String getFileDir() {
		return fileDir;
	}

	public void setFileDir(String fileDir) {
		this.fileDir = fileDir;
	}

	public String getPubInstance() {
		return pubInstance;
	}

	public void setPubInstance(String pubInstance) {
		this.pubInstance = pubInstance;
	}

	public VMSPublicationMode getPubMode() {
		return pubMode;
	}

	public void setPubMode(String pubMode) {
		this.pubMode = VMSPublicationMode.valueOf( pubMode );
	}

	public int getPubInterval() {
		return pubInterval;
	}

	public void setPubInterval(int pubInterval) {
		this.pubInterval = pubInterval;
	}

	public int getCheckInterval() {
		return checkInterval;
	}

	public void setCheckInterval(int checkInterval) {
		this.checkInterval = checkInterval;
	}

	public String getDatex2Target() {
		return datex2Target;
	}

	public void setDatex2Target(String datex2Target) {
		this.datex2Target = datex2Target;
	}

	public String getReceiverType() {
		return receiverType;
	}

	public void setReceiverType(String receiverType) {
		this.receiverType = receiverType;
	}


	public String getD2SchemaLocation() {
		return d2SchemaLocation;
	}

	public void setD2SchemaLocation(String d2SchemaLocation) {
		this.d2SchemaLocation = d2SchemaLocation;
	}

	public boolean isCheckData() {
		return checkData;
	}

	public void setCheckData(boolean checkData) {
		this.checkData = checkData;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getSwitchingDatakindName() {
		return switchingDatakindName;
	}

	public void setSwitchingDatakindName(String switchingDatakindName) {
		this.switchingDatakindName = switchingDatakindName;
	}

	public int getVmsServiceListenerPortExt() {
		return vmsServiceListenerPortExt;
	}

	public void setVmsServiceListenerPortExt(int vmsServiceListenerPortExt) {
		this.vmsServiceListenerPortExt = vmsServiceListenerPortExt;
	}

	public String getVmsServiceListenerHostExt() {
		return vmsServiceListenerHostExt;
	}

	public void setVmsServiceListenerHostExt(String vmsServiceListenerHostExt) {
		this.vmsServiceListenerHostExt = vmsServiceListenerHostExt;
	}

	public boolean isWritePictogramUrl() {
		return writePictogramUrl;
	}

	public void setWritePictogramUrl(boolean writePictogramUrl) {
		this.writePictogramUrl = writePictogramUrl;
	}
	
}
