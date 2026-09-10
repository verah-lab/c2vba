package de.heuboe.datex2.vms.service.db;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import de.heuboe.datex2.vms.service.table.data.D2VMSTable;

/**
 * 
 * D2VMSTable repository
 * 
 * @author peters
 *
 */
@Repository
public interface D2VMSTableRepository extends MongoRepository<D2VMSTable, String> {

	
    /**
     * Returns a D2VMSTables matching ID.
     * 
     * @param d2Id          package name
     * @return List of D2VMSTable
     */
    @Query(value = "{ 'd2Id' : ?0 }")
    public List<D2VMSTable> findByD2Id( String d2Id );

    /**
     * Returns a single D2VMSTable matching ID and version.
     * 
     * @param d2Id          	VMS table ID
     * @param d2Version         VMS table version
     * @return List of D2VMSTable (# should be less than 1)
     */
    @Query(value = "{ 'd2Id' : ?0, 'd2Version' : ?1 }")
    public List<D2VMSTable> findByD2IdVersion( String d2Id, String d2Version );
    
    /**
     * Deletes VMS table
     * 
     * @param d2Id          	VMS table ID
     * @param d2Version         VMS table version
     * @return List of PSystemPackage
     */
    @Query(value = "{ 'd2Id' : ?0, 'd2Version' : ?1 }", delete = true)
    public D2VMSTable deleteByD2IdVersion( String d2Id, String d2Version );

    
}
