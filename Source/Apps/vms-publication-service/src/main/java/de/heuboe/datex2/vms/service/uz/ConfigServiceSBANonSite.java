package de.heuboe.datex2.vms.service.uz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.heuboe.log.Logger;
import eu.vmis_ehe.vmis2.configservice.CfgAq;
import eu.vmis_ehe.vmis2.configservice.CfgAq.AqType;
import eu.vmis_ehe.vmis2.configservice.ChildOpt;
import eu.vmis_ehe.vmis2.configservice.ConfigItemType;
import eu.vmis_ehe.vmis2.configservice.ConfigServiceGrpc;
import eu.vmis_ehe.vmis2.configservice.GetAllItemsRequest;
import eu.vmis_ehe.vmis2.configservice.GetItemsReply;
import net.devh.boot.grpc.client.inject.GrpcClient;

/**
 * Class that encapsulates calls to configuration service.
 * 
 */
public class ConfigServiceSBANonSite {
	
	private static final Logger LOGGER = Logger.getLogger( ConfigServiceSBANonSite.class );

    @GrpcClient("ConfigService")
    private ConfigServiceGrpc.ConfigServiceBlockingStub stub;
    
    private Map<String,CfgAq> aqs = new HashMap<>();

    private List<AqType> sbaAqTypes;
    
    public void init() {
        GetAllItemsRequest.Builder request = GetAllItemsRequest.newBuilder();
        request.setType( ConfigItemType.AQ );
        request.addAllChildOpts( Arrays.asList( ChildOpt.newBuilder().build() ) );
        GetItemsReply reply = stub.getAllItems( request.build() );
        
        for( CfgAq aq : reply.getAqs().getAqsList() ) {
        	if( ( aq.getType() != null ) && sbaAqTypes.contains( aq.getType() ) ) {
        		aqs.put( aq.getId(), aq );
        	}
        }
        
        LOGGER.info( "Retrieved " + aqs.size() + " AQs of type(s) " + 
                     sbaAqTypes.stream().map( t -> t.name() ).collect( Collectors.joining( ", " ) ) + " from ConfigService" );
    }

    public ConfigServiceSBANonSite( List<AqType> sbaAqTypes )  {
    	this.sbaAqTypes = sbaAqTypes;
    }
    
    /**
     * 
     * Returns all AQs with the exception of SBA AQs 
     * 
     * @param siteAqIds 	Excluded AQs from sites (Anlagen) 
     * @return AQs			All AQs
     */
    public List<CfgAq> getSbaAqs( Set<String> siteAqIds ) {
    	return aqs.values().stream().filter( a -> !siteAqIds.contains( a.getId() ) ).collect( Collectors.toList() ); 
    }
    
    /**
     * 
     * Returns all AQs for UZ configuration versions
     * 
     * @param siteAqIds 	Excluded AQs from sites (Anlagen) 
     * @return AQs
     */
    public List<CfgAq> getAqs( List<String> aqIds ) {
    	
    	List<CfgAq> result = new ArrayList<>();
    	for( String id : aqIds ) {
    		CfgAq aq = aqs.get( id );
    		if( aq != null ) {
    			result.add( aq );
    		}
    	}
    	
    	return result;
    }


}
