package de.heuboe.vmis2.jprotoc.catalogtest;

import static de.heuboe.vmis2.jprotoc.Utils.extractOuterClassname;
import static de.heuboe.vmis2.jprotoc.Utils.extractPackageName;
import static de.heuboe.vmis2.jprotoc.Utils.protoToPojoPackage;
import static de.heuboe.vmis2.jprotoc.Utils.toCatalogName;
import static de.heuboe.vmis2.jprotoc.Utils.toPath;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

import de.heuboe.vmis2.jprotoc.PluginVersion;
import de.heuboe.vmis2.jprotoc.api.FatalGenerationException;
import de.heuboe.vmis2.jprotoc.api.JProtocHelper;
import de.heuboe.vmis2.jprotoc.api.JProtocPlugin;
import de.heuboe.vmis2.jprotoc.catalog.ProtoTransferCatalogGenerator;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

/**
 * This generator generates the tests for {@code ProtoTransferCatalog}s for each requested proto
 * file in the request.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public class ProtoTransferCatalogTestGenerator implements JProtocPlugin {

    /**
     * The main method called from maven-protobuf-plugin.
     *
     * @param args An array containing a single element that specifies the version that should be used
     *        in the generated tests.
     * @throws FatalGenerationException If something went wrong during the generation.
     */
    public static void main(final String... args) throws FatalGenerationException {
        try {
            if (args.length == 0 || args[0] == null || args[0].isEmpty()) {
                throw new FatalGenerationException("Missing version parameter");
            }
            if (args.length > 1) {
                throw new FatalGenerationException("Too many version parameters");
            }
            JProtocHelper.generate(new ProtoTransferCatalogTestGenerator(args[0]));
        } catch (final Throwable e) { // NOSONAR
            // This ugly exception handling is required otherwise protoc will ignore the exception.
            e.printStackTrace(); // NOSONAR
            System.exit(1);
        }
    }

    private final Template catalogTemplate;
    private final String version;

    /**
     * Creates a new ProtoTransferCatalogTestGenerator.
     *
     * @param version The version to use in the generated tests.
     */
    public ProtoTransferCatalogTestGenerator(final String version) {
        this.version = requireNonNull(version, "version");

        final Configuration cfg = new Configuration(Configuration.VERSION_2_3_27);
        cfg.setClassForTemplateLoading(ProtoTransferCatalogGenerator.class, "/templates/catalogtest/");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        try {
            this.catalogTemplate = cfg.getTemplate("ProtoTransferCatalogTest.ftl");
        } catch (final IOException e) {
            throw new IllegalArgumentException("Failed to load ProtoTransferCatalogTest template", e);
        }
    }

    @Override
    public Stream<PluginProtos.CodeGeneratorResponse.File> generate(final PluginProtos.CodeGeneratorRequest request) {
        return request.getProtoFileList().stream()
                .filter(protoFile -> request.getFileToGenerateList().contains(protoFile.getName()))
                .map(this::buildCatalogFile);
    }

    /**
     * Generates a {@code ProtoTransferCatalog} file that was generated for the given proto file.
     *
     * @param protoFile The proto file to
     * @return The generated catalog file.
     */
    public PluginProtos.CodeGeneratorResponse.File buildCatalogFile(
            final DescriptorProtos.FileDescriptorProto protoFile) {
        final String protoFileName = protoFile.getName();
        final String protoPackageName = extractPackageName(protoFile);
        final String javaPackageName = protoToPojoPackage(protoPackageName);
        final String catalogName = toCatalogName(extractOuterClassname(protoFile));

        final Writer out = new StringWriter();
        final Map<String, Object> dataModel = new HashMap<>();
        // Metadata
        dataModel.put("protoFile", protoFileName);
        dataModel.put("date", OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        dataModel.put("plugin", new PluginVersion());

        dataModel.put("javaPackage", javaPackageName);
        dataModel.put("catalogClass", catalogName);
        dataModel.put("version", this.version);

        try {
            this.catalogTemplate.process(dataModel, out);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Failed to generate catalog test template", e);
        }
        return PluginProtos.CodeGeneratorResponse.File
                .newBuilder()
                .setName(toPath(javaPackageName, catalogName + "Test"))
                .setContent(out.toString())
                .build();
    }
}
