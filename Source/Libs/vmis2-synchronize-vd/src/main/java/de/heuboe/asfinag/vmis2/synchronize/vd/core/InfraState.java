package de.heuboe.asfinag.vmis2.synchronize.vd.core;

import java.time.Instant;
import lombok.Value;

/**
 * Infrastructure state per lane for short term recording lane data
 */
@Value
public class InfraState {
    /**
     * Id of infrastructure object
     */
    private String id;
    
    /**
     * True if the infrastructure object can supply correct data.
     */
    private boolean ok;
    /**
     * Contains the cause if ok is set to false.
     */
    private String cause;
    
    /**
     * Time of the status change
     */
    private Instant time;
    
    /**
     * True if the infrastructure object is physical passivated
     */
    private boolean physicalPassivated;
    
    /**
     * True if the infrastructure object is logical passivated
     */
    private boolean logicalPassivated;
    
}
