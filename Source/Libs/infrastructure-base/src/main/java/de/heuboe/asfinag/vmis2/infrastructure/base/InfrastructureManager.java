package de.heuboe.asfinag.vmis2.infrastructure.base;

import java.util.List;

/**
 * Access to infrastructure objects.
 */
public interface InfrastructureManager {
    /**
     * Returns a list of infrastructure types, e.g. detection-site, road
     * @return infrastructure types
     */
    List<String> getTypes();
    /**
     * Returns all objects of given type
     * @param type infrastructure type  
     * @return objects of given type
     */
    List<InfrastructureObject> getInfrastructureObjectsOfType(String type);
    /**
     * Returns all objects of given ids.
     * @param ids ids of infrastructure objects
     * @return objects of given ids
     */
    List<InfrastructureObject> getInfrastructureObjects(List<String> ids);


}
