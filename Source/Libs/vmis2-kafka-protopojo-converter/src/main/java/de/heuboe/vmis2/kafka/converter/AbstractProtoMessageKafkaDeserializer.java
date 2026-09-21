package de.heuboe.vmis2.kafka.converter;

import java.io.UncheckedIOException;
import java.util.Map;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

/**
 * Abstract deserializer for proto messages.
 *
 * @param <T> The type to deserialize.
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public abstract class AbstractProtoMessageKafkaDeserializer<T> implements Deserializer<T> {

    /**
     * The configuration key for the default protobuf message type.
     */
    public static final String KEY_DEFAULT_TYPE = "default_type";

    private String defaultType;

    /**
     * Sets the default protobuf type to use, if not specified by a header.
     *
     * @param defaultType The default protobuf type.
     */
    public void setDefaultType(final String defaultType) {
        this.defaultType = defaultType;
    }

    @Override
    public void configure(final Map<String, ?> configs, final boolean isKey) {
        setDefaultType((String) configs.get(KEY_DEFAULT_TYPE));
    }

    /**
     * Deserializes the given data as proto message. This method assumes that the
     * {@link #setDefaultType(String) default type} has been configured.
     *
     * @param data The data to deserialize or null.
     * @return The deserialized message, or null if the given data were null.
     * @throws UncheckedIOException If there was a deserialization error.
     */
    public T deserialize(final byte[] data) {
        if (this.defaultType == null) {
            if (data == null) {
                return null;
            } else {
                throw new UnsupportedOperationException(
                        "Deserialization requires the headers or a configured 'default_type'");
            }
        } else {
            return deserializeType(this.defaultType, data);
        }
    }

    /**
     * {@inheritDoc} This method assumes that the {@link #setDefaultType(String) default type} has been
     * configured.
     */
    @Override
    public T deserialize(final String topic, final byte[] data) {
        return deserialize(data);
    }

    @Override
    public T deserialize(final String topic, final Headers headers, final byte[] data) {
        final String type = KafkaUtil.getTypeFromHeaders(headers);
        if (type == null) {
            return deserialize(data);
        } else {
            return deserializeType(type, data);
        }
    }

    /**
     * Deserializes the given data as proto message.
     *
     * @param type The proto type associated with the data.
     * @param data The data to deserialize or null.
     * @return The deserialized message, or null if the given data were null.
     * @throws UncheckedIOException If there was a deserialization error.
     */
    public abstract T deserializeType(final String type, final byte[] data);

}
