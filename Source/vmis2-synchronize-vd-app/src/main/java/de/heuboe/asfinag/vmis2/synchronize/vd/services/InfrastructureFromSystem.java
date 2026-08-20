package de.heuboe.asfinag.vmis2.synchronize.vd.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.protobuf.Empty;
import com.google.protobuf.MessageOrBuilder;

import de.heuboe.asfinag.control.base.services.DebugWriter;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureManager;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureObject;
import de.heuboe.asfinag.vmis2.infrastructure.types.Lane;
import de.heuboe.asfinag.vmis2.infrastructure.types.ReferenceTypes;
import de.heuboe.asfinag.vmis2.infrastructure.types.Road;
import de.heuboe.asfinag.vmis2.infrastructure.types.RouteStation;
import de.heuboe.asfinag.vmis2.synchronize.vd.config.SynchronizeVdProperties;
import eu.vmis_ehe.vmis2.configservice.CfgRouteStation;
import eu.vmis_ehe.vmis2.configservice.CfgRouteStations;
import eu.vmis_ehe.vmis2.configservice.CfgVdeSensor;
import eu.vmis_ehe.vmis2.configservice.CfgVdeSensors;
import eu.vmis_ehe.vmis2.configservice.ConfigServiceGrpc;
import eu.vmis_ehe.vmis2.configservice.GetItemsReply;
import eu.vmis_ehe.vmis2.configservice.ServiceVersion;
import eu.vmis_ehe.vmis2.configservice.pojo.PChildOpt;
import eu.vmis_ehe.vmis2.configservice.pojo.PConfigItemType;
import eu.vmis_ehe.vmis2.configservice.pojo.PGetAllItemsRequest;
import io.vavr.Tuple;
import lombok.extern.slf4j.Slf4j;

/**
 * Create the needed Infrastructure using the GeoManager and the ConfigService
 */
@Slf4j
public class InfrastructureFromSystem implements InfrastructureManager {
    
    private Marker marker = MarkerFactory.getMarker("InfrastrureManager");

    private ConfigServiceGrpc.ConfigServiceBlockingStub configServiceStub;
    private String system;
    private SynchronizeVdProperties properties;

    private List<InfrastructureObject> laneInfraObjects = new ArrayList<>();
    private List<InfrastructureObject> rstInfraObjects = new ArrayList<>();
    private List<InfrastructureObject> roadInfraObjects = new ArrayList<>();
    
    // key: InfrastructureType
    private Map<String, List<InfrastructureObject>> infrastructure = new HashMap<>();
    private int counterNoRoadId;
    private int counterRoadId;
 

    /**
     * Constructor
     * 
     * @param configServiceStub     stub to access the ConfigService
     * @param properties            the application properties, SynchronizeVdProperties
     */
    public InfrastructureFromSystem(ConfigServiceGrpc.ConfigServiceBlockingStub configServiceStub,
            SynchronizeVdProperties properties) {
        this.configServiceStub = configServiceStub;
        this.system = properties.getCentreId();
        if (this.system.length() != this.system.trim().length()) {
            log.warn(marker,"Property centreId = '{}' contains spaces!!! Is that on purpose?",
                    properties.getCentreId());
        }
        this.properties = properties;
    }

    /**
     * Initialize infrastructure from services
     */
    @PostConstruct // Run after Autowired has been injected
    public void init() {
        // reset known values:
        counterNoRoadId = 0;
        counterRoadId = 0;
        laneInfraObjects.clear();
        rstInfraObjects.clear();
        roadInfraObjects.clear();
        infrastructure.clear();
        
        ServiceVersion serviceVersion = configServiceStub.getServiceVersion(Empty.getDefaultInstance());
        if (system.equals(properties.getCentreIdAllUZ())) {
           	log.info(marker, "Working with config service version: {} and for all configured UZs",
                    serviceVersion.toString(), system);     	
        } else {
        	log.info(marker, "Working with config service version: {} and UZ/rVMZ system {}",
                serviceVersion.toString(), system);
        }
        
        // Create child VDE_SENSORs
        PChildOpt childOpt = PChildOpt.builder()
                .childTypeList(Arrays.asList(PConfigItemType.VDE_SENSOR.name())).build();
        GetItemsReply rsts;
        
        // Get all RouteStations(RST) with child VDE_SENSORs for all UZs that the configService
        // knows => without uzId filter
        if (system.equals(properties.getCentreIdAllUZ())) {
        	rsts = configServiceStub.getAllItems(PGetAllItemsRequest.to(PGetAllItemsRequest.builder()
                    .type(PConfigItemType.RST).childOptsList(Arrays.asList(childOpt)).build()));
        } else {
            // Get all RouteStations(RST) with child VDE_SENSORs only for one UZ (filter with uzId)
        	rsts = configServiceStub.getAllItems(PGetAllItemsRequest.to(PGetAllItemsRequest.builder()
                    .type(PConfigItemType.RST).uzId(system).childOptsList(Arrays.asList(childOpt)).build()));
        }
        CfgRouteStations cfgRSTs = rsts.getRouteStations();
        Map<CfgRouteStation, List<CfgVdeSensor>> rst2Lanes;
        
        if(cfgRSTs == null || cfgRSTs.getRouteStationsCount() == 0) {
            log.warn("Got NO RouteStations from configService for system {}!", system);
            rst2Lanes = new HashMap<>();
        } else {
            log.info(marker, "Received {} RouteStations for system {}.", cfgRSTs.getRouteStationsCount(), system);
            rst2Lanes = cfgRSTs.getRouteStationsList().stream()
                    .filter(r -> !r.getVdeSensors().getSensorsList().isEmpty())
                    .collect(Collectors.toMap(Function.identity(), r -> r.getVdeSensors().getSensorsList()));
        }
        
        // Do Mappings
        Map<String,String> laneIds2RstId = rst2Lanes.entrySet().stream().flatMap(e -> e.getValue() 
                .stream().map(vdeSensor -> Tuple.of(vdeSensor.getId(), e.getKey().getId())))
                .collect(Collectors.toMap(t2 -> t2._1, t2 -> t2._2));
        // Create infrastructure objects of route stations with referenced lanes
        rst2Lanes.entrySet().forEach(entry -> 
        rstInfraObjects.add(createRouteStation(entry.getKey().getId(), entry.getValue(), serviceVersion.toString())));
        
        // Getting the VDE_SENSORs for all UZs that the configService knows => without uzId filter
        GetItemsReply vdeSensors;
        if (system.equals(properties.getCentreIdAllUZ())) {
            vdeSensors = configServiceStub.getAllItems(PGetAllItemsRequest.to(PGetAllItemsRequest.builder()
                    .type(PConfigItemType.VDE_SENSOR).build()));
        } else {
            // Getting the VDE_SENSORs only for one UZ (filter with uzId)
            vdeSensors = configServiceStub.getAllItems(PGetAllItemsRequest.to(PGetAllItemsRequest.builder()
                    .type(PConfigItemType.VDE_SENSOR).uzId(system).build()));
        }
        CfgVdeSensors cfgVdeSensors = vdeSensors.getVdeSensors();
        if(cfgVdeSensors == null || cfgVdeSensors.getSensorsList() == null || cfgVdeSensors.getSensorsList().isEmpty()) {
            log.warn(marker, "No VDE_SENSORS found for system {}!", system);
            throw new IllegalStateException("InfrastructureFromSystem found No VDE_SENSORS for " + system);
        }
        List<CfgVdeSensor> sensorsList = cfgVdeSensors.getSensorsList();
        log.info(marker, "Received {} VDE_SENSORs for system {}.", sensorsList.size(), system);
        
        // Do mappings
        Map<CfgVdeSensor, String> lanesToRoad = new HashMap<>();
        sensorsList.forEach(vde -> {
             String roadId = getRoadIdOfVde(vde);
             if(roadId != null) {
                 lanesToRoad.put(vde, roadId);
             }
        });
        
        Map<String, List<CfgVdeSensor>> roadToLanes = new HashMap<>();
        lanesToRoad.forEach((vde, roadId) -> {
            List<CfgVdeSensor> vdes = roadToLanes.getOrDefault(roadId, new ArrayList<>());
            vdes.add(vde);
            roadToLanes.put(roadId, vdes);
        });
        
        // Create infrastructure object of lanes with referenced road and vice versa
        laneInfraObjects.addAll(sensorsList.stream()
                .map(v -> createLane(v, laneIds2RstId.get(v.getId()), lanesToRoad.get(v), null))
                .collect(Collectors.toList()));
        roadToLanes.forEach((roadId, vdes) -> roadInfraObjects.add(createRoad(roadId, vdes, null)));
        log.info(marker, "Received {} roads", roadInfraObjects.size());

        // Put objects to infrastructure
        infrastructure.put(ReferenceTypes.LANE, laneInfraObjects);
        infrastructure.put(ReferenceTypes.ROAD, roadInfraObjects);
        infrastructure.put(ReferenceTypes.ROUTE_STATION, rstInfraObjects);
        
        // Do logging
        log.info("Got {} VDE_SENSORs with roadId, {} without roadId!", counterRoadId, counterNoRoadId);
        if(properties.isWriteFileLog()) {
            writeFileLog("cfgRSTVDE", rsts);
            writeFileLog("cfgVde", vdeSensors);
        }
    }
    
    private String getRoadIdOfVde(CfgVdeSensor vde) {
        if (vde.getLocation() != null && vde.getLocation().getRoadId() != null 
                && !vde.getLocation().getRoadId().isEmpty() 
                && !"null".equals(vde.getLocation().getRoadId().toLowerCase())) {
            log.info(marker, "Got a VDE_SENSOR with id {} for road {} !",
                    vde.getId(), vde.getLocation().getRoadId());
            counterRoadId++;
            return vde.getLocation().getRoadId();
        } else if(properties.getDefaultRoadId() != null && !properties.getDefaultRoadId().isEmpty()) {
            log.info(marker, "No roadId for VDE_SENSOR with id {} found! Take default road id {}",
                    vde.getId(), properties.getDefaultRoadId());
            counterNoRoadId++;
            return properties.getDefaultRoadId();
        } else {
            log.info(marker, "No roadId for VDE_SENSOR with id {} found! No default road id configured.", vde.getId());
            counterNoRoadId++;
            return null;
        }
    }

    private InfrastructureObject createLane(CfgVdeSensor v, String routeStationId, String roadId, String version) {
        Map<String, List<InfrastructureObject>> references = new HashMap<>();
        log.info(marker, "Create InfrastructureObject for lane {} with road {} and route station {}",
                v.getId(), roadId, routeStationId);
        InfrastructureObject refRoad =
                new Road(roadId, roadId, "", version, null, Collections.emptyMap(), Collections.emptyMap());
        InfrastructureObject refRst = new RouteStation(routeStationId, routeStationId, "", version, null,
                Collections.emptyMap(), Collections.emptyMap());

        references.computeIfAbsent(ReferenceTypes.ROAD, infa -> new ArrayList<>()).add(refRoad);
        references.computeIfAbsent(ReferenceTypes.ROUTE_STATION, infa -> new ArrayList<>()).add(refRst);
        return new Lane(v.getId(), v.getName(), "", version, null, references, Collections.emptyMap());
    }

    private InfrastructureObject createRoad(String roadId, List<CfgVdeSensor> vdes, String version) {
        Map<String, List<InfrastructureObject>> references = new HashMap<>();
        List<InfrastructureObject> vdeSList = vdes.stream().map(v -> new Lane(v.getId(), v.getName(), "", version, null,
                Collections.emptyMap(), Collections.emptyMap())).collect(Collectors.toList());
        references.computeIfAbsent(ReferenceTypes.LANE, infa -> new ArrayList<>()).addAll(vdeSList);
        return new Road(roadId, roadId, "", version, null, references, Collections.emptyMap());
    }

    private InfrastructureObject createRouteStation(String routeStationId, List<CfgVdeSensor> vdes, String version) {
        Map<String, List<InfrastructureObject>> references = new HashMap<>();
        List<InfrastructureObject> vdeSList = vdes.stream().map(v -> new Lane(v.getId(), v.getName(), "", version, null,
                Collections.emptyMap(), Collections.emptyMap())).collect(Collectors.toList());
        references.computeIfAbsent(ReferenceTypes.LANE, infa -> new ArrayList<>()).addAll(vdeSList);
        return new RouteStation(routeStationId, routeStationId, "", version, null, references, Collections.emptyMap());
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see de.heuboe.vmis2.infrastructure.base.InfrastructureManager#getTypes()
     */
    @Override
    public List<String> getTypes() {
        return !infrastructure.keySet().isEmpty()? new LinkedList<>(infrastructure.keySet()) : Collections.emptyList();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.heuboe.vmis2.infrastructure.base.InfrastructureManager#getInfrastructureObjectsOfType(java.
     * lang.String)
     */
    @Override
    public List<InfrastructureObject> getInfrastructureObjectsOfType(String type) {
        return infrastructure.containsKey(type)? infrastructure.get(type) : Collections.emptyList();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.heuboe.vmis2.infrastructure.base.InfrastructureManager#getInfrastructureObjects(java.util.
     * List)
     */
    @Override
    public List<InfrastructureObject> getInfrastructureObjects(List<String> ids) {
        List<InfrastructureObject> flatInfraList =
                infrastructure.values().stream().flatMap(List::stream).collect(Collectors.toList());
        return flatInfraList.stream().filter(infra -> ids.contains(infra.getId())).collect(Collectors.toList());
    }
    
    private void writeFileLog(String typeName, MessageOrBuilder msg) {
        File outDir = new File(properties.getFileLogPath());
        File outDirMS = new File(outDir, "cfg");
        outDirMS.mkdirs();
        File outFile = new File(outDirMS, typeName + "-" + System.currentTimeMillis() + DebugWriter.JSON_FILE);
        DebugWriter.proto2file(outFile, msg);
    }
    
}
