package de.heuboe.vmis2.kafka.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import de.heuboe.asfinag.kafka.data.pojo.PSampleProto;
import de.heuboe.vmis2.jprotoc.transferinterface.HbProtoBufPojo;

/**
 * Tests {@link ProtoPojoKafkaDeserializer}.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
class ProtoPojoKafkaDeserializerTest extends AbstractProtoMessageKafkaDeserializerTest<HbProtoBufPojo> {

    @Override
    protected AbstractProtoMessageKafkaDeserializer<HbProtoBufPojo> deserializer() {
        return new ProtoPojoKafkaDeserializer();
    }

    @Override
    protected void assertEqualsEmpty(final HbProtoBufPojo data) {
        assertEquals(new PSampleProto(0L, "", Collections.emptyList(), ""), data);
    }

    @Override
    protected void assertEqualsSample(final HbProtoBufPojo data) {
        assertEquals(new PSampleProto(1L, "test", Collections.emptyList(), ""), data);
    }

}
