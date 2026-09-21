package de.heuboe.vmis2.kafka.converter;

import static de.heuboe.vmis2.jprotoc.utils.Constants.HEADER_X_IID;
import static de.heuboe.vmis2.jprotoc.utils.Constants.HEADER_X_PROTOBUF_TYPE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import de.heuboe.asfinag.kafka.data.SampleProto;
import de.heuboe.asfinag.kafka.data.pojo.PSampleProto;
import de.heuboe.asfinag.kafka.data.pojo.PSampleProto.Transfer;
import de.heuboe.vmis2.jprotoc.transferinterface.HbProtoBufPojo;
import de.heuboe.vmis2.kafka.test.KafkaTestConfiguration;

/**
 * Tests the complete integration of Spring-Kafka and the {@link ProtoPojoKafkaMessageConverter}.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@SpringJUnitConfig(classes = KafkaTestConfiguration.class)
@EmbeddedKafka(partitions = 1, controlledShutdown = true,
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:3333",
                "port=3333"
        })
class ProtoPojoKafkaMessageConverterITTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtoPojoKafkaMessageConverterITTest.class);

    private static final String IID = "qwertzuoipasdklj";
    private static final byte[] IID_BYTES = IID.getBytes(UTF_8);
    private static final SampleProto PROTO = SampleProto.newBuilder()
            .setId(42)
            .setIid(IID)
            .setName("Foo Bar")
            .addTag("Tag A")
            .addTag("Tag B")
            .build();
    private static final PSampleProto POJO = Transfer.INSTANCE.fromProto(PROTO);
    private static final byte[] PBYTES = PROTO.toBuilder().build().toByteArray();

    private static final String PROTO_TOPIC = "junit-test-proto";
    private static final String PROTO_TYPE_TOPIC = "junit-test-proto-type";
    private static final String PROTO_TYPE2_TOPIC = "junit-test-proto-type2";
    private static final String DYNAMIC_PROTO_TOPIC = "junit-test-dynamic";
    private static final String POJO_TOPIC = "junit-test-pojo";
    private static final String POJO_TYPE_TOPIC = "junit-test-pojo-type";
    private static final String LAZY_PROTO_TOPIC = "junit-test-proto-lazy";
    private static final String LAZY_POJO_TOPIC = "junit-test-pojo-lazy";
    private static final String BYTES_TOPIC = "junit-test-bytes";

    private static final String[] TOPICS = {
            PROTO_TOPIC,
            PROTO_TYPE_TOPIC,
            PROTO_TYPE2_TOPIC,
            DYNAMIC_PROTO_TOPIC,
            POJO_TOPIC,
            POJO_TYPE_TOPIC,
            LAZY_PROTO_TOPIC,
            LAZY_POJO_TOPIC,
            BYTES_TOPIC,
    };

    private static final byte[] PTYPE = SampleProto.getDescriptor().getFullName().getBytes(UTF_8);

    @Autowired
    private EmbeddedKafkaBroker kafkaEmbeded;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    // These must be static for some strange reason
    private static String listenerTopic;
    private static CountDownLatch counter;
    private static boolean initialized;

    @BeforeEach
    void setUp() throws Exception {
        if (initialized) {
            return;
        }
        initialized = true;

        LOGGER.warn("Kafka is still starting... the following warnings can be ignored.");
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            for (final MessageListenerContainer messageListenerContainer : this.kafkaListenerEndpointRegistry
                    .getListenerContainers()) {
                ContainerTestUtils.waitForAssignment(messageListenerContainer,
                        this.kafkaEmbeded.getPartitionsPerTopic());
            }
        }, "Kafka took unexpectedly long to start");
        LOGGER.warn("Kafka is started.");
    }

    public static String[] getTopics() {
        return TOPICS;
    }

    @ParameterizedTest
    @MethodSource("getTopics")
    void testMessageConversion(final String topic) throws InterruptedException, ExecutionException {
        listenerTopic = topic;
        final CountDownLatch latch = new CountDownLatch(3);
        counter = latch;

        LOGGER.info("Sending messages to {}", topic);
        this.kafkaTemplate.send(MessageBuilder.withPayload(PROTO)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .build()).get();
        this.kafkaTemplate.send(MessageBuilder.withPayload(POJO)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .build()).get();
        this.kafkaTemplate.send(MessageBuilder.withPayload(PBYTES)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader(HEADER_X_PROTOBUF_TYPE, PTYPE)
                .setHeader(HEADER_X_IID, IID_BYTES)
                .build()).get();

        LOGGER.info("Sending completed - Waiting up to 3s each for messages to be Received");

        // If you get a failure here, there was an assertion error in the listener, check the logs
        assertTrue(latch.await(3, TimeUnit.SECONDS), "Did not receive messages in time");

        listenerTopic = null;
        counter = null;
    }

    @KafkaListener(topics = PROTO_TOPIC)
    void protoListener(final SampleProto proto,
            @Header(name = HEADER_X_PROTOBUF_TYPE, required = false) final byte[] type,
            @Header(name = HEADER_X_IID, required = false) final byte[] iid) {

        LOGGER.info("Received proto");
        assertReceived(PROTO_TOPIC, type, iid);
        assertEquals(PROTO, proto);
    }

    @KafkaListener(topics = PROTO_TYPE_TOPIC)
    void protoTypeListener(final Message proto,
            @Header(name = HEADER_X_PROTOBUF_TYPE, required = false) final byte[] type,
            @Header(name = HEADER_X_IID, required = false) final byte[] iid) {

        LOGGER.info("Received proto type");
        assertReceived(PROTO_TYPE_TOPIC, type, iid);
        assertEquals(PROTO, proto);
    }

    @KafkaListener(topics = PROTO_TYPE2_TOPIC)
    void protoType2Listener(final GeneratedMessageV3 proto,
            @Header(name = HEADER_X_PROTOBUF_TYPE, required = false) final byte[] type,
            @Header(name = HEADER_X_IID, required = false) final byte[] iid) {

        LOGGER.info("Received proto type2");
        assertReceived(PROTO_TYPE2_TOPIC, type, iid);
        assertEquals(PROTO, proto);
    }

    @KafkaListener(topics = DYNAMIC_PROTO_TOPIC)
    void dynamicListener(final DynamicMessage proto,
            @Header(name = HEADER_X_PROTOBUF_TYPE, required = false) final byte[] type,
            @Header(name = HEADER_X_IID, required = false) final byte[] iid)
            throws InvalidProtocolBufferException {

        LOGGER.info("Received dynamic proto");
        assertReceived(DYNAMIC_PROTO_TOPIC, type, iid);
        assertEquals(DynamicMessage.parseFrom(SampleProto.getDescriptor(), PBYTES), proto);
    }

    @KafkaListener(topics = POJO_TOPIC)
    void pojoListener(final PSampleProto pojo,
            @Header(name = HEADER_X_PROTOBUF_TYPE, required = false) final byte[] type,
            @Header(name = HEADER_X_IID, required = false) final byte[] iid) {

        LOGGER.info("Received pojo");
        assertReceived(POJO_TOPIC, type, iid);
        assertEquals(POJO, pojo);
    }

    @KafkaListener(topics = POJO_TYPE_TOPIC)
    void pojoListener(final HbProtoBufPojo pojo,
            @Header(name = HEADER_X_PROTOBUF_TYPE, required = false) final byte[] type,
            @Header(name = HEADER_X_IID, required = false) final byte[] iid) {

        LOGGER.info("Received pojo");
        assertReceived(POJO_TYPE_TOPIC, type, iid);
        assertEquals(POJO, pojo);
    }

    @KafkaListener(topics = LAZY_PROTO_TOPIC)
    void lazyProtoListener(final Lazy<SampleProto> proto,
            @Header(name = HEADER_X_PROTOBUF_TYPE, required = false) final byte[] type,
            @Header(name = HEADER_X_IID, required = false) final byte[] iid) {

        LOGGER.info("Received lazy proto");
        assertReceived(LAZY_PROTO_TOPIC, type, iid);
        assertEquals(PROTO, proto.get());
    }

    @KafkaListener(topics = LAZY_POJO_TOPIC)
    void lazyPojoListener(final Lazy<PSampleProto> pojo,
            @Header(name = HEADER_X_PROTOBUF_TYPE, required = false) final byte[] type,
            @Header(name = HEADER_X_IID, required = false) final byte[] iid) {

        LOGGER.info("Received lazy pojo");
        assertReceived(LAZY_POJO_TOPIC, type, iid);
        assertEquals(POJO, pojo.get());
    }

    @KafkaListener(topics = BYTES_TOPIC)
    void bytesListener(final byte[] bytes,
            @Header(name = HEADER_X_PROTOBUF_TYPE, required = false) final byte[] type,
            @Header(name = HEADER_X_IID, required = false) final byte[] iid) {

        LOGGER.info("Received bytes");
        assertReceived(BYTES_TOPIC, type, iid);
        assertArrayEquals(PBYTES, bytes);
    }

    private void assertReceived(final String topic, final byte[] type, final byte[] iid) {
        assertEquals(topic, listenerTopic, "Received on wrong listener");
        assertArrayEquals(PTYPE, type);
        assertArrayEquals(IID_BYTES, iid);
        counter.countDown();
    }

}
