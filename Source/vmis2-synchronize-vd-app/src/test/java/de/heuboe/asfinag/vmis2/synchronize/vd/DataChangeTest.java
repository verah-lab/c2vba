package de.heuboe.asfinag.vmis2.synchronize.vd;

import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.springframework.kafka.test.utils.ContainerTestUtils.waitForAssignment;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureManager;
import de.heuboe.asfinag.vmis2.synchronize.vd.config.AlgoParameterIdProperties;
import de.heuboe.asfinag.vmis2.synchronize.vd.config.SynchronizeVdProperties;
import de.heuboe.asfinag.vmis2.synchronize.vd.services.AlgoContext;
import de.heuboe.idgenerator.generator.IDGenerator;
import eu.vmis_ehe.vmis2.configservice.pojo.PAreaChange;
import eu.vmis_ehe.vmis2.configservice.pojo.PConfigItemType;
import eu.vmis_ehe.vmis2.configservice.pojo.PDataChange;
import eu.vmis_ehe.vmis2.configservice.pojo.PDataChanges;
import eu.vmis_ehe.vmis2.configservice.pojo.PItemChange;
import eu.vmis_ehe.vmis2.receiving.processing.data.pojo.PShortTermCollectedDataLanes;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEBetriebsparameter;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEBetriebsparameterList;
import lombok.extern.slf4j.Slf4j;

@EnableAutoConfiguration
@EnableKafka
@SpringBootTest(classes = DataChangeTestConfig.class, properties = {
        "spring.kafka.client-id=SYNC-APP-DATACHANGE-TEST",
        "spring.main.allow-bean-definition-overriding=true",
        "de.heuboe.asfinag.vmis2.synchronize.vd.fakeInfraParams=true",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData0TopicTemplate=DC-TEST-{centreTopic}-tlsin-LVEErgebnisVersion0",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData1TopicTemplate=DC-TEST-{centreTopic}-tlsin-LVEErgebnisVersion1",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData2TopicTemplate=DC-TEST-{centreTopic}-tlsin-LVEErgebnisVersion2",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData3TopicTemplate=DC-TEST-{centreTopic}-tlsin-LVEErgebnisVersion3",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData4TopicTemplate=DC-TEST-{centreTopic}-tlsin-LVEErgebnisVersion4",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData5TopicTemplate=DC-TEST-{centreTopic}-tlsin-LVEErgebnisVersion5",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData6TopicTemplate=DC-TEST-{centreTopic}-tlsin-LVEErgebnisVersion6",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsSingleVehDataTopicTemplate=DC-TEST-{centreTopic}-tlsin-LVEKfzEinzeldaten",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsSingleVehCollectedDataTopicTemplate=DC-TEST-{centreTopic}-tlsin-LVEKfzEinzeldatenSammelmeldung",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsErrorTopicTemplate=DC-TEST-{centreTopic}-tlsin-LVEDeFehler",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsOperatingParamTopicTemplate=DC-TEST-{centreTopic}-tlsin-LVEBetriebsparameter",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsChannelControlTopicTemplate=DC-TEST-{centreTopic}-tlsin-LVEKanalsteuerung",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsSysErrorTopicTemplate=DC-TEST-{centreTopic}-tlsin-SYSFehlerDUE",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsTrafficCategoriesParamTopicTemplate=DC-TEST-{systemWideShortcut}-tlsout-LVEGeschwindigkeitsklassenKurzSoll",
        "de.heuboe.asfinag.vmis2.synchronize.vd.parameterSystemTopics[0]=DC-TEST-{systemWideShortcut}-Parameter-Zeitsynchronisation",
        "de.heuboe.asfinag.vmis2.synchronize.vd.parameterRoadTopics[0]=DC-TEST-{systemWideShortcut}-Parameter-LogischePassivierungVDE",
        "de.heuboe.asfinag.vmis2.synchronize.vd.dataChangeTopicTemplate=DC-TEST-{centreTopic}-DataChange",
        "de.heuboe.asfinag.vmis2.synchronize.vd.maxNrOfRestartRetries=4",
        "de.heuboe.asfinag.vmis2.synchronize.vd.restartsWithinTimeRange=5",
        "de.heuboe.asfinag.vmis2.synchronize.vd.collectedDataLaneTopicTemplate=DC-TEST-{centreTopic}-ShortTermCollectedDataLane",
        "de.heuboe.asfinag.vmis2.synchronize.vd.collectedTrafficCategoriesLaneTopicTemplate=DC-TEST-{centreTopic}-ShortTermCollectedTrafficCategoriesLane",
        "de.heuboe.asfinag.vmis2.synchronize.vd.colletedOpcUaDataLaneTopicTemplate=DC-TEST-{centreTopic}-ShortTermCollectedOpcUaDataLane",
        "de.heuboe.asfinag.vmis2.synchronize.vd.discardedDataLaneTopicTemplate=DC-TEST-{centreTopic}-DiscardedShortTermCollectedDataLane",
        "de.heuboe.asfinag.vmis2.synchronize.vd.topicTmpltControlSequence=DC-TEST-{systemWideShortcut}-tlsout-SYSSteuerSequenz",
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
@ActiveProfiles({"test", "UseServices" })
@DirtiesContext
@EmbeddedKafka(partitions = 1,
    controlledShutdown = true,
    brokerProperties = {"log.dir=target/kafka${random.int}" },
    topics = {
        "DC-TEST-VRZ-Parameter-LogischePassivierungVDE",
        "DC-TEST-VRZ-Parameter-Zeitsynchronisation",
        "DC-TEST-WIE-tlsin-LVEErgebnisVersion0",
        "DC-TEST-WIE-tlsin-LVEErgebnisVersion1",
        "DC-TEST-WIE-tlsin-LVEErgebnisVersion2",
        "DC-TEST-WIE-tlsin-LVEErgebnisVersion3",
        "DC-TEST-WIE-tlsin-LVEErgebnisVersion4",
        "DC-TEST-WIE-tlsin-LVEErgebnisVersion5",
        "DC-TEST-WIE-tlsin-LVEErgebnisVersion6",
        "DC-TEST-WIE-tlsin-LVEKfzEinzeldaten",
        "DC-TEST-WIE-tlsin-LVEKfzEinzeldatenSammelMeldung",
        "DC-TEST-WIE-tlsin-LVEDeFehler",
        "DC-TEST-WIE-tlsin-LVEBetriebsparameter",
        "DC-TEST-WIE-tlsin-LVEKanalsteuerung",
        "DC-TEST-WIE-tlsin-SYSFehlerDUE",
        "DC-TEST-VRZ-tlsout-LVEGeschwindigkeitsklassenKurzSoll",
        "DC-TEST-WIE-ShortTermCollectedDataLane",
        "DC-TEST-WIE-ShortTermCollectedTrafficCategoriesLane",
        "DC-TEST-WIE-ShortTermCollectedOpcUaDataLane",
        "DC-TEST-WIE-DiscardedShortTermCollectedDataLane",
        "DC-TEST-VRZ-tlsout-SYSSteuerSequenz",
        "DC-TEST-WIE-DataChange"
    }
)
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.consumer.group-id=EmbeddedKafkaTest",
    "spring.kafka.listener.missing-topics-fatal=true",
    "spring.kafka.consumer.enable-auto-commit=false"})
@Slf4j
public class DataChangeTest {
    
    protected static final String DATA_BASE_PATH = "src/test/resources/testData/";
    private final static String TEST_ID = "MQ_A23_1_200_F1";
    // we only check data for roadId "A23_1", so EXPTECTED_RESULT_SIZE is the number of VDE_SENSORs laying on this road.
    private final static int EXPECTED_RESULT_SIZE = 77;
    // EXPECTED_RESULT_SIZE_VDE_UPDATE is the number of VDE_SENSORs 
    private final static int EXPECTED_RESULT_SIZE_VDE_UPDATE = 7;

    private static final String TEST_COLLECTED = "DC-TEST-WIE-ShortTermCollectedDataLane";

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

    class Result {
        public List<ConsumerRecord<String, byte[]>> records;
        public long newPosition;

        public Result(List<ConsumerRecord<String, byte[]>> records, long newPosition) {
            super();
            this.records = records;
            this.newPosition = newPosition;
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

//        // Read synchronize algo parameter from file
//        ParameterSetList.Builder algoParaBuilder = ParameterSetList.newBuilder();
//        String json = new String(Files.readAllBytes(
//                Paths.get(DATA_BASE_PATH + "Parameter/PParmeterSetList#ZeitsychronisationINSTANZ1.json")));
//        JsonFormat.parser().merge(json, algoParaBuilder);
//
//        PParameterSetList algoPara = PParameterSetList.from(algoParaBuilder.build());
//        assertNotNull(algoPara);
//        
//        // Send algo parameter
//        kafkaTemplate.send(MessageBuilder.withPayload(algoPara)
//                .setHeader(KafkaHeaders.TOPIC, appProperties.getParameterSystemTopics().get(0))
//                .setHeader(KafkaHeaders.MESSAGE_KEY, "Zeitsynchronisation-WIE-INSTANZ1")
//                .setHeader(KafkaConstants.KAFKA_HEADER_DEFINITION_SET_ID, paramIds.getTimeSyncDefSetId())
//                .setHeader(KafkaConstants.KAFKA_HEADER_ROAD_ID, "")
//                .setHeader(KafkaConstants.KAFKA_HEADER_SYSTEM, "WIE")
//                .setHeader(KafkaConstants.KAFKA_HEADER_INSTANCE, appProperties.getInstanceName())
//                .build()).get();
        Thread.sleep(2000); //Allow initialization
    }
    
    @AfterEach
    public void cleanUp() throws InterruptedException {
        Thread.sleep(5000); //give app time to close tests before shutdown
        log.info("DataChangeTest:Clean up called => Destroy kafkaTemplate bean");
        kafkaTemplate.destroy();
    }

    @Test
    void testFullCycle() throws IOException, InterruptedException, ExecutionException {
        initConsumers();
        Instant now = Instant.now();
        
        Thread.sleep(30000); //NOSONAR: give application a chance to be initialized
        log.info("TEST: check if first set of data is processed; expected result size: {}", EXPECTED_RESULT_SIZE);
        checkA23Data(now, EXPECTED_RESULT_SIZE);

        //Creating a new config trigger - this rVMZ - update VDE sensors
        log.info("Sent DataChange with changed VDE sensors...");
        Thread.sleep(3000);
        List<PConfigItemType> typeList = List.of(PConfigItemType.VDE_SENSOR, PConfigItemType.RST);
        PAreaChange pAreaChange =
                PAreaChange.builder().roadId("A23_1").roadComponentId("A23_1").featureChangesList(List.of(
                        PItemChange.builder().itemTypesList(typeList).build())).build();
        PDataChanges pDataChanges = PDataChanges.builder()
                .dataChangesList(List.of(PDataChange.builder().rVmzId("WIE").roadChangesList(List.of(pAreaChange)).build()))
                .gipChanged(false).iid(idGenerator.newID()).build();
        TestUtils.updateType = TestUtils.UpdateType.UPDATE;
        kafkaTemplate.send(MessageBuilder.withPayload(pDataChanges).setHeader(KafkaHeaders.TOPIC, "DC-TEST-WIE-DataChange")
                .setHeader(KafkaHeaders.KEY, "Trigger").build()).get();

        Thread.sleep(90000);
        log.info("TEST: check if second set of data is processed; expected result size: {}", EXPECTED_RESULT_SIZE_VDE_UPDATE);
        consumerCollected = consumerFactory.createConsumer("consumerDataChang2");
        partitionCollected = new TopicPartition(TEST_COLLECTED, 0);
        topicPartitionsCollected = Collections.singletonList(partitionCollected);
        consumerCollected.assign(topicPartitionsCollected);
        consumerCollected.seekToEnd(topicPartitionsCollected);
        oldPosCollected = consumerCollected.position(partitionCollected) + 1;
        checkA23Data(Instant.now(), EXPECTED_RESULT_SIZE_VDE_UPDATE);
        
        TestUtils.updateType = TestUtils.UpdateType.DEFAULT;
        
    }

    private void checkA23Data(Instant now, int expectedSize) throws InvalidProtocolBufferException {
        log.debug("#### Listen to topic from timestamp '{}'; now it is: {}", now, Instant.now());
        assertTimeoutPreemptively(ofSeconds(90), () -> {
            Result res = null;
            while (res == null) {
                res = checkTopic(consumerCollected, partitionCollected, topicPartitionsCollected, now, oldPosCollected);
                boolean gotIt = false;
                if(res != null) {
                    for(ConsumerRecord<String, byte[]> rec : res.records) {
                        if("A23_1".equals(rec.key())) {
                            gotIt = true;
                            PShortTermCollectedDataLanes collectedData = PShortTermCollectedDataLanes.fromBytes(rec.value());
                            assertEquals(expectedSize, collectedData.getDataList().size());
                        }
                    }
                }
                if(!gotIt) {
                    res = null;
                }
            }
        });
    }

    private void initConsumers() {
        consumerCollected = consumerFactory.createConsumer("consumerDataChange");
        partitionCollected = new TopicPartition(TEST_COLLECTED, 0);
        topicPartitionsCollected = Collections.singletonList(partitionCollected);
        consumerCollected.assign(topicPartitionsCollected);
        consumerCollected.seekToEnd(topicPartitionsCollected);
        oldPosCollected = consumerCollected.position(partitionCollected) + 1;
    }

    protected Result checkTopic(Consumer<String, byte[]> consumer, TopicPartition partition,
            List<TopicPartition> topicPartitions, Instant now, long oldPos) {
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
                return null;
            }
            List<ConsumerRecord<String, byte[]>> recs = records.records(partition);
            if (recs == null || recs.isEmpty()) {
                return null;
            }
            List<ConsumerRecord<String, byte[]>> sorted =
                    recs.stream().sorted((r1, r2) -> Long.compare(r1.timestamp(), r2.timestamp()))
                            .collect(Collectors.toList());
            long lNow = now.toEpochMilli();
            ConsumerRecord<String, byte[]> last = sorted.get(sorted.size() - 1);

            nextPos = last.offset() + 1;
            if (last.timestamp() < lNow) {
                log.info("CHECKTOPIC: Got a record, but its timestamp ({}) is BEFORE listening timestamp ({})",
                        last.timestamp(),  lNow);
                return null;
            } else {
            }
            return new Result(sorted, nextPos);
        }
        return null;
    }
}
