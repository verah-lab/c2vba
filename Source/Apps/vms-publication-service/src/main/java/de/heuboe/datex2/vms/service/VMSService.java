package de.heuboe.datex2.vms.service;

import java.util.List;
import java.util.Set;

import javax.jws.WebMethod;
import javax.jws.WebService;

import de.heuboe.datex2.vms.service.table.data.D2VMSTable;
import de.heuboe.datex2.vms.service.table.data.D2VMSUnit;


/**
 * 
 * VMS service
 * 
 * @author peters
 *
 */
@WebService
public interface VMSService 
{
	/**
	 * 
	 * Clients shall periodically call this method
	 * 
	 * @param clientId		Client ID
	 * @return				(will always return true))
	 */
	@WebMethod
	boolean ping( String clientId );
	 
	/**
	 * 
	 * Builds D2VMSUnits from infrastructure/configuration
	 * 
	 * @param filter				Configuration
	 * @return						D2VMSUnits
	 * @throws VMSServiceException	Error
	 */
	@WebMethod
	List<D2VMSUnit> getD2VMSUnits( VMSTableConfiguration configuration )
			throws VMSServiceException;	
	
	@WebMethod
	String getCurrentTableVersion( String tableId )
			throws VMSServiceException;
	
	@WebMethod
	String getNextTableVersion( String tableId )
			throws VMSServiceException;
	
	/**
	 * 
	 * Writes D2VMSTable to database
	 * 
	 * @param vmsTable				D2VMSTable
	 * @throws VMSServiceException	Error
	 */
	@WebMethod
	void saveVMSTable( D2VMSTable vmsTable  )
			throws VMSServiceException;
	
	@WebMethod
	D2VMSTable readVMSTable( String id, String version )
			throws VMSServiceException;
	
	@WebMethod
	List<D2VMSMessage> getVMSMessages( Set<ObjectKey> objKeys )
			throws VMSServiceException;
	
	@WebMethod
	String registerDataListener( String tableId, 
								 String tableVersion, 
								 VMSDataListener.Identity identity )
		throws VMSServiceException;
}
