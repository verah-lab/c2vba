package de.heuboe.asfinag.vmis2.infrastructure.types;

import java.util.List;
import java.util.Map;
import de.heuboe.asfinag.vmis2.infrastructure.base.GeoReference;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureObject;
import lombok.NoArgsConstructor;


/**
 * Object to indicate the infrastructure object "lane".
 */
@NoArgsConstructor
public class Lane extends InfrastructureObjectBase {

    /**
     * Constructs a lane infrastructure object
     * 
     * @param id Id of the lane
     * @param name Name of the lane
     * @param shortName Short name of the lane or null
     * @param version Configuration version
     * @param geoReference Geo reference or null
     * @param references Map of reference type and list of related InfrastructureObject or null
     * @param attachedData An infrastructureObject may have attached data of a certain class
     */
    public Lane(String id, String name, String shortName, String version, GeoReference geoReference,
            Map<String, List<InfrastructureObject>> references, Map<String, Object> attachedData) {
        super(id, name, shortName, version, geoReference, "Lane", references, attachedData);
    }

    
}
