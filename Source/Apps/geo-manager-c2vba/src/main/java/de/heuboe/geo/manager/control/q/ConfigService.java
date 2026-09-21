package de.heuboe.geo.manager.control.q;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.heuboe.log.Logger;
import eu.vmis_ehe.vmis2.configservice.BaseVersionKey;
import eu.vmis_ehe.vmis2.configservice.CfgAq;
import eu.vmis_ehe.vmis2.configservice.CfgMq;
import eu.vmis_ehe.vmis2.configservice.CfgUdeSensor;
import eu.vmis_ehe.vmis2.configservice.ChildOpt;
import eu.vmis_ehe.vmis2.configservice.ConfigItemType;
import eu.vmis_ehe.vmis2.configservice.ConfigServiceGrpc;
import eu.vmis_ehe.vmis2.configservice.GetAllItemsRequest;
import eu.vmis_ehe.vmis2.configservice.GetItemsReply;
import eu.vmis_ehe.vmis2.configservice.TlsDevice;
import net.devh.boot.grpc.client.inject.GrpcClient;

/**
 * 
 * VMIS2-ConfigService client
 * 
 * @author peters
 *
 */
public class ConfigService {

	private static final Logger LOGGER = Logger.getLogger(ConfigService.class);

	@GrpcClient("ConfigService")
	private ConfigServiceGrpc.ConfigServiceBlockingStub stub;

	
    private void addUzVersions( Map<String,String> uzId2Version, GetAllItemsRequest.Builder request ) {
        if( ( uzId2Version != null ) && !uzId2Version.isEmpty() ) {
            
            List<BaseVersionKey> bvks = new ArrayList<>();
            for( Map.Entry<String,String> entry : uzId2Version.entrySet() ) {
                bvks.add( BaseVersionKey.newBuilder().setUzId( entry.getKey() ).setBaseVersionId( entry.getValue() ).build() );
            }
            request.addAllVersions( bvks );
        }
    }

    /**
     * 
     * Returns all MQs for UZ configuration versions
     * 
     * @param uzId2Version UZ configuration versions
     * @return MQs
     */
    public List<CfgMq> getAllMQs( Map<String,String> uzId2Version ) {
    	
    	LOGGER.info( "Read all MQs" );
    	
        GetAllItemsRequest.Builder request = GetAllItemsRequest.newBuilder();
        request.setType( ConfigItemType.MQ );

        addUzVersions( uzId2Version, request );

        request.addAllChildOpts( Arrays.asList( ChildOpt.newBuilder().build() ) );
        GetItemsReply mqs = stub.getAllItems( request.build() );
        return mqs.getMqs().getMqsList();
    }
    
    /**
     * 
     * Returns all MQs for UZ configuration versions
     * 
     * @param uzId2Version UZ configuration versions
     * @return MQs
     */
    public List<CfgAq> getAllAQs( Map<String,String> uzId2Version ) {
    	
    	LOGGER.info( "Read all Aqs" );
    	
        GetAllItemsRequest.Builder request = GetAllItemsRequest.newBuilder();
        request.setType( ConfigItemType.AQ );

        addUzVersions( uzId2Version, request );

        request.addAllChildOpts( Arrays.asList( ChildOpt.newBuilder().build() ) );
        GetItemsReply aqs = stub.getAllItems( request.build() );
        return aqs.getAqs().getAqsList();
    }
    
	
    /**
     * 
     * Returns all UDEs for UZ configuration versions
     * 
     * @param uzId2Version UZ configuration versions
     * @return UDEs
     */
    public List<CfgUdeSensor> getAllUFDs( Map<String,String> uzId2Version ) {
    	
    	LOGGER.info( "Read all UFDs" );
    	
        GetAllItemsRequest.Builder request = GetAllItemsRequest.newBuilder();
        request.setType( ConfigItemType.UDE_SENSOR );
        
        addUzVersions( uzId2Version, request );
        
        GetItemsReply udes = stub.getAllItems( request.build() );
        return udes.getUdeSensors().getSensorsList();
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
        
        addUzVersions( uzId2Version, request );
        
        GetItemsReply udes = stub.getAllItems( request.build() );
        return udes.getDevices().getDevicesList();
    }

    
}
