package de.heuboe.tls.receiver.interfaces;

import de.heuboe.tls.receiver.impl.DataObject;

public interface DataWriter {

	public void beginEtel();
	public void endEtel();
	public void write(DataObject obj);
}
