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

@Import({KafkaConfig.class})
@EmbeddedKafka(partitions = 1, controlledShutdown = true, brokerProperties = {"log.dir=target/kafka"})
@EnableAutoConfiguration
@DirtiesContext
@TestPropertySource(properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.consumer.group-id=EmbeddedKafkaTest", "spring.kafka.listener.missing-topics-fatal=false"})
@SpringJUnitConfig
@Component
@Slf4j
public class InitialTopicReaderTest {


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

    @Autowired
    private RuntimeBeanFactoryNotCompacted runtimeBeanFactoryNotCompacted;

    private boolean hasKey1 = false;
    private boolean hasKey2 = false;
    private boolean ncHasKey1 = false;
    private boolean ncHasKey2 = false;
    private boolean ncHasKey3 = false;

    @BeforeEach
    void init() throws InterruptedException, IOException, ExecutionException {

//        for (final MessageListenerContainer messageListenerContainer : this.registry.getListenerContainers()) {
//            waitForAssignment(messageListenerContainer, this.embeddedKafkaBroker.getPartitionsPerTopic());
//        }

        // add topic
        Utils.createTopicIfNeeded(kafkaAdminClient, "TestTopic", true, 5, (short) 1);
        Utils.createTopicIfNeeded(kafkaAdminClient, "TestTopicNotCompacted", false, 5, (short) 1);

        String s1 = "1-1";
        String s2 = "2a";
        String s3 = "1-2";
        String s4 = "1-3";
        String s5 = "2b";

        kafkaTemplate.send(MessageBuilder.withPayload(s1).setHeader(KafkaHeaders.TOPIC, "TestTopic")
                .setHeader(KafkaHeaders.KEY, "Key1").setHeader(KafkaConstants.KAFKA_HEADER_INSTANCE, "i1").build()).get();

        kafkaTemplate.send(MessageBuilder.withPayload(s2).setHeader(KafkaHeaders.TOPIC, "TestTopic")
                .setHeader(KafkaHeaders.KEY, "Key2").setHeader(KafkaConstants.KAFKA_HEADER_INSTANCE, "i10").build()).get();

        kafkaTemplate.send(MessageBuilder.withPayload(s3).setHeader(KafkaHeaders.TOPIC, "TestTopic")
                .setHeader(KafkaHeaders.KEY, "Key1").setHeader(KafkaConstants.KAFKA_HEADER_INSTANCE, "i2").build()).get();

        kafkaTemplate.send(MessageBuilder.withPayload(s4).setHeader(KafkaHeaders.TOPIC, "TestTopic")
                .setHeader(KafkaHeaders.KEY, "Key1").setHeader(KafkaConstants.KAFKA_HEADER_INSTANCE, "i3").build()).get();

        kafkaTemplate.send(MessageBuilder.withPayload(s5).setHeader(KafkaHeaders.TOPIC, "TestTopic")
                .setHeader(KafkaHeaders.KEY, "Key2").setHeader(KafkaConstants.KAFKA_HEADER_INSTANCE, "i11").build()).get();
    }

    /**
     * Test initialisation of a kafka topic where the last entry of each key is relevant.
     */
    @Test
    void initialTopicReaderTest() {

        Runner runner = runtimeBeanFactory.createRuntimeBean(new Runner.CallbackInterface() {
            @Override
            public void callback(String result, String instance) {
                if (result.equals("2b")){
                    hasKey2 = true;
                    assertEquals("i11", instance);
                }else if (result.equals("1-3")){
                    hasKey1 = true;
                    assertEquals("i3", instance);
                }else{
                    fail("unexpected message: " + result);
                }
            }
        }, true);

        await().until(() ->
        {
            return hasKey1 && hasKey2;
        });
    }

    /**
     * Test initialisation of a kafka topic where the last entry of each key is relevant.
     * We start reading at a certain timestamp 10 seconds in the past.
     */
    @Test
    void initialTopicReaderTestReadFromTimestamp() throws ExecutionException, InterruptedException {

        String s0 = "0-0";
        String s1 = "1-1";
        String s2 = "2a";
        String s3 = "1-2";
        String s4 = "1-3";
        String s5 = "2b";

        kafkaTemplate.send(MessageBuilder.withPayload(s0).setHeader(KafkaHeaders.TOPIC, "TestTopicNotCompacted")
                                         .setHeader(KafkaHeaders.KEY, "Key3").build()).get();

        Thread.sleep(11000); //NOSONAR: It's a test. Sleep 11 seconds so that Key3 should not be read if we read data from 10 Seconds back.
        
        kafkaTemplate.send(MessageBuilder.withPayload(s1).setHeader(KafkaHeaders.TOPIC, "TestTopicNotCompacted")
                                         .setHeader(KafkaHeaders.KEY, "Key1").build()).get();

        kafkaTemplate.send(MessageBuilder.withPayload(s2).setHeader(KafkaHeaders.TOPIC, "TestTopicNotCompacted")
                                         .setHeader(KafkaHeaders.KEY, "Key2").build()).get();

        kafkaTemplate.send(MessageBuilder.withPayload(s3).setHeader(KafkaHeaders.TOPIC, "TestTopicNotCompacted")
                                         .setHeader(KafkaHeaders.KEY, "Key1").build()).get();

        kafkaTemplate.send(MessageBuilder.withPayload(s4).setHeader(KafkaHeaders.TOPIC, "TestTopicNotCompacted")
                                         .setHeader(KafkaHeaders.KEY, "Key1").build()).get();

        kafkaTemplate.send(MessageBuilder.withPayload(s5).setHeader(KafkaHeaders.TOPIC, "TestTopicNotCompacted")
                                         .setHeader(KafkaHeaders.KEY, "Key2").build()).get();

        RunnerNotCompacted runner = runtimeBeanFactoryNotCompacted.createRuntimeBean(new RunnerNotCompacted.CallbackInterface() {
            @Override
            public void callback(String result) {
                if (result.equals("2b")){
                    ncHasKey2 = true;
                }else if (result.equals("1-3")) {
                    ncHasKey1 = true;
                }else if(result.equals("0-0")) {
                    ncHasKey3 = true;
                }else{
                    fail("unexpected message: " + result);
                }
            }
        }, true);

        await().until(() ->
        {
            return ncHasKey1 && ncHasKey2;
        });

        assertFalse(ncHasKey3);
    }

}
