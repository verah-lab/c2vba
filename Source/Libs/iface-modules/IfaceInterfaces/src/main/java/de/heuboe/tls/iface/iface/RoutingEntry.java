package de.heuboe.tls.iface.iface;

import java.util.Arrays;

/**
 * An entry for a routing table.
 * @author ralfz
 *
 */
public class RoutingEntry {
	private final int node; // the osi7 node number
	private final byte[] routing; // the routing in pairs of osi2 ports
	private final int key;
	
	
	/**
	 * Constructor for all members
	 * @param node the osi7 node number of a device
	 * @param routing the routing to this device in pairs of osi2 ports
	 * @param key a key for this object
	 */
	public RoutingEntry(int node, byte[] routing, int key) {
		super();
		this.node = node;
		this.routing = routing;
		this.key = key;
	}

	
	public int getNode() {
		return node;
	}


	public byte[] getRouting() {
		return Arrays.copyOf(routing, routing.length);
	}


	public int getKey() {
		return key;
	}
	
	
	/**
	 * Compare the routing of two objects
	 * @param other the other routing
	 * @return true if routings ar the same
	 */
	public boolean equalsRouting(byte[] other) {
		if (routing.length != other.length) {
			return false;
		}
		for(int i=0; i< routing.length; ++i) {
			if (routing[i] != other[i]) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * compare the node number of two objects
	 * @param other the other object
	 * @return true if the node numbers are identical
	 */
	public boolean equalsNode(int other) {
		return node == other;
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder( node / 256 + "-" + node % 256 + " ->" );
        for( int b : routing ) {
            if( b < 0 ) {
                b += 256;
            }
            s.append( " " ).append( b );
        }
		return s.toString();
	}
}
