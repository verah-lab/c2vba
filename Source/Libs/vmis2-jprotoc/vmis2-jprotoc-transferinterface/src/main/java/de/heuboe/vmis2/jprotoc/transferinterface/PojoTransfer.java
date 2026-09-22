package de.heuboe.vmis2.jprotoc.transferinterface;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Transfer instances provide methods for easy conversion from and to pojo and protobuf instances or
 * plain bytes. Implementations are expected to be thread safe and stateless.
 *
 * @param <ProtoBuf> The protobuf type used as conversion source/target.
 * @param <Pojo> The pojo type used as conversion source/target
 */
public interface PojoTransfer<ProtoBuf extends GeneratedMessageV3, Pojo extends HbProtoBufPojo>
        extends BaseTransfer<ProtoBuf, Pojo> {

    /**
     * Serializes the given pojo to a byte array.
     *
     * <p>
     * This is usually done in a two step conversion. First from the pojo to a protobuf instance, and
     * then from the protobuf instance to a byte array.
     * </p>
     *
     * @param pojo The pojo to convert.
     * @return The byte array representing the given pojo.
     * @throws NullPointerException If the given pojo is null.
     */
    default byte[] toBytes(final Pojo pojo) {
        return toProto(pojo).toByteArray();
    }

    /**
     * Deserializes the given input bytes to a pojo instance.
     *
     * <p>
     * This is usually done in a two step conversion. First from the byte array to a protobuf instance,
     * and then from the protobuf instance to a pojo.
     * </p>
     *
     * @param input The input bytes to convert.
     * @return The pojo represented by the given byte array.
     * @throws InvalidProtocolBufferException If the given input data do not match those required for a
     *         pojo.
     */
    Pojo fromBytes(byte[] input) throws InvalidProtocolBufferException;

    /**
     * Deserializes the given input bytes to a proto instance.
     *
     * @param input The input bytes to convert.
     * @return The proto represented by the given byte array.
     * @throws InvalidProtocolBufferException If the given input data do not match those required for a
     *         proto.
     */
    ProtoBuf protoFromBytes(final byte[] input) throws InvalidProtocolBufferException;

    // Narrow types - Do not remove (Breaking change)!

    @Override
    Class<ProtoBuf> protoClass();

    @Override
    Class<Pojo> pojoClass();

    @Override
    Descriptor getDescriptor();

    @Override
    Pojo fromProto(ProtoBuf protobuf);

    @Override
    ProtoBuf toProto(Pojo pojo);

}
