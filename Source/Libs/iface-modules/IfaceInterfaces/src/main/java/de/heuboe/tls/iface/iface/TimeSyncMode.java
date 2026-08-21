package de.heuboe.tls.iface.iface;

/**
 * choices to set the mode for time syncs
 * @author Ronald Nikel
 *
 */
public enum TimeSyncMode {
	WALLTIME,	// sync with wall time, this shall be the default
	UTC,		// sync with UTC
	USERDELIVERED // user has to sypply an object implementing TimeSyncGenerator
}
