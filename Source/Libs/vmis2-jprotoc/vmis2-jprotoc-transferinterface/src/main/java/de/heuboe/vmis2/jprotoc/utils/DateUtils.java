package de.heuboe.vmis2.jprotoc.utils;

import java.time.Instant;

import com.google.protobuf.Timestamp;

/**
 * This utility class helps with converting between java time and protobuf time.
 */
public final class DateUtils {

    /**
     * Converts the given protobuf Timestamp to a java Instant.
     *
     * @param time The timestamp to convert.
     * @return The instant that is equivalent to the given timestamp or null if the input was null.
     */
    public static Instant toInstant(final Timestamp time) {
        if (time == null) {
            return null;
        } else {
            return Instant.ofEpochSecond(time.getSeconds(), time.getNanos());
        }
    }

    /**
     * Converts the java given Instant to a protobuf timestamp.
     *
     * @param instant the instant to convert.
     * @return The timestamp that is equivalent to the given instant or null if the input was null.
     */
    public static Timestamp fromInstantUtc(final Instant instant) {
        if (instant == null) {
            return null;
        } else {
            return Timestamp.newBuilder()
                    .setSeconds(instant.getEpochSecond())
                    .setNanos(instant.getNano())
                    .build();
        }
    }

    private DateUtils() {}

}
