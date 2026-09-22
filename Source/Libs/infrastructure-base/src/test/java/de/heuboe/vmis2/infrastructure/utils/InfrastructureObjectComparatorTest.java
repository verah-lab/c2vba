package de.heuboe.vmis2.infrastructure.utils;

import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureObject;
import de.heuboe.asfinag.vmis2.infrastructure.types.DisplayPanel;
import de.heuboe.asfinag.vmis2.infrastructure.types.LineReference;
import de.heuboe.asfinag.vmis2.infrastructure.utils.InfrastructureObjectComparator;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InfrastructureObjectComparatorTest {

    @Test
    public void checkComparator() {
        String id = "AQ2";
        String name = "AQ2_a23-1";
        String shortName = "AQ2";
        LineReference geoReference = new LineReference("A23-1", 2, 2, "Todo");
        String version = "verison";
        Map<String, List<InfrastructureObject>> references = new HashMap<>();

        DisplayPanel dp2 = new DisplayPanel(id, name, shortName, version, geoReference, references, Map.of());

         id = "AQ1";
         name = "AQ1_a23-1";
         shortName = "AQ1";
         geoReference = new LineReference("A23-1", 1, 1, "Todo");

        DisplayPanel dp1 = new DisplayPanel(id, name, shortName, version, geoReference, references, Map.of());

         id = "AQ3";
         name = "AQ3_a23-1";
         shortName = "AQ3";
         geoReference = new LineReference("A23-1", 4, 4, "Todo");

        DisplayPanel dp3 = new DisplayPanel(id, name, shortName, version, geoReference, references, Map.of());

        List<InfrastructureObject>  aqs = new ArrayList<>();
        aqs.add(dp2);
        aqs.add(dp3);
        aqs.add(dp1);

        InfrastructureObjectComparator comparator = new InfrastructureObjectComparator();

        aqs= aqs.stream().sorted(comparator).toList();

        assertEquals("AQ1", aqs.get(0).getId());
        assertEquals("AQ2", aqs.get(1).getId());
        assertEquals("AQ3", aqs.get(2).getId());

        
        
    }

    @Test
    public void checkNoArgsConstructor() {
        DisplayPanel dp1 = new DisplayPanel();

        assertEquals(null, dp1.getId());
    }
}
