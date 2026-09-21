package de.heuboe.asfinag.control.base.services;

import de.heuboe.asfinag.vmis2.constants.KafkaConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.kafka.test.utils.ContainerTestUtils.waitForAssignment;

@Import({KafkaConfig.class})
@EmbeddedKafka(partitions = 1, controlledShutdown = true, brokerProperties = {"log.dir=target/kafka"})
@EnableAutoConfiguration
@DirtiesContext
@TestPropertySource(properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=EmbeddedKafkaTest", "spring.kafka.listener.missing-topics-fatal=false"})
@SpringJUnitConfig
@Component
@Slf4j
public class InitialNotCompactTopicReaderTest {


    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    protected KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    protected ConsumerFactory<String, byte[]> consumerFactory;

    @Autowired
    private AdminClient kafkaAdminClient;

    @Autowired
    private RuntimeBeanFactory runtimeBeanFactory;

    private boolean hasKey1 = false;
    private boolean hasKey2 = false;

    @BeforeEach
    void init() throws InterruptedException, IOException, ExecutionException {

        for (final MessageListenerContainer messageListenerContainer : this.registry.getListenerContainers()) {
            waitForAssignment(messageListenerContainer, this.embeddedKafkaBroker.getPartitionsPerTopic());
        }

        // add topic
        //Only works for topics with one partition
        Utils.createTopicIfNeeded(kafkaAdminClient, "TestTopic", true, 1, (short) 1);

        String s1 = "1-1";
        String s2 = "2a";
        String s3 = "1-2";
        String s4 = "1-3";
        String s5 = "2b";

        kafkaTemplate.send(MessageBuilder.withPayload(s1).setHeader(KafkaHeaders.TOPIC, "TestTopic")
                .setHeader(KafkaHeaders.KEY, "Key1").setHeader(KafkaConstants.KAFKA_HEADER_INSTANCE, "i1").build()).get();

        kafkaTemplate.send(MessageBuilder.withPayload(s2).setHeader(KafkaHeaders.TOPIC, "TestTopic")
                .setHeader(KafkaHeaders.KEY, "Key2").setHeader(KafkaConstants.KAFKA_HEADER_INSTANCE, "i2").build()).get();

        kafkaTemplate.send(MessageBuilder.withPayload(s3).setHeader(KafkaHeaders.TOPIC, "TestTopic")
                .setHeader(KafkaHeaders.KEY, "Key1").setHeader(KafkaConstants.KAFKA_HEADER_INSTANCE, "i3").build()).get();

        kafkaTemplate.send(MessageBuilder.withPayload(s4).setHeader(KafkaHeaders.TOPIC, "TestTopic")
                .setHeader(KafkaHeaders.KEY, "Key2").setHeader(KafkaConstants.KAFKA_HEADER_INSTANCE, "i4").build()).get();

        kafkaTemplate.send(MessageBuilder.withPayload(s5).setHeader(KafkaHeaders.TOPIC, "TestTopic")
                .setHeader(KafkaHeaders.KEY, "Key1").setHeader(KafkaConstants.KAFKA_HEADER_INSTANCE, "i5").build()).get();
    }

    /**
     * Test initialisation of a kafka topic where only the last entry of the topic is relevant.
     */
    @Test
    void initWithLatestTest() {
        Runner runner = runtimeBeanFactory.createRuntimeBean(new Runner.CallbackInterface() {
            @Override
            public void callback(String result, String instance) {
                // expect only last entry
                assertTrue(result.equals("2b"));
                hasKey1 = true;
                assertEquals("i5", instance);
            }
        }, false);

        await().until(() ->
        {
            return hasKey1;
        });
    }
}
