package de.heuboe.asfinag.vmis2.synchronize.vd;

import static org.springframework.kafka.test.utils.ContainerTestUtils.waitForAssignment;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
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
import de.heuboe.idgenerator.generator.IDGenerator;
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
@SpringBootTest(classes = MissingParamTestConfig.class, properties = {
        "spring.kafka.client-id=SYNC-APP-MISSING-PARAM-TEST",
        "spring.main.allow-bean-definition-overriding=true",
        "de.heuboe.asfinag.vmis2.synchronize.vd.fakeInfraParams=true",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData0TopicTemplate=MI-TEST-{centreTopic}-tlsin-LVEErgebnisVersion0",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData1TopicTemplate=MI-TEST-{centreTopic}-tlsin-LVEErgebnisVersion1",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData2TopicTemplate=MI-TEST-{centreTopic}-tlsin-LVEErgebnisVersion2",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData3TopicTemplate=MI-TEST-{centreTopic}-tlsin-LVEErgebnisVersion3",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData4TopicTemplate=MI-TEST-{centreTopic}-tlsin-LVEErgebnisVersion4",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData5TopicTemplate=MI-TEST-{centreTopic}-tlsin-LVEErgebnisVersion5",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData6TopicTemplate=MI-TEST-{centreTopic}-tlsin-LVEErgebnisVersion6",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsSingleVehDataTopicTemplate=MI-TEST-{centreTopic}-tlsin-LVEKfzEinzeldaten",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsSingleVehCollectedDataTopicTemplate=MI-TEST-{centreTopic}-tlsin-LVEKfzEinzeldatenSammelmeldung",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsErrorTopicTemplate=MI-TEST-{centreTopic}-tlsin-LVEDeFehler",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsOperatingParamTopicTemplate=MI-TEST-{centreTopic}-tlsin-LVEBetriebsparameter",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsChannelControlTopicTemplate=MI-TEST-{centreTopic}-tlsin-LVEKanalsteuerung",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsSysErrorTopicTemplate=MI-TEST-{centreTopic}-tlsin-SYSFehlerDUE",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsTrafficCategoriesParamTopicTemplate=MI-TEST-{systemWideShortcut}-tlsout-LVEGeschwindigkeitsklassenKurzSoll",
        "de.heuboe.asfinag.vmis2.synchronize.vd.parameterSystemTopics[0]=MI-TEST-{systemWideShortcut}-Parameter-Zeitsynchronisation",
        "de.heuboe.asfinag.vmis2.synchronize.vd.parameterRoadTopics[0]=MI-TEST-{systemWideShortcut}-Parameter-LogischePassivierungVDE",
        "de.heuboe.asfinag.vmis2.synchronize.vd.dataChangeTopicTemplate=MI-TEST-{centreTopic}-DataChange",
        "de.heuboe.asfinag.vmis2.synchronize.vd.maxNrOfRestartRetries=4",
        "de.heuboe.asfinag.vmis2.synchronize.vd.restartsWithinTimeRange=5",
        "de.heuboe.asfinag.vmis2.synchronize.vd.collectedDataLaneTopicTemplate=MI-TEST-{centreTopic}-ShortTermCollectedDataLane",
        "de.heuboe.asfinag.vmis2.synchronize.vd.collectedTrafficCategoriesLaneTopicTemplate=MI-TEST-{centreTopic}-ShortTermCollectedTrafficCategoriesLane",
        "de.heuboe.asfinag.vmis2.synchronize.vd.colletedOpcUaDataLaneTopicTemplate=MI-TEST-{centreTopic}-ShortTermCollectedOpcUaDataLane",
        "de.heuboe.asfinag.vmis2.synchronize.vd.discardedDataLaneTopicTemplate=MI-TEST-{centreTopic}-DiscardedShortTermCollectedDataLane",
        "de.heuboe.asfinag.vmis2.synchronize.vd.topicTmpltControlSequence=MI-TEST-{systemWideShortcut}-tlsout-SYSSteuerSequenz",
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
        "de.heuboe.asfinag.vmis2.synchronize.vd.defaultErrorValueFloat=-99999.0"
})
@ActiveProfiles({"test", "UseServices"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(partitions = 1, controlledShutdown = true, brokerProperties = {
        "log.dir=target/kafka${random.int}"}, topics = {
                "MI-TEST-VRZ-Parameter-LogischePassivierungVDE",
                "MI-TEST-VRZ-Parameter-Zeitsynchronisation",
                "MI-TEST-WIE-tlsin-LVEErgebnisVersion0",
                "MI-TEST-WIE-tlsin-LVEErgebnisVersion1",
                "MI-TEST-WIE-tlsin-LVEErgebnisVersion2",
                "MI-TEST-WIE-tlsin-LVEErgebnisVersion3",
                "MI-TEST-WIE-tlsin-LVEErgebnisVersion4",
                "MI-TEST-WIE-tlsin-LVEErgebnisVersion5", 
                "MI-TEST-WIE-tlsin-LVEErgebnisVersion6",
                "MI-TEST-WIE-tlsin-LVEKfzEinzeldaten",
                "MI-TEST-WIE-tlsin-LVEKfzEinzeldatenSammelmeldung",
                "MI-TEST-WIE-tlsin-LVEDeFehler",
                "MI-TEST-WIE-tlsin-LVEBetriebsparameter",
                "MI-TEST-WIE-tlsin-LVEKanalsteuerung",
                "MI-TEST-WIE-tlsin-SYSFehlerDUE",
                "MI-TEST-VRZ-tlsout-LVEGeschwindigkeitsklassenKurzSoll",
                "MI-TEST-WIE-ShortTermCollectedDataLane", 
                "MI-TEST-WIE-ShortTermCollectedTrafficCategoriesLane",
                "MI-TEST-WIE-ShortTermCollectedOpcUaDataLane",
                "MI-TEST-WIE-DiscardedShortTermCollectedDataLane",
                "MI-TEST-VRZ-tlsout-SYSSteuerSequenz"})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=EmbeddedKafkaTest",
        "spring.kafka.listener.missing-topics-fatal=true",
        "spring.kafka.consumer.enable-auto-commit=false"})

@Slf4j
public class MissingParamTest {

    protected static final String DATA_BASE_PATH = "src/test/resources/testData/";

   
    private static final String testId0 = "MQ_A23_2_884_F1";
    private static final String testId1 = "MQ_A23_2_740_F1";
    private static final String testId2 = "MQ_A23_2_710_F1";
    private static final String testId3 = "MQ_A23_2_730_F1";
    private static final String testId4 = "MQ_A23_2_720_F1";
    private static final String testId5 = "MQ_A23_2_750_F2";
    
    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

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
        
        Thread.sleep(2000); //Allow initialization
    }

    @AfterEach
    public void cleanUp() throws InterruptedException {
        Thread.sleep(5000); //give app time to close tests before shutdown
        log.info("MissingParamTest:Clean up called => Destroy kafkaTemplate bean");
        kafkaTemplate.destroy();
    }

    @Test
    void testSystemExit() throws IOException, InterruptedException, ExecutionException {

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
        
        Mockito.verify(systemExit).exit(0);               
     }
}
