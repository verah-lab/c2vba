package de.heuboe.vmis2.jprotoc.utils;

import static de.heuboe.vmis2.jprotoc.utils.InterfaceVersionUtils.getInterfaceVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.GenericDescriptor;
import com.google.protobuf.Timestamp;
import com.google.protobuf.TimestampProto;

import de.heuboe.protobuf.DocumentationProto;
import de.heuboe.protobuf.ExternalReference;
import de.heuboe.protobuf.InterfaceVersionProto;
import de.heuboe.protobuf.Stereotype;
import de.heuboe.vmis2.jprotoc.transferinterface.Project;

/**
 * Tests for {@link InterfaceVersionUtils}.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
class InterfaceVersionUtilsTest {

    private static final String VERSION = Project.VERSION.replace("-SNAPSHOT", "");

    /**
     * Test for {@link InterfaceVersionUtils#getInterfaceVersion(FileDescriptor)}.
     */
    @Test
    void testGetInterfaceVersionFile() {
        assertEquals(VERSION, getInterfaceVersion(InterfaceVersionProto.getDescriptor()));
        assertEquals(VERSION, getInterfaceVersion(DocumentationProto.getDescriptor()));
        assertNull(getInterfaceVersion(TimestampProto.getDescriptor()));
    }

    /**
     * Test for {@link InterfaceVersionUtils#getInterfaceVersion(GenericDescriptor)}.
     */
    @Test
    void testGetInterfaceVersionGeneric() {
        assertEquals(VERSION, getInterfaceVersion((GenericDescriptor) InterfaceVersionProto.getDescriptor()));
        assertEquals(VERSION, getInterfaceVersion(ExternalReference.getDescriptor()));
        assertEquals(VERSION, getInterfaceVersion(Stereotype.getDescriptor()));
        assertNull(getInterfaceVersion(Timestamp.getDescriptor()));
    }

}
