package de.heuboe.tls.sequencer.test.helper;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import de.heuboe.idgenerator.generator.IDGenerator;
import de.heuboe.test.KafkaTestcontainer;
import de.heuboe.tls.grammar.base.ValueCollection;
import de.heuboe.tls.grammar.interfaces.Variable;
import de.heuboe.tls.grammar.sequencer.ProtectedBasicVariable;
import de.heuboe.tls.received.*;
import de.heuboe.tls.received.pojo.*;
import de.heuboe.tls.send.SteuerSequenz;
import de.heuboe.tls.send.SteuerSequenzList;
import de.heuboe.tls.send.pojo.PSteuerSequenz;
import de.heuboe.tls.send.pojo.PSteuerSequenzList;
import de.heuboe.tls.sequencer.config.SequencerProperties;
import de.heuboe.tls.sequencer.parser.Parser;
import eu.vmis_ehe.vmis2.configservice.AreaChange;
import eu.vmis_ehe.vmis2.configservice.DataChange;
import eu.vmis_ehe.vmis2.configservice.DataChanges;
import eu.vmis_ehe.vmis2.configservice.ItemChange;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Slf4j
public class TestBase {

    @Autowired
    protected KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    protected ConsumerFactory<String, byte[]> consumerFactory;

    @Autowired
    protected Parser parser;

    @Autowired
    protected IDGenerator idGenerator;

    @Autowired
    protected MockedCfgGetter mockedCfgGetter;

    @Autowired
    protected SequencerProperties properties;

    protected Consumer<String, byte[]> consumer;

    protected static KafkaTestcontainer testContainer = new KafkaTestcontainer();

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        testContainer.startContainer(registry);
    }

    private static final String SPEC_FILE = "spec.yaml";
    public int historyMessages = 500000;
    protected String lastSendMessageId = "";

    protected static Timestamp getTime() {
        Instant time = Instant.now();
        return Timestamp.newBuilder()
                .setSeconds(time.getEpochSecond())
                .setNanos(time.getNano())
                .build();
    }

    public static void fillMessageBuilder(Message.Builder object, String contentFileName) throws IOException {
        com.google.protobuf.util.JsonFormat.parser().merge(
                new String(Files.readAllBytes(new File(TestConsts.DATA_BASE_PATH + contentFileName).toPath())),
                object
        );
    }

    // ------------------------------ Kafka Consumer method ------------------------------

    public ConsumerRecords<String, byte[]> getMessage(Long delay) throws InterruptedException {
        // wait some time to let the sequencer do his work
        Thread.sleep(2000 + delay);

        ConsumerRecords<String, byte[]> records = null;

        // get all records from topic
        while (records == null || records.isEmpty()) {
            Thread.sleep(500);
            records = consumer.poll(Duration.ofMillis(2000));
        }

        return records;
    }

    // ------------------------------ Kafka Sending method -------------------------------

    private ConsumerRecords<String, byte[]> sendToKafka(String id, String iid, byte[] object, String className,
                                                        boolean sequencerMessage, Long delay, String... topic)
            throws ExecutionException, InterruptedException {
        // build record to send to kafka
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic[0], id, object);
        record.headers().add("X-Protobuf-Type", className.getBytes());
        record.headers().add("X-Protobuf-InterfaceVersion", "1.0.0".getBytes());
        record.headers().add("X-IID", iid.getBytes());
        if (sequencerMessage) {
            record.headers()
                    .add(properties.getHeaderSequencerMarker(), properties.getHeaderSequencerContent().getBytes());
        }

        // save for evaluation the correct sent message in test
        lastSendMessageId = id;

        List<TopicPartition> tpList = new ArrayList<>();

        // build consumer to receive objects
        Arrays.stream(topic).forEach(t -> {
            tpList.add(new TopicPartition(t, 0));
        });

        consumer.assign(tpList);
        consumer.seekToBeginning(tpList);

        // send record to kafka
        kafkaTemplate.send(record).get();

        // read records from kafka
        return getMessage(delay);
    }

    private String[] constructTopics(String inTopic, String outTopic, String[] resultTopics) {
        String[] topics = new String[resultTopics.length + 2];
        topics[0] = inTopic;
        topics[1] = outTopic;
        System.arraycopy(resultTopics, 0, topics, 2, resultTopics.length);
        return topics;
    }

    public HashMap<String, ConsumerRecord<String, byte[]>> extractMessages(ConsumerRecords<String, byte[]> resultList) {

        HashMap<String, ConsumerRecord<String, byte[]>> result = new HashMap<>();

        // always return the last entry for the current record
        resultList.partitions().forEach(partition -> {
            result.put(partition.topic(), resultList.records(partition).get(resultList.records(partition).size() - 1));
        });

        return result;
    }

    // ------------------------------ helper methods ------------------------------
    protected Map<String, Variable> loadVariables() {

        String currentValue = "";
        String currentKey = "";
        Map<String, Variable> variableTable = new HashMap<>();
        String separator = System.getProperty("file.separator");

        // load yaml file as InputStream
        try (InputStream yamlStream = new FileInputStream(properties.getScriptPath() + separator + SPEC_FILE)) {

            log.info("Loading spec file '{}{}{}'.", new File(properties.getScriptPath()).getPath(), separator, SPEC_FILE);

            // load yaml input
            Yaml yaml = new Yaml();
            Map<String, Object> spec = yaml.load(yamlStream);

            // load data from spec.yaml
            for (Map.Entry<String, Object> entry : spec.entrySet()) {
                currentValue = entry.getValue().toString();
                currentKey = entry.getKey();

                // differ between hex and numeric entries and save them on different ways in the interpreter environment
                if (currentValue.startsWith("0x")) {
                    // decode hex values into integer
                    variableTable.put(currentKey, new ProtectedBasicVariable(
                            currentKey, new ValueCollection.IntValue(Integer.decode(currentValue))));
                } else if (currentValue.startsWith("[") && currentValue.endsWith("]")) {
                    // ignore lists
                } else {
                    variableTable.put(currentKey, new ProtectedBasicVariable(
                            currentKey, new ValueCollection.IntValue(Integer.parseInt(currentValue))));
                }
            }
            log.info("{} variables loaded from specification file '{}'.", variableTable.size(), SPEC_FILE);
        } catch (NumberFormatException nfe) {
            String errMsg = "The hex value of the specification parameter '" + currentKey + "' in '" + SPEC_FILE
                    + "' could not be decoded to an integer value!";
            throw new NumberFormatException(errMsg);
        } catch (FileNotFoundException e) {
            String errMsg = "Specification file '" + SPEC_FILE + "' could not be loaded at path ' "
                    + properties.getScriptPath() + "'!";
            log.error(errMsg);
            System.exit(-1);
        } catch (IOException e) {
            String errMsg = "Failed to load specification file '" + SPEC_FILE + "' with error message "
                    + e.getLocalizedMessage();
            log.error(errMsg);
            System.exit(-1);
        }

        return variableTable;
    }

    protected ConsumerRecords<String, byte[]> switchConfig(Long delay) throws Exception {
        mockedCfgGetter.switchConfig(); // add a virtual de
        // now using another configuration

        /* init change */
        eu.vmis_ehe.vmis2.configservice.DataChange.Builder b1 = DataChange.newBuilder();
        DataChanges.Builder b = DataChanges.newBuilder();
        b1.setRVmzId("WIE");
        b1.addRoadChanges(AreaChange.newBuilder().addGeoChangesValue(1).addFeatureChanges(ItemChange.newBuilder().addAqTypesValue(1).build()).build());
        b.addDataChanges(b1.build());
        b.setIid("HänselUndGretel");
        DataChanges changes = b.build();

        return sendToKafka("blub", changes.getIid(), changes.toByteArray(),
                changes.getClass().getName(), false, delay, "WIE-ConfigSrv-DataChange");
    }

    // ------------------------------ object handler methods ------------------------------

    protected ConsumerRecords<String, byte[]> handleSYSDeFehler(String contentFile, String contentListFile, Long delay,
                                                                String... resultTopics)
            throws IOException, ExecutionException, InterruptedException {
        String baseTopic = "SYSDeFehler";
        String inTopic = properties.getReceiveTopicPrefix() + baseTopic + properties.getReceiveTopicSuffix();
        String outTopic = properties.getSendTopicPrefix() + baseTopic + properties.getSendTopicSuffix();

        // build input object
        SYSDeFehler.Builder singeltonBuilder = SYSDeFehler.newBuilder();
        TestBase.fillMessageBuilder(singeltonBuilder, contentFile);
        SYSDeFehler singletonPojo = PSYSDeFehler.to(PSYSDeFehler.from(singeltonBuilder.build()));

        SYSDeFehlerList.Builder listBuilder = SYSDeFehlerList.newBuilder();
        TestBase.fillMessageBuilder(listBuilder, contentListFile);
        SYSDeFehlerList listPojo =
                PSYSDeFehlerList.to(PSYSDeFehlerList.from(listBuilder.addElements(singletonPojo).build()));

        log.debug("Object send to kafka topic '{}':\r\n{}", inTopic, listPojo);

        // send object to kafka
        return sendToKafka(singletonPojo.getId(), listPojo.getIid(), listPojo.toByteArray(),
                listPojo.getClass().getName(), false, delay, constructTopics(inTopic, outTopic, resultTopics));
    }

    protected ConsumerRecords<String, byte[]> handleSYSKommunikationsstatus(String contentFile, String contentListFile,
                                                                            Long delay,
                                                                            String... resultTopics)
            throws IOException, ExecutionException, InterruptedException {
        String baseTopic = "SYSKommunikationsstatus";
        String inTopic = properties.getReceiveTopicPrefix() + baseTopic + properties.getReceiveTopicSuffix();
        String outTopic = properties.getSendTopicPrefix() + baseTopic + properties.getSendTopicSuffix();

        // build input object
        SYSKommunikationsstatus.Builder singeltonBuilder = SYSKommunikationsstatus.newBuilder();
        TestBase.fillMessageBuilder(singeltonBuilder, contentFile);
        SYSKommunikationsstatus singletonPojo =
                PSYSKommunikationsstatus.to(PSYSKommunikationsstatus.from(singeltonBuilder.build()));

        SYSKommunikationsstatusList.Builder listBuilder = SYSKommunikationsstatusList.newBuilder();
        TestBase.fillMessageBuilder(listBuilder, contentListFile);
        SYSKommunikationsstatusList listPojo =
                PSYSKommunikationsstatusList.to(
                        PSYSKommunikationsstatusList.from(listBuilder.addElements(singletonPojo).build()));

        log.debug("Object send to kafka topic '{}':\r\n{}", inTopic, listPojo);

        // send object to kafka
        return sendToKafka(singletonPojo.getId(), listPojo.getIid(), listPojo.toByteArray(),
                listPojo.getClass().getName(), false, delay, constructTopics(inTopic, outTopic, resultTopics));
    }

    protected ConsumerRecords<String, byte[]> handleLVEDeFehler(String contentFile, Long delay,
                                                                String... resultTopics)
            throws IOException, ExecutionException, InterruptedException {
        String baseTopic = "LVEDeFehler";
        String inTopic = properties.getReceiveTopicPrefix() + baseTopic + properties.getReceiveTopicSuffix();
        String outTopic = properties.getSendTopicPrefix() + baseTopic + properties.getSendTopicSuffix();

        // build input object
        LVEDeFehler.Builder singeltonBuilder = LVEDeFehler.newBuilder();
        TestBase.fillMessageBuilder(singeltonBuilder, contentFile);
        LVEDeFehler singletonPojo = PLVEDeFehler.to(PLVEDeFehler.from(singeltonBuilder.build()));

        LVEDeFehlerList listPojo = PLVEDeFehlerList.to(PLVEDeFehlerList.from(
                LVEDeFehlerList.newBuilder().setIid(idGenerator.newID()).addElements(singletonPojo).build()));

        log.debug("Object send to kafka topic '{}':\r\n{}", inTopic, listPojo);

        // send object to kafka
        return sendToKafka(singletonPojo.getId(), listPojo.getIid(), listPojo.toByteArray(),
                listPojo.getClass().getName(), false, delay, constructTopics(inTopic, outTopic, resultTopics));
    }

    protected ConsumerRecords<String, byte[]> handleAXLDeFehler(String contentFile, Long delay,
                                                                String... resultTopics)
            throws IOException, ExecutionException, InterruptedException {
        String baseTopic = "AXLDeFehler";
        String inTopic = properties.getReceiveTopicPrefix() + baseTopic + properties.getReceiveTopicSuffix();
        String outTopic = properties.getSendTopicPrefix() + baseTopic + properties.getSendTopicSuffix();

        // build input object
        AXLDeFehler.Builder singeltonBuilder = AXLDeFehler.newBuilder();
        TestBase.fillMessageBuilder(singeltonBuilder, contentFile);
        AXLDeFehler singletonPojo = PAXLDeFehler.to(PAXLDeFehler.from(singeltonBuilder.build()));

        AXLDeFehlerList listPojo = PAXLDeFehlerList.to(PAXLDeFehlerList.from(
                AXLDeFehlerList.newBuilder().setIid(idGenerator.newID()).addElements(singletonPojo).build()));

        log.debug("Object send to kafka topic '{}':\r\n{}", inTopic, listPojo);

        // send object to kafka
        return sendToKafka(singletonPojo.getId(), listPojo.getIid(), listPojo.toByteArray(),
                listPojo.getClass().getName(), false, delay, constructTopics(inTopic, outTopic, resultTopics));
    }

    protected ConsumerRecords<String, byte[]> handleUFDDeFehler(String contentFile, Long delay,
                                                                String... resultTopics)
            throws IOException, ExecutionException, InterruptedException {
        String baseTopic = "UFDDeFehler";
        String inTopic = properties.getReceiveTopicPrefix() + baseTopic + properties.getReceiveTopicSuffix();
        String outTopic = properties.getSendTopicPrefix() + baseTopic + properties.getSendTopicSuffix();

        // build input object
        UFDDeFehler.Builder singeltonBuilder = UFDDeFehler.newBuilder();
        TestBase.fillMessageBuilder(singeltonBuilder, contentFile);
        UFDDeFehler singletonPojo = PUFDDeFehler.to(PUFDDeFehler.from(singeltonBuilder.build()));

        UFDDeFehlerList listPojo = PUFDDeFehlerList.to(PUFDDeFehlerList.from(
                UFDDeFehlerList.newBuilder().setIid(idGenerator.newID()).addElements(singletonPojo).build()));

        log.debug("Object send to kafka topic '{}':\r\n{}", inTopic, listPojo);

        // send object to kafka
        return sendToKafka(singletonPojo.getId(), listPojo.getIid(), listPojo.toByteArray(),
                listPojo.getClass().getName(), false, delay, constructTopics(inTopic, outTopic, resultTopics));
    }

    protected ConsumerRecords<String, byte[]> handleWZGDeFehler(String contentFile, Long delay,
                                                                String... resultTopics)
            throws IOException, ExecutionException, InterruptedException {
        String baseTopic = "WZGDeFehler";
        String inTopic = properties.getReceiveTopicPrefix() + baseTopic + properties.getReceiveTopicSuffix();
        String outTopic = properties.getSendTopicPrefix() + baseTopic + properties.getSendTopicSuffix();

        // build input object
        WZGDeFehler.Builder singeltonBuilder = WZGDeFehler.newBuilder();
        TestBase.fillMessageBuilder(singeltonBuilder, contentFile);
        WZGDeFehler singletonPojo = PWZGDeFehler.to(PWZGDeFehler.from(singeltonBuilder.build()));

        WZGDeFehlerList listPojo = PWZGDeFehlerList.to(PWZGDeFehlerList.from(
                WZGDeFehlerList.newBuilder().setIid(idGenerator.newID()).addElements(singletonPojo).build()));

        log.debug("Object send to kafka topic '{}':\r\n{}", inTopic, listPojo);

        // send object to kafka
        return sendToKafka(singletonPojo.getId(), listPojo.getIid(), listPojo.toByteArray(),
                listPojo.getClass().getName(), false, delay, constructTopics(inTopic, outTopic, resultTopics));
    }

    protected ConsumerRecords<String, byte[]> handleWZGBetriebsart(String contentFile, Long delay,
                                                                   String... resultTopics)
            throws IOException, ExecutionException, InterruptedException {
        String baseTopic = "WZGBetriebsart";
        String inTopic = properties.getReceiveTopicPrefix() + baseTopic + properties.getReceiveTopicSuffix();
        String outTopic = properties.getSendTopicPrefix() + baseTopic + properties.getSendTopicSuffix();

        // build input object
        WZGBetriebsart.Builder singeltonBuilder = WZGBetriebsart.newBuilder();
        TestBase.fillMessageBuilder(singeltonBuilder, contentFile);
        WZGBetriebsart singletonPojo = PWZGBetriebsart.to(PWZGBetriebsart.from(singeltonBuilder.build()));

        WZGBetriebsartList listPojo = PWZGBetriebsartList.to(PWZGBetriebsartList.from(
                WZGBetriebsartList.newBuilder().setIid(idGenerator.newID()).addElements(singletonPojo).build()));

        log.debug("Object send to kafka topic '{}':\r\n{}", inTopic, listPojo);

        // send object to kafka
        return sendToKafka(singletonPojo.getId(), listPojo.getIid(), listPojo.toByteArray(),
                listPojo.getClass().getName(), false, delay, constructTopics(inTopic, outTopic, resultTopics));
    }

    protected ConsumerRecords<String, byte[]> handleVLTDeFehler(String contentFile, Long delay,
                                                                String... resultTopics)
            throws IOException, ExecutionException, InterruptedException {
        String baseTopic = "VLTDeFehler";
        String inTopic = properties.getReceiveTopicPrefix() + baseTopic + properties.getReceiveTopicSuffix();
        String outTopic = properties.getSendTopicPrefix() + baseTopic + properties.getSendTopicSuffix();

        // build input object
        VLTDeFehler.Builder singeltonBuilder = VLTDeFehler.newBuilder();
        TestBase.fillMessageBuilder(singeltonBuilder, contentFile);
        VLTDeFehler singletonPojo = PVLTDeFehler.to(PVLTDeFehler.from(singeltonBuilder.build()));

        VLTDeFehlerList listPojo = PVLTDeFehlerList.to(PVLTDeFehlerList.from(
                VLTDeFehlerList.newBuilder().setIid(idGenerator.newID()).addElements(singletonPojo).build()));

        log.debug("Object send to kafka topic '{}':\r\n{}", inTopic, listPojo);

        // send object to kafka
        return sendToKafka(singletonPojo.getId(), listPojo.getIid(), listPojo.toByteArray(),
                listPojo.getClass().getName(), false, delay, constructTopics(inTopic, outTopic, resultTopics));
    }

    protected ConsumerRecords<String, byte[]> handleSYSZeitsynchronisation(String contentFile, Long delay,
                                                                           String... resultTopics)
            throws IOException, ExecutionException, InterruptedException {
        String baseTopic = "SYSZeitsynchronisation";
        String inTopic = properties.getReceiveTopicPrefix() + baseTopic + properties.getReceiveTopicSuffix();
        String outTopic = properties.getSendTopicPrefix() + baseTopic + properties.getSendTopicSuffix();

        // build input object
        SYSZeitsynchronisation.Builder singeltonBuilder = SYSZeitsynchronisation.newBuilder();
        TestBase.fillMessageBuilder(singeltonBuilder, contentFile);
        SYSZeitsynchronisation singletonPojo = PSYSZeitsynchronisation.to(PSYSZeitsynchronisation.from(singeltonBuilder.build()));

        SYSZeitsynchronisationList listPojo = PSYSZeitsynchronisationList.to(PSYSZeitsynchronisationList.from(
                SYSZeitsynchronisationList.newBuilder().setIid(idGenerator.newID()).addElements(singletonPojo).build()));

        log.debug("Object send to kafka topic '{}':\r\n{}", inTopic, listPojo);

        // send object to kafka
        return sendToKafka(singletonPojo.getId(), listPojo.getIid(), listPojo.toByteArray(),
                listPojo.getClass().getName(), false, delay, constructTopics(inTopic, outTopic, resultTopics));
    }

    protected ConsumerRecords<String, byte[]> handleSYSFehlerDUE(String contentFile, Long delay,
                                                                 String... resultTopics)
            throws IOException, ExecutionException, InterruptedException {
        String baseTopic = "SYSFehlerDUE";
        String inTopic = properties.getReceiveTopicPrefix() + baseTopic + properties.getReceiveTopicSuffix();
        String outTopic = properties.getSendTopicPrefix() + baseTopic + properties.getSendTopicSuffix();

        // build input object
        SYSFehlerDUE.Builder singeltonBuilder = SYSFehlerDUE.newBuilder();
        TestBase.fillMessageBuilder(singeltonBuilder, contentFile);
        SYSFehlerDUE singletonPojo = PSYSFehlerDUE.to(PSYSFehlerDUE.from(singeltonBuilder.build()));

        SYSFehlerDUEList listPojo = PSYSFehlerDUEList.to(PSYSFehlerDUEList.from(
                SYSFehlerDUEList.newBuilder().setIid(idGenerator.newID()).addElements(singletonPojo).build()));

        log.debug("Object send to kafka topic '{}':\r\n{}", inTopic, listPojo);

        // send object to kafka
        return sendToKafka(singletonPojo.getId(), listPojo.getIid(), listPojo.toByteArray(),
                listPojo.getClass().getName(), false, delay, constructTopics(inTopic, outTopic, resultTopics));
    }

    protected ConsumerRecords<String, byte[]> handleSYSKommunikationsstatus(String contentFile, Long delay,
                                                                            String... resultTopics)
            throws IOException, ExecutionException, InterruptedException {
        String baseTopic = "SYSKommunikationsstatus";
        String inTopic = properties.getReceiveTopicPrefix() + baseTopic + properties.getReceiveTopicSuffix();
        String outTopic = properties.getSendTopicPrefix() + baseTopic + properties.getSendTopicSuffix();

        // build input object
        SYSKommunikationsstatus.Builder singeltonBuilder = SYSKommunikationsstatus.newBuilder();
        TestBase.fillMessageBuilder(singeltonBuilder, contentFile);
        SYSKommunikationsstatus singletonPojo = PSYSKommunikationsstatus.to(PSYSKommunikationsstatus.from(singeltonBuilder.build()));

        SYSKommunikationsstatusList listPojo = PSYSKommunikationsstatusList.to(PSYSKommunikationsstatusList.from(
                SYSKommunikationsstatusList.newBuilder().setIid(idGenerator.newID()).addElements(singletonPojo).build()));

        log.debug("Object send to kafka topic '{}':\r\n{}", inTopic, listPojo);

        // send object to kafka
        return sendToKafka(singletonPojo.getId(), listPojo.getIid(), listPojo.toByteArray(),
                listPojo.getClass().getName(), false, delay, constructTopics(inTopic, outTopic, resultTopics));
    }

    protected ConsumerRecords<String, byte[]> handleSYSSteuerSequenz(String contentFile, Long delay,
                                                                     String... resultTopics)
            throws IOException, ExecutionException, InterruptedException {
        String baseTopic = "SYSSteuerSequenz";
        String inTopic = properties.getSendTopicPrefix() + baseTopic + properties.getSendTopicSuffix();
        String outTopic = properties.getSendTopicPrefix() + baseTopic + properties.getSendTopicSuffix();

        // build input object
        SteuerSequenz.Builder singeltonBuilder = SteuerSequenz.newBuilder();
        TestBase.fillMessageBuilder(singeltonBuilder, contentFile);
        SteuerSequenz singletonPojo = PSteuerSequenz.to(PSteuerSequenz.from(singeltonBuilder.build()));

        SteuerSequenzList listPojo = PSteuerSequenzList.to(PSteuerSequenzList.from(
                SteuerSequenzList.newBuilder().setIid(idGenerator.newID()).addElements(singletonPojo).build()));

        log.debug("Object send to kafka topic '{}':\r\n{}", inTopic, listPojo);

        // send object to kafka
        return sendToKafka(singletonPojo.getId(), listPojo.getIid(), listPojo.toByteArray(),
                listPojo.getClass().getName(), false, delay, constructTopics(inTopic, outTopic, resultTopics));
    }
}
