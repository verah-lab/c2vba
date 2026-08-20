package de.heuboe.asfinag.vmis2.synchronize.vd.services;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.support.MessageBuilder;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureManager;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureObject;
import de.heuboe.asfinag.vmis2.infrastructure.types.ReferenceTypes;
import de.heuboe.asfinag.vmis2.synchronize.vd.config.SynchronizeVdProperties;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.AbstractData;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.InfraParameter;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.InfraState;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.SingleVehicleData;
import de.heuboe.asfinag.vmis2.synchronize.vd.data.ShortTermData;
import de.heuboe.asfinag.vmis2.synchronize.vd.data.TlsInputData;
import de.heuboe.asfinag.vmis2.synchronize.vd.publish.SyncVdPublisher;
import de.heuboe.idgenerator.generator.IDGenerator;
import eu.vmis_ehe.vmis2.receiving.processing.data.pojo.PShortTermCollectedDataLane;
import eu.vmis_ehe.vmis2.receiving.processing.data.pojo.PShortTermCollectedDataLane.PShortTermCollectedDataLaneBuilder;
import eu.vmis_ehe.vmis2.receiving.processing.data.pojo.PShortTermCollectedDataLanes;
import eu.vmis_ehe.vmis2.receiving.processing.data.pojo.PShortTermCollectedTrafficCategoriesLane;
import eu.vmis_ehe.vmis2.receiving.processing.data.pojo.PShortTermCollectedTrafficCategoriesLane.PShortTermCollectedTrafficCategoriesLaneBuilder;
import eu.vmis_ehe.vmis2.receiving.processing.data.pojo.PShortTermCollectedTrafficCategoriesLanes;
import eu.vmis_ehe.vmis2.receiving.processing.data.pojo.PTlsDataVersion;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion0;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion1;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion2;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion3;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion4;
import eu.vmis_ehe.vmis2.tls.send.pojo.PSteuerSequenz;
import eu.vmis_ehe.vmis2.tls.send.pojo.PSteuerSequenzList;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import lombok.extern.slf4j.Slf4j;

/**
 * The publisher receives collected and synchronized data and passes it on (broker, database etc.)
 * 
 * @author Marion Keune
 *
 */

@Slf4j
public class TlsSynVdPublisher implements SyncVdPublisher<AbstractData> {
    private static final int MIN_VERSION = 0;
    private static final int MAX_VERSION = 4;
    private static final String COUNTER_NAME = "synchronize.publish.data.records";
    private static final String DISCARDED_COUNTER_NAME = "synchronize.publish.discarded.data.records";
    private static final String DATA_TYPE_TAG = "type";
    private static final String INTERVAL_TAG = "interval";
    private static final String RST_TAG = "routeStation";
    private static final String ROAD_TAG = "road";
    private static final String ACCURATE_DATA_TYPE = "accurate short term collected data";
    private static final String DEFAULT_DATA_TYPE = "missing short term collected data";
    private static final String DISCARDED_DATA_TYPE = "discarded short term data";
    private static final String NUM_SENSOR_TAG = "numberSensors";


    private Map<String, String> lane2road = new HashMap<>();
    private Map<String, String> lane2rst = new HashMap<>();
    private Map<String, Integer> road2NumLanes = new HashMap<>();
    Map<String, InfraParameter> infraParameter = new HashMap<>();
    Map<String, InfraState> infraStates = new HashMap<>();
    private String oneRouteStationId = ""; 
    
    private Marker logMarker = MarkerFactory.getMarker("TlsSyncVdPublisher");
 
    @Autowired
    private Clock clock;
    @Autowired
    InfrastructureManager infra;
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    private IDGenerator idGenerator;
    @Autowired
    private AlgoContext algoContext;
    @Autowired
    private SynchronizeVdProperties props;
    @Autowired
    private MeterRegistry meterRegistry;

    private void init(Map<String, InfraParameter> infraParameter, Map<String, InfraState> infraStates, boolean initInfra) {
        this.infraParameter = infraParameter;
        this.infraStates = infraStates;
        if (initInfra) {
            initInfrastructure();
        }
    }
    
    @Override
    public void initInfrastructure() {
        if(infra != null) {
            this.lane2road = infra.getInfrastructureObjectsOfType(ReferenceTypes.LANE).stream()
                    .filter(l -> !l.getReferences(ReferenceTypes.ROAD).isEmpty() &&
                            l.getReferences(ReferenceTypes.ROAD).get(0).getId() != null)
                    .collect(Collectors.toMap(InfrastructureObject::getId,
                            l -> l.getReferences(ReferenceTypes.ROAD).get(0).getId()));
            if (!infra.getInfrastructureObjectsOfType(ReferenceTypes.ROUTE_STATION).isEmpty()) {
                oneRouteStationId = infra.getInfrastructureObjectsOfType(ReferenceTypes.ROUTE_STATION).get(0).getId();
            }
            this.lane2rst = infra.getInfrastructureObjectsOfType(ReferenceTypes.LANE).stream()
                    .filter(l -> !l.getReferences(ReferenceTypes.ROUTE_STATION).isEmpty() &&
                            l.getReferences(ReferenceTypes.ROUTE_STATION).get(0).getId() != null)
                    .collect(Collectors.toMap(InfrastructureObject::getId,
                            l -> l.getReferences(ReferenceTypes.ROUTE_STATION).get(0).getId()));
            this.road2NumLanes = infra.getInfrastructureObjectsOfType(ReferenceTypes.ROAD).stream()
                    .filter(l -> !l.getReferences(ReferenceTypes.LANE).isEmpty())
                    .collect(Collectors.toMap(InfrastructureObject::getId,
                            l -> l.getReferences(ReferenceTypes.LANE).size()));
        }       
    }
    
    @Override
    public void publish(List<AbstractData> data, List<Tuple3<String, Instant, Integer>> missingIds,
            Map<String, InfraParameter> infraParameter, Map<String, InfraState> infraStates,
            Map<String, SingleVehicleData> infraId2SlowestVehData) {
        log.info("Publish synchronized data: Received TLS input data for {} lanes and {} lanes are missing",
                data.size(), missingIds.size());
        init(infraParameter, infraStates, false);
                             
        Map<Tuple2<Integer, Instant>, List<String>> missingIdsSorted = new HashMap<>();
        
        missingIds.forEach(tup3 -> missingIdsSorted.computeIfAbsent(
                new Tuple2<Integer, Instant>(tup3._3, tup3._2), l -> new ArrayList<>()).add(tup3._1));
        missingIdsSorted.entrySet().stream().forEach(e ->
                log.debug("For Interval end {} and interval length {}, missing ids: {}{} ", 
                e.getKey()._2, e.getKey()._1, System.lineSeparator(), 
                Arrays.toString(e.getValue().toArray()).replaceAll("(.{160})", "$1" + System.lineSeparator())));

        // Create and fill "normal" datasets and sort them by roads
        ShortTermData shortTermData = sortDataByRoads(data, infraId2SlowestVehData, false);

        Map<String, List<PShortTermCollectedDataLane>> syncCollectedRoadData = shortTermData.getRoad2ShortTermData();
        Map<String, List<PShortTermCollectedTrafficCategoriesLane>> road2ShortTermTrafficCategories = 
                shortTermData.getRoad2ShortTermTrafficCategories();
        log.info("{} normal datasets filled", syncCollectedRoadData.size());
         
        // Add datasets for missing ids
        ShortTermData shortTermMissingData = createMissingDatasets(missingIds);
        shortTermMissingData.getRoad2ShortTermData().forEach((k, v) -> syncCollectedRoadData
                .computeIfAbsent(k, l -> new LinkedList<PShortTermCollectedDataLane>())
                .addAll(v));
        shortTermMissingData.getRoad2ShortTermTrafficCategories().forEach((k, v) -> road2ShortTermTrafficCategories
                .computeIfAbsent(k, l -> new LinkedList<PShortTermCollectedTrafficCategoriesLane>())
                .addAll(v));
        log.info("{} normal and missing datasets filled", syncCollectedRoadData.size());
       
        // Publish all datasets for version 0-4
        syncCollectedRoadData.entrySet().forEach(entry -> {
            
            if (meterRegistry != null) {    
                entry.getValue().forEach(v -> {
                    String rst = lane2rst.containsKey(v.getId())? lane2rst.get(v.getId()):"";
                    String road = lane2road.containsKey(v.getId())? lane2road.get(v.getId()):"";
                    if (!v.isNoMeasuredData()) {
                        setCounter(ACCURATE_DATA_TYPE, v.getIntervalLength(), rst, road, 1);
                    } else {
                        setCounter(DEFAULT_DATA_TYPE, v.getIntervalLength(), rst, road, 1);
                    }                  
                });
            }
            
            PShortTermCollectedDataLanes collectedLanes =
                    PShortTermCollectedDataLanes.builder().dataList(entry.getValue()).iid(idGenerator.newID()).build();
            
            String msgKey = entry.getKey();  // roadId
            publishAsyncToKafka(algoContext.getTopicCollectedDataLane(), collectedLanes, msgKey,
                    "ShortTermCollectedDataLanes");
         });
        
        // Publish additional datasets for version 4
        if (!road2ShortTermTrafficCategories.isEmpty()) {
            road2ShortTermTrafficCategories.entrySet().forEach(entry -> {
                PShortTermCollectedTrafficCategoriesLanes collectedLanes =
                        PShortTermCollectedTrafficCategoriesLanes.builder()
                        .dataList(entry.getValue()).iid(idGenerator.newID()).build();
                
                String msgKey = entry.getKey();  //roadId
                publishAsyncToKafka(algoContext.getTopicCollectedTrafficCategoriesLane(), collectedLanes, msgKey,
                        "ShortTermCollectedTrafficCategoriesLanes");
            });
        }
    }

    @Override
    public void publishDiscardedData(List<AbstractData> data, Map<String, InfraParameter> infraParameter,
            Map<String, InfraState> infraStates) {
        if(!props.isWriteDiscarded()) {
            return;
        }
        init(infraParameter, infraStates, false);
        log.debug(logMarker, "Publish discarded data: Received TLS input discarded data for {} lanes", data.size());
        Map<String, List<PShortTermCollectedDataLane>> syncDiscardedRoadData 
                                                = sortDataByRoads(data, new HashMap<>(), true).getRoad2ShortTermData();
        syncDiscardedRoadData.entrySet().forEach(entry -> {
            if (meterRegistry != null) {    
                entry.getValue().forEach(v -> {
                    String rst = lane2rst.containsKey(v.getId())? lane2rst.get(v.getId()):"";
                    String road = lane2road.containsKey(v.getId())? lane2road.get(v.getId()):"";
                    setDiscardedCounter(DISCARDED_DATA_TYPE, v.getIntervalLength(), rst, road, 1);
                });
            }           
            PShortTermCollectedDataLanes collectedLanes =
                    PShortTermCollectedDataLanes.builder().dataList(entry.getValue()).iid(idGenerator.newID()).build();
            
            String msgKey = entry.getValue().get(0).getId();  // laneId
            publishAsyncToKafka(algoContext.getTopicDiscardedDataLane(), collectedLanes, msgKey,
                    "ShortTermCollectedDataLanes");
         });
    }

    @Override
    public void publishTimeSynchronization() {
        log.debug("Publish time synchronization: A time synchronization has been triggered.");
        if (!oneRouteStationId.isEmpty()) {
            List<PSteuerSequenz> seqList = new ArrayList<>();
            seqList.add(PSteuerSequenz.builder()
                    .id(oneRouteStationId)
                    .action(props.getActionNrRequestGlobalTimeSync())
                    .build());
            PSteuerSequenzList pList = PSteuerSequenzList.builder().elementsList(seqList).iid(idGenerator.newID()).build();
            
            String systemId = props.getCentreId();
            // Working for all(centreId=-ALL-) UZs that the configService knows
            if (props.getCentreId().equals(props.getCentreIdAllUZ())) {
                systemId = props.getSystemWideShortcut();
            }
            publishSyncToKafka(algoContext.getTopicControlSequence(), pList, systemId);
        } else {
            log.warn(logMarker, "No route station configured => Publish global time synchronization is not possible!");
        }
    }

    private ShortTermData sortDataByRoads(List<AbstractData> data,
            Map<String, SingleVehicleData> infraId2SlowestVehData, boolean initValues2SlowVehData) { // NOSONAR complexity
                                                                                                     // required
        Map<String, List<PShortTermCollectedDataLane>> road2ShortTermData = new HashMap<>();
        Map<String, List<PShortTermCollectedTrafficCategoriesLane>> road2ShortTermTrafficCategories = new HashMap<>();
        if (data != null && !data.isEmpty()) {
            data.forEach(d -> {
                if (d instanceof TlsInputData) {
                    boolean logicalPassivated = false;
                    PTlsDataVersion version = PTlsDataVersion.UNRECOGNIZED;
                    boolean vArithmetically = true;

                    TlsInputData tData = (TlsInputData) d;
                    if (infraStates.containsKey(tData.getId())) {
                        logicalPassivated = infraStates.get(tData.getId()).isLogicalPassivated();
                    }
                    if (tData.getVersion().isPresent()) {
                        version = PTlsDataVersion.forNumber(tData.getVersion().get());
                    }
                    if (infraParameter.containsKey(tData.getId())) {
                        vArithmetically = infraParameter.get(tData.getId()).getVArithmetical();
                    }
                    if (version.getNumber() >= MIN_VERSION && version.getNumber() <= MAX_VERSION) {
                         if (this.lane2road.containsKey(tData.getId())) {
                             road2ShortTermData
                                    .computeIfAbsent(this.lane2road.get(tData.getId()),
                                            l -> new LinkedList<PShortTermCollectedDataLane>())
                                    .add(createDataset(tData, version, vArithmetically, logicalPassivated,
                                            initValues2SlowVehData, infraId2SlowestVehData).build());
                            if (version.equals(PTlsDataVersion.VERSION_4)) {
                                road2ShortTermTrafficCategories
                                    .computeIfAbsent(this.lane2road.get(tData.getId()),
                                        l -> new LinkedList<PShortTermCollectedTrafficCategoriesLane>())
                                    .add(createTrafficCategoriesDataset(tData).build());  
                             }
                        } else {
                            log.warn("No road configured for lane {}", tData.getId());
                        }
                    } else {
                        // TODO: Processing of data version 5 and 6
                    }
                }
            });
        }
        return new ShortTermData(road2ShortTermData, road2ShortTermTrafficCategories);
    }

    private PShortTermCollectedDataLaneBuilder createDataset(
            TlsInputData tData,
            PTlsDataVersion version,
            boolean vArithmetically,
            boolean logicalPassivated,
            boolean initValues2SlowVehData,
            Map<String, SingleVehicleData> infraId2SlowestVehData) {
        Instant processingTime = Instant.now(clock);
        PShortTermCollectedDataLaneBuilder pDataBuilder = PShortTermCollectedDataLane.builder();
        boolean slowestVehFound = false;
         
        // Set values of slowest vehicle data and single vehicle data available?
        if (!initValues2SlowVehData && infraId2SlowestVehData.containsKey(tData.getId())) {
            SingleVehicleData svData = infraId2SlowestVehData.get(tData.getId());
            pDataBuilder.tlsTimeSlow(svData.getPassageTime())
                        .vehicleCategorySlow(svData.getVehicleCategory())
                        .vFZSlow(svData.getVFZ())
                        .vFZSlowQuality(props.getMaxQualitySVDataInput()); 
            slowestVehFound = true;
        }
               
        // Set general fields
        pDataBuilder.id(tData.getId()).intervalLength(tData.getIntervalLength()).eventTime(tData.getEventTime())
                .processingTime(processingTime).noMeasuredData(false).passivated(logicalPassivated)
                .vArithmetically(vArithmetically);
        
        int defErrVal = props.getDefaultErrorValue();
        float defErrValFloat = props.getDefaultErrorValueFloat();
        switch (version) {
            case VERSION_0:
                PLVEErgebnisVersion0 tV0Data = (PLVEErgebnisVersion0) tData.getInputData();
                pDataBuilder.version(version)
                        .qKFZ(tV0Data.getQKfz())
                        .qLKW(tV0Data.getQLkwAe())
                        .vPKW(tV0Data.getVPkwAe())
                        .vLKW(tV0Data.getVLkwAe())
                        .tNetto(defErrValFloat)
                        .b(defErrVal)
                        .s(defErrVal)
                        .vKFZSmoothed(defErrVal);
                break;
            case VERSION_1:
                PLVEErgebnisVersion1 tV1Data = (PLVEErgebnisVersion1) tData.getInputData();
                pDataBuilder.version(version)
                        .qKFZ(tV1Data.getQKfz())
                        .qLKW(tV1Data.getQLkwAe())
                        .vPKW(tV1Data.getVPkwAe())
                        .vLKW(tV1Data.getVLkwAe())
                        .tNetto((float) tV1Data.getNettozeitluecke())
                        .b(defErrVal)
                        .s(defErrVal)
                        .vKFZSmoothed(defErrVal);
                break;
            case VERSION_2:
                PLVEErgebnisVersion2 tV2Data = (PLVEErgebnisVersion2) tData.getInputData();
                pDataBuilder.version(version)
                        .qKFZ(tV2Data.getQKfz())
                        .qLKW(tV2Data.getQLkwAe())
                        .vPKW(tV2Data.getVPkwAe())
                        .vLKW(tV2Data.getVLkwAe())
                        .b(tV2Data.getBelegung())
                        .s(defErrVal)
                        .vKFZSmoothed(defErrVal);
                break;
            case VERSION_3:
                PLVEErgebnisVersion3 tV3Data = (PLVEErgebnisVersion3) tData.getInputData();
                pDataBuilder.version(version)
                        .qKFZ(tV3Data.getQKfz())
                        .qLKW(tV3Data.getQLkwAe())
                        .vPKW(tV3Data.getVPkwAe())
                        .vLKW(tV3Data.getVLkwAe())
                        .tNetto((float) tV3Data.getNettozeitluecke())
                        .b(tV3Data.getBelegung())
                        .s(tV3Data.getSKfz())
                        .vKFZSmoothed(tV3Data.getVKfz());
                break;
            case VERSION_4:
                PLVEErgebnisVersion4 tV4Data = (PLVEErgebnisVersion4) tData.getInputData();
                pDataBuilder.version(version)
                        .qKFZ(tV4Data.getQKfz())
                        .qLKW(tV4Data.getQLkwAe())
                        .vPKW(tV4Data.getVPkwAe())
                        .vLKW(tV4Data.getVLkwAe())
                        .tNetto((float) tV4Data.getNettozeitluecke())
                        .b(tV4Data.getBelegung())
                        .s(tV4Data.getSKfz())
                        .vKFZSmoothed(tV4Data.getVKfz());
                
                
                // Setting the values for the slowest vehicle and no individual vehicle data available?
                if (!initValues2SlowVehData && !slowestVehFound) {
                    if (infraParameter.containsKey(tV4Data.getId())) {
                        InfraParameter infraPara = infraParameter.get(tV4Data.getId());
 
                        List<Integer> catBoundariesPkw = infraPara.getCategoryBoundariesPkw();
                        List<Integer> catBoundariesLkw = infraPara.getCategoryBoundariesLkw();
                        int vSlowestPkw = props.getDefaultErrorValue();
                        int vSlowestLkw = props.getDefaultErrorValue();
                        if (catBoundariesPkw.size()+1 != tV4Data.getVKlassenPkwAeList().size()) {
                            log.debug(logMarker,
                                    "{}: The number of PkwAe speed classes in the parameters {} does not match the number {} in the input data(version 4).",
                                    tV4Data.getId(), (catBoundariesPkw.size()+1), tV4Data.getVKlassenPkwAeList().size());
                        } else {
                            // Search slowest PKW
                            for (int indexPkw = 0; indexPkw < catBoundariesPkw.size()+1; indexPkw++) {
                                if (tV4Data.getVKlassenPkwAeList().get(indexPkw) != 0 && vSlowestPkw == props.getDefaultErrorValue()) {
                                    vSlowestPkw = 1;
                                    if (indexPkw > 0) {
                                        vSlowestPkw += catBoundariesPkw.get(indexPkw-1);
                                    } 
                                    break;
                                }
                            }
                        }
                        if (catBoundariesLkw.size()+1 != tV4Data.getVKlassenLkwAeList().size()) {
                            log.debug(logMarker,
                                    "{}: The number of LkwAe speed classes in the parameters {} does not match the number {} in the input data(version 4).",
                                    tV4Data.getId(), (catBoundariesLkw.size()+1), tV4Data.getVKlassenLkwAeList().size());
                        } else {
                            // Search slowest LKW
                            for (int indexLkw = 0; indexLkw < catBoundariesLkw.size()+1; indexLkw++) {
                                if (tV4Data.getVKlassenLkwAeList().get(indexLkw) != 0 && vSlowestLkw == props.getDefaultErrorValue()) {
                                    vSlowestLkw = 1;
                                    if (indexLkw > 0) {
                                        vSlowestLkw += catBoundariesLkw.get(indexLkw-1);
                                    }  
                                    break;
                                }
                            } 
                        }
                        if(vSlowestPkw != props.getDefaultErrorValue()
                                && (vSlowestLkw == props.getDefaultErrorValue() || vSlowestPkw < vSlowestLkw)) {
                            slowestVehFound = true;
                            pDataBuilder.vFZSlow(vSlowestPkw);
                            pDataBuilder.vehicleCategorySlow(props.getCategoryPkw());
                        } else if(vSlowestLkw != props.getDefaultErrorValue()
                                && (vSlowestPkw == props.getDefaultErrorValue() || vSlowestLkw < vSlowestPkw)) {
                            slowestVehFound = true;
                            pDataBuilder.vFZSlow(vSlowestLkw);
                            pDataBuilder.vehicleCategorySlow(props.getCategoryLkw());
                        }
                    }
                    if (slowestVehFound) {
                        pDataBuilder
                            .tlsTimeSlow(tData.getEventTime().plusSeconds(tData.getIntervalLength()))
                            .vFZSlowQuality(props.getMaxQualityTCDataInput()); 
                    }
                }
                break;
            default:
                break;
        }
        if (!slowestVehFound) {
            // Set values of slowest vehicle to initial values
            pDataBuilder
                .vehicleCategorySlow(props.getDefaultErrorValue())
                .vFZSlow(props.getDefaultErrorValue())
                .vFZSlowQuality(props.getMinQualityInputSlowV());
            log.debug(logMarker, "{}: Slowest vehicle not determinable", tData.getId());
        }
        return pDataBuilder;
    }
    
    private PShortTermCollectedTrafficCategoriesLaneBuilder createTrafficCategoriesDataset(TlsInputData tData) {
        Instant processingTime = Instant.now(clock);
        List<Integer> catBoundariesPkw = new ArrayList<>();
        List<Integer> catBoundariesLkw = new ArrayList<>();
        PShortTermCollectedTrafficCategoriesLaneBuilder pTrCatBuilder =
                PShortTermCollectedTrafficCategoriesLane.builder();
        PLVEErgebnisVersion4 tV4Data = (PLVEErgebnisVersion4) tData.getInputData();
        if (infraParameter.containsKey(tData.getId())) {
            InfraParameter infraPara = infraParameter.get(tData.getId());
            catBoundariesPkw = infraPara.getCategoryBoundariesPkw();
            catBoundariesLkw = infraPara.getCategoryBoundariesLkw();
        } 
        pTrCatBuilder.id(tData.getId()).intervalLength(tData.getIntervalLength()).eventTime(tData.getEventTime())
                .processingTime(processingTime)
                .categoryBoundariesPKWList(catBoundariesPkw)
                .categoryBoundariesLKWList(catBoundariesLkw)
                .numPKWSimilarCategories(catBoundariesPkw.size())
                .numLKWSimilarCategories(catBoundariesLkw.size())
                .PKWSimilarCategoryList(tV4Data.getVKlassenPkwAeList())
                .LKWSimilarCategoryList(tV4Data.getVKlassenLkwAeList());
        return pTrCatBuilder;
    }
    
    private ShortTermData createMissingDatasets(
            List<Tuple3<String, Instant, Integer>> missingIds) {
        Map<String, List<PShortTermCollectedDataLane>> missingRoadData = new HashMap<>();
        Map<String, List<PShortTermCollectedTrafficCategoriesLane>> missingTrafficCategories = new HashMap<>();
        Instant processingTime = Instant.now(clock);
        int defErrVal = props.getDefaultErrorValue();
        float defErrValFloat = props.getDefaultErrorValueFloat();

        // Set missing values for fields
        missingIds.forEach(m -> {
            boolean logicalPassivated = false;
            PTlsDataVersion version = PTlsDataVersion.UNRECOGNIZED;
            boolean vArithmetically = true;
            if (this.lane2road.containsKey(m._1)) {
                if (infraStates.containsKey(m._1)) {
                    logicalPassivated = infraStates.get(m._1).isLogicalPassivated();
                }
                if (infraParameter.containsKey(m._1)) {
                   if (infraParameter.get(m._1).getVersion().isPresent()) {
                       version = PTlsDataVersion.forNumber(infraParameter.get(m._1).getVersion().get());
                   }
                   vArithmetically = infraParameter.get(m._1).getVArithmetical();
                }
                PShortTermCollectedDataLaneBuilder pDataBuilder = PShortTermCollectedDataLane.builder();
                pDataBuilder.id(m._1).intervalLength(m._3).eventTime(m._2.minusSeconds(m._3))
                        .processingTime(processingTime)
                        .noMeasuredData(true)
                        .passivated(logicalPassivated)
                        .vArithmetically(vArithmetically)
                        .version(version)
                        .qKFZ(defErrVal)
                        .qLKW(defErrVal)
                        .vPKW(defErrVal)
                        .vLKW(defErrVal)
                        .tNetto(defErrValFloat)
                        .b(defErrVal)
                        .s(defErrVal)
                        .vKFZSmoothed(defErrVal)
                        .vehicleCategorySlow(defErrVal)
                        .vFZSlow(defErrVal)
                        .vFZSlowQuality(props.getMinQualityInputSlowV());
                missingRoadData
                        .computeIfAbsent(this.lane2road.get(m._1), l -> new LinkedList<PShortTermCollectedDataLane>())
                        .add(pDataBuilder.build());
                
                if (version.equals(PTlsDataVersion.VERSION_4)) {
                    PShortTermCollectedTrafficCategoriesLaneBuilder pTrCatBuilder = 
                            PShortTermCollectedTrafficCategoriesLane.builder().id(m._1)
                                    .intervalLength(m._3)
                                    .eventTime(m._2.minusSeconds(m._3))
                                    .processingTime(processingTime)
                                    .numPKWSimilarCategories(defErrVal)
                                    .numLKWSimilarCategories(defErrVal);
                    missingTrafficCategories.computeIfAbsent(
                            this.lane2road.get(m._1), l -> new LinkedList<PShortTermCollectedTrafficCategoriesLane>())
                        .add(pTrCatBuilder.build()); 
                }
            } else {
                log.warn("No road configured for lane {}", m._1);
            }
        });

        return new ShortTermData(missingRoadData, missingTrafficCategories);
    }

    private <T> void publishSyncToKafka(String topic, T protoList, String msgKey) {
        try {
            log.debug ("Write topic {}", topic);
            kafkaTemplate.send(MessageBuilder.withPayload(protoList)
                    .setHeader(KafkaHeaders.TOPIC, HbKafkaUtils.encodeTopicName(topic))
                    .setHeader(KafkaHeaders.KEY, msgKey)
                    .build()).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error writing topic {} to kafka: {} ", topic, e);
            Thread.currentThread().interrupt();
        }
    }
    
    private <T> void publishAsyncToKafka(String topic, T protoList, String msgKey, String topicLabel) {
        CompletableFuture<SendResult<String, Object>> completable = kafkaTemplate.send(
                MessageBuilder.withPayload(protoList)
                        .setHeader(KafkaHeaders.TOPIC, HbKafkaUtils.encodeTopicName(topic))
                        .setHeader(KafkaHeaders.KEY, msgKey)
                        .build());
        completable.whenComplete((r, e) -> completableLogging(e, topic, topicLabel, msgKey));
    }
    
    private void completableLogging(Throwable e, String topic, String topicLabel, String msgKey) {
        if (e != null) {
            log.error(logMarker, "Error writing P" + topicLabel + " with msg key {} to kafka", e, msgKey);
        } else {
            log.debug(logMarker, "Publishing new " + topicLabel +  " to topic {} with msg key {}", topic, msgKey);
        }
    }
    
    private void setCounter(String dataType, int intervalLength, String rst, String road, int number) {
        int numVdSensors = 0;
        if (road2NumLanes.containsKey(road)) {
            numVdSensors = road2NumLanes.get(road);
        }
        String sensors = "total vd sensors:" + String.valueOf(numVdSensors);
        Counter c = meterRegistry.counter(COUNTER_NAME, DATA_TYPE_TAG, dataType, INTERVAL_TAG,
                String.valueOf(intervalLength), RST_TAG, rst, ROAD_TAG, road, NUM_SENSOR_TAG, sensors);
        c.increment(number);
    }
    
    private void setDiscardedCounter(String dataType, int intervalLength, String rst, String road, int number) {
        int numVdSensors = 0;
        if (road2NumLanes.containsKey(road)) {
            numVdSensors = road2NumLanes.get(road);
        }
        String sensors = "total vd sensors:" + String.valueOf(numVdSensors);
        Counter c = meterRegistry.counter(DISCARDED_COUNTER_NAME, DATA_TYPE_TAG, dataType, INTERVAL_TAG,
                String.valueOf(intervalLength), RST_TAG, rst, ROAD_TAG, road, NUM_SENSOR_TAG, sensors);
        c.increment(number);
    }
}
