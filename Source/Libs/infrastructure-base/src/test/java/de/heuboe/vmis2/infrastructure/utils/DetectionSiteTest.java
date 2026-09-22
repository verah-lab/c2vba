package de.heuboe.vmis2.infrastructure.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.heuboe.asfinag.vmis2.infrastructure.types.PointReference;
import eu.vmis_ehe.vmis2.control.data.pojo.PInfrastructureObject;
import eu.vmis_ehe.vmis2.control.data.pojo.PValiditySection;
import eu.vmis_ehe.vmis2.geomanager.validitySection.pojo.PMapReference;
import eu.vmis_ehe.vmis2.geomanager.validitySection.pojo.PSection;
import org.junit.jupiter.api.Test;
import de.heuboe.asfinag.vmis2.infrastructure.base.GeoReference;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureObject;
import de.heuboe.asfinag.vmis2.infrastructure.types.DetectionSite;
import de.heuboe.asfinag.vmis2.infrastructure.types.LineReference;

public class DetectionSiteTest {
    @Test
    public void checkGetter() {
        String id = "1";
        String name = "DetectionSite-1";
        String shortName = "DS-1";
        String roadId = "A23_1";
        int kmFrom = 4;
        int kmTo = 6;

        LineReference lineRef = new LineReference(roadId, kmFrom, kmTo, "1.0");
        Map<String, List<InfrastructureObject>> refs = new HashMap<>();
        DetectionSite detSite = new DetectionSite(id, name, shortName, "Version", lineRef, refs, Map.of());

        assertNotNull(detSite);
        assertEquals(kmFrom, detSite.getGeoReference().get().getValiditySection().getLogKmFrom());
        assertEquals(kmTo, detSite.getGeoReference().get().getValiditySection().getLogKmTo());

    }

    @Test
    public void checkLineReferenceConstructor() {
        String id = "1";
        String name = "DetectionSite-1";
        String shortName = "DS-1";
        String roadId = "A23_1";
        int kmFrom = 4;
        int kmTo = 6;
        String mapVersion = "2.1";

        LineReference lineRef = new LineReference(PValiditySection.builder().logKmFrom(kmFrom).logKmTo(6).roadId(roadId).section(
                PSection.builder().mapReference(PMapReference.builder().mapVersion(mapVersion).build()).build()).build());
        Map<String, List<InfrastructureObject>> refs = new HashMap<>();
        DetectionSite detSite = new DetectionSite(id, name, shortName, mapVersion, lineRef, refs, Map.of());

        assertNotNull(detSite);
        assertEquals(kmFrom, detSite.getGeoReference().get().getKmFrom());
        assertEquals(kmTo, detSite.getGeoReference().get().getKmTo());

    }
    @Test
    public void checkGetInfrastructureObject() {
        String id = "1";
        String name = "DetectionSite-1";
        String shortName = "DS-1";
        String roadId = "A23_1";
        int kmFrom = 4;
        int kmTo = 6;
        String mapVersion = "2.1";

        PointReference pointReference = new PointReference("a23_1", 1.,"Map2000");
        Map<String, List<InfrastructureObject>> refs = new HashMap<>();
        DetectionSite detSite = new DetectionSite(id, name, shortName, mapVersion, pointReference, refs, Map.of());

        PInfrastructureObject pInfrastructureObject =detSite.getInfrastructureObject();
        assertEquals(1.,pInfrastructureObject.getLineReference().getKmTo());

    }

    @Test
    public void checkGetAttached() {
        String id = "1";
        String name = "DetectionSite-1";
        String shortName = "DS-1";
        String roadId = "A23_1";
        int kmFrom = 4;
        int kmTo = 6;

        LineReference lineRef = new LineReference(roadId, kmFrom, kmTo, "1.0");
        Map<String, List<InfrastructureObject>> refs = new HashMap<>();
        Map<String, Object> eddTypeMap = new HashMap<>();
        GeoReference logKm = new LineReference("A23_1", 2, 4, "");
        eddTypeMap.put(LineReference.class.getName(), logKm);

        DetectionSite detSite = new DetectionSite(id, name, shortName, "version", lineRef, null, eddTypeMap);


        assertEquals(id, detSite.getId());

        assertEquals(logKm.getRoadId(), detSite.getAttachedData(LineReference.class).get().getRoadId());

        detSite.removeAttachedData(lineRef);

        assertEquals(Optional.ofNullable(null), detSite.getAttachedData(LineReference.class));
        detSite.updateAttachedData(lineRef);

        assertEquals(logKm.getRoadId(), detSite.getAttachedData(LineReference.class).get().getRoadId());
    }

    @Test
    public void checkNoArgsConstructor() {
        DetectionSite ds1 = new DetectionSite();

        assertEquals(null, ds1.getId());
    }
}
