package de.heuboe.vmis2.kafka.converter;

import static de.heuboe.asfinag.kafka.data.pojo.PSampleProto.Transfer.INSTANCE;
import static de.heuboe.vmis2.jprotoc.utils.Constants.HEADER_X_IID;
import static de.heuboe.vmis2.jprotoc.utils.Constants.HEADER_X_ORIGIN;
import static de.heuboe.vmis2.jprotoc.utils.Constants.HEADER_X_PROTOBUF_INTERFACEVERSION;
import static de.heuboe.vmis2.jprotoc.utils.Constants.HEADER_X_PROTOBUF_TYPE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.KafkaNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;

import de.heuboe.asfinag.kafka.data.SampleProto;
import de.heuboe.asfinag.kafka.data.pojo.PSampleProto;
import de.heuboe.asfinag.kafka.data.pojo.PSampleProtoDDCProtoTransferCatalog;
import de.heuboe.vmis2.jprotoc.transferinterface.HbProtoBufPojo;

/**
 * Test for {@link ProtoPojoKafkaMessageConverter} (conversion methods only).
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
class ProtoPojoKafkaMessageConverterTest {

    // Utils
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    // Types
    private static final Class<? extends HbProtoBufPojo> POJO_CLASS = PSampleProto.class;
    private static final Class<? extends GeneratedMessageV3> PROTO_CLASS = SampleProto.class;
    private static final Class<byte[]> BYTE_ARRAY_CLASS = byte[].class;
    @SuppressWarnings("rawtypes")
    private static final Class<Map> MAP_CLASS = Map.class;
    private static final Type MAP_TYPE = new TypeReference<Map<String, Object>>() {}.getType();
    @SuppressWarnings("rawtypes")
    private static final Class<HashMap> MAP2_CLASS = HashMap.class;
    private static final Class<Object> OBJECT_CLASS = Object.class;
    private static final Class<String> STRING_CLASS = String.class;
    private static final Class<Unconvertible> UNCONVERTIBLE_CLASS = Unconvertible.class;

    // Sample-Data
    private static final String IID = "qwertzupoi";
    private static final SampleProto PROTO = SampleProto.newBuilder()
            .setId(42)
            .setIid(IID)
            .setName("Foo Bar")
            .addTag("Kreisverkehr")
            .addTag("Laufrad")
            .build();
    private static final PSampleProto POJO = INSTANCE.fromProto(PROTO);
    private static final byte[] PBYTES = PROTO.toBuilder().build().toByteArray();

    private static final byte[] BBYTES = new byte[0];

    private static final Map<String, Object> MAP = new LinkedHashMap<>();
    private static final Map<String, Object> MAP2 = new HashMap<>();
    private static final byte[] MBYTES;

    private static final String STRING = "This is a test!";
    private static final byte[] SBYTES = STRING.getBytes(UTF_8);

    private static final String RSTRING = "random";
    private static final byte[] RSBYTES = RSTRING.getBytes(UTF_8);

    private static final Unconvertible UNCONVERTIBLE = new Unconvertible("No way!");
    private static final byte[] UBYTES = "{}".getBytes(UTF_8);

    private static final Headers PHEADERS = new RecordHeaders()
            .add(HEADER_X_PROTOBUF_TYPE,
                    SampleProto.getDescriptor().getFullName().getBytes(UTF_8))
            .add(HEADER_X_PROTOBUF_INTERFACEVERSION,
                    PSampleProtoDDCProtoTransferCatalog.getInterfaceVersion().getBytes(UTF_8))
            .add(HEADER_X_IID, IID.getBytes(UTF_8));
    private static final Headers XHEADERS = new RecordHeaders();

    private final ProtoPojoKafkaMessageConverter converter = new ProtoPojoKafkaMessageConverter();

    static {
        MAP.put("Foo Bar", 42);
        MAP2.putAll(MAP);
        try {
            MBYTES = OBJECT_MAPPER.writeValueAsBytes(MAP);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException("Someone broke the test", e);
        }
    }

    @BeforeEach
    void setup() {
        this.converter.registerDeserializer(String.class, bytes -> new String(bytes, UTF_8));
        this.converter.registerSerializer(String.class, string -> string.getBytes(UTF_8));
        this.converter.registerHeaderSpec(String.class, Collections.emptyMap());
        this.converter.registerIIDReader(String.class, string -> null);

        // Don't spam the log:
        this.converter.registerIIDReader(MAP_CLASS, string -> null);
        this.converter.registerIIDReader(MAP2_CLASS, string -> null);
    }

    /**
     * Test for {@link ProtoPojoKafkaMessageConverter#extractAndConvertValue(ConsumerRecord, Type)}
     */
    @Test
    void testExtractAndConvertValue() {
        assertSame(KafkaNull.INSTANCE, extractAndConvertValue(null, PROTO_CLASS));
        assertSame(KafkaNull.INSTANCE, extractAndConvertValue(null, POJO_CLASS));
        assertSame(KafkaNull.INSTANCE, extractAndConvertValue(null, BYTE_ARRAY_CLASS));

        assertEquals(PROTO, extractAndConvertValue(PBYTES, PROTO_CLASS));
        assertEquals(POJO, extractAndConvertValue(PBYTES, POJO_CLASS));

        assertEquals(BBYTES, extractAndConvertValue(BBYTES, BYTE_ARRAY_CLASS));

        assertThrows(IllegalStateException.class, () -> extractAndConvertValue(MBYTES, MAP_CLASS));

        this.converter.setFallbackSerdeGenerator(OBJECT_MAPPER);

        assertEquals(MAP, extractAndConvertValue(MBYTES, MAP_TYPE));
        assertEquals(MAP2, extractAndConvertValue(MBYTES, MAP2_CLASS));
        assertEquals(MAP, extractAndConvertValue(MBYTES, OBJECT_CLASS));

        assertEquals(STRING, extractAndConvertValue(SBYTES, STRING_CLASS));

        assertThrows(RuntimeException.class, () -> extractAndConvertValue(UBYTES, UNCONVERTIBLE_CLASS));

        {
            // Test parsing failure with (assumed) broken configuration
            final Throwable cause =
                    assertThrows(IllegalArgumentException.class, () -> extractAndConvertValue("not bytes", PROTO_CLASS))
                            .getCause();
            assertNotNull(cause);
            assertEquals(ClassCastException.class, cause.getClass());
        }

        {
            // Test parsing failure with unspecified type
            final Throwable cause = assertThrows(IllegalArgumentException.class,
                    () -> extractAndConvertValue(RSBYTES, PROTO_CLASS))
                            .getCause();
            assertNotNull(cause);
            assertEquals(InvalidProtocolBufferException.class, cause.getClass());
        }

        {
            // Test parsing failure with matching type
            final String fullName = SampleProto.getDescriptor().getFullName();
            final Throwable cause = assertThrows(IllegalArgumentException.class,
                    () -> extractAndConvertValue(RSBYTES, fullName, PROTO_CLASS))
                            .getCause();
            assertNotNull(cause);
            assertEquals(InvalidProtocolBufferException.class, cause.getClass());
        }

        {
            // Test parsing failure with dismatching type
            final String fullName = SampleProto.getDescriptor().getFullName() + "List";
            final Throwable cause = assertThrows(IllegalArgumentException.class,
                    () -> extractAndConvertValue(RSBYTES, fullName, PROTO_CLASS))
                            .getCause();
            assertNotNull(cause);
            assertEquals(InvalidProtocolBufferException.class, cause.getClass());
        }
    }

    private Object extractAndConvertValue(final Object obj, final Type targetType) {
        return this.converter.extractAndConvertValue(
                new ConsumerRecord<>("-", 0, 0, null, obj),
                targetType);
    }

    private Object extractAndConvertValue(final Object obj, final String type, final Type targetType) {
        final ConsumerRecord<Object, Object> record = new ConsumerRecord<>("-", 0, 0, null, obj);
        record.headers().add(HEADER_X_PROTOBUF_TYPE, this.converter.headerToBytes(type));
        return this.converter.extractAndConvertValue(
                record,
                targetType);
    }

    /**
     * Test for {@link ProtoPojoKafkaMessageConverter#convertPayload(Message)}
     */
    @Test
    void testConvertPayload() {
        assertNull(convertPayload(KafkaNull.INSTANCE));

        assertArrayEquals(PBYTES, convertPayload(PROTO));
        assertArrayEquals(PBYTES, convertPayload(POJO));

        assertArrayEquals(BBYTES, convertPayload(BBYTES));

        assertThrows(IllegalStateException.class, () -> convertPayload(MAP));

        this.converter.setFallbackSerdeGenerator(OBJECT_MAPPER);

        assertArrayEquals(MBYTES, convertPayload(MAP2));

        assertArrayEquals(SBYTES, convertPayload(STRING));

        assertThrows(RuntimeException.class, () -> convertPayload(UNCONVERTIBLE));
    }

    private byte[] convertPayload(final Object payload) {
        return this.converter.convertPayload(new GenericMessage<>(payload));
    }

    @Test
    void testHeaderSpec() {
        assertEquals(XHEADERS, initialRecordHeaders(KafkaNull.INSTANCE));

        assertEquals(PHEADERS, initialRecordHeaders(PROTO));
        assertEquals(PHEADERS, initialRecordHeaders(POJO));

        final SampleProto noIIDProto = PROTO.toBuilder().clearIid().build();
        final PSampleProto noIIDPojo = INSTANCE.fromProto(noIIDProto);
        assertThrows(IllegalArgumentException.class, () -> initialRecordHeaders(noIIDProto));
        assertThrows(IllegalArgumentException.class, () -> initialRecordHeaders(noIIDPojo));

        assertEquals(XHEADERS, initialRecordHeaders(BBYTES));

        assertThrows(IllegalStateException.class, () -> initialRecordHeaders(MAP));

        this.converter.setFallbackSerdeGenerator(OBJECT_MAPPER);

        assertEquals(XHEADERS, initialRecordHeaders(MAP2));

        assertEquals(XHEADERS, initialRecordHeaders(STRING));

        assertEquals(XHEADERS, initialRecordHeaders(UNCONVERTIBLE));
    }

    @Test
    void testHeaderSpecOrigin() {
        final String originHeader = "testSource";
        final ProtoPojoKafkaMessageConverter converter = new ProtoPojoKafkaMessageConverter(originHeader);

        final Headers xHeaders2 = new RecordHeaders(XHEADERS)
                .add(HEADER_X_ORIGIN, originHeader.getBytes(UTF_8));

        final Headers pHeaders2 = new RecordHeaders(PHEADERS)
                .add(HEADER_X_ORIGIN, originHeader.getBytes(UTF_8));

        assertEquals(xHeaders2, converter.initialRecordHeaders(new GenericMessage<>(KafkaNull.INSTANCE)));

        assertEquals(pHeaders2, converter.initialRecordHeaders(new GenericMessage<>(PROTO)));
        assertEquals(pHeaders2, converter.initialRecordHeaders(new GenericMessage<>(POJO)));
    }

    private Headers initialRecordHeaders(final Object payload) {
        return this.converter.initialRecordHeaders(new GenericMessage<>(payload));
    }

    public static class Unconvertible {

        private final String text;

        public Unconvertible(final String text) {
            this.text = text;
        }

        public String getText() {
            throw new UnsupportedOperationException("You shall not pass!");
        }

        @Override
        public String toString() {
            return this.text;
        }

    }

}
