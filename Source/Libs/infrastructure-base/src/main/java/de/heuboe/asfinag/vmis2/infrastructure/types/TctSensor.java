package de.heuboe.asfinag.vmis2.infrastructure.types;

import java.util.List;
import java.util.Map;
import de.heuboe.asfinag.vmis2.infrastructure.base.GeoReference;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One sensor of traffic control technology data.
 * 
 * @author David Hermanns, Heusch/Boesefeldt GmbH, david.hermanns@heuboe.de; 07.10.2020
 *
 */
@NoArgsConstructor
public class TctSensor extends InfrastructureObjectBase {
    /**
     *
     * 
     * Class holds the traffic control sensor type.
     * 
     * @author David Hermanns, Heusch/Boesefeldt GmbH, david.hermanns@heuboe.de; 07.10.2020
     *
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TctSensorType {
        String type;
    }

    /**
     * Constructs a traffic control data sensor infrastructure object.
     * 
     * @param id            id of the sensor.
     * @param name          name of the sensor.
     * @param shortName     shortName of the sensor.
     * @param version       config version.
     * @param geoReference  the geo reference or null.
     * @param references    references to other infrastructure object.
     * @param attachedData  possibility to attach additional data.
     */
    public TctSensor(String id, String name, String shortName, String version, GeoReference geoReference,
            Map<String, List<InfrastructureObject>> references, Map<String, Object> attachedData) {
        super(id, name, shortName, version, geoReference, ReferenceTypes.TCT_SENSOR_TYPE, references, attachedData);
    }

}
