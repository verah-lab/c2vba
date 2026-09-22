package de.heuboe.vmis2.jprotoc.catalogtest;

import static de.heuboe.vmis2.jprotoc.Utils.extractInterfaceVersion;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.removeEnd;

import java.util.stream.Stream;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

import de.heuboe.vmis2.jprotoc.api.GenerationException;
import de.heuboe.vmis2.jprotoc.api.JProtocPlugin;

/**
 * This generator asserts that the proto files contains the correct interface version.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public class ProtoTransferCatalogAsserter implements JProtocPlugin {

    private static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";
    private final String version;
    private final String sanitizedVersion;
    private final boolean ignoreAbsentVersion;

    /**
     * Creates a new ProtoTransferCatalogTestGenerator.
     *
     * @param version The version to use in the generated tests.
     * @param ignoreAbsentVersion Whether the assertion should pass if the proto file does not specify
     *        an interfaceVersion.
     */
    public ProtoTransferCatalogAsserter(final String version, final boolean ignoreAbsentVersion) {
        this.version = requireNonNull(version, "version");
        this.sanitizedVersion = sanitize(version);
        this.ignoreAbsentVersion = ignoreAbsentVersion;
    }

    /**
     * Removes the snapshot suffix from the given version.
     *
     * @param version The version to sanitize.
     * @return The version without the snapshot suffix.
     */
    private String sanitize(final String version) {
        return removeEnd(version, SNAPSHOT_SUFFIX);
    }

    @Override
    public Stream<PluginProtos.CodeGeneratorResponse.File> generate(final PluginProtos.CodeGeneratorRequest request) {
        request.getProtoFileList().stream()
                .filter(protoFile -> request.getFileToGenerateList().contains(protoFile.getName()))
                .forEach(this::testProtoFile);
        return Stream.empty();
    }

    /**
     * Tests the given proto file.
     *
     * @param protoFile The proto file to
     */
    public void testProtoFile(final DescriptorProtos.FileDescriptorProto protoFile) {
        final String interfaceVersion = extractInterfaceVersion(protoFile);
        if (interfaceVersion == null || interfaceVersion.isEmpty()) {
            if (this.ignoreAbsentVersion) {
                return;
            } else {
                throw new GenerationException(protoFile.getName() + " does not specify a interfaceVersion!\n"
                        + "Please add the following snippet to your proto file:\n"
                        + "import \"heuboe/protobuf/InterfaceVersion.proto\";\n" +
                        "\n" +
                        "option (.heuboe.protobuf.interface_version) = \"" + this.sanitizedVersion + "\";");
            }
        }
        if (this.version.endsWith(SNAPSHOT_SUFFIX)) {
            if (!this.sanitizedVersion.equals(sanitize(interfaceVersion))) {
                throw new GenerationException(protoFile.getName() + " specifies the wrong interfaceVersion!\n"
                        + "Expected: " + this.sanitizedVersion + ", but was: " + interfaceVersion);
            }
        } else {
            if (!this.version.equals(interfaceVersion)) {
                throw new GenerationException(protoFile.getName() + " specifies the wrong interfaceVersion!\n"
                        + "Expected: " + this.version + ", but was: " + interfaceVersion);
            }
        }

    }
}
