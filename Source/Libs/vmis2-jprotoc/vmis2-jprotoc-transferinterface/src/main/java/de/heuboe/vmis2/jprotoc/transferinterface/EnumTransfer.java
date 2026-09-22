package de.heuboe.vmis2.jprotoc.transferinterface;

import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.ProtocolMessageEnum;

/**
 * Transfer instances provide methods for easy conversion from and to enum and protobuf instances.
 * Implementations are expected to be thread safe and stateless.
 *
 * @param <ProtoBuf> The protobuf type used as conversion source/target.
 * @param <Pojo> The java enum type used as conversion source/target
 */
public interface EnumTransfer<ProtoBuf extends ProtocolMessageEnum, Pojo extends Enum<Pojo> & HbProtoBufJavaEnum>
        extends BaseTransfer<ProtoBuf, Pojo> {

    /**
     * Converts the given number to the a pojo enum instance.
     *
     * @param number The number to convert.
     * @return The pojo represented by the given number.
     */
    default Pojo fromNumber(final int number) {
        return fromProto(protoFromNumber(number));
    }

    /**
     * Converts the given number to the a proto enum instance.
     *
     * @param number The number to convert.
     * @return The proto represented by the given number.
     */
    ProtoBuf protoFromNumber(final int number);

    // Narrow types - Do NOT remove (Breaking change)!

    @Override
    Class<ProtoBuf> protoClass();

    @Override
    Class<Pojo> pojoClass();

    @Override
    EnumDescriptor getDescriptor();

    @Override
    Pojo fromProto(ProtoBuf protobuf);

    @Override
    ProtoBuf toProto(Pojo pojo);

}
