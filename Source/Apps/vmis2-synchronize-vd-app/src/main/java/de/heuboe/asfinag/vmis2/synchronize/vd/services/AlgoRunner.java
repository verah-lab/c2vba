package de.heuboe.asfinag.vmis2.synchronize.vd.services;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import jakarta.annotation.PostConstruct;

import de.heuboe.asfinag.control.base.config.CheckerHealthIndicator;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import com.google.protobuf.MessageOrBuilder;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import eu.vmis_ehe.vmis2.configservice.pojo.PDataChanges;
import de.heuboe.asfinag.control.base.actors.AbstractParameterActor;
import de.heuboe.asfinag.control.base.actors.ExitingSupervisorActor;
import de.heuboe.asfinag.control.base.actors.ParameterActor;
import de.heuboe.asfinag.control.base.actors.SpringExtension;
import de.heuboe.asfinag.control.base.services.DebugWriter;
import de.heuboe.asfinag.control.base.services.InitialTopicReader;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureManager;
import de.heuboe.asfinag.vmis2.synchronize.vd.config.AlgoParameterIdProperties;
import de.heuboe.asfinag.vmis2.synchronize.vd.config.SynchronizeVdProperties;
import eu.vmis_ehe.vmis2.paramservice.pojo.PParameterSetList;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEBetriebsparameterList;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEDeFehlerList;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion0List;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion1List;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion2List;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion3List;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion4List;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion5List;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion6List;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEGeschwindigkeitsklassenKurzList;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEKanalsteuerungList;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEKfzEinzeldatenList;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEKfzEinzeldatenSammelmeldungList;
import eu.vmis_ehe.vmis2.tls.received.pojo.PSYSFehlerDUEList;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.duration.Duration;
/**
 * Class to receive data from kafka, to create actors for each roads/situation class combination and
 * to map received data to the right actor.
 */
@Slf4j
public class AlgoRunner implements ConsumerSeekAware {
    
    public static final String PARA_ACTOR_NAME_SYSTEM = "Synchronize-System-Param";
    public static final String PARA_ACTOR_NAME_ROAD = "Synchronize-Road-Param";

    private static final String BEAN_NAME_PARAMETER_ACTOR = "parameterActor";
    private static final String BEAN_NAME_SUPERVISOR_ACTOR = "supervisorActor";
    private static final String TOPIC_KEY_PAYLOAD = "received topic = '{}' key='{}' payload='{}'";

    @Autowired
    private SpringExtension springExtension;
    
    @Autowired
    private ConsumerFactory<String, byte[]> consumerFactory;

    private Marker logMarker = MarkerFactory.getMarker("AlgoRunner ");

    private ActorSystem actorSystem;
    private InfrastructureManager infrastructure;
    private ActorRef paramSystemActorTimeSync;
    private ActorRef paramRoadActorLogicalPassive;
    private SynchronizeVdProperties properties;
    private AlgoParameterIdProperties paramIds;
    private AlgoContext algoContext;
    
    private InitialTopicReader<PLVEDeFehlerList> tlsErrorInit;
    private InitialTopicReader<PLVEBetriebsparameterList> tlsOpParamInit;
    private InitialTopicReader<PLVEKanalsteuerungList> tlsChControlInit;
    private InitialTopicReader<PSYSFehlerDUEList> tlsSysErrorInit;
    private InitialTopicReader<PLVEGeschwindigkeitsklassenKurzList> tlsTrCatInit;

    @Autowired
    private CheckerHealthIndicator healthIndicator;

    
/**
     * Constructor for the AlgoRunner
     * 
     * @param actorSystem       the actor system to run actors
     * @param properties        application properties.
     * @param paramIds          parameter id properties.
     * @param algoContext       the algoContext.
     * @param infrastructure    the infrastructure.
     */
    public AlgoRunner(ActorSystem actorSystem, 
            SynchronizeVdProperties properties,
            AlgoParameterIdProperties paramIds,
            AlgoContext algoContext,
            InfrastructureManager infrastructure) {
        this.actorSystem = actorSystem;
        this.properties = properties;
        this.paramIds = paramIds;
        this.algoContext = algoContext;
        this.infrastructure = infrastructure;
    }

    /**
     * Initialize algorithm
     */
    @PostConstruct // Run after Autowired has been injected
    public void init() {
        // AbstractParameterActors for parameter from parameter service
        // Parameter time synchronization (timeouts etc. for different interval length)
        // system wide
        String system;
        // Working for all(centreId=-ALL-) UZten that the configService knows
        if (properties.getCentreId().equals(properties.getCentreIdAllUZ())) {
            system = properties.getSystemWideShortcut();
        } else {
            system = properties.getCentreId();
        }
        if (paramSystemActorTimeSync == null) {
            List<String> paraSystemTopics = calculateTopicPattern(properties.getParameterSystemTopics());

            Set<Tuple2<String, String>> algos2Handle = Set
                    .of(Tuple.of(paramIds.getTimeSyncDefSetId(), properties.getInstanceName()));
            AbstractParameterActor.InstanceHandler<PParameterSetList> instanceHandler = new ParameterActor.UZDefaultInstanceHandler(
                    system, algos2Handle);
            paramSystemActorTimeSync = actorSystem.actorOf(
                    springExtension.props(BEAN_NAME_PARAMETER_ACTOR, instanceHandler,
                            paraSystemTopics, PARA_ACTOR_NAME_SYSTEM),
                    "UZDefaultSyncParameterActor");
            log.info("ParameterSystemActor for time synchronisation({}) created!", paraSystemTopics);
        }
        // Parameter logical passivation per system and road
        if (paramRoadActorLogicalPassive == null) {
            List<String> paraRoadTopics = calculateTopicPattern(properties.getParameterRoadTopics());

            Set<Tuple2<String, String>> algos2Handle = Set
                    .of(Tuple.of(paramIds.getLogPassiveDefSetId(), properties.getInstanceName()));

            AbstractParameterActor.InstanceHandler<PParameterSetList> instanceHandler = new ParameterActor.DefaultInstanceHandler(
                    system, properties.getStreets(), algos2Handle);
            paramRoadActorLogicalPassive = actorSystem.actorOf(springExtension.props(BEAN_NAME_PARAMETER_ACTOR,
                    instanceHandler, paraRoadTopics, PARA_ACTOR_NAME_ROAD), "ParameterRoadActor");
            log.info("ParameterRoadActor(filter road) for logical passivation({}) created!", paraRoadTopics);
        }

        // Initial topic readers for TLS parameter, TLS errors etc.
        this.tlsErrorInit = new InitialTopicReader<>(algoContext.getTopicTlsError(), consumerFactory);
        this.tlsSysErrorInit = new InitialTopicReader<>(algoContext.getTopicTlsSysError(), consumerFactory);
        this.tlsOpParamInit = new InitialTopicReader<>(algoContext.getTopicTlsOperatingParam(), consumerFactory);
        this.tlsChControlInit = new InitialTopicReader<>(algoContext.getTopicTlsChannelControl(), consumerFactory);
        this.tlsTrCatInit = new InitialTopicReader<>(algoContext.getTopicTlsTrafficCategoriesParam(), consumerFactory);
        List<InitialTopicReader<?>> initialTopicReaders = Arrays.asList(tlsErrorInit, tlsSysErrorInit, tlsOpParamInit,
                tlsChControlInit, tlsTrCatInit);

        ActorRef exitingSupervisorActor = actorSystem.actorOf(ExitingSupervisorActor.props(
                springExtension.props(BEAN_NAME_SUPERVISOR_ACTOR, this.infrastructure,
                        this.paramRoadActorLogicalPassive, this.paramSystemActorTimeSync, initialTopicReaders),
                "TheSupervisorActor", 1, Duration.create(1, TimeUnit.SECONDS)));

        healthIndicator.setActorRef(exitingSupervisorActor);
    }

    // Listen to administrative topics.

    /**
     * Listen to DE-errors.
     * 
     * @param data      a list of vehicle data.
     * @param key       key of message (detector id).
     * @param partition partition of message.
     * @param offset    offset of message.
     * @param topic     topic of message.
     */
    @KafkaListener(id = "LVEDeFehlerList", groupId = "${spring.kafka.consumer.group-id}", topicPattern = "#{algoContext.topicTlsError}", clientIdPrefix = "${spring.kafka.client-id}.receiveLVEDeFehlerList")
    public void receiveTlsError(@Payload PLVEDeFehlerList data, @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition, @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        log.debug(logMarker, TOPIC_KEY_PAYLOAD, topic, key, data);

        if (properties.isWriteFileLog()) {
            writeFileLog("pLVEDeFehlerList", PLVEDeFehlerList.to(data));
        }

        if (tlsErrorInit.isInitialized(data, topic, partition, key, offset,
                msg -> actorSystem.eventStream().publish(msg.getProtoObj()))) {
            actorSystem.eventStream().publish(data);
        }
        actorSystem.eventStream().publish(data);
    }

    /**
     * Listen to SYSFehlerDUE.
     * 
     * @param data      a list of vehicle data.
     * @param key       key of message (detector id).
     * @param partition partition of message.
     * @param offset    offset of message.
     * @param topic     topic of message.
     */
    @KafkaListener(id = "SYSFehlerDUEList", groupId = "${spring.kafka.consumer.group-id}", topicPattern = "#{algoContext.topicTlsSysError}", clientIdPrefix = "${spring.kafka.client-id}.receiveSYSFehlerDUEList")
    public void receiveTlsSysError(@Payload PSYSFehlerDUEList data, @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition, @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        log.debug(logMarker, TOPIC_KEY_PAYLOAD, topic, key, data);

        if (properties.isWriteFileLog()) {
            writeFileLog("pSYSFehlerDUEList", PSYSFehlerDUEList.to(data));
        }

        if (tlsSysErrorInit.isInitialized(data, topic, partition, key, offset,
                msg -> actorSystem.eventStream().publish(msg.getProtoObj()))) {
            actorSystem.eventStream().publish(data);
        }
        actorSystem.eventStream().publish(data);
    }

    /**
     * Listen to channel controls of vehicle data.
     * 
     * @param data      a list of vehicle data.
     * @param key       key of message (detector id).
     * @param partition partition of message.
     * @param offset    offset of message.
     * @param topic     topic of message.
     */
    @KafkaListener(id = "LVEKanalsteuerungList", groupId = "${spring.kafka.consumer.group-id}", topicPattern = "#{algoContext.topicTlsChannelControl}", clientIdPrefix = "${spring.kafka.client-id}.receiveLVEKanalsteuerungList")
    public void receiveTlsChannelControl(@Payload PLVEKanalsteuerungList data,
            @Header(KafkaHeaders.RECEIVED_KEY) String key, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        log.debug(logMarker, TOPIC_KEY_PAYLOAD, topic, key, data);

        if (properties.isWriteFileLog()) {
            writeFileLog("pLVEKanalsteuerungList", PLVEKanalsteuerungList.to(data));
        }

        if (tlsChControlInit.isInitialized(data, topic, partition, key, offset,
                msg -> actorSystem.eventStream().publish(msg.getProtoObj()))) {
            actorSystem.eventStream().publish(data);
        }
        actorSystem.eventStream().publish(data);
    }

    /**
     * Listen to traffic categories parameters of vehicle data.
     * 
     * @param data      a list of vehicle data.
     * @param key       key of message (detector id).
     * @param partition partition of message.
     * @param offset    offset of message.
     * @param topic     topic of message.
     */
    @KafkaListener(id = "LVEBetriebsparameterList", groupId = "${spring.kafka.consumer.group-id}", topicPattern = "#{algoContext.topicTlsOperatingParam}", clientIdPrefix = "${spring.kafka.client-id}.receiveLVEBetriebsparameterList")
    public void receiveTlsChannelControl(@Payload PLVEBetriebsparameterList data,
            @Header(KafkaHeaders.RECEIVED_KEY) String key, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        log.debug(logMarker, TOPIC_KEY_PAYLOAD, topic, key, data);

        if (properties.isWriteFileLog()) {
            writeFileLog("pLVEBetriebsparameterList", PLVEBetriebsparameterList.to(data));
        }

        if (tlsOpParamInit.isInitialized(data, topic, partition, key, offset,
                msg -> actorSystem.eventStream().publish(msg.getProtoObj()))) {
            actorSystem.eventStream().publish(data);
        }
        actorSystem.eventStream().publish(data);
    }

    /**
     * Listen to operating parameters of vehicle data.
     * 
     * @param data      a list of vehicle data.
     * @param key       key of message (detector id).
     * @param partition partition of message.
     * @param offset    offset of message.
     * @param topic     topic of message.
     */
    @KafkaListener(id = "LVEGeschwindigkeitsklassenKurzList", groupId = "${spring.kafka.consumer.group-id}", topicPattern = "#{algoContext.topicTlsTrafficCategoriesParam}", clientIdPrefix = "${spring.kafka.client-id}.receiveLVEGeschwindigkeitsklassenKurzList")
    public void receiveTlsTrafficCategories(@Payload PLVEGeschwindigkeitsklassenKurzList data,
            @Header(KafkaHeaders.RECEIVED_KEY) String key, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        log.debug(logMarker, TOPIC_KEY_PAYLOAD, topic, key, data);

        if (properties.isWriteFileLog()) {
            writeFileLog("pLVEGeschwindigkeitsklassenKurzList", PLVEGeschwindigkeitsklassenKurzList.to(data));
        }

        if (tlsTrCatInit.isInitialized(data, topic, partition, key, offset,
                msg -> actorSystem.eventStream().publish(msg.getProtoObj()))) {
            actorSystem.eventStream().publish(data);
        }
        actorSystem.eventStream().publish(data);
    }

    /**
     * Listen to recognized tls data.
     * 
     * @param pTlsDataVersion0List a list of tls data.
     * @param topic                topic of message
     */
    @KafkaListener(id = "PLVEErgebnisVersion0List", groupId = "${spring.kafka.consumer.group-id}", topicPattern = "#{algoContext.topicTlsData0}", clientIdPrefix = "${spring.kafka.client-id}.receivePLVEErgebnisVersion0List")
    public void receiveNotCompacted(@Payload PLVEErgebnisVersion0List pTlsDataVersion0List,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        actorSystem.eventStream().publish(pTlsDataVersion0List);
    }

    /**
     * Listen to recognized tls data.
     * 
     * @param pTlsDataVersion1List a list of tls data.
     * @param topic                topic of message
     */
    @KafkaListener(id = "PLVEErgebnisVersion1List", groupId = "${spring.kafka.consumer.group-id}", topicPattern = "#{algoContext.topicTlsData1}", clientIdPrefix = "${spring.kafka.client-id}.receivePLVEErgebnisVersion1List")
    public void receiveNotCompacted(@Payload PLVEErgebnisVersion1List pTlsDataVersion1List,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        actorSystem.eventStream().publish(pTlsDataVersion1List);
    }

    /**
     * Listen to recognized tls data.
     * 
     * @param pTlsDataVersion2List a list of tls data.
     * @param topic                topic of message
     */
    @KafkaListener(id = "PLVEErgebnisVersion2List", groupId = "${spring.kafka.consumer.group-id}", topicPattern = "#{algoContext.topicTlsData2}", clientIdPrefix = "${spring.kafka.client-id}.receivePLVEErgebnisVersion2List")
    public void receiveNotCompacted(@Payload PLVEErgebnisVersion2List pTlsDataVersion2List,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        actorSystem.eventStream().publish(pTlsDataVersion2List);
    }

    /**
     * Listen to recognized tls data.
     * 
     * @param pTlsDataVersion3List a list of tls data.
     * @param topic                topic of message
     */
    @KafkaListener(id = "PLVEErgebnisVersion3List", groupId = "${spring.kafka.consumer.group-id}", topicPattern = "#{algoContext.topicTlsData3}", clientIdPrefix = "${spring.kafka.client-id}.receivePLVEErgebnisVersion3List")
    public void receiveNotCompacted(@Payload PLVEErgebnisVersion3List pTlsDataVersion3List,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        actorSystem.eventStream().publish(pTlsDataVersion3List);
    }

    /**
     * Listen to recognized tls data.
     * 
     * @param pTlsDataVersion4List a list of tls data.
     * @param topic                topic of message
     */
    @KafkaListener(id = "PLVEErgebnisVersion4List", groupId = "${spring.kafka.consumer.group-id}", topicPattern = "#{algoContext.topicTlsData4}", clientIdPrefix = "${spring.kafka.client-id}.receivePLVEErgebnisVersion4List")
    public void receiveNotCompacted(@Payload PLVEErgebnisVersion4List pTlsDataVersion4List,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        actorSystem.eventStream().publish(pTlsDataVersion4List);
    }

    /**
     * Listen to recognized tls data.
     * 
     * @param pTlsDataVersion5List a list of tls data.
     * @param topic                topic of message
     */
    @KafkaListener(id = "PLVEErgebnisVersion5List", groupId = "${spring.kafka.consumer.group-id}", topicPattern = "#{algoContext.topicTlsData5}", clientIdPrefix = "${spring.kafka.client-id}.receivePLVEErgebnisVersion5List")
    public void receiveNotCompacted(@Payload PLVEErgebnisVersion5List pTlsDataVersion5List,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        actorSystem.eventStream().publish(pTlsDataVersion5List);
    }

    /**
     * Listen to recognized tls data.
     * 
     * @param pTlsDataVersion6List a list of tls data.
     * @param topic                topic of message
     */
    @KafkaListener(id = "PLVEErgebnisVersion6List", groupId = "${spring.kafka.consumer.group-id}", topicPattern = "#{algoContext.topicTlsData6}", clientIdPrefix = "${spring.kafka.client-id}.receivePLVEErgebnisVersion6List")
    public void receiveNotCompacted(@Payload PLVEErgebnisVersion6List pTlsDataVersion6List,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        actorSystem.eventStream().publish(pTlsDataVersion6List);
    }

    /**
     * Listen to recognized tls single vehicle data.
     * 
     * @param pLVEKfzEinzeldatenList a list of tls single vehicle data.
     * @param topic                  topic of message
     */
    @KafkaListener(id = "PLVEKfzEinzeldatenList", autoStartup = "#{algoContext.listenSingleVehicleData}", groupId = "${spring.kafka.consumer.group-id}", topicPattern = "#{algoContext.topicTlsSingleVehicleData}", clientIdPrefix = "${spring.kafka.client-id}.receivePLVEKfzEinzeldatenList")
    public void receiveNotCompacted(@Payload PLVEKfzEinzeldatenList pLVEKfzEinzeldatenList,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        actorSystem.eventStream().publish(pLVEKfzEinzeldatenList);
    }

    /**
     * Listen to recognized tls single vehicle data.
     * 
     * @param pLVEKfzEinzeldatenSammelmeldungList a list of tls single vehicle data.
     * @param topic                               topic of message
     */
    @KafkaListener(id = "PLVEKfzEinzeldatenSammelmeldungList", autoStartup = "#{algoContext.listenSingleVehicleData}", groupId = "${spring.kafka.consumer.group-id}", topicPattern = "#{algoContext.topicTlsSingleVehicleCollectedData}", clientIdPrefix = "${spring.kafka.client-id}.receivePLVEKfzEinzeldatenSammelmeldungList")
    public void receiveNotCompacted(@Payload PLVEKfzEinzeldatenSammelmeldungList pLVEKfzEinzeldatenSammelmeldungList,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        actorSystem.eventStream().publish(pLVEKfzEinzeldatenSammelmeldungList);
    }

    /**
     * Listen to triggers for DataChanges from ConfigService
     *
     * @param pDataChanges msg with Config DataChanges
     * @param key          key of message
     * @param partition    partition of message
     * @param topic        topic of message
     */
    @KafkaListener(id = "DataChanges", groupId = "${spring.kafka.consumer.group-id}", topicPattern = "#{algoContext.topicDataChange}", clientIdPrefix = "${spring.kafka.client-id}.receiveDataChange")
    public void receiveDataChange(@Payload PDataChanges pDataChanges, @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        actorSystem.eventStream().publish(pDataChanges);
    }

    @Override
    public void registerSeekCallback(ConsumerSeekCallback callback) {
        // nothing to do here
    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        String topicsRegexp = algoContext.getTopicTlsData0() + "|" + algoContext.getTopicTlsData1() + "|"
                + algoContext.getTopicTlsData2() + "|" + algoContext.getTopicTlsData3() + "|"
                + algoContext.getTopicTlsData4() + "|" + algoContext.getTopicTlsData5() + "|"
                + algoContext.getTopicTlsData6();

        Pattern topicRexExpPattern = Pattern.compile(topicsRegexp);
        assignments.keySet().stream().filter(partition -> topicRexExpPattern.matcher(partition.topic()).matches())
                .peek(partition -> // NOSONAR just for debugging
                log.info("Partition assigned: Topic: {} Partition: {} -> seekToEnd!", partition.topic(),
                        partition.partition()))
                .forEach(partition -> callback.seekToEnd(partition.topic(), partition.partition()));

        tlsErrorInit.onPartitionsAssigned(assignments, callback);
        tlsSysErrorInit.onPartitionsAssigned(assignments, callback);
        tlsOpParamInit.onPartitionsAssigned(assignments, callback);
        tlsChControlInit.onPartitionsAssigned(assignments, callback);
        tlsTrCatInit.onPartitionsAssigned(assignments, callback);
    }

    @Override
    public void onIdleContainer(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        // nothing to do here
    }

    private void writeFileLog(String typeName, MessageOrBuilder msg) {
        File outDir = new File(properties.getFileLogPath());
        File outDirMS = new File(outDir, "Input");
        outDirMS.mkdirs();
        File outFile = new File(outDirMS, typeName + "-" + System.currentTimeMillis() + DebugWriter.JSON_FILE);
        DebugWriter.proto2file(outFile, msg);
    }
    
    private List<String> calculateTopicPattern(List<String> topicTemplates) {
        
        return topicTemplates.stream()
                .map(tp -> tp.replace("{systemWideShortcut}", properties.getSystemWideShortcut()))
                .toList();
    }
}
