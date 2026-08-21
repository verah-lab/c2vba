package de.heuboe.tls.iface.iface;

/**
 * In order to be used in an {@link IfaceApplication} an IfaceSystemConnector is required
 * An implementation defines how received telegrams or status messages are to be handled in the
 * surrounding (programming) context
 */
public interface IfaceSystemConnector {

	/**
	 * Setter for the iface application.
	 * @param ifaceApplication the iface application
	 * @throws IfaceException if something bad happens 
	 */
	void setIfaceApplication(IfaceApplication ifaceApplication) throws IfaceException;
	
	/**
	 * pass a received telegram to the iface system connector to put it into the tcc system.
	 *   
	 * @param tele the telegram
	 * @param node the node number of receiver
	 * @throws IfaceException if something bad happens 
	 */
	void recvTelegram(byte[] tele, int node) throws IfaceException;
	
	/**
	 * receive a communication state.
	 * @param node the node number of the communication partner
	 * @param alive true if state is alive
	 * @param queried true if state was queried
	 * @throws IfaceException if something bad happens
	 */
	void recvCommState(int node, boolean alive, boolean queried) throws IfaceException;

}
