package de.heuboe.vmis2.kafka.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DynamicMessage;

import de.heuboe.asfinag.kafka.data.SampleProto;

/**
 * Tests for {@link ProtoDynamicMessageKafkaDeserializer}.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
class ProtoDynamicMessageKafkaDeserializerTest extends AbstractProtoMessageKafkaDeserializerTest<DynamicMessage> {

    private static final Descriptor DESCRIPTOR = SampleProto.getDescriptor();

    @Override
    protected AbstractProtoMessageKafkaDeserializer<DynamicMessage> deserializer() {
        return new ProtoDynamicMessageKafkaDeserializer();
    }

    @Override
    protected void assertEqualsEmpty(final DynamicMessage data) {
        assertEquals(DynamicMessage.newBuilder(DESCRIPTOR).build(), data);
    }

    @Override
    protected void assertEqualsSample(final DynamicMessage data) {
        assertNotNull(data);
        assertEquals(1L, data.getField(DESCRIPTOR.findFieldByNumber(SampleProto.ID_FIELD_NUMBER)));
        assertEquals("test", data.getField(DESCRIPTOR.findFieldByNumber(SampleProto.NAME_FIELD_NUMBER)));
    }

}
