package de.heuboe.vmis2.infrastructure.utils;

import de.heuboe.asfinag.vmis2.infrastructure.base.GeoReference;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureObject;
import de.heuboe.asfinag.vmis2.infrastructure.types.Lane;
import de.heuboe.asfinag.vmis2.infrastructure.types.LineReference;
import de.heuboe.asfinag.vmis2.infrastructure.types.Road;
import de.heuboe.asfinag.vmis2.infrastructure.types.RouteStation;
import eu.vmis_ehe.vmis2.configservice.WzgSymbol;
import eu.vmis_ehe.vmis2.control.data.WzgSymbolsMsg;
import eu.vmis_ehe.vmis2.control.data.pojo.PInfrastructureObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RoadTest {
    @Test
    public void checkGetter() {

        String version = "config-version-1";
        int kmFrom = 4;
        int kmTo = 6;



        String roadName = "A23_1";
        String roadId = "A23_1";
        Map<String, List<InfrastructureObject>> roadReferences = new HashMap<>();

        String routeStationId = "A23_1_A1";
        String routeStationName = "A23_1-route-station-1";
        String routeStationShortName = "A1";
        Map<String, List<InfrastructureObject>> routeStationReferences = new HashMap<>();

        String laneId = "A23_1_A1_L1";
        String laneName = "A23_1-route-station-1-lane-1";
        String laneShortName = "l1";
        Map<String, List<InfrastructureObject>> laneReferences = new HashMap<>();


        GeoReference geoReference = new LineReference(roadId, kmFrom, kmTo, "1.0");

        Lane refLane = new Lane(laneId, laneName, laneShortName, version, geoReference,
                laneReferences, Map.of());
        InfrastructureObject refRouteStation = new RouteStation(routeStationId, routeStationName, routeStationShortName,
                version, new LineReference(roadId, 5, 5, "1.0"),
                routeStationReferences, Map.of());

        roadReferences.put(refLane.getType(), List.of(refLane));
        roadReferences.put(refRouteStation.getType(), List.of(refRouteStation));
        WzgSymbol symbol1 =  WzgSymbol.newBuilder().setCode(22).build();
        WzgSymbol symbol2 =  WzgSymbol.newBuilder().setCode(24).build();

        List<WzgSymbol> symbols = new ArrayList<>();
        symbols.add(symbol1);
        symbols.add(symbol2);


        Map<String, Object> attachedData= new HashMap<>();

        InfrastructureObject road = new Road(roadId, roadName, roadName, version, geoReference, roadReferences, attachedData);

        assertEquals(roadId, road.getId());
        assertEquals(roadName, road.getName().get());
        assertEquals("Road", road.getType());
        assertEquals(geoReference, road.getGeoReference().get());
        assertEquals(routeStationShortName, road.getReferences(refRouteStation.getType()).get(0).getShortName().get());



        PInfrastructureObject pRoad = road.getInfrastructureObject();


        assertEquals(road.getId(),pRoad.getId());
    }
    
    @Test
    public void checkNoArgsConstructor() {
        Road r1 = new Road();
        RouteStation rs1 = new RouteStation();
        
        assertEquals(null, r1.getId());
        assertEquals(null, rs1.getId());
    }
}
