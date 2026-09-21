package de.heuboe.vmis2.kafka.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.protobuf.GeneratedMessageV3;

import de.heuboe.asfinag.kafka.data.SampleProto;

/**
 * Tests {@link AbstractProtoMessageKafkaDeserializer}.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
class ProtoMessageKafkaDeserializerTest extends AbstractProtoMessageKafkaDeserializerTest<GeneratedMessageV3> {

    @Override
    protected AbstractProtoMessageKafkaDeserializer<GeneratedMessageV3> deserializer() {
        return new ProtoMessageKafkaDeserializer();
    }

    @Override
    protected void assertEqualsEmpty(final GeneratedMessageV3 data) {
        assertEquals(SampleProto.getDefaultInstance(), data);

    }

    @Override
    protected void assertEqualsSample(final GeneratedMessageV3 data) {
        assertEquals(SampleProto.newBuilder()
                .setId(1)
                .setName("test")
                .build(), data);
    }

}
