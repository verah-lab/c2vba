package de.heuboe.asfinag.vmis2.synchronize.vd;

import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.springframework.kafka.test.utils.ContainerTestUtils.waitForAssignment;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import org.springframework.kafka.annotation.EnableKafka;
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

import com.google.protobuf.InvalidProtocolBufferException;

import akka.actor.ActorSystem;
import de.heuboe.asfinag.control.base.actors.SpringExtension;
import de.heuboe.asfinag.vmis2.infrastructure.types.ReferenceTypes;
import de.heuboe.asfinag.vmis2.synchronize.vd.config.AlgoParameterIdProperties;
import de.heuboe.asfinag.vmis2.synchronize.vd.services.AlgoContext;
import de.heuboe.asfinag.vmis2.synchronize.vd.services.InfrastructureFromSystem;
import de.heuboe.idgenerator.generator.IDGenerator;
import eu.vmis_ehe.vmis2.configservice.ConfigServiceGrpc;
import eu.vmis_ehe.vmis2.configservice.pojo.PAreaChange;
import eu.vmis_ehe.vmis2.configservice.pojo.PConfigItemType;
import eu.vmis_ehe.vmis2.configservice.pojo.PDataChange;
import eu.vmis_ehe.vmis2.configservice.pojo.PDataChanges;
import eu.vmis_ehe.vmis2.configservice.pojo.PItemChange;
import eu.vmis_ehe.vmis2.receiving.processing.data.pojo.PShortTermCollectedDataLane;
import eu.vmis_ehe.vmis2.receiving.processing.data.pojo.PShortTermCollectedDataLanes;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEBetriebsparameter;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEBetriebsparameterList;
import lombok.extern.slf4j.Slf4j;

@EnableAutoConfiguration
@EnableKafka
@SpringBootTest(classes = ReadAllUZTestConfig.class, properties = {
        "spring.kafka.client-id=SYNC-APP-READ-ALL-UZ-TEST",
        "spring.main.allow-bean-definition-overriding=true",
        "de.heuboe.asfinag.vmis2.synchronize.vd.fakeInfraParams=true",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData0TopicTemplate=ALL-UZ-TEST-{centreTopic}-tlsin-LVEErgebnisVersion0",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData1TopicTemplate=ALL-UZ-TEST-{centreTopic}-tlsin-LVEErgebnisVersion1",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData2TopicTemplate=ALL-UZ-TEST-{centreTopic}-tlsin-LVEErgebnisVersion2",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData3TopicTemplate=ALL-UZ-TEST-{centreTopic}-tlsin-LVEErgebnisVersion3",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData4TopicTemplate=ALL-UZ-TEST-{centreTopic}-tlsin-LVEErgebnisVersion4",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData5TopicTemplate=ALL-UZ-TEST-{centreTopic}-tlsin-LVEErgebnisVersion5",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData6TopicTemplate=ALL-UZ-TEST-{centreTopic}-tlsin-LVEErgebnisVersion6",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsSingleVehDataTopicTemplate=ALL-UZ-TEST-{centreTopic}-tlsin-LVEKfzEinzeldaten",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsSingleVehCollectedDataTopicTemplate=ALL-UZ-TEST-{centreTopic}-tlsin-LVEKfzEinzeldatenSammelmeldung",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsErrorTopicTemplate=ALL-UZ-TEST-{centreTopic}-tlsin-LVEDeFehler",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsOperatingParamTopicTemplate=ALL-UZ-TEST-{centreTopic}-tlsin-LVEBetriebsparameter",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsChannelControlTopicTemplate=ALL-UZ-TEST-{centreTopic}-tlsin-LVEKanalsteuerung",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsSysErrorTopicTemplate=ALL-UZ-TEST-{centreTopic}-tlsin-SYSFehlerDUE",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsTrafficCategoriesParamTopicTemplate=ALL-UZ-TEST-{systemWideShortcut}-tlsout-LVEGeschwindigkeitsklassenKurzSoll",
        "de.heuboe.asfinag.vmis2.synchronize.vd.parameterSystemTopics[0]=ALL-UZ-TEST-{systemWideShortcut}-Parameter-Zeitsynchronisation",
        "de.heuboe.asfinag.vmis2.synchronize.vd.parameterRoadTopics[0]=ALL-UZ-TEST-{systemWideShortcut}-Parameter-LogischePassivierungVDE",
        "de.heuboe.asfinag.vmis2.synchronize.vd.dataChangeTopicTemplate=ALL-UZ-TEST-{centreTopic}-DataChange",
        "de.heuboe.asfinag.vmis2.synchronize.vd.maxNrOfRestartRetries=4",
        "de.heuboe.asfinag.vmis2.synchronize.vd.restartsWithinTimeRange=5",
        "de.heuboe.asfinag.vmis2.synchronize.vd.collectedDataLaneTopicTemplate=ALL-UZ-TEST-{centreTopic}-ShortTermCollectedDataLane",
        "de.heuboe.asfinag.vmis2.synchronize.vd.collectedTrafficCategoriesLaneTopicTemplate=ALL-UZ-TEST-{centreTopic}-ShortTermCollectedTrafficCategoriesLane",
        "de.heuboe.asfinag.vmis2.synchronize.vd.colletedOpcUaDataLaneTopicTemplate=ALL-UZ-TEST-{centreTopic}-ShortTermCollectedOpcUaDataLane",
        "de.heuboe.asfinag.vmis2.synchronize.vd.discardedDataLaneTopicTemplate=ALL-UZ-TEST-{centreTopic}-DiscardedShortTermCollectedDataLane",
        "de.heuboe.asfinag.vmis2.synchronize.vd.topicTmpltControlSequence=ALL-UZ-TEST-{systemWideShortcut}-tlsout-SYSSteuerSequenz",
        "de.heuboe.asfinag.vmis2.test.infra.testInfraJson=src/main/resources/TestInfraRoadS1.json",
        "de.heuboe.asfinag.vmis2.synchronize.vd.centreId=-ALL-",
        "de.heuboe.asfinag.vmis2.synchronize.vd.centreTopic=VRZ",
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
@ActiveProfiles({"test", "UseServices" })
@DirtiesContext
@EmbeddedKafka(partitions = 1,
    controlledShutdown = true,
    brokerProperties = {"log.dir=target/kafka${random.int}" },
    topics = {
        "ALL-UZ-TEST-VRZ-Parameter-LogischePassivierungVDE",
        "ALL-UZ-TEST-VRZ-Parameter-Zeitsynchronisation",
        "ALL-UZ-TEST-VRZ-tlsin-LVEErgebnisVersion0",
        "ALL-UZ-TEST-VRZ-tlsin-LVEErgebnisVersion1",
        "ALL-UZ-TEST-VRZ-tlsin-LVEErgebnisVersion2",
        "ALL-UZ-TEST-VRZ-tlsin-LVEErgebnisVersion3",
        "ALL-UZ-TEST-VRZ-tlsin-LVEErgebnisVersion4",
        "ALL-UZ-TEST-VRZ-tlsin-LVEErgebnisVersion5",
        "ALL-UZ-TEST-VRZ-tlsin-LVEErgebnisVersion6",
        "ALL-UZ-TEST-VRZ-tlsin-LVEKfzEinzeldaten",
        "ALL-UZ-TEST-VRZ-tlsin-LVEKfzEinzeldatenSammelMeldung",
        "ALL-UZ-TEST-VRZ-tlsin-LVEDeFehler",
        "ALL-UZ-TEST-VRZ-tlsin-LVEBetriebsparameter",
        "ALL-UZ-TEST-VRZ-tlsin-LVEKanalsteuerung",
        "ALL-UZ-TEST-VRZ-tlsin-SYSFehlerDUE",
        "ALL-UZ-TEST-VRZ-tlsout-LVEGeschwindigkeitsklassenKurzSoll",
        "ALL-UZ-TEST-VRZ-ShortTermCollectedDataLane",
        "ALL-UZ-TEST-VRZ-ShortTermCollectedTrafficCategoriesLane",
        "ALL-UZ-TEST-VRZ-ShortTermCollectedOpcUaDataLane",
        "ALL-UZ-TEST-VRZ-DiscardedShortTermCollectedDataLane",
        "ALL-UZ-TEST-VRZ-tlsout-SYSSteuerSequenz",
        "ALL-UZ-TEST-VRZ-DataChange"
    }
)
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.consumer.group-id=EmbeddedKafkaTest",
    "spring.kafka.listener.missing-topics-fatal=true",
    "spring.kafka.consumer.enable-auto-commit=false"})
@Slf4j
public class ReadAllUZTest {
    
    protected static final String DATA_BASE_PATH = "src/test/resources/testData/";
    private final static String TEST_ID = "MQ_A23_1_200_F1";
    private final static short TEST_60_SEC_INTERVAL = 60;
    private final static short TIMEOUT_60_SEC_INTERVAL = 40;

    private static final String TEST_COLLECTED = "ALL-UZ-TEST-VRZ-ShortTermCollectedDataLane";

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    protected KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    protected ConsumerFactory<String, byte[]> consumerFactory;

    @Autowired
    protected ActorSystem system;
    
    @Autowired
    protected AlgoParameterIdProperties paramIds;
    
    @Autowired
    protected AlgoContext algoContext;
   
    @Autowired
    protected InfrastructureFromSystem infraSystem;
 
    @Autowired
    SpringExtension springExtension;

    @Autowired
    IDGenerator idGenerator;    
    
    @Autowired
    ConfigServiceGrpc.ConfigServiceBlockingStub configServiceStub;
    
    class Result {
        public List<ConsumerRecord<String, byte[]>> records;
        public long newPosition;
        public Instant latestTimestamp;

        public Result(List<ConsumerRecord<String, byte[]>> records, long newPosition, Instant latestTimestamp){
            super();
            this.records = records;
            this.newPosition = newPosition;
            this.latestTimestamp = latestTimestamp;
        }
    }
    
    class Result2 {
        public List<ConsumerRecord<String, byte[]>> records;
        public Instant latestTimestamp;

        public Result2(List<ConsumerRecord<String, byte[]>> records, Instant latestTimestamp) {
            super();
            this.records = records;
            this.latestTimestamp = latestTimestamp;
        }
    }
    
    private Consumer<String, byte[]> consumerCollected;
    private long oldPosCollected = 0;
    private TopicPartition partitionCollected;
    private List<TopicPartition> topicPartitionsCollected;

    @BeforeEach
    void init() throws Exception {

        for (final MessageListenerContainer messageListenerContainer : this.registry.getListenerContainers()) {
            waitForAssignment(messageListenerContainer,
                    TestUtils.getExpectedPartitions(messageListenerContainer, embeddedKafkaBroker));
        }
        Thread.sleep(2000); //Allow initialization
        
        PLVEBetriebsparameter pb = PLVEBetriebsparameter.builder()
                                    .id(TEST_ID)
                                    .alpha1(0.30078125)
                                    .alpha2(0.30078125)
                                    .artMittelwertbildung(1)
                                    .datenversionKurz(3)
                                    .datenversionLang(13)
                                    .erfassungsintervalldauerKurz(4)
                                    .erfassungsintervalldauerLang(129)
                                    .jobnummer(0)
                                    .laengengrenzwert(525)
                                    .processTime(Instant.now())
                                    .startwertMittelwertbildung(60)
                                    .tlsTime(Instant.now())
                                    .build();

        kafkaTemplate.send(MessageBuilder.withPayload(PLVEBetriebsparameterList.builder()
                            .iid(idGenerator.newID()).elementsList(Collections.singletonList(pb)).build())
                        .setHeader(KafkaHeaders.TOPIC, algoContext.getTopicTlsOperatingParam())
                        .setHeader(KafkaHeaders.KEY, TEST_ID).build()).get();

        Thread.sleep(2000); //Allow initialization
    }
    
    @AfterEach
    public void cleanUp() throws InterruptedException {
        Thread.sleep(5000); //give app time to close tests before shutdown
        log.info("ReadAllUZTest:Clean up called => Destroy kafkaTemplate bean");
        kafkaTemplate.destroy();
    }
    
    @Test
    void testFullCycle() throws IOException, InterruptedException, ExecutionException {
        initConsumers();

        // Determination of the current interval to be analyzed and wait until the
        // timeout of the current interval.
        Instant ivBegin = determineIntervalBegin();

        // Getting the number of lanes (VDE_SENSORs), in fact all(centreId=-ALL-)
        // UZten(WIE and PLA) that the configService knows in the test configuration
        int size = infraSystem.getInfrastructureObjectsOfType(ReferenceTypes.LANE).size();
        log.info("ReadAllUZTest: Number of VDE_SENSORs for all UZs: {}", size);

        // Check the sent fault values.
        checkOutputDataSize(ivBegin.plusSeconds(TEST_60_SEC_INTERVAL), size);

        // Creating a new config trigger for rVMZ WIE update VDE sensors
        Thread.sleep(5000);
        log.info(
                "ReadAllUZTest: Sent DataChange with possible changed VDE sensors for rVMZ Wien...");
        List<PConfigItemType> typeList = List.of(PConfigItemType.VDE_SENSOR, PConfigItemType.RST);
        PAreaChange pAreaChange = PAreaChange.builder()
                .featureChangesList(List.of(PItemChange.builder().itemTypesList(typeList).build()))
                .build();
        PDataChanges pDataChanges = PDataChanges.builder()
                .dataChangesList(
                        List.of(PDataChange.builder().rVmzId("WIE")
                                .roadChangesList(List.of(pAreaChange)).build()))
                .gipChanged(false).iid(idGenerator.newID()).build();
        kafkaTemplate.send(MessageBuilder.withPayload(pDataChanges)
                .setHeader(KafkaHeaders.TOPIC, "ALL-UZ-TEST-VRZ-DataChange")
                .setHeader(KafkaHeaders.KEY, "Trigger").build())
                .get();
        Thread.sleep(4000);

        // Determination of the current interval to be analyzed and wait until the
        // timeout of the current interval.
        ivBegin = determineIntervalBegin();

        // Getting the number of lanes (VDE_SENSORs)
        size = infraSystem.getInfrastructureObjectsOfType(ReferenceTypes.LANE).size();
        log.info("ReadAllUZTest:Number of VDE_SENSORs after first DataChange: {}", size);

        // Check the sent fault values. Nothing has changed => number of values is the same
        checkOutputDataSize(ivBegin.plusSeconds(TEST_60_SEC_INTERVAL), size);

        // Creating a new config trigger for rVMZ WIE update VDE sensors
        Thread.sleep(5000);
        log.info(
                "ReadAllUZTest: Sent DataChange with possible changed VDE sensors for rVMZ Wien...");
        typeList = List.of(PConfigItemType.VDE_SENSOR, PConfigItemType.RST);
        pAreaChange = PAreaChange.builder()
                .featureChangesList(List.of(PItemChange.builder().itemTypesList(typeList).build()))
                .build();
        pDataChanges = PDataChanges.builder()
                .dataChangesList(
                        List.of(PDataChange.builder().rVmzId("WIE")
                                .roadChangesList(List.of(pAreaChange)).build()))
                .gipChanged(false).iid(idGenerator.newID()).build();

        // GetAllItemRequest without uzId filter (get all UZs the configService knows)
        // => now only supplies VDE_SENSORS for one UZ
        TestUtils.updateType = TestUtils.UpdateType.UPDATE;
        kafkaTemplate.send(MessageBuilder.withPayload(pDataChanges)
                .setHeader(KafkaHeaders.TOPIC, "ALL-UZ-TEST-VRZ-DataChange")
                .setHeader(KafkaHeaders.KEY, "Trigger").build())
                .get();
        Thread.sleep(4000);

        // Determination of the current interval to be analyzed and wait until the
        // timeout of the current interval.
        ivBegin = determineIntervalBegin();

        // Getting the number of lanes (VDE_SENSORs)
        size = infraSystem.getInfrastructureObjectsOfType(ReferenceTypes.LANE).size();
        log.info("ReadAllUZTest:Number of VDE_SENSORs after second DataChange: {}", size);

        // Check the sent fault values. Nothing has changed => number of values is the same
        checkOutputDataSize(ivBegin.plusSeconds(TEST_60_SEC_INTERVAL), size);
    }

    private void initConsumers() {
        consumerCollected = consumerFactory.createConsumer("consumerAllUZs");
        partitionCollected = new TopicPartition(TEST_COLLECTED, 0);
        topicPartitionsCollected = Collections.singletonList(partitionCollected);
        consumerCollected.assign(topicPartitionsCollected);
        consumerCollected.seekToEnd(topicPartitionsCollected);
        oldPosCollected = consumerCollected.position(partitionCollected);
    }
    
    private Instant determineIntervalBegin() throws InterruptedException {
        // Determine current begin of interval for 60 second interval
        Instant now = Instant.now();
        Instant ivBegin = now.truncatedTo(ChronoUnit.MINUTES);

        // If there is still enough time left to look at the current interval?
        long duration = Duration.between(ivBegin, now).getSeconds();
        // Is the end of the interval nearly reached?
        if (duration >= 55) {
            // Sleep a little longer than the end of the current interval
            Thread.sleep((TEST_60_SEC_INTERVAL - duration) * 1100);
            // Determine new begin of interval for 60 second interval
            now = Instant.now();
            ivBegin = now.truncatedTo(ChronoUnit.MINUTES);
            // Determine the small gap between the current time and the begin of the
            // interval.
            duration = Duration.between(ivBegin, now).getSeconds();
        }

        // Wait until the timeout of the current interval. The fault values for all
        // lanes(VDE_SENSORs) of the currently read infrastructure should then have been
        // sent.
        long waitSec = TEST_60_SEC_INTERVAL - duration + TIMEOUT_60_SEC_INTERVAL + 2;
        Thread.sleep(waitSec * 1000);
        return ivBegin;
    }

    private void checkOutputDataSize(Instant from, int expectedSize)
            throws InvalidProtocolBufferException {
        List<PShortTermCollectedDataLane> allcollectedData = new ArrayList<>();;
        int timeout = 30;
        assertTimeoutPreemptively(ofSeconds(timeout), () -> {
            Result res = null;
            Instant readFrom = from;
            while (allcollectedData.size() < expectedSize) {
                log.info(
                        "Listen to topic from kafka timestamp '{}' expected size {}; received size {} so far; offset {}; now it is: {}",
                        readFrom, expectedSize, allcollectedData.size(), oldPosCollected,
                        Instant.now());
                res = checkTopic(consumerCollected, partitionCollected, topicPartitionsCollected,
                        readFrom,
                        oldPosCollected);
                oldPosCollected = res.newPosition;
                if (res != null) {
                    for (ConsumerRecord<String, byte[]> rec : res.records) {
                        PShortTermCollectedDataLanes collectedData = PShortTermCollectedDataLanes
                                .fromBytes(rec.value());
                        log.info(
                                "Received {} entries for ShortTermCollectedDataLane for key {}, with timestamp {} and with offset {}",
                                collectedData.getDataList().size(), rec.key(), rec.timestamp(),
                                rec.offset());
                        allcollectedData.addAll(collectedData.getDataList());
                    }
                    // readFrom = res.latestTimestamp;
                }
                Thread.sleep(1000);
            }
            assertEquals(expectedSize, allcollectedData.size());
        }, "execution timed out after " + timeout + " s (while reading collected data from '"
                + TEST_COLLECTED + "')");
    }

    protected Result checkTopic(Consumer<String, byte[]> consumer, TopicPartition partition,
            List<TopicPartition> topicPartitions, Instant from, long oldPos) {
        Map<TopicPartition, Long> topicMap = consumer.endOffsets(topicPartitions);
        long endOffset = topicMap.get(topicPartitions.get(0));
        long nextPos = oldPos;
        if (endOffset == nextPos) {
            return null;
        }
        while (nextPos < endOffset) {
            consumer.seek(partition, nextPos);
            ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(500));
            if (records.isEmpty()) {
                log.info("Poll: ConsumerRecords is empty!");
                return null;
            }
            List<ConsumerRecord<String, byte[]>> recs = records.records(partition);
            if (recs == null || recs.isEmpty()) {
                log.info("List ConsumerRecord is empty!");
                return null;
            } else {
                log.info("Received ConsumerRecord list with {} entries", recs.size());
            }

            // Sort data records by kafka timestamp in descending order(newest timestamp first)
            List<ConsumerRecord<String, byte[]>> sorted = recs.stream()
                    .filter(rec -> rec.timestamp() >= from.toEpochMilli())
                    .sorted((r1, r2) -> Long.compare(r1.timestamp(), r2.timestamp()))
                    .collect(Collectors.toList());
            if (sorted.isEmpty()) {
                log.info("No further records read from timestamp: {}", from);
                return new Result(sorted, nextPos, from);
            } else {
                Optional<ConsumerRecord<String, byte[]>> optionalConsumerRecord =
                        Optional.ofNullable(sorted.get(sorted.size() - 1));
                ConsumerRecord<String, byte[]> latest = optionalConsumerRecord.get();
                nextPos = latest.offset() + 1;
                log.info("Return topic {} with latest timestamp {} and next offset {}",
                        TEST_COLLECTED,
                        Instant.ofEpochMilli(latest.timestamp()), nextPos);
                return new Result(sorted, nextPos, Instant.ofEpochMilli(latest.timestamp()));
            }
        }
        return null;
    }
}
