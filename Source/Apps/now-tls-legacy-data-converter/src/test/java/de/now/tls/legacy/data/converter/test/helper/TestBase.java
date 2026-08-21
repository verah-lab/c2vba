package de.now.tls.legacy.data.converter.test.helper;

import de.heuboe.idgenerator.generator.IDGenerator;
import de.heuboe.test.KafkaTestcontainer;
import de.now.tls.legacy.data.converter.config.LegacyDataConverterProperties;
import eu.vmis_ehe.vmis2.configservice.*;
import jakarta.annotation.PostConstruct;
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
public class TestBase {

    @Autowired
    protected KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    protected ConsumerFactory<String, byte[]> consumerFactory;

    @Autowired
    protected IDGenerator idGenerator;

    @Autowired
    private LegacyDataConverterProperties properties;

    @Autowired
    protected MockedCfgGetter mockedCfgGetter;

    protected Consumer<String, byte[]> consumer;

    protected static KafkaTestcontainer testContainer = new KafkaTestcontainer();

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        testContainer.startContainer(registry);
    }

    @PostConstruct
    public void init() throws InterruptedException {
        consumer = consumerFactory.createConsumer();
        Thread.sleep(2000L); // give the KafkaOperatorService some time to initialize
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

    public ConsumerRecords<String, byte[]> sendToKafka(String id, String iid, byte[] object, String className, Long delay,
                                                       String... topic)
            throws ExecutionException, InterruptedException {
        // build record to send to kafka
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic[0], id, object);
        record.headers().add("X-Protobuf-Type", className.getBytes());
        record.headers().add("X-Protobuf-InterfaceVersion", "1.0.0".getBytes());
        record.headers().add("X-IID", iid.getBytes());

        List<TopicPartition> tpList = new ArrayList<>();

        // build consumer to receive objects
        Arrays.stream(topic).forEach(t -> {
            tpList.add(new TopicPartition(t, 0));
        });

        consumer.assign(tpList);
        consumer.seekToBeginning(tpList);

        // send record to kafka
        kafkaTemplate.send(record).get();

        log.info("Sent message to topic '{}'", topic[0]);
        // read records from kafka
        return getMessage(delay);
    }

    public List<ConsumerRecord<String, byte[]>> extractMessages(ConsumerRecords<String, byte[]> resultList, String topic) {

        List<ConsumerRecord<String, byte[]>> result = new ArrayList<>();

        // always return the last entry for the current record
        resultList.partitions().stream()
                .filter(p -> p.topic().equals(topic))
                .forEach(partition -> result.add(resultList.records(partition).get(resultList.records(partition).size() - 1)));

        return result;
    }

    // ---------------------------------- helper method ----------------------------------

    protected ConsumerRecords<String, byte[]> switchConfig(Long delay) throws Exception {
        mockedCfgGetter.switchConfig(); // add a virtual de
        // now using another configuration

        /* init change */
        eu.vmis_ehe.vmis2.configservice.DataChange.Builder change1 = DataChange.newBuilder();
        DataChanges.Builder changeBuilder = DataChanges.newBuilder();
        change1.setRVmzId(properties.getUzId());
        change1.addRoadChanges(
                AreaChange.newBuilder().addGeoChangesValue(1).addFeatureChanges(
                        ItemChange.newBuilder()
                                .addAqTypesValue(1)
                                .build()
                ).build()
        );
        changeBuilder.addDataChanges(change1.build());
        changeBuilder.setIid("HänselUndGretel");
        DataChanges changes = changeBuilder.build();

        return sendToKafka("blub", changes.getIid(), changes.toByteArray(),
                changes.getClass().getName(), delay, "NOW-DataChange");
    }
}
