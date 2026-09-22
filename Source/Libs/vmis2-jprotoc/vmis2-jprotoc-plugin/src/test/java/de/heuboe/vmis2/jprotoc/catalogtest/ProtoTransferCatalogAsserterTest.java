package de.heuboe.vmis2.jprotoc.catalogtest;

import static de.heuboe.vmis2.jprotoc.GeneratorTestUtils.loadResource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

import de.heuboe.protobuf.DocumentationProto;
import de.heuboe.vmis2.jprotoc.api.GenerationException;
import de.heuboe.vmis2.jprotoc.test.DocumentationTestProto;

/**
 * Tests for {@link ProtoTransferCatalogAsserter}.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
class ProtoTransferCatalogAsserterTest {

    private final ProtoTransferCatalogAsserter mercifulAsserter = new ProtoTransferCatalogAsserter("99-TEST", true);
    private final ProtoTransferCatalogAsserter strictAsserter = new ProtoTransferCatalogAsserter("99-TEST", false);

    @Test
    void test() throws IOException {
        final Stream<File> generated =
                this.mercifulAsserter.generate(loadResource("/meinTestInput.protoB"));
        assertNotNull(generated);
        assertEquals(0, generated.count());
    }

    @Test
    void testMissing() throws IOException {
        // The version will always be absent (hopefully)
        final CodeGeneratorRequest request = CodeGeneratorRequest.newBuilder()
                .addFileToGenerate(DocumentationTestProto.getDescriptor().getFullName())
                .addProtoFile(DocumentationTestProto.getDescriptor().toProto())
                .build();

        assertThrows(GenerationException.class, () -> this.strictAsserter.generate(request));

        final Stream<File> generated = this.mercifulAsserter.generate(request);
        assertNotNull(generated);
        assertEquals(0, generated.count());
    }

    @Test
    void testWrong() throws IOException {
        // The version will always be different
        final CodeGeneratorRequest request = CodeGeneratorRequest.newBuilder()
                .addFileToGenerate(DocumentationProto.getDescriptor().getFullName())
                .addProtoFile(DocumentationProto.getDescriptor().toProto())
                .build();

        assertThrows(GenerationException.class, () -> this.strictAsserter.generate(request));
        assertThrows(GenerationException.class, () -> this.mercifulAsserter.generate(request));
    }

}
