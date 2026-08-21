package de.heuboe.tls.iface.iface;

import java.util.List;
import java.util.Map;

/**
 * This interface defines the methods that a tls protocol such as tls-over-ip
 * has to implement.
 * 
 * @author ralfz
 *
 */
public interface IfaceProtocol {

	/**
	 * Send a TLS telegram to a tls node 
	 * @param tele the telegram including tls osi-3 layer
	 * @param osi2port the tls osi-2 number of the port (aka Inselbus)
	 * @param osi2partner the tls osi-2 number of the connected device
	 */
	public void sendTelegram(byte[] tele, short osi2port, short osi2partner);
	
	/**
	 * Start the communication for the given port and partner
	 * @param osi2Port the tls osi-2 number of the port, if null start all ports
	 * @param osi2Partner the tls osi-2 number of the connected device, if null start all devices
	 */
	public void startCommunication(Short osi2Port, Short osi2Partner);
	
	/**
	 * Stop the communication for the given port and partner
	 * @param osi2Port the tls osi-2 number of the port, if null stop all ports
	 * @param osi2Partner the tls osi-2 number of the connected device, if null stop all devices
	 */
	public void stopCommunication(Short osi2Port, Short osi2Partner);
	
	/**
	 * Setter for the iface application.
	 * @param ifaceApplication the iface application
	 */
	public void setIfaceApplication(IfaceApplication ifaceApplication);
	
	/**
	 * Set the configured communication partners.
	 * @param partners a map with an entry for each port and a list of all partners for each port
	 */
	public void setPartners(Map<Short, List<Short>> partners);
	
	/**
	 * send a time sysnchronization to all connected communication partners
	 */
	public void timeSynchronzation();

    /**
     * Getter for the iface application.
     * @return ifaceApplication the iface application
     */
    IfaceApplication getIfaceApplication();
}
