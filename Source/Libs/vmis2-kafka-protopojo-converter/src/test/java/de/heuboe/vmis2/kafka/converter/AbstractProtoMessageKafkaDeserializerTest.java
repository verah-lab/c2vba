package de.heuboe.vmis2.kafka.converter;

import static de.heuboe.vmis2.jprotoc.utils.Constants.HEADER_X_PROTOBUF_TYPE;
import static de.heuboe.vmis2.kafka.converter.AbstractProtoMessageKafkaDeserializer.KEY_DEFAULT_TYPE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.Test;

import de.heuboe.asfinag.kafka.data.SampleProto;

abstract class AbstractProtoMessageKafkaDeserializerTest<T> {

    private static final byte[] sampleData = SampleProto.newBuilder().setId(1).setName("test").build().toByteArray();
    private static final String protoType = SampleProto.getDescriptor().getFullName();
    private static final Map<String, String> config =
            singletonMap(KEY_DEFAULT_TYPE, protoType);
    private static final Headers emptyHeaders = new RecordHeaders();
    private static final Headers sampleHeaders =
            new RecordHeaders().add(HEADER_X_PROTOBUF_TYPE, protoType.getBytes(UTF_8));
    private static final String topic = "test";
    private static final byte[] emptyData = new byte[0];

    protected abstract AbstractProtoMessageKafkaDeserializer<T> deserializer();

    protected abstract void assertEqualsEmpty(T data);

    protected abstract void assertEqualsSample(T data);

    /**
     * Tests {@link AbstractProtoMessageKafkaDeserializer#deserialize(String, byte[])}
     */
    @Test
    void testDeserializeNoHeader() {
        try (final AbstractProtoMessageKafkaDeserializer<T> deserializer = deserializer()) {

            assertNull(deserializer.deserialize(topic, null));
            assertThrows(RuntimeException.class, () -> deserializer.deserialize(topic, emptyData));
            assertThrows(RuntimeException.class, () -> deserializer.deserialize(topic, sampleData));

            deserializer.configure(config, false);

            assertNull(deserializer.deserialize(topic, null));
            assertEqualsEmpty(deserializer.deserialize(topic, emptyData));
            assertEqualsSample(deserializer.deserialize(topic, sampleData));
        }
    }

    /**
     * Tests {@link AbstractProtoMessageKafkaDeserializer#deserialize(String, Headers, byte[])}
     */
    @Test
    void testDeserializeWithHeader() {
        try (final AbstractProtoMessageKafkaDeserializer<T> deserializer = deserializer()) {

            assertNull(deserializer.deserialize(topic, emptyHeaders, null));
            assertThrows(RuntimeException.class, () -> deserializer.deserialize(topic, emptyHeaders, emptyData));
            assertThrows(RuntimeException.class, () -> deserializer.deserialize(topic, emptyHeaders, sampleData));

            assertNull(deserializer.deserialize(topic, sampleHeaders, null));
            assertEqualsEmpty(deserializer.deserialize(topic, sampleHeaders, emptyData));
            assertEqualsSample(deserializer.deserialize(topic, sampleHeaders, sampleData));

            deserializer.configure(config, false);

            assertNull(deserializer.deserialize(topic, emptyHeaders, null));
            assertEqualsEmpty(deserializer.deserialize(topic, emptyHeaders, emptyData));
            assertEqualsSample(deserializer.deserialize(topic, emptyHeaders, sampleData));

            assertNull(deserializer.deserialize(topic, sampleHeaders, null));
            assertEqualsEmpty(deserializer.deserialize(topic, sampleHeaders, emptyData));
            assertEqualsSample(deserializer.deserialize(topic, sampleHeaders, sampleData));
        }
    }

    /**
     * Tests {@link AbstractProtoMessageKafkaDeserializer#deserializeType(String, byte[])}
     */
    @Test
    void testDeserializeType() {
        try (final AbstractProtoMessageKafkaDeserializer<T> deserializer = deserializer()) {

            assertNull(deserializer.deserializeType(protoType, null));
            assertEqualsEmpty(deserializer.deserializeType(protoType, emptyData));
            assertEqualsSample(deserializer.deserializeType(protoType, sampleData));
        }
    }

}
