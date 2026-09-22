package de.heuboe.vmis2.jprotoc.helpers;

import static java.util.Objects.requireNonNull;

import com.google.protobuf.Any;
import com.google.protobuf.Message;

import de.heuboe.vmis2.jprotoc.transferinterface.HbProtoBufPojo;
import de.heuboe.vmis2.jprotoc.transferinterface.PojoTransfer;
import de.heuboe.vmis2.jprotoc.utils.ProtoPojoUtils;

/**
 * {@link Any} wrapper utility class for pojos generated from protobuf java classes.
 */
public class PAny {

    /**
     * Unpacks the raw bytes from the given {@link Any} and converts them to a pojo based on the type
     * url.
     *
     * @param any The instance to be unpacked and converted, or null.
     * @return The pojo instance that was wrapped in the given Any, or null if the given Any was null.
     */
    public static HbProtoBufPojo unpack(final Any any) {
        if (any == null) {
            return null;
        }
        try {
            final Class<? extends HbProtoBufPojo> pojoClass = getPojoClass(any.getTypeUrl());
            final PojoTransfer<?, ?> transfer = getPojoTransfer(pojoClass);
            return transfer.fromBytes(any.getValue().toByteArray());
        } catch (final Exception e) {
            throw new PAnyException("Failed to unpack Any with content: " + any.getTypeUrl(), e);
        }
    }

    /**
     * Converts and packs a Pojo to a protobuf {@link Any}
     *
     * @param o The pojo to convert and wrap in an Any, or null.
     * @return The Any that contains the converted pojo, or null if the given pojo was null.
     */
    public static Any pack(final HbProtoBufPojo o) {
        if (o == null) {
            return null;
        }
        try {
            final PojoTransfer<?, HbProtoBufPojo> transfer = getPojoTransfer(o.getClass());
            final Message proto = transfer.toProto(o);
            return Any.pack(proto);
        } catch (final Exception e) {
            // Should never happen
            throw new PAnyException("Could not pack Object of class: " + o.getClass().getName(), e);
        }
    }

    /**
     * Checks whether the given any contains an instance of the proto equivalent of the given class.
     *
     * @param any The any to check the contents of, or null.
     * @param clazz The pojo class to check for.
     * @return True, if the given any is not null and contains the proto equivalent of the given class.
     *         False, otherwise.
     */
    public static boolean is(final Any any, final Class<? extends HbProtoBufPojo> clazz) {
        requireNonNull(clazz, "clazz");
        if (any == null) {
            return false;
        } else {
            return any.is(getPojoTransfer(clazz).protoClass());
        }
    }

    /**
     * Gets the {@link PojoTransfer} instance belonging to the given pojo class.
     *
     * @param <T> The type of the pojo.
     * @param clazz The pojo class to get the PojoTransfer for.
     * @return The PojoTransfer belonging to the given class.
     */
    @SuppressWarnings("unchecked")
    private static <T extends HbProtoBufPojo> PojoTransfer<?, T> getPojoTransfer(final Class<? extends T> clazz) {
        return (PojoTransfer<?, T>) ProtoPojoUtils.toPojoTransfer(clazz);
    }

    /**
     * Searches for the pojo class that belongs to the given type url.
     *
     * @param url The url describing the protobuf type.
     * @return The pojo class associated with the given type url.
     */
    private static Class<? extends HbProtoBufPojo> getPojoClass(final String url) {
        final String protoTypeName = getTypeNameFromTypeUrl(url);
        if (protoTypeName.isEmpty()) {
            // Should never happen
            throw new PAnyException("Malformed PAny URL: " + url + " (Empty type part)");
        }

        try {
            return ProtoPojoUtils.toJavaClass(protoTypeName).asSubclass(HbProtoBufPojo.class);
        } catch (final IllegalArgumentException e) {
            throw new PAnyException("Unable to find Pojo-Class for url: " + url, e);
        }
    }

    /**
     * Extracts the fully-qualified proto name from the given type url.
     *
     * <p>
     * <b>Note:</b> This method is copied from {@link Any Any#getTypeNameFromTypeUrl(String)}.
     * </p>
     *
     * @param typeUrl The url describing the protobuf type.
     * @return The fully qualified proto name or and empty String.
     */
    private static String getTypeNameFromTypeUrl(final String typeUrl) {
        final int pos = typeUrl.lastIndexOf('/');
        return pos == -1 ? "" : typeUrl.substring(pos + 1);
    }

    private PAny() {}

    /**
     * Exception for conversion errors
     */
    public static class PAnyException extends RuntimeException {

        private static final long serialVersionUID = 3879861914431620357L;

        /**
         * Creates a new PAnyException with just a message.
         *
         * @param message The error message for the exception.
         */
        PAnyException(final String message) {
            super(message);
        }

        /**
         * Creates a new PAnyException with a message and a cause.
         *
         * @param message The error message for the exception.
         * @param cause The cause for the exception.
         */
        PAnyException(final String message, final Throwable cause) {
            super(message, cause);
        }

    }

}
