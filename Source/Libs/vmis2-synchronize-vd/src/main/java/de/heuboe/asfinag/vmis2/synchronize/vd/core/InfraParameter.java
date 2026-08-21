package de.heuboe.asfinag.vmis2.synchronize.vd.core;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Infrastructure parameter per lane for short term recording lane data
 */

@Value
@EqualsAndHashCode(callSuper = true)
public class InfraParameter extends AbstractInfraParameter {
    private int intervalLength;
    private Integer version;
    private Boolean vArithmetical;
    private List<Integer> categoryBoundariesPkw;
    private List<Integer> categoryBoundariesLkw;
    
    /**
     * Constructor
     * 
     * @param id Lane id
     * @param type Type {@link Type} of parameter
     * @param time Time of last change
     * @param intervalLength Interval length in seconds
     * @param version Data version of short term recording lane data
     * @param vArithmetical true, speeds are arithmetically averaged.
     * @param categoryBoundariesPkw category boundaries for PKW (from TLS parameter
     *        LVEGeschwindigkeitsklassenKurz)
     * @param categoryBoundariesLkw category boundaries for LKW (from TLS parameter
     *        LVEGeschwindigkeitsklassenKurz)
     * 
     */
    public InfraParameter(String id, Type type, Instant time, int intervalLength, Integer version,
            Boolean vArithmetical, List<Integer> categoryBoundariesPkw, List<Integer> categoryBoundariesLkw) {
        super(id, type, time);
        this.intervalLength = intervalLength;
        this.version =version;
        this.vArithmetical = vArithmetical;
        this.categoryBoundariesPkw = categoryBoundariesPkw;
        this.categoryBoundariesLkw = categoryBoundariesLkw;
    }
   
    public Optional<Integer> getVersion() {
        return Optional.ofNullable(version);
    }
}
