package de.heuboe.asfinag.vmis2.infrastructure.types;

import de.heuboe.asfinag.vmis2.infrastructure.base.GeoReference;
import eu.vmis_ehe.vmis2.control.data.pojo.PValiditySection;
import eu.vmis_ehe.vmis2.geomanager.features.PointRoadReference;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Optional;

/**
 * Logical validity section.
 */
@AllArgsConstructor
@NoArgsConstructor
public class PointReference implements GeoReference{

    private String roadId;
    private double km; // logical km
    private String mapVersion;
    
    @Override
    public String getRoadId() {
        return roadId;
    }

    @Override
    public double getKmFrom() {
        return km;
    }

    @Override
    public double getKmTo() {
        return  getKmFrom();
    }

    @Override
    public PValiditySection getValiditySection() {
        return PValiditySection.builder().logKmFrom(km).logKmTo(km).roadId(roadId).build();
    }

    @Override
    public Optional<PointRoadReference> getPointRoadReference() {
        return Optional.empty();
    }

    @Override
    public String getMapVersion() {
        return mapVersion;
    }
}
