// Generated from ${protoFile} on ${date}
// by ${plugin.groupId}:${plugin.artifactId}:${plugin.version}

package ${javaPackage};

import java.util.Arrays;
import java.util.Collection;

import com.google.protobuf.Descriptors.FileDescriptor;

import de.heuboe.vmis2.jprotoc.transferinterface.BaseTransfer;
import de.heuboe.vmis2.jprotoc.transferinterface.ProtoTransferCatalog;
import de.heuboe.vmis2.jprotoc.utils.InterfaceVersionUtils;

/**
 * The catalog for the {@link BaseTransfer transfer definitions} specified in:
 * {@code ${protoFile}}
 */
public final class ${catalogClass} implements ProtoTransferCatalog {

    @Override
    public Collection<BaseTransfer<?, ?>> getTransfers() {
        return Arrays.asList(
<#list javaClasses as javaClass>
                ${javaClass}.transfer()<#sep>,
</#list>);
    }

    @Override
    public int hashCode() {
        return getClass().getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ${catalogClass};
    }

    @Override
    public String toString() {
        return "${javaPackage}.${catalogClass} (Generated from ${protoFile} on ${date})";
    }

    /**
     * Gets the descriptor for the file that was used to generate the classes referenced by this catalog.
     *
     * @return The file descriptor associated to this catalog.
     */
    public static FileDescriptor getDescriptor() {
        return ${protoPackageName}.${outerClassName}.getDescriptor();
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
