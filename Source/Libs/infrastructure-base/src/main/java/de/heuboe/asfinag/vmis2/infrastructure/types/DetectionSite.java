package de.heuboe.asfinag.vmis2.infrastructure.types;

import java.util.List;
import java.util.Map;
import de.heuboe.asfinag.vmis2.infrastructure.base.GeoReference;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureObject;
import lombok.NoArgsConstructor;

/**
 * One traffic data detection site with logical validity section and references (related lanes,
 * related neighbour detection site, ...).
 *
 */
@NoArgsConstructor
public class DetectionSite extends InfrastructureObjectBase {

    /**
     * Constructor for a detection site infrastructure object
     * 
     * @param id Detection site id
     * @param name Detection site name
     * @param shortName Short name of the detection site or null
     * @param version Configuration version
     * @param geoReference Geo reference or null
     * @param references Map of reference type and list of related InfrastructureObject or null
     * @param attachedData An infrastructureObject may have attached data of a certain class
     */
    public DetectionSite(String id, String name, String shortName, String version, GeoReference geoReference,
            Map<String, List<InfrastructureObject>> references, Map<String, Object> attachedData) {
        super(id, name, shortName, version, geoReference, "DetectionSite", references, attachedData);
    }


}
