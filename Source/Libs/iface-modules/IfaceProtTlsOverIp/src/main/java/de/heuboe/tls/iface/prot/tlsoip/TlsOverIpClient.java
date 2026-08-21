package de.heuboe.tls.iface.prot.tlsoip;

import de.heuboe.tls.iface.iface.SystemMessageManagement;
import lombok.extern.slf4j.Slf4j;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;

/**
 * TLS over ip client.
 * Objects of this class will try to establish a tcp/ip connection to a tls over ip master.
 */
@Slf4j
public class TlsOverIpClient implements Runnable {
	private static final String CLIENT = "client ";
	
	private ConnectionConfig config;
	private Connection connection;
	private boolean shutDown;
	private TlsOverIp parent;
	private SystemMessageManagement smm;
	
	private String name;
	
	/**
	 * construct a tls over ip client connection an start it
	 * @param config Configuration for this connection
	 * @param parent The controlling object
	 * @param name Name (mostly for logging) fro this connection
	 */
	public TlsOverIpClient(ConnectionConfig config, TlsOverIp parent, String name) {
		this.config = config;
		this.parent = parent;
		this.name = name;
		if (null != parent.getIfaceApplication()) {
		    setSystemMessageManagement( parent.getIfaceApplication().getSystemMessageManagement() );
		} else {
		    setSystemMessageManagement( null );
		}

		this.shutDown = false;		
		log.info("start client " + name);
		
		Thread thread = new Thread(this);
		thread.setDaemon(true);
		thread.setName("Client " + name);
		thread.start();
	}
	
	/**
	 * stop communication the next time possible
	 */
	public void stopCommunication() {
		log.info("shutdown client " + name);
		shutDown = true;
		if (null != connection) {
		        connection.stop();
		}
	}
	
	static final int PRIMARY = 1;
	static final int SECONDARY = 2;

	@Override
	public void run() {
	    String serverHost = config.getServerHost();
	    Integer port = config.getTcpPort();
	    boolean delay = true;
	    int usedConn = PRIMARY;
		while(!shutDown) {
			try {
				connection = null;
				Socket socket = null;
				if (config.getSecureConnection() != null && config.getSecureConnection() ) {
					SocketFactory sslsocketfactory = SSLSocketFactory.getDefault();
		            socket = sslsocketfactory.createSocket(serverHost, port);			
				} else {
					socket = new Socket(serverHost, port);
				}
				parent.createSendQueue(config.getOsi2Port(), config.getOsi2Address());
				connection = new Connection(socket, config, parent, name, null, parent.getIfaceApplication().getSystemMessageManagement());
				/*
				 *  start no separate thread because the client is already a separate thread 
				 *  which can only handle one connection at a time.
				 */
				connection.run();
			} catch (IOException e) {
				String msg = CLIENT + name + " [ip=" + serverHost + "] got exception: ";
				log.info( msg + e);
                sysMsg( msg + e.getMessage() );
			}
			delay = true;
			if (null != config.getServerHostB()) { // we have two alternate adresses for server
			    if (PRIMARY == usedConn) {
    			    serverHost = config.getServerHostB();
    			    usedConn = SECONDARY;
    			    if (log.isDebugEnabled()) {
        			    String msg = String.format( "trying alternate ip %s for osi2 [%d/%d]", serverHost, config.getOsi2Port(), config.getOsi2Address() );
        			    log.debug( msg  );
    			    }
    			    delay = false; // alternate ip address is tried immediately
			    } else {
			        serverHost = config.getServerHost();
			        usedConn = PRIMARY;
			        delay = true;
			    }
			}
			if (delay) {
    			try {
    				Thread.sleep(config.getReconnectDelay()*1000L);
    			} catch (InterruptedException e) { // NOSONAR catch only for sleep
    				log.error( CLIENT + name + " interrupted: " + e);
    				sysMsg( CLIENT + name + " interrupted: " + e.getMessage() );
    			}
			}

		}
		log.info("end of thread for client: "  + name);
	}
	
	/**
	 * Test whether a given osi2 port pair is the pair of the current connection
	 * @param port own port number (output)
	 * @param partner partner port number (input)
	 * @return true if both ports match or one of the parameters is null (wildcard)
	 */
	public boolean isPortPartner(Short port, Short partner) {
		if (port == null) {
			return true;
		}
		if ( !port.equals( config.getOsi2Port() ) ) {
			return false;
		}
		if (partner == null) {
			return true;
		}
		return partner.equals( config.getOsi2Address() );
	}
	
	/**
	 * Initiate a time sync the next time possible
	 */
	public void timeSync() {
		if (connection != null) {
			connection.requestTimeSync();
		}
	}
    
    private void sysMsg( String msg ) {
        if (null != smm) {
            smm.sendMessage( msg );
        }
    }
    
    public void setSystemMessageManagement( SystemMessageManagement smm ) {
        this.smm = smm;
    }
	
}
