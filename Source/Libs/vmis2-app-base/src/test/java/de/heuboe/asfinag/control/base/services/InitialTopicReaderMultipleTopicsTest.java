package de.heuboe.asfinag.control.base.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringJUnitConfig(InitialTopicReaderMultipleTopicsTest.Config.class)
@EmbeddedKafka(
        controlledShutdown = true,
        topics = {
                "topic1",
                "topic2",
                "topic3",
                "topic4"
        }
)
@TestPropertySource(
        properties = {
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "spring.kafka.listener.missing-topics-fatal=false"
        }
)
@DirtiesContext
@Import(KafkaAutoConfiguration.class)
public class InitialTopicReaderMultipleTopicsTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ConsumerFactory<String, byte[]> consumerFactory;

    @Autowired
    private ApplicationContext applicationContext;

    @Configuration
    static class Config {

        @Bean
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        TopicListener topicListener(String[] topics, Map<String, String> readMessage,
                InitialTopicReader<String> initialTopicReader) {
            return new TopicListener(topics, readMessage, initialTopicReader);
        }
    }

    @RequiredArgsConstructor
    public static class TopicListener implements ConsumerSeekAware {

        @Getter
        private final String[] topics;

        private final Map<String, String> readMessages;

        private final InitialTopicReader<String> initialTopicReader;

        @KafkaListener(topics = "#{__listener.topics}", groupId = "testGroupId")
        public void listen(ConsumerRecord<String, String> consumerRecord) {
            initialTopicReader.isInitialized(consumerRecord.value(), consumerRecord.topic(), consumerRecord.partition(),
                    consumerRecord.key(), consumerRecord.offset(),
                    message -> readMessages.put(message.getMessageKey(), message.getProtoObj())
            );
        }

        @Override
        public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
            initialTopicReader.onPartitionsAssigned(assignments, callback);
        }
    }

    @Test
    void multipleTopics_startAtBeginning_test() throws ExecutionException, InterruptedException {

        kafkaTemplate.send("topic1", 0, "topic1_key1", "topic1_object1").get();
        kafkaTemplate.send("topic1", 0, "topic1_key1", "topic1_object1_2").get();
        kafkaTemplate.send("topic1", 1, "topic1_key2", "topic1_object2").get();

        kafkaTemplate.send("topic2", 0, "topic2_key1", "topic1_object1").get();
        kafkaTemplate.send("topic2", 1, "topic2_key2", "topic1_object2").get();

        InitialTopicReader<String> initialTopicReader =
                new InitialTopicReader<>(Set.of("topic1", "topic2"), consumerFactory);

        Map<String, String> readMessages = new HashMap<>();

        applicationContext.getBean(TopicListener.class, new String[] {"topic1", "topic2"}, readMessages,
                initialTopicReader);

        await().until(initialTopicReader::isInitialReadFinished);

        Map<String, String> expectedReadMessages = Map.of(
                "topic1_key1", "topic1_object1_2",
                "topic1_key2", "topic1_object2",
                "topic2_key1", "topic1_object1",
                "topic2_key2", "topic1_object2"
        );

        assertEquals(expectedReadMessages, readMessages);
    }

    @Test
    void multipleTopics_startAtTimestamp_test() throws ExecutionException, InterruptedException {

        Instant timestamp = Instant.now();

        kafkaTemplate.send("topic3", 0, timestamp.toEpochMilli(), "topic3_key1", "topic3_object1").get();
        kafkaTemplate.send("topic3", 0, timestamp.plusSeconds(5).toEpochMilli(), "topic3_key1", "topic3_object1_2")
                .get();
        kafkaTemplate.send("topic3", 1, timestamp.plusSeconds(1).toEpochMilli(), "topic3_key2", "topic3_object2").get();

        kafkaTemplate.send("topic4", 0, timestamp.plusSeconds(4).toEpochMilli(), "topic4_key1", "topic4_object1").get();
        kafkaTemplate.send("topic4", 1, timestamp.plusSeconds(5).toEpochMilli(), "topic4_key2", "topic4_object2").get();

        InitialTopicReader<String> initialTopicReader =
                new InitialTopicReader<>(Set.of("topic3", "topic4"), timestamp.plusSeconds(4), consumerFactory);

        Map<String, String> readMessages = new HashMap<>();

        applicationContext.getBean(TopicListener.class, new String[] {"topic3", "topic4"}, readMessages,
                initialTopicReader);

        await().until(initialTopicReader::isInitialReadFinished);

        Map<String, String> expectedReadMessages = Map.of(
                "topic3_key1", "topic3_object1_2",
                "topic4_key1", "topic4_object1",
                "topic4_key2", "topic4_object2"
        );

        assertEquals(expectedReadMessages, readMessages);
    }
}
