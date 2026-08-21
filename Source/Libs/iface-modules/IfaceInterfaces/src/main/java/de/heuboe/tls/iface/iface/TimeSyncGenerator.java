package de.heuboe.tls.iface.iface;

/**
 * when the TimeSyncMode USERDELIVERED is selected an object satisfying the following interface has to be supplied to setTimeSyncGenerator
 * @author Ronald Nikel
 *
 */
public interface TimeSyncGenerator {
	/**
	 * 
	 * @return One Einzeltelegramm as byte block. Telegram with time sync contents. I.e. beginning with length of ETel followed by FG an so on.
	 */
	byte[] makeTimeSyncTele();
}
