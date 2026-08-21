package de.heuboe.tls.iface.prot.tlsoip;

import lombok.extern.slf4j.Slf4j;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Objects of ths class are TLS over IP servers
 * i.e. the may be connected via tcp/ip by other systems or processes
 * an use the TLS over IP protocol
 */
@Slf4j
public class TlsOverIpServer implements Runnable {
	
	private List<ConnectionConfig> configList;
	private List<Connection> connectionList;
	private boolean shutDown;
	private TlsOverIp parent;
	
	private String name;

	private boolean singleServer = false;
	
	/**
	 * Construct a TLS over IP server
	 * @param config The configuration for this connection
	 * @param parent the managing object
	 * @param name name of this thread (mostly for logging)
	 */
	public TlsOverIpServer(ConnectionConfig config, TlsOverIp parent, String name) {
		configList = new ArrayList<>();
		configList.add(config);
		this.parent = parent;
		this.name = name;

		this.shutDown = false;	
		this.connectionList = new ArrayList<>();
		
		log.info("start server " + name);
		
		Thread thread = new Thread(this);
		thread.setDaemon(true);
		thread.setName("Server " + name);
		thread.start();
	}
	
	/**
	 * Keep an additional configuration
	 * @param config The configuration to keep
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
					Connection conn = new Connection(client, config, parent, connectionName, this, parent.getIfaceApplication().getSystemMessageManagement());
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
				log.error("server socket got exception: " + name + ":" + e);
			}
		}
	}

	private ServerSocket makeServerSocket(ConnectionConfig config) throws IOException {
		ServerSocket serverSocket;
		/*
		 * start connections with the first tls over ip configuration.
		 * It is updated when the partner is identified.
		 */ 
		if (config.getSecureConnection() != null && config.getSecureConnection() ) {
		    ServerSocketFactory sslserversocketfactory = SSLServerSocketFactory.getDefault();
		    SSLServerSocket serverSock = (SSLServerSocket) sslserversocketfactory.createServerSocket(config.getTcpPort());
		    boolean clientAuth = config.getClientAuthentication() != null && config.getClientAuthentication();
		    if (clientAuth) {
		    	log.info(name + ": using client authentication");
		    	serverSock.setNeedClientAuth(clientAuth);
		    }
		    serverSocket = serverSock; 
		} else {
			serverSocket = new ServerSocket(config.getTcpPort());
		}
		return serverSocket;
	}
	
	/**
	 * stop communication the next time possible
	 */
	public void stopCommunication() {
		log.info("shutdown server " + name);
		shutDown = true;
		for(Connection conn : connectionList) {
			conn.stop();
		}
	}
	
	/**
	 * look for a connection configuration by a pair of osi2 ports
	 * @param osi2Port parent port
	 * @param osi2Address child port
	 * @return a connection configuration, if the port pair ist found, null otherwise
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
	 * Initiate a time sync the next time possible
	 */
	public void timeSync() {
		for(Connection conn : connectionList) {
			conn.requestTimeSync();
		}		
	}

}
