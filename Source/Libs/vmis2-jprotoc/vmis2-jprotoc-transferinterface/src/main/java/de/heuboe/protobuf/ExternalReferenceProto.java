package de.heuboe.protobuf;

import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.DescriptorProtos.MessageOptions;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;

/**
 * Helper class used to ease the burden of the renaming to {@link DocumentationProto}.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 * @deprecated Use DocumentationProto instead!
 */
@Deprecated // For removal
public final class ExternalReferenceProto {

    /**
     * <pre>
     * Hint for a message that it is a reference to different message
     * (This is usually used if the target type uses a composite key/ref)
     * </pre>
     */
    public static final GeneratedExtension<MessageOptions, String> compositeTypeRef =
            DocumentationProto.compositeTypeRef;
    /**
     * <pre>
     * Describes of the reference target.
     * </pre>
     */
    public static final GeneratedExtension<FieldOptions, ExternalReference> extRef =
            DocumentationProto.extRef;

    /**
     * Gets the {@link DocumentationProto} {@link FileDescriptor}.
     * 
     * @return The file descriptor.
     * @see DocumentationProto#getDescriptor()
     */
    public static FileDescriptor getDescriptor() {
        return DocumentationProto.getDescriptor();
    }

    private ExternalReferenceProto() {}

}
