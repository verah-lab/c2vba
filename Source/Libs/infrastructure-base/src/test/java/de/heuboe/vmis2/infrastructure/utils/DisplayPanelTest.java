package de.heuboe.vmis2.infrastructure.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureObject;
import de.heuboe.asfinag.vmis2.infrastructure.types.DisplayPanel;
import de.heuboe.asfinag.vmis2.infrastructure.types.LineReference;
import de.heuboe.asfinag.vmis2.infrastructure.types.ReferenceTypes;

public class DisplayPanelTest {

    @Test
    public void checkGetter() {

        String id = "AQ1";
        String name = "AQ1_a23-1";
        String shortName = "AQ1";
        String version = "ToDo";
        LineReference geoReference = new LineReference("A23-1", 1, 1, "Todo");
        Map<String, List<InfrastructureObject>> references = new HashMap<>();

        DisplayPanel dp1 = new DisplayPanel(id, name, shortName, version, geoReference, references, Map.of());

        assertEquals(id, dp1.getId());
        
        
    }

    @Test
    public void checkNoArgsConstructor() {
        DisplayPanel dp1 = new DisplayPanel();

        assertEquals(null, dp1.getId());
    }
}
