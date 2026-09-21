package de.heuboe.vmis2.kafka.converter;

import static de.heuboe.vmis2.jprotoc.utils.Constants.HEADER_X_PROTOBUF_TYPE;
import static java.nio.charset.StandardCharsets.UTF_8;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

/**
 * Helper class with some shared functions related to (de-)serialization.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
public final class KafkaUtil {

    /**
     * Extracts the proto type from the header.
     *
     * @param headers The header to extract the details from.
     * @return The proto type or null if missing.
     */
    public static String getTypeFromHeaders(final Headers headers) {
        final Header typeHeader = headers.lastHeader(HEADER_X_PROTOBUF_TYPE);
        final byte[] typeBytes = typeHeader == null ? null : typeHeader.value();
        return typeBytes == null ? null : new String(typeBytes, UTF_8);
    }

    private KafkaUtil() {}

}
