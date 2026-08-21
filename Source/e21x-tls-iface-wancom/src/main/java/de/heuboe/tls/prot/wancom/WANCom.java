package de.heuboe.tls.prot.wancom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.heuboe.log.Logger;
import de.heuboe.tls.iface.iface.IfaceApplication;
import de.heuboe.tls.iface.iface.IfaceProtocol;
import de.heuboe.tls.iface.lib.Util;
import lombok.Builder;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

/**
 * parent (coordinator) of the wancom connections
 * @author ronald
 */
@Slf4j
public class WANCom implements IfaceProtocol {

	/**
	 * Class in order to handle headers of the wancom protocol
	 */
    @Builder
    public static class WANComHeader {
        public static final int MIN_HEADER_SIZE = 28;
        int                     version;             // 4 byte | ==35
        int                     size;                // 4 byte | Header + Data (incl size and version)
        int                     type;                // 4 byte | ==0
        int                     numTargetAdr;        // 4 byte | number of target addresses / value not really used
        int                     adrPtr;              // 4 byte | pointer to current ip address
        int                     srcIp;               // 8 byte | 28 bytes upto here
        int[]                   recptAdr;            // 8 byte for each recipient according to numAdr usually empty

		/**
		 * Get the sequence of bytes of a wancom header
		 * @param buf where the header will be written to
		 * @param offset offset into the byte sequence
		 */
		public void getBytes( byte[] buf, int offset ) {
            int sz = MIN_HEADER_SIZE + 8 * numTargetAdr;
            if ( 0 > buf.length - offset - sz ) {
                throw new IllegalStateException( "buffer too small for WANCom header. bl=" + buf.length + " offset=" + offset + " sz=" + sz  );
            }
            makeBytes( version, buf, offset );
            makeBytes( size, buf, offset + 4 );
            makeBytes( type, buf, offset + 8 );
            if (null == recptAdr) {
                makeBytes( 0, buf, offset + 12 );
            } else {
                makeBytes( recptAdr.length, buf, offset + 12 );
            }
            makeBytes( adrPtr, buf, offset + 16 );

            makeBytes( srcIp, buf, offset + 20 );
            makeBytes( 0, buf, offset + 24 );

            int numTarg = 1;
            if (numTargetAdr == 0 || null == recptAdr) {
                numTarg = 0;
            }
            if (null != recptAdr) {
                numTargetAdr = recptAdr.length;
            }

            if (((numTargetAdr != 0) && (null == recptAdr)) || ((numTargetAdr == 0) && (null != recptAdr))) {
                throw new IllegalStateException("numTargetAdr does not match recptAdr[]");
            }

            int ptr = 28;
            for ( int i = 0; i < numTarg; ++i ) {
                int adr = recptAdr[i];
                makeBytes( adr, buf, offset + ptr );
                ptr += 4;
            }
        }

        public int getSize() {
            return MIN_HEADER_SIZE + 8 * numTargetAdr;
        }

        private void makeBytes( int val, byte[] buf, int offset ) {
            buf[offset] = (byte) ( val % 256 );
            offset += 1;
            val /= 256;
            buf[offset] = (byte) ( val % 256 );
            offset += 1;
            val /= 256;
            buf[offset] = (byte) ( val % 256 );
            offset += 1;
            val /= 256;
            buf[offset] = (byte) ( val % 256 );
        }
    }


    private static TimeZone     defaultTimeZone   = TimeZone.getTimeZone( "Europe/Berlin" ); // timezone where tls timestamps operate in
    // Europe/Berlin MEZ with daylight saving time
    // GMT+01:00 MEZ but always wintertime !needs to be exact!
    // UTC ...

    public static void setTimeZone( String tzID ) { // need a valid timezone ID here. otherwise check may fails
        defaultTimeZone = TimeZone.getTimeZone( tzID );
        if ( !tzID.equals( defaultTimeZone.getID() ) ) {
            throw new IllegalArgumentException( tzID + " seems to be no valid timezone id" );
        }
        LOGGER.info( "Using timezone " + defaultTimeZone.getID() + " for TLS timestamps" );
    }

	/**
	 * build a byte array that can be used for keepalive handshake
	 * @param senderIp will be woven into telegram
	 * @param keepAliveDambach determine what kind of keepalaive handshake is to be used
	 * @return the byte array representign the time sync telegram
	 */
	public static byte[] getKeepAliveTelegram( int senderIp, boolean keepAliveDambach ) { // according to specification document WNACOM 1.3
        int ptr = 0;

        int additionalKADSize = keepAliveDambach ? 8 : 0;

        byte[] res = new byte[ 43 + additionalKADSize ];

        // WANCom Header || !!! little endian

        res[ptr++] = 35; // version
        res[ptr++] = 0;
        res[ptr++] = 0;
        res[ptr++] = 0;
        res[ptr++] = (byte)(43 + additionalKADSize); // size
        res[ptr++] = 0;
        res[ptr++] = 0;
        res[ptr++] = 0;
        res[ptr++] = 50; // type
        res[ptr++] = 0;
        res[ptr++] = 0;
        res[ptr++] = 0;
        res[ptr++] = 0; // numTargetAdr
        res[ptr++] = 0;
        res[ptr++] = 0;
        res[ptr++] = 0;
        res[ptr++] = 0; // adrPtr
        res[ptr++] = 0;
        res[ptr++] = 0;
        res[ptr++] = 0;
        // little endian eh? lets laugh ...
        res[ptr++] = (byte) ((senderIp & 0xff000000) >> 24);
        res[ptr++] = (byte) ((senderIp & 0xff0000) >> 16);
        res[ptr++] = (byte) ((senderIp & 0xff00) >> 8);
        res[ptr++] = (byte) (senderIp & 0xff);
        res[ptr++] = 0;
        res[ptr++] = 0;
        res[ptr++] = 0;
        res[ptr++] = 0;

        // Data

        res[ptr++] = 9; // osi3
        res[ptr++] = (byte) 255;
        res[ptr++] = (byte) 255;
        res[ptr++] = 0; // osi7
        res[ptr++] = 0;
        res[ptr++] = 0;
        res[ptr++] = 1; // number of etels

        if (keepAliveDambach) { // Dambach WANCOM keep alive telegram
            GregorianCalendar t = new GregorianCalendar( defaultTimeZone );

            // @formatter:off
            res[ptr++] = 15;         // Laenge E-Tel
            res[ptr++] = (byte) 220;
            res[ptr++] = 2;
            res[ptr++] = 0;
            res[ptr++] = 1;

            res[ptr++] = 10;        // Laenge DE
            res[ptr++] = (byte) 255;
            res[ptr++] = (byte) 152;

            res[ptr++] = (byte)  t.get( Calendar.DAY_OF_MONTH );
            log.trace( "Day {}", (res[ptr-1] & 0xFF) );
            res[ptr++] = (byte) (t.get( Calendar.MONTH ) + 1);
            log.trace( "Mon {}", (res[ptr-1] & 0xFF) );
            res[ptr++] = (byte) (t.get( Calendar.YEAR ) % 100);
            log.trace( "Year {}", (res[ptr-1] & 0xFF) );
            res[ptr++] = (byte)  t.get( Calendar.HOUR_OF_DAY );
            log.trace( "Hour {}", (res[ptr-1] & 0xFF) );
            res[ptr++] = (byte)  t.get( Calendar.MINUTE );
            log.trace( "Min {}", (res[ptr-1] & 0xFF) );
            res[ptr++] = (byte)  t.get( Calendar.SECOND );
            log.trace( "Sec {}", (res[ptr-1] & 0xFF) );
            res[ptr++] = (byte) (t.get( Calendar.DST_OFFSET ) != 0 ? 1 : 0);
            log.trace( "IsDst {}", (res[ptr-1] & 0xFF) );
            res[ptr]   = (byte) (t.get( Calendar.DAY_OF_WEEK ) - 1);
            if (res[ptr] == 0) {
                res[ptr] = 7; // Spezialkodierung fuer Sonntag
            }
            log.trace( "DOW {}", (res[ptr] & 0xFF) );
            // @formatter:on
        } else { // 'standard' WANCOM keep alive telegram
            res[ptr++] = 7; // single tele // einzeltelegramm // length
            res[ptr++] = (byte) 134;
            res[ptr++] = 2;
            res[ptr++] = 0;
            res[ptr++] = 1;

            res[ptr++] = 2; // de block
            res[ptr++] = (byte) 255;
            res[ptr++] = (byte) 130; // NOSONAR better optics
        }

        return res;
    }

	private static final Logger LOGGER = Logger.getLogger(WANCom.class);

	/**
	 * access to the iface application instance to pass received telegrams to
	 */
	private IfaceApplication ifaceApplication;

	/**
	 * a map for telegrams that have to be send by the communication threads
	 */
	private ConcurrentHashMap<String, ConcurrentLinkedQueue<Telegram>> teleToSend = new ConcurrentHashMap<>();

	/**
	 * the partner list received from the application (generated by routing data)
	 */
	private Map<Short, List<Short>> applicationKnownPartners;

	/**
	 * the own config containing the tls over ip parameters.
	 */
	private WANComConfig config;

	/**
	 * a map with the communication states
	 */
	private List<WANComClient> clientList = new ArrayList<>();

	/**
	 * a map with all runing server ports and their configuration
	 */
	private Map<Integer, WANComServer> serverMap = new HashMap<>();

	/**
	 * Constructor with external provided configuration.
	 *
	 * @param config the WONCom configuration
	 */
	public WANCom(WANComConfig config) {
		this.config = config;
	}

	/**
	 * Constructor with filename for the configuration.
	 * @param configFile a file containing the tls over ip configuration
	 * @throws IOException if file could not be read
	 */
	public WANCom(String configFile) throws IOException {
	    LOGGER.info( "Starting WANCOM with config " + configFile );
		config = new ConfigReader(configFile).getWANComConfig();
		LOGGER.info( "Config read." );
	}

	/**
	 * send a tls telegram to osi2port/osi2partner.
	 * @param tele the tls telegram (Sammeltelegramm)
	 * @param osi2port osi2port where receiver is connected
	 * @param osi2partner osi2 address of receiver
	 */
	@Override
	public void sendTelegram(byte[] tele, short osi2port, short osi2partner) {
		LOGGER.debug("sendTelegram to " + osi2port + "/" + osi2partner);
		LOGGER.debug("Data = " + Util.toHex(tele));
		if (getConfiguration(osi2port, osi2partner) == null) {
			// silently ignore
			return;
		}
		ConcurrentLinkedQueue<Telegram> teleList = teleToSend.get(Util.getKey(osi2port, osi2partner));
		if (teleList != null) {
			teleList.add(new Telegram(tele));
			if (teleList.size() > 100 && (teleList.size() % 1000) == 0) {
				LOGGER.warn("Size of teleList for " + osi2port + "/" + osi2partner + " = " + teleList.size());
			}
		} else {
			LOGGER.warn("cannot send telegram: communication to " + osi2port + "/" + osi2partner + " didn't identify yet.");
		}
	}

	/**
	 * start communication for a single receiver, all receivers that are connected to a given osi2port or all configured receivers.
	 * @param osi2port osi2port where receiver is connected. If null, all configured receivers are started
	 * @param osi2partner osi2 address of receiver. If null, all receivers connected to the osi2port are started
	 */
	@Override
	public void startCommunication(Short osi2port, Short osi2partner) {
		if (osi2port != null && osi2partner != null) {
			startCommForPartner(osi2port, osi2partner);
		} else if (osi2port != null) {
			startCommForPort(osi2port);
		} else {
			startCommForAll();
		}

	}

	/**
	 * stop communication for a single receiver, all receivers that are connected to a given osi2port or all configured receivers.
	 * @param osi2port osi2port where receiver is connected. If null, all configured receivers are started
	 * @param osi2partner osi2 address of receiver. If null, all receivers connected to the osi2port are started
	 */
	@Override
	public void stopCommunication(Short osi2port, Short osi2partner) {
		List<WANComClient> clientsToStop = new ArrayList<>();
		for(WANComClient client : clientList) {
			// the method isPortPartner() handles null values for osi2port and/or osi2partner
			if (client.isPortPartner(osi2port, osi2partner)) {
				clientsToStop.add(client);
			}
		}
		for(WANComClient client : clientsToStop) {
			client.stopCommunication();
			clientList.remove(client);
		}
	}

	/**
	 * Setter for the iface application.
	 * @param ifaceApplication the iface application
	 */
	@Override
	public void setIfaceApplication(IfaceApplication ifaceApplication) {
		this.ifaceApplication = ifaceApplication;
	}

	/**
	 * Setter for the configured receivers. The map contains the osi2port as key and a list of osi2partner addresses as value.
	 * @param partnerMap the map containing all receivers
	 */
	@Override
	public void setPartners(Map<Short, List<Short>> partnerMap) {
		applicationKnownPartners = partnerMap;
	}

	void createSendQueue(short osi2Port, short osi2Partner) {
		String key = Util.getKey(osi2Port, osi2Partner);
		ConcurrentLinkedQueue<Telegram> queue = new ConcurrentLinkedQueue<>();
		teleToSend.put(key, queue);
	}

	void recvTelegram(byte[] tele, short osi2port, short osi2Address) {
		ifaceApplication.recvTelegramm(tele, osi2port, osi2Address);
	}

	Telegram getNextTelegram(short osi2port, short osi2partner) {
		ConcurrentLinkedQueue<Telegram> teleList = teleToSend.get(Util.getKey(osi2port, osi2partner));
		if (teleList != null) {
			if (!teleList.isEmpty()) {
				return teleList.remove();
			}
		} else {
			LOGGER.warn("cannot send telegram: connection " + Util.getKey(osi2port, osi2partner) + " is not known.");
		}
		return null;
	}

	void sendCommState(short osi2Port, short osi2Address, boolean state) {
		ifaceApplication.recvCommunicationState(osi2Port, osi2Address, state, false);
	}

	private void startCommForAll() {
		for(Entry<Short, List<Short>> entry : applicationKnownPartners.entrySet()) {
			Short port = entry.getKey();
			for(Short partner : entry.getValue()) {
				startCommForPartner(port, partner);
			}
		}
	}

	private void startCommForPort(short port) {
		List<Short> partners = applicationKnownPartners.get(port);
		for(Short partner : partners) {
			startCommForPartner(port, partner);
		}
	}

	private void startCommForPartner(short osi2Port, short osi2Partner) {
		ConnectionConfig curConfig = getConfiguration(osi2Port, osi2Partner);
		LOGGER.debug( "startCommForPartner osi2 " + osi2Port + "/" + osi2Partner + " cfg=" + curConfig );
		if (curConfig == null) {
			LOGGER.warn("cannot start communication for " + osi2Port + "/" + osi2Partner + " because it has no tls over ip configuration");
			return;
		}
		if (curConfig.getClient() == null) {
			LOGGER.warn("cannot start communication for " + osi2Port + "/" + osi2Partner + " because you didn't tell if it is client or server");
			return;
		}
		LOGGER.debug( "start a connection" );
        if ( Boolean.TRUE.equals(curConfig.getClient()) ) { // teamwork sonar and IntelliJ ...
			startClientCommunication(curConfig);
		} else {
			startServerCommunication(curConfig);
		}
	}

	private void startClientCommunication(ConnectionConfig config) {
		String name = config.getServerHost() + ":" + config.getTcpPort();
		WANComClient client = new WANComClient(config, this, name);
		clientList.add(client);
	}

	private void startServerCommunication(ConnectionConfig config) {
		WANComServer server = serverMap.get(config.getTcpPort());
		if (server == null) {
			String name = "ServerPort: " + config.getTcpPort();
			boolean isSingleServer = checkSingleServer(config.getTcpPort());
			if (isSingleServer) {
				LOGGER.info(name + ": is single server");
				server = new WANComServer(config, this, name);
				server.setSingleServer();
			} else {
				LOGGER.info(name + ": is multi server");
				// try to get a configuration for 0/0 or osi2Port/0
				ConnectionConfig cfg = getConfiguration(config.getOsi2Port(), (short)0);
				if (cfg == null) {
					cfg = getConfiguration((short)0, (short)0);
				}
				if (cfg == null) {
					cfg = config;
				}
				server = new WANComServer(cfg, this, name);
				if (cfg != config) {
					server.addConfiguration(config);
				}
			}
			serverMap.put(config.getTcpPort(), server);
		} else {
			server.addConfiguration(config);
		}
	}

	private boolean checkSingleServer(int tcpport) {
		int cnt=0;
		for(ConnectionConfig cfg : config.getConnectionConfigList()) {
			if (cfg.getTcpPort() == tcpport) {
				++cnt;
			}
		}
		return (cnt == 1);
	}

	private ConnectionConfig getConfiguration(short osi2port, short osi2address) {
		for(ConnectionConfig cfg : config.getConnectionConfigList()) {
			if (cfg.getOsi2Port().equals(osi2port) && cfg.getOsi2Address().equals(osi2address)) {
				return cfg;
			}
		}
		return null;
	}

	/**
	 * generate a global time synchronisation for all receivers.
	 */
	@Override
	public void timeSynchronzation() {
		for(WANComClient client : clientList) {
			client.timeSync();
		}
		for(WANComServer server : serverMap.values()) {
			server.timeSync();
		}
	}

    @Override
    public IfaceApplication getIfaceApplication() {
        return this.ifaceApplication;
    }

}
