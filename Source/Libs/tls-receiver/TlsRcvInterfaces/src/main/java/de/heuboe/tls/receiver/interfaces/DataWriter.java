package de.heuboe.tls.receiver.interfaces;

import de.heuboe.tls.receiver.interfaces.DataObjectIf;

public interface DataWriter {
        /*
         * called when a tls etel begins
         */
	public void beginEtel();
	
	/*
	 * called when all deblocks of a tls etel are handled
	 */
	public void endEtel();
	
	/*
	 * write the accumulated data
	 */
	public void write(DataObjectIf obj);
}
