package de.heuboe.vmis2.jprotoc.documentation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.GenericDescriptor;
import com.google.protobuf.Extension;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

import de.heuboe.protobuf.DocumentationProto;
import de.heuboe.protobuf.ExternalReference;
import de.heuboe.vmis2.jprotoc.api.GenerationException;
import de.heuboe.vmis2.jprotoc.api.JProtocHelper;
import de.heuboe.vmis2.jprotoc.api.JProtocPlugin;

/**
 * A generator that asserts that the external references in the proto files are valid.
 *
 * <p>
 * <b>Note:</b> You can skip the validation of a single type/field reference by prefixing it with a
 * tilde (<code>~</code>).
 * </p>
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public class ProtoExternalReferenceAsserter implements JProtocPlugin {

    /**
     * The prefix used to skip the reference validation.
     */
    private static final String PREFIX_SKIP_VALIDATION = "~";

    @Override
    public Stream<File> generate(final CodeGeneratorRequest request) {
        Map<String, FileDescriptor> fileDescriptors;
        try {
            fileDescriptors = JProtocHelper.toFileDescriptor(request);
        } catch (final DescriptorValidationException e) {
            throw new GenerationException("Failed to parse descriptors", e);
        }
        final Map<String, GenericDescriptor> typeMap = new HashMap<>();
        for (final FileDescriptor fileDescriptor : fileDescriptors.values()) {
            streamTypes(fileDescriptor).forEach(e -> typeMap.put(e.getFullName(), e));
        }

        // Only validate files contained in the current project to avoid issues due to #13
        final Set<String> filesToGenerate = new HashSet<>(request.getFileToGenerateList());
        typeMap.values().stream()
                .filter(Descriptor.class::isInstance)
                .map(Descriptor.class::cast)
                .filter(d -> filesToGenerate.contains(d.getFile().getName()))
                .forEach(type -> verify(type, typeMap));

        for (final FileDescriptor fileDescriptor : fileDescriptors.values()) {
            verifyFields(fileDescriptor.getExtensions(), typeMap);
        }

        return Stream.empty();
    }

    /**
     * Verifies that the given descriptor contains only valid references.
     *
     * @param type The descriptor to validate.
     * @param typeMap The known types that can be referenced.
     */
    public void verify(final Descriptor type, final Map<String, ? extends GenericDescriptor> typeMap) {
        if (type.getOptions().hasExtension(DocumentationProto.compositeTypeRef)) {
            final String compositeRef = type.getOptions().getExtension(DocumentationProto.compositeTypeRef);
            if (!compositeRef.startsWith(PREFIX_SKIP_VALIDATION)) {
                final GenericDescriptor target = typeMap.get(compositeRef);
                if (target == null) {
                    throw new GenerationException(
                            "Could not find target reference for: " + type.getFullName() + " -> " + compositeRef);
                }
                verifyComposite(type, target);
            }
            return;
        }
        verifyFields(type.getFields(), typeMap);
    }

    /**
     * Verifies that the given extension contains only valid references.
     *
     * @param extension The extension to validate.
     * @param typeMap The known types that can be referenced.
     */
    void verify(final Extension<?, ?> extension, final Map<String, ? extends GenericDescriptor> typeMap) {
        verifyField(extension.getDescriptor(), typeMap);
    }

    /**
     * Verifies that all the given fields contains only valid references.
     *
     * @param fields The fields to validate.
     * @param typeMap The known types that can be referenced.
     */
    private void verifyFields(final Iterable<FieldDescriptor> fields,
            final Map<String, ? extends GenericDescriptor> typeMap) {
        for (final FieldDescriptor field : fields) {
            verifyField(field, typeMap);
        }
    }

    /**
     * Verifies that the given field contains only valid references.
     *
     * @param field The field to validate.
     * @param typeMap The known types that can be referenced.
     */
    private void verifyField(final FieldDescriptor field, final Map<String, ? extends GenericDescriptor> typeMap) {
        if (field.getOptions().hasExtension(DocumentationProto.extRef)) {
            final ExternalReference extRef = field.getOptions().getExtension(DocumentationProto.extRef);
            verifyExtRef(field, extRef, typeMap);
        }
    }

    /**
     * Verifies that the given reference type contain only valid references to the specified target.
     *
     * @param referenceType The type that is the composite reference to the target.
     * @param target The target for the references.
     */
    private void verifyComposite(final Descriptor referenceType, final GenericDescriptor target) {
        for (final FieldDescriptor field : referenceType.getFields()) {
            if (field.getOptions().hasExtension(DocumentationProto.extRef)) {
                final ExternalReference extRef = field.getOptions().getExtension(DocumentationProto.extRef);
                verifyFieldRef(field, extRef, target);
            }
        }
    }

    /**
     * Verifies that the given field with its external reference is valid.
     *
     * @param referenceField The field that is a reference.
     * @param extRef The reference details obtained from that field.
     * @param typeMap The known types that can be referenced.
     */
    private void verifyExtRef(final FieldDescriptor referenceField, final ExternalReference extRef,
            final Map<String, ? extends GenericDescriptor> typeMap) {
        final String targetTypeName = extRef.getType();
        if (targetTypeName == null || targetTypeName.isEmpty()) {
            throw new GenerationException(referenceField.getFullName()
                    + " specifies an external reference, but it does not specify a target type.");
        } else if (targetTypeName.startsWith(PREFIX_SKIP_VALIDATION)) {
            // Skip verification
        } else {
            final GenericDescriptor target = typeMap.get(targetTypeName);
            if (target == null) {
                throw new GenerationException("Failed to find referenced target type " + targetTypeName
                        + " as specified by " + referenceField.getFullName());
            }
            verifyFieldRef(referenceField, extRef, target);
        }
    }

    /**
     * Verifies that the given field with its reference is valid for the given target.
     *
     * @param referenceField The field that is a reference.
     * @param extRef The reference details obtained from that field.
     * @param target The target for the reference.
     */
    private void verifyFieldRef(final FieldDescriptor referenceField, final ExternalReference extRef,
            final GenericDescriptor target) {
        final String targetFieldName = extRef.getField();
        if (targetFieldName == null || targetFieldName.isEmpty()) {
            if (target instanceof Descriptor) {
                throw new GenerationException(referenceField.getFullName()
                        + " specifies an external reference, but it does not specify a target field.");
            } else {
                // Skip verification
            }
        } else if (targetFieldName.startsWith(PREFIX_SKIP_VALIDATION)) {
            // Skip verification
        } else if (target instanceof Descriptor) {
            final FieldDescriptor targetField = ((Descriptor) target).findFieldByName(targetFieldName);
            if (targetField == null) {
                throw new GenerationException("Failed to find referenced target field " + target.getFullName() + "."
                        + targetFieldName + " as specified by " + referenceField.getFullName());
            }
        } else if (target instanceof EnumDescriptor) {
            final ImmutableSet<String> allowed = ImmutableSet.of("name", "number");
            if (!allowed.contains(targetFieldName)) {
                throw new GenerationException("Failed to find referenced target field " + target.getFullName() + "."
                        + targetFieldName + " as specified by " + referenceField.getFullName()
                        + " - Supported: " + allowed);
            }
        }
    }

    /**
     * Streams all defined type descriptors in the given file recursively.
     *
     * @param file The file to stream the type descriptors from.
     * @return A stream with all defined type descriptors.
     */
    private Stream<GenericDescriptor> streamTypes(final FileDescriptor file) {
        return Stream.concat(
                file.getEnumTypes().stream(),
                file.getMessageTypes().stream().flatMap(this::streamTypes));
    }

    /**
     * Streams all defined type descriptors in the given message recursively (including itself).
     *
     * @param descriptor The descriptor to stream the type descriptors from.
     * @return A stream with all defined type descriptors.
     */
    private Stream<GenericDescriptor> streamTypes(final Descriptor descriptor) {
        return Stream.concat(
                Stream.of(descriptor),
                Stream.concat(
                        descriptor.getEnumTypes().stream(),
                        descriptor.getNestedTypes().stream().flatMap(this::streamTypes)));
    }

}
