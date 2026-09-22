package de.heuboe.asfinag.vmis2.infrastructure.types;

import de.heuboe.asfinag.vmis2.infrastructure.base.GeoReference;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureObject;

import java.util.List;
import java.util.Map;

/**
 * Object to indicate the infrastructure object communication controller.
 */
public class CommunicationController extends InfrastructureObjectBase {

    /**
     * Constructs a communication controller infrastructure object
     *
     * @param id           Id of the communication controller
     * @param name         Name of the communication controller
     * @param shortName    Short name of the communication controller or null
     * @param version      Configuration version
     * @param geoReference Geo reference or null
     * @param references   Map of reference type and list of related InfrastructureObject or null
     * @param attachedData An infrastructureObject may have attached data of a certain class
     */
    public CommunicationController(String id, String name, String shortName, String version, GeoReference geoReference,
            Map<String, List<InfrastructureObject>> references, Map<String, Object> attachedData) {
        super(id, name, shortName, version, geoReference, ReferenceTypes.COMMUNICATION_CONTROLLER, references,
                attachedData);
    }
}
