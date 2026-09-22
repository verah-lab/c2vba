package de.heuboe.vmis2.jprotoc.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

import de.heuboe.protobuf.DocumentationProto;
import de.heuboe.protobuf.InterfaceVersionProto;

/**
 * Helper class that should be invoked to execute the {@link JProtocPlugin}s.
 *
 * <p>
 * <b>Note:</b> The {@code protobuf-maven-plugin} assumes that the executed plugins read the
 * requests from StdIn and output the response to StdOut.
 * </p>
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public final class JProtocHelper {

    /**
     * Generates the files using the given {@link JProtocPlugin}s.
     *
     * @param plugins The jprotoc plugins used to generate the code.
     * @throws FatalGenerationException If the generation failed unrecoverably. Either print the
     *         stacktrace and exit the application with a non zero exit code or don't catch it and let
     *         the JVM handle it.
     */
    @SuppressWarnings("squid:S106") // Usage of System.out is required
    public static void generate(final JProtocPlugin... plugins) throws FatalGenerationException {
        generate(System.in, System.out, Arrays.asList(plugins));
    }

    /**
     * Generates the files using the given {@link JProtocPlugin}s.
     *
     * @param plugins The jprotoc plugins used to generate the code.
     * @throws FatalGenerationException If the generation failed unrecoverably. Either print the
     *         stacktrace and exit the application with a non zero exit code or don't catch it and let
     *         the JVM handle it.
     */
    @SuppressWarnings("squid:S106") // Usage of System.out is required
    public static void generate(final Iterable<JProtocPlugin> plugins) throws FatalGenerationException {
        generate(System.in, System.out, plugins);
    }

    /**
     * Generates the files using the given {@link JProtocPlugin}s using the input and output streams as
     * target. Useful for testing.
     *
     * @param input The input stream to read the request from.
     * @param output The output stream to write the response to.
     * @param plugins The jprotoc plugins used to generate the code.
     * @throws FatalGenerationException If the generation failed unrecoverably. Either print the
     *         stacktrace and exit the application with a non zero exit code or don't catch it and let
     *         the JVM handle it.
     */
    public static void generate(final InputStream input, final OutputStream output, final JProtocPlugin... plugins)
            throws FatalGenerationException {
        generate(input, output, Arrays.asList(plugins));
    }

    /**
     * Generates the files using the given {@link JProtocPlugin}s using the input and output streams as
     * target. Useful for testing.
     *
     * @param input The input stream to read the request from.
     * @param output The output stream to write the response to.
     * @param plugins The jprotoc plugins used to generate the code.
     * @throws FatalGenerationException If the generation failed unrecoverably. Either print the
     *         stacktrace and exit the application with a non zero exit code or don't catch it and let
     *         the JVM handle it.
     */
    public static void generate(final InputStream input,
            final OutputStream output, final Iterable<JProtocPlugin> plugins)
            throws FatalGenerationException {
        final ExtensionRegistry extensionRegistry = defaultExtensionRegistry();
        try {
            final CodeGeneratorRequest request = CodeGeneratorRequest.parseFrom(input, extensionRegistry);
            process(request, plugins).writeTo(output);
        } catch (final Exception e) {
            throw new FatalGenerationException("Unexpected error during jprotoc plugin run", e);
        }
    }

    /**
     * Processes the given {@link CodeGeneratorRequest} using the given {@link JProtocPlugin}s. Useful
     * for testing.
     *
     * @param request The request to process.
     * @param plugins The jprotoc plugins used to generate the code.
     * @return The CodeGeneratorResponse containing either the error message or the generated files.
     */
    public static CodeGeneratorResponse process(final CodeGeneratorRequest request,
            final JProtocPlugin... plugins) {
        return process(request, Arrays.asList(plugins));
    }

    /**
     * Processes the given {@link CodeGeneratorRequest} using the given {@link JProtocPlugin}s. Useful
     * for testing.
     *
     * @param request The request to process.
     * @param plugins The jprotoc plugins used to generate the code.
     * @return The CodeGeneratorResponse containing either the error message or the generated files.
     */
    @SuppressWarnings("squid:S1166") // We handle the exception by setting its message to the error response.
    public static CodeGeneratorResponse process(final CodeGeneratorRequest request,
            final Iterable<JProtocPlugin> plugins) {
        try {
            final List<File> generatedFiles = new ArrayList<>();
            for (final JProtocPlugin plugin : plugins) {
                plugin.generate(request).forEach(generatedFiles::add);
            }
            return CodeGeneratorResponse.newBuilder().addAllFile(generatedFiles).build();
        } catch (final RuntimeException e) {
            // Maybe use ExceptionUtils.getStackTrace(e) instead?
            return CodeGeneratorResponse.newBuilder().setError(e.getMessage()).build();
        }
    }

    /**
     * Loads the {@link FileDescriptor}s that are described in the {@link CodeGeneratorRequest} and
     * initializes them in the correct order.
     *
     * @param request The request with the {@link FileDescriptorProto}s to load.
     * @return A map that contains the loaded file descriptors with their full name.
     * @throws DescriptorValidationException If a {@link FileDescriptor} couldn't be created due to
     *         validation errors/duplicates/conflicts.
     */
    public static Map<String, Descriptors.FileDescriptor> toFileDescriptor(final CodeGeneratorRequest request)
            throws DescriptorValidationException {
        final Map<String, Descriptors.FileDescriptor> result = new LinkedHashMap<>();
        final List<FileDescriptorProto> remaining = new ArrayList<>(request.getProtoFileCount());
        final List<FileDescriptorProto> skipped = new ArrayList<>(request.getProtoFileList());
        while (!skipped.isEmpty()) {
            remaining.addAll(skipped);
            skipped.clear();
            boolean changed = false;

            for (final FileDescriptorProto fileDescriptorProto : remaining) {
                // Are all dependencies met?
                if (result.keySet().containsAll(fileDescriptorProto.getDependencyList())) {
                    changed = true;
                    final FileDescriptor fileDescriptor = FileDescriptor.buildFrom(fileDescriptorProto,
                            result.values().toArray(new FileDescriptor[0]));
                    result.put(fileDescriptor.getFullName(), fileDescriptor);
                } else {
                    skipped.add(fileDescriptorProto);
                }
            }

            if (!changed) {
                throw new IllegalArgumentException("Unable to build file descriptors: "
                        + skipped.stream().map(FileDescriptorProto::getName).collect(Collectors.joining(", ")));
            }
        }

        return result;
    }

    /**
     * Creates a new {@link ExtensionRegistry} with the default extensions from the transfer interface
     * library.
     *
     * @return A new extension registry with the default extension set.
     */
    public static ExtensionRegistry defaultExtensionRegistry() {
        final ExtensionRegistry registry = ExtensionRegistry.newInstance();
        InterfaceVersionProto.registerAllExtensions(registry);
        DocumentationProto.registerAllExtensions(registry);
        return registry;
    }

    private JProtocHelper() {}

}
