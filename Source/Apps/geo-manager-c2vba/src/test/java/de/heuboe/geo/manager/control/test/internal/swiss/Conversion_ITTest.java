package de.heuboe.geo.manager.control.test.internal.swiss;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import de.heuboe.geo.manager.control.test.base.BaseT;
import eu.vmis_ehe.vmis2.geomanager.features.PointRoadReference;
import eu.vmis_ehe.vmis2.geomanager.road.ConvertRoadMeterDataRequest;
import eu.vmis_ehe.vmis2.geomanager.road.ConvertRoadMeterDataRequest.RoadMeterData;
import eu.vmis_ehe.vmis2.geomanager.road.ConvertRoadMeterDataResult;
import eu.vmis_ehe.vmis2.geomanager.road.RoadMeter;
import eu.vmis_ehe.vmis2.geomanager.routing.RouteDefinition;
import eu.vmis_ehe.vmis2.geomanager.routing.RoutingRequest;
import eu.vmis_ehe.vmis2.geomanager.routing.RoutingResult;



/**
 *
 * Road test
 *
 * @author Peter Schmitz, Heusch Boesefeldt GmbH, peter.schmitz@heuboe.de
 */
@EnableAutoConfiguration
public class Conversion_ITTest extends BaseT {
    
    /**
     * Test conversion of RoadMeter to PointRoadReference. Requested Road starts with 0 metering.
     */
    @Test
    public void testConvertToPointRoadReference_Swiss()  throws Exception {
        ConvertRoadMeterDataRequest.Builder request = ConvertRoadMeterDataRequest.newBuilder();
        RoadMeterData.Builder rmd = RoadMeterData.newBuilder();
        rmd.setRoadMeter( RoadMeter.newBuilder().setRouteId( "A6_1" ).setMeter( 15678 ) );
        rmd.setId( "1" );
        request.addRoadMeterData( rmd );  

        ConvertRoadMeterDataResult result = gmr.convertRoadMeterData( request.build() );
        assertTrue( result.getRoadOffsetList().get(0).getRoadOffset().getOffset() > 10000 );
    }
    
    
    /**
     * Test conversion of RoadMeter to PointRoadReference. Requested Road starts with 0 metering.
     */
    @Test
    public void testConvertToPointRoadReference() throws Exception {
        String roadId = "A6_2";
        double meter = 6000.0;
        
        //Road: A6_2 Meter: 5494,4 und 29563
        //Road: A6_1 Meter: 4721,4 und 29561,1

        RoadMeter.Builder builder = RoadMeter.newBuilder();
        builder.setRouteId( roadId );
        builder.setMeter( meter );

        PointRoadReference roadPoint = gmr.getRoadPoint( builder.build() );
        assertEquals( roadId, roadPoint.getRoadId() );
        assertTrue( roadPoint.getOffset() > 20000 );
    }

    
    private RouteDefinition createRouteDefinition1() {
        
        RouteDefinition.Builder rd = RouteDefinition.newBuilder();
        rd.setId( "1" );
        
        PointRoadReference.Builder prr1 = PointRoadReference.newBuilder(); 
        prr1.setRoadId( "A6_1" );
        prr1.setOffset( 13998.0 );
        rd.setFromPoint( prr1.build() );
        
        PointRoadReference.Builder prr2 = PointRoadReference.newBuilder(); 
        prr2.setRoadId( "A6_1" );
        prr2.setOffset( 15197.31 );
        rd.setToPoint( prr2.build() );
        
        return rd.build();
    }
    
    @Test
    public void testRouting() throws Exception {
        RoutingRequest.Builder request = RoutingRequest.newBuilder();
        RouteDefinition rd = createRouteDefinition1();
        request.addRouteDefinition( rd );
        RoutingResult result = gmr.calculateRoutes( request.build() );
        
        assertTrue(  result.getRouteList().get(0).getLength() > 1000.0 );
    }

    
}
