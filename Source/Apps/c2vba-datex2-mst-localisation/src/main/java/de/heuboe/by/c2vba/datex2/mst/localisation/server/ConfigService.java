package de.heuboe.by.c2vba.datex2.mst.localisation.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.heuboe.log.Logger;
import eu.datex2.schema._2._2_0.LaneEnum;
import eu.vmis_ehe.vmis2.configservice.CfgMq;
import eu.vmis_ehe.vmis2.configservice.CfgUdeSensor;
import eu.vmis_ehe.vmis2.configservice.CfgUdeSensor.UdeType;
import eu.vmis_ehe.vmis2.configservice.CfgVdeSensor;
import eu.vmis_ehe.vmis2.configservice.ChildOpt;
import eu.vmis_ehe.vmis2.configservice.ConfigItemType;
import eu.vmis_ehe.vmis2.configservice.ConfigServiceGrpc;
import eu.vmis_ehe.vmis2.configservice.GetAllItemsRequest;
import eu.vmis_ehe.vmis2.configservice.GetItemsReply;
import eu.vmis_ehe.vmis2.configservice.GetItemsRequest;
import eu.vmis_ehe.vmis2.configservice.LanePos;
import eu.vmis_ehe.vmis2.configservice.LanePos.LaneType;
import eu.vmis_ehe.vmis2.configservice.TlsDevice;
import eu.vmis_ehe.vmis2.configservice.TlsEa;
import eu.vmis_ehe.vmis2.configservice.TlsFg;
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

	/**
	 *
	 * Lane item data
	 *
	 */
	public class LaneData {
		private String id;
		private String name;
		private LaneEnum lane;
		private Integer de;

		/**
		 * 
		 * Constructor
		 * 
		 * @param id       Lane ID
		 * @param name     Lane name
		 * @param lanePos  Lane position
		 * @param de  	   DE-Nummer
		 */
		public LaneData(String id, String name, LanePos lanePos, Integer de ) {
			this.id = id;
			this.name = name;
			this.de = de;
			
			if( lanePos.getLaneType() == LaneType.EMERGENCY ) {
				lane = LaneEnum.HARD_SHOULDER;
			} else {
				switch ( lanePos.getHIndex() ) {
				case 0:
					lane = LaneEnum.LANE_1;
					break;
				case 1:
					lane = LaneEnum.LANE_2;
					break;
				case 2:
					lane = LaneEnum.LANE_3;
					break;
				case 3:
					lane = LaneEnum.LANE_4;
					break;
				case 4:
					lane = LaneEnum.LANE_5;
					break;
				default:
					LOGGER.warn("ConfigService: Unknown LanePos.HIndex <" + lanePos.getHIndex() + ">, assume HFS");
					lane = LaneEnum.LANE_1;
					break;
				}
			}
		}

		public String getId() {
			return id;
		}

		public LaneEnum getLane() {
			return lane;
		}

		public String getName() {
			return name;
		}

		public Integer getDe() {
			return de;
		}
	}

	/**
	 * 
	 * Gets 
	 * 
	 * @param mqIds		MQ IDs
	 * @return			Lane data
	 */
	public Map<String, List<LaneData>> getMqLaneData(List<String> mqIds ) {

		Map<String,Integer> des = getDes();
		
		Map<String, List<LaneData>>  result = new HashMap<>();
		
		GetItemsRequest.Builder request = GetItemsRequest.newBuilder();
		request.setType(ConfigItemType.MQ);
		request.addAllIds( mqIds );
		request.addAllChildOpts(Arrays.asList(ChildOpt.newBuilder().build()));
		GetItemsReply mqs = stub.getItems(request.build());
		
		for( CfgMq mq : mqs.getMqs().getMqsList() ) {
			List<LaneData> lds = new ArrayList<>();
			for( CfgVdeSensor vs : mq.getVdeSensors().getSensorsList() ) {
				Integer de = des.get( vs.getId() );
				LaneData ld = new LaneData( vs.getId(), vs.getName(), vs.getLanePos(), de );
				lds.add( ld );
			}
			result.put( mq.getId(), lds );
		}

		return result;
	}
	
	
    /**
     * 
     * Returns Des for FG1 sensors
     * 
     * @return Des
     */
    private Map<String,Integer> getDes() {
    	LOGGER.info( "Read all SST devices" );
    	
        GetAllItemsRequest.Builder request = GetAllItemsRequest.newBuilder();
        request.setType( ConfigItemType.DEVICE );
        request.setItemType( "RST" );
        
        GetItemsReply items = stub.getAllItems( request.build() );
        List<TlsDevice> tlsDevices = items.getDevices().getDevicesList();
        
        Map<String,Integer> des = new HashMap<>();
        
		for( TlsDevice tlsDevice : tlsDevices ) {
			for( TlsFg fg : tlsDevice.getFgsList() ) {
				if( fg.getNumber() == 1 ) {
					for( TlsEa ea : fg.getEasList() ) {
						des.put( ea.getEaid(), ea.getDeNummer() );
					}
				}
			}
		}

		return des;
    }


	/**
	 * 
	 * Retrieves IDs of UFD sensors of type 'type'
	 * 
	 * @param type	UFD type
	 * @return		UFD IDs
	 */
	public Set<String> getUfdIds( int type ) {

		GetAllItemsRequest.Builder request = GetAllItemsRequest.newBuilder();
		request.setType(ConfigItemType.UDE_SENSOR );
		request.addAllChildOpts(Arrays.asList(ChildOpt.newBuilder().build()));
		GetItemsReply ufds = stub.getAllItems(request.build());
		
		Set<String> ids = new HashSet<>();
		for( CfgUdeSensor ude : ufds.getUdeSensors().getSensorsList() ) {
			if( ( ude.getType() == UdeType.UNRECOGNIZED ) ) {
				LOGGER.debug( "Unrecognized UDE type: " + ude.getId() );
			} else if( ( ude.getType().getNumber() == type ) ) {
				ids.add( ude.getId() );
			}
		}
		
		return ids;
	}
	
	/**
	 * 
	 * Returns ufd type name
	 * 
	 * @param ufdType	UFD type
	 * @return			Type name
	 */
	public String getTypeName( int ufdType ) {
		UdeType ut = UdeType.forNumber( ufdType );
		if( ut != null ) {
			return ut.name();
		}
		
		return "???";
	}
}
