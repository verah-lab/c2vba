package de.heuboe.tls.prot.wancom;

import java.io.IOException;
import java.net.Socket;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import de.heuboe.log.Logger;

/**
 * A Class implemeting clients of the wancom protocol
 */
public class WANComClient implements Runnable {

	private static final Logger LOGGER = Logger.getLogger(WANComClient.class);
	
	private ConnectionConfig config;
	private Connection connection;
	private boolean shutDown;
	private WANCom parent;
	
	private String name;

	/**
	 * Constructor for a wancom client
	 * @param config parameters for connection behaviour
	 * @param parent parent object for this connection
	 * @param name name for this connection. Mainly for logging
	 */
	public WANComClient(ConnectionConfig config, WANCom parent, String name) {
		this.config = config;
		this.parent = parent;
		this.name = name;

		this.shutDown = false;		
		LOGGER.info("start client " + name);
		
		Thread thread = new Thread(this);
		thread.setDaemon(true);
		thread.setName("Client " + name);
		thread.start();
	}

	/**
	 * Stop communication of this connection
	 */
	public void stopCommunication() {
		LOGGER.info("shutdown client " + name);
		shutDown = true;
		if (null != connection) {
		        connection.stop();
		}
	}

	@Override
	public void run() {
		while(!shutDown) {
			try {
				connection = null;
				Socket socket = null;
				if (config.getSecureConnection() != null && config.getSecureConnection().booleanValue()) {
					SocketFactory sslsocketfactory = SSLSocketFactory.getDefault();
		            socket = sslsocketfactory.createSocket(config.getServerHost(), config.getTcpPort());			
				} else {
					socket = new Socket(config.getServerHost(), config.getTcpPort());
				}
				parent.createSendQueue(config.getOsi2Port(), config.getOsi2Address());
				connection = new Connection(socket, config, parent, name, null);
				/*
				 *  start no separate thread because the client is already a separate thread 
				 *  which can only handle one connection at a time.
				 */
				connection.run();
			} catch (IOException e) {
				LOGGER.info("client " + name + " got exception: " + e);
			}
			try {
				Thread.sleep(config.getReconnectDelay()*1000L);
			} catch (InterruptedException e) { // NOSONAR keep running
				LOGGER.error("client " + name + " interrupted: " + e);
			}

		}
		LOGGER.info("end of thread for client: "  + name);
	}

	/**
	 * decide whether a given osi2 port pair corresponds to a (direct) partner
	 * @param port osi2 parent port
	 * @param partner osi2 child port
	 * @return true if pair belongs to a direct partner
	 */
	public boolean isPortPartner(Short port, Short partner) {
		if (port == null) {
			return true;
		}
        if ( !port.equals( config.getOsi2Port() ) ) {
            return false;
        }
        if ( partner == null ) {
            return true;
        }
		return partner.equals( config.getOsi2Address() );
	}

	/**
	 * request a time synchronization
	 */
	public void timeSync() {
		if (connection != null) {
			connection.setDoTimeSync(true);
		}
	}

	//public void setTimeSyncMode(TimeSyncMode timeSyncMode) { if (connection != null) { connection.setTimeSyncMode(timeSyncMode); } } // NOSONAR backup
	
}
