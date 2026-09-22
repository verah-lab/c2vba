package de.heuboe.vmis2.jprotoc.transferinterface;

import com.google.protobuf.Descriptors.GenericDescriptor;

/**
 * Transfer instances provide methods for easy conversion from and to pojo and protobuf instances.
 * Implementations are expected to be thread safe and stateless.
 *
 * @param <ProtoBuf> The protobuf type used as conversion source/target.
 * @param <Pojo> The pojo type used as conversion source/target
 */
public interface BaseTransfer<ProtoBuf, Pojo extends HbProtoBufJavaBase> {

    /**
     * Gets the protobuf class that can be used as conversion source/target.
     *
     * @return The protobuf class.
     */
    Class<ProtoBuf> protoClass();

    /**
     * Gets the pojo class that can be used as conversion source/target.
     *
     * @return The pojo class.
     */
    Class<Pojo> pojoClass();

    /**
     * Gets the descriptor that describes the protobuf structure.
     *
     * @return The descriptor.
     */
    GenericDescriptor getDescriptor();

    /**
     * Gets the type's fully-qualified name, within the proto language's namespace. This differs from
     * the Java name. For example, given this {@code .proto}:
     *
     * <pre>
     *   package foo.bar;
     *   option java_package = "com.example.protos"
     *   message Baz {}
     * </pre>
     *
     * {@code Baz}'s full name is {@code "foo.bar.Baz"}.
     *
     * @return The full proto name.
     */
    default String getFullProtoName() {
        return getDescriptor().getFullName();
    }

    /**
     * Converts the given protobuf instance to a pojo.
     *
     * @param protobuf The protobuf instance to convert.
     * @return The converted pojo.
     * @throws NullPointerException If the given protobuf instances is null.
     */
    Pojo fromProto(ProtoBuf protobuf);

    /**
     * Converts the given pojo to a protobuf instance.
     *
     * @param pojo The pojo to convert.
     * @return The converted protobuf instance.
     * @throws NullPointerException If the given pojo is null.
     */
    ProtoBuf toProto(Pojo pojo);

}
