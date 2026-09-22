package de.heuboe.vmis2.jprotoc.utils;

import static de.heuboe.protobuf.DocumentationProto.compositeTypeRef;
import static de.heuboe.protobuf.DocumentationProto.extRef;
import static de.heuboe.protobuf.DocumentationProto.required;
import static de.heuboe.protobuf.DocumentationProto.stereotype;
import static java.util.Objects.requireNonNull;

import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.DescriptorProtos.MessageOptions;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;

import de.heuboe.protobuf.DocumentationProto;
import de.heuboe.protobuf.ExternalReference;
import de.heuboe.protobuf.Stereotype;

/**
 * Utility methods related to {@link DocumentationProto}.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public final class DocumentationUtils {

    /**
     * Gets the name type that the given type is a reference for.
     *
     * <p>
     * <b>Note:</b> References that are prefixed with {@code ~} aren't actual types and are only used
     * for documentation purposes.
     * </p>
     *
     * @param descriptor The descriptor to get the information from.
     * @return The name of the referenced type or null.
     */
    public static String getCompositeTypeRef(final Descriptor descriptor) {
        final MessageOptions options = requireNonNull(descriptor, "descriptor").getOptions();
        if (options.hasExtension(compositeTypeRef)) {
            return options.getExtension(compositeTypeRef);
        } else {
            return null;
        }
    }

    /**
     * Gets the name of the type and field that the given field references.
     *
     * <p>
     * <b>Note:</b> References that are prefixed with {@code ~} aren't actual types and are only used
     * for documentation purposes.
     * </p>
     *
     * @param descriptor The descriptor to get the information from.
     * @return The reference information or null.
     */
    public static ExternalReference getExternalReference(final FieldDescriptor descriptor) {
        final FieldOptions options = requireNonNull(descriptor, "descriptor").getOptions();
        if (options.hasExtension(extRef)) {
            return options.getExtension(extRef);
        } else {
            return null;
        }
    }

    /**
     * Gets the stereotype of the given message.
     *
     * @param descriptor The descriptor to get the information from.
     * @return The stereotype information or UNKNOWN.
     */
    public static Stereotype getStereotype(final Descriptor descriptor) {
        final MessageOptions options = requireNonNull(descriptor, "descriptor").getOptions();
        if (options.hasExtension(stereotype)) {
            return options.getExtension(stereotype);
        } else {
            return Stereotype.UNKNOWN;
        }
    }

    /**
     * Gets whether the given field is marked as required.
     *
     * @param descriptor The descriptor to get the information from.
     * @return False, if the given field is marked as not-required. True otherwise.
     */
    public static boolean isRequired(final FieldDescriptor descriptor) {
        final FieldOptions options = requireNonNull(descriptor, "descriptor").getOptions();
        if (options.hasExtension(required)) {
            return options.getExtension(required);
        } else {
            return true;
        }
    }

    private DocumentationUtils() {}

}
