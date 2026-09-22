package de.heuboe.vmis2.jprotoc.utils;

import com.google.protobuf.Descriptors.GenericDescriptor;

import de.heuboe.protobuf.InterfaceVersionProto;

/**
 * A helper class with global constants for protobuf classes and their java pojos/enums.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public final class Constants {

    /**
     * The header that specifies the iid of the message.
     */
    public static final String HEADER_X_IID = "X-IID";

    /**
     * The header that specifies which application sent the message.
     */
    public static final String HEADER_X_ORIGIN = "X-Origin";

    /**
     * The header that specifies the interface version of the proto file that specifies the protobuf
     * message type, that is sent.
     *
     * @see InterfaceVersionProto#interfaceVersion
     * @see ProtoPojoUtils#toFullProtoName(Class)
     */
    public static final String HEADER_X_PROTOBUF_INTERFACEVERSION = "X-Protobuf-InterfaceVersion";

    /**
     * The header that specifies the protobuf message type, that is sent.
     *
     * <p>
     * <b>Note:</b> The header will be sent as the fully qualified name of the protobuf message. This
     * will neither contain the POJO name, nor will it contain surrounding quotes.
     * </p>
     *
     * @see GenericDescriptor#getFullName()
     * @see ProtoPojoUtils#toFullProtoName(Class)
     */
    public static final String HEADER_X_PROTOBUF_TYPE = "X-Protobuf-Type";

    /**
     * The header that specifies a reference to another message.
     */
    public static final String HEADER_X_REF_IID = "X-REF-IID";

    private Constants() {}
}
