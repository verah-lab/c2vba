package de.heuboe.sdbby.strategy.matching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.heuboe.log.Logger;
import eu.vmis_ehe.vmis2.configservice.CfgAq;
import eu.vmis_ehe.vmis2.configservice.CfgKri;
import eu.vmis_ehe.vmis2.configservice.CfgRouteStation;
import eu.vmis_ehe.vmis2.configservice.CfgUz;
import eu.vmis_ehe.vmis2.configservice.ChildOpt;
import eu.vmis_ehe.vmis2.configservice.ConfigItemType;
import eu.vmis_ehe.vmis2.configservice.ConfigServiceGrpc;
import eu.vmis_ehe.vmis2.configservice.GetAllItemsRequest;
import eu.vmis_ehe.vmis2.configservice.GetItemsReply;
import eu.vmis_ehe.vmis2.configservice.TlsDevice;
import eu.vmis_ehe.vmis2.configservice.TlsEa;
import eu.vmis_ehe.vmis2.configservice.TlsFg;
import net.devh.boot.grpc.client.inject.GrpcClient;

/**
 * Class that encapsulates calls to configuration service.
 * 
 */
public class ConfigService {

	private static final Logger LOGGER = Logger.getLogger(ConfigService.class);
	
	@GrpcClient("ConfigService")
    private ConfigServiceGrpc.ConfigServiceBlockingStub stub;
    
    private Map<String,CfgAq> aqs = new HashMap<>();
    
    private Map<String,String> wzgId2De = new HashMap<>();
    private Map<String,Integer> wzgId2KnNr = new HashMap<>();  
    private Map<Integer,String> knNr2KriId = new HashMap<>();  

    
    /**
     * 
     * Init
     * 
     */
    public void init() {
    	
    	LOGGER.info( "Read configuration ..." );
    	
        GetAllItemsRequest.Builder request = GetAllItemsRequest.newBuilder();
        request.setType( ConfigItemType.AQ );
        request.addAllChildOpts( Arrays.asList( ChildOpt.newBuilder().build() ) );
        GetItemsReply reply = stub.getAllItems( request.build() );
        
        int countWzg = 0;
        for( CfgAq aq : reply.getAqs().getAqsList() ) {
        	countWzg += aq.getWzgs().getWzgsCount();
        	aqs.put( aq.getId(), aq );
        }
        
        LOGGER.info( "Number of AQs: " + reply.getAqs().getAqsCount() );
        LOGGER.info( "Number of Wzgs: " + countWzg );
        
        
        // knNr2KriId
        
		List<TlsDevice> tlsDevices = getAllDevices( new HashMap<>() );
		Map<String,Integer> rst2KnNr = new HashMap<>();
		
		for( TlsDevice tlsDevice : tlsDevices ) {
			rst2KnNr.put( tlsDevice.getId(), tlsDevice.getOsi7Address() );
		}
    	
    	List<CfgUz> uzs = getAllUzs( null );
		for( CfgUz uz : uzs ) {
			for( CfgKri cfgKri : uz.getKris().getKrisList() ) {
				
				for( CfgRouteStation cfgSst : cfgKri.getRouteStations().getRouteStationsList() ) {
					Integer knNr = rst2KnNr.get( cfgSst.getId() );
					if( knNr != null ) {
						knNr2KriId.put( knNr, cfgKri.getName() );
					}
				}
			}
		}
		
		
		// wzgId2De, wzgId2KnNr
		
		for( TlsDevice tlsDevice : tlsDevices ) {
			for( TlsFg fg : tlsDevice.getFgsList() ) {
				if( fg.getNumber() == 4 ) {
					for( TlsEa ea : fg.getEasList() ) {
						wzgId2De.put( ea.getEaid(), "" + ea.getDeNummer() ); 
						wzgId2KnNr.put( ea.getEaid(), tlsDevice.getOsi7Address() );
					}
				}
			}
		}

    }


    public Map<String,String> getWzgId2De() {
    	return wzgId2De;
    }
    
    public Map<String,Integer> getWzgId2KnNr() {
    	return wzgId2KnNr;
    }

    public Map<Integer,String> getKnNr2KriId() {
    	return knNr2KriId;
    }

    
    /**
     * 
     * Returns all AQs for UZ configuration versions
     * 
     * @param ids 	AQ IDs
     * @return AQs
     */
    public List<CfgAq> getAQs( List<String> ids ) {
    	
    	if( ( ids == null ) || ids.isEmpty() ) {
    		return aqs.values().stream().collect( Collectors.toList() ); 
    	} else {
        	List<CfgAq> result = new ArrayList<>();
	    	for( String id : ids ) {
	    		CfgAq aq = aqs.get( id );
	    		if( aq != null ) {
	    			result.add( aq );
	    		}
	    	}
	    	return result;
    	}
    }

    /**
     * 
     * Returns all UDEs for UZ configuration versions
     * 
     * @param uzId2Version UZ configuration versions
     * @return UDEs
     */
    public List<TlsDevice> getAllDevices( Map<String,String> uzId2Version ) {
    	LOGGER.info( "Read all SST devices" );
    	
        GetAllItemsRequest.Builder request = GetAllItemsRequest.newBuilder();
        request.setType( ConfigItemType.DEVICE );
        request.setItemType( "RST" );
        
        GetItemsReply udes = stub.getAllItems( request.build() );
        return udes.getDevices().getDevicesList();
    }
    

    private List<CfgUz> getAllUzs( Map<String,String> uzId2Version ) {
    	LOGGER.info( "Read all UZ" );
    	
        GetAllItemsRequest.Builder request = GetAllItemsRequest.newBuilder();
        request.setType( ConfigItemType.UZ );
        
        request.addAllChildOpts( Arrays.asList( ChildOpt.newBuilder().build() ) );
        
        GetItemsReply reply = stub.getAllItems( request.build() );
        return reply.getUzs().getUzsList();

    }

    
}
