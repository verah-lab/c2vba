package de.heuboe.vmis2.kafka.converter;

import java.io.UncheckedIOException;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import de.heuboe.vmis2.jprotoc.transferinterface.PojoTransfer;
import de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils;

/**
 * Proto deserializer for proto {@link Message}s.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public class ProtoMessageKafkaDeserializer extends AbstractProtoMessageKafkaDeserializer<GeneratedMessageV3> {

    @Override
    public GeneratedMessageV3 deserializeType(final String type, final byte[] data) {
        final PojoTransfer<?, ?> transfer = ProtoPojoUtils.toPojoTransfer(type);
        return deserializeType(transfer, data);
    }

    /**
     * Deserializes the given data as {@link GeneratedMessageV3}.
     *
     * @param <T> The type to deserialize.
     * @param transfer The transfer instance with the type information.
     * @param data The data to deserialize or null.
     * @return The deserialized message, or null if the given data were null.
     * @throws UncheckedIOException If there was a deserialization error.
     */
    public <T extends GeneratedMessageV3> T deserializeType(final PojoTransfer<T, ?> transfer, final byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            return transfer.protoFromBytes(data);
        } catch (final InvalidProtocolBufferException e) {
            throw new UncheckedIOException("Failed to deserialize " + transfer.getFullProtoName(), e);
        }
    }

}
