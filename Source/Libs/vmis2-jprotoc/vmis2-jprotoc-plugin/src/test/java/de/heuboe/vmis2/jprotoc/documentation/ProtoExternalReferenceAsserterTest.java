package de.heuboe.vmis2.jprotoc.documentation;

import static de.heuboe.vmis2.jprotoc.GeneratorTestUtils.loadResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.GenericDescriptor;
import com.google.protobuf.Extension;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

import de.heuboe.protobuf.DocumentationProto;
import de.heuboe.protobuf.ExternalReference;
import de.heuboe.protobuf.InterfaceVersionProto;
import de.heuboe.vmis2.jprotoc.api.GenerationException;
import de.heuboe.vmis2.jprotoc.test.BrokenCompositeReference;
import de.heuboe.vmis2.jprotoc.test.BrokenExtRefEmpty;
import de.heuboe.vmis2.jprotoc.test.BrokenExtRefField;
import de.heuboe.vmis2.jprotoc.test.BrokenExtRefFieldAbsent;
import de.heuboe.vmis2.jprotoc.test.BrokenExtRefFieldEmpty;
import de.heuboe.vmis2.jprotoc.test.BrokenExtRefType;
import de.heuboe.vmis2.jprotoc.test.BrokenExtRefTypeAbsent;
import de.heuboe.vmis2.jprotoc.test.BrokenExtRefTypeEmpty;
import de.heuboe.vmis2.jprotoc.test.IgnoredBrokenCompositeReference;
import de.heuboe.vmis2.jprotoc.test.IgnoredBrokenExtRefField;
import de.heuboe.vmis2.jprotoc.test.IgnoredBrokenExtRefFieldEmpty;
import de.heuboe.vmis2.jprotoc.test.IgnoredBrokenExtRefType;
import de.heuboe.vmis2.jprotoc.test.IgnoredBrokenExtRefTypeEmpty;
import de.heuboe.vmis2.jprotoc.test.MessgeWithBrokenEnumConstantAssistance;
import de.heuboe.vmis2.jprotoc.test.MessgeWithEnumConstantAssistance;
import de.heuboe.vmis2.jprotoc.test.RefMessage;
import de.heuboe.vmis2.jprotoc.test.TypeConstants;

/**
 * Tests for the {@link ProtoExternalReferenceAsserter}.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
class ProtoExternalReferenceAsserterTest {

    private final ProtoExternalReferenceAsserter asserter = new ProtoExternalReferenceAsserter();
    private final Map<String, GenericDescriptor> typeMap = ImmutableMap.<String, GenericDescriptor>builder()
            .put(DescriptorProto.getDescriptor().getFullName(), DescriptorProto.getDescriptor())
            .put(FieldDescriptorProto.getDescriptor().getFullName(), FieldDescriptorProto.getDescriptor())
            .put(RefMessage.getDescriptor().getFullName(), RefMessage.getDescriptor())
            .put(TypeConstants.getDescriptor().getFullName(), TypeConstants.getDescriptor())
            .build();

    /**
     * Tests that the asserter doesn't actually generate files.
     *
     * @throws IOException Shouldn't happen.
     */
    @Test
    void testGeneration() throws IOException {
        final Stream<File> generated = this.asserter.generate(loadResource("/ReferenceTest.protobin"));
        assertNotNull(generated);
        assertEquals(0, generated.count());
    }

    /**
     * Tests that the asserter detects issues.
     */
    @Test
    void testGenerationWithIssues() {
        assertThrows(GenerationException.class,
                () -> this.asserter.generate(loadResource("/ReferenceTestBroken.protobin")));
    }

    /**
     * Tests that verify method passes valid entries and fails with broken ones.
     */
    @Test
    void testVerify() {
        // Verify working ones
        verify(DocumentationProto.compositeTypeRef);
        verify(DocumentationProto.extRef);
        verify(InterfaceVersionProto.interfaceVersion);
        verify(ExternalReference.getDescriptor());
        verify(RefMessage.getDescriptor());
        verify(RefMessage.Ref.getDescriptor());
        verify(MessgeWithEnumConstantAssistance.getDescriptor());

        // Verify broken ones
        final GenerationException e1 = assertThrowsGEx(BrokenCompositeReference.getDescriptor());
        assertThat(e1.getMessage())
                .contains("test.BrokenCompositeReference")
                .contains("test.DoesNotExist");
        final GenerationException e2 = assertThrowsGEx(BrokenExtRefEmpty.getDescriptor());
        assertThat(e2.getMessage())
                .contains("test.BrokenExtRefEmpty.ref");
        final GenerationException e3 = assertThrowsGEx(BrokenExtRefType.getDescriptor());
        assertThat(e3.getMessage())
                .contains("test.DoesNotExist");
        final GenerationException e4 = assertThrowsGEx(BrokenExtRefTypeEmpty.getDescriptor());
        assertThat(e4.getMessage())
                .contains("test.BrokenExtRefTypeEmpty.ref");
        final GenerationException e5 = assertThrowsGEx(BrokenExtRefTypeAbsent.getDescriptor());
        assertThat(e5.getMessage())
                .contains("test.BrokenExtRefTypeAbsent.ref");
        final GenerationException e6 = assertThrowsGEx(BrokenExtRefField.getDescriptor());
        assertThat(e6.getMessage())
                .contains("test.RefMessage.doesNotExist")
                .contains("test.BrokenExtRefField.ref");
        final GenerationException e7 = assertThrowsGEx(BrokenExtRefFieldEmpty.getDescriptor());
        assertThat(e7.getMessage())
                .contains("test.BrokenExtRefFieldEmpty.ref");
        final GenerationException e8 = assertThrowsGEx(BrokenExtRefFieldAbsent.getDescriptor());
        assertThat(e8.getMessage())
                .contains("test.BrokenExtRefFieldAbsent.ref");
        final GenerationException e9 = assertThrowsGEx(MessgeWithBrokenEnumConstantAssistance.getDescriptor());
        assertThat(e9.getMessage())
                .contains("test.TypeConstants.doesNotExist")
                .contains("test.MessgeWithBrokenEnumConstantAssistance.typeName");

        // Verify ignored broken ones
        verify(IgnoredBrokenCompositeReference.getDescriptor());
        verify(IgnoredBrokenExtRefType.getDescriptor());
        verify(IgnoredBrokenExtRefTypeEmpty.getDescriptor());
        verify(IgnoredBrokenExtRefField.getDescriptor());
        verify(IgnoredBrokenExtRefFieldEmpty.getDescriptor());
    }

    /**
     * Asserts that verifying the given descriptor throws an {@link GenerationException}.
     *
     * @param descriptor The descriptor to verify.
     * @return The thrown GenerationException.
     * @see Assertions#assertThrows(Class, Executable)
     */
    private GenerationException assertThrowsGEx(final Descriptor descriptor) {
        return assertThrows(GenerationException.class, () -> verify(descriptor));
    }

    /**
     * Helper method that invokes the actual method on the asserter. Used to increase the readability of
     * the test.
     *
     * @param descriptor The descriptor to verify.
     * @throws GenerationException If the verification failed.
     * @see ProtoExternalReferenceAsserter#verify(Descriptor, Map)
     */
    private void verify(final Descriptor descriptor) throws GenerationException {
        this.asserter.verify(descriptor, this.typeMap);
    }

    /**
     * Helper method that invokes the actual method on the asserter. Used to increase the readability of
     * the test.
     *
     * @param extension The extension to verify.
     * @throws GenerationException If the verification failed.
     * @see ProtoExternalReferenceAsserter#verify(Extension, Map)
     */
    private void verify(final Extension<?, ?> extension) throws GenerationException {
        this.asserter.verify(extension, this.typeMap);
    }

}
