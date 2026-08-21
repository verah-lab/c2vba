package de.heuboe.tls.prot.wancom;

import com.google.common.net.InetAddresses;
import de.heuboe.log.Logger;
import de.heuboe.tls.iface.iface.TimeSyncGenerator;
import de.heuboe.tls.iface.iface.TimeSyncMode;
import de.heuboe.tls.iface.lib.Util;
import de.heuboe.tls.prot.wancom.WANCom.WANComHeader;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;

/**
 * This class handles all the tls over ip protocol details for a connected tcp ip connection.
 * 
 * @author ralfz, ronald
 *
 */
class Connection implements Runnable {

    private static final Logger LOGGER                   = Logger.getLogger( Connection.class );

    private static final int    BUFFERSIZE               = 1024;
    private static final int    HEADERLENGTH             = 28;                                  // minimum fix header

    private static final int    SOCKET_TIMEOUT           = 500 /* 1000 */;                      // milliseconds
    private static final int    SOCKET_TIMEOUT_SSL       = 15000;                               // milliseconds

    private static final int    WANCOM_OUT               = 600;                                 // type of data when sent
    private static final int    WANCOM_IN                = 500;                                 // type of data when received
    private static final int    KEEP_ALIVE_TIMEOUT       = 60;                                  // seconds
    private static final int    KEEP_ALIVE_SEND_INTERVAL = 50;                                  // seconds
	
    private Socket              socket;
    private int                 localIp;
    private ConnectionConfig    config;
    private WANCom              parent;
    private WANComServer        server;
	
	private byte[] receivedData;
	private int receivedSize;

	private boolean isConnectionAccepted;

	private String name;
	private WANComLogger log;

	private Date lastReceivedKeepAlive;
	private Date lastSentData;

	private short osi2Port;
	private short osi2Address;

	private boolean doShutdown;

	private boolean msgNotYetIdentified;
	
	private boolean doTimeSync;
	private TimeSyncMode timeSyncMode;
	private TimeSyncGenerator timeSyncGenerator = null;
	
	/**
	 * Constructor for a tls over ip connection
	 * @param socket the connected socket
	 * @param config the tls over ip configuration for this connection 
	 * @param parent the consumer of received telegrams and so on
	 * @param name name of the connection 
	 * @throws IOException 
	 */
	Connection(Socket socket, ConnectionConfig config, WANCom parent, String name, WANComServer server) throws IOException {
		this.socket = socket;
		this.config = config;
		this.parent = parent;
		this.server = server;
		this.name = name;
		this.doTimeSync = false;
		
        InetAddress ia = this.socket.getLocalAddress();
        
        String addr = ia.getHostAddress();
        String[] adrParts = addr.split( "\\." );
        
        if (4 != adrParts.length) {
            throw new IllegalStateException( "Can only handle local address type ipv4" );
        }
        
        this.localIp = InetAddresses.coerceToInteger( ia );
		
		LOGGER.info("new Connection: " + name + " (localIp=" + this.localIp + " ~ " + addr + ")");
		
		// if server, obtain osi2 addresses from the first received telegram
		osi2Port = server == null ? config.getOsi2Port() : 0;
		osi2Address = server == null ? config.getOsi2Address() : 0;
		if (osi2Port > 0 && osi2Address > 0) {
			this.name = "[" + osi2Port + "/" + osi2Address + "]";
		}
		receivedData = new byte[BUFFERSIZE];
		receivedSize = 0;
		isConnectionAccepted = false;
		lastReceivedKeepAlive = new Date();
		lastSentData = new Date();
		msgNotYetIdentified = false;
		timeSyncMode = config.getTimeSyncMode();
		timeSyncGenerator = config.getTimeSyncGenerator();

		try {
			log = WANComLogger.getWANComLogger(config.getLogFile(), config.getLogFileSize(), config.getLogFileRotate());
		} catch (IOException e) {
			LOGGER.error(name + ": cannot create tls over ip log file: " + e);
			log = WANComLogger.getWANComLogger(null, 0,  0);
		}
		log.msg(WANComLogger.Msg.ConnectionAccept, "Connection-Accept", osi2Port, osi2Address);
		if( (!socket.isClosed()) && (osi2Address != 0) ) {
			isConnectionAccepted = true;
			sendCommState( true );
		}
	}
	
	/**
	 * do the tls over ip stuff as long as the socket is connected.
	 */
	@Override
	public void run() {
        boolean handleException = false;
		try {
			if (socket instanceof SSLSocket) {
				SSLSocket sslSocket = (SSLSocket) socket;
				MyHandshakeCompletedListener handshakeListener = new MyHandshakeCompletedListener();
				sslSocket.addHandshakeCompletedListener(handshakeListener);
				socket.setSoTimeout(SOCKET_TIMEOUT_SSL);
				LOGGER.info(name + ": SSL Handshake started");
				sslSocket.startHandshake();
				LOGGER.info(name + ": SSL Handshake wait for completion");
				int i=0;
				while(!handshakeListener.isCompleted()) {
					if (++i > 10) {
						LOGGER.info(name + ": SSL Handshake timed out");
						disconnect();
						return;
					}
					uninterruptedSleep200();
				}
				LOGGER.info(name + ": SSL Handshake finished");
			}
			socket.setSoTimeout(SOCKET_TIMEOUT);
			sendHello();
			while (!socket.isClosed()) {
				checkHelloTimeouts(); // check keep alive
				checkHelloToSend();
				while(checkTelegramsToSend()) { // NOSONAR keep running while result of checkTelegramsToSend is true
				}
				checkTimeSync();

				readData();

				if (doShutdown) {
					disconnect();
				}
			}
        } catch ( IOException | IllegalStateException e ) { // now also catch IllegalStateException as result of errors in telegram
            // structure
            LOGGER.error( name + " (Somewhat expected exception): " + e );
            handleException = true;
        } catch ( Exception e ) { // now also catch IllegalStateException as result of errors in telegram structure
            LOGGER.error( name + " (Unexpected exception): " + e );
            handleException = true;
        }
        if ( handleException ) {
            log.msg( WANComLogger.Msg.ConnectionBroken, "Connection-Broken", osi2Port, osi2Address );
            disconnect();
            sendCommState( false );
            return;
        }
		LOGGER.info( name + ": finished");
		sendCommState(false);
	}

	private static void uninterruptedSleep200() {
		try {
			Thread.sleep( 200 );
		} catch( InterruptedException e ) {} // NOSONAR ignore break of sleep
	}

	/**
	 * request a time synchronisation in the next round
	 */
	public void requestTimeSync() {
		setDoTimeSync( true );
	}
	
	public synchronized void setDoTimeSync(boolean doTimeSync) {
		this.doTimeSync = doTimeSync;
	}

	/**
	 * stop communication
	 */
	public void stop() {
		doShutdown = true;		
	}

	/**
	 * closes the socket and reports the new communication state.
	 * This method should be used in case of protocol errors.
	 */
	private void disconnect() {
		try {
			if (!socket.isClosed()) {
				socket.close();
			}
			log.msg(WANComLogger.Msg.ConnectionClose, "Connection-Close", osi2Port, osi2Address);
		} catch (IOException e) {
			LOGGER.error(name + ": disconnect: "+e);
		}
	}

	/**
	 * read data from the socket. 
	 * This method blocks for with a timeout.
	 * @throws IOException IOException
	 */
	private void readData() throws IOException {
		try {
			int len = socket.getInputStream().read(receivedData, receivedSize, BUFFERSIZE-receivedSize);
			if (len < 0) {
				LOGGER.info("connection " + name + ": end of connection detected");
				disconnect();
				return;
			}
			receivedSize += len;
			
			LOGGER.debug(name + ": readData: received " + len + " bytes");
			// reassemble as much as possible
			while(reassemble()) { // NOSONAR continue while reassembles result is true
			}
			
		} catch(SocketTimeoutException e) { // NOSONAR ignore exception
		}
	}
	
	private int makeInt( byte b1, byte b2, byte b3, byte b4 ) {
	    return (((b4 & 0xFF) * 256 + (b3 & 0xFF)) * 256 + (b2 & 0xFF)) * 256 + (b1 & 0xFF); 
	}
	
	/*
    public static class WANComHeader
        int version      // 4 byte | ==35
        int size         // 4 byte | Header + Data
        int type         // 4 byte | ==0
        int numTargetAdr // 4 byte | number of target addresses
        int adrPtr       // 4 byte | pointer to current ip address
        byte[] srcIp     // 8 byte |                                           28 bytes upto here
        byte[] recptAdr  // 8 byte for each recipient according to numAdr
	 */
	
	/**
	 * put the received data fragments together and if enough data is received,
	 * analyze the received data.
	 * @return true if there may be more data in the buffer to reassemble
	 */
	private boolean reassemble() {
        if (receivedSize < HEADERLENGTH) {
        	return false;
        }
        int version = makeInt( receivedData[0], receivedData[1], receivedData[2], receivedData[3] );
        int size = makeInt( receivedData[4], receivedData[5], receivedData[6], receivedData[7] );
        int type = makeInt( receivedData[8], receivedData[9], receivedData[10], receivedData[11] );
        int numTargetAdr = makeInt( receivedData[12], receivedData[13], receivedData[14], receivedData[15] );
        int adrPtr = makeInt( receivedData[16], receivedData[17], receivedData[18], receivedData[19] ); // NOSONAR doc only
        int srcIp = makeInt( receivedData[20], receivedData[21], receivedData[22], receivedData[23] ); // next 4 bytes are ignored  NOSONAR doc only
        
        int teleSize = size;
        if (35 != version) {
            LOGGER.error( name + ": synchronisation failure. Version " + version + " != 35 (required)");
            LOGGER.error( name + ":" + Util.toHex( receivedData, 0, HEADERLENGTH ) );
            disconnect();
            return false;
        }
        if (teleSize > receivedSize) { // the addition of HEADERLENGTH to teleSize was an ugly error
            // wait for more data
            return false;
		}
        
        int additionalHeader = numTargetAdr * 8;

        // genug Daten...
        log.msg( WANComLogger.Msg.RecvDataOsiWANCom, receivedData, 0, teleSize, osi2Port, osi2Address );

        if ( ( WANCOM_OUT == type ) || ( WANCOM_IN == type ) ) {
            doTeleReceived( teleSize, additionalHeader );
        } else if ( 50 == type ) {
            doKeepAliveReceived();
        } else {
            LOGGER.error( name + ": Unbekannter TelType " + type );
            String text = "Invalid TelTyp " + type;
            log.msg( WANComLogger.Msg.InvalidTelType, text, osi2Port, osi2Address );
            disconnect();
            return false;
        }
		memmove(teleSize, receivedSize);

		// Bei den ersten gueltigen Daten den Verbindungsaufbau melden
		if( (!socket.isClosed()) && (!isConnectionAccepted) && ( osi2Address != 0 ) ) {
			isConnectionAccepted = true;
			sendCommState( true );
		}
		return true;
	}


	/**
	 * treat received keep alives
	 */
	private void doKeepAliveReceived() {
		LOGGER.debug(name  + ": got keep alive");
        lastReceivedKeepAlive = new Date();	
	}

	/**
	 * treat received telegram data.
	 * @param teleSize the size of the telegram data
	 */
    private void doTeleReceived( int teleSize, int additionalHeader ) {
        LOGGER.debug( name + ": got telegram, size " + teleSize );
        LOGGER.debug( name + ": " + Util.toHex( receivedData, 0, HEADERLENGTH + additionalHeader ) );
        int nettoTeleSize = teleSize - HEADERLENGTH - additionalHeader;
        if ( nettoTeleSize < 4 || nettoTeleSize > 253 ) {
            LOGGER.error( name + ": Ungueltige Laenge fuer TLS-Telegramm" );
            String text = "Invalid Len (TLS) " + Integer.toHexString( nettoTeleSize );
            log.msg( WANComLogger.Msg.InvalidLenTls, text, osi2Port, osi2Address );
            disconnect();
            return;
        }
        int osi3len = ( ( receivedData[HEADERLENGTH+additionalHeader] & 0x38 ) >> 2 ) + 1;
        log.msg( WANComLogger.Msg.RecvDataOsi3, receivedData, HEADERLENGTH+additionalHeader, osi3len, osi2Port, osi2Address );
        log.msg( WANComLogger.Msg.RecvDataOsi7, receivedData, HEADERLENGTH+additionalHeader + osi3len, teleSize - osi3len, osi2Port, osi2Address );

        // empfangenes Telegramm verarbeiten
        recvTelegram( receivedData, HEADERLENGTH+additionalHeader, nettoTeleSize );
    }

	/**
	 * moves the bytes in the input buffer to the beginning of the input buffer
	 * @param begin index of first byte to move
	 * @param end index of last byte to move
	 */
	private void memmove(int begin, int end) {
		for(int i=0; i<end-begin; ++i) {
			receivedData[i] = receivedData[i+begin];  
		}
		receivedSize -= begin;
	}

	/**
	 * send communication state to parent 
	 * @param state true on connect, false on disconnect
	 */
	private void sendCommState(boolean state) {
		LOGGER.info(name + ": communication state changed to " + (state ? "ALIVE" : "DEAD"));
		parent.sendCommState(osi2Port, osi2Address, state);
	}

	/**
	 * send a received telegram to the parent
	 * @param buffer incoming data where a telegram is taken from
	 * @param offset offset into the buffer
	 * @param size size of the telegram
	 */
	private void recvTelegram(byte[] buffer, int offset, int size) {
		byte[] tele = Arrays.copyOfRange(buffer, offset, offset+size);
		if (osi2Port == 0) {
			// only routings with one routing step are allowed by now
			if ((tele[0] & 0x38) != 8) {
				LOGGER.error(name + ": cannot determine osi2 address, telegram skipped:");
				LOGGER.error(name + ": " + Util.toHex(tele));
				return;
			}
			// received telegram has mirrored routing.
			osi2Address = Util.toUnsignedShort(tele[1]);
			osi2Port = Util.toUnsignedShort(tele[2]);
			config = server.getConfiguration(osi2Port, osi2Address);
			if (config == null) {
				LOGGER.error(name + ": client with the following routing is not configured");
				LOGGER.error(name + ": " + Util.toHex(tele));
				disconnect();
				return;
			}
			
			try {
				log.msg(WANComLogger.Msg.SwitchLogFile, "Switch to log file " + config.getLogFile(), osi2Port, osi2Address);
				log = WANComLogger.getWANComLogger(config.getLogFile(), config.getLogFileSize(), config.getLogFileRotate());
			} catch (IOException e) {
				LOGGER.error("cannot switch logfile: ", e);
			}
			log.msg(WANComLogger.Msg.ConnectionIdentified, "Connection-Identified", osi2Port, osi2Address);

			parent.createSendQueue(osi2Port, osi2Address);
			
			LOGGER.info("Partner identified: " + socket.getRemoteSocketAddress().toString() + " = " + osi2Port + "/" + osi2Address);
			if (osi2Port > 0 && osi2Address > 0) {
				this.name = socket.getRemoteSocketAddress().toString() + " = [" + osi2Port + "/" + osi2Address + "]";
			}

			if (!isConnectionAccepted && osi2Address != 0 ) {
				isConnectionAccepted = true;
				sendCommState( true );
			}
		}
		parent.recvTelegram(tele, osi2Port, osi2Address);
	}

	/**
	 * check for telegrams to send.
	 * @return true if there may be more telegrams to send
	 * @throws IOException IOException 
	 */
	private boolean checkTelegramsToSend() throws IOException {
		if (osi2Port == 0) {
			if (!msgNotYetIdentified) {
				LOGGER.warn(name + ": cannot send telegrams because communication partner is not yet identified");
				msgNotYetIdentified = true;
			}
			return false;
		}
		
		Telegram telegram = parent.getNextTelegram(osi2Port, osi2Address);
		if (telegram == null) {
			return false;
		}
		
		sendTelegram(telegram);
		return true;
	}

	private void sendTelegram(Telegram telegram) throws IOException {
        byte[] tele = telegram.getTelegram();
        
        WANComHeader.WANComHeaderBuilder builder = WANComHeader.builder();
        builder.version( 35 ).size( HEADERLENGTH + tele.length ).type( WANCOM_OUT ).adrPtr( 0 ).srcIp( localIp );
        WANComHeader wcHeader = builder.build();
        
        int hdSize = wcHeader.getSize();
        byte[] buf = new byte[hdSize + tele.length];
        Arrays.fill( buf, (byte) 0 );
        
        wcHeader.getBytes( buf, 0 );
        System.arraycopy( tele, 0, buf, hdSize, tele.length );
        
        LOGGER.debug( name + ": Telegram to send" );
        LOGGER.debug( name + ": Whole data:" + Util.toHex( buf ) );
        sendData( buf );
        
        int osi3len = ( ( buf[hdSize] & 0x38 ) >> 2 ) + 1;
        log.msg( WANComLogger.Msg.SendDataOsiWANCom, buf,                0,                hdSize, osi2Port, osi2Address );
        log.msg( WANComLogger.Msg.SendDataOsi3,      buf,           hdSize,               osi3len, osi2Port, osi2Address );
        log.msg( WANComLogger.Msg.SendDataOsi7,      buf, hdSize + osi3len, tele.length - osi3len, osi2Port, osi2Address );
	}
	

	/**
	 * check if keep alive timed out.
	 */
	private void checkHelloTimeouts() {
		Date now = new Date();
		if (lastReceivedKeepAlive.getTime() + KEEP_ALIVE_TIMEOUT*1000L < now.getTime()) {
			LOGGER.error(name + ": Keep Alive Timeout");
			log.msg(WANComLogger.Msg.TimeoutKeepalive, "Timeout-Keep-Alive", osi2Port, osi2Address);
			disconnect();
		}
	}

	/**
	 * check if we have to send a keep alive.
	 * @throws IOException IOException
	 */
	private void checkHelloToSend() throws IOException {
		Date now = new Date();
		if (lastSentData.getTime() + KEEP_ALIVE_SEND_INTERVAL*1000L < now.getTime()) {
			sendHello();
		}
	}
	
	/**
	 * send hello aka keep alive.
	 * @throws IOException IOException
	 */
	private void sendHello() throws IOException {
	    
	    byte[] buf = WANCom.getKeepAliveTelegram( localIp, config.getKeepAliveDambach() );
		LOGGER.debug(name  + ": KeepAlive to send");
        sendData(buf);
        lastSentData = new Date();
        log.msg(WANComLogger.Msg.TimeoutQuittung, buf, osi2Port, osi2Address);
	}

	/**
	 * send any data to the socket
	 * @param buf the data
	 * @throws IOException IOException
	 */
	private void sendData(byte[] buf) throws IOException {
		socket.getOutputStream().write(buf);
	}

	/**
	 * Send a time synchronization if requested and if send is not blocked.
	 */
	private void checkTimeSync() throws IOException {
		if (!doTimeSync || osi2Port == 0) {
			return;
		}

		byte[] tele;
		switch (timeSyncMode) {
		case WALLTIME:
			tele = makeWalltimeSyncTelegram();
			break;
		case UTC:
			tele = makeUTCtimeSyncTelegram();
			break;
		case USERDELIVERED:
			if (null == timeSyncGenerator) {
				LOGGER.error(this.name + ": No TimeSyncGenerator!");
				setDoTimeSync(false);
				return;
			}
			byte[] etele = timeSyncGenerator.makeTimeSyncTele();
			tele = new byte[7 + etele.length];
	        tele[0] = (byte) 0x89;              // Routing Laenge 1
	        tele[1] = (byte) osi2Port;          // Portadresse
	        tele[2] = (byte) osi2Address;       // Partner- oder Sammeladresse
	        tele[3] = 0;                        // Knotennummer
	        tele[4] = 0;                        // Knotennummer
	        tele[5] = 0;                        // Knotennummer
	        tele[6] = 1;                        // Anzahl Etels
	        
	        int dest = 7;
            for ( byte b : etele ) {
	            tele[dest++] = b;
	        }
			break;
		default:
			LOGGER.error("No timeSync sent to {}. Error in timeSyncMode currently set: {}", this.name,
					timeSyncMode);
			setDoTimeSync(false);
			return;
		}

		Telegram telegram = new Telegram(tele);
		sendTelegram(telegram);

		setDoTimeSync(false);
	}

	private byte[] makeWalltimeSyncTelegram() {
		GregorianCalendar cal = new GregorianCalendar();
		boolean isSummerTime = cal.get(Calendar.DST_OFFSET) > 0;
		byte[] tele = new byte[22];
		tele[0] = (byte) 0x89;              // Routing Laenge 1
		tele[1] = (byte) osi2Port;			// Portadresse
		tele[2] = (byte) osi2Address;       // Partner- oder Sammeladresse
		tele[3] = 0;                      	// Knotennummer
		tele[4] = 0;                      	// Knotennummer
		tele[5] = 0;                      	// Knotennummer
		tele[6] = 1;                      	// Anzahl Etels
		tele[7] = 14;                     	// Länge Etel
		tele[8] = (byte) 254;               // FG
		tele[9] = 2;                      	// ID
		tele[10] = 88;                     	// Jobnummer
		tele[11] = 1;                      	// Anzahl DE-Blöcke
		tele[12] = 9;                      	// Länge DE-Block
		tele[13] = (byte) 255;     	        // DE-Nummer (an KRI und alle IB)
		tele[14] = 18;                     	// DE-Typ
		tele[15] = (byte) (cal.get(Calendar.HOUR_OF_DAY) + (isSummerTime ? 128 : 0)); 	// Stunde
		tele[16] = (byte) (cal.get(Calendar.MINUTE));              						// Minute
		tele[17] = (byte) (cal.get(Calendar.SECOND));              						// Sekunde
		tele[18] = (byte) (cal.get(Calendar.DAY_OF_MONTH));             				// Tag
		tele[19] = (byte) (cal.get(Calendar.MONTH)+1);            						// Monat
		tele[20] = (byte) (cal.get(Calendar.YEAR)%100);         						// Jahr
		tele[21] = (byte) (cal.get(Calendar.DAY_OF_WEEK)-1);           					// Wochentag
		if (tele[21] == 0) {
			tele[21] = 7;    				// Spezialkodierung fuer Sonntag
		}
		return tele;
	}

	private byte[] makeUTCtimeSyncTelegram() {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		GregorianCalendar cal = new GregorianCalendar(tz);
		byte[] tele = new byte[22];
		tele[0] = (byte) 0x89;              // Routing Laenge 1
		tele[1] = (byte) osi2Port;			// Portadresse
		tele[2] = (byte) osi2Address;       // Partner- oder Sammeladresse
		tele[3] = 0;                      	// Knotennummer
		tele[4] = 0;                      	// Knotennummer
		tele[5] = 0;                      	// Knotennummer
		tele[6] = 1;                      	// Anzahl Etels
		tele[7] = 14;                     	// Länge Etel
		tele[8] = (byte) 254;               // FG
		tele[9] = 2;                      	// ID
		tele[10] = 88;                     	// Jobnummer
		tele[11] = 1;                      	// Anzahl DE-Blöcke
		tele[12] = 9;                      	// Länge DE-Block
		tele[13] = (byte) 255;     	        // DE-Nummer (an KRI und alle IB)
		tele[14] = 18;                     	// DE-Typ
		tele[15] = (byte) (cal.get(Calendar.HOUR_OF_DAY));								// Stunde
		tele[16] = (byte) (cal.get(Calendar.MINUTE));              						// Minute
		tele[17] = (byte) (cal.get(Calendar.SECOND));              						// Sekunde
		tele[18] = (byte) (cal.get(Calendar.DAY_OF_MONTH));             				// Tag
		tele[19] = (byte) (cal.get(Calendar.MONTH)+1);            						// Monat
		tele[20] = (byte) (cal.get(Calendar.YEAR)%100);         						// Jahr
		tele[21] = (byte) (cal.get(Calendar.DAY_OF_WEEK)-1);           					// Wochentag
		if (tele[21] == 0) {
			tele[21] = 7;    				// Spezialkodierung fuer Sonntag
		}
		return tele;
	}

	/**
	 * if this server is used only for one client, we can use the osi2 addresses directly
	 */
	public void setSingleServer() {
		osi2Port = config.getOsi2Port();
		osi2Address = config.getOsi2Address();
		parent.createSendQueue(osi2Port, osi2Address);
	}

	private class MyHandshakeCompletedListener implements HandshakeCompletedListener {
		boolean completed = false;
		@Override
		public void handshakeCompleted(HandshakeCompletedEvent event) {
			LOGGER.info(name + ": SSL Handshake completed");
			completed = true;
		}
		boolean isCompleted() {
			return completed;
		}
	}

	public void setTimeSyncMode(TimeSyncMode timeSyncMode) {
		this.timeSyncMode = timeSyncMode;
	}
}
