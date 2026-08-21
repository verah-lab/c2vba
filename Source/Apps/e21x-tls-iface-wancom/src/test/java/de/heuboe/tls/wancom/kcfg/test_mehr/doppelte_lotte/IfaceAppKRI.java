package de.heuboe.tls.wancom.kcfg.test_mehr.doppelte_lotte;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import de.heuboe.log.Logger;
import de.heuboe.tls.iface.iface.IfaceApplication;
import de.heuboe.tls.iface.iface.IfaceException;
import de.heuboe.tls.iface.iface.IfaceProtocol;
import de.heuboe.tls.iface.iface.IfaceRouting;
import de.heuboe.tls.iface.iface.IfaceSystemConnector;
import de.heuboe.tls.iface.iface.RoutingEntry;
import de.heuboe.tls.iface.iface.SystemMessageManagement;
import de.heuboe.tls.iface.lib.Util;

public class IfaceAppKRI implements IfaceApplication {

	private static final Logger LOGGER = Logger.getLogger(IfaceAppKRI.class);

	private IfaceSystemConnector ifaceSystemConnector;
	private IfaceProtocol ifaceProtocol;
	private IfaceRouting ifaceRouting;
	private int ifaceKey;

	private ConcurrentHashMap<Integer, Boolean> commStateMap;

	public void init() throws IfaceException {
		if (ifaceSystemConnector == null) {
			throw new IfaceException("invalid configuration: iface system connector is not set");
		}
		if (ifaceProtocol== null) {
			throw new IfaceException("invalid configuration: iface protocol is not set");
		}
		if (ifaceRouting == null) {
			throw new IfaceException("invalid configuration: iface routing is not set");
		}
		if (ifaceKey == 0) {
			throw new IfaceException("invalid configuration: iface key is not set");
		}
		commStateMap = new ConcurrentHashMap<>();
        LOGGER.info( "csm " + commStateMap + " !D" );
		ifaceProtocol.setIfaceApplication(this);
		ifaceSystemConnector.setIfaceApplication(this);

		initPartners();
	}

	public void setIfaceSystemConnector(IfaceSystemConnector ifaceSystemConnector) {
		this.ifaceSystemConnector = ifaceSystemConnector;
	}

	public void setIfaceProtocol(IfaceProtocol ifaceProtocol) {
		this.ifaceProtocol = ifaceProtocol;
	}

	public void setIfaceRouting(IfaceRouting ifaceRouting) {
		if (this.ifaceRouting != null) {
			// re-initialisation of routing: stop communication and init partners again
			this.ifaceRouting = ifaceRouting;
			ifaceProtocol.stopCommunication(null, null);
			initPartners();
		} else {
			// first time, then only set the routing
			this.ifaceRouting = ifaceRouting;
		}
	}

	@Override
	public int getIfaceKey() {
		return ifaceKey;
	}

    public void setIfaceKey(int ifacekey) {
		this.ifaceKey = ifacekey;
	}

	@Override
	public void recvTelegramm(byte[] tele, short osi2port, short osi2partner) {
		int osi3len = (tele[0] & 0x38)>>3;
		int osi3ptr = (tele[0] & 0x07);
		int prio = (tele[0] & 0xC0)>>6;
		LOGGER.trace( "osi3-head prio " + prio + " osi3len " + osi3len + " osi3ptr " + osi3ptr );
        if (osi3len != osi3ptr) {
            int skipLen =
                    1              // 1 byte osi3len/osi3ptr
                    + osi3len * 2; // 2 bytes for routing each stage
            LOGGER.error("forwarding not supported: osi3len=" + osi3len + " != osi3ptr=" + osi3ptr );
            LOGGER.error("                 routing: " + Util.toHex( tele, 1, osi3len > 0 ? osi3len * 2 : 2 ));
            LOGGER.error("                    tele: " + Util.toHex( tele, skipLen, tele.length - skipLen));
            return;
        }
//		if (osi3len != osi3ptr) {
//			LOGGER.error("forwarding not supported: osi3len=" + osi3len + " != osi3ptr=" + osi3ptr );
//			LOGGER.error("                 routing: " + Util.toHex( tele, 1, osi3len > 0 ? osi3len * 2 : 2 ));
//			return;
//		}
		osi3len *= 2;
		// create a mirrored routing for node number lookup
		int routingLength = osi3len > 0 ? osi3len : 2;
		byte[] routing = new byte[routingLength];
		if (osi3len > 0) {
			for(int i=0; i<osi3len; ++i) {
				routing[i] = tele[osi3len-i];
			}
		} else {
			// in case of null routing create (reverse) routing from osi2 addresses
			routing[0] = (byte) osi2port;
			routing[1] = (byte) osi2partner;
		}
		Integer node = ifaceRouting.getNodeNumber(routing);
		if (node == null) {
			node = 0xFFFFFF;
		}
		try {
			ifaceSystemConnector.recvTelegram(Arrays.copyOfRange(tele, osi3len+1, tele.length), node);
		} catch (IfaceException e) {
			LOGGER.fatal("cannot pass received telegram to system connector: " + e);
		}
	}

	@Override
	public void recvCommunicationState(short osi2Port, short osi2Address, boolean state, boolean queried) {
		if (osi2Port == 0 || osi2Address == 0) {
			return;
		}
		// get node number
		byte[] routing = new byte[2];
		routing[0] = (byte) osi2Port;
		routing[1] = (byte) osi2Address;
		Integer node = ifaceRouting.getNodeNumber(routing);
		if (node == null) {
			LOGGER.fatal("recvCommunicationState: there is no node number for " + osi2Port + "/" + osi2Address);
			return;
		}
		// put state to communication state map
		commStateMap.put(node, state);
        LOGGER.info( "csm " + commStateMap + " " + node + " = " + state );
        if (true == state && 8438321 == node) {
            breakPointPossibleHere();
        }

		// send state to tcc system
		try {
			ifaceSystemConnector.recvCommState(node, state, queried);
		} catch (IfaceException e) {
			LOGGER.fatal("cannot pass received communication state to system connector: " + e);
		}
	}
	
	static private void breakPointPossibleHere() {
	}

	@Override
	public void sendTelegram(byte[] tele, int node) { // tel ~ Sammeltelegram [low(node),high(node),ehigh(node),numETel, {ETel1}, {ETel2}]
	        // get routing for telegram
		byte[] routing = ifaceRouting.getRouting(node);
		if (routing == null) {
			LOGGER.warn("cannot send telegram: there is no routing for node " + Util.nodeToString(node));
			LOGGER.warn("lost telegram: " + Util.toHex(tele, 0, tele.length));
			return;
		}
		// special test for damaged telegram
		if ( (11373307 == node) && 13 == tele.length ) {
		    // mirror routing
		    byte [] r2 = new byte[ routing.length ];
		    for ( int i = 0; i < routing.length; i++ ) {
		        int swtch = routing.length - i -1;
		        r2[swtch] = routing[i];
		    }
		    //routing = r2;
		    LOGGER.debug( "NOT mirrored routing: " +  Util.toHex(routing, 0, routing.length) );
		}
		if (!isAlive(routing)) {
			LOGGER.warn("cannot send telegram to " + Util.nodeToString(node) + " because he is dead");
			LOGGER.warn("lost telegram: " + Util.toHex(tele, 0, tele.length));
			return;
		}
		// prepend osi3 routing to STel
		byte[] osi3tele = new byte[1+routing.length+tele.length];
//		osi3tele[0] = getOsi3Pointer(routing);
        osi3tele[0] = getOsi3PointerAsKri( routing );
		System.arraycopy(routing, 0, osi3tele, 1, routing.length);
		System.arraycopy(tele, 0, osi3tele, 1+routing.length, tele.length);

		LOGGER.debug( "Fake-KRI sending: " + Util.toHex(osi3tele, 0, osi3tele.length) );
		ifaceProtocol.sendTelegram(osi3tele, Util.toUnsignedShort(routing[0]), Util.toUnsignedShort(routing[1]));
	}

	@Override
	public void startComm(Short osi2port, Short osi2partner) {
		LOGGER.info("start communication on port " + (osi2port == null ? "<all>" : osi2port) + " for partner " +
				(osi2partner == null ? "<all>" : osi2partner));
		ifaceProtocol.startCommunication(osi2port, osi2partner);
	}

	@Override
	public void stopComm(Short osi2port, Short osi2partner) {
		LOGGER.info("stop communication on port " + (osi2port == null ? "<all>" : osi2port) + " for partner " +
				(osi2partner == null ? "<all>" : osi2partner));
		ifaceProtocol.stopCommunication(osi2port, osi2partner);
	}

	@Override
	public void queryState() {
		LOGGER.info("query communication states");
        LOGGER.info("csm ???");
		for(Entry<Integer, Boolean> entry : commStateMap.entrySet()) {
			Integer node = entry.getKey();
			Boolean state = entry.getValue();
			try {
				ifaceSystemConnector.recvCommState(node, state, true);
			} catch (IfaceException e) {
				LOGGER.fatal("cannot return queried communication states: " + e);
			}
		}
	}

	@Override
	public void timeSync() {
		LOGGER.info("send time synchronization telegram");
		ifaceProtocol.timeSynchronzation();
	}

	private void initPartners() {
		Map<Short, List<Short>> partners = getPartners();
		setAllDead(partners);
		ifaceProtocol.setPartners(partners);
		ifaceProtocol.startCommunication(null, null);
	}

	private boolean isAlive(byte[] routing) {
		// we need to check the communication state for our communication partner, not for the node which may be
		// a node behind our communication partner
		byte[] route = Arrays.copyOfRange(routing, 0, 2);
		Integer node = ifaceRouting.getNodeNumber(route);
		if (node == null) {
			// should not happen
			LOGGER.error("cannot get communication partner for routing: " + Util.toHex(routing));
			return false;
		}
		Boolean state = commStateMap.get(node);
        LOGGER.info( "csm " + commStateMap + " " + node + " ? " + state );
		boolean rv = (state == null ? false : state);
		if (false == rv) {
		    int i = 0; i = i + 1;
		}
		return rv;
	}

	// Aufbau erstes Byte im OSI3-Routing
	// !|!|L|L|L|P|P|P
	// ! Priorität
	//   00b Prioritätsklasse 1
	//   10b Prioritätsklasse 2
	// L Länge
	// P Pointer

	private byte getOsi3Pointer(byte[] routing) {
        int ptr = 0x81; // i.e. Prioritätsklasse 2 / pointer 1
        // Eine Alternative wäre hier das Verschieben um 2 Bit nach links, aber nicht direkt zu verstehen
        // Die Division durch Zwei entspricht dem Schieben um ein Bit nach rechts ...
        // Die korrekte Begründung: Ein Paar(2) von Adressbytes ist eine Routing-Stufe. Also Länge / 2 => Anzahl Paare/Stufen
        // dieser Wert ist nun um 3 nach links zu schieben.
        //ptr += (routing.length << 2); // Code von RalfZ
        ptr += ( ( routing.length / 2 ) << 3 ); // Code von Ronald
        return (byte) ptr;
	}

    private byte getOsi3PointerAsKri(byte[] routing) {
        int ptr = 0x82; // i.e. Prioritätsklasse 2 / pointer 2
        int len = routing.length / 2;
        if ( routing.length == 2 ) {
            ptr = 0x81;
        }
        len <<= 3;
        ptr += len;
        return (byte) ptr;
    }

	private Map<Short, List<Short>> getPartners() {
		Map<Short, List<Short>> partners = new HashMap<>();
		Collection<RoutingEntry> routingList = ifaceRouting.getRoutingTable();
		printRoutingTable(routingList);
		for(RoutingEntry routingEntry : routingList) {
			if (routingEntry.getKey() != ifaceKey) {
				continue;
			}
			byte[] routing = routingEntry.getRouting();
			if (routing.length != 2) {
				continue;
			}
			short port = Util.toUnsignedShort(routing[0]);
			List<Short> partnersOnPort = partners.get(port);
			if (partnersOnPort == null) {
				partnersOnPort = new ArrayList<>();
				partners.put(port , partnersOnPort);
			}
			short partner = Util.toUnsignedShort(routing[1]);
			partnersOnPort.add(partner);
		}
		return partners;
	}

	private void printRoutingTable(Collection<RoutingEntry> routingList) {
		for(RoutingEntry entry : routingList) {
			String routing = "";
			for(byte b : entry.getRouting()) {
				routing += Util.toUnsignedString(b) + " ";
			}
			LOGGER.debug(entry.getKey() + " - " + entry.getNode() / 256 + "-" + entry.getNode() % 256 + ": " + routing);
		}

	}

    private void setAllDead( Map<Short, List<Short>> partners ) {
        for ( Entry<Short, List<Short>> portEntry : partners.entrySet() ) {
            Short port = portEntry.getKey();
            for ( Short partner : portEntry.getValue() ) {
                recvCommunicationState( port, partner, false, false );
            }
        }
    }

    @Override
    public SystemMessageManagement getSystemMessageManagement() {
        return null;
    }

}
