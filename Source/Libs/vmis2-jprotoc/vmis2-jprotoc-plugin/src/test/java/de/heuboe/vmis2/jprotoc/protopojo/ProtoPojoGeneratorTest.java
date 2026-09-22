package de.heuboe.vmis2.jprotoc.protopojo;

import static de.heuboe.vmis2.jprotoc.GeneratorTestUtils.loadResource;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

/**
 * Tests for the {@link ProtoPojoGenerator}.
 */
class ProtoPojoGeneratorTest {

    @Test
    void test() throws IOException {
        assertThat(new ProtoPojoGenerator().generate(loadResource("/meinTestInput.protoB")))
                .hasSize(7);
    }

    @Test
    void testIsField() throws IOException {
        final CodeGeneratorRequest request = CodeGeneratorRequest.newBuilder()
                .addProtoFile(FileDescriptorProto.newBuilder()
                        .setName("IsMessage.proto")
                        .setPackage("de.heuboe.vmis2.test")
                        .addMessageType(DescriptorProto.newBuilder()
                                .setName("IsMessage")
                                .addField(FieldDescriptorProto.newBuilder()
                                        .setName("is_field")
                                        .setJsonName("isField")
                                        .setType(Type.TYPE_BOOL)
                                        .setNumber(1))))
                .addFileToGenerate("IsMessage.proto")
                .build();
        final List<File> generated = new ProtoPojoGenerator().generate(request).collect(Collectors.toList());
        assertThat(generated).hasSize(1);
        final File file = generated.get(0);
        assertThat(file.getContent()).contains(""
                + "    public static de.heuboe.vmis2.test.IsMessage to(PIsMessage pojo) {\n"
                + "\n"
                + "        de.heuboe.vmis2.test.IsMessage.Builder builder = de.heuboe.vmis2.test.IsMessage.newBuilder();\n"
                + "        builder.setIsField(\n"
                + "            pojo.isField()\n" // <-- This part is important
                + "        );\n"
                + "        return builder.build();\n"
                + "    }");
    }

}
