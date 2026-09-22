package de.heuboe.asfinag.vmis2.infrastructure.relations;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One relation between one source object and multiple target object.
 * 
 * @author David Hermanns, Heusch/Boesefeldt GmbH, david.hermanns@heuboe.de; 30.04.2020
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Relation {

    /**
     * Typo of relation between objects.
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class RelationObject {
        String type;
        String id;
    }
    
    String type;
    RelationObject source;
    List<RelationObject> targets;

}
