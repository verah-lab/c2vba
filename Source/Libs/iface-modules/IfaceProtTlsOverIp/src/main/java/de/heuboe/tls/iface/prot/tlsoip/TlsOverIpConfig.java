package de.heuboe.tls.iface.prot.tlsoip;

import java.util.List;

/**
 * Objects of this calss con tain a default configuration for TLS over IP
 * and an array of prarameters for each connection to be established
 */
public class TlsOverIpConfig {

    private ConnectionConfig       defaultConfig;
    private List<ConnectionConfig> connectionConfigList;
	
	public ConnectionConfig getDefaultConfig() {
		return defaultConfig;
	}
	public void setDefaultConfig(ConnectionConfig defaultConfig) {
		this.defaultConfig = defaultConfig;
	}
	public List<ConnectionConfig> getConnectionConfigList() {
		return connectionConfigList;
	}
	public void setConnectionConfigList(List<ConnectionConfig> connectionConfigList) {
		this.connectionConfigList = connectionConfigList;
	}
}
