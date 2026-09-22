package de.heuboe.vmis2.jprotoc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;

import de.heuboe.vmis2.jprotoc.api.JProtocHelper;

/**
 * A utility class for jprotoc generator tests.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public final class GeneratorTestUtils {

    /**
     * Creates an {@link InputStream} from a file with the given path.
     *
     * @param path The path to the file.
     * @return The input stream for the contents of the file.
     * @throws IOException If the file does not exist, is a directory rather than a regular file, or for
     *         some other reason cannot be opened for reading.
     */
    public static InputStream fileStream(final String path) throws IOException {
        return new FileInputStream(path);
    }

    /**
     * Creates an {@link InputStream} from a resource with the given path.
     *
     * @param path The path to the resource.
     * @return The input stream for the contents of the file.
     * @throws IOException If the resource does not exist.
     */
    public static InputStream resourceStream(final String path) throws IOException {
        final InputStream stream = GeneratorTestUtils.class.getResourceAsStream(path);
        if (stream == null) {
            throw new IOException("Resource " + path + " does not exist!");
        }
        return stream;
    }

    /**
     * Loads a {@link CodeGeneratorRequest} from the given file path.
     *
     * @param path The path to the file.
     * @return The loaded code generator request.
     * @throws IOException If the file does not exist, is a directory rather than a regular file, or for
     *         some other reason cannot be opened for reading.
     */
    public static CodeGeneratorRequest loadFile(final String path) throws IOException {
        try (InputStream stream = fileStream(path)) {
            final ExtensionRegistry extensionRegistry = JProtocHelper.defaultExtensionRegistry();
            return CodeGeneratorRequest.parseFrom(stream, extensionRegistry);
        }
    }

    /**
     * Loads a {@link CodeGeneratorRequest} from the given resource path.
     *
     * @param path The path to the resource.
     * @return The loaded code generator request.
     * @throws IOException If the resource does not exist.
     */
    public static CodeGeneratorRequest loadResource(final String path) throws IOException {
        try (InputStream stream = resourceStream(path)) {
            final ExtensionRegistry extensionRegistry = JProtocHelper.defaultExtensionRegistry();
            return CodeGeneratorRequest.parseFrom(stream, extensionRegistry);
        }
    }

    private GeneratorTestUtils() {}

}
