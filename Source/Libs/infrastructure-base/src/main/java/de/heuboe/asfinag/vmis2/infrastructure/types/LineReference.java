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
public class LineReference implements GeoReference{

    private String roadId;
    private double kmFrom; // in km
    private double kmTo; // in km
    private String mapVersion;
    private PValiditySection validitySection;
    private PointRoadReference pointRoadReference;

   /**
    * Constructor
    *
     * @param roadId id of the road
     * @param kmFrom kilometer from
     * @param kmTo kilometer to
     * @param mapVersion version of map
     */
    public LineReference(String roadId, double kmFrom, double kmTo, String mapVersion) {
        this.roadId = roadId;
        this.kmFrom = kmFrom;
        this.kmTo = kmTo;
        this.mapVersion = mapVersion;
    }

    /**
     * Constructor
     *
     * @param validitySection validity section of line reference
     * @param mapVersion version of map
     */
    public LineReference(PValiditySection validitySection, String mapVersion) {
        this.validitySection = validitySection;
        this.kmFrom = validitySection.getLogKmFrom();
        this.kmTo = validitySection.getLogKmTo();
        this.mapVersion = mapVersion;
        this.roadId = validitySection.getRoadId();
    }

    /**
     * Constructor
     *
     * @param validitySection validity section of line reference
     */
    public LineReference(PValiditySection validitySection) {
        this.validitySection = validitySection;
        this.kmFrom = validitySection.getLogKmFrom();
        this.kmTo = validitySection.getLogKmTo();
        if (validitySection.getSection()!=null && validitySection.getSection().getMapReference() != null) {
            this.mapVersion = validitySection.getSection().getMapReference().getMapVersion();
        }
        this.roadId = validitySection.getRoadId();
    }

    /**
     * Constructor
     *
     * @param validitySection validity section of line reference
     * @param pointRoadReference point road reference
     */
    public LineReference(PValiditySection validitySection, PointRoadReference pointRoadReference) {
        this.validitySection = validitySection;
        this.kmFrom = validitySection.getLogKmFrom();
        this.kmTo = validitySection.getLogKmTo();
        if (validitySection.getSection()!=null && validitySection.getSection().getMapReference() != null) {
            this.mapVersion = validitySection.getSection().getMapReference().getMapVersion();
        }
        this.roadId = validitySection.getRoadId();
        this.pointRoadReference = pointRoadReference;
    }

    @Override
    public String getRoadId() {
        return roadId;
    }

    @Override
    public double getKmFrom() {
        return kmFrom;
    }

    @Override
    public double getKmTo() {
        return kmTo;
    }

    @Override
    public PValiditySection getValiditySection() {
        if (validitySection == null) {
            validitySection = PValiditySection.builder().logKmFrom(kmFrom).logKmTo(kmTo).roadId(roadId).build();
        }
        return validitySection;
    }

    @Override
    public Optional<PointRoadReference> getPointRoadReference() {
        return Optional.ofNullable(pointRoadReference);
    }

    @Override
    public String getMapVersion() {
        return mapVersion;
    }
}
