package de.heuboe.vmis2.kafka.converter;

import static de.heuboe.vmis2.jprotoc.utils.Constants.HEADER_X_IID;
import static de.heuboe.vmis2.jprotoc.utils.Constants.HEADER_X_ORIGIN;
import static de.heuboe.vmis2.jprotoc.utils.Constants.HEADER_X_PROTOBUF_INTERFACEVERSION;
import static de.heuboe.vmis2.jprotoc.utils.Constants.HEADER_X_PROTOBUF_TYPE;
import static de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils.isJavaClass;
import static de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils.isProtoClass;
import static de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils.isProtoMessage;
import static de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils.toDescriptor;
import static de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils.toFullProtoName;
import static de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils.toInterfaceVersion;
import static de.heuboe.vmis2.util.ReflectionToFunctionalConverter.publicMethodAsFunction;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.support.KafkaNull;
import org.springframework.kafka.support.converter.MessagingMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;

import de.heuboe.vmis2.jprotoc.transferinterface.HbProtoBufPojo;
import de.heuboe.vmis2.jprotoc.transferinterface.IIDContainer;
import de.heuboe.vmis2.jprotoc.transferinterface.PojoTransfer;
import de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * A generic kafka message converter supporting multiple way to (de-)serialize Objects to bytes and
 * vice versa. It uses an {@link ObjectMapper} as fallback converter.
 *
 * <p>
 * <b>Important:</b> Requires the given Kafka Serde config:
 * </p>
 *
 * <pre>
 * spring.kafka.consumer.properties.value.deserializer=org.apache.kafka.common.serialization.ByteArrayDeserializer
 * spring.kafka.producer.properties.value.serializer=org.apache.kafka.common.serialization.ByteArraySerializer
 * </pre>
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Slf4j
@Component
public class ProtoPojoKafkaMessageConverter extends MessagingMessageConverter {

    // Functional fields - Kept the references for faster access / avoid casts
    private static final Class<byte[]> BYTE_ARRAY_CLASS = byte[].class;
    private final Function<Type, BiFunction<Headers, byte[], ?>> deserializerGenerator = this::deserializerFor;
    // It is not possible to specify a generic variable on a field
    // and not possible use a functional interface as replacement
    // (lambda not supported if method has a generic type parameter)
    // Thus we can only rely on dirty casts to Object input parameters
    @SuppressWarnings({"unchecked", "rawtypes"})
    private final Function<Class<?>, Function<Object, byte[]>> serializerGenerator =
            (Function) (Function<Class<?>, Function<?, byte[]>>) this::serializerFor;

    private final Function<Class<?>, Map<String, byte[]>> headerSpecGenerator = this::headerSpecFor;
    @SuppressWarnings({"unchecked", "rawtypes"})
    private final Function<Class<?>, Function<Object, String>> iidReaderGenerator =
            (Function) (Function<Class<?>, Function<?, String>>) this::iidReaderFor;

    // State fields
    /**
     * A map containing the registered deserializers for a given type.
     *
     * <p>
     * <b>Content specification:</b>
     * <code>Map&lt;Class&lt;T&gt;, Function&lt;byte[], ? extends T&gt;&gt;</code>
     * </p>
     */
    private final Map<Type, BiFunction<Headers, byte[], ?>> deserializers = new ConcurrentHashMap<>();

    /**
     * A map containing the registered serializers for a given type.
     *
     * <p>
     * <b>Content specification:</b>
     * <code>Map&lt;Class&lt;T&gt;, Function&lt;? super T, byte[]&gt;&gt;</code>
     * </p>
     */
    private final Map<Class<?>, Function<Object, byte[]>> serializers = new ConcurrentHashMap<>();

    private final Map<Class<?>, Map<String, byte[]>> headerSpecs = new ConcurrentHashMap<>();
    private final Map<Class<?>, Function<Object, String>> iidReaders = new ConcurrentHashMap<>();

    /**
     * The optional origin of a sent message. Will be attached as header if set.
     */
    private final String origin;

    // - Fallbacks-Functions
    /**
     * A function that will be used to generate fallback deserializers for a given type.
     *
     * <p>
     * <b>Content specification:</b>
     * <code>Function&lt;Class&lt;T&gt;, Function&lt;byte[], ? extends T&gt;&gt;</code>
     * </p>
     */
    private Function<Type, BiFunction<Headers, byte[], ?>> fallbackDeserializerGenerator =
            type -> (headers, payload) -> {
                throw new IllegalStateException("No deserializer registered for type: " + type.getTypeName() + "\n"
                        + "Either register one explicitly or configure a fallback generator.");
            };

    /**
     * A function that will be used to generate fallback serializers for a given type.
     *
     * <p>
     * <b>Content specification:</b>
     * <code>Function&lt;Class&lt;T&gt;, Function&lt;? super T, byte[]&gt;&gt;</code>
     * </p>
     */
    private Function<Class<?>, Function<?, byte[]>> fallbackSerializerGenerator = clazz -> value -> {
        throw new IllegalStateException("No serializer registered for type: " + clazz.getTypeName() + "\n"
                + "Either register one explicitly or configure a fallback generator.");
    };

    /**
     * A function that will be used to generate fallback header specs for a given type.
     *
     * <p>
     * <b>Content specification:</b>
     * <code>Function&lt;Class&lt;?&gt;, Map&lt;String, byte[]&gt;&gt;</code>
     * </p>
     */
    private Function<Class<?>, Map<String, byte[]>> fallbackHeaderSpecGenerator = clazz -> {
        throw new IllegalStateException("No header spec registered for type: " + clazz.getTypeName() + "\n"
                + "Either register one explicitly or configure a fallback generator.");
    };

    /**
     * Creates a new ProtoPojoKafkaMessageConverter instance with some default serdes registered.
     */
    public ProtoPojoKafkaMessageConverter() {
        this(null);
    }

    /**
     * Creates a new ProtoPojoKafkaMessageConverter instance with some default serdes registered.
     *
     * @param originHeader The optional origin header to attach. May be null.
     */
    public ProtoPojoKafkaMessageConverter(final String originHeader) {
        registerDefaults();
        if (originHeader == null || originHeader.isEmpty()) {
            this.origin = null;
        } else {
            this.origin = originHeader;
        }
    }

    /**
     * Registers some default converter that don't need any dynamic handling.
     */
    protected void registerDefaults() {

        registerHeaderBasedProtoDeserializer(HbProtoBufPojo.class,
                transfer -> transfer::fromBytes);
        registerHeaderBasedProtoDeserializer(com.google.protobuf.Message.class,
                transfer -> transfer::protoFromBytes);
        registerHeaderBasedProtoDeserializer(GeneratedMessageV3.class,
                transfer -> transfer::protoFromBytes);
        registerHeaderBasedProtoDeserializer(DynamicMessage.class,
                transfer -> payload -> DynamicMessage.parseFrom(transfer.getDescriptor(), payload));

        registerDeserializer(BYTE_ARRAY_CLASS, UnaryOperator.identity());
        registerDeserializer(Void.class, value -> null);
        registerDeserializer(void.class, value -> null);

        registerSerializer(BYTE_ARRAY_CLASS, UnaryOperator.identity());
        registerSerializer(KafkaNull.class, value -> null);
        registerSerializer(Void.class, value -> null);
        registerSerializer(void.class, value -> null);

        registerHeaderSpec(BYTE_ARRAY_CLASS, Collections.emptyMap());
        registerHeaderSpec(KafkaNull.class, Collections.emptyMap());
        registerHeaderSpec(Void.class, Collections.emptyMap());
        registerHeaderSpec(void.class, Collections.emptyMap());

        final Function<Object, String> noIIDFunction = value -> null;
        registerIIDReader(BYTE_ARRAY_CLASS, noIIDFunction);
        registerIIDReader(KafkaNull.class, noIIDFunction);
        registerIIDReader(Void.class, noIIDFunction);
        registerIIDReader(void.class, noIIDFunction);
    }

    @Override
    protected Object extractAndConvertValue(final ConsumerRecord<?, ?> record, final Type type) {
        final Object payload = record.value();
        if (payload == null) {
            return KafkaNull.INSTANCE;
        }
        try {
            return this.deserializers.computeIfAbsent(type, this.deserializerGenerator)
                    .apply(record.headers(), (byte[]) payload);
        } catch (final ClassCastException e) {
            // Throw exception with a helpful error message in case of configuration errors.
            if (payload instanceof byte[]) {
                throw e;
            } else {
                throw new IllegalArgumentException(
                        "Payload is not of type byte[] did you configure your native kafka converter correctly?\n"
                                + "Add the following to lines to your application.properties:\n\n"
                                + "spring.kafka.consumer.properties.value.deserializer=org.apache.kafka.common.serialization.ByteArrayDeserializer\n"
                                + "spring.kafka.producer.properties.value.serializer=org.apache.kafka.common.serialization.ByteArraySerializer\n",
                        e);
            }
        }
    }

    /**
     * Registers the given deserializer for the given type. This method will replace the previous
     * deserializer, if any. The registered deserializer must return the same type or a compatible type
     * as specified by the given type.
     *
     * @param type The type to register the deserializer for. The type must match using
     *        {@link Object#equals(Object) equals()} in order to be used.
     * @param deserializer The deserializer to register.
     */
    public void registerDeserializer(final Type type, final Function<byte[], ?> deserializer) {
        requireNonNull(type, "type");
        requireNonNull(deserializer, "deserializer");
        this.deserializers.put(type, (headers, payload) -> deserializer.apply(payload));
    }

    /**
     * Registers the given deserializer for the given type. This method will replace the previous
     * deserializer, if any. The registered deserializer must return the same type or a compatible type
     * as specified by the given type.
     *
     * @param type The type to register the deserializer for. The type must match using
     *        {@link Object#equals(Object) equals()} in order to be used.
     * @param deserializer The deserializer to register.
     */
    public void registerDeserializer(final Type type, final BiFunction<Headers, byte[], ?> deserializer) {
        requireNonNull(type, "type");
        requireNonNull(deserializer, "deserializer");
        this.deserializers.put(type, deserializer);
    }

    /**
     * Creates a new deserializer for the given type.
     *
     * @param type The type that should be converted.
     * @return The newly created deserializer for the given type.
     */
    @SuppressWarnings("squid:S1452") // Prevent unnecessary raw casts
    protected BiFunction<Headers, byte[], ?> deserializerFor(final Type type) {
        requireNonNull(type, "type");
        if (type instanceof Class) {
            final Class<?> clazz = (Class<?>) type;
            if (GeneratedMessageV3.class.isAssignableFrom(clazz)) {
                final PojoTransfer<?, ?> transfer = ProtoPojoUtils.toPojoTransfer(clazz);
                return protoDeserializer(transfer, transfer::protoFromBytes);
            }
            if (HbProtoBufPojo.class.isAssignableFrom(clazz)) {
                final PojoTransfer<?, ?> transfer = ProtoPojoUtils.toPojoTransfer(clazz);
                return protoDeserializer(transfer, transfer::fromBytes);
            }
        } else if (type instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) type;
            if (Lazy.class.equals(parameterizedType.getRawType())) {
                final Type typeParameter = parameterizedType.getActualTypeArguments()[0];
                final BiFunction<Headers, byte[], ?> deserializer = deserializerFor(typeParameter);
                return (headers, data) -> new Lazy<>(() -> deserializer.apply(headers, data));
            }
        }
        return this.fallbackDeserializerGenerator.apply(type);
    }

    /**
     * Wrapper that ensures the correct error handling for protobuf deserialization.
     *
     * @param <R> The expected return type.
     * @param transfer The transfer instance to get.
     * @param deserializer The deserializer to use.
     * @return The deserializer with some improved error messages.
     */
    private <R> BiFunction<Headers, byte[], R> protoDeserializer(final PojoTransfer<?, ?> transfer,
            final ProtoDeserializer<R> deserializer) {
        return (headers, payload) -> {
            try {
                return deserializer.deserialize(payload);
            } catch (final InvalidProtocolBufferException e) {
                final Headers headers1 = headers;
                final String type = KafkaUtil.getTypeFromHeaders(headers1);
                final String expectedProtoName = transfer.getFullProtoName();
                if (type == null || type.equals(expectedProtoName)) {
                    throw new IllegalArgumentException("Failed to parse payload as " + expectedProtoName, e);
                } else {
                    throw new IllegalArgumentException("Received " + type + " but expected " + expectedProtoName, e);
                }
            }
        };
    }

    /**
     * Wrapper that ensures the correct error handling for protobuf deserialization with lazy type
     * evaluation.
     *
     * @param <R> The expected return type.
     * @param deserializerFactory The deserializer factory to use.
     * @return The deserializer with some improved error messages.
     */
    private <R> BiFunction<Headers, byte[], R> headerBasedProtoDeserializer(
            final Function<PojoTransfer<?, ?>, ProtoDeserializer<R>> deserializerFactory) {

        return (headers, payload) -> {
            final Headers headers1 = headers;
            final String type = KafkaUtil.getTypeFromHeaders(headers1);
            if (type == null) {
                throw new IllegalArgumentException("Message does not contain a protobuf type header");
            }
            final PojoTransfer<?, ?> transfer = ProtoPojoUtils.toPojoTransfer(type);
            try {
                return deserializerFactory.apply(transfer).deserialize(payload);
            } catch (final InvalidProtocolBufferException e) {
                throw new IllegalArgumentException("Failed to parse payload as " + type, e);
            }
        };
    }

    /**
     * Helper function that combines {@link #registerDeserializer(Type, BiFunction)} and
     * {@link #headerBasedProtoDeserializer(Function)}.
     *
     * @param <R> The expected return type.
     * @param type The type to register the deserializer for.
     * @param deserializerFactory The deserializer factory to use.
     */
    private <R> void registerHeaderBasedProtoDeserializer(final Type type,
            final Function<PojoTransfer<?, ?>, ProtoDeserializer<R>> deserializerFactory) {
        registerDeserializer(type, headerBasedProtoDeserializer(deserializerFactory));
    }

    @FunctionalInterface
    private interface ProtoDeserializer<R> {

        /**
         * Deserializes the given input.
         *
         * @param payload The payload to deserialize.
         * @return The deserialized instance.
         * @throws InvalidProtocolBufferException If the conversion failed due to parsing errors.
         */
        R deserialize(byte[] payload) throws InvalidProtocolBufferException;

    }

    @Override
    protected byte[] convertPayload(final Message<?> message) {
        final Object payload = message.getPayload();
        return this.serializers.computeIfAbsent(payload.getClass(), this.serializerGenerator).apply(payload);
    }

    @Override
    protected Headers initialRecordHeaders(final Message<?> message) {
        final Headers headers = super.initialRecordHeaders(message);
        final Object payload = message.getPayload();
        final Class<?> clazz = payload.getClass();
        this.headerSpecs.computeIfAbsent(clazz, this.headerSpecGenerator)
                .forEach(headers::add);
        final String iid = this.iidReaders.computeIfAbsent(clazz, this.iidReaderGenerator).apply(payload);
        if (iid != null) {
            headers.add(HEADER_X_IID, headerToBytes(iid));
        }
        if (this.origin != null) {
            headers.add(HEADER_X_ORIGIN, headerToBytes(this.origin));
        }
        return headers;
    }

    /**
     * Registers the given serializer for the given type. This method will replace the previous
     * serializer, if any.
     *
     * @param <T> The type to register the serializer for.
     * @param clazz The type to register the serializer for. The type must match using
     *        {@link Object#equals(Object) equals()} in order to be used.
     * @param serializer The serializer to register.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> void registerSerializer(final Class<T> clazz, final Function<? super T, byte[]> serializer) {
        requireNonNull(clazz, "clazz");
        requireNonNull(serializer, "serializer");
        this.serializers.put(clazz, (Function) serializer);
    }

    /**
     * Creates a new serializer for the given type.
     *
     * @param <T> The type the serializer is for.
     * @param clazz The type that should be converted.
     * @return The newly created serializer for the given type.
     */
    @SuppressWarnings({"squid:S1452", "unchecked"}) // Prevent unnecessary raw casts
    protected <T> Function<? super T, byte[]> serializerFor(final Class<T> clazz) {
        if (GeneratedMessageV3.class.isAssignableFrom(clazz)) {
            return value -> ((GeneratedMessageV3) value).toByteArray();
        }
        if (HbProtoBufPojo.class.isAssignableFrom(clazz)) {
            return publicMethodAsFunction(clazz, "toBytes", clazz);
        }
        // Cannot avoid the cast because variables/fields cannot have a type parameter :-(
        return (Function<? super T, byte[]>) this.fallbackSerializerGenerator.apply(clazz);
    }

    /**
     * Registers the given header spec for the given type. This method will replace the previous header
     * spec, if any.
     *
     * @param clazz The type to register the header spec for. The type must match using
     *        {@link Object#equals(Object) equals()} in order to be used.
     * @param spec The header spec to register.
     */
    public void registerHeaderSpec(final Class<?> clazz, final Map<String, byte[]> spec) {
        requireNonNull(clazz, "clazz");
        requireNonNull(spec, "spec");
        this.headerSpecs.put(clazz, spec);
    }

    /**
     * Creates a new header spec for the given type.
     *
     * @param clazz The type that should be converted.
     * @return The newly created header spec for the given type.
     */
    protected Map<String, byte[]> headerSpecFor(final Class<?> clazz) {
        if (isProtoClass(clazz) || isJavaClass(clazz)) {
            final Map<String, byte[]> spec = new LinkedHashMap<>(1);
            final String protoTypeName = toFullProtoName(clazz);
            spec.put(HEADER_X_PROTOBUF_TYPE, headerToBytes(protoTypeName));
            final String interfaceVersion = toInterfaceVersion(clazz);
            if (interfaceVersion == null) {
                final String protoFileName = toDescriptor(clazz).getFile().getFullName();
                log.warn("Could not find interface version for {} ({}@{})\n"
                        + "    Please update your interface project ASAP!\n"
                        + "    Clients might misbehave!", clazz, protoTypeName, protoFileName);
            } else {
                spec.put(HEADER_X_PROTOBUF_INTERFACEVERSION, headerToBytes(interfaceVersion));
            }
            return spec;
        }
        return this.fallbackHeaderSpecGenerator.apply(clazz);
    }

    /**
     * Registers the given iid reader for the given type. This method will replace the previous iid
     * reader, if any. The iid reader should return null, if the type does not support IIDs.
     *
     * @param <T> The type to register the iid reader for.
     * @param clazz The type to register the iid reader for. The type must match using
     *        {@link Object#equals(Object) equals()} in order to be used.
     * @param iidReader The iid reader to register.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> void registerIIDReader(final Class<T> clazz, final Function<? super T, String> iidReader) {
        requireNonNull(clazz, "clazz");
        requireNonNull(iidReader, "iidReader");
        this.iidReaders.put(clazz, (Function) iidReader);
    }

    /**
     * Creates a new iid reader for the given type.
     *
     * @param clazz The type that should be converted.
     * @return The newly created iid reader for the given type.
     */
    protected Function<?, String> iidReaderFor(final Class<?> clazz) {
        if (isProtoMessage(clazz)) {
            return iidReaderForMessage(clazz.asSubclass(com.google.protobuf.Message.class));
        }
        if (IIDContainer.class.isAssignableFrom(clazz)) {
            return iidReaderForIIDContainer(clazz.asSubclass(IIDContainer.class));
        }
        // Print a stacktrace for easier lookup where the bad class was send.
        final ClassCastException t =
                new ClassCastException("Java-Class " + clazz.getName() + " does not implement IIDContainer");
        log.warn("Failed to prepare iid reader", t);
        return message -> null;
    }

    /**
     * Creates a new iid reader for the given type.
     *
     * @param <T> The message type to create it for.
     * @param clazz The clazz to create it for.
     * @return The newly created iid reader for the given type.
     */
    protected <T extends com.google.protobuf.Message> Function<T, String> iidReaderForMessage(final Class<T> clazz) {
        final Descriptor descriptor = ProtoPojoUtils.toMessageDescriptor(clazz);
        final Optional<FieldDescriptor> optIIDField = descriptor.getFields().stream()
                .filter(field -> field.getName().equals("iid"))
                .findAny();
        if (optIIDField.isPresent()) {
            final FieldDescriptor iidField = optIIDField.get();
            return message -> {
                final String iid = (String) message.getField(iidField);
                if (iid == null || iid.isEmpty()) {
                    throw new IllegalArgumentException("IID cannot be null or empty for " + descriptor.getFullName());
                }
                return iid;
            };
        } else {
            log.warn("Protobuf-Type {} does not specify an iid field", clazz);
            return message -> null;
        }
    }

    /**
     * Creates a new iid reader for the given type.
     *
     * @param <T> The container type to create it for.
     * @param clazz The clazz to create it for.
     * @return The newly created iid reader for the given type.
     */
    protected <T extends IIDContainer> Function<T, String> iidReaderForIIDContainer(final Class<T> clazz) {
        return message -> {
            final String iid = message.getIid();
            if (iid == null || iid.isEmpty()) {
                throw new IllegalArgumentException("IID cannot be null or empty for " + clazz.getName());
            }
            return iid;
        };
    }

    /**
     * Converts the given header content to bytes. This method uses UTF-8 encoding.
     *
     * @param content The header content to convert.
     * @return The bytes representing the given header content.
     */
    protected byte[] headerToBytes(final String content) {
        return content.getBytes(UTF_8);
    }

    /**
     * Gets the fallback deserializer generator this instance uses if there is neither a registered
     * deserializer nor any default deserializers for a given type.
     *
     * @return The fallback deserializer generator
     */
    @SuppressWarnings("squid:S1452") // Prevent unnecessary raw casts (in the implementation)
    public Function<Type, BiFunction<Headers, byte[], ?>> getFallbackDeserializerGenerator() {
        return this.fallbackDeserializerGenerator;
    }

    /**
     * Sets the fallback deserializer generator this instance should use if there is neither a
     * registered deserializer nor any default deserializers for a given type. This method replaces the
     * previous fallback. Beware it does not remove the serdes, that have already been created by the
     * existing fallback. The generated functions must produce results of the same type or a compatible
     * type as was used to generate them.
     *
     * @param fallbackDeserializerGenerator The fallback deserializer generator
     */
    public void setFallbackDeserializerGenerator(
            final Function<Type, BiFunction<Headers, byte[], ?>> fallbackDeserializerGenerator) {
        this.fallbackDeserializerGenerator = requireNonNull(fallbackDeserializerGenerator,
                "fallbackDeserializerGenerator");
    }

    /**
     * Gets the fallback serializer generator this instance uses if there is neither a registered
     * serializer nor any default serializers for a given type.
     *
     * @return The fallback deserializer generator
     */
    @SuppressWarnings("squid:S1452") // Prevent unnecessary raw casts (while implementing these)
    public Function<Class<?>, Function<?, byte[]>> getFallbackSerializerGenerator() {
        return this.fallbackSerializerGenerator;
    }

    /**
     * Sets the fallback serializer generator this instance should use if there is neither a registered
     * serializer nor any default serializers for a given type. This method replaces the previous
     * fallback. Beware, it does not remove the serdes, that have already been created by the existing
     * fallback. The generated function must accept instances of the class that was used to generate
     * them.
     *
     * @param fallbackSerializerGenerator The fallback serializer generator
     */
    public void setFallbackSerializerGenerator(
            final Function<Class<?>, Function<?, byte[]>> fallbackSerializerGenerator) {
        this.fallbackSerializerGenerator = requireNonNull(fallbackSerializerGenerator, "fallbackSerializerGenerator");
    }

    /**
     * Gets the fallback serializer generator this instance uses if there is neither a registered header
     * spec nor any default header spec for a given type.
     *
     * @return The fallback deserializer generator
     */
    public Function<Class<?>, Map<String, byte[]>> getFallbackHeaderSpecGenerator() {
        return this.fallbackHeaderSpecGenerator;
    }

    /**
     * Sets the fallback header spec generator this instance should use if there is neither a registered
     * header spec nor any default header spec for a given type. This method replaces the previous
     * fallback. Beware, it does not remove the header spec, that have already been created by the
     * existing fallback.
     *
     * @param fallbackHeaderSpecGenerator The fallback header spec generator
     */
    public void setFallbackHeaderSpecGenerator(
            final Function<Class<?>, Map<String, byte[]>> fallbackHeaderSpecGenerator) {
        this.fallbackHeaderSpecGenerator = requireNonNull(fallbackHeaderSpecGenerator, "fallbackHeaderSpecGenerator");
    }

    /**
     * Sets both the fallback deserializer generator and serializer generator this instance should use
     * if there are neither an registered serde nor any default serdes for a given type. They are
     * created using the given {@link ObjectMapper} instance and replace any previously set fallbacks.
     * Beware it does not remove the serdes, that have already been created by the existing fallback.
     *
     * @param objectMapper The objectMapper instance to use as fallback.
     */
    public void setFallbackSerdeGenerator(final ObjectMapper objectMapper) {
        requireNonNull(objectMapper, "objectMapper");
        setFallbackDeserializerGenerator(type -> {
            final JavaType javaType = objectMapper.constructType(type);
            return (headers, payload) -> {
                try {
                    return objectMapper.readValue(payload, javaType);
                } catch (final IOException e) {
                    throw new UncheckedIOException("Failed to json deserialize type " + type.getTypeName(), e);
                }
            };
        });
        setFallbackSerializerGenerator(clazz -> value -> {
            try {
                return objectMapper.writeValueAsBytes(value);
            } catch (final IOException e) {
                throw new UncheckedIOException("Failed to json serialize type " + clazz.getName(), e);
            }
        });
        setFallbackHeaderSpecGenerator(clazz -> Collections.emptyMap());
    }

}
