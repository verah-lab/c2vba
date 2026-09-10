package de.heuboe.sdbby.strategy.matching;

import java.util.List;

import de.heuboe.log.Logger;
import eu.vmis_ehe.vmis2.geomanager.GeoManagerGrpc;
import eu.vmis_ehe.vmis2.geomanager.strategy.GetStrategiesRequest;
import eu.vmis_ehe.vmis2.geomanager.strategy.Strategy;
import net.devh.boot.grpc.client.inject.GrpcClient;

/**
 * 
 * VMIS2-ConfigService client
 * 
 * @author peters
 *
 */
public class GeoManager {

	private static final Logger LOGGER = Logger.getLogger(GeoManager.class);

	@GrpcClient("GeoManager")
	private GeoManagerGrpc.GeoManagerBlockingStub stub;

    /**
     * 
     * Returns all strategies
     * 
     * @return Strategies
     */
    public List<Strategy> getStrategies() {
    	
    	LOGGER.info( "Read all strategies" );
    	
        GetStrategiesRequest.Builder request = GetStrategiesRequest.newBuilder();
        return stub.getStrategies( request.build() ).getStrategiesList();
    }
    
    
}
