package de.heuboe.tls.iface.lib;

import de.heuboe.tls.iface.iface.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An implementation of {@link IfaceApplication}
 * Here implements common orchestration of other classes related to TLS transport interface processes
 */
@Slf4j
public class IfaceApp implements IfaceApplication {

	private static final String ALL = "<all>";
	
	private IfaceSystemConnector ifaceSystemConnector;
	private IfaceProtocol ifaceProtocol;
	private IfaceRouting ifaceRouting;
	private int ifaceKey;
	private SystemMessageManagement smm = null;
	
	private ConcurrentHashMap<Integer, Boolean> commStateMap;
	
	/**
	 * Checks setting of major components and initialises them
	 * Sets all direct partners to intially state dead
	 * Starts the communictione to direct partners
	 * @throws IfaceException thrown if a component is missing
	 */
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
    
    public void setSystemMessageManagement(SystemMessageManagement smm) {
        this.smm = smm;
    }
    
    /**
     * 
     * @return the object handling system messages (null if undefined)
     */
    @Override
    public SystemMessageManagement getSystemMessageManagement() {
        return this.smm;
    }
	
	@Override
	public void recvTelegramm(byte[] tele, short osi2port, short osi2partner) { // NOSONAR ignore complexity for now
		int osi3len = (tele[0] & 0x38)>>3;
		int osi3ptr = (tele[0] & 0x07);
        int prio = (tele[0] & 0xC0)>>6;
        log.trace( "osi3-head prio {} osi3len {} osi3ptr {}", prio, osi3len, osi3ptr );
		if (osi3len != osi3ptr) {
		    int skipLen = 
		            1              // 1 byte osi3len/osi3ptr
		            + osi3len * 2; // 2 bytes for routing each stage
			if ( log.isErrorEnabled() ) {
				log.error( "forwarding not supported: osi3len={} != osi3ptr={}", osi3len, osi3ptr );
				log.error( "                 routing: {}", Util.toHex( tele, 1, osi3len > 0 ? osi3len * 2 : 2 ) );
				log.error( "                    tele: {}", Util.toHex( tele, skipLen, tele.length - skipLen ) );
			}
			
			return;
		}
		osi3len *= 2;
		// create a mirrored routing for node number lookup
		int routingLength = osi3len > 0 ? osi3len : 2;
		byte[] routing = new byte[routingLength]; 
		if (osi3len > 0) {
			for(int i=0; i<osi3len; ++i) { // ! reverse/mirrored routing
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
			if (e.isPotentialStreamProblem()) { // we need to tell the caller that there may be a problem in the stream of input data
			    log.error("Potential stream problem. Going to disconnect. Cannot pass received telegram to system connector: ", e);
			    if (null != smm) {
			        smm.sendMessage( "Exception: Potential stream problem. Going to disconnect. Cannot pass received telegram to system connector. See process log." );
			    }
			    
			    throw new IllegalStateException( e ); // don't want to use a checked exception.
			}
			log.error("cannot pass received telegram to system connector: ", e);
			if (null != smm) {
			    smm.sendMessage( "Exception: cannot pass received telegram to system connector." );
			}
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
			log.error("recvCommunicationState: there is no node number for {}/{}", osi2Port, + osi2Address);
			if (null != smm) {
			    smm.sendMessage( "recvCommunicationState: there is no node number for " + osi2Port + "/" + osi2Address );
			}
			return;
		}
		// put state to communication state map
		commStateMap.put(node, state);
				
		// send state to tcc system
		try {
			ifaceSystemConnector.recvCommState(node, state, queried);
		} catch (IfaceException e) {
			log.error("cannot pass received communication state to system connector: ", e);
            if (null != smm) {
                smm.sendMessage( "Exception: cannot pass received communication state to system connector. see process log." );
            }
		}
	}

	@Override
	public void sendTelegram(byte[] tele, int node) {
	        // get routing for telegram
		byte[] routing = ifaceRouting.getRouting(node);
		if (routing == null) {
			if ( log.isWarnEnabled() ) {
				log.warn( "cannot send telegram: there is no routing for node {}", Util.nodeToString( node ) );
				log.warn( "lost telegram: {}", Util.toHex( tele, 0, tele.length ) );
			}
			if (null != smm) {
				String sb = "cannot send telegram: there is no routing for node " +
							Util.nodeToString( node ) +
							"\n" +
							"lost telegram: " +
							Util.toHex( tele, 0, tele.length );
			    smm.sendMessage( sb );
			}
			return;
		}
		if (!isAlive(routing)) {
			if ( log.isWarnEnabled() ) {
				log.warn( "cannot send telegram to {} because he is dead", Util.nodeToString( node ) );
				log.warn( " routing: {}", Util.toHex( routing ) );
				log.warn( " lost telegram: {}", Util.toHex( tele, 0, tele.length ) );
			}
            if (null != smm) {
				String sb = "cannot send telegram to " +
							Util.nodeToString( node ) +
							" because he is dead\n" +
							" routing: " +
							Util.toHex( routing ) +
							"\n" +
							" lost telegram: " +
							Util.toHex( tele, 0, tele.length );
                smm.sendMessage( sb );
            }
			return;
		}
		// prepend osi3 routing to STel
		byte[] osi3tele = new byte[1+routing.length+tele.length];
		osi3tele[0] = getOsi3Pointer(routing);
		System.arraycopy(routing, 0, osi3tele, 1, routing.length);
		System.arraycopy(tele, 0, osi3tele, 1+routing.length, tele.length);
		
		ifaceProtocol.sendTelegram(osi3tele, Util.toUnsignedShort(routing[0]), Util.toUnsignedShort(routing[1]));
	}

	@Override
	public void startComm(Short osi2port, Short osi2partner) {
		log.info( "start communication on port {} for partner {}", (osi2port == null ? ALL : osi2port), (
				osi2partner == null ? ALL : osi2partner) );
		ifaceProtocol.startCommunication(osi2port, osi2partner);
	}
	
	@Override
	public void stopComm( Short osi2port, Short osi2partner ) {
		log.info( "stop communication on port {} for partner {}", (osi2port == null ? ALL : osi2port), (
				osi2partner == null ? ALL : osi2partner) );
		ifaceProtocol.stopCommunication( osi2port, osi2partner );
	}

	@Override
	public void queryState() {
		log.info("query communication states");
		for(Entry<Integer, Boolean> entry : commStateMap.entrySet()) {
			Integer node = entry.getKey();
			Boolean state = entry.getValue();
			try {
				ifaceSystemConnector.recvCommState(node, state, true);
			} catch (IfaceException e) {
				log.error("cannot return queried communication states: ", e);
                if ( null != smm ) {
                    smm.sendMessage( "Exception: cannot return queried communication states. See process log." );
                }
			}
		}
	}

	@Override
	public void timeSync() {
		log.info("send time synchronization telegram");
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
			if ( log.isErrorEnabled() ) {
				log.error( "cannot get communication partner for routing: {}", Util.toHex( routing ) );
			}
            if ( null != smm ) {
                smm.sendMessage( "cannot get communication partner for routing: " + Util.toHex(routing) );
            }
			return false;
		}
		Boolean state = commStateMap.get(node);
		//noinspection SimplifiableConditionalExpression
		return state == null ? false : state; // NOSONAR sonar simplification less obvious
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
	
	private Map<Short, List<Short>> getPartners() {
		Map<Short, List<Short>> partners = new HashMap<>();
		Collection<RoutingEntry> routingList = ifaceRouting.getRoutingTable();
		printRoutingTable(routingList);
		for(RoutingEntry routingEntry : routingList) {
            if( routingEntry.getKey() == ifaceKey ) //noinspection CommentedOutCode
			{
                byte[] routing = routingEntry.getRouting();
                if( routing.length != 2 ) {
                    continue;
                }
                short port = Util.toUnsignedShort( routing[0] );
                List< Short > partnersOnPort = partners.computeIfAbsent( port, k -> new ArrayList<>() );
				// prior code, shall remain here since functional approach is new
				//List< Short > partnersOnPort = partners.get( port ); // NOSONAR keep for doc until next commit
				//if( partnersOnPort == null ) {                       // NOSONAR keep for doc until next commit
				//	partnersOnPort = new ArrayList<>();
				//	partners.put( port, partnersOnPort );
				//}
                short partner = Util.toUnsignedShort( routing[1] );
                partnersOnPort.add( partner );
            }
        }
		return partners;
	}

	private void printRoutingTable(Collection<RoutingEntry> routingList) {
		for(RoutingEntry entry : routingList) {
			String routing = "";
			for(byte b : entry.getRouting()) {
				//noinspection StringConcatenationInLoop
				routing += Util.toUnsignedString(b) + " "; // NOSONAR used seldom and with few elements
			}
			log.debug( "{}-{}-{}:{}", entry.getKey(), entry.getNode() / 256, entry.getNode() % 256, routing);
		}
		
	}

	private void setAllDead(Map<Short, List<Short>> partners) {
		for(Entry<Short, List<Short>> portEntry : partners.entrySet()) {
			Short port = portEntry.getKey();
			for(Short partner : portEntry.getValue()) {
				recvCommunicationState(port, partner, false, false);
			}
		}		
	}
	
	/**
	 * stop a communication thread by specifying its target osi7 node number
	 * @param targetNodeNumber the osi7 number of the partner device // won't work for KRUZ!, transparent KRIs
	 */
	@Override
	public void stopCommByOsi7Node( Integer targetNodeNumber ) {
		byte[] routing = ifaceRouting.getRouting( targetNodeNumber );
		if ( null == routing || routing.length == 0 ) {
			log.error( "Cannot find routing for node to stop connection to: {}", targetNodeNumber );
			return;
		}
		short port = routing[0];
		short partnerPort = routing[1];
		stopComm( port, partnerPort );
	}
	
	/**
	 * start a communication thread by specifying its target osi7 node number
	 * This will only work after the change update has been completed
	 *
	 * @param targetNodeNumber the osi7 number of the partner device // won't work for KRUZ!, transparent KRIs
	 */
	@Override
	public void startCommByOsi7Node( Integer targetNodeNumber ) {
		byte[] routing = ifaceRouting.getRouting( targetNodeNumber );
		if ( null == routing || routing.length == 0 ) {
			log.error( "Cannot find routing for node to start connection to: {}", targetNodeNumber );
			return;
		}
		short port = routing[0];
		short partnerPort = routing[1];
		startComm( port, partnerPort );
	}

}
