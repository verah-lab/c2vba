package de.heuboe.asfinag.vmis2.synchronize.vd.schedule;

import java.time.Instant;
import java.util.List;

import lombok.Value;

/**
 * Object to transport interval end trigger data.
 * 
 * @author David Hermanns, Heusch/Boesefeldt GmbH, david.hermanns@heuboe.de; 15.08.2019
 *
 */
@Value
public class IntervalEndTriggerData {
    Instant intervalEnd;
    List<Integer> intervalLenghts;
}
