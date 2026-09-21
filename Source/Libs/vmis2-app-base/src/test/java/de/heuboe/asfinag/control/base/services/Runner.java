package de.heuboe.asfinag.control.base.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Map;

import static de.heuboe.asfinag.vmis2.constants.KafkaConstants.KAFKA_HEADER_INSTANCE;


@Slf4j
public class Runner implements ConsumerSeekAware {

    public interface CallbackInterface {
        public void callback(String result, String instance);
    }
    private final String TOPIC_KEY_PAYLOAD = "received topic = '{}' key='{}' payload='{}'";

    private InitialTopicReader<String> topicInit;

    @Autowired
    private ConsumerFactory<String, byte[]> consumerFactory;

    @Autowired
    private AdminClient kafkaAdminClient;

    private CallbackInterface callback;
    private boolean lastForEachKey;

    public Runner(CallbackInterface callback, boolean lastForEachKey){
        this.callback = callback;
        this.lastForEachKey = lastForEachKey;
    }

    @PostConstruct // Run after Autowired has been injected
    public void init() {

        this.topicInit = new InitialTopicReader<>("TestTopic", consumerFactory);
        long timestamp = Instant.now().minusSeconds(10).getEpochSecond() * 1000L;
    }

    @KafkaListener(id = "TESTTOPIC", groupId = "TEST", topicPattern = "TestTopic")
    public void receiveCompacted(@Payload String msg,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition, @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KAFKA_HEADER_INSTANCE) String instance,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        log.debug(TOPIC_KEY_PAYLOAD, topic, key, msg);

        if (this.lastForEachKey) {
            if (topicInit.isInitialized(msg, topic, partition, key, offset, Map.of(KAFKA_HEADER_INSTANCE, instance), m -> this.callback.callback(m.getProtoObj(), m.getHeaderValues().get(KAFKA_HEADER_INSTANCE)), true)) {

            }
        } else {
            this.callback.callback(msg, instance);
        }
    }

    @Override
    public void registerSeekCallback(ConsumerSeekCallback callback) {
        // not needed at the moment
    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        if (this.lastForEachKey) {
            topicInit.onPartitionsAssigned(assignments, callback);
        } else {
            assignments.entrySet().stream().filter(topicPartitionLongEntry -> topicPartitionLongEntry.getKey().topic().equals("TestTopic"))
                    .forEach(topicPartitionLongEntry -> Utils
                            .seekToLatest(consumerFactory, callback, topicPartitionLongEntry.getKey()));
            assignments.entrySet().stream().filter(topicPartitionLongEntry -> topicPartitionLongEntry.getKey().topic().equals("TestTopicNotCompacted"))
                       .forEach(topicPartitionLongEntry -> Utils
                               .seekToLatest(consumerFactory, callback, topicPartitionLongEntry.getKey()));
        }
    }

    @Override
    public void onIdleContainer(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        // not needed at the moment
    }

}
