package de.heuboe.vmis2.kafka.converter;

import java.io.UncheckedIOException;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;

import de.heuboe.vmis2.jprotoc.transferinterface.PojoTransfer;
import de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils;

/**
 * Proto deserializer for proto {@link DynamicMessage}s.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public class ProtoDynamicMessageKafkaDeserializer extends AbstractProtoMessageKafkaDeserializer<DynamicMessage> {

    @Override
    public DynamicMessage deserializeType(final String type, final byte[] data) {
        final PojoTransfer<?, ?> transfer = ProtoPojoUtils.toPojoTransfer(type);
        return deserializeType(transfer, data);
    }

    /**
     * Deserializes the given data as {@link DynamicMessage}.
     *
     * @param transfer The transfer instance with the type information.
     * @param data The data to deserialize or null.
     * @return The deserialized message, or null if the given data were null.
     * @throws UncheckedIOException If there was a deserialization error.
     */
    public DynamicMessage deserializeType(final PojoTransfer<?, ?> transfer, final byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            return DynamicMessage.parseFrom(transfer.getDescriptor(), data);
        } catch (final InvalidProtocolBufferException e) {
            throw new UncheckedIOException(
                    "Failed to deserialize " + transfer.getFullProtoName() + " to DynamicMessage", e);
        }
    }

}
