package de.heuboe.asfinag.vmis2.synchronize.vd.core;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Base class of Parameters of infrastructure objects
 */

@Data
@Setter(AccessLevel.NONE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public abstract class AbstractInfraParameter {
    /**
     * enumeration whether parameter is a 'global default', a 'local default' for the current sub system
     * or 'direct set' for a measurement site.
     */
    public enum Type {
    GLOBAL_DEFAULT, LOCAL_DEFAULT, DIRECT_SET
    }

    private String id; // Id or corresponding infrastructure object
    private Type type; // Is the parameter a default or is it directly set
    private Instant time; // Time of last change
}
