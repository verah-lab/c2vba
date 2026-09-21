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


@Slf4j
public class RunnerNotCompacted implements ConsumerSeekAware {

    public interface CallbackInterface {
        public void callback(String result);
    }
    private final String TOPIC_KEY_PAYLOAD = "received topic = '{}' key='{}' payload='{}'";

    private InitialTopicReader<String> topicNotCompactedInit;

    @Autowired
    private ConsumerFactory<String, byte[]> consumerFactory;

    @Autowired
    private AdminClient kafkaAdminClient;

    private CallbackInterface callback;
    private boolean lastForEachKey;

    public RunnerNotCompacted(CallbackInterface callback, boolean lastForEachKey){
        this.callback = callback;
        this.lastForEachKey = lastForEachKey;
    }

    @PostConstruct // Run after Autowired has been injected
    public void init() {

        Instant timestamp = Instant.now().minusSeconds(10);
        this.topicNotCompactedInit = new InitialTopicReader<>("TestTopicNotCompacted", timestamp, consumerFactory);
    }

    @KafkaListener(id = "TESTTOPICNOTCOMPACTED", groupId = "TEST", topicPattern = "TestTopicNotCompacted")
    public void receiveNotCompacted(@Payload String msg,
                                 @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION) int partition, @Header(KafkaHeaders.OFFSET) long offset,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        log.debug(TOPIC_KEY_PAYLOAD, topic, key, msg);

        if (this.lastForEachKey) {
            if (topicNotCompactedInit.isInitialized(msg, topic, partition, key, offset, m -> this.callback.callback(m.getProtoObj()))) {

            }
        } else {
            this.callback.callback(msg);
        }
    }

    @Override
    public void registerSeekCallback(ConsumerSeekCallback callback) {
        // not needed at the moment
    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        if (this.lastForEachKey) {
            topicNotCompactedInit.onPartitionsAssigned(assignments, callback);
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
