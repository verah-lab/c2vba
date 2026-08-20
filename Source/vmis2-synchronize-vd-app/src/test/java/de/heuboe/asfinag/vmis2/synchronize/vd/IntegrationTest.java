package de.heuboe.asfinag.vmis2.synchronize.vd;

import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.kafka.test.utils.ContainerTestUtils.waitForAssignment;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import akka.actor.ActorSystem;
import de.heuboe.asfinag.control.base.actors.SpringExtension;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureManager;
import de.heuboe.asfinag.vmis2.synchronize.vd.config.AlgoParameterIdProperties;
import de.heuboe.asfinag.vmis2.synchronize.vd.config.SynchronizeVdProperties;
import de.heuboe.asfinag.vmis2.synchronize.vd.data.TestData;
import de.heuboe.asfinag.vmis2.synchronize.vd.services.AlgoContext;
import de.heuboe.asfinag.vmis2.synchronize.vd.services.HbKafkaUtils;
import de.heuboe.idgenerator.generator.IDGenerator;
import eu.vmis_ehe.vmis2.receiving.processing.data.pojo.PShortTermCollectedDataLanes;
import eu.vmis_ehe.vmis2.receiving.processing.data.pojo.PShortTermCollectedTrafficCategoriesLanes;
import eu.vmis_ehe.vmis2.receiving.processing.data.pojo.PTlsDataVersion;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEBetriebsparameter;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEBetriebsparameterList;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEDeFehler;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEDeFehlerList;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion0;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion0List;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion1;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion1List;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion2;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion2List;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion3;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion3List;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion4;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEErgebnisVersion4List;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEGeschwindigkeitsklassenKurz;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEGeschwindigkeitsklassenKurzList;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEKanalsteuerung;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEKanalsteuerungList;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEKfzEinzeldaten;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEKfzEinzeldatenList;
import eu.vmis_ehe.vmis2.tls.received.pojo.PSYSFehlerDUE;
import eu.vmis_ehe.vmis2.tls.received.pojo.PSYSFehlerDUEList;
import eu.vmis_ehe.vmis2.tls.send.pojo.PSteuerSequenzList;
import lombok.extern.slf4j.Slf4j;

/**
 * Synchronize vehicle data - Integration test.
 * 
 * Rquirements: 
 * VMIS2-S1ANF-122      Automatische Umschaltung bei Berechnungen (bei logischer Passivierung)  (Z. 348, 400)
 * 
 * @author David Hermanns, Heusch/Boesefeldt GmbH, david.hermanns@heuboe.de; 15.06.2021
 *
 */
@EnableAutoConfiguration
@SpringBootTest(classes = TestConfig.class, properties = {"spring.kafka.client-id=SYNC-APP-INTEGRATION-TEST",
        "spring.main.allow-bean-definition-overriding=true",
        "de.heuboe.asfinag.vmis2.synchronize.vd.fakeInfraParams=true",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData0TopicTemplate=TEST-{centreTopic}-tlsin-LVEErgebnisVersion0",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData1TopicTemplate=TEST-{centreTopic}-tlsin-LVEErgebnisVersion1",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData2TopicTemplate=TEST-{centreTopic}-tlsin-LVEErgebnisVersion2",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData3TopicTemplate=TEST-{centreTopic}-tlsin-LVEErgebnisVersion3",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData4TopicTemplate=TEST-{centreTopic}-tlsin-LVEErgebnisVersion4",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData5TopicTemplate=TEST-{centreTopic}-tlsin-LVEErgebnisVersion5",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData6TopicTemplate=TEST-{centreTopic}-tlsin-LVEErgebnisVersion6",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsSingleVehDataTopicTemplate=TEST-{centreTopic}-tlsin-LVEKfzEinzeldaten",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsSingleVehCollectedDataTopicTemplate=TEST-{centreTopic}-tlsin-LVEKfzEinzeldatenSammelmeldung",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsErrorTopicTemplate=TEST-{centreTopic}-tlsin-LVEDeFehler",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsOperatingParamTopicTemplate=TEST-{centreTopic}-tlsin-LVEBetriebsparameter",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsChannelControlTopicTemplate=TEST-{centreTopic}-tlsin-LVEKanalsteuerung",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsSysErrorTopicTemplate=TEST-{centreTopic}-tlsin-SYSFehlerDUE",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsTrafficCategoriesParamTopicTemplate=TEST-{systemWideShortcut}-tlsout-LVEGeschwindigkeitsklassenKurzSoll",
        "de.heuboe.asfinag.vmis2.synchronize.vd.parameterSystemTopics[0]=TEST-{systemWideShortcut}-Parameter-Zeitsynchronisation",
        "de.heuboe.asfinag.vmis2.synchronize.vd.parameterRoadTopics[0]=TEST-{systemWideShortcut}-Parameter-LogischePassivierungVDE",
        "de.heuboe.asfinag.vmis2.synchronize.vd.dataChangeTopicTemplate=TEST-{centreTopic}-DataChange",
        "de.heuboe.asfinag.vmis2.synchronize.vd.maxNrOfRestartRetries=4",
        "de.heuboe.asfinag.vmis2.synchronize.vd.restartsWithinTimeRange=5",
        "de.heuboe.asfinag.vmis2.synchronize.vd.collectedDataLaneTopicTemplate=TEST-{centreTopic}-ShortTermCollectedDataLane",
        "de.heuboe.asfinag.vmis2.synchronize.vd.collectedTrafficCategoriesLaneTopicTemplate=TEST-{centreTopic}-ShortTermCollectedTrafficCategoriesLane",
        "de.heuboe.asfinag.vmis2.synchronize.vd.colletedOpcUaDataLaneTopicTemplate=TEST-{centreTopic}-ShortTermCollectedOpcUaDataLane",
        "de.heuboe.asfinag.vmis2.synchronize.vd.discardedDataLaneTopicTemplate=TEST-{centreTopic}-DiscardedShortTermCollectedDataLane",
        "de.heuboe.asfinag.vmis2.synchronize.vd.topicTmpltControlSequence=TEST-{systemWideShortcut}-tlsout-SYSSteuerSequenz",
        "de.heuboe.asfinag.vmis2.test.infra.testInfraJson=src/main/resources/TestInfraRoadS1.json",
        "de.heuboe.asfinag.vmis2.synchronize.vd.centreId=WIE", 
        "de.heuboe.asfinag.vmis2.synchronize.vd.centreTopic=WIE",
        "de.heuboe.asfinag.vmis2.synchronize.vd.systemWideShortcut=VRZ",
        "de.heuboe.asfinag.vmis2.synchronize.vd.streets=.*",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algoName=synchronize vehicle data",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algoShortName=SYNCVD",
        "de.heuboe.asfinag.vmis2.synchronize.vd.instanceName=INSTANZ1",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.timeSyncDefSetId=Zeitsynchronisation",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.syncWaitSec=AbstandZeitsync",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.timelead=moeglicheVorlaufzeit",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.timeout15Secs=Timeout15",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.timeout30Secs=Timeout30",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.timeout60Secs=Timeout60",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.timeout2Min=Timeout2m",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.timeout3Min=Timeout3m",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.timeout4Min=Timeout4m",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.timeout5Min=Timeout5m",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.thresholdUpper15Sec=zeitsyncOben15",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.thresholdLower15Sec=zeitsyncUnten15",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.thresholdUpper30Sec=zeitsyncOben30",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.thresholdLower30Sec=zeitsyncUnten30",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.thresholdUpper60Sec=zeitsyncOben60",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.thresholdLower60Sec=zeitsyncUnten60",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.thresholdUpper2Min=zeitsyncOben2m",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.thresholdLower2Min=zeitsyncUnten2m",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.thresholdUpper3Min=zeitsyncOben3m",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.thresholdLower3Min=zeitsyncUnten3m",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.thresholdUpper4Min=zeitsyncOben4m",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.thresholdLower4Min=zeitsyncUnten4m",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.thresholdUpper5Min=zeitsyncOben5m",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.thresholdLower5Min=zeitsyncUnten5m",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.logPassiveDefSetId=LogischePassivierungVDE",
        "de.heuboe.asfinag.vmis2.synchronize.vd.algo.param.names.logPassive=log_passiv",
        "de.heuboe.asfinag.vmis2.synchronize.vd.dataChangeType[0]=RST",
        "de.heuboe.asfinag.vmis2.synchronize.vd.dataChangeType[1]=VDE_SENSOR",
        "de.heuboe.asfinag.vmis2.synchronize.vd.writeDiscarded=true",
        "de.heuboe.asfinag.vmis2.synchronize.vd.categoryPkw=32",
        "de.heuboe.asfinag.vmis2.synchronize.vd.categoryLkw=33",
        "de.heuboe.asfinag.vmis2.synchronize.vd.maxQualitySVDataInput=100",
        "de.heuboe.asfinag.vmis2.synchronize.vd.maxQualityTCDataInput=80",
        "de.heuboe.asfinag.vmis2.synchronize.vd.minQualityInputSlowV=0",
        "de.heuboe.asfinag.vmis2.synchronize.vd.listenSingleVehicleData=true",
        "de.heuboe.asfinag.vmis2.synchronize.vd.defaultErrorValue=-1",
        "de.heuboe.asfinag.vmis2.synchronize.vd.defaultErrorValueFloat=-99999.0"})
@ActiveProfiles({"test", "UseServices"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(partitions = 1, controlledShutdown = true, brokerProperties = {"log.dir=target/kafka${random.int}"},
        topics = {"TEST-VRZ-Parameter-LogischePassivierungVDE", "TEST-VRZ-Parameter-Zeitsynchronisation",
                "TEST-WIE-tlsin-LVEErgebnisVersion0", "TEST-WIE-tlsin-LVEErgebnisVersion1",
                "TEST-WIE-tlsin-LVEErgebnisVersion2", "TEST-WIE-tlsin-LVEErgebnisVersion3",
                "TEST-WIE-tlsin-LVEErgebnisVersion4", "TEST-WIE-tlsin-LVEErgebnisVersion5",
                "TEST-WIE-tlsin-LVEErgebnisVersion6", "TEST-WIE-tlsin-LVEKfzEinzeldaten",
                "TEST-WIE-tlsin-LVEKfzEinzeldatenSammelmeldung", "TEST-WIE-tlsin-LVEDeFehler",
                "TEST-WIE-tlsin-LVEBetriebsparameter", "TEST-WIE-tlsin-LVEKanalsteuerung",
                "TEST-WIE-tlsin-SYSFehlerDUE", "TEST-VRZ-tlsout-LVEGeschwindigkeitsklassenKurzSoll",
                "TEST-WIE-ShortTermCollectedDataLane", "TEST-WIE-ShortTermCollectedTrafficCategoriesLane",
                "TEST-WIE-ShortTermCollectedOpcUaDataLane", "TEST-WIE-DiscardedShortTermCollectedDataLane",
                "TEST-VRZ-tlsout-SYSSteuerSequenz"})
@TestPropertySource(properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=EmbeddedKafkaTest", 
        "spring.kafka.listener.missing-topics-fatal=true",
        "spring.kafka.consumer.enable-auto-commit=false"
        })

@Slf4j
public class IntegrationTest {

    protected static final String DATA_BASE_PATH = "src/test/resources/testData/";

    private static final String TEST_TOPIC_COLLECTED = "TEST-WIE-ShortTermCollectedDataLane";
    private static final String TEST_TOPIC_DISCARDED = "TEST-WIE-DiscardedShortTermCollectedDataLane";
    private static final String TEST_TOPIC_TRAFFIC_CAT = "TEST-WIE-ShortTermCollectedTrafficCategoriesLane";
    private static final String TEST_TOPIC_SYS_STEUER_SEQUENZ = "TEST-VRZ-tlsout-SYSSteuerSequenz";

    private static final String testId0 = "MQ_A23_2_884_F1";
    private static final String testId1 = "MQ_A23_2_740_F1";
    private static final String testId2 = "MQ_A23_2_710_F1";
    private static final String testId3 = "MQ_A23_2_730_F1";
    private static final String testId4 = "MQ_A23_2_720_F1";
    private static final String testId5 = "MQ_A23_2_750_F2";
    private static final String testIdLogPass = "MQ_A23_2_580_F3";

    private boolean version0 = false;
    private boolean version1 = false;
    private boolean version2 = false;
    private boolean version3 = false;
    private boolean version4 = false;
    private boolean version4_2 = false;
    private boolean passivated = false;
    
    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ConsumerFactory<String, byte[]> consumerFactory;

    @Autowired
    protected ActorSystem system;

    @Autowired
    protected SynchronizeVdProperties appProperties;

    @Autowired
    protected AlgoParameterIdProperties paramIds;

    @Autowired
    protected AlgoContext algoContext;

    @Autowired
    protected InfrastructureManager infrastructure;

    @Autowired
    SpringExtension springExtension;

    @Autowired
    IDGenerator idGenerator;
    
    @Autowired
    SystemExit systemExit;


    private Instant ivBegin;
    private Instant processingTime;


    @BeforeEach
    void init() throws Exception {

        for (final MessageListenerContainer messageListenerContainer : this.registry.getListenerContainers()) {
            waitForAssignment(messageListenerContainer,
                    TestUtils.getExpectedPartitions(messageListenerContainer, embeddedKafkaBroker));
        }
        Thread.sleep(2000);
        
        PLVEBetriebsparameter pb0 = TestData.getBetriebsparam(testId0, 0);
        PLVEBetriebsparameterList listBP0 = PLVEBetriebsparameterList.builder()
                    .iid(idGenerator.newID()).elementsList(Collections.singletonList(pb0)).build();
        kafkaTemplate.send(MessageBuilder.withPayload(listBP0)
                .setHeader(KafkaHeaders.TOPIC, algoContext.getTopicTlsOperatingParam())
                .setHeader(KafkaHeaders.KEY, testId0).build()).get();
        
        PLVEBetriebsparameter pb1 = TestData.getBetriebsparam(testId1, 1);
        PLVEBetriebsparameterList listBP1 = PLVEBetriebsparameterList.builder()
                    .iid(idGenerator.newID()).elementsList(Collections.singletonList(pb1)).build();
        kafkaTemplate.send(MessageBuilder.withPayload(listBP1)
                .setHeader(KafkaHeaders.TOPIC, algoContext.getTopicTlsOperatingParam())
                .setHeader(KafkaHeaders.KEY, testId1).build()).get();
        
        PLVEBetriebsparameter pb2 = TestData.getBetriebsparam(testId2, 2);
        PLVEBetriebsparameterList listBP2 = PLVEBetriebsparameterList.builder()
                    .iid(idGenerator.newID()).elementsList(Collections.singletonList(pb2)).build();
        kafkaTemplate.send(MessageBuilder.withPayload(listBP2)
                .setHeader(KafkaHeaders.TOPIC, algoContext.getTopicTlsOperatingParam())
                .setHeader(KafkaHeaders.KEY, testId2).build()).get();
        
        PLVEBetriebsparameter pb3 = TestData.getBetriebsparam(testId3, 3);
        PLVEBetriebsparameterList listBP3 = PLVEBetriebsparameterList.builder()
                    .iid(idGenerator.newID()).elementsList(Collections.singletonList(pb3)).build();
        kafkaTemplate.send(MessageBuilder.withPayload(listBP3)
                .setHeader(KafkaHeaders.TOPIC, algoContext.getTopicTlsOperatingParam())
                .setHeader(KafkaHeaders.KEY, testId3).build()).get();
        
        PLVEBetriebsparameter pb4 = TestData.getBetriebsparam(testId4, 4);
        PLVEBetriebsparameterList listBP4 = PLVEBetriebsparameterList.builder()
                    .iid(idGenerator.newID()).elementsList(Collections.singletonList(pb4)).build();
        kafkaTemplate.send(MessageBuilder.withPayload(listBP4)
                .setHeader(KafkaHeaders.TOPIC, algoContext.getTopicTlsOperatingParam())
                .setHeader(KafkaHeaders.KEY, testId4).build()).get();
        
        PLVEBetriebsparameter pb5 = TestData.getBetriebsparam(testId5, 4);
        PLVEBetriebsparameterList listBP5 = PLVEBetriebsparameterList.builder()
                    .iid(idGenerator.newID()).elementsList(Collections.singletonList(pb5)).build();
        kafkaTemplate.send(MessageBuilder.withPayload(listBP5)
                .setHeader(KafkaHeaders.TOPIC, algoContext.getTopicTlsOperatingParam())
                .setHeader(KafkaHeaders.KEY, testId5).build()).get();
        
        PLVEKanalsteuerung ks = TestData.getKanalsteuerung(testId2);
        PLVEKanalsteuerungList listKS = PLVEKanalsteuerungList.builder()
                    .iid(idGenerator.newID()).elementsList(Collections.singletonList(ks)).build();
        kafkaTemplate.send(MessageBuilder.withPayload(listKS)
                .setHeader(KafkaHeaders.TOPIC, algoContext.getTopicTlsChannelControl())
                .setHeader(KafkaHeaders.KEY, testId2).build()).get();
        
        PLVEDeFehler err = TestData.getDeFehler(testId2);
        PLVEDeFehlerList listErr = PLVEDeFehlerList.builder()
                .iid(idGenerator.newID()).elementsList(Collections.singletonList(err)).build();
        kafkaTemplate.send(MessageBuilder.withPayload(listErr)
                .setHeader(KafkaHeaders.TOPIC, algoContext.getTopicTlsError())
                .setHeader(KafkaHeaders.KEY, testId2).build()).get();
        
        PLVEGeschwindigkeitsklassenKurz gkPkw = TestData.getGeschwkl(testId4, appProperties.getCategoryPkw());
        PLVEGeschwindigkeitsklassenKurz gkLkw = TestData.getGeschwkl(testId4, appProperties.getCategoryLkw());
        PLVEGeschwindigkeitsklassenKurzList listGk = PLVEGeschwindigkeitsklassenKurzList.builder()
                .iid(idGenerator.newID()).elementsList(Arrays.asList(gkPkw, gkLkw)).build();
        kafkaTemplate.send(MessageBuilder.withPayload(listGk)
                .setHeader(KafkaHeaders.TOPIC, algoContext.getTopicTlsTrafficCategoriesParam())
                .setHeader(KafkaHeaders.KEY, testId4).build()).get();
        
        PLVEGeschwindigkeitsklassenKurz gkPkw2 = TestData.getGeschwkl(testId5, appProperties.getCategoryPkw());
        PLVEGeschwindigkeitsklassenKurz gkLkw2 = TestData.getGeschwkl(testId5, appProperties.getCategoryLkw());
        PLVEGeschwindigkeitsklassenKurzList listGk2 = PLVEGeschwindigkeitsklassenKurzList.builder()
                .iid(idGenerator.newID()).elementsList(Arrays.asList(gkPkw2, gkLkw2)).build();
        kafkaTemplate.send(MessageBuilder.withPayload(listGk2)
                .setHeader(KafkaHeaders.TOPIC, algoContext.getTopicTlsTrafficCategoriesParam())
                .setHeader(KafkaHeaders.KEY, testId5).build()).get();
        
        PSYSFehlerDUE sys = TestData.getSysFehlerDue(testId2);
        PSYSFehlerDUEList listSys = PSYSFehlerDUEList.builder()
                .iid(idGenerator.newID()).elementsList(Collections.singletonList(sys)).build();
        kafkaTemplate.send(MessageBuilder.withPayload(listSys)
                .setHeader(KafkaHeaders.TOPIC, algoContext.getTopicTlsSysError())
                .setHeader(KafkaHeaders.KEY, testId2).build()).get();
        
        //sendTimeSyncParam();
        //sendLogPassParam();

        Thread.sleep(2000); //Allow initialization
    }

    @AfterEach
    public void cleanUp() throws InterruptedException {
        Thread.sleep(5000); //give app time to close tests before shutdown
        log.info("IntegrationTest:Clean up called => Destroy kafkaTemplate bean");
        kafkaTemplate.destroy();
    }

    @Test
    void testFullCycle() throws IOException, InterruptedException, ExecutionException {

        Instant now  = Instant.now();
        ivBegin = now.truncatedTo(ChronoUnit.MINUTES);
        long duration = Duration.between(ivBegin, now).getSeconds();
        if (duration >= 54) {
            Thread.sleep((60-duration)*1000);
            now  = Instant.now();
            ivBegin = now.truncatedTo(ChronoUnit.MINUTES);
            duration = Duration.between(ivBegin, now).getSeconds();
        }
        Thread.sleep(1500);
        
        // Define objects single vehicle data objects
        List<PLVEKfzEinzeldaten> svList = new ArrayList<>();
        Instant passageTime = ivBegin.plusSeconds(duration + 1);
        svList.add(PLVEKfzEinzeldaten.builder()
                .id(testId3)
                .jobnummer(0)
                .tlsTime(passageTime)
                .processTime(passageTime.plusMillis(500))
                .fahrzeugklassencode(3)
                .status(0)
                .geschwindigkeit(60)
                .build());
        PLVEKfzEinzeldatenList sv = new PLVEKfzEinzeldatenList(idGenerator.newID(), svList);       
        kafkaTemplate.send(MessageBuilder.withPayload(sv)
                .setHeader(KafkaHeaders.TOPIC, HbKafkaUtils.encodeTopicName(algoContext.getTopicTlsSingleVehicleData()))
                .setHeader(KafkaHeaders.KEY, "A23_2").build()).get();
        Thread.sleep(1000);
        
        svList = new ArrayList<>();        
        Instant passageTime2 = ivBegin.plusSeconds(duration + 2);
        svList.add(PLVEKfzEinzeldaten.builder()
                .id(testId3)
                .jobnummer(0)
                .tlsTime(passageTime2)
                .processTime(passageTime2.plusMillis(500))
                .fahrzeugklassencode(3)
                .status(0)
                .geschwindigkeit(70)
                .build());
        sv = new PLVEKfzEinzeldatenList(idGenerator.newID(), svList);      
        kafkaTemplate.send(MessageBuilder.withPayload(sv)
                .setHeader(KafkaHeaders.TOPIC, HbKafkaUtils.encodeTopicName(algoContext.getTopicTlsSingleVehicleData()))
                .setHeader(KafkaHeaders.KEY, "A23_2").build()).get();
        Thread.sleep(1000);
       
        svList = new ArrayList<>();      
        Instant passageTime3 = ivBegin.plusSeconds(duration + 3);
        svList.add(PLVEKfzEinzeldaten.builder()
                .id(testId3)
                .jobnummer(0)
                .tlsTime(passageTime3)
                .processTime(passageTime3.plusMillis(500))
                .fahrzeugklassencode(3)
                .status(0)
                .geschwindigkeit(50)
                .build());
        sv = new PLVEKfzEinzeldatenList(idGenerator.newID(), svList);        
        kafkaTemplate.send(MessageBuilder.withPayload(sv)
                .setHeader(KafkaHeaders.TOPIC, HbKafkaUtils.encodeTopicName(algoContext.getTopicTlsSingleVehicleData()))
                .setHeader(KafkaHeaders.KEY, "A23_2").build()).get();
        
        
        long waitSec = 59 - duration;
        processingTime = now.plusSeconds(waitSec);      
        Thread.sleep(waitSec*1000);
        
        // Define objects, which should be handled by the actor:
        List<PLVEErgebnisVersion0> l0 = new ArrayList<>();
        l0.add(new PLVEErgebnisVersion0(testId0, 0, ivBegin, processingTime, 1, 4, 16, 0, 77, -1));
        PLVEErgebnisVersion0List v0 = new PLVEErgebnisVersion0List(idGenerator.newID(), l0);

        List<PLVEErgebnisVersion1> l1 = new ArrayList<>();
        l1.add(new PLVEErgebnisVersion1(testId1, 0, ivBegin, processingTime, 1, 4, 17, 0, 77, -1, 8.5));
        PLVEErgebnisVersion1List v1 = new PLVEErgebnisVersion1List(idGenerator.newID(), l1);

        List<PLVEErgebnisVersion2> l2 = new ArrayList<>();
        l2.add(new PLVEErgebnisVersion2(testId2, 0, ivBegin, processingTime, 1, 4, 18, 0, 77, -1, 5));
        PLVEErgebnisVersion2List v2 = new PLVEErgebnisVersion2List(idGenerator.newID(), l2);

        List<PLVEErgebnisVersion3> l3 = new ArrayList<>();
        l3.add(new PLVEErgebnisVersion3(testId3, 0, ivBegin, processingTime, 1, 4, 19, 0, 77, -1, 8.5, 5, 12, 75));
        PLVEErgebnisVersion3List v3 = new PLVEErgebnisVersion3List(idGenerator.newID(), l3);

        List<PLVEErgebnisVersion4> l4 = new ArrayList<>();
        l4.add(new PLVEErgebnisVersion4(testId4, 0, ivBegin, processingTime, 1, 4, 20, 0, 77, -1, 8.5, 5, 12, 75,
                Arrays.asList(1, 0, 8, 20, 5), Arrays.asList(0, 4, 10, 9, 1)));
        PLVEErgebnisVersion4List v4 = new PLVEErgebnisVersion4List(idGenerator.newID(), l4);
        
        List<PLVEErgebnisVersion4> l5 = new ArrayList<>();
        l5.add(new PLVEErgebnisVersion4(testId5, 0, ivBegin, processingTime, 1, 4, 20, 0, 77, -1, 8.5, 5, 12, 75,
                Arrays.asList(0, 0, 0, 0, 5), Arrays.asList(0, 0, 0, 0, 1)));
        PLVEErgebnisVersion4List v5 = new PLVEErgebnisVersion4List(idGenerator.newID(), l5);

        List<PLVEErgebnisVersion3> logPass = new ArrayList<>();
        logPass.add(new PLVEErgebnisVersion3(testIdLogPass, 0, ivBegin, processingTime, 1, 4, 19, 0, 77, -1, 8.5, 5, 12, 75));
        PLVEErgebnisVersion3List vLogPass = new PLVEErgebnisVersion3List(idGenerator.newID(), l3);
        
        // Send single vehicle data for next interval
        svList = new ArrayList<>();
        Instant passageTime4 = passageTime.plusSeconds(62);
        svList.add(PLVEKfzEinzeldaten.builder()
                .id(testId3)
                .jobnummer(0)
                .tlsTime(passageTime4)
                .processTime(passageTime4.plusMillis(500))
                .fahrzeugklassencode(3)
                .status(0)
                .geschwindigkeit(40)
                .build());
        sv = new PLVEKfzEinzeldatenList(idGenerator.newID(), svList);        
        kafkaTemplate.send(MessageBuilder.withPayload(sv)
                .setHeader(KafkaHeaders.TOPIC, HbKafkaUtils.encodeTopicName(algoContext.getTopicTlsSingleVehicleData()))
                .setHeader(KafkaHeaders.KEY, "A23_2").build()).get();        
        
        // Send short term data 
        kafkaTemplate.send(MessageBuilder.withPayload(adjustTime(v0, ivBegin))
                .setHeader(KafkaHeaders.TOPIC, HbKafkaUtils.encodeTopicName(algoContext.getTopicTlsData0()))
                .setHeader(KafkaHeaders.KEY, "A23_2").build()).get();

        kafkaTemplate.send(MessageBuilder.withPayload(adjustTime(v1, ivBegin))
                .setHeader(KafkaHeaders.TOPIC, HbKafkaUtils.encodeTopicName(algoContext.getTopicTlsData1()))
                .setHeader(KafkaHeaders.KEY, "A23_2").build()).get();

        kafkaTemplate.send(MessageBuilder.withPayload(adjustTime(v2, ivBegin))
                .setHeader(KafkaHeaders.TOPIC, HbKafkaUtils.encodeTopicName(algoContext.getTopicTlsData2()))
                .setHeader(KafkaHeaders.KEY, "A23_2").build()).get();

        kafkaTemplate.send(MessageBuilder.withPayload(adjustTime(v3, ivBegin))
                .setHeader(KafkaHeaders.TOPIC, HbKafkaUtils.encodeTopicName(algoContext.getTopicTlsData3()))
                .setHeader(KafkaHeaders.KEY, "A23_2").build()).get();

        kafkaTemplate.send(MessageBuilder.withPayload(adjustTime(v4, ivBegin))
                .setHeader(KafkaHeaders.TOPIC, HbKafkaUtils.encodeTopicName(algoContext.getTopicTlsData4()))
                .setHeader(KafkaHeaders.KEY, "A23_2").build()).get();
        
        kafkaTemplate.send(MessageBuilder.withPayload(adjustTime(v5, ivBegin))
                .setHeader(KafkaHeaders.TOPIC, HbKafkaUtils.encodeTopicName(algoContext.getTopicTlsData4()))
                .setHeader(KafkaHeaders.KEY, "A23_2").build()).get();

        kafkaTemplate.send(MessageBuilder.withPayload(adjustTime(vLogPass, ivBegin))
                .setHeader(KafkaHeaders.TOPIC, HbKafkaUtils.encodeTopicName(algoContext.getTopicTlsData3()))
                .setHeader(KafkaHeaders.KEY, "A23_2").build()).get();
        
        int timeout = 200;
        assertTimeoutPreemptively(ofSeconds(timeout), () -> {
            // check output
            log.info ("Check output ShortTermCollectedDataLanes with time {} and interval begin {}", processingTime, ivBegin);
            byte[] messageShortTermCollected = null;
            while (messageShortTermCollected == null) {
                messageShortTermCollected = checkTopic(TEST_TOPIC_COLLECTED, "A23_2", processingTime);                
            }   
            PShortTermCollectedDataLanes collectedData = PShortTermCollectedDataLanes.fromBytes(messageShortTermCollected);
            log.info("Received {} ShortTermCollectedDataLane entries", collectedData.getDataList().size());
            collectedData.getDataList().forEach(c -> {
                if (c.getEventTime().equals(ivBegin)) {
                    if(testId0.equals(c.getId()) && PTlsDataVersion.VERSION_0.equals(c.getVersion()) && c.getQKFZ() == 16) {
                        version0 = true;
                        log.info("Correct values for version0 received" );
                    }
                    if(testId1.equals(c.getId()) && PTlsDataVersion.VERSION_1.equals(c.getVersion()) && c.getQKFZ() == 17) {
                        version1 = true;                      
                        log.info("Correct values for version1 received" );
                    }
                    if(testId2.equals(c.getId()) && PTlsDataVersion.VERSION_2.equals(c.getVersion()) && c.getQKFZ() == 18) {
                        version2 = true;                                          
                        log.info("Correct values for version2 received" );
                    }
                    if (testId3.equals(c.getId()) && PTlsDataVersion.VERSION_3.equals(c.getVersion())
                            && c.getQKFZ() == 19 && c.getVehicleCategorySlow() == 3 && c.getVFZSlowQuality() == 100
                            && c.getVFZSlow() == 50 && c.getTlsTimeSlow().equals(passageTime3)) {
                        version3 = true;                                                                
                        log.info("Correct values for version3 received" );
                    }
                    if (testId4.equals(c.getId())) {
                        if (PTlsDataVersion.VERSION_4.equals(c.getVersion())
                            && c.getQKFZ() == 20 && c.getVehicleCategorySlow() == 32  && c.getVFZSlowQuality() == 80
                            && c.getVFZSlow() == 1 && c.getTlsTimeSlow().equals(ivBegin.plusSeconds(60))) {
                            log.info("Correct values for version4 received" );
                            version4 = true;
                        }
                    }
                    if (testId5.equals(c.getId())){
                        if (PTlsDataVersion.VERSION_4.equals(c.getVersion())
                            && c.getQKFZ() == 20 && c.getVehicleCategorySlow() == 33  && c.getVFZSlowQuality() == 80
                            && c.getVFZSlow() == 121 && c.getTlsTimeSlow().equals(ivBegin.plusSeconds(60))) {
                            log.info("Correct values for version4 variation 2 received" );
                            version4_2 = true;
                        }
                    }
                    if(testIdLogPass.equals(c.getId())) {
                        //is test id logical passivated
                        assertTrue(c.isPassivated());
                        passivated = true;
                    }
                 } else {
                    log.info("Event time {} is not equal interval begin {} of input short term data", c.getEventTime(), ivBegin);
                }
            });
            assertNotNull(messageShortTermCollected);
            assertTrue(version0);
            assertTrue(version1);
            assertTrue(version2);
            assertTrue(version3);
            assertTrue(version4);
            assertTrue(version4_2);
            assertTrue(passivated);
            
            // check output
            log.info ("Check output ShortTermCollectedTrafficCategoriesLanes with time {} and interval begin {}", processingTime, ivBegin);
            byte[] messageTrafficCategories = null;
            while (messageTrafficCategories == null) {
                messageTrafficCategories = checkTopic(TEST_TOPIC_TRAFFIC_CAT, "A23_2", processingTime);                
            } 
            PShortTermCollectedTrafficCategoriesLanes trafficCatData = PShortTermCollectedTrafficCategoriesLanes.fromBytes(messageTrafficCategories);
            log.info("Received {} ShortTermCollectedTrafficCategoriesLane entries", trafficCatData.getDataList().size());
            trafficCatData.getDataList().forEach(t -> {
                List<Integer> pkwList = t.getCategoryBoundariesPKWList();
                List<Integer> lkwList = t.getCategoryBoundariesLKWList();
                assertTrue(pkwList.equals(Arrays.asList(10, 50, 120, 165)));
                assertTrue(lkwList.equals(Arrays.asList(10, 55, 80, 120)));
            });
            assertNotNull(messageTrafficCategories);
          
        }, "execution timed out after " + timeout + " s (while reading collected data from '" + TEST_TOPIC_COLLECTED
                + " and " + TEST_TOPIC_TRAFFIC_CAT + "')");
       
        // Try to create ShortTermDiscardedDataLane
        now  = Instant.now();
        ivBegin = now.truncatedTo(ChronoUnit.MINUTES);
        processingTime = now;
        
        // Are we still in the allowed time range?
        long durSec = Duration.between(ivBegin, now).getSeconds();
        if (durSec > 53) {
            // Wait for next interval begin + offset. So that, the values are written too 10 seconds early for
            // the next interval
            waitSec = 60 - durSec + 50;
            processingTime = now.plusSeconds(waitSec);
            ivBegin = processingTime.truncatedTo(ChronoUnit.MINUTES);
            
            Thread.sleep(waitSec*1000);
        }
        
        // Send short term vehicle input outside the permissible time range
        kafkaTemplate.send(MessageBuilder.withPayload(adjustTime(v3, ivBegin))
                .setHeader(KafkaHeaders.TOPIC, HbKafkaUtils.encodeTopicName(algoContext.getTopicTlsData3()))
                .setHeader(KafkaHeaders.KEY, "A23_2").build()).get();
        
        assertTimeoutPreemptively(ofSeconds(timeout), () -> {
            // check output
            log.info ("Check output DiscardedShortTermCollectedDataLanes with time {} and interval begin {}", processingTime, ivBegin);
            byte[] messageDiscardedShortTerm = null;
            while (messageDiscardedShortTerm == null) {
                messageDiscardedShortTerm = checkTopic(TEST_TOPIC_DISCARDED, testId3, processingTime);
            }   
            PShortTermCollectedDataLanes discardedData = PShortTermCollectedDataLanes.fromBytes(messageDiscardedShortTerm);
            log.info("Received {} DiscardedShortTermCollectedDataLane entries", discardedData.getDataList().size());
            assertEquals(1, discardedData.getDataList().size());
            assertEquals(testId3, discardedData.getDataList().get(0).getId());
            assertEquals(19, discardedData.getDataList().get(0).getQKFZ());
            assertEquals(-1, discardedData.getDataList().get(0).getVehicleCategorySlow());
            assertNotNull(messageDiscardedShortTerm);
          
            // check output
            log.info ("Check output SYSSteuerSequenzList with time {} and interval begin {}", processingTime, ivBegin);
            byte[] messageSysSteuerSequenz = null;
            while (messageSysSteuerSequenz == null) {
                messageSysSteuerSequenz = checkTopic(TEST_TOPIC_SYS_STEUER_SEQUENZ, "", processingTime);
            }           
            PSteuerSequenzList steuerSeqData = PSteuerSequenzList.fromBytes(messageSysSteuerSequenz);
            log.info("Received {} SYSSteuerSequenz entries", steuerSeqData.getElementsList().size());
            assertEquals(1, steuerSeqData.getElementsList().size());
            // Action equals time synchronization
            log.info("Received SYSSteuerSequenz entry with action {}", steuerSeqData.getElementsList().get(0).getAction());
            assertEquals(appProperties.getActionNrRequestGlobalTimeSync(), steuerSeqData.getElementsList().get(0).getAction());
            assertNotNull(messageSysSteuerSequenz);

        }, "execution timed out after " + timeout + " s (while reading collected data from '" + TEST_TOPIC_DISCARDED
                + " and " + TEST_TOPIC_SYS_STEUER_SEQUENZ + "')");
        
        // Try to create single vehicle data for the next interval
        Thread.sleep(durSec*1000);
        now  = Instant.now();
        ivBegin = now.truncatedTo(ChronoUnit.MINUTES);
        processingTime = now;
        durSec = Duration.between(ivBegin, now).getSeconds();
        
        // Are we still in the allowed time range?
        if (durSec > 53) {
            // Wait for next interval begin + offset. So that, the single vehicle data values are written 10
            // seconds for the next interval begin
            waitSec = 60 - durSec + 50;
            processingTime = now.plusSeconds(waitSec);
            ivBegin = processingTime.truncatedTo(ChronoUnit.MINUTES);
            
            Thread.sleep(waitSec*1000);
        }

        // Send single vehicle data for next interval
        svList = new ArrayList<>();
        Instant passageTime5 = processingTime;
        svList.add(PLVEKfzEinzeldaten.builder()
                .id(testId3)
                .jobnummer(0)
                .tlsTime(passageTime5)
                .processTime(processingTime)
                .fahrzeugklassencode(3)
                .status(0)
                .geschwindigkeit(35)
                .build());
        sv = new PLVEKfzEinzeldatenList(idGenerator.newID(), svList);        
        kafkaTemplate.send(MessageBuilder.withPayload(sv)
                .setHeader(KafkaHeaders.TOPIC, HbKafkaUtils.encodeTopicName(algoContext.getTopicTlsSingleVehicleData()))
                .setHeader(KafkaHeaders.KEY, "A23_2").build()).get();
        
        now  = Instant.now();
        durSec = Duration.between(ivBegin, now).getSeconds();
        waitSec = 60 - durSec + 39;
        Thread.sleep(waitSec*1000);
        processingTime = Instant.now();
        
        // Send short term vehicle input data for the same interval as for the single vehicle data
        kafkaTemplate.send(MessageBuilder.withPayload(adjustTime(v3, ivBegin))
                .setHeader(KafkaHeaders.TOPIC, HbKafkaUtils.encodeTopicName(algoContext.getTopicTlsData3()))
                .setHeader(KafkaHeaders.KEY, "A23_2").build()).get();
        
        assertTimeoutPreemptively(ofSeconds(timeout), () -> {
            // check output
            log.info ("Check output ShortTermCollectedDataLanes with time {} and interval begin {}", processingTime, ivBegin);
            byte[] messageShortTermCollected = null;
            while (messageShortTermCollected == null) {
                messageShortTermCollected = checkTopic(TEST_TOPIC_COLLECTED, "A23_2", processingTime);                
            }   
            PShortTermCollectedDataLanes collectedData = PShortTermCollectedDataLanes.fromBytes(messageShortTermCollected);
            log.info("Received {} ShortTermCollectedDataLane entries", collectedData.getDataList().size());
            collectedData.getDataList().forEach(c -> {
                if (testId3.equals(c.getId())) {
                    assertEquals(19, c.getQKFZ());
                    assertEquals(3, c.getVehicleCategorySlow());
                    assertEquals(100, c.getVFZSlowQuality());
                    assertEquals(35, c.getVFZSlow());
                    assertEquals(passageTime5, c.getTlsTimeSlow());                   
                }
            });
        }, "execution timed out after " + timeout + " s (while reading collected data from '" + TEST_TOPIC_COLLECTED + "')");
        
     }

    private PLVEErgebnisVersion0List adjustTime(PLVEErgebnisVersion0List d, Instant now) {
        return PLVEErgebnisVersion0List.builder().iid(idGenerator.newID()).elementsList(
                d.getElementsList().stream().map(p -> p.toBuilder()).map(p -> p.tlsTime(now).build())
                        .collect(Collectors.toList())).build();
    }

    private PLVEErgebnisVersion1List adjustTime(PLVEErgebnisVersion1List d, Instant now) {
        return PLVEErgebnisVersion1List.builder().iid(idGenerator.newID()).elementsList(
                d.getElementsList().stream().map(p -> p.toBuilder()).map(p -> p.tlsTime(now).build())
                        .collect(Collectors.toList())).build();
    }

    private PLVEErgebnisVersion2List adjustTime(PLVEErgebnisVersion2List d, Instant now) {
        return PLVEErgebnisVersion2List.builder().iid(idGenerator.newID()).elementsList(
                d.getElementsList().stream().map(p -> p.toBuilder()).map(p -> p.tlsTime(now).build())
                        .collect(Collectors.toList())).build();
    }

    private PLVEErgebnisVersion3List adjustTime(PLVEErgebnisVersion3List d, Instant now) {
        return PLVEErgebnisVersion3List.builder().iid(idGenerator.newID()).elementsList(
                d.getElementsList().stream().map(p -> p.toBuilder()).map(p -> p.tlsTime(now).build())
                        .collect(Collectors.toList())).build();
    }

    private PLVEErgebnisVersion4List adjustTime(PLVEErgebnisVersion4List d, Instant now) {
        return PLVEErgebnisVersion4List.builder().iid(idGenerator.newID()).elementsList(
                d.getElementsList().stream().map(p -> p.toBuilder()).map(p -> p.tlsTime(now).build())
                        .collect(Collectors.toList())).build();
    }
    
    byte[] checkTopic(String topic, String testKey, Instant now) {
        try (Consumer<String, byte[]> consumer = consumerFactory.createConsumer()) {
            // int numPartitions = consumer.partitionsFor(topic).size();
            // int partitionOfKey = Utils.toPositive(Utils.murmur2(testKey.getBytes())) % numPartitions;
            TopicPartition partition = new TopicPartition(topic, 0);
            List<TopicPartition> topicPartitions = Collections.singletonList(partition);
            consumer.assign(topicPartitions);
            // embedded Kafka. We read from beginning!
            consumer.seekToBeginning(topicPartitions);
            // long pos = consumer.position(partition);
            // pos = pos == 0 ? 0 : pos - 1;
            // consumer.seek(partition, pos);

            ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofSeconds(10));
            if(records.isEmpty()) {
                return null;
            }
            List<ConsumerRecord<String, byte[]>> recs = records.records(partition);

            if (recs == null || recs.isEmpty()) {
                return null;
            }

            List<ConsumerRecord<String, byte[]>> sorted =
                    recs.stream().sorted((r1, r2) -> Long.compare(r2.timestamp(), r1.timestamp()))
                            .collect(Collectors.toList());
            Optional<ConsumerRecord<String, byte[]>> optionalConsumerRecord = Optional.empty();

            if (!testKey.isEmpty()) {
                optionalConsumerRecord = sorted.stream().filter(cr -> cr.key().equals(testKey)).findFirst();
            } else if (!sorted.isEmpty()){               
                optionalConsumerRecord = Optional.ofNullable(sorted.get(0));
            }             
            if (optionalConsumerRecord.isEmpty()) {
                return null;
            }
            long lNow = now.toEpochMilli();
            ConsumerRecord<String, byte[]> last = optionalConsumerRecord.get();
            if (last.timestamp() < lNow) {
                return null;
            }
            log.info("Return topic {} with time stamp {}", topic, Instant.ofEpochMilli(last.timestamp()));
            return last.value();
        }
    }
    
//    private void sendLogPassParam() throws IOException, InterruptedException, ExecutionException {
//        // Read synchronize algo parameter from file
//        ParameterSetList.Builder algoParaBuilder = ParameterSetList.newBuilder();
//        String json = new String(Files.readAllBytes(
//                Paths.get(DATA_BASE_PATH + "Parameter/PParmeterSetList#LogischePassivierungVDEINSTANZ1.json")));
//        JsonFormat.parser().merge(json, algoParaBuilder);
//        PParameterSetList algoPara = PParameterSetList.from(algoParaBuilder.build());
//        assertNotNull(algoPara);
//
//        // Send algo parameter
//        kafkaTemplate.send(MessageBuilder.withPayload(algoPara)
//                .setHeader(KafkaHeaders.TOPIC, "TEST-VRZ-Parameter-LogischePassivierungVDE")
//                .setHeader(KafkaHeaders.MESSAGE_KEY, "LogischePassivierungVDE-WIE-INSTANZ1-A23_2")
//                .setHeader(KafkaConstants.KAFKA_HEADER_DEFINITION_SET_ID, paramIds.getLogPassiveDefSetId())
//                .setHeader(KafkaConstants.KAFKA_HEADER_SYSTEM, "WIE")
//                .setHeader(KafkaConstants.KAFKA_HEADER_ROAD_ID, "A23_2")
//                .setHeader(KafkaConstants.KAFKA_HEADER_INSTANCE, appProperties.getInstanceName()).build()).get();
//    }
    
//    public void sendTimeSyncParam() throws IOException, InterruptedException, ExecutionException {
//        // Read synchronize algo parameter from file
//        ParameterSetList.Builder algoParaBuilder = ParameterSetList.newBuilder();
//        String json = new String(Files.readAllBytes(
//                Paths.get(DATA_BASE_PATH + "Parameter/PParmeterSetList#ZeitsychronisationINSTANZ1.json")));
//        JsonFormat.parser().merge(json, algoParaBuilder);
//        PParameterSetList algoPara = PParameterSetList.from(algoParaBuilder.build());
//        assertNotNull(algoPara);
//
//        // Send algo parameter
//        kafkaTemplate.send(MessageBuilder.withPayload(algoPara)
//                .setHeader(KafkaHeaders.TOPIC, "TEST-VRZ-Parameter-Zeitsynchronisation")
//                .setHeader(KafkaHeaders.MESSAGE_KEY, "Zeitsynchronisation-WIE-INSTANZ1")
//                .setHeader(KafkaConstants.KAFKA_HEADER_DEFINITION_SET_ID, paramIds.getTimeSyncDefSetId())
//                .setHeader(KafkaConstants.KAFKA_HEADER_SYSTEM, "WIE")
//                .setHeader(KafkaConstants.KAFKA_HEADER_INSTANCE, appProperties.getInstanceName()).build()).get();
//    }
}
