// Generated from Test.proto on 2019-06-06T11:10:14+02:00
// by de.heuboe.asfinag:vmis2-jprotoc-plugin:2.2.0-SNAPSHOT

package de.heuboe.vmis2.jprotoc.test.pojo;

import java.util.Arrays;
import java.util.Collection;

import com.google.protobuf.Descriptors.FileDescriptor;

import de.heuboe.vmis2.jprotoc.transferinterface.BaseTransfer;
import de.heuboe.vmis2.jprotoc.transferinterface.ProtoTransferCatalog;

/**
 * The catalog for the {@link BaseTransfer transfer defintions} specified in:
 * {@code Test.proto}
 */
public final class PTestProtoTransferCatalog implements ProtoTransferCatalog {

    @Override
    public Collection<BaseTransfer<?, ?>> getTransfers() {
        return Arrays.asList(
                PTestEnum.transfer(),
                PTestMessage.transfer(),
                PTestMessage.InnerMessage.transfer());
    }

    @Override
    public int hashCode() {
        return getClass().getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PTestProtoTransferCatalog;
    }

    @Override
    public String toString() {
        return "de.heuboe.vmis2.jprotoc.test.pojo.PTestProtoTransferCatalog (Generated from Test.proto on 2019-06-06T11:10:14+02:00)";
    }

    /**
     * Gets the descriptor for the file that was used to generate the classes referenced by this catalog.
     *
     * @return The file descriptor associated to this catalog.
     */
    public static FileDescriptor getDescriptor() {
        return de.heuboe.vmis2.jprotoc.test.TestProto.getDescriptor();
    }

}
