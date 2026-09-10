package de.heuboe.datex2.vms.status.pub.check;

import java.util.List;

import de.heuboe.datex2.vms.service.D2VMSUnitMessage;


/**
 * 
 * Update observer
 * 
 * @author peters
 *
 */
public interface UpdateObserver
{
	/**
	 * 
	 * Notification on DATEX-II publication
	 * 
	 * @param messages		D2VMSUnitMessages
	 * @param snapshot		true: publish complete data
	 */
	void notifyUpdate( List<D2VMSUnitMessage> messages, boolean snapshot );
}
