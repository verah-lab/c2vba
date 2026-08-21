package de.heuboe.tls.iface.iface;

import java.util.Date;

/**
 * Implementors of this interface will be able to receive telegrams sent or recevied 
 * thus the whole traffic could be recorded to file or database
 * @author Ronald Nikel
 *
 */
public interface TelegramSentRcvdTracker {
	/**
	 * possible directions of the telegrams
	 * @author Ronald Nikel
	 *
	 */
	enum Direction{ RCVD, SENT } // NOSONAR use of this class has to be checked globally (within H/B)
	
	/**
	 * this method will be called with a telegram sent or received
	 * @param telegram the bytes of a tls Einzeltegramm
	 * @param when the time the telegram was transferred
	 * @param direction whether the telegram was received or sent
	 * @param connectionName this should uniquely identify the connection in the context given
	 */
	void telegramTransferred(byte[] telegram, Date when, Direction direction, String connectionName);
}
