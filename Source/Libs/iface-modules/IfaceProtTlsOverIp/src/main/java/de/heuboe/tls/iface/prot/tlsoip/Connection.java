package de.heuboe.tls.iface.prot.tlsoip;

import de.heuboe.tls.iface.iface.SystemMessageManagement;
import de.heuboe.tls.iface.iface.TimeSyncGenerator;
import de.heuboe.tls.iface.iface.TimeSyncMode;
import de.heuboe.tls.iface.lib.Util;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;

/**
 * This class handles all the tls over ip protocol details for a connected tcp ip connection.
 * 
 * @author ralfz
 *
 */
@Slf4j
class Connection implements Runnable {

	private static final int BUFFERSIZE = 1024;
	private static final int HEADERLENGTH = 10;

	private static final int SOCKET_TIMEOUT = 1000;
	private static final int SOCKET_TIMEOUT_SSL = 15000;
	
	private final Socket socket;
	private ConnectionConfig config;
	private final TlsOverIp parent;
	private final TlsOverIpServer server;
	
	private final byte[] receivedData;
	private int receivedSize;

	private boolean isConnectionAccepted;

	private String name;
	private TlsOverIpLogger protocolLogger;

	private Date lastReceivedData;
	private Date lastSentData;

	private short osi2Port;
	private short osi2Address;
	
	private int lastReceivedSeqNumToConfirm;
	private int lastSentSeqNum;
	private final List<TimeOfSeqNum> receivedTelegramsToConfirm;
	private final List<TimeOfSeqNum> sentTelegramsNotConfirmed;

	private boolean doShutdown;

	private boolean msgNotYetIdentified;
	
	private boolean doTimeSync;
	private TimeSyncMode timeSyncMode;
	private final TimeSyncGenerator timeSyncGenerator;
	
	private final SystemMessageManagement smm;
	
	private boolean warned = false;

	/**
	 * Constructor for a tls over ip connection
	 * @param socket the connected socket
	 * @param config the tls over ip configuration for this connection 
	 * @param parent the consumer of received telegrams and so on
	 * @param name name of the connection 
	 * @throws IOException If there are problems during dedicated logging
	 */
	Connection(Socket socket, ConnectionConfig config, TlsOverIp parent, String name, TlsOverIpServer server, SystemMessageManagement smm) throws IOException {
		this.socket = socket;
		this.config = config;
		this.parent = parent;
		this.server = server;
		this.name = name;
		this.doTimeSync = false;
		this.smm = smm;
		
		log.info( "new Connection: " + name);
		
		// if server, obtain osi2 addresses from the first received telegram
		osi2Port = server == null ? config.getOsi2Port() : 0;
		osi2Address = server == null ? config.getOsi2Address() : 0;
		if (osi2Port > 0 && osi2Address > 0) {
			this.name = "[" + osi2Port + "/" + osi2Address + "]";
		}
		receivedData = new byte[BUFFERSIZE];
		receivedSize = 0;
		isConnectionAccepted = false;
		lastReceivedSeqNumToConfirm = -1;
		lastSentSeqNum = -1;
		receivedTelegramsToConfirm = new ArrayList<>();
		sentTelegramsNotConfirmed = new ArrayList<>();
		lastReceivedData = new Date();
		lastSentData = new Date();
		msgNotYetIdentified = false;
		timeSyncMode = config.getTimeSyncMode();
		timeSyncGenerator = config.getTimeSyncGenerator();

		try {
			protocolLogger = TlsOverIpLogger.getTlsOverIpLogger(config.getLogFile(), config.getLogFileSize(), config.getLogFileRotate(), smm);
		} catch (IOException e) {
			log.error( name + ": cannot create tls over ip log file: " + e);
			sendToSMM( name  + ": cannot create tls over ip log file: " + e.getMessage() );
			protocolLogger = TlsOverIpLogger.getTlsOverIpLogger( null, 0,  0, smm );
		}
		protocolLogger.msg(TlsOverIpLogger.Msg.ConnectionAccept, "Connection-Accept", osi2Port, osi2Address);
	}
	
	/**
	 * do the tls over ip stuff as long as the socket is connected.
	 */
	@Override
    public void run() {
        boolean handleException = false;
        try {
            if ( socket instanceof SSLSocket ) {
                SSLSocket sslSocket = (SSLSocket) socket;
                MyHandshakeCompletedListener handshakeListener = new MyHandshakeCompletedListener();
                sslSocket.addHandshakeCompletedListener( handshakeListener );
                socket.setSoTimeout( SOCKET_TIMEOUT_SSL );
                log.info( name + ": SSL Handshake started" );
                sslSocket.startHandshake();
                log.info( name + ": SSL Handshake wait for completion" );
                int i = 0;
                while ( !handshakeListener.isCompleted() ) {
                    if ( ++i > 10 ) {
                        log.info( name + ": SSL Handshake timed out" );
                        disconnect();
                        return;
                    }
					//noinspection BusyWait
					Thread.sleep( 200 );
                }
                log.info( name + ": SSL Handshake finished" );
            }
            socket.setSoTimeout( SOCKET_TIMEOUT );
            sendHello();
            while ( !socket.isClosed() ) {
                checkReceiptTimeouts();
                checkHelloTimeouts();
                checkReceiptsToSend();
                checkHelloToSend();
				boolean haveMoreTelegrams = true;
                while ( haveMoreTelegrams ) {
					haveMoreTelegrams = checkTelegramsToSend();
				}
                checkTimeSync();

                readData();

                if ( doShutdown ) {
                    disconnect();
                }
            }
        } catch ( IOException | InterruptedException | IllegalStateException e ) { // now also catch IllegalStateException as result of errors in telegram // NOSONAR ign Interr...
                                                                                 // structure
            log.error( name + " (Somewhat expected exception): " + e );
            handleException = true;
        } catch ( Exception e ) { // now also catch IllegalStateException as result of errors in telegram structure
            log.error( name + " (Unexpected exception): " + e );
            handleException = true;
        }
        if ( handleException ) {
            sendToSMM( this.name + "Connection-Broken osi2 ports: [" + osi2Port + ", " + osi2Address + "]" );
            protocolLogger.msg( TlsOverIpLogger.Msg.ConnectionBroken, "Connection-Broken", osi2Port, osi2Address );
            disconnect();
            sendCommState( false );
            return;
        }
        log.info( name + ": finished" );
        sendCommState( false );
    }
	
	/**
	 * Set a flag to indicate time synchronisation hast to be sent the next time possible
	 */
	public void requestTimeSync() {
		setDoTimeSync( true );
	}
	
	public synchronized void setDoTimeSync(boolean doTimeSync) {
		this.doTimeSync = doTimeSync;
	}
	
	/**
	 * stop this communication thread the next time possible
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
			protocolLogger.msg(TlsOverIpLogger.Msg.ConnectionClose, "Connection-Close", osi2Port, osi2Address);
		} catch (IOException e) {
			log.error( name + ": disconnect: " + e);
			sendToSMM( name + ": disconnect: " + e.getMessage() + ". osi2 ports: [" + osi2Port + ", " + osi2Address + "]" );
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
				log.info( "connection " + name + ": end of connection detected");
				disconnect();
				return;
			}
			receivedSize += len;
			
			log.debug( name + ": readData: received " + len + " bytes");
			// reassemble as much as possible
			boolean canReassemble = true;
			while(canReassemble) {
				canReassemble = reassemble();
			}
			
		} catch(SocketTimeoutException e) { // NOSONAR just to catch the exception
		}
	}

	/**
	 * put the received data fragments together and if enough data is received,
	 * analyze the received data.
	 * @return true if there may be more data in the buffer to reassemble
	 */
	private boolean reassemble() {
        if (receivedSize < HEADERLENGTH) {
        	return false;
        }
        int teleSize = Util.toUnsignedInt(receivedData[6]);
        if (receivedData[0] != 0x68) {
            log.error( name + ": synchronisation failure");
            sendToSMM( name + ": synchronisation failure. osi2 ports: [" + osi2Port + ", " + osi2Address + "]" );
            disconnect();
            return false;
        }
        if (teleSize+HEADERLENGTH > receivedSize) {
            // wait for more data
            return false;
		}
		
		// genug Daten... 
        protocolLogger.msg(TlsOverIpLogger.Msg.RecvDataOsi2, receivedData, 0, HEADERLENGTH, osi2Port, osi2Address);
        int byte1 = Util.toUnsignedInt(receivedData[1]);
		if (byte1 == 0x11) {
		if ( !isConnectionAccepted && (osi2Address != 0) ) {
				// send alive before! frist telegram
				isConnectionAccepted = true;
				log.debug( "alive ra0 " + Thread.currentThread().getId() );
				sendCommState(true);
				try {
					Thread.sleep( 250 );
				}
				catch ( InterruptedException e ) { // NOSONAR ignore sleep exceptions
				}
			}
			doTeleReceived(teleSize);
		} else if (byte1 == 0x80) {
			doKeepAliveReceived();
        } else if (byte1 == 0x90) {
        	doReceiptReceived();
        } else {
        	log.error( name + ": Unbekannter TelType " + byte1);
            String text = "Invalid TelTyp" + Util.toHex(receivedData[1]);
            protocolLogger.msg(TlsOverIpLogger.Msg.InvalidTelType, text, osi2Port, osi2Address);
            sendToSMM( "Invalid TelTyp " + Util.toHex( receivedData[1] ) );
            disconnect();
            return false;                    
		}
		memmove(teleSize+HEADERLENGTH, receivedSize);
		
		// Bei den ersten gueltigen Daten den Verbindungsaufbau melden
		if( (!socket.isClosed()) && (!isConnectionAccepted) && (osi2Address != 0) ) {
			isConnectionAccepted = true;
			log.debug( "alive ra1 " + Thread.currentThread().getId() );
			sendCommState( true );
		}
		return true;
	}

	/**
	 * treat received receipts.
	 */
	private void doReceiptReceived() {
        int seqnum = Util.toUnsignedInt(receivedData[2]) + Util.toUnsignedInt(receivedData[3])*256;
        log.debug( name + ": got receipt for seqnum " + seqnum);
        lastReceivedData = new Date();   // Quittungstelegramme sind Keep Alive 
        if (!isSeqNumOK(seqnum)) {
        	log.error( name + ": Fehler in Sequenznummer (out of range)");
            String text = "Invalid SeqNum " + Integer.toHexString(seqnum);
            protocolLogger.msg(TlsOverIpLogger.Msg.InvalidSeqNum, text, osi2Port, osi2Address);
            sendToSMM( text );
            disconnect();
            return;                    
        }

		// Quittungen aus der Liste der offenen Quittungen entfernen
		// d.h. solange löschen, bis seqNum gefunden wird (sie muss gefunden werden)
		boolean seqNumFound = false;
		for ( int i = 0; i < config.getReceiptCount() && !sentTelegramsNotConfirmed.isEmpty(); ++i) {
			TimeOfSeqNum openSeqNum = sentTelegramsNotConfirmed.get(0);
			sentTelegramsNotConfirmed.remove(0);
			if (openSeqNum.seqNum == seqnum) {
				seqNumFound = true;
				break;
			}
		}
		if (!seqNumFound) {
		    String text = "";
			log.error( name + ": Fehler in der Quittungsverfolgung");
		    text += "Fehler in der Quittungsverfolgung\n";
			log.error( name + ": quittiert wurde Sequenznummer " + seqnum +
					   " und folgende Sequenznummern sind noch offen:");
			text += ": quittiert wurde Sequenznummer \" + seqnum + \r\n"
			        + "                \" und folgende Sequenznummern sind noch offen:\n";
			for(TimeOfSeqNum seqNum : sentTelegramsNotConfirmed) {
				log.error( name + ": " + seqNum.seqNum);
				//noinspection StringConcatenationInLoop
				text += " " + seqNum.seqNum; // NOSONAR happens very rarely
			}
			sendToSMM( text );
		}
	}

	/**
	 * treat received keep alives
	 */
	private void doKeepAliveReceived() {
		log.debug( name + ": got keep alive");
        lastReceivedData = new Date();	
	}

	/**
	 * treat received telegram data.
	 * @param teleSize the size of the teleram data
	 */
	private void doTeleReceived(int teleSize) {
		log.debug( name + ": got telegram, size " + teleSize);
		log.debug( name + ": " + Util.toHex(receivedData, 0, HEADERLENGTH));
        if (teleSize < 4 || teleSize > 253) {
        	log.error( name + ": Ungueltige Laenge fuer TLS-Telegramm");
            String text = "Invalid Len (TLS) " + Integer.toHexString(teleSize);
            protocolLogger.msg(TlsOverIpLogger.Msg.InvalidLenTls, text, osi2Port, osi2Address);
            sendToSMM( text + ": " + teleSize );
            disconnect();
            return;                                        
        }
        int osi3len = ((receivedData[HEADERLENGTH] & 0x38) >> 2) + 1;
        protocolLogger.msg(TlsOverIpLogger.Msg.RecvDataOsi3, receivedData,HEADERLENGTH, osi3len, osi2Port, osi2Address);
        protocolLogger.msg(TlsOverIpLogger.Msg.RecvDataOsi7, receivedData, HEADERLENGTH + osi3len, teleSize - osi3len, osi2Port, osi2Address);
        
        // Sequenznummer überprüfen
		int rcvSeqNum = Util.toUnsignedInt(receivedData[2]) + Util.toUnsignedInt(receivedData[3])*256;
		if (rcvSeqNum != lastReceivedSeqNumToConfirm+1) {
			log.error( name + ": Ungueltige Sequenz-Nummer empfangen");
			String text = "Invalid SeqNum " + Integer.toHexString(rcvSeqNum);
			protocolLogger.msg(TlsOverIpLogger.Msg.InvalidSeqNum, text, osi2Port, osi2Address);
            sendToSMM( text + " != " + lastReceivedSeqNumToConfirm+1 );
			disconnect();
			return;                                        
		}
		// ggf. Quittung senden
		receivedTelegramsToConfirm.add( new TimeOfSeqNum( rcvSeqNum, new Date() ) );

		// empfangen Sequenznummer merken
		lastReceivedSeqNumToConfirm = rcvSeqNum;
		if (lastReceivedSeqNumToConfirm == 65535) {
			lastReceivedSeqNumToConfirm = -1;
		}
		
		// empfangenes Telegramm verarbeiten
		recvTelegram(receivedData, HEADERLENGTH, teleSize);
		lastReceivedData = new Date();					// fuer Keep Alive

	}

	/**
	 * moves the bytes in the input buffer to the beginning of the input buffer
	 * @param begin of area to be moved
	 * @param end of area to be moved
	 */
	private void memmove(int begin, int end) {
		for(int i=0; i<end-begin; ++i) {
			receivedData[i] = receivedData[i+begin];  
		}
		receivedSize -= begin;
	}

	/**
	 * check if a given sequence number corresponds to an not yet confirmed sent telegram
	 * @param seqnum the sequence number
	 * @return true if sequence number is valid
	 */
	private boolean isSeqNumOK(int seqnum) {
		for(TimeOfSeqNum s : sentTelegramsNotConfirmed) {
			if (s.seqNum == seqnum) {
				return true;
			}
		}
		return false;
	}

	/**
	 * send communication state to parent 
	 * @param state true on connect, false on disconnect
	 */
	private void sendCommState(boolean state) {
		log.info( name + " [" + osi2Port + "/" + osi2Address + "]: communication state changed to " + (state ? "ALIVE" : "DEAD"));
		parent.sendCommState(osi2Port, osi2Address, state);
	}

	/**
	 * send a received telegram to the parent
	 * @param buffer where received telegram has to be created from
	 * @param offset offset into buffer
	 * @param size number of bytes that constitute the telegram
	 */
	private void recvTelegram(
			byte[] buffer, @SuppressWarnings(
			{ "SameParameterValue" } ) int offset,
			int size) {
		byte[] tele = Arrays.copyOfRange(buffer, offset, offset+size);
		if (osi2Port == 0) {
			// only routings with one routing step are allowed by now
			if ((tele[0] & 0x38) != 8) {
				log.error( name + ": cannot determine osi2 address, telegram skipped:");
				log.error( name + ": " + Util.toHex(tele));
	            sendToSMM( "cannot determine osi2 address, telegram skipped:" +  Util.toHex(tele) );				
				return;
			}
			// received telegram has mirrored routing.
			osi2Address = Util.toUnsignedShort(tele[1]);
			osi2Port = Util.toUnsignedShort(tele[2]);
			config = server.getConfiguration(osi2Port, osi2Address);
			if (config == null) {
				log.error( name + ": client with the following routing is not configured");
				log.error( name + ": " + Util.toHex(tele));
				sendToSMM( "client with the following routing is not configured:" +  Util.toHex(tele) );				
				disconnect();
				return;
			}

            try {
                protocolLogger.msg( TlsOverIpLogger.Msg.SwitchLogFile, "Switch to log file " + config.getLogFile(), osi2Port, osi2Address );
                protocolLogger = TlsOverIpLogger.getTlsOverIpLogger( config.getLogFile(), config.getLogFileSize(), config.getLogFileRotate(), smm );
            } catch ( IOException e ) {
                log.error( "cannot switch logfile: ", e );
                sendToSMM( "cannot switch logfile:" + e.getMessage() );
            }
            protocolLogger.msg( TlsOverIpLogger.Msg.ConnectionIdentified, "Connection-Identified", osi2Port, osi2Address );

			parent.createSendQueue(osi2Port, osi2Address);
			
			log.info( "Partner identified: " + socket.getRemoteSocketAddress().toString() + " = " + osi2Port + "/" + osi2Address);
			if (osi2Port > 0 && osi2Address > 0) {
				this.name = socket.getRemoteSocketAddress().toString() + " = [" + osi2Port + "/" + osi2Address + "]";
			}

		if ( !isConnectionAccepted  && (osi2Address != 0) ) {
				isConnectionAccepted = true;
				log.debug( "alive recv" );
				sendCommState(true);
			}
		}
		log.debug( "have tele in thread " + Thread.currentThread().getId() );
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
				log.warn( name + ": cannot send telegrams because communication partner is not yet identified");
                sendToSMM( "warn: cannot send telegrams because communication partner is not yet identified" );             
				msgNotYetIdentified = true;
			}
			return false;
		}
		if (sentTelegramsNotConfirmed.size() >= config.getReceiptCount()) {
			log.debug( name + ": cannot send telegrams because of to much open receipts");
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
		++lastSentSeqNum;
		if (lastSentSeqNum > 0xFFFF) {
			lastSentSeqNum = 0;
		}
		sentTelegramsNotConfirmed.add( new TimeOfSeqNum( lastSentSeqNum, new Date() ) );
		
		byte[] tele = telegram.getTelegram();
		
		byte[] buf = new byte[10+tele.length];
		Arrays.fill(buf, (byte) 0);
		buf[0] = (byte) 0x68;
		buf[1] = (byte) 0x11;
		buf[2] = (byte) (lastSentSeqNum % 256);
		buf[3] = (byte) (lastSentSeqNum / 256);
		buf[6] = (byte) tele.length;
		
		System.arraycopy(tele, 0, buf, HEADERLENGTH, tele.length);
		log.debug( name + ": Telegram to send");
        sendData(buf);

        int osi3len = ((buf[HEADERLENGTH] & 0x38) >> 2) + 1;            
        protocolLogger.msg(TlsOverIpLogger.Msg.SendDataOsi2, buf, 0, HEADERLENGTH, osi2Port, osi2Address);
        protocolLogger.msg(TlsOverIpLogger.Msg.SendDataOsi3, buf, HEADERLENGTH, osi3len, osi2Port, osi2Address);
        protocolLogger.msg(TlsOverIpLogger.Msg.SendDataOsi7, buf, HEADERLENGTH + osi3len, tele.length - osi3len, osi2Port, osi2Address);
	}

	/**
	 * check if keep alive timed out.
	 */
	private void checkHelloTimeouts() {
		Date now = new Date();
		if (lastReceivedData.getTime() + config.getHelloTimeout()*1000L < now.getTime()) {
			log.error( name + ": Keep Alive Timeout");
			protocolLogger.msg(TlsOverIpLogger.Msg.TimeoutKeepalive, "Timeout-Keep-Alive", osi2Port, osi2Address);
            sendToSMM( "Keep Alive Timeout" );             
			disconnect();
		}
	}

	/**
	 * check if receipts timed out.
	 */
	private void checkReceiptTimeouts() {
		if (sentTelegramsNotConfirmed.isEmpty()) {
		    warned = false;
			return;
		}
		TimeOfSeqNum seqnum = sentTelegramsNotConfirmed.get(0);
		Date now = new Date();
		long receiptConfirmationTime = seqnum.date.getTime() + config.getReceiptTimeout()*1000L;
		if (receiptConfirmationTime < now.getTime()) {
		    if (null != config.getReceiptGraceTime() && 0 != config.getReceiptGraceTime()) {
		        // we have a grace time here
	            if ( !warned ) {
	                if (receiptConfirmationTime + 1010 >= now.getTime()) { //warning 1000 msec after configured timeout
	                    return;
	                }
	                log.warn( name + ": Receipt Timeout(grace)");
	                protocolLogger.msg(TlsOverIpLogger.Msg.TimeoutQuittungGrace, "Timeout-Quittung-Grace", osi2Port, osi2Address);
	                sendToSMM( "Receipt Timeout Grace" );
	                warned = true;
	                return;
	            }
	            if (receiptConfirmationTime + config.getReceiptGraceTime() < now.getTime()) {
	                log.error( name + ": Receipt Timeout");
                    protocolLogger.msg(TlsOverIpLogger.Msg.TimeoutQuittung, "Timeout-Quittung", osi2Port, osi2Address);
    	            disconnect();
    	            warned = false;
	            }
		    } else {
		        // NO grace time // old behaviour
		        log.error( name + ": Receipt Timeout");
		        protocolLogger.msg(TlsOverIpLogger.Msg.TimeoutQuittung, "Timeout-Quittung", osi2Port, osi2Address);
		        sendToSMM( "Receipt Timeout" );
		        disconnect();
	        }
		}
	}

	/**
	 * check if we have to send receipts for received telegrams.
	 * @throws IOException IOException
	 */
	private void checkReceiptsToSend() throws IOException {
		if (receivedTelegramsToConfirm.isEmpty()) {
			return;
		}
		TimeOfSeqNum seqnum = receivedTelegramsToConfirm.get(0);
		if (receivedTelegramsToConfirm.size() >= config.getReceiptCount()) {
			log.debug( name + ": checkReceiptsToSend: receipt count reached");
			for(int i=0; i<config.getReceiptCount(); ++i) {
				seqnum = receivedTelegramsToConfirm.remove(0); // NOSONAR kind of  a queue
			}
			sendReceipt(seqnum.seqNum);
			return;
		}
		Date now = new Date();
		if (seqnum.date.getTime() + config.getReceiptDelay()*1000L < now.getTime()) {
			log.debug( name + ": checkReceiptsToSend: receipt delay reached");
			seqnum = receivedTelegramsToConfirm.get(receivedTelegramsToConfirm.size()-1);
			sendReceipt(seqnum.seqNum);
			receivedTelegramsToConfirm.clear();
		}
	}
	
	/**
	 * send a receipt.
	 * @param seqNum the sequence number of the receipt
	 * @throws IOException IOException
	 */
	private void sendReceipt(int seqNum) throws IOException {
		byte[] buf = new byte[10];
		Arrays.fill(buf, (byte) 0);
		buf[0] = (byte) 0x68;
		buf[1] = (byte) 0x90;
		buf[2] = (byte) (seqNum % 256);
		buf[3] = (byte) (seqNum / 256);
		log.debug( name + ": Receipt to send: seqNum 0x" + Integer.toHexString(seqNum));
        sendData(buf);
        protocolLogger.msg(TlsOverIpLogger.Msg.SendDataOsi2, buf, osi2Port, osi2Address);
		
	}

	/**
	 * check if we have to send a keep alive.
	 * @throws IOException IOException
	 */
	private void checkHelloToSend() throws IOException {
		Date now = new Date();
		if (lastSentData.getTime() + config.getHelloDelay()*1000L < now.getTime()) {
			sendHello();
		}
	}
	
	/**
	 * send hello aka keep alive.
	 * @throws IOException IOException
	 */
	private void sendHello() throws IOException {
		byte[] buf = new byte[10];
		Arrays.fill(buf, (byte) 0);
		buf[0] = (byte) 0x68;
		buf[1] = (byte) 0x80;
		log.debug( name + ": KeepAlive to send");
        sendData(buf);
        protocolLogger.msg(TlsOverIpLogger.Msg.SendDataOsi2, buf, osi2Port, osi2Address);
	}

	/**
	 * send any data to the socket
	 * @param buf the data
	 * @throws IOException IOException
	 */
	private void sendData(byte[] buf) throws IOException {
		socket.getOutputStream().write(buf);
		lastSentData = new Date();
	}

	/**
	 * a helper class combining a sequence number with the time of it's creation.
	 * @author ralfz
	 *
	 */
	private static class TimeOfSeqNum {
		int seqNum;
		Date date;
		public TimeOfSeqNum(int seqNum, Date date) {
			super();
			this.seqNum = seqNum;
			this.date = date;
		}
	}

	/**
	 * Send a time synchronization if requested and if send is not blocked.
	 */
	private void checkTimeSync() throws IOException {
		if (!doTimeSync || osi2Port == 0) {
			return;
		}
		if (sentTelegramsNotConfirmed.size() >= config.getReceiptCount()) {
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
				log.error( this.name + ": No TimeSyncGenerator!");
	            sendToSMM( "Missing TimeSyncGenerator!" );             
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
            log.error( "No timeSync sent to " + this.name + ". Error in timeSyncMode currently set to: " + timeSyncMode );
            sendToSMM( "No timeSync sent. Error in timeSyncMode currently set to: " + timeSyncMode );
            setDoTimeSync( false );
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
			log.info( name + ": SSL Handshake completed");
			completed = true;
		}
		boolean isCompleted() {
			return completed;
		}
	}

	@SuppressWarnings( "unused" )
	public void setTimeSyncMode( TimeSyncMode timeSyncMode) {
		this.timeSyncMode = timeSyncMode;
	}
    
    private void sendToSMM( String msg ) {
        if ( null != smm ) {
            smm.sendMessage( name + "- osi2 ports: [" + osi2Port + ", " + osi2Address + "]:" + msg );
        }
    }
}
