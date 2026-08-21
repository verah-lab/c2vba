package de.heuboe.tls.prot.wancom;

import java.util.List;

/**
 * Objects of this class hold a complete configuration of potential multiple connections
 * for wancom protocol connections
 */
public class WANComConfig {

	private ConnectionConfig defaultConfig;
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
