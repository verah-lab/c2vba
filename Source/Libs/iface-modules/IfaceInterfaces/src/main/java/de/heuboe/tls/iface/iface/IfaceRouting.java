package de.heuboe.tls.iface.iface;

import java.util.Collection;

/**
 * This interface defines methods that a routing configuration has to provide.
 * 
 * @author ralfz
 *
 */
public interface IfaceRouting {

	/**
	 * convert an osi3 routing to an osi7 node number.
	 * 
	 * @param routing the routing
	 * @return node number or null if not found
	 */
	public Integer getNodeNumber(byte[] routing);
	
	/**
	 * convert an osi7 node number to an osi3 routing.
	 * @param node the node number
	 * @return routing or null if not found
	 */
	public byte[] getRouting(int node);
	
	/**
	 * get all routing entries.
	 * @return
	 */
	public Collection<RoutingEntry> getRoutingTable();
}
