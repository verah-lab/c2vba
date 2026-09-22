package de.heuboe.vmis2.jprotoc.utils;

import static de.heuboe.protobuf.InterfaceVersionProto.interfaceVersion;

import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.GenericDescriptor;

import de.heuboe.protobuf.InterfaceVersionProto;

/**
 * Utility methods related to {@link InterfaceVersionProto}.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public final class InterfaceVersionUtils {

    /**
     * Gets the interface version from the given proto file.
     *
     * @param descriptor The descriptor to get the information from.
     * @return The proto file's interface version or null.
     */
    public static String getInterfaceVersion(final FileDescriptor descriptor) {
        final FileOptions options = descriptor.getOptions();
        if (options.hasExtension(interfaceVersion)) {
            return options.getExtension(interfaceVersion);
        } else {
            return null;
        }
    }

    /**
     * Gets the interface version from the given proto.
     *
     * @param descriptor The descriptor to get the information from.
     * @return The proto's file's interface version or null.
     */
    public static String getInterfaceVersion(final GenericDescriptor descriptor) {
        return getInterfaceVersion(descriptor.getFile());
    }

    private InterfaceVersionUtils() {}

}
