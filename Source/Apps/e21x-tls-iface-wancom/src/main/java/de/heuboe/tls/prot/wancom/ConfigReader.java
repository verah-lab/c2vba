package de.heuboe.tls.prot.wancom;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;

import de.heuboe.log.Logger;

/**
 * Implement reader for connection parameters for th wancom protocal
 */
public class ConfigReader {

	private static final Logger LOGGER = Logger.getLogger(ConfigReader.class);

    private static final Boolean SECURE_CONNECTION     = false;
    private static final Boolean CLIENT_AUTHENTICATION = false;
    private static final Integer CONNECT_DELAY         = 0;
    private static final Integer RECONNECT_DELAY       = 5;
    private static final Integer LOGFILE_SIZE          = 100000;                                // size is measured in count of lines!
    private static final Integer LOGFILE_ROTATE        = 2;
    private static final Boolean KEEP_ALIVE_DAMBACH    = false;

    private WANComConfig         mainCfg;
	
	public ConfigReader(String filename) throws IOException { // NOSONAR reducing would be ineffective
		LOGGER.info("reading wancom configuration file " + filename);
		
		BufferedReader br = new BufferedReader(new FileReader(filename)); 

		Gson gson = new Gson();
		mainCfg = gson.fromJson(br, WANComConfig.class);

		if (mainCfg.getDefaultConfig() == null) {
			mainCfg.setDefaultConfig(new ConnectionConfig());
		}
 		// set default values in default configuration if not already set
		if (mainCfg.getDefaultConfig().getSecureConnection() == null) {
			mainCfg.getDefaultConfig().setSecureConnection(SECURE_CONNECTION);
		}
		if (mainCfg.getDefaultConfig().getClientAuthentication() == null) {
			mainCfg.getDefaultConfig().setClientAuthentication(CLIENT_AUTHENTICATION);
		}
		if (mainCfg.getDefaultConfig().getConnectDelay() == null) {
			mainCfg.getDefaultConfig().setConnectDelay(CONNECT_DELAY);
		}
		if (mainCfg.getDefaultConfig().getReconnectDelay() == null) {
			mainCfg.getDefaultConfig().setReconnectDelay(RECONNECT_DELAY);
		}
		if (mainCfg.getDefaultConfig().getLogFileSize() == null) {
			mainCfg.getDefaultConfig().setLogFileSize(LOGFILE_SIZE);
		}
		if (mainCfg.getDefaultConfig().getLogFileRotate() == null) {
			mainCfg.getDefaultConfig().setLogFileRotate(LOGFILE_ROTATE);
		}
        if (mainCfg.getDefaultConfig().getKeepAliveDambach() == null) {
            mainCfg.getDefaultConfig().setKeepAliveDambach(KEEP_ALIVE_DAMBACH);
        }
		// there are no defaults for tcp-port and serverhost
		
		/*
		 * Apply default values to connection configurations if needed
		 */
        for ( ConnectionConfig cfg : mainCfg.getConnectionConfigList() ) {
            if ( cfg.getSecureConnection() == null ) {
                cfg.setSecureConnection( mainCfg.getDefaultConfig().getSecureConnection() );
            }
            if ( cfg.getClientAuthentication() == null ) {
                cfg.setClientAuthentication( mainCfg.getDefaultConfig().getClientAuthentication() );
            }
            if ( cfg.getConnectDelay() == null ) {
                cfg.setConnectDelay( mainCfg.getDefaultConfig().getConnectDelay() );
            }
            if ( cfg.getReconnectDelay() == null ) {
                cfg.setReconnectDelay( mainCfg.getDefaultConfig().getReconnectDelay() );
            }
            if ( cfg.getLogFile() == null ) {
                cfg.setLogFile( mainCfg.getDefaultConfig().getLogFile() );
            }
            if ( cfg.getLogFileSize() == null ) {
                cfg.setLogFileSize( mainCfg.getDefaultConfig().getLogFileSize() );
            }
            if ( cfg.getLogFileRotate() == null ) {
                cfg.setLogFileRotate( mainCfg.getDefaultConfig().getLogFileRotate() );
            }
            if ( cfg.getTcpPort() == null ) {
                if ( mainCfg.getDefaultConfig().getTcpPort() == null ) {
                    throw new IOException( "you must specify a tcp port for your connections" );
                }
                cfg.setTcpPort( mainCfg.getDefaultConfig().getTcpPort() );
            }
            if ( cfg.getClient() == null ) {
                cfg.setClient( mainCfg.getDefaultConfig().getClient() );
            }
            if ( cfg.getClient() == null ) {
                throw new IOException( "you must specify if your connection is server or client" );
            }
            if ( cfg.getServerHost() == null && cfg.getClient().booleanValue() ) {
                throw new IOException( "you must specify a server host for for your client connections" );
            }
            if ( cfg.getOsi2Port() == null ) {
                throw new IOException( "you must specify an osi2 port for for your connections" );
            }
            if ( cfg.getOsi2Address() == null ) {
                throw new IOException( "you must specify an osi2 adress for for your connections" );
            }
            if ( cfg.getKeepAliveDambach() == null ) {
                cfg.setKeepAliveDambach( mainCfg.getDefaultConfig().getKeepAliveDambach() );
            }
		}
	}
	
	public WANComConfig getWANComConfig() {
		return mainCfg;
	}
}
