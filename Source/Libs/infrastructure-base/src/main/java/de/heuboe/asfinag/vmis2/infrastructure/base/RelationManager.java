package de.heuboe.asfinag.vmis2.infrastructure.base;

import java.util.List;

import de.heuboe.asfinag.vmis2.infrastructure.relations.Relation;

/**
 * Access to item relations.
 */
public interface RelationManager {
    /**
     * Returns a list of relation types, e.g. REL_LPL1_NS_LT, REL_LPL2_NS_NI
     * @return relation types
     */
    List<String> getTypes();
    /**
     * Returns all objects of given type
     * @param type infrastructure type  
     * @return objects of given type
     */
    List<Relation> getRelationsOfType(String type);

}
