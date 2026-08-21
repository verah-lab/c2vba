package de.heuboe.tls.receiver.interfaces;

import java.util.List;

import de.heuboe.tls.tlstele.TlsTele;

public interface TeleReceiver {

	public List<TlsTele> receive();
	
	/**
	 * in case a receiver has an (infinite) loop we want to break for a shutdown for instance
	 */
	public void stopReceive();
}
