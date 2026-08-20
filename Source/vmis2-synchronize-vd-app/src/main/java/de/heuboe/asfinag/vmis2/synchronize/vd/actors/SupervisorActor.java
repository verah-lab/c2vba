package de.heuboe.asfinag.vmis2.synchronize.vd.actors;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import de.heuboe.asfinag.control.base.messages.HealthMessage;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.actor.Terminated;
import akka.japi.pf.DeciderBuilder;
import de.heuboe.asfinag.control.base.actors.AbstractParameterActor;
import de.heuboe.asfinag.control.base.actors.SpringExtension;
import de.heuboe.asfinag.control.base.services.InitialTopicReader;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureManager;
import de.heuboe.asfinag.vmis2.synchronize.vd.config.AlgoParameterIdProperties;
import de.heuboe.asfinag.vmis2.synchronize.vd.config.SynchronizeVdProperties;
import de.heuboe.asfinag.vmis2.synchronize.vd.services.InfrastructureFromSystem;
import eu.vmis_ehe.vmis2.configservice.pojo.PDataChange;
import eu.vmis_ehe.vmis2.configservice.pojo.PDataChanges;
import eu.vmis_ehe.vmis2.configservice.pojo.PItemChange;
import eu.vmis_ehe.vmis2.paramservice.pojo.PParameterSetList;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEBetriebsparameter;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEBetriebsparameterList;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEDeFehler;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEDeFehlerList;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEGeschwindigkeitsklassenKurz;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEGeschwindigkeitsklassenKurzList;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEKanalsteuerung;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEKanalsteuerungList;
import eu.vmis_ehe.vmis2.tls.received.pojo.PSYSFehlerDUE;
import eu.vmis_ehe.vmis2.tls.received.pojo.PSYSFehlerDUEList;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Actor to run situation merging for one road and situation class
 */
@Component
@Scope("prototype")
@Slf4j
public class SupervisorActor extends AbstractActorWithTimers {

    /**
     * Relevant actor details for restarting.
     */
    @RequiredArgsConstructor
    @Data
    private static class ActorDetails {
        @NonNull
        private ActorRef actorRef;
        @NonNull
        private AlgoActor.Init init;
        @NonNull
        private InfrastructureManager infrastructure;
        @Getter
        private List<Instant> restartTimes = new LinkedList<>();
        @Getter
        private int restartCounter = 0;

        /**
         * Adds a restart timestamp to actor details.
         *
         * @param now time of restart
         * @param timeWithin duration to watch regarding maximum restart number
         */
        public void addRestart(Instant now, Duration timeWithin) {
            restartTimes = restartTimes.stream().filter(t -> {
                Duration d = Duration.between(t, now);
                return d.getSeconds() < timeWithin.getSeconds();
            }).collect(Collectors.toList());
            restartTimes.add(now);
            restartCounter++;
        }
    }

    public static final String ACTOR_NAME_PREFIX = "AlgoActor_";
    public static final String ALGO_ACTOR = "algoActor";
    public static final String SINGLE_ACTOR = "SINGLE_ACTOR";
    public static final String PARAMETERS_ACTOR_ROAD = "parametersActorRoad";
    public static final String PARAMETERS_ACTOR_SYSTEM = "parametersActorSystem";
    private static final String TICK = "TICK";
    private static final long POLL_CYCLE = 1000L;
    private Marker logMarker = MarkerFactory.getMarker("SupervisorActor");

    @Autowired
    private Clock clock;

    @Autowired
    private SpringExtension springExtension;

    @Autowired
    private SynchronizeVdProperties properties;
    
    @Autowired
    private AlgoParameterIdProperties paramIds;

    @Autowired
    private ActorSystem system;

    private ActorRef paramRoadActorLogicalPassive;
    private ActorRef paramSystemActorTimeSync;
    boolean initialParametersReadRoad = false;
    boolean initialParametersReadSystem = false;
    private boolean isInitialized = false;

    private InfrastructureManager infrastructure;
    private Duration restartMaxDuration;

    private final List<InitialTopicReader<?>> initialTopicReaders;

    // key = actorRef name
    private Map<String, ActorDetails> actorDetails = new HashMap<>();
    private Map<String, PLVEDeFehler> tlsErrors = new HashMap<>();
    private Map<String, PSYSFehlerDUE> tlsSysErrors = new HashMap<>();
    private Map<String, PLVEBetriebsparameter> tlsOpParams = new HashMap<>();
    private Map<String, PLVEKanalsteuerung> tlsChControls = new HashMap<>();
    private Map<String, List<PLVEGeschwindigkeitsklassenKurz>> tlsTrafficCategories = new HashMap<>();
    //parameter logical passivation per roadId (String = roadId).
    private Map<String, AbstractParameterActor.Parameters<PParameterSetList>> paramsLogPassive = new HashMap<>();

    /**
     * Actor supervising the algo actor that handles the synchronize-vd-app. The actor coordinates the
     * start of the actor. The algo actor created only if all necessary data (parameters, input data,
     * etc.) has been received. Further handles this actor restarts of the algo Actor if it fails
     * (Exception) to often
     *
     * @param infrastructure the infrastructure.
     * @param paramRoadActorLogicalPassive ParameterActor for logical passivation per system and and road
     * @param paramSystemActorTimeSync ParameterActor for time synchronization (timeouts etc. for
     *        different interval length) system(UZ/rVMZ) wide
     * @param initialTopicReaders initial topic readers
     */
    public SupervisorActor(InfrastructureManager infrastructure, ActorRef paramRoadActorLogicalPassive,
            ActorRef paramSystemActorTimeSync, List<InitialTopicReader<?>> initialTopicReaders) {
        this.infrastructure = infrastructure;
        this.paramRoadActorLogicalPassive = paramRoadActorLogicalPassive;
        this.paramSystemActorTimeSync = paramSystemActorTimeSync;
        this.initialTopicReaders = initialTopicReaders;
    }

    @Override
    public void preStart() throws Exception {
        this.isInitialized = false;
        system.eventStream().subscribe(this.getSelf(), PLVEDeFehlerList.class);
        system.eventStream().subscribe(this.getSelf(), PSYSFehlerDUEList.class);
        system.eventStream().subscribe(this.getSelf(), PLVEBetriebsparameterList.class);
        system.eventStream().subscribe(this.getSelf(), PLVEKanalsteuerungList.class);
        system.eventStream().subscribe(this.getSelf(), PLVEGeschwindigkeitsklassenKurzList.class);
        system.eventStream().subscribe(this.getSelf(), AbstractParameterActor.Parameters.class);
        system.eventStream().subscribe(this.getSelf(), PDataChanges.class);
        getTimers().startSingleTimer(TICK, TICK, Duration.ofMillis(POLL_CYCLE));
        
        ActorRef algoActor =  context().actorOf(springExtension.props(ALGO_ACTOR), SINGLE_ACTOR);
        context().watch(algoActor); // Watch for termination message
        
        //creating the Actor Init msg
        AlgoActor.Init actorInit = new AlgoActor.Init(this.infrastructure,
                        this.paramSystemActorTimeSync,
                        this.paramRoadActorLogicalPassive,
                        this.tlsErrors.values().stream().collect(Collectors.toList()),
                        this.tlsSysErrors.values().stream().collect(Collectors.toList()),
                        this.tlsOpParams.values().stream().collect(Collectors.toList()),
                        this.tlsChControls.values().stream().collect(Collectors.toList()),
                        this.tlsTrafficCategories.values().stream().flatMap(List::stream).collect(Collectors.toList()));

        ActorDetails details = new ActorDetails(algoActor, actorInit, this.infrastructure);
        actorDetails.put(SINGLE_ACTOR, details);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                // timing ticker
                .matchEquals(TICK, t -> checkPreconditions())
                // parameters
                .match(AbstractParameterActor.AnswerInitialParametersRead.class,
                        this::handleInitialParametersReadAnswer)
                .match(AbstractParameterActor.Parameters.class, this::handleParameters)
                // termination
                .match(Terminated.class, this::handleTermination)
                //administrative
                .match(PDataChanges.class, this::handleDataChanges)
                .match(PLVEDeFehlerList.class, this::handleTlsErrors)
                .match(PSYSFehlerDUEList.class, this::handleTlsSysErrors)
                .match(PLVEBetriebsparameterList.class, this::handleTlsOpParams)
                .match(PLVEKanalsteuerungList.class, this::handleTlsChControls)
                .match(PLVEGeschwindigkeitsklassenKurzList.class, this::handleTrafficCategories)
                .match(HealthMessage.AskStatus.class, as -> sendStatus())
                .matchAny(
                        o -> log.error(logMarker,
                                "received unknown message of type {}. Couldn't handle ... discarding!",
                                o.getClass().getName())).build();
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        // TerminateActor after three Exceptions in 5 minutes.
        return new OneForOneStrategy(3, Duration.ofMinutes(5),
                DeciderBuilder.match(Throwable.class, throwable -> {
                    log.error(logMarker, "Exception from child actor: ", throwable);
                    return SupervisorStrategy.stop();
                }).build());
    }

    private void handleInitialParametersReadAnswer(AbstractParameterActor.AnswerInitialParametersRead a) {
        if (PARAMETERS_ACTOR_ROAD.equals(a.name())) {
            log.info("Initial parameter for logical passivation read: {}", a.name());
            initialParametersReadRoad = a.initialParametersRead();
        }
        
        if (PARAMETERS_ACTOR_SYSTEM.equals(a.name())) {
            log.info("Initial parameter for time synchronization read: {}", a.name());
            initialParametersReadSystem = a.initialParametersRead();
        }
    }

    private void checkPreconditions() {
        log.info(logMarker, "Checking preconditions to start synchronize-vd AlgoActor(s)");
        boolean itrsFinised = initialTopicReaders.stream().allMatch(InitialTopicReader::isInitialReadFinished);
       
        if (initialParametersReadRoad && initialParametersReadSystem && itrsFinised) {
            isInitialized = true;
            log.info(logMarker, "All initial parameters and data are read. Starting algo initializing.");

            // send the init Msg to all Actors
            for (Entry<String, ActorDetails> currentActorEntry : actorDetails.entrySet()) {

                currentActorEntry.getValue().getActorRef()
                        .tell(currentActorEntry.getValue().getInit(), ActorRef.noSender());

                log.info(logMarker, "AlgoActor {} successfully initialized.", currentActorEntry.getKey());
            }
        } else {
            if (!initialParametersReadRoad) {
                paramRoadActorLogicalPassive
                    .tell(new AbstractParameterActor.AskInitialParametersRead(PARAMETERS_ACTOR_ROAD), self());
            }
            if (!initialParametersReadSystem) {
                paramSystemActorTimeSync
                    .tell(new AbstractParameterActor.AskInitialParametersRead(PARAMETERS_ACTOR_SYSTEM), self());
            }

            log.info(logMarker,
                    "Preconditions to start AlgoActor are not fulfilled. ParameterActorRoad logical passivation initial read: {},"
                     + " ParameterActorSystem time synchronization initial read: {}, InitialTopicReaders: {}",
                    initialParametersReadRoad, initialParametersReadSystem, itrsFinised);
            getTimers().startSingleTimer(TICK, TICK, Duration.ofMillis(POLL_CYCLE));
        }
    }

    private void sendStatus() {
        String failure = "";

        HealthMessage.Status status = HealthMessage.Status.NOT_OK;
        if (isInitialized) {
            status = HealthMessage.Status.OK;
        } else {
            failure = "Waiting for parameter and initialTopicReader.";
        }

        HealthMessage.AppStatus overallStatus = HealthMessage.AppStatus.builder()
                .status(status)
                .failureMessage(failure)
                .build();

        sender().tell(overallStatus, self());
    }

    private void handleTlsErrors(PLVEDeFehlerList data) {
        if(data != null && data.getElementsList() != null) {
            data.getElementsList().forEach(d -> tlsErrors.put(d.getId(), d));
        }
        updateActorDetail(SINGLE_ACTOR);
    }

    private void handleTlsSysErrors(PSYSFehlerDUEList data) {
        if(data != null && data.getElementsList() != null) {
            data.getElementsList().forEach(d -> tlsSysErrors.put(d.getId(), d));
        }
        updateActorDetail(SINGLE_ACTOR);
    }

    private void handleTlsOpParams(PLVEBetriebsparameterList data) {
        if(data != null && data.getElementsList() != null) {
            data.getElementsList().forEach(d -> tlsOpParams.put(d.getId(), d));
        }
        updateActorDetail(SINGLE_ACTOR);
    }

    private void handleTlsChControls(PLVEKanalsteuerungList data) {
        if(data != null && data.getElementsList() != null) {
            data.getElementsList().forEach(d -> tlsChControls.put(d.getId(), d));
        }
        updateActorDetail(SINGLE_ACTOR);
    }
    
    private void handleTrafficCategories(PLVEGeschwindigkeitsklassenKurzList data) {
        if(data != null && data.getElementsList() != null) {
            data.getElementsList().forEach(d -> {
                tlsTrafficCategories.computeIfAbsent(d.getId(), l -> new ArrayList<>()).add(d);
            });
        }
        updateActorDetail(SINGLE_ACTOR);
    }    
    private void handleParameters(AbstractParameterActor.Parameters<PParameterSetList> p) {
        if(p != null && p.algo() != null) {
            if(paramIds.getTimeSyncDefSetId().equals(p.algo())) {
                handleTimeSyncParameter();
            } else if(paramIds.getLogPassiveDefSetId().equals(p.algo())) {
                handleLogPassParameter(p);
            }
        }
    }
    
    private void handleTimeSyncParameter() {
        updateActorDetail(SINGLE_ACTOR);
    }
    
    private void handleLogPassParameter(AbstractParameterActor.Parameters<PParameterSetList> p) {
        if(p != null) {
            this.paramsLogPassive.put(p.roadId(), p);
        }
        updateActorDetail(SINGLE_ACTOR);
    }
    
    private void updateActorDetail(String actorId) {
        ActorDetails actorDetail = actorDetails.get(actorId);
        //update the Init Msg of this actor 
        AlgoActor.Init actorInitUpdate = new AlgoActor.Init(
                this.infrastructure,
                this.paramSystemActorTimeSync,
                this.paramRoadActorLogicalPassive,
                this.tlsErrors.values().stream().collect(Collectors.toList()),
                this.tlsSysErrors.values().stream().collect(Collectors.toList()),
                this.tlsOpParams.values().stream().collect(Collectors.toList()),
                this.tlsChControls.values().stream().collect(Collectors.toList()),
                this.tlsTrafficCategories.values().stream().flatMap(List::stream).collect(Collectors.toList()));
        actorDetail.setInit(actorInitUpdate);
        actorDetails.put(actorId, actorDetail);
    }
    
    private  void handleDataChanges(PDataChanges data) {
        if (data.isAllChanged()) {
            log.info(logMarker, "DataChange with allChanged=true, update infrastructure!");
            ((InfrastructureFromSystem) infrastructure).init();
            doInfrastructureUpdate();
        } else {
            if (data.getDataChangesList().stream().anyMatch(this::containsRelevantChanges)
                    && infrastructure instanceof InfrastructureFromSystem) {
                log.info(logMarker,
                        "Received new PDataChanges (version: '{}') with relevant changes, reload infrastructure...",
                        data.getVersion());
                ((InfrastructureFromSystem) infrastructure).init();
                doInfrastructureUpdate();
            } else {
                log.info(logMarker,
                        "Received new PDataChanges (version: '{}'), but no relevant changes detected. Ignore.",
                        data.getVersion());
            }
        }
    }

    private boolean containsRelevantChanges(PDataChange dc) {
        // If you want to work with all UZten contained in the configuration, the rVmzId
        // is not checked.
        return properties.getCentreId().equals(properties.getCentreIdAllUZ())
                || dc.getRVmzId().equals(properties.getCentreId())
                        && dc.getRoadChangesList().stream()
                                .anyMatch(rc -> rc.getFeatureChangesList().stream()
                                        .anyMatch(this::checkType));
    }

    private boolean checkType(PItemChange fc) {
        return fc.getItemTypesList().stream()
                .anyMatch(itemtype -> properties.getDataChangeType().contains(itemtype.name()));
    }
    
    private void doInfrastructureUpdate() {
        log.info(logMarker, "Reinit algo actor after config update.");
        ActorDetails det = actorDetails.get(SINGLE_ACTOR);
        ActorRef algoActor = det.getActorRef();
        det.setInfrastructure(this.infrastructure);
        
        AlgoActor.Init actorInit = new AlgoActor.Init(
                this.infrastructure,
                this.paramSystemActorTimeSync,
                this.paramRoadActorLogicalPassive,
                this.tlsErrors.values().stream().collect(Collectors.toList()),
                this.tlsSysErrors.values().stream().collect(Collectors.toList()),
                this.tlsOpParams.values().stream().collect(Collectors.toList()),
                this.tlsChControls.values().stream().collect(Collectors.toList()),
                this.tlsTrafficCategories.values().stream().flatMap(List::stream).collect(Collectors.toList()));
        det.setInit(actorInit);
        actorDetails.put(SINGLE_ACTOR, det);
        algoActor.tell(actorInit, ActorRef.noSender());
    }
    
    private void handleTermination(Terminated terminated) {
        log.warn(logMarker, "Child {} is terminated! Try to restart actor.", terminated.actor().path());

        if (restartMaxDuration == null) {
            restartMaxDuration = Duration.ofMinutes(properties.getRestartsWithinTimeRange());
        }

        String terminatedActorname = terminated.actor().path().name();
        for (Map.Entry<String, ActorDetails> actorEntry : actorDetails.entrySet()) {
            if(actorEntry.getKey().equals(terminatedActorname)) {
                ActorDetails details = actorEntry.getValue();

                // creating the actor 
                ActorRef algoActor =
                        context().actorOf(springExtension.props(ALGO_ACTOR), terminatedActorname);
                context().watch(algoActor);

                details.setActorRef(algoActor);
                algoActor.tell(details.getInit(), ActorRef.noSender());
                details.addRestart(Instant.now(clock), restartMaxDuration);
                log.warn(logMarker, "Number of restarts of actor {} since app start: {}", actorEntry.getKey(),
                        details.getRestartCounter());
                if (properties.getRestartsWithinTimeRange() > 0
                        && details.getRestartTimes().size() >= properties.getMaxNrOfRestartRetries()) {
                    log.error(logMarker, "Exit app because of reaching maximum number ({}) of restart retries for"
                            + " actor {}", properties.getMaxNrOfRestartRetries(), actorEntry.getKey());
                    System.exit(1);
                }
                return;
            }
        }
    }
}
