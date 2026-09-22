package de.heuboe.protobuf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import de.heuboe.protobuf.pojo.PInterfaceVersionProtoTransferCatalog;
import de.heuboe.vmis2.jprotoc.transferinterface.Project;

class InterfaceVersionProtoTest {

    @Test
    void test() {
        assertEquals(Project.VERSION.replace("-SNAPSHOT", ""), InterfaceVersionProto.getDescriptor().getOptions()
                .getExtension(InterfaceVersionProto.interfaceVersion).replace("-SNAPSHOT", ""));
    }

    /**
     * Tests that the interface version of the file matches the project version..
     */
    @Test
    void testInterfaceVersion() {
        final String interfaceVersion = PInterfaceVersionProtoTransferCatalog.getInterfaceVersion();
        assertNotNull(interfaceVersion);
        if (Project.VERSION.endsWith("-SNAPSHOT")) {
            assertEquals(Project.VERSION.replace("-SNAPSHOT", ""), interfaceVersion.replace("-SNAPSHOT", ""));
        } else {
            assertEquals(Project.VERSION, interfaceVersion);
        }
    }

}
