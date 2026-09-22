package de.heuboe.asfinag.vmis2.infrastructure.base;

import eu.vmis_ehe.vmis2.control.data.pojo.PValiditySection;
import eu.vmis_ehe.vmis2.geomanager.features.PointRoadReference;

import java.util.Optional;

/**
 * Describes a geo reference.
 */
public interface GeoReference {
    /**
     * Returns id of road, e.g. A23_1
     * @return road id
     */
    public String getRoadId();
    /**
     * Returns from km.
     * @return from km
     */
    public double getKmFrom();
    /**
     * Returns to km
     * @return to km
     */
    public double getKmTo();
    /**
     * Returns validity section including logical from and to km.
     * @return validity section
     */
    public PValiditySection getValiditySection();

    /**
     * Returns point reference on road
     * @return point road reference
     */
    public Optional<PointRoadReference> getPointRoadReference();

    /**
     * Returns to map version
     * @return to map version
     */
    public String getMapVersion();
}
