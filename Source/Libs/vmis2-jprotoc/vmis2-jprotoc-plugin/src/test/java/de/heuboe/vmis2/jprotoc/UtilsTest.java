package de.heuboe.vmis2.jprotoc;

import static de.heuboe.vmis2.jprotoc.Utils.extractOuterClassname;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;

class UtilsTest {

    @Test
    void testExtractOuterClassname() {
        assertEquals("Test",
                extractOuterClassname(newFileBuilder().setName("test.proto").build()));
        assertEquals("FooBar",
                extractOuterClassname(newFileBuilder().setName("foo_bar.proto").build()));
        // https://gitlab.heuboe.hbintern/VMIS2/base/vmis2-jprotoc/issues/7
        assertEquals("Vmis2Broker2DbBridgeInterface",
                extractOuterClassname(newFileBuilder().setName("vmis2-broker2db-bridge-interface.proto").build()));
        assertEquals("LargeNumbers4Enums",
                extractOuterClassname(newFileBuilder().setName("large-Numbers4Enums.proto").build()));
        assertEquals("VErYUGGlyNAME4The5ThGeneration",
                extractOuterClassname(newFileBuilder().setName("vErY-UGGly_nAME_4the5th.generation.proto").build()));
        assertEquals("TestOuterClass",
                extractOuterClassname(newFileBuilder().setName("test.proto")
                        .addMessageType(newMessageBuilder().setName("Test"))
                        .build()));
    }

    private DescriptorProto.Builder newMessageBuilder() {
        return DescriptorProto.newBuilder();
    }

    private FileDescriptorProto.Builder newFileBuilder() {
        return FileDescriptorProto.newBuilder();
    }

}
