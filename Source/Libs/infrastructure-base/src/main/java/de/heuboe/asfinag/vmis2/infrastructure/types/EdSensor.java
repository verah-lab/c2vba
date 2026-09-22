package de.heuboe.asfinag.vmis2.infrastructure.types;

import java.util.List;
import java.util.Map;
import de.heuboe.asfinag.vmis2.infrastructure.base.GeoReference;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One sensor of environmental data.
 * 
 * @author David Hermanns, Heusch/Boesefeldt GmbH, david.hermanns@heuboe.de; 30.04.2020
 *
 */
@NoArgsConstructor
public class EdSensor extends InfrastructureObjectBase {
    /**
    *
    * 
    * class holds the ed sensor type.
    * 
    * @author felixd
    */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
  
    public static class EdSensorType {
        String type;
    }

    /**
     * Constructs a environmental data sensor infrastructure object.
     * 
     * @param id id of the sensor.
     * @param name name of the sensor.
     * @param shortName shortName of the sensor.
     * @param version config version.
     * @param geoReference the geo reference or null.
     * @param references references to other infrastructure object.
     * @param attachedData possibility to attach additional data.
     */
    public EdSensor(String id, String name, String shortName, String version, GeoReference geoReference,
            Map<String, List<InfrastructureObject>> references, Map<String, Object> attachedData) {
        super(id, name, shortName, version, geoReference, ReferenceTypes.ED_SENSOR, references, attachedData);
    }

}
