package de.heuboe.tls.iface.prot.tlsoip;

import com.google.gson.Gson;
import de.heuboe.tls.iface.lib.Pair;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Objects of this class are used to read and handle the configuration of TLS over IP connections
 */
@Slf4j
public class ConfigReader {
	
	private static final Boolean CLIENT = true; // if true this code tries to connect
	private static final Boolean SECURE_CONNECTION = false;
	private static final Boolean CLIENT_AUTHENTICATION = false;
	private static final Integer CONNECT_DELAY = 0;
	private static final Integer CONNECT_DURATION = 0;
	private static final Integer HELLO_DELAY = 10;
	private static final Integer HELLO_TIMEOUT = 30;
	private static final Integer RECEIPT_COUNT = 10;
	private static final Integer RECEIPT_DELAY = 10;
	private static final Integer RECEIPT_TIMEOUT = 30;
	private static final Integer RECEIPT_GRACE_TIME = 0;
	private static final Integer RECONNECT_DELAY = 60;
	private static final Integer LOGFILE_SIZE = 100000;	// size is measured in count of lines!
	private static final Integer LOGFILE_ROTATE = 2;
	
	private TlsOverIpConfig mainCfg;
	
	private List< Pair<String, String> > defaultParamList;
	
	
	/**
	 * Construct ConfigReader by a file
	 * @param filename The file incl. path where the config or the iface connections is located
	 * @throws IOException File handling ...
	 */
	public ConfigReader(String filename) throws IOException {
		log.info( "reading tls over ip configuration file {}", filename);
		
		BufferedReader br = new BufferedReader(new FileReader(filename)); 

		Gson gson = new Gson();
		mainCfg = gson.fromJson(br, TlsOverIpConfig.class);

		checkAndSpreadDefaults();
	}
	
	private void initDefaultList() { // NOSONAR keep! May be used later and is some work to develop
	    if (null == defaultParamList) {
	        defaultParamList = new LinkedList<>();
	    }
	    defaultParamList.add( new Pair<>("CLIENT"                                                                   , "true"   ) ); // if true this code tries to connect
	    defaultParamList.add( new Pair<>(":SECURE_CONNECTION:SECURECONNECTION"                                      , "false"  ) );
	    defaultParamList.add( new Pair<>(":CLIENT_AUTHENTICATION:CLIENTAUTHENTICATION"                              , "false"  ) );
	    defaultParamList.add( new Pair<>(":CONNECT_DELAY:CONNECTDELAY"                                              , "0"      ) );
	    defaultParamList.add( new Pair<>(":CONNECT_DURATION:CONNECTDURATION"                                        , "0"      ) );
	    defaultParamList.add( new Pair<>(":HELLO_DELAY:HELLODELAY"                                                  , "10"     ) );
	    defaultParamList.add( new Pair<>(":HELLO_TIMEOUT:HELLOTIMEOUT"                                              , "30"     ) );
	    defaultParamList.add( new Pair<>(":RECEIPT_COUNT:RECEIPTCOUNT"                                              , "10"     ) );
	    defaultParamList.add( new Pair<>(":RECEIPT_DELAY:RECEIPTDELAY"                                              , "10"     ) );
	    defaultParamList.add( new Pair<>(":RECEIPT_TIMEOUT:RECEIPTTIMEOUT"                                          , "30"     ) );
	    defaultParamList.add( new Pair<>(":RECEIPT_GRACE_TIME:RECEIPT_GRACETIME:RECEIPT_GRACETIME:RECEIPTGRACETIME" , "0"      ) );
	    defaultParamList.add( new Pair<>(":RECONNECT_DELAY:RECONNECTDELAY"                                          , "60"     ) );
	    defaultParamList.add( new Pair<>(":LOGFILE_SIZE:LOGFILESIZE"                                                , "100000" ) ); // size is measured in count of lines!
	    defaultParamList.add( new Pair<>(":LOGFILE_ROTATE:LOGFILE_ROTATE"                                           , "2"      ) );	    
	}
	
	/**
	 * Sets the default grace time for receipt timeouts
	 * if checkAndSpreadDefaults is used afterwards, all connections will have this grace time
	 * @param graceTime a default grace time unit msec
	 */
	public void setReceiptGraceTime( int graceTime ) {
	    mainCfg.getDefaultConfig().setReceiptGraceTime( graceTime );
	}
	
	/**
	 * Get default grace time for receipt timeouts
	 * @return grace time for receipt timeouts
	 */
	public Integer getReceiptGraceTime() {
	    return mainCfg.getDefaultConfig().getReceiptGraceTime();
	}

	/**
	 * Checks the default values and spreads them over all defined connections where values are missing
	 * @throws IOException May happen due to filesystem operations
	 */
	public void checkAndSpreadDefaults() throws IOException {
        if (mainCfg.getDefaultConfig() == null) {
			mainCfg.setDefaultConfig(new ConnectionConfig());
		}
        
 		initDefaults();
 		
		// there are no defaults for tcp-ports and serverhost and there may not be
		if (mainCfg.getDefaultConfig().getId() != null) {
		    log.warn( "There may not be a default id in default. Discarded." );
		    mainCfg.getDefaultConfig().setId( "" );
		}
		if (mainCfg.getDefaultConfig().getTcpPort() != null) {
		    log.warn( "There may not be a default tcpPort in default. Discarded." );
		    mainCfg.getDefaultConfig().setTcpPort( null );
		}
		if (mainCfg.getDefaultConfig().getTcpPortB() != null) {
		    log.warn( "There may not be a default tcpPortB in default. Discarded." );
		    mainCfg.getDefaultConfig().setTcpPortB( null );
		}
		if (mainCfg.getDefaultConfig().getServerHost() != null) {
		    log.warn( "There may not be a default serverHost in default. Discarded." );
		    mainCfg.getDefaultConfig().setServerHost( null );
		}
		if (mainCfg.getDefaultConfig().getServerHostB() != null) {
		    log.warn( "There may not be a default serverHostB in default. Discarded." );
		    mainCfg.getDefaultConfig().setServerHostB( null );
		}
		if (mainCfg.getDefaultConfig().getLogFile() != null) {
		    log.warn( "There may not be a default logfile in default. Discarded." );
		    mainCfg.getDefaultConfig().setLogFile( null );
		}
		if (mainCfg.getDefaultConfig().getOsi2Port() != null) {
		    log.warn( "There may not be a default osi2Port in default. Discarded." );
		    mainCfg.getDefaultConfig().setOsi2Port( null );
		}
		if (mainCfg.getDefaultConfig().getOsi2Address() != null) {
		    log.warn( "There may not be a default osi2Address in default. Discarded." );
		    mainCfg.getDefaultConfig().setOsi2Address( null );
		}
		
		/*
		 * Apply default values to connectio configurations if needed
		 */
		for(ConnectionConfig cfg : mainCfg.getConnectionConfigList()) {
			initDefaultConnection( cfg, mainCfg.getDefaultConfig() );
		}
    }
	
	private static void initDefaultConnection( ConnectionConfig cfg, ConnectionConfig defaultCfg ) throws IOException { //NOSONAR uniform comparisons ...
		if ( cfg.getSecureConnection() == null) {
			cfg.setSecureConnection( defaultCfg.getSecureConnection());
		}
		if ( cfg.getClientAuthentication() == null) {
			cfg.setClientAuthentication( defaultCfg.getClientAuthentication());
		}
		if ( cfg.getConnectDelay() == null) {
			cfg.setConnectDelay( defaultCfg.getConnectDelay());
		}
		if ( cfg.getConnectDuration() == null) {
			cfg.setConnectDuration( defaultCfg.getConnectDuration());
		}
		if ( cfg.getHelloDelay() == null) {
			cfg.setHelloDelay( defaultCfg.getHelloDelay());
		}
		if ( cfg.getHelloTimeout() == null) {
			cfg.setHelloTimeout( defaultCfg.getHelloTimeout());
		}
		if ( cfg.getReceiptCount() == null ) {
			cfg.setReceiptCount( defaultCfg.getReceiptCount() );
		}
		if ( cfg.getReceiptDelay() == null ) {
			cfg.setReceiptDelay( defaultCfg.getReceiptDelay() );
		}
		if ( cfg.getReceiptTimeout() == null ) {
			cfg.setReceiptTimeout( defaultCfg.getReceiptTimeout() );
		}
		if ( cfg.getReceiptGraceTime() == null || 0 == cfg.getReceiptGraceTime() ) {
			cfg.setReceiptGraceTime( defaultCfg.getReceiptGraceTime() );
			int val = cfg.getReceiptGraceTime();
			if ( val !=0 && val < 2000 ){
				throw new IOException( "receipt grace time hast to be greater than 2000 msec if specified different from zero." );
			}
		}
		if ( cfg.getReconnectDelay() == null ) {
			cfg.setReconnectDelay( defaultCfg.getReconnectDelay() );
		}
		if ( cfg.getLogFileSize() == null ) {
			cfg.setLogFileSize( defaultCfg.getLogFileSize() );
		}
		if ( cfg.getLogFileRotate() == null ) {
			cfg.setLogFileRotate( defaultCfg.getLogFileRotate() );
		}
		if ( cfg.getTcpPort() == null ) {
			throw new IOException( "you must specify a tcp port for your connections" );
		}
		// optional
		if ( cfg.getTcpPortB() == null && cfg.getServerHostB() != null ) {
			if ( cfg.getTcpPort() == null ) {
				throw new IOException( "you must specify a tcp port / tcp port b for your connections" );
			}
			cfg.setTcpPortB( cfg.getTcpPort() );
		}
		if ( cfg.getClient() == null ) {
			cfg.setClient( defaultCfg.getClient() );
		}
		if ( cfg.getServerHost() == null && cfg.getClient() ) {
			throw new IOException( "you must specify a server host for for your client connections" );
		}
		if ( cfg.getOsi2Port() == null ) {
			throw new IOException( "you must specify an osi2 port for for your connections" );
		}
		if ( cfg.getOsi2Address() == null ) {
			if ( Boolean.TRUE.equals(cfg.getClient()) ) {
				throw new IOException( "you must specify an osi2 adress for for your client connections" );
			}
			log.warn( "found no osi2 adress for connection. assume multi connection server." );
			cfg.setOsi2Address( (short) 0 );
		}
	}
	
	private void initDefaults() {
        // set default values in default configuration if not already set
		if (mainCfg.getDefaultConfig().getClient() == null) {
		    mainCfg.getDefaultConfig().setClient(CLIENT);
		}
		if (mainCfg.getDefaultConfig().getSecureConnection() == null) {
			mainCfg.getDefaultConfig().setSecureConnection(SECURE_CONNECTION);
		}
		if (mainCfg.getDefaultConfig().getClientAuthentication() == null) {
			mainCfg.getDefaultConfig().setClientAuthentication(CLIENT_AUTHENTICATION);
		}
		if (mainCfg.getDefaultConfig().getConnectDelay() == null) {
			mainCfg.getDefaultConfig().setConnectDelay(CONNECT_DELAY);
		}
		if (mainCfg.getDefaultConfig().getConnectDuration() == null) {
			mainCfg.getDefaultConfig().setConnectDuration(CONNECT_DURATION);
		}
		if (mainCfg.getDefaultConfig().getHelloDelay() == null) {
			mainCfg.getDefaultConfig().setHelloDelay(HELLO_DELAY);
		}
		if (mainCfg.getDefaultConfig().getHelloTimeout() == null) {
			mainCfg.getDefaultConfig().setHelloTimeout(HELLO_TIMEOUT);
		}
		if (mainCfg.getDefaultConfig().getReceiptCount() == null) {
			mainCfg.getDefaultConfig().setReceiptCount(RECEIPT_COUNT);
		}
		if (mainCfg.getDefaultConfig().getReceiptDelay() == null) {
			mainCfg.getDefaultConfig().setReceiptDelay(RECEIPT_DELAY);
		}
		if (mainCfg.getDefaultConfig().getReceiptTimeout() == null) {
			mainCfg.getDefaultConfig().setReceiptTimeout(RECEIPT_TIMEOUT);
		}
        if (mainCfg.getDefaultConfig().getReceiptGraceTime() == null) {
            mainCfg.getDefaultConfig().setReceiptGraceTime(RECEIPT_GRACE_TIME);
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
    }

    public TlsOverIpConfig getTlsOverIpConfig() {
        return mainCfg;
    }
}
