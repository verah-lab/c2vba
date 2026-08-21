package de.heuboe.asfinag.vmis2.synchronize.vd.core;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Parts of the single vehicle data needed to determine the speed of the slowest vehicle at the
 * detector.
 */
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Data
@Setter(AccessLevel.NONE)
@Builder(toBuilder = true)
@AllArgsConstructor
public class SingleVehicleData {
    /**
     * Id of infrastructure object
     */
    private String id;

    /**
     * It indicates the time when the vehicle passage at the detector is completed.
     */
    private Instant passageTime;

    /**
     * Vehicle category of the single vehicle
     */
    private int vehicleCategory;

    /**
     * Speed of the single vehicle
     */
    private int vFZ;
    
    /**
     * State of single vehicle data
     * 0: Data set is completely determined
     * 1-127: Data set not completely determined, further processing only possible with restrictions.
     */
    private int state;
    
    /** 
     * Quality of the statement, this is the slowest vehicle. 
     */
    private int quality;
}
