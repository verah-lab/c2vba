package de.heuboe.tls.receiver.interfaces;

import java.util.Collection;

/**
 * @author ronald
 *
 */
public interface AddressConverter {

	/**
	 * @param node Node number the de belongs to
	 * @param de Number of the DE (sensor or actor) TLS resource
	 * @param fg Function group (Funktionsgruppe) the TLS resource delivers data for
	 * @return The string identifier of a TLS resource
	 */
	public String convert(int node, int fg, int de);
	
	
	/**
	 * @param realAddress The node number of the direct communication partner
	 * @return List of all node numbers that can be reached by the node with node number realAddress
	 */
	public Collection<Integer/*node numbers of children*/> descendants( int realAddress );
}
