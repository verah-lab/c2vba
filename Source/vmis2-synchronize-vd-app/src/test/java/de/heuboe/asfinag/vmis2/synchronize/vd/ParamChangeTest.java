package de.heuboe.asfinag.vmis2.synchronize.vd;

import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.springframework.kafka.test.utils.ContainerTestUtils.waitForAssignment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
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
import com.google.protobuf.util.JsonFormat;

import akka.actor.ActorSystem;
import de.heuboe.asfinag.control.base.actors.SpringExtension;
import de.heuboe.asfinag.vmis2.constants.KafkaConstants;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureManager;
import de.heuboe.asfinag.vmis2.synchronize.vd.config.AlgoParameterIdProperties;
import de.heuboe.asfinag.vmis2.synchronize.vd.config.SynchronizeVdProperties;
import de.heuboe.asfinag.vmis2.synchronize.vd.services.AlgoContext;
import de.heuboe.idgenerator.generator.IDGenerator;
import eu.vmis_ehe.vmis2.paramservice.ParameterSetList;
import eu.vmis_ehe.vmis2.paramservice.pojo.PParameterSetList;
import eu.vmis_ehe.vmis2.receiving.processing.data.pojo.PShortTermCollectedDataLanes;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEBetriebsparameter;
import eu.vmis_ehe.vmis2.tls.received.pojo.PLVEBetriebsparameterList;
import lombok.extern.slf4j.Slf4j;

@EnableAutoConfiguration
@SpringBootTest(classes = ParamChangeTestConfig.class, properties = {
        "spring.kafka.client-id=SYNC-APP-PARAMCHANGE-TEST",
        "spring.main.allow-bean-definition-overriding=true",
        "de.heuboe.asfinag.vmis2.synchronize.vd.fakeInfraParams=true",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData0TopicTemplate=PA-TEST-{centreTopic}-tlsin-LVEErgebnisVersion0",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData1TopicTemplate=PA-TEST-{centreTopic}-tlsin-LVEErgebnisVersion1",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData2TopicTemplate=PA-TEST-{centreTopic}-tlsin-LVEErgebnisVersion2",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData3TopicTemplate=PA-TEST-{centreTopic}-tlsin-LVEErgebnisVersion3",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData4TopicTemplate=PA-TEST-{centreTopic}-tlsin-LVEErgebnisVersion4",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData5TopicTemplate=PA-TEST-{centreTopic}-tlsin-LVEErgebnisVersion5",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsData6TopicTemplate=PA-TEST-{centreTopic}-tlsin-LVEErgebnisVersion6",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsSingleVehDataTopicTemplate=PA-TEST-{centreTopic}-tlsin-LVEKfzEinzeldaten",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsSingleVehCollectedDataTopicTemplate=PA-TEST-{centreTopic}-tlsin-LVEKfzEinzeldatenSammelmeldung",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsErrorTopicTemplate=PA-TEST-{centreTopic}-tlsin-LVEDeFehler",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsOperatingParamTopicTemplate=PA-TEST-{centreTopic}-tlsin-LVEBetriebsparameter",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsChannelControlTopicTemplate=PA-TEST-{centreTopic}-tlsin-LVEKanalsteuerung",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsSysErrorTopicTemplate=PA-TEST-{centreTopic}-tlsin-SYSFehlerDUE",
        "de.heuboe.asfinag.vmis2.synchronize.vd.tlsTrafficCategoriesParamTopicTemplate=PA-TEST-{systemWideShortcut}-tlsout-LVEGeschwindigkeitsklassenKurzSoll",
        "de.heuboe.asfinag.vmis2.synchronize.vd.parameterSystemTopics[0]=PA-TEST-{systemWideShortcut}-Parameter-Zeitsynchronisation",
        "de.heuboe.asfinag.vmis2.synchronize.vd.parameterRoadTopics[0]=PA-TEST-{systemWideShortcut}-Parameter-LogischePassivierungVDE",
        "de.heuboe.asfinag.vmis2.synchronize.vd.dataChangeTopicTemplate=PA-TEST-{centreTopic}-DataChange",
        "de.heuboe.asfinag.vmis2.synchronize.vd.maxNrOfRestartRetries=4",
        "de.heuboe.asfinag.vmis2.synchronize.vd.restartsWithinTimeRange=5",
        "de.heuboe.asfinag.vmis2.synchronize.vd.collectedDataLaneTopicTemplate=PA-TEST-{centreTopic}-ShortTermCollectedDataLane",
        "de.heuboe.asfinag.vmis2.synchronize.vd.collectedTrafficCategoriesLaneTopicTemplate=PA-TEST-{centreTopic}-ShortTermCollectedTrafficCategoriesLane",
        "de.heuboe.asfinag.vmis2.synchronize.vd.colletedOpcUaDataLaneTopicTemplate=PA-TEST-{centreTopic}-ShortTermCollectedOpcUaDataLane",
        "de.heuboe.asfinag.vmis2.synchronize.vd.discardedDataLaneTopicTemplate=PA-TEST-{centreTopic}-DiscardedShortTermCollectedDataLane",
        "de.heuboe.asfinag.vmis2.synchronize.vd.topicTmpltControlSequence=PA-TEST-{systemWideShortcut}-tlsout-SYSSteuerSequenz",
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(partitions = 1,
    controlledShutdown = true,
    brokerProperties = {"log.dir=target/kafka${random.int}" },
    topics = {
        "PA-TEST-VRZ-Parameter-LogischePassivierungVDE",
        "PA-TEST-VRZ-Parameter-Zeitsynchronisation",
        "PA-TEST-WIE-tlsin-LVEErgebnisVersion0",
        "PA-TEST-WIE-tlsin-LVEErgebnisVersion1",
        "PA-TEST-WIE-tlsin-LVEErgebnisVersion2",
        "PA-TEST-WIE-tlsin-LVEErgebnisVersion3",
        "PA-TEST-WIE-tlsin-LVEErgebnisVersion4",
        "PA-TEST-WIE-tlsin-LVEErgebnisVersion5",
        "PA-TEST-WIE-tlsin-LVEErgebnisVersion6",
        "PA-TEST-WIE-tlsin-LVEKfzEinzeldaten",
        "PA-TEST-WIE-tlsin-LVEKfzEinzeldatenSammelMeldung",
        "PA-TEST-WIE-tlsin-LVEDeFehler",
        "PA-TEST-WIE-tlsin-LVEBetriebsparameter",
        "PA-TEST-WIE-tlsin-LVEKanalsteuerung",
        "PA-TEST-WIE-tlsin-SYSFehlerDUE",
        "PA-TEST-VRZ-tlsout-LVEGeschwindigkeitsklassenKurzSoll",
        "PA-TEST-WIE-ShortTermCollectedDataLane",
        "PA-TEST-WIE-ShortTermCollectedTrafficCategoriesLane",
        "PA-TEST-WIE-ShortTermCollectedOpcUaDataLane",
        "PA-TEST-WIE-DiscardedShortTermCollectedDataLane",
        "PA-TEST-VRZ-tlsout-SYSSteuerSequenz",
        "PA-TEST-WIE-DataChange"
    }
)
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.consumer.group-id=EmbeddedKafkaTest",
    "spring.kafka.listener.missing-topics-fatal=true",
    "spring.kafka.consumer.enable-auto-commit=false"})
@Slf4j
public class ParamChangeTest {
    
    protected static final String DATA_BASE_PATH = "src/test/resources/testData/";
    private final static String TEST_ID = "MQ_A23_1_200_F1";

    private static final String TEST_COLLECTED = "PA-TEST-WIE-ShortTermCollectedDataLane";

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

        kafkaTemplate.send(MessageBuilder
                .withPayload(PLVEBetriebsparameterList.builder().iid(idGenerator.newID())
                        .elementsList(Collections.singletonList(pb)).build())
                .setHeader(KafkaHeaders.TOPIC, algoContext.getTopicTlsOperatingParam())
                .setHeader(KafkaHeaders.KEY, TEST_ID).build()).get();

        // initial time sync param with timeout 40 seconds (1 minute values).
        //sendTimeSyncParam(DATA_BASE_PATH + "Parameter/PParmeterSetList#ZeitsychronisationINSTANZ1.json");
        
    }
    
    @AfterEach
    public void cleanUp() throws InterruptedException {
        Thread.sleep(10000); //give app time to close tests before shutdown
        log.info("ParamChangeTest:Clean up called => Destroy kafkaTemplate bean");
        kafkaTemplate.destroy();
    }
    
    @Test
    void testFullCycle() throws IOException, InterruptedException, ExecutionException {
        initConsumers();
        Instant now = Instant.now();
        
        Thread.sleep(30000); //NOSONAR: give application a chance to be initialized
        log.info("TEST: check if first set of data is processed; expected timeout second: 40");
        checkA23Data(now, 40);
        
        // time sync parameter with timeout 33 seconds (1 minute values).
        sendTimeSyncParam(DATA_BASE_PATH + "Parameter/PParmeterSetList#Zeitsychronisation33INSTANZ1.json");
     
        Thread.sleep(30000);
        consumerCollected = consumerFactory.createConsumer("consumer2");
        partitionCollected = new TopicPartition(TEST_COLLECTED, 0);
        topicPartitionsCollected = Collections.singletonList(partitionCollected);
        consumerCollected.assign(topicPartitionsCollected);
        consumerCollected.seekToEnd(topicPartitionsCollected);
        oldPosCollected = consumerCollected.position(partitionCollected) + 1;

        log.info("TEST: check if second set of data is processed; expected timeout second: 33");
        checkA23Data(now, 33);
    }

    private void checkA23Data(Instant now, int expectedSecond) throws InvalidProtocolBufferException {
        assertTimeoutPreemptively(ofSeconds(90), () -> {
            Result res = null;
            while (res == null) {
                res = checkTopic(consumerCollected, partitionCollected, topicPartitionsCollected, now, oldPosCollected);
                if(res != null) {
                    for(ConsumerRecord<String, byte[]> rec : res.records) {
                        PShortTermCollectedDataLanes collectedData = PShortTermCollectedDataLanes.fromBytes(rec.value());
                        assertEquals(expectedSecond, collectedData.getDataList().get(0).getProcessingTime()
                                .atZone(ZoneOffset.UTC).getSecond());
                        log.info("Expected seconds {} received", expectedSecond);
                    }
                }
            }
        }, "execution timed out after " + 90 + " s (while reading collected data from '" + TEST_COLLECTED + "')");
    }

    private void sendTimeSyncParam(String file) throws IOException, InterruptedException, ExecutionException {
     // Read synchronize algo parameter from file
        ParameterSetList.Builder algoParaBuilder = ParameterSetList.newBuilder();
        String json = new String(Files.readAllBytes(Paths.get(file)));
        JsonFormat.parser().merge(json, algoParaBuilder);

        PParameterSetList algoPara = PParameterSetList.from(algoParaBuilder.build());
        assertNotNull(algoPara);
        
        // Send algo parameter
        String topic = appProperties.getParameterSystemTopics().get(0)
                .replace("{systemWideShortcut}", appProperties.getSystemWideShortcut());
        kafkaTemplate.send(MessageBuilder.withPayload(algoPara)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader(KafkaHeaders.KEY, "Zeitsynchronisation-WIE-INSTANZ1")
                .setHeader(KafkaConstants.KAFKA_HEADER_DEFINITION_SET_ID, paramIds.getTimeSyncDefSetId())
                //.setHeader(KafkaConstants.KAFKA_HEADER_ROAD_ID, "")
                .setHeader(KafkaConstants.KAFKA_HEADER_SYSTEM, "WIE")
                .setHeader(KafkaConstants.KAFKA_HEADER_INSTANCE, appProperties.getInstanceName())
                .build()).get();
        Thread.sleep(2000); //Allow initialization
    }
    
    private void initConsumers() {
        consumerCollected = consumerFactory.createConsumer("consumer");
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
