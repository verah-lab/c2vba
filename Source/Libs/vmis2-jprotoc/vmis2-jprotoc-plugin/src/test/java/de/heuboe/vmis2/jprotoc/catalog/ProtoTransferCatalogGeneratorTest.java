package de.heuboe.vmis2.jprotoc.catalog;

import static de.heuboe.vmis2.jprotoc.GeneratorTestUtils.loadResource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

/**
 * Tests for the {@link ProtoTransferCatalogGenerator}.
 */
class ProtoTransferCatalogGeneratorTest {

    @Test
    void test() throws IOException {
        final Stream<File> generated =
                new ProtoTransferCatalogGenerator().generate(loadResource("/meinTestInput.protoB"));
        assertNotNull(generated);
        assertEquals(4, generated.count());
    }

}
