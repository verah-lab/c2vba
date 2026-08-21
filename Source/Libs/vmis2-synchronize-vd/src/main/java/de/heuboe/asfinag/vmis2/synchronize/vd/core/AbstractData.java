package de.heuboe.asfinag.vmis2.synchronize.vd.core;

import java.time.Instant;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Base class of input data of infrastructure objects
 */

@lombok.Data
// @Setter(AccessLevel.NONE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public abstract class AbstractData {

    // Id of corresponding infrastructure object
    private String id;

    // Event time: The point in time when an event or data record occurred,
    // i.e. was originally created "at the source". Contains
    // e.g. the interval time stamp of measurement or calculation time
    private Instant eventTime;

    // Processing time - The point in time when the event or data
    // record happens to be processed by the processing application
    // i.e. when the record is being consumed or written to database or broker.
    // The processing time may be milliseconds, hours, or days etc. later than the
    // original event time. This is something like setting a system_time from
    // the database in old systems */
    private Instant processingTime;

    private int intervalLength;

    private Integer version;

    private boolean createTimeSynchronization;

    /**
     * Get the data version of the input data
     * 
     * @return the data version of the input data if it has one or an empty optional.
     */
    public Optional<Integer> getVersion() {
        return Optional.ofNullable(version);
    }
}
