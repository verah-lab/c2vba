package de.heuboe.vmis2.jprotoc.catalog;

import static de.heuboe.vmis2.jprotoc.Utils.extractOuterClassname;
import static de.heuboe.vmis2.jprotoc.Utils.extractPackageName;
import static de.heuboe.vmis2.jprotoc.Utils.protoToPojoPackage;
import static de.heuboe.vmis2.jprotoc.Utils.toCatalogName;
import static de.heuboe.vmis2.jprotoc.Utils.toPath;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.compiler.PluginProtos;

import de.heuboe.vmis2.jprotoc.PluginVersion;
import de.heuboe.vmis2.jprotoc.api.JProtocPlugin;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

/**
 * This generator generates the {@code ProtoTransferCatalog}s for each requested proto file in the
 * request.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public class ProtoTransferCatalogGenerator implements JProtocPlugin {

    private final Template catalogTemplate;

    /**
     * Creates a new ProtoTransferCatalogGenerator.
     */
    public ProtoTransferCatalogGenerator() {
        final Configuration cfg = new Configuration(Configuration.VERSION_2_3_27);
        cfg.setClassForTemplateLoading(ProtoTransferCatalogGenerator.class, "/templates/catalog/");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        try {
            this.catalogTemplate = cfg.getTemplate("ProtoTransferCatalog.ftl");
        } catch (final IOException e) {
            throw new IllegalArgumentException("Failed to load ProtoTransferCatalog template", e);
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
        final String outerClassName = extractOuterClassname(protoFile);
        final String catalogName = toCatalogName(outerClassName);

        final Writer out = new StringWriter();
        final Map<String, Object> dataModel = new HashMap<>();
        // Metadata
        dataModel.put("protoFile", protoFileName);
        dataModel.put("date", OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        dataModel.put("plugin", new PluginVersion());

        dataModel.put("javaPackage", javaPackageName);
        dataModel.put("catalogClass", catalogName);
        dataModel.put("protoPackageName", protoPackageName);
        dataModel.put("outerClassName", outerClassName);
        dataModel.put("javaClasses", streamAllTypeNames(protoFile)
                .map("P"::concat).iterator());

        try {
            this.catalogTemplate.process(dataModel, out);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Failed to generate catalog template", e);
        }
        return PluginProtos.CodeGeneratorResponse.File
                .newBuilder()
                .setName(toPath(javaPackageName, catalogName))
                .setContent(out.toString())
                .build();
    }

    /**
     * Streams all qualified (proto) type names (without the package-prefix) contained in the proto
     * file.
     *
     * @param protoFile The proto file to scan through.
     * @return A stream containing all qualified type names in that file.
     */
    private Stream<String> streamAllTypeNames(final DescriptorProtos.FileDescriptorProto protoFile) {
        return Stream.concat(
                protoFile.getEnumTypeList().stream()
                        .map(EnumDescriptorProto::getName),
                protoFile.getMessageTypeList().stream()
                        .flatMap(this::streamTypeNamesRecursive));
    }

    /**
     * Streams its own name and all qualified (proto) type names nested in the proto message.
     *
     * @param protoMessage The proto message to scan through.
     * @return A stream containing its own name and all qualified type names in that message.
     */
    private Stream<String> streamTypeNamesRecursive(final DescriptorProtos.DescriptorProto protoMessage) {
        final String name = protoMessage.getName();
        final String subPrefix = name + '.';
        return Stream.concat(
                Stream.of(name),
                Stream.concat(
                        protoMessage.getEnumTypeList().stream()
                                .map(EnumDescriptorProto::getName),
                        protoMessage.getNestedTypeList().stream()
                                .flatMap(this::streamTypeNamesRecursive))
                        .map(subPrefix::concat));
    }

}
