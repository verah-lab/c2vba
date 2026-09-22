package de.heuboe.asfinag.vmis2.infrastructure.base;

import eu.vmis_ehe.vmis2.control.data.pojo.PInfrastructureObject;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * An infrastructure object describes
 */
public interface InfrastructureObject {
    /**
     * An infrastructure object must have a permanent id
     *
     * @return the permanent id
     */
    String getId();

    /**
     * An infrastructure object should have a type. This may be the class name of a derived class or a
     * free type scheme
     *
     * @return the type
     */
    String getType();

    /**
     * An infrastructure object should have a name
     *
     * @return the name
     */
    Optional<String> getName();

    /**
     * An infrastructure object may have a short name
     *
     * @return the short name
     */
    Optional<String> getShortName();

    /**
     * An infrastructure object should have a version
     *
     * @return the version
     */
    Optional<String> getVersion();

    /**
     * An infrastructure object should have a geo reference
     *
     * @return the geo reference
     */
    Optional<GeoReference> getGeoReference();

    /**
     * An infrastructure object may have relations to other infrastructure objects of certain types
     * E.g.: NEXT, PREV, RELATED_MS, RELATED_VMS, etc.
     *
     * @return the map with the reference type as key and a list of infrastructure objects as value.
     */
    Map<String, List<InfrastructureObject>> getReferences();

    /**
     * An infrastructure object may have relations to other infrastructure objects of certain types
     * E.g.: NEXT, PREV, RELATED_MS, RELATED_VMS, etc.
     *
     * @param refType infrastructure reference type
     * @return list of related infrastructure objects
     */
    List<InfrastructureObject> getReferences(String refType);

    /**
     * An infrastructure object may have attached data of a certain class
     *
     * @param <T> certain class
     * @param clazz the class of the returned data
     * @return attached data
     */
    <T> Optional<T> getAttachedData(Class<T> clazz);

    /**
     * An infrastructure object may want to update or add references.
     * Given references will be overwrite old references of given refType.
     *
     * @param refType infrastructure reference type
     * @param references list of related infrastructure objects
     */
    void updateReferences(String refType, List<InfrastructureObject> references);

    /**
     * Attached data of An infrastructure object.
     *
     * @return attached data
     */
    Map<String, Object> getAttachedData();



    /**
     * An infrastructure object may want to update a attached data of a certain class
     *
     * @param obj that should be attached to the infrastructure object.
     */
    void updateAttachedData(Object obj);

    /**
     * An infrastructure object may want to remove a attached data of a certain class
     *
     * @param obj that should be attached to the infrastructure object.
     */
    void removeAttachedData(Object obj);

    /**
     * Setting a complete new attachedDataMap
     *
     * @param attachedDataMap to set
     */
    void setAttachedDataMap(Map<String, Object> attachedDataMap);

    /**
     * getPInfrastructureObject of infrastructure object
     */
    PInfrastructureObject getInfrastructureObject();

}
