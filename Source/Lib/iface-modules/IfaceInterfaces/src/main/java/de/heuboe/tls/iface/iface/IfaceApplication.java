package de.heuboe.tls.iface.iface;

/**
 * This Interface defines the methods that the iface application implements
 * for use by the iface protocol, the iface system connector or the iface configuration
 * 
 * @author ralfz
 *
 */
public interface IfaceApplication {

	/**
	 * receive a telegramm from the iface protocol and pass it to the 
	 * iface system connector.
	 * 
	 * @param tele the telegram
	 * @param osi2port osi2 port address of the sender
	 * @param osi2partner osi2 address of the sender
	 */
	void recvTelegramm(byte[] tele, short osi2port, short osi2partner);
	
	/**
	 * pass a telegram received from the iface system connector to the iface protocol.
	 *   
	 * @param tele the telegram // tele ~ Sammeltelegram [low(node),high(node),ehigh(node),numETel, {ETel1}, {ETel2}]
	 * @param node the node number of receiver / real target
	 */
	void sendTelegram(byte[] tele, int node);
	
	/**
	 * start the communication for the given port/partner
	 * @param osi2port may be null if all ports shall be started
	 * @param osi2partner may be null if all partners for a port shall be started
	 */
	void startComm(Short osi2port, Short osi2partner);
	
	/**
	 * stop the communication for the given port/partner
	 * @param osi2port may be null if all ports shall be stopped
	 * @param osi2partner may be null if all partners for a port shall be stopped
	 */
	void stopComm(Short osi2port, Short osi2partner);
	
	/**
	 * query communication state (i.e. dead or alive) for all communication partners.
	 */
	void queryState();
	
	/**
	 * send a time synchronization telegram.
	 */
	void timeSync();

	/**
	 * receive a communication state
	 * @param osi2Port the osi2 port
	 * @param osi2Address the osi2 address of the partner
	 * @param state the communication state
	 * @param queried true if state was queried
	 */
	void recvCommunicationState(short osi2Port, short osi2Address,
			boolean state, boolean queried);

	/**
	 * Getter for the iface key
	 * @return ifaceKey
	 */
	int getIfaceKey();
	
	/**
	 * Getter for message management
	 * @return object for message management, or null if undefined
	 */
	SystemMessageManagement getSystemMessageManagement();
	
	/**
	 * Similar to stopComm with osi2 ports, but with osi7 number here
	 * @param targetNodeNumber the osi7 number of the partner device // won't work for KRUZ!, transparent KRIs
	 */
	void stopCommByOsi7Node( Integer targetNodeNumber );
	
	
	/**
	 * Similar to startComm with osi2 ports, but with osi7 number here
	 * @param targetNodeNumber the osi7 number of the partner device // won't work for KRUZ!, transparent KRIs
	 */
	void startCommByOsi7Node( Integer targetNodeNumber );
}
