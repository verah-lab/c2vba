package de.heuboe.asfinag.vmis2.infrastructure.types;

import java.util.List;
import java.util.Map;
import de.heuboe.asfinag.vmis2.infrastructure.base.GeoReference;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureObject;
import lombok.NoArgsConstructor;

/**
 * Object to indicate the infrastructure object "DevicePanel".
 */
@NoArgsConstructor
public class VariableTrafficPanel extends InfrastructureObjectBase {


    /**
     * Constructor for a variable traffic panel infrastructure object
     * 
     * @param id variable traffic panel id
     * @param name variable traffic panel name
     * @param shortName Short name of the variable traffic panel or null
     * @param version Configuration version
     * @param geoReference Geo reference or null
     * @param references Map of reference type and list of related InfrastructureObject or null
     * @param attachedData An infrastructureObject may have attached data of a certain class
     */
    public VariableTrafficPanel(String id, String name, String shortName, String version, GeoReference geoReference,
             Map<String, List<InfrastructureObject>> references, Map<String, Object> attachedData) {
        super(id, name, shortName, version, geoReference, "VariableTrafficPanel", references, attachedData);
    }
}
