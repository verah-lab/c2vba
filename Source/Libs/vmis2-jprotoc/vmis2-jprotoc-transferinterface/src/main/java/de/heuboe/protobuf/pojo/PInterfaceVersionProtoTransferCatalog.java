// Generated from InterfaceVersion.proto on 2020-03-25T17:36:59+01:00
// by de.heuboe.asfinag:vmis2-jprotoc-plugin:4.0.0-SNAPSHOT

package de.heuboe.protobuf.pojo;

import java.util.Arrays;
import java.util.Collection;

import com.google.protobuf.Descriptors.FileDescriptor;

import de.heuboe.vmis2.jprotoc.transferinterface.BaseTransfer;
import de.heuboe.vmis2.jprotoc.transferinterface.ProtoTransferCatalog;
import de.heuboe.vmis2.jprotoc.utils.InterfaceVersionUtils;

/**
 * The catalog for the {@link BaseTransfer transfer defintions} specified in:
 * {@code InterfaceVersion.proto}
 */
public final class PInterfaceVersionProtoTransferCatalog implements ProtoTransferCatalog {

    @Override
    public Collection<BaseTransfer<?, ?>> getTransfers() {
        return Arrays.asList(
);
    }

    @Override
    public int hashCode() {
        return getClass().getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PInterfaceVersionProtoTransferCatalog;
    }

    @Override
    public String toString() {
        return "de.heuboe.protobuf.pojo.PInterfaceVersionProtoTransferCatalog (Generated from InterfaceVersion.proto on 2020-03-25T17:36:59+01:00)";
    }

    /**
     * Gets the descriptor for the file that was used to generate the classes referenced by this catalog.
     *
     * @return The file descriptor associated to this catalog.
     */
    public static FileDescriptor getDescriptor() {
        return de.heuboe.protobuf.InterfaceVersionProto.getDescriptor();
    }

    /**
     * Gets the interface version for the file that was used to generate the classes referenced by this catalog.
     *
     * @return The interface version associated to this catalog.
     */
    public static String getInterfaceVersion() {
        return InterfaceVersionUtils.getInterfaceVersion(getDescriptor());
    }

}
