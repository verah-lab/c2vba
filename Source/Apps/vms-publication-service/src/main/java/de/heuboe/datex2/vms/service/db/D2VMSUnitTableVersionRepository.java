package de.heuboe.datex2.vms.service.db;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * 
 * D2VMSTable repository
 * 
 * @author peters
 *
 */
@Repository
public interface D2VMSUnitTableVersionRepository extends MongoRepository<D2VMSUnitTableVersion, String> {

    /**
     * Returns D2VMSUnit matching table ID and table version.
     * 
     * @param d2Id          	VMS table ID
     * @param d2Version         VMS table version
     * @return List of D2VMSUnitTableVersion 
     */
    @Query(value = "{ 'tableD2Id' : ?0, 'tableD2Version' : ?1 }")
    public List<D2VMSUnitTableVersion> findByD2IdVersion( String d2Id, String d2Version );
    
    /**
     * Deletes D2VMSUnits of VMS table
     * 
     * @param d2Id          	VMS table ID
     * @param d2Version         VMS table version
     * @return List of PSystemPackage
     */
    @Query(value = "{ 'tableD2Id' : ?0, 'tableD2Version' : ?1 }", delete = true)
    public void deleteByD2IdVersion( String d2Id, String d2Version );
}
