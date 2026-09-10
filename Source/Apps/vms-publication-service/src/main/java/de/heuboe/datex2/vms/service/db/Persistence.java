package de.heuboe.datex2.vms.service.db;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import de.heuboe.datex2.vms.service.VMSServiceException;
import de.heuboe.datex2.vms.service.table.data.D2VMSTable;
import de.heuboe.datex2.vms.service.table.data.D2VMSUnit;
import de.heuboe.log.Logger;

/**
 * 
 * D2VMSTable persistence
 * 
 * @author peters
 *
 */
public class Persistence
{
	private static final Logger LOGGER = Logger.getLogger( Persistence.class );
	
    @Autowired
	D2VMSUnitTableVersionRepository unitRepo;
	
    @Autowired
    D2VMSTableRepository repo;                                                                            
	
    /**
     * 
     * Constructor
     * 
     * @throws VMSServiceException	Error
     */
	public Persistence()
			throws VMSServiceException
	{
		LOGGER.info( "Database connection successfully initialised." );
	}
	
	/**
	 * 
	 * Writes D2VMSTable to database
	 * 
	 * @param vmsTable					D2VMSTable	
	 * @throws VMSServiceException		Error
	 */
	public void saveVMSTable( D2VMSTable vmsTable  )
			throws VMSServiceException
	{
		vmsTable.createId();
		
		List<D2VMSUnit> units = vmsTable.getUnits();
		List<D2VMSUnitTableVersion> tvUnits = new ArrayList<>();
		for( D2VMSUnit unit : units ) {
			D2VMSUnitTableVersion tvUnit = new D2VMSUnitTableVersion( vmsTable.getD2Id(), vmsTable.getD2Version(), unit );
			tvUnit.createId();
			tvUnits.add( tvUnit );
		}
		
		vmsTable.getUnits().clear();
		
		repo.save( vmsTable );
		unitRepo.deleteByD2IdVersion( vmsTable.getD2Id(), vmsTable.getD2Version() );
		unitRepo.saveAll( tvUnits );
		
	    LOGGER.info( "Saved D2VMSTable" );
	}
	
	/**
	 * 
	 * Reads D2VMSTable from database
	 * 
	 * @param id					VMS table ID
	 * @param version				VMS table version
	 * @return						D2VMSTable	
	 * @throws VMSServiceException	Error
	 */
	public D2VMSTable readVMSTable( String id, String version )
			throws VMSServiceException
	{
        List<D2VMSTable> tables = repo.findByD2IdVersion( id, version );
        List<D2VMSUnitTableVersion> tvUnits = unitRepo.findByD2IdVersion( id, version );
        
        if( ( tables != null ) && !tables.isEmpty() ) {
        	if( tvUnits != null ) {
        		List<D2VMSUnit> units = tvUnits.stream().map( D2VMSUnitTableVersion::getUnit ).collect( Collectors.toList() );
        		D2VMSTable table = tables.get(0);
        		table.getUnits().addAll( units );
        		return table;
        	}
        }
        
		return null;
	}
	
	public void removeVMSTable( String id, String version )
			throws VMSServiceException
	{
		repo.deleteByD2IdVersion( id, version );
		unitRepo.deleteByD2IdVersion( id, version );
	}

	public void deleteVMSTable( String id, String version )
			throws VMSServiceException
	{
	}
	
	
	private D2VMSTable readVMSActiveTableHeader( String tableId )
			throws VMSServiceException
	{
        List<D2VMSTable> tables = repo.findByD2Id( tableId );
        
		for( D2VMSTable table : tables ) {
			if( table.getD2Id().equals( tableId ) && table.isActive() ) {
	        		return table;
			}
		}
        
		return null;
	}
	
	public String getNextTableVersion( String tableId )
			throws VMSServiceException
	{
		List<D2VMSTable> tables = repo.findAll() ;
		
		int maxV = 0;
		for( D2VMSTable table : tables ) {
			if( table.getD2Id().equals( tableId ) ) {
				int v = Integer.parseInt( table.getD2Version() );
				maxV = Math.max( v,  maxV ); 
			}
		}
		
		return "" + ( maxV + 1 );
	}
	
	
	
	public String getCurrentTableVersion( String tableId )
			throws VMSServiceException
	{
		D2VMSTable table = readVMSActiveTableHeader( tableId );
		return ( table == null ) ? null : table.getD2Version(); 
	}
}
