package de.heuboe.vmis2.infrastructure.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import de.heuboe.asfinag.vmis2.infrastructure.base.GeoReference;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureObject;
import de.heuboe.asfinag.vmis2.infrastructure.types.DisplayPanel;
import de.heuboe.asfinag.vmis2.infrastructure.types.Lane;
import de.heuboe.asfinag.vmis2.infrastructure.types.LineReference;
import de.heuboe.asfinag.vmis2.infrastructure.types.ReferenceTypes;
import de.heuboe.asfinag.vmis2.infrastructure.types.Road;
import de.heuboe.asfinag.vmis2.infrastructure.types.RouteStation;

public class LanesTest {
    @Test
    public void checkGetter() {

        String version = "config-version-1";    
        int kmFrom = 4;
        int kmTo = 6;
        
        String roadName = "A23_1";
        String roadId = "A23_1";
        Map<String,List<InfrastructureObject>> roadReferences = new HashMap<>();
        
        String routeStationId = "A23_1_A1";
        String routeStationName = "A23_1-route-station-1";
        String routeStationShortName = "A1";
        Map<String,List<InfrastructureObject>> routeStationReferences= new HashMap<>();
        
        String laneId = "A23_1_A1_L1";
        String laneName = "A23_1-route-station-1-lane-1";
        String laneShortName = "l1";
        Map<String,List<InfrastructureObject>> laneReferences= new HashMap<>();
        
        
        GeoReference geoReference = new LineReference(roadId, kmFrom, kmTo, "1.0");
        
        
        InfrastructureObject refRoad = new Road(roadId, roadName, roadName, version, geoReference, roadReferences, Map.of() );
        InfrastructureObject refRouteStation =
                new RouteStation(routeStationId, routeStationName, routeStationShortName, version, new LineReference(roadId, 5 , 5, "1.0"), routeStationReferences,Map.of());
        
        laneReferences.put(refRoad.getType(), List.of(refRoad));
        laneReferences.put(refRouteStation.getType(), List.of(refRouteStation));
        
        Lane lane = new Lane(laneId, laneName, laneShortName, version, geoReference, laneReferences, Map.of());
        
        

        assertEquals(laneId, lane.getId());
        assertEquals(laneName, lane.getName().get());
        assertEquals("Lane", lane.getType());
        assertEquals(geoReference, lane.getGeoReference().get());
        assertEquals(version, lane.getVersion().get());
        assertEquals(roadName, lane.getReferences(refRoad.getType()).get(0).getName().get());
    }
    
    @Test
    public void checkNoArgsConstructor() {
        Lane lane1 = new Lane();
        
        assertEquals(null, lane1.getId());
    }
}
