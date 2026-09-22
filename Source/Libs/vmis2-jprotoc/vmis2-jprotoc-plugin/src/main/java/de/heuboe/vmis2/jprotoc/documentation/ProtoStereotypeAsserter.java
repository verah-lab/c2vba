package de.heuboe.vmis2.jprotoc.documentation;

import java.util.Map;
import java.util.stream.Stream;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

import de.heuboe.protobuf.Stereotype;
import de.heuboe.vmis2.jprotoc.api.GenerationException;
import de.heuboe.vmis2.jprotoc.api.JProtocHelper;
import de.heuboe.vmis2.jprotoc.api.JProtocPlugin;
import de.heuboe.vmis2.jprotoc.utils.DocumentationUtils;

/**
 * This asserter ensures that all messages specify a proper stereotype.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public class ProtoStereotypeAsserter implements JProtocPlugin {

    @Override
    public Stream<File> generate(final CodeGeneratorRequest request) {
        Map<String, FileDescriptor> fileDescriptors;
        try {
            fileDescriptors = JProtocHelper.toFileDescriptor(request);
        } catch (final DescriptorValidationException e) {
            throw new GenerationException("Failed to parse descriptors", e);
        }

        request.getFileToGenerateList()
                .stream()
                .map(fileDescriptors::get)
                .flatMap(this::streamTypes)
                .forEach(this::verify);

        return Stream.empty();
    }

    private void verify(final Descriptor message) {
        if (DocumentationUtils.getStereotype(message) == Stereotype.UNKNOWN) {
            throw new GenerationException("Type " + message.getFullName()
                    + " in file " + message.getFile().getFullName()
                    + " does not specify a Stereotype");
        }
    }

    /**
     * Streams all defined type descriptors in the given file recursively.
     *
     * @param file The file to stream the type descriptors from.
     * @return A stream with all defined type descriptors.
     */
    private Stream<Descriptor> streamTypes(final FileDescriptor file) {
        return file.getMessageTypes().stream().flatMap(this::streamTypes);
    }

    /**
     * Streams all defined type descriptors in the given message recursively (including itself).
     *
     * @param descriptor The descriptor to stream the type descriptors from.
     * @return A stream with all defined type descriptors.
     */
    private Stream<Descriptor> streamTypes(final Descriptor descriptor) {
        return Stream.concat(
                Stream.of(descriptor),
                descriptor.getNestedTypes().stream().flatMap(this::streamTypes));
    }

}
