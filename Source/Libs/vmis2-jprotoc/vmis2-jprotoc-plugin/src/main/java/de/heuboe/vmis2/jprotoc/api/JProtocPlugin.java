package de.heuboe.vmis2.jprotoc.api;

import java.util.stream.Stream;

import com.google.protobuf.compiler.PluginProtos;

/**
 * A java based protoc plugin that can be used to generate code using the given code generator
 * request.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@FunctionalInterface
public interface JProtocPlugin {

    /**
     * Creates a stream of files that should be generated for the given request.
     *
     * @param request The code generator request that this plugin should process.
     * @return A stream that contains the files to be generated, can be empty. If the contents of the
     *         stream are generated lazily, it might throw a {@link GenerationException} at any time.
     * @throws GenerationException If something went wrong during the generation process/preparations.
     */
    Stream<PluginProtos.CodeGeneratorResponse.File> generate(PluginProtos.CodeGeneratorRequest request)
            throws GenerationException;

}
