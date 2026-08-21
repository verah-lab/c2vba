package de.heuboe.tls.prot.wancom;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import de.heuboe.log.Logger;

/**
 * This class realizes a server for the wancom protocol
 */
public class WANComServer implements Runnable {

	private static final Logger LOGGER = Logger.getLogger(WANComServer.class);
	
	private List<ConnectionConfig> configList;
	private List<Connection> connectionList;
	private boolean shutDown;
	private WANCom parent;
	
	private String name;

	private boolean singleServer = false;

	/**
	 * Construct a wancom server
	 * @param config config parametrizing the behaviour of the server
	 * @param parent parent module of this server
	 * @param name name for this server. Mainly for logging purposes
	 */
	public WANComServer(ConnectionConfig config, WANCom parent, String name) {
		configList = new ArrayList<>();
		configList.add(config);
		this.parent = parent;
		this.name = name;

		this.shutDown = false;	
		this.connectionList = new ArrayList<>();
		
		LOGGER.info("start server " + name);
		
		Thread thread = new Thread(this);
		thread.setDaemon(true);
		thread.setName("Server " + name);
		thread.start();
	}

	/**
	 * Add a  configuration
	 * @param config configuration to add
	 */
	public void addConfiguration(ConnectionConfig config) {
		configList.add(config);
	}

	/**
	 * if this server is used only for one client, we can use the osi2 addresses directly
	 */
	public void setSingleServer() {
		singleServer  = true;
	}
	
	@Override
	public void run() {
		ConnectionConfig config = configList.get(0);
		while (!shutDown) {
			try (ServerSocket serverSocket = makeServerSocket(config)) { // use ofclosableresources
				while (!shutDown && !serverSocket.isClosed()) {
					Socket client = serverSocket.accept();
					String connectionName = name + ":" + client.getInetAddress() + ":"
							+ client.getRemoteSocketAddress();
					Connection conn = new Connection(client, config, parent, connectionName, this);
					if (singleServer) {
						conn.setSingleServer();
					}
					connectionList.add(conn);
					Thread thread = new Thread(conn);
					thread.setDaemon(true);
					thread.setName(connectionName);
					thread.start();
				}
			} catch (IOException e) {
				LOGGER.error("server socket got exception: " + name + ":" + e);
			}
		}
	}

	private ServerSocket makeServerSocket(ConnectionConfig config) throws IOException {
		ServerSocket serverSocket;
		/*
		 * start connections with the first tls over ip configuration.
		 * It is updated when the partner is identified.
		 */ 
		if (config.getSecureConnection() != null && config.getSecureConnection().booleanValue()) {
		    ServerSocketFactory sslserversocketfactory = SSLServerSocketFactory.getDefault();
		    SSLServerSocket serverSock = (SSLServerSocket) sslserversocketfactory.createServerSocket(config.getTcpPort());
		    boolean clientAuth = config.getClientAuthentication() != null && config.getClientAuthentication();
		    if (clientAuth) {
		    	LOGGER.info(name + ": using client authentication");
		    	serverSock.setNeedClientAuth(clientAuth);
		    }
		    serverSocket = serverSock; 
		} else {
			serverSocket = new ServerSocket(config.getTcpPort());
		}
		return serverSocket;
	}

	/**
	 * Stop the communication of all running connection threads
	 */
	public void stopCommunication() {
		LOGGER.info("shutdown server " + name);
		shutDown = true;
		for(Connection conn : connectionList) {
			conn.stop();
		}
	}

	/**
	 * Enquire the configuration of a given pair of osi2 ports
	 * @param osi2Port The parent osi2 port
	 * @param osi2Address The child osi2 port
	 * @return the configuration for this pair of osi2 ports
	 */
	public ConnectionConfig getConfiguration(short osi2Port, short osi2Address) {
		for(ConnectionConfig config : configList) {
			if (config.getOsi2Port() == osi2Port && config.getOsi2Address() == osi2Address) {
				return config;
			}
		}
		return null;
	}

	public String getName() {
		return name;
	}

	/**
	 * Request all communication threads to send a time synchronisation
	 */
	public void timeSync() {
		for(Connection conn : connectionList) {
			conn.setDoTimeSync(true);
		}		
	}

//	public void setTimeSyncMode(TimeSyncMode timeSyncMode) { for(Connection conn : connectionList) { conn.setTimeSyncMode(timeSyncMode); } } // NOSONAR backup

}
