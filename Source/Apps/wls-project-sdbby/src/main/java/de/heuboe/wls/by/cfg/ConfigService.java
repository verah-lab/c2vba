package de.heuboe.wls.by.cfg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.heuboe.log.Logger;
import eu.vmis_ehe.vmis2.configservice.BaseVersionKey;
import eu.vmis_ehe.vmis2.configservice.CfgAq;
import eu.vmis_ehe.vmis2.configservice.CfgKri;
import eu.vmis_ehe.vmis2.configservice.CfgMq;
import eu.vmis_ehe.vmis2.configservice.CfgRouteStation;
import eu.vmis_ehe.vmis2.configservice.CfgUdeSensor;
import eu.vmis_ehe.vmis2.configservice.CfgUz;
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
	
	// Kri names for VDESensors, Wzgs an UDEs
	private Map<String,String> knNr2Kri = new HashMap<>();

	
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
    

    private List<CfgUz> getAllUzs( Map<String,String> uzId2Version ) {
    	LOGGER.info( "Read all UZ" );
    	
        GetAllItemsRequest.Builder request = GetAllItemsRequest.newBuilder();
        request.setType( ConfigItemType.UZ );
        
        addUzVersions( uzId2Version, request );
        request.addAllChildOpts( Arrays.asList( ChildOpt.newBuilder().build() ) );
        
        GetItemsReply reply = stub.getAllItems( request.build() );
        return reply.getUzs().getUzsList();

    }
    
    /**
     * 
     *  Fill knNr2Kri
     * 
     */
    public void init() {     // NOSONAR
    	
		List<TlsDevice> tlsDevices = getAllDevices( new HashMap<>() );
		Map<String,String> rst2KnNr = new HashMap<>();
		
		for( TlsDevice tlsDevice : tlsDevices ) {
			rst2KnNr.put( tlsDevice.getId(), ""  + tlsDevice.getOsi7Address() );
		}
    	
		Map< String, Set<String> > knNr2Kris = new HashMap<>();
		Set< String > nonEmptyKnNr = new HashSet<>();
		
    	List<CfgUz> uzs = getAllUzs( null );
		for( CfgUz uz : uzs ) {
			for( CfgKri cfgKri : uz.getKris().getKrisList() ) {
				
				for( CfgRouteStation cfgSst : cfgKri.getRouteStations().getRouteStationsList() ) {
					String knNr = rst2KnNr.get( cfgSst.getId() );
					if( knNr != null ) {
						
						knNr2Kris.computeIfAbsent( "" + knNr, s -> new HashSet<>() ).add( cfgKri.getName() );
						boolean sstIsEmpty = cfgSst.getUdeSensors().getSensorsList().isEmpty() &&
						                     cfgSst.getVdeSensors().getSensorsList().isEmpty() &&
						                     cfgSst.getWzgs().getWzgsList().isEmpty();
						
						if( !nonEmptyKnNr.contains( knNr ) || !sstIsEmpty ) {
							
							// Some (3) SSts have duplicate Knotennummer.
							// This problem can be solved by ignoring SSts without are any sensor
							knNr2Kri.put( knNr, cfgKri.getName() );
						}
					
						if( !sstIsEmpty ) {
							nonEmptyKnNr.add( knNr );
						}
					}
				}
			}
		}
		
		if( !knNr2Kris.entrySet().isEmpty() ) {
			LOGGER.warn( "" );
			LOGGER.warn( "KnNr in multiple KRIs:" );
			for( Map.Entry< String, Set<String> > e : knNr2Kris.entrySet() ) {
				if( e.getValue().size() > 1 ) {
					LOGGER.warn( e.getKey() + ": " + e.getValue().stream().collect( Collectors.joining( ", ") ) );
				}
			}
			LOGGER.warn( "" );
		}
    }

    public  Map<String,String>  getKriNames() {
    	return knNr2Kri;
    }
}
