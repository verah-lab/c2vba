package de.heuboe.vmis2.jprotoc.documentation;

import static de.heuboe.vmis2.jprotoc.GeneratorTestUtils.loadResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

import de.heuboe.protobuf.DocumentationProto;
import de.heuboe.protobuf.InterfaceVersionProto;
import de.heuboe.vmis2.jprotoc.api.GenerationException;
import de.heuboe.vmis2.jprotoc.test.AbsentNested;
import de.heuboe.vmis2.jprotoc.test.AbsentNestedStereotype;
import de.heuboe.vmis2.jprotoc.test.AbsentRoot;
import de.heuboe.vmis2.jprotoc.test.AbsentRootStereotype;
import de.heuboe.vmis2.jprotoc.test.Present;

/**
 * Tests for {@link ProtoStereotypeAsserter}.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
class ProtoStereotypeAsserterTest {

    private final ProtoStereotypeAsserter asserter = new ProtoStereotypeAsserter();

    /**
     * Tests that the asserter doesn't actually generate files.
     */
    @Test
    void testPresent() {
        final Stream<File> generated = assertDoesNotThrow(() -> generateUsingDecriptor(Present.getDescriptor()));
        assertNotNull(generated);
        assertEquals(0, generated.count());
    }

    /**
     * Tests that the asserter doesn't actually generate files.
     */
    @Test
    void testPresentProto() {
        final Stream<File> generated =
                assertDoesNotThrow(() -> generateUsingProto(Present.getDescriptor().getFullName()));
        assertNotNull(generated);
        assertEquals(0, generated.count());
    }

    /**
     * Tests that the asserter detects issues.
     */
    @Test
    void testAbsentRoot() {
        final GenerationException e = assertThrows(GenerationException.class,
                () -> generateUsingDecriptor(AbsentRoot.getDescriptor()));
        assertThat(e)
                .hasMessageContaining(AbsentRoot.getDescriptor().getFullName())
                .hasMessageContaining(AbsentRootStereotype.getDescriptor().getFullName());
    }

    /**
     * Tests that the asserter detects issues.
     */
    @Test
    void testAbsentRootProto() {
        final GenerationException e = assertThrows(GenerationException.class,
                () -> generateUsingProto(AbsentRoot.getDescriptor().getFullName()));
        assertThat(e)
                .hasMessageContaining(AbsentRoot.getDescriptor().getFullName())
                .hasMessageContaining(AbsentRootStereotype.getDescriptor().getFullName());
    }

    /**
     * Tests that the asserter detects nested issues.
     */
    @Test
    void testAbsentNested() {
        final GenerationException e = assertThrows(GenerationException.class,
                () -> generateUsingDecriptor(AbsentNested.getDescriptor()));
        assertThat(e)
                .hasMessageContaining(AbsentNested.getDescriptor().getFullName())
                .hasMessageContaining(AbsentNestedStereotype.getDescriptor().getFullName());
    }

    /**
     * Tests that the asserter detects nested issues.
     */
    @Test
    void testAbsentNestedProto() {
        final GenerationException e = assertThrows(GenerationException.class,
                () -> generateUsingProto(AbsentNested.getDescriptor().getFullName()));
        assertThat(e)
                .hasMessageContaining(AbsentNested.getDescriptor().getFullName())
                .hasMessageContaining(AbsentNestedStereotype.getDescriptor().getFullName());
    }

    /**
     * Creates a {@link CodeGeneratorRequest} using the given descriptor as dynamic element and invokes
     * the generator with it.
     *
     * @param descriptor The file descriptor to use.
     * @return The stream of generated files.
     */
    private Stream<File> generateUsingDecriptor(final FileDescriptor descriptor) {
        final CodeGeneratorRequest request = CodeGeneratorRequest.newBuilder()
                .addFileToGenerate(descriptor.getFullName())
                .addProtoFile(DescriptorProtos.getDescriptor().toProto())
                .addProtoFile(InterfaceVersionProto.getDescriptor().toProto())
                .addProtoFile(DocumentationProto.getDescriptor().toProto())
                .addProtoFile(descriptor.toProto())
                .build();
        return this.asserter.generate(request);
    }

    /**
     * Creates a {@link CodeGeneratorRequest} using only the given file name to generate and invokes the
     * generator with it.
     *
     * @param file The file name to generate.
     * @return The stream of generated files.
     */
    private Stream<File> generateUsingProto(final String file) {
        final CodeGeneratorRequest request = assertDoesNotThrow(() -> loadResource("/stereotype.protobin"))
                .toBuilder()
                .clearFileToGenerate()
                .addFileToGenerate(file)
                .build();
        return this.asserter.generate(request);
    }

}
