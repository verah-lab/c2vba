package de.heuboe.asfinag.vmis2.infrastructure.types;

import de.heuboe.asfinag.vmis2.infrastructure.base.GeoReference;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureObject;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;


/**
 * Object to indicate the infrastructure object "junction".
 */
@NoArgsConstructor
public class Junction extends InfrastructureObjectBase {

    /**
     * Constructs a junction infrastructure object
     *
     * @param id id of the junction
     * @param name Name of the junction
     * @param shortName Short name of the junction or null
     * @param version Configuration version
     * @param geoReference Geo reference or null
     * @param references Map of reference type and list of related InfrastructureObject or null
     * @param attachedData An infrastructureObject may have attached data of a certain class
     */
    public Junction(String id, String name, String shortName, String version, GeoReference geoReference,
                    Map<String, List<InfrastructureObject>> references, Map<String, Object> attachedData) {
        super(id, name, shortName, version, geoReference, "Junction", references, attachedData);
    }

    
}
