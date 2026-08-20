package de.heuboe.asfinag.vmis2.synchronize.vd.actors;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import de.heuboe.asfinag.control.base.actors.AbstractParameterActor;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureManager;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureObject;
import de.heuboe.asfinag.vmis2.infrastructure.types.ReferenceTypes;
import de.heuboe.asfinag.vmis2.synchronize.vd.SystemExit;
import de.heuboe.asfinag.vmis2.synchronize.vd.config.AlgoParameterIdProperties;
import de.heuboe.asfinag.vmis2.synchronize.vd.config.SynchronizeVdProperties;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.AbstractData;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.AbstractInfraParameter.Type;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.AlgoParameter;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.AlgoParameter.IntervalTemporaryThresholds;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.AlgoParameter.IntervalTimeLead;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.AlgoParameter.IntervalTimout;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.InfraParameter;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.InfraState;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.IntervalLengthValue;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.SingleVehicleData;
import de.heuboe.asfinag.vmis2.synchronize.vd.core.SyncVdAlgo;
import de.heuboe.asfinag.vmis2.synchronize.vd.data.TlsInputData;
import de.heuboe.asfinag.vmis2.synchronize.vd.schedule.IntervalEndJob;
import de.heuboe.asfinag.vmis2.synchronize.vd.schedule.IntervalEndTriggerData;
import de.heuboe.asfinag.vmis2.synchronize.vd.schedule.IntervalTimeoutTriggerData;
import de.heuboe.asfinag.vmis2.synchronize.vd.schedule.ScheduleUtils;
import de.heuboe.asfinag.vmis2.synchronize.vd.schedule.TimeoutJob;
import de.heuboe.asfinag.vmis2.synchronize.vd.services.TlsSynVdPublisher;
import eu.vmis_ehe.vmis2.paramservice.pojo.PParameterSet;
import eu.vmis_ehe.vmis2.paramservice.pojo.PParameterSetList;
import eu.vmis_ehe.vmis2.paramservice.pojo.PParameterValue;
import eu.vmis_ehe.vmis2.paramservice.pojo.PValueWrapper;
import eu.vmis_ehe.vmis2.receiving.processing.data.TlsDataVersion;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEBetriebsparameter;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEBetriebsparameterList;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEDeFehler;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEDeFehlerList;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion0List;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion1List;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion2List;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion3List;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion4List;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion5List;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion6List;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEGeschwindigkeitsklassenKurz;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEGeschwindigkeitsklassenKurzList;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEKanalsteuerung;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEKanalsteuerungList;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEKfzEinzeldatenList;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEKfzEinzeldatenSammelmeldungList;
import eu.vmis_ehe.vmis2.tls.received.pojo.PSYSFehlerDUE;
import eu.vmis_ehe.vmis2.tls.received.pojo.PSYSFehlerDUEList;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * Actor to run situation merging for one road and situation class
 */
@Component
@Scope("prototype")
@Slf4j
public class AlgoActor extends AbstractActorWithTimers {
    private Marker logMarker1 = MarkerFactory.getMarker("actor");   
   
    private static final String CRON_EXPRESSION_EVERY_SECOND = "* * * * * ? *";
    private static final String ALGO_NOT_INITIALIZED = "algo is null (not initialized)!";
    private static final DateTimeFormatter f = DateTimeFormatter
            .ofLocalizedDateTime( FormatStyle.LONG )
            .withLocale( Locale.GERMAN )
            .withZone( ZoneId.systemDefault() );
    private static final String COUNTER_NAME = "synchronize.input.data";
    private static final String INPUT_TAG = "input";

     
    /**
     * Init Class which stores the main information data for the Actor.
     */
    @Value
    public static class Init {
        private InfrastructureManager infrastructure;
        private ActorRef paramSystemActorTimeSync;
        private ActorRef paramRoadActorLogicalPassive;
        
        private Collection<PLVEDeFehler> tlsErrors;
        private Collection<PSYSFehlerDUE> tlsSysErrors;
        private Collection<PLVEBetriebsparameter> tlsOpParams;
        private Collection<PLVEKanalsteuerung>  tlsChControls;
        private Collection<PLVEGeschwindigkeitsklassenKurz>  tlsTrafficCategories;
    }
    
    @Autowired
    private SynchronizeVdProperties appProperties;
       
    @Autowired
    private AlgoParameterIdProperties paramIds;
    
    @Autowired
    private TlsSynVdPublisher publisher;
    
    @Autowired
    private Clock clock;

    private InfrastructureManager infrastructure;
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    @Autowired
    private SystemExit systemExit;; 
    
    
    private SyncVdAlgo<AbstractData> algo;
    private AlgoParameter algoParams;
    private Map<String, InfraState> infraStates = new HashMap<>();
    private Map<String, InfraParameter> infraParams = new HashMap<>();
    private StdSchedulerFactory sf;
    private boolean isInitialized = false;
    private Scheduler schedTimeout;
    private Scheduler schedIntervalEnd;
   

    protected void setCounter(String input, int number) {
        Counter c = meterRegistry.counter(COUNTER_NAME, INPUT_TAG, input);
        if (c == null) {
            c = Counter.builder(COUNTER_NAME).tag(INPUT_TAG, input)
                    .description("synchronize-vd-app test counter").register(meterRegistry);
        }
        c.increment(number);
    }
    
    @Override
    public void preStart() throws Exception {
        getContext().getSystem().eventStream().subscribe(this.getSelf(), PLVEErgebnisVersion0List.class);
        getContext().getSystem().eventStream().subscribe(this.getSelf(), PLVEErgebnisVersion1List.class);
        getContext().getSystem().eventStream().subscribe(this.getSelf(), PLVEErgebnisVersion2List.class);
        getContext().getSystem().eventStream().subscribe(this.getSelf(), PLVEErgebnisVersion3List.class);
        getContext().getSystem().eventStream().subscribe(this.getSelf(), PLVEErgebnisVersion4List.class);
        getContext().getSystem().eventStream().subscribe(this.getSelf(), PLVEErgebnisVersion5List.class);
        getContext().getSystem().eventStream().subscribe(this.getSelf(), PLVEErgebnisVersion6List.class);
        getContext().getSystem().eventStream().subscribe(this.getSelf(), PLVEKfzEinzeldatenList.class);
        getContext().getSystem().eventStream().subscribe(this.getSelf(), PLVEKfzEinzeldatenSammelmeldungList.class);
        getContext().getSystem().eventStream().subscribe(this.getSelf(), PLVEDeFehlerList.class);
        getContext().getSystem().eventStream().subscribe(this.getSelf(), PSYSFehlerDUEList.class);
        getContext().getSystem().eventStream().subscribe(this.getSelf(), PLVEBetriebsparameterList.class);
        getContext().getSystem().eventStream().subscribe(this.getSelf(), PLVEKanalsteuerungList.class);
        getContext().getSystem().eventStream().subscribe(this.getSelf(), PLVEGeschwindigkeitsklassenKurzList.class);
        getContext().getSystem().eventStream().subscribe(this.getSelf(), AbstractParameterActor.Parameters.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Init.class, this::init)
                // handle data
                .match(PLVEErgebnisVersion0List.class, this::handleVersion0Data)
                .match(PLVEErgebnisVersion1List.class, this::handleVersion1Data)
                .match(PLVEErgebnisVersion2List.class, this::handleVersion2Data)
                .match(PLVEErgebnisVersion3List.class, this::handleVersion3Data)
                .match(PLVEErgebnisVersion4List.class, this::handleVersion4Data)
                .match(PLVEErgebnisVersion5List.class, this::handleVersion5Data)
                .match(PLVEErgebnisVersion6List.class, this::handleVersion6Data)
                .match(PLVEKfzEinzeldatenList.class, this::handleSingleVehicleData)
                .match(PLVEKfzEinzeldatenSammelmeldungList.class, this::handleSingleVehicleCollectionData)
                // administrative tls
                .match(PLVEBetriebsparameterList.class, this::handleTlsOperatingParam)
                .match(PLVEKanalsteuerungList.class, this::handleTlsChannelControl)
                .match(PLVEDeFehlerList.class, this::handleTlsError)
                .match(PSYSFehlerDUEList.class, this::handleTlsSysError)
                .match(PLVEGeschwindigkeitsklassenKurzList.class, this::handleTrafficCategory)
                // parameters
                .match(AbstractParameterActor.Parameters.class, this::handleParameters)
                .match(AbstractParameterActor.MissingParameter.class, this::handleMissingParmeters)
                 // triggers
                .match(IntervalEndTriggerData.class, this::handleIntervalEndTrigger)
                .match(IntervalTimeoutTriggerData.class, this::handleIntervalTimeoutTrigger)
                .matchAny(o -> log.error(logMarker1, "received unknown message of type {}. Couldn't handle ... discarding!",
                        o.getClass().getName()))
                .build();
    }

    /**
     * Initialize AlgoActor.
     * 
     * @param i     Init object
     * @throws SchedulerException exception while initializing timers.
     */
    public void init(Init i) throws SchedulerException {
        this.infrastructure = i.getInfrastructure();
        // Create new algo
        algo = new SyncVdAlgo<>(publisher, Clock.systemDefaultZone());
        
        log.info(logMarker1, "New synchronize-algo created");
        logRoadLaneCount();
        // Set meta data to algo:
        algo.setInfrastructure(this.infrastructure.getInfrastructureObjectsOfType(ReferenceTypes.LANE));
        
        // Fill initial infraStates from infrastructure
        initInfraStates();
        // Fill initial infraParams from infrastructure
        initInfraParams();
        
        isInitialized = true;
        
        // Send the tell to publish parameters logical passivation for all roads!
        ActorRef parameterActorLogPassive = i.getParamRoadActorLogicalPassive();
        List<InfrastructureObject> roads = infrastructure.getInfrastructureObjectsOfType(ReferenceTypes.getRoadReferenceType());
        log.debug(logMarker1, "Get parameter logical passivation for potentially {} roads", roads.size());
        roads.forEach(rd -> {
            parameterActorLogPassive.tell(new AbstractParameterActor.PublishParameters(
                    rd.getId(), paramIds.getLogPassiveDefSetId(), appProperties.getInstanceName()), this.getSelf());        
        });
        
        // Send the tell to publish parameter time synchronization!
        ActorRef parameterActorTimeSync= i.getParamSystemActorTimeSync();
        log.debug(logMarker1, "Get rVMZ wide parameter for time synchronization");
        parameterActorTimeSync.tell(new AbstractParameterActor.PublishParameters(
                "", paramIds.getTimeSyncDefSetId(), appProperties.getInstanceName()), this.getSelf());
        
        handleTlsOperatingParams(i.getTlsOpParams());
        handleTlsErrors(i.getTlsErrors());
        handleTlsSysErrors(i.getTlsSysErrors());
        handleTlsChannelControls(i.getTlsChControls());
        handleTrafficCategories(i.getTlsTrafficCategories());    

      }

    private void logRoadLaneCount() {
        List<InfrastructureObject> roads = this.infrastructure.getInfrastructureObjectsOfType(ReferenceTypes.ROAD);
        Map<String, Integer> laneCountPerRoad = new HashMap<>();
        for(InfrastructureObject road : roads) {
            List<InfrastructureObject> lanes = road.getReferences(ReferenceTypes.LANE);
            Integer curCount = laneCountPerRoad.getOrDefault(road.getId(), 0);
            curCount += lanes!=null?lanes.size():0;
            laneCountPerRoad.put(road.getId(), curCount);
        }
        for(Entry<String, Integer> entry : laneCountPerRoad.entrySet()) {
            log.debug(getIdMarker(entry.getKey()), "has {} lanes.",  entry.getValue());
        }
    }

    @SneakyThrows
    private void handleParameters(AbstractParameterActor.Parameters<PParameterSetList> p) {
        if(!isInitialized) {
            log.warn(logMarker1, "Handle parameters: Init must be called before handling Data. Waiting for init...");
            return;
        }
        if(p != null && p.algo() != null) {
            if(paramIds.getTimeSyncDefSetId().equals(p.algo())) {
                handleTimeSyncParameter(p);
            } else if(paramIds.getLogPassiveDefSetId().equals(p.algo())) {
                handleLogPassParameter(p);
            }
        }
    }

    @SneakyThrows
    private void handleMissingParmeters(AbstractParameterActor.MissingParameter p) {
        log.debug(logMarker1, "Missing parameter called: {}", p);
        if(!isInitialized) {
            log.warn(logMarker1, "Handle missing parameters: Init must be called before handling Data. Waiting for init...");
            return;
        }
        if(p != null && p.algo() != null) {
            if(paramIds.getTimeSyncDefSetId().equals(p.algo())) {
                log.error(logMarker1, "Missing rVMZ wide parameter for time synchronization");
                systemExit.exit(0);
            } else if(paramIds.getLogPassiveDefSetId().equals(p.algo())) {
                log.debug(logMarker1, "Missing logical passivation parameter for {} and road {} => NO error", p.algo(), p.roadId());
                return;
            }
        }
    }

    @SneakyThrows
    private void handleLogPassParameter(AbstractParameterActor.Parameters<PParameterSetList> p) { //NOSONAR
        if(p != null && p.parameters() != null
                && p.parameters().getValuesList() != null
                && p.parameters().getValuesList().isEmpty()) {
            log.info(getIdMarker(paramIds.getLogPassiveDefSetId()), "Got empty parameter for system '{}' and road '{}'",
                    appProperties.getCentreId(), p.roadId());
            return;
        }
        log.info(getIdMarker(paramIds.getLogPassiveDefSetId()), "Received new LogicalPassivationParameter for {}, {} and road {}",
                p.system(), p.algo(), p.roadId());
       
        List<PParameterSet> sets = p.parameters().getValuesList();
        for(PParameterSet set : sets) {
            if(set.getTarget() != null && set.getTarget().getItemId() != null) {
                String laneId = set.getTarget().getItemId();
                if(set.getValuesList() != null && !set.getValuesList().isEmpty()) {
                     List<PParameterValue> vals = set.getValuesList();
                     for(PParameterValue val : vals) {
                         if(paramIds.getLogPassive().equals(val.getParameterId()) && val.getValue() != null) {
                             Boolean logicalPassive = val.getValue().getBooleanVal();
                             InfraState is = this.infraStates.get(laneId);
                             if(is != null && !logicalPassive.equals(is.isLogicalPassivated())) {
                                this.infraStates.put(laneId, new InfraState(laneId, is.isOk(), is.getCause(),
                                        Instant.now(clock), is.isPhysicalPassivated(), logicalPassive));
                             }
                         }
                     }
                }
            } else {
                log.info(getIdMarker(paramIds.getLogPassiveDefSetId()),
                        "Got parameter for logical passivation without plausible parameter target value for "
                                + "roadId '{}'. Can not determine lane id.",
                        p.roadId());
                continue;
            }
        }
    }
    
    @SneakyThrows
    private void handleTimeSyncParameter(AbstractParameterActor.Parameters<PParameterSetList> p) {
        if(p == null || (p.parameters() != null
                && p.parameters().getValuesList() != null
                && p.parameters().getValuesList().isEmpty())) {
            log.info(getIdMarker(paramIds.getTimeSyncDefSetId()), "No {} parameter for system {}", paramIds.getTimeSyncDefSetId(),
                    appProperties.getCentreId());
            return;
        }
        
        log.info(getIdMarker(paramIds.getTimeSyncDefSetId()), "Received new TimeSyncParameter({}) for {}, {}",
                paramIds.getTimeSyncDefSetId(), p.system(), p.algo());
        
        PParameterSet paramSet = p.parameters().getValuesList().get(0);
        String currentSystemId = paramSet.getTarget().getItemId();
        Map<String, PValueWrapper> algoParaMap = getSyncAlgoParaValues(paramSet);
        Set<Tuple2<IntervalLengthValue, String>> toTuples = Set.of(
                Tuple.of(IntervalLengthValue.SEC_15, paramIds.getTimeout15Secs()),
                Tuple.of(IntervalLengthValue.SEC_30, paramIds.getTimeout30Secs()),
                Tuple.of(IntervalLengthValue.SEC_60, paramIds.getTimeout60Secs()),
                Tuple.of(IntervalLengthValue.MIN_2, paramIds.getTimeout2Min()),
                Tuple.of(IntervalLengthValue.MIN_3, paramIds.getTimeout3Min()),
                Tuple.of(IntervalLengthValue.MIN_4, paramIds.getTimeout4Min()),
                Tuple.of(IntervalLengthValue.MIN_5, paramIds.getTimeout5Min())
            );             
        List<IntervalTimout> timeouts = toTuples.stream()
                    .filter(toTuple -> algoParaMap.containsKey(toTuple._2))
                    .map(toTuple -> new IntervalTimout(toTuple._1, algoParaMap.get(toTuple._2).getIntVal()))
                    .collect(Collectors.toList());
            
        // TODO: The lead time should be adjustable per interval
        Set<Tuple2<IntervalLengthValue, String>> tlTuples = Set.of(
                    Tuple.of(IntervalLengthValue.SEC_15, paramIds.getTimelead()),
                    Tuple.of(IntervalLengthValue.SEC_30, paramIds.getTimelead()),
                    Tuple.of(IntervalLengthValue.SEC_60, paramIds.getTimelead()),
                    Tuple.of(IntervalLengthValue.MIN_2, paramIds.getTimelead()),
                    Tuple.of(IntervalLengthValue.MIN_3, paramIds.getTimelead()),
                    Tuple.of(IntervalLengthValue.MIN_4, paramIds.getTimelead()),
                    Tuple.of(IntervalLengthValue.MIN_5, paramIds.getTimelead())
                );             
        List<IntervalTimeLead> timeleads = tlTuples.stream()
                .filter(tlTuple -> algoParaMap.containsKey(tlTuple._2))
                .map(tlTuple -> new IntervalTimeLead(tlTuple._1, algoParaMap.get(tlTuple._2).getIntVal()))
                .collect(Collectors.toList());
        
        Set<Tuple3<IntervalLengthValue, String, String>> thTriples = Set.of(
                Tuple.of(IntervalLengthValue.SEC_15, paramIds.getThresholdLower15Sec(),
                        paramIds.getThresholdUpper15Sec()),
                Tuple.of(IntervalLengthValue.SEC_30, paramIds.getThresholdLower30Sec(),
                        paramIds.getThresholdUpper30Sec()),
                Tuple.of(IntervalLengthValue.SEC_60, paramIds.getThresholdLower60Sec(),
                        paramIds.getThresholdUpper60Sec()),
                Tuple.of(IntervalLengthValue.MIN_2, paramIds.getThresholdLower2Min(),
                        paramIds.getThresholdUpper2Min()),
                Tuple.of(IntervalLengthValue.MIN_3, paramIds.getThresholdLower3Min(),
                        paramIds.getThresholdUpper3Min()),
                Tuple.of(IntervalLengthValue.MIN_4, paramIds.getThresholdLower4Min(),
                        paramIds.getThresholdUpper4Min()),
                Tuple.of(IntervalLengthValue.MIN_5, paramIds.getThresholdLower5Min(),
                        paramIds.getThresholdUpper5Min()));
        List<IntervalTemporaryThresholds> thresholds = thTriples.stream()
                .filter(thTriple -> algoParaMap.containsKey(thTriple._2) && algoParaMap.containsKey(thTriple._3))
                .map(thTriple -> new IntervalTemporaryThresholds(thTriple._1, algoParaMap.get(thTriple._2).getIntVal(),
                        algoParaMap.get(thTriple._3).getIntVal()))
                .collect(Collectors.toList());
        
        this.algoParams = new AlgoParameter(currentSystemId, Type.LOCAL_DEFAULT, clock.instant(),
                algoParaMap.get(paramIds.getSyncWaitSec()).getIntVal(),
                algoParaMap.get(paramIds.getTimelead()).getIntVal(), timeouts, timeleads, thresholds);
          
        updateAlgoParams();
    }
    
    
    private Map<String, PValueWrapper> getSyncAlgoParaValues(PParameterSet para) {
        Map<String, PValueWrapper> algoParaMap = new HashMap<>();
        for (PParameterValue p : para.getValuesList()) {
            algoParaMap.put(p.getParameterId(), p.getValue());
        }
        return algoParaMap;
    }
    
    private void initInfraParams() {
        infraParams.clear();
        // If the infrastructure parameters should be set later via another mechanism, you can set the
        // flag to false in the application.properties.
        if (!appProperties.isFakeInfraParams()) {
            return;
        }
        // Initialize infrastructure parameter. So that not a large part of the input values is discarded.
        if (infrastructure != null && infrastructure.getInfrastructureObjectsOfType(ReferenceTypes.LANE) != null) {
            log.info(logMarker1, "Initialize infrastructure parameters for {} lanes",
                    infrastructure.getInfrastructureObjectsOfType(ReferenceTypes.LANE).size());
            infrastructure.getInfrastructureObjectsOfType(ReferenceTypes.LANE)
                    .forEach(lane -> infraParams.put(lane.getId(),
                            new InfraParameter(lane.getId(), Type.LOCAL_DEFAULT, Instant.now(clock),
                                    IntervalLengthValue.SEC_60.getSeconds(), TlsDataVersion.VERSION_3_VALUE, true,
                                    Collections.emptyList(), Collections.emptyList())));
        }
    }

    private void initInfraStates() {
        // Initialize the infrastructure state for all lane ids to ok. So that not a large part of the input
        // values is discarded.
        infraStates.clear();
        if (infrastructure != null && infrastructure.getInfrastructureObjectsOfType(ReferenceTypes.LANE) != null) {
            log.info(logMarker1,"Initialize infrastructure state for {} lanes",
                    infrastructure.getInfrastructureObjectsOfType(ReferenceTypes.LANE).size());
             infrastructure.getInfrastructureObjectsOfType(ReferenceTypes.LANE).forEach(lane -> infraStates
                    .put(lane.getId(), new InfraState(lane.getId(), true, null, Instant.now(clock), false, false)));
        }
    }

    
    private void startScheduler() throws SchedulerException {
        // set timeouts etc. periodically
        if (algoParams == null) {
            return;
        }
        // Set threadCount to 1 in order to prevent multiple thread workers to call same triggers multiple
        // times:
        Properties props = new Properties();
        props.setProperty("org.quartz.threadPool.threadCount", "1");
        // Initialize SchedulerFactory with properties.
        sf = new StdSchedulerFactory(props);

        // Collect informations about cron-expressions (timeout, interval end) and timeouts - grouped by
        // interval lengths
        Map<Integer, String> ilCronExpMapTimeout = new HashMap<>();
        Map<Integer, String> ilCronExpMapIntervalend = new HashMap<>();
        Map<Integer, Integer> ilTimeoutSecondsMap = new HashMap<>();

        for (IntervalLengthValue il : IntervalLengthValue.values()) {
            if(IntervalLengthValue.UNDEFINED.equals(il)) {
                //do not init timer for UNDEFINED (seconds: -1)
                continue;
            }
            IntervalTimout to = (algoParams.getTimeouts() != null ? algoParams.getTimeouts().get(il) : null);
            List<Integer> ieParts = ScheduleUtils.getCronExpParts(il.getSeconds());

            StringBuilder expTimeout = new StringBuilder();
            StringBuilder expIntervalEnd = new StringBuilder();
            buildCronExpressions(expTimeout, expIntervalEnd, ieParts, to);
            expTimeout.append("* * ? *");
            expIntervalEnd.append("* * ? *");

            if (to != null) {
                ilCronExpMapTimeout.put(il.getSeconds(), expTimeout.toString());
                ilTimeoutSecondsMap.put(il.getSeconds(), to.getTimeout());
            }
            ilCronExpMapIntervalend.put(il.getSeconds(), expIntervalEnd.toString());

        }
        initTimerTimeout(ilCronExpMapTimeout, ilTimeoutSecondsMap);
        initTimerIntervalEnd(ilCronExpMapIntervalend);
    }

    private void buildCronExpressions(StringBuilder expTimeout, StringBuilder expIntervalEnd, List<Integer> ieParts,
            IntervalTimout to) {
        for (int i = 0; i <= 2; i++) {
            Integer pIE = ieParts.size() >= i + 1 ? ieParts.get(i) : null;
            Integer pTO = null;
            if (to != null) {
                int curTimeoutSec = to.getTimeout();
                List<Integer> toParts = ScheduleUtils.getCronExpParts(curTimeoutSec);
                pTO = toParts.size() >= i + 1 ? toParts.get(i) : null;
            }
            String repeatExp = pIE != null ? ("0/" + pIE + " ") : "* ";
            if (pTO != null) {
                expTimeout.append(pTO + "/" + (pIE != null ? pIE : 1) + " ");
            } else {
                expTimeout.append(repeatExp);
            }
            expIntervalEnd.append(repeatExp);
        }
    }

    private void initTimerTimeout(Map<Integer, String> ilCronExpMap, Map<Integer, Integer> ilTimeoutMap)
            throws SchedulerException {
        resetTimerTimeout();
        this.schedTimeout = sf.getScheduler();
        this.schedTimeout.start();
        JobDetail job = JobBuilder.newJob(TimeoutJob.class).build();
        JobDataMap jobMap = job.getJobDataMap();
        jobMap.put(TimeoutJob.ACTORREF, self());
        jobMap.put(TimeoutJob.IL_TIMEOUT_MAP, ilTimeoutMap);
        jobMap.put(TimeoutJob.IL_CRON_EXPRESSION_MAP, ilCronExpMap);
        Trigger t = TriggerBuilder.newTrigger().startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(CRON_EXPRESSION_EVERY_SECOND)).build();
        log.info(logMarker1, "init timer 'timeout': cronexp={}", CRON_EXPRESSION_EVERY_SECOND);
        this.schedTimeout.scheduleJob(job, t);
    }

    private void initTimerIntervalEnd(Map<Integer, String> ilCronExpMap) throws SchedulerException {
        resetTimerIntervalEnd();
        this.schedIntervalEnd = sf.getScheduler();
        this.schedIntervalEnd.start();
        JobDetail job = JobBuilder.newJob(IntervalEndJob.class).build();
        JobDataMap jobMap = job.getJobDataMap();
        jobMap.put(IntervalEndJob.ACTORREF, self());
        jobMap.put(IntervalEndJob.IL_CRON_EXPRESSION_MAP, ilCronExpMap);
        Trigger t = TriggerBuilder.newTrigger().startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(CRON_EXPRESSION_EVERY_SECOND)).build();
        log.info(logMarker1, "init timer 'intervalend': cronexp={}", CRON_EXPRESSION_EVERY_SECOND);
        this.schedIntervalEnd.scheduleJob(job, t);
    }

    private void updateAlgoParams() throws SchedulerException {
        if( this.algo != null && this.algoParams != null ) {
            algo.setAlgoParameter(this.algoParams);
        }
        startScheduler();
    }

    private void updateInfraParams() {
        if( this.algo != null && this.infraParams != null ) {
            this.algo.setInfraParameter(this.infraParams);
        }
    }

    private void updateInfraStates() {
        if( this.algo != null ) {
            algo.setInfraState(this.infraStates);
        }
    }

    private void handleTlsError(PLVEDeFehlerList failures) {       
        if (failures != null && failures.getElementsList() != null) {
            handleTlsErrors(failures.getElementsList());
        }
    }

    private void handleTlsErrors(Collection<PLVEDeFehler> failures) {
        if(isInitialized && failures != null) {
            for (PLVEDeFehler fail : failures) {
                if (fail != null) {
                    List<String> ids = new ArrayList<>();
                    ids.add(fail.getId());
                    List<InfrastructureObject> infra = infrastructure.getInfrastructureObjects(ids);
                    if(infra != null && !infra.isEmpty()) {
                        log.debug(getIdMarker(fail.getId()),"LVEDeFehler received");
                        updateOkFlagAndCause(fail.getId(),
                                fail.getFehlercode() == 0, fail.getClass().getSimpleName().substring(1),
                                fail.getTlsTime());
                    } else {
                        log.warn(getIdMarker(fail.getId()),"Received id for LVEDeFehler is unknown!");
                    }
                }
            }
        }
    }

    private void handleTlsOperatingParam(PLVEBetriebsparameterList opParams) {
        if (opParams != null && opParams.getElementsList() != null) {
            handleTlsOperatingParams(opParams.getElementsList());
        }
    }

    private void handleTlsOperatingParams(Collection<PLVEBetriebsparameter> opParams) {
        if(isInitialized && opParams != null) {
            for (PLVEBetriebsparameter op : opParams) {
                List<String> ids = new ArrayList<>();
                ids.add(op.getId());
                IntervalLengthValue ilv = IntervalLengthValue.getIntervalLengthValue(
                        op.getErfassungsintervalldauerKurz());
                if (ilv.equals(IntervalLengthValue.UNDEFINED)) {
                    log.warn(getIdMarker(op.getId()), "Received illegal interval length '{}' in LVEBetriebsparameter",
                            op.getErfassungsintervalldauerKurz());
                }
                int intervalLengthSec = ilv.getSeconds();
                if(!infrastructure.getInfrastructureObjects(ids).isEmpty()) {

                    InfraParameter curInfraPara = infraParams.get(op.getId());
                    InfraParameter newInfraParam =
                            new InfraParameter(op.getId(), Type.LOCAL_DEFAULT, op.getTlsTime(), intervalLengthSec,
                                    op.getDatenversionKurz(), op.getArtMittelwertbildung() == 1 ? true : false,
                                    curInfraPara.getCategoryBoundariesPkw(), curInfraPara.getCategoryBoundariesLkw());
                    infraParams.put(op.getId(), newInfraParam);
                    log.debug(getIdMarker(op.getId()), "LVEBetriebsparameter received");                   
                } else {
                    log.warn(getIdMarker(op.getId()),"Received id for LVEBetriebsparameter is unknown!");
               }
            }
        }
        updateInfraParams();
    }

    private void handleTlsChannelControl(PLVEKanalsteuerungList channelControl) {
        if (channelControl != null && channelControl.getElementsList() != null) {
            handleTlsChannelControls(channelControl.getElementsList());
        }
    }

    private void handleTlsChannelControls(Collection<PLVEKanalsteuerung> channelControl) {
        if(isInitialized && channelControl != null) {
            for (PLVEKanalsteuerung ch : channelControl) {
                List<String> ids = new ArrayList<>();
                ids.add(ch.getId());
                if (!infrastructure.getInfrastructureObjects(ids).isEmpty()) {
                    InfraState curState = infraStates.get(ch.getId());
                    InfraState newState = new InfraState(ch.getId(), curState == null || curState.isOk(),
                            curState != null ? curState.getCause() : null, ch.getTlsTime(),
                            ch.getKanalsteuerbyte() == 1,
                            curState!=null?curState.isLogicalPassivated():false);
                    infraStates.put(ch.getId(), newState);
                    log.debug(getIdMarker(ch.getId()),"LVEKanalsteuerung received");
                } else {
                    log.warn(getIdMarker(ch.getId()),"Received id for LVEKanalsteuerung is unknown!");
                }
            }
        }
        updateInfraStates();
    }
    
    private void handleTlsSysError(PSYSFehlerDUEList rsStates) {
        if (rsStates != null && rsStates.getElementsList() != null) {
            handleTlsSysErrors(rsStates.getElementsList());
        }
    }
    private void handleTlsSysErrors(Collection<PSYSFehlerDUE> rsStates) {
        if(isInitialized && rsStates != null) {
            for (PSYSFehlerDUE rsState : rsStates) {
                ArrayList<String> rsIds = new ArrayList<>();
                rsIds.add(rsState.getId());
                List<InfrastructureObject> routeStations = infrastructure.getInfrastructureObjects(rsIds);
                if (!routeStations.isEmpty()) {
                    routeStations.get(0).getReferences(ReferenceTypes.LANE).forEach(l -> {
                        updateOkFlagAndCause(l.getId(), rsState.getFehlercode() == 0,
                                rsState.getClass().getSimpleName().substring(1), rsState.getTlsTime());
                    });
                    log.debug(getIdMarker(rsState.getId()),"SYSFehlerDUE received");
                } else {
                    log.warn(getIdMarker(rsState.getId()),"Received id for SYSFehlerDUE is unknown!");
                }
            }
        }
    }
    private void handleTrafficCategory(PLVEGeschwindigkeitsklassenKurzList trafficCategory) {
        if (trafficCategory != null && trafficCategory.getElementsList() != null) {
            handleTrafficCategories(trafficCategory.getElementsList());
            
        }
    }
    private void handleTrafficCategories(Collection<PLVEGeschwindigkeitsklassenKurz> trafficCategories) {
        if(isInitialized && trafficCategories != null) {
            for (PLVEGeschwindigkeitsklassenKurz trCat : trafficCategories) {
                ArrayList<String> trCatIds = new ArrayList<>();
                trCatIds.add(trCat.getId());
                if (!infrastructure.getInfrastructureObjects(trCatIds).isEmpty()) {
                    InfraParameter curInfraPara = infraParams.get(trCat.getId());
                    List<Integer> categoryBoundaries = new ArrayList<>();
                    List<Integer> categoryBoundariesPkw = curInfraPara.getCategoryBoundariesPkw();
                    List<Integer> categoryBoundariesLkw = curInfraPara.getCategoryBoundariesLkw();
                    for (byte vBound : trCat.getVGrenzen().toByteArray()) {
                        // Mask signed byte to unsigned and save to int 
                        categoryBoundaries.add(vBound & 0xff); 
                    }
                    if (Integer.valueOf(trCat.getFahrzeugklasse()).equals(appProperties.getCategoryPkw())) {
                        categoryBoundariesPkw = categoryBoundaries;
                    } else {
                        categoryBoundariesLkw = categoryBoundaries;
                    }
                    int version = TlsDataVersion.VERSION_3_VALUE;
                    if (curInfraPara.getVersion().isPresent()) {
                        version = curInfraPara.getVersion().get();                    
                    } else {
                        log.warn(getIdMarker(trCat.getId()), "No tls version received");  
                    }
                    InfraParameter newInfraPara = new InfraParameter(trCat.getId(), Type.LOCAL_DEFAULT,
                            trCat.getTlsTime(), curInfraPara.getIntervalLength(), version,
                            curInfraPara.getVArithmetical(), categoryBoundariesPkw, categoryBoundariesLkw);
                    infraParams.put(trCat.getId(), newInfraPara);
                    log.debug(getIdMarker(trCat.getId()),"LVEGeschwindigkeitsklassenKurz received");                    
                } else {
                    log.warn(getIdMarker(trCat.getId()),"Received id for LVEGeschwindigkeitsklassenKurz is unknown!");                
                }
            }
        }
    }
    private void updateOkFlagAndCause(String laneId, boolean newIsOk, String newCause, Instant time) {
        boolean update = false;
        boolean isPhysicalPassivated = false;
        boolean isLogicalPassivated = false;
        if (infraStates.containsKey(laneId)) {
            InfraState curState = infraStates.get(laneId);
            boolean curIsOk = curState.isOk();
            String curCause = curState.getCause();
            isPhysicalPassivated = curState.isPhysicalPassivated();
            isLogicalPassivated = curState.isLogicalPassivated();
            // ok state changed?
            if (newIsOk != curIsOk) {
                // new ok state == not ok or new state == ok and cause unchanged?
                if (!newIsOk) {
                    update = true;
                } else {
                    // new state == ok and cause unchanged?
                    if (newCause.equals(curCause)) {
                        update = true;
                        newCause = "";
                    }
                }
            } else { // ok state unchanged
                // new ok state == not ok and cause changed?
                if (!newIsOk && !newCause.equals(curCause)) {
                    update = true;
                }
            }
        } else {
            update = true;
        }
        if (update) {
            infraStates.put(laneId,
                    new InfraState(laneId, newIsOk, newCause, time, isPhysicalPassivated, isLogicalPassivated));
            updateInfraStates();
        }
    }

    private void handleVersion0Data(PLVEErgebnisVersion0List data) {
        
        if (isInitialized && data != null && data.getElementsList() != null && !data.getElementsList().isEmpty()) {
            List<AbstractData> inputData = data.getElementsList().stream()
                    .filter(d -> checkIntervalLength(d.getIntervalllaenge(), d.getId()))
                    .peek(d -> log.trace(getIdMarker(d.getId()),"LVEErgebnisVersion0 received")) // NOSONAR just for debugging
                    .map(TlsInputData::new)
                    .collect(Collectors.toList());
            if (algo != null) {
                long start = clock.millis();
                algo.setData(inputData);
                log.trace(logMarker1, "{} LVEErgebnisVersion0 processed within {} millis.", inputData.size(),
                        (clock.millis() - start));
            } else {
                handleAlgoNotInitialized("Handle Input LVEErgebnisVersion0:");
            }
        }
    }

    private void handleVersion1Data(PLVEErgebnisVersion1List data) {
        if(isInitialized && data != null && data.getElementsList() != null && !data.getElementsList().isEmpty()) {
            List<AbstractData> inputData = data.getElementsList().stream()
                    .filter(d -> checkIntervalLength(d.getIntervalllaenge(), d.getId()))
                    .peek(d -> log.trace(getIdMarker(d.getId()),"LVEErgebnisVersion1 received") )// NOSONAR just for debugging
                    .map(TlsInputData::new).collect(Collectors.toList());
            if (algo != null) {
                long start = clock.millis();
                algo.setData(inputData);
                log.trace(logMarker1,
                        "{} LVEErgebnisVersion1 processed within {} millis.", inputData.size(),
                        (clock.millis() - start));
            } else {
                handleAlgoNotInitialized("Handle Input LVEErgebnisVersion1:");
            }
        }
    }

    private void handleVersion2Data(PLVEErgebnisVersion2List data) {
        if(isInitialized && data != null && data.getElementsList() != null && !data.getElementsList().isEmpty()) {
            List<AbstractData> inputData = data.getElementsList().stream()
                    .filter(d -> checkIntervalLength(d.getIntervalllaenge(), d.getId()))
                    .peek(d -> log.trace(getIdMarker(d.getId()),"LVEErgebnisVersion2 received")) // NOSONAR just for debugging
                    .map(TlsInputData::new)
                    .collect(Collectors.toList());
            if (algo != null) {
                long start = clock.millis();
                algo.setData(inputData);
                log.trace(logMarker1,
                        "{} LVEErgebnisVersion2 processed within {} millis.", inputData.size(),
                        (clock.millis() - start));
            } else {
                handleAlgoNotInitialized("Handle Input LVEErgebnisVersion2:");
           }       
        }
    }

    private void handleVersion3Data(PLVEErgebnisVersion3List data) {
        if (isInitialized && data != null && data.getElementsList() != null && !data.getElementsList().isEmpty()) {
            List<AbstractData> inputData = data.getElementsList().stream()
                    .filter(d -> checkIntervalLength(d.getIntervalllaenge(), d.getId()))
                    .peek(d -> log.trace(getIdMarker(d.getId()), "LVEErgebnisVersion3 received")) // NOSONAR just for debugging
                    .map(TlsInputData::new)
                    .collect(Collectors.toList());
            if (algo != null) {
                long start = clock.millis();
                algo.setData(inputData);
                log.trace(logMarker1,
                        "{} LVEErgebnisVersion3 processed within {} millis.", inputData.size(),
                        (clock.millis() - start));
            } else {
                handleAlgoNotInitialized("Handle Input LVEErgebnisVersion3:");
            }
        }
    }

    private void handleVersion4Data(PLVEErgebnisVersion4List data) {
        if (isInitialized && data != null && data.getElementsList() != null && !data.getElementsList().isEmpty()) {
            List<AbstractData> inputData = data.getElementsList().stream()
                    .filter(d -> checkIntervalLength(d.getIntervalllaenge(), d.getId()))
                    .peek(d -> log.trace(getIdMarker(d.getId()), "LVEErgebnisVersion4 received")) // NOSONAR just for debugging
                    .map(TlsInputData::new)
                    .collect(Collectors.toList());
            if (algo != null) {
                long start = clock.millis();
                algo.setData(inputData);
                log.trace(logMarker1,
                        "{} LVEErgebnisVersion4 processed within {} millis.", inputData.size(),
                        (clock.millis() - start));
            } else {
                handleAlgoNotInitialized("Handle Input LVEErgebnisVersion4:");
            }
        }
    }

    private void handleVersion5Data(PLVEErgebnisVersion5List data) {
        if(isInitialized && data != null && data.getElementsList() != null && !data.getElementsList().isEmpty()) {
            List<AbstractData> inputData = data.getElementsList().stream()
                    .filter(d -> checkIntervalLength(d.getIntervalllaenge(), d.getId()))
                    .peek(d -> log.trace(getIdMarker(d.getId()),"LVEErgebnisVersion5 received")) // NOSONAR just for debugging
                    .map(TlsInputData::new)
                    .collect(Collectors.toList());
            if (algo != null) {
                long start = clock.millis();
                algo.setData(inputData);
                log.trace(logMarker1,
                        "{} LVEErgebnisVersion5 processed within {} millis.", inputData.size(),
                        (clock.millis() - start));
            } else {
                handleAlgoNotInitialized("Handle Input LVEErgebnisVersion5:");
            }        
        }
    }

    private void handleVersion6Data(PLVEErgebnisVersion6List data) {
        if(isInitialized && data != null && data.getElementsList() != null && !data.getElementsList().isEmpty()) {
            List<AbstractData> inputData = data.getElementsList().stream()
                    .filter(d -> checkIntervalLength(d.getIntervalllaenge(), d.getId()))
                    .peek(d -> log.trace(getIdMarker(d.getId()),"LVEErgebnisVersion6 received")) // NOSONAR just for debugging
                    .map(TlsInputData::new).collect(Collectors.toList());
            if (algo != null) {
                long start = clock.millis();
                algo.setData(inputData);
                log.trace(logMarker1,
                        "{} LVEErgebnisVersion6 processed within {} millis.", inputData.size(),
                        (clock.millis() - start));
            } else {
                handleAlgoNotInitialized("Handle Input LVEErgebnisVersion6:");
            }
        }
    }
    
    private void handleSingleVehicleData(PLVEKfzEinzeldatenList data) {
        if(isInitialized && data != null && data.getElementsList() != null && !data.getElementsList().isEmpty()) {
            setCounter("LVEKfzEinzeldaten", data.getElementsList().size());
            List<SingleVehicleData> inputData = data.getElementsList().stream()
                    .peek(d -> log.trace(getIdMarker(d.getId()),"LVEKfzEinzeldaten received")) // NOSONAR just for debugging
                    .map(sv -> new SingleVehicleData(sv.getId(), sv.getTlsTime(), sv.getFahrzeugklassencode(),
                            sv.getGeschwindigkeit(), sv.getStatus(), 0))
                    .collect(Collectors.toList());
            if (algo != null) {
                long start = clock.millis();
                inputData.forEach(algo::setSingleVehicleData);
                log.trace(logMarker1,
                        "{} LVEKfzEinzeldaten processed within {} millis.", inputData.size(),
                        (clock.millis() - start));
            } else {
                handleAlgoNotInitialized("Handle Input LVEKfzEinzeldaten:");
            }
        }
    }

    private void handleSingleVehicleCollectionData(PLVEKfzEinzeldatenSammelmeldungList data) {
        if (algo != null) {
            long start = clock.millis();
            if(isInitialized && data != null && data.getElementsList() != null && !data.getElementsList().isEmpty()) {
                setCounter("LVEKfzEinzeldatenSammelmeldung", data.getElementsList().size());
                data.getElementsList().forEach(c -> {
                    log.trace(getIdMarker(c.getId()),"LVEKfzEinzeldatenSammelmeldung received ({} single data)",
                            c.getDatensaetzeList()!=null?c.getDatensaetzeList().size():"0"); // NOSONAR just for debugging
                    if(c.getDatensaetzeList() != null) {
                        c.getDatensaetzeList().forEach(sv -> {
                            long offsetMs = Math.round(sv.getZeitoffset() * 1000);
                            if(c.getId() != null && c.getTlsTime() != null) {
                                algo.setSingleVehicleData(new SingleVehicleData(
                                        c.getId(),
                                        c.getTlsTime().plusSeconds(offsetMs),
                                        sv.getFahrzeugklassencode(),
                                        sv.getGeschwindigkeit(),
                                        sv.getStatus(),
                                        0));
                            }
                        });
                    }
                });
                log.trace(logMarker1,"{} LVEKfzEinzeldatenSammelmeldung processed within {} millis.",
                        data.getElementsList().size(), (clock.millis() - start));
            } else {
                handleAlgoNotInitialized("Handle Input LVEKfzEinzeldatenSammelmeldung:");
            }
        }
    }
    
    private void handleAlgoNotInitialized(String error) {
        log.debug(error + ALGO_NOT_INITIALIZED);
    }
    
    private boolean checkIntervalLength(int intervalLengthCount, String id) {
        IntervalLengthValue ilv = IntervalLengthValue.getIntervalLengthValue(intervalLengthCount);
        if(ilv != null && ilv != IntervalLengthValue.UNDEFINED) {
            return true;
        } else {
            log.debug (getIdMarker(id), "interval length not allowed");
            return false;
        }
    }

    private void handleIntervalEndTrigger(IntervalEndTriggerData d) {
        if (d != null) {
            String time = f.format(d.getIntervalEnd());
            log.info(logMarker1, "Got interval end trigger for interval end: {}", time);
            algo.intervalEndTrigger(d.getIntervalEnd(), d.getIntervalLenghts());
        }
    }

    private void handleIntervalTimeoutTrigger(IntervalTimeoutTriggerData d) {
        if (d != null) {
            String timeOffset = f.format(d.getTimeout());
            String timeIntvlEnd = f.format(d.getIntervalEnd());
            log.info(logMarker1, "Got timeout trigger for interval end: {} and timeout end: {}", timeIntvlEnd, timeOffset);
            algo.intervalTimoutTrigger(d.getTimeout(), d.getIntervalEnd(), d.getIntervalLengths());
        }
    }
    
    private void resetTimerTimeout() {
        if(this.schedTimeout != null) {
            try {
                log.info(logMarker1, "shutdown previous timer 'timeout'.");
                this.schedTimeout.shutdown();
            } catch (SchedulerException e) {
                log.warn(logMarker1, "shutdown of previous timer 'timeout' was not successful!", e);
            }
        }
        schedTimeout = null;
    }
    
    private void resetTimerIntervalEnd() {
        if(this.schedIntervalEnd != null) {
            try {
                log.info(logMarker1, "shutdown previous timer 'intervalend'.");
                this.schedIntervalEnd.shutdown();
            } catch (SchedulerException e) {
                log.warn(logMarker1, "shutdown of previous timer 'intervalend' was not successful!", e);
            }
        }
        schedIntervalEnd = null;
    } 
    
    private Marker getIdMarker(String id) {
        return (MarkerFactory.getMarker(id));
    }
}
