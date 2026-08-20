package de.heuboe.asfinag.vmis2.synchronize.vd.schedule;

import java.time.Instant;
import java.util.List;

import lombok.Value;

/**
 * Object to transport interval timeout trigger data.
 * 
 * @author David Hermanns, Heusch/Boesefeldt GmbH, david.hermanns@heuboe.de; 15.08.2019
 *
 */
@Value
public class IntervalTimeoutTriggerData {
    Instant timeout;
    Instant intervalEnd;
    List<Integer> intervalLengths;
}
