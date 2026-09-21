package de.heuboe.geo.manager.control.test.internal;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import de.heuboe.geo.manager.control.test.base.BaseT;
import de.heuboe.util.Pair;
import de.heuboe.wls.basic.util.DecimalPointFormatter;
import eu.vmis_ehe.vmis2.geomanager.features.LineOffset;
import eu.vmis_ehe.vmis2.geomanager.features.LineRoadReference;
import eu.vmis_ehe.vmis2.geomanager.features.PointRoadReference;
import eu.vmis_ehe.vmis2.geomanager.features.RoadReference;
import eu.vmis_ehe.vmis2.geomanager.road.ConvertRoadMeterDataRequest;
import eu.vmis_ehe.vmis2.geomanager.road.ConvertRoadMeterDataResult;
import eu.vmis_ehe.vmis2.geomanager.road.ConvertToExternalReferenceRequest;
import eu.vmis_ehe.vmis2.geomanager.road.ConvertToExternalReferenceResult;
import eu.vmis_ehe.vmis2.geomanager.road.ConvertRoadMeterDataResult.RoadOffsetData;
import eu.vmis_ehe.vmis2.geomanager.road.ConvertToExternalReferenceRequest.RoadReferenceData;
import eu.vmis_ehe.vmis2.geomanager.routing.RouteDefinition;
import eu.vmis_ehe.vmis2.geomanager.routing.RoutingRequest;
import eu.vmis_ehe.vmis2.geomanager.routing.RoutingResult;
import eu.vmis_ehe.vmis2.geomanager.road.RoadMeter;
import eu.vmis_ehe.vmis2.geomanager.road.ConvertRoadMeterDataRequest.RoadMeterData;



/**
 *
 * Road test
 *
 * @author Peter Schmitz, Heusch Boesefeldt GmbH, peter.schmitz@heuboe.de
 */
@EnableAutoConfiguration
public class Conversion_ITTest extends BaseT {
    
    private static final DecimalPointFormatter KMF = new DecimalPointFormatter( "#0.000000" ); 
    
    
    @SuppressWarnings("serial")
    private List< Pair<String,Double> > virtualAqKmPosA57S = new ArrayList< Pair<String,Double> >() {{
        add( new Pair<>( "68,481", 68.481 ) );
        add( new Pair<>( "70,741", 70.741 ) );
        add( new Pair<>( "71,183", 71.183 ) );
        add( new Pair<>( "72,017", 72.017 ) );
        add( new Pair<>( "72,411", 72.411 ) );
        add( new Pair<>( "73,482", 73.482 ) );
        add( new Pair<>( "74,401", 74.401 ) );
        add( new Pair<>( "74,937", 74.937 ) );
        add( new Pair<>( "75,983", 75.983 ) );
        add( new Pair<>( "76,491", 76.491 ) );
    }};    

    @SuppressWarnings("serial")
    private List< Pair<String,Double> > virtualAqKmPosA57N = new ArrayList< Pair<String,Double> >() {{
        add( new Pair<>( "77,225", 77.225 ) );
        add( new Pair<>( "69,438", 69.438 ));
        add( new Pair<>( "70,979", 70.979 ));
        add( new Pair<>( "71,951", 71.951 ));
        add( new Pair<>( "72,377", 72.377 ));
        add( new Pair<>( "73,545", 73.545 ));
        add( new Pair<>( "73,989", 73.989 ));
        add( new Pair<>( "74,924", 74.924 ));
        add( new Pair<>( "75,414", 75.414 ));
        add( new Pair<>( "76,269", 76.269 ));
        add( new Pair<>( "76,635", 76.635 ));
    }};    

    
    /**
     * Test conversion of RoadMeter to PointRoadReference. Requested Road starts with 0 metering.
     */
    @Test
    public void testConvertToPointRoadReference_S()  throws Exception {
        ConvertRoadMeterDataRequest.Builder request = ConvertRoadMeterDataRequest.newBuilder();
        RoadMeterData.Builder rmd = RoadMeterData.newBuilder();
        rmd.setRoadMeter( RoadMeter.newBuilder().setRouteId( "A57 S" ).setMeter( 72017 ) );
        rmd.setId( "1" );
        request.addRoadMeterData( rmd );

        ConvertRoadMeterDataResult result = gmr.convertRoadMeterData( request.build() );
        assertTrue( result.getRoadOffsetList().get(0).getRoadOffset().getOffset() > 5000 );
    }
    
    
    /**
     * Test conversion of RoadMeter to PointRoadReference. Requested Road starts with 0 metering.
     */
    @Test
    public void testConvertToPointRoadReference_N() throws Exception {
        String roadId = "A57 N";
        double meter = 75000.0;

        RoadMeter.Builder builder = RoadMeter.newBuilder();
        builder.setRouteId( roadId );
        builder.setMeter( meter );

        PointRoadReference roadPoint = gmr.getRoadPoint( builder.build() );
        assertEquals( roadId, roadPoint.getRoadId() );
        assertTrue( roadPoint.getOffset() > 1000 );
    }

    
    private RouteDefinition createRouteDefinition1() {
        
        RouteDefinition.Builder rd = RouteDefinition.newBuilder();
        rd.setId( "1" );
        
        PointRoadReference.Builder prr1 = PointRoadReference.newBuilder(); 
        prr1.setRoadId( "A57 S" );
        prr1.setOffset( 7000.0 );
        rd.setFromPoint( prr1.build() );
        
        PointRoadReference.Builder prr2 = PointRoadReference.newBuilder(); 
        prr2.setRoadId( "A57 S" );
        prr2.setOffset( 10000.0 );
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

	
	//@Test
	public void testkmConversion() throws Exception {
	
        ConvertRoadMeterDataRequest.Builder request = ConvertRoadMeterDataRequest.newBuilder();
        
        for( Pair<String,Double> kmPos : virtualAqKmPosA57N ) {
            RoadMeterData.Builder rmd = RoadMeterData.newBuilder();
            rmd.setRoadMeter( RoadMeter.newBuilder().setRouteId( "A57 N" ).setMeter( kmPos.getSecond() * 1000.0 ) );
            rmd.setId( kmPos.getFirst() );
            request.addRoadMeterData( rmd );
        }
        for( Pair<String,Double> kmPos : virtualAqKmPosA57S ) {
            RoadMeterData.Builder rmd = RoadMeterData.newBuilder();
            rmd.setRoadMeter( RoadMeter.newBuilder().setRouteId( "A57 S" ).setMeter( kmPos.getSecond() * 1000.0 ) );
            rmd.setId( kmPos.getFirst() );
            request.addRoadMeterData( rmd );
        }
        
        request.setResultEpsg( "EPSG:4326" );
        ConvertRoadMeterDataResult result = gmr.convertRoadMeterData( request.build() );
        
        assertTrue( result.getRoadOffsetList().size() == virtualAqKmPosA57S.size() + virtualAqKmPosA57N.size());
        
        for( RoadOffsetData rod : result.getRoadOffsetList() ) {
            double lat = rod.getRoadOffset().getPoint().getCoordinates(0).getLatitude();
            double lng = rod.getRoadOffset().getPoint().getCoordinates(0).getLongitude();
            System.out.println( rod.getRoadOffset().getRoadId() + ";" + rod.getId() + ";" + KMF.format(lat) + ";" + KMF.format(lng) );
        }
	}
    
	@Test
    public void testFcdkmConversion() throws Exception {
    
        ConvertRoadMeterDataRequest.Builder request = ConvertRoadMeterDataRequest.newBuilder();
        
        double km = 70.3;
        
        
        while( km < 76.2 ) {
            
            {
                RoadMeterData.Builder rmd = RoadMeterData.newBuilder();
                rmd.setRoadMeter( RoadMeter.newBuilder().setRouteId( "A57 N" ).setMeter( km * 1000.0 ) );
                rmd.setId( "N-" + km );
                request.addRoadMeterData( rmd );
            }
            {
                RoadMeterData.Builder rmd = RoadMeterData.newBuilder();
                rmd.setRoadMeter( RoadMeter.newBuilder().setRouteId( "A57 S" ).setMeter( km * 1000.0 ) );
                rmd.setId( "S-" + km );
                request.addRoadMeterData( rmd );
            }
            
            km = km + 0.2;
        }
        
        request.setResultEpsg( "EPSG:4326" );
        ConvertRoadMeterDataResult result = gmr.convertRoadMeterData( request.build() );
        
        for( RoadOffsetData rod : result.getRoadOffsetList() ) {
            double lat = rod.getRoadOffset().getPoint().getCoordinates(0).getLatitude();
            double lng = rod.getRoadOffset().getPoint().getCoordinates(0).getLongitude();
            String key = rod.getId().substring( 0, 6 ) + "0";
            System.out.println( key.replace( ".", "") + ";" + KMF.format(lat) + ";" + KMF.format(lng) );
        }
        
        assertTrue( !result.getRoadOffsetList().isEmpty() );
    }

	
    @SuppressWarnings("unused")
    private void addRoadReferenceData(  ConvertToExternalReferenceRequest.Builder request,
                                        String id, 
                                        String roadId,
                                        Double offset) {
        RoadReferenceData.Builder rrd = RoadReferenceData.newBuilder();
        rrd.setId(id);

        PointRoadReference.Builder prr = PointRoadReference.newBuilder();
        if (roadId != null) {
            prr.setRoadId(roadId);
        }

        if (offset != null) {
            prr.setOffset(offset);
        }

        RoadReference rr = RoadReference.newBuilder().setPointReference(prr.build()).build();
        rrd.setRoadReference(rr);
        request.addRoadReference(rrd);
    }
    
    private void addRoadReferenceData( ConvertToExternalReferenceRequest.Builder request, 
                                       String id, 
                                       String roadId,
                                       Double from, 
                                       Double length ) {
        RoadReferenceData.Builder rrd = RoadReferenceData.newBuilder();
        rrd.setId(id);

        LineRoadReference.Builder lrr = LineRoadReference.newBuilder();
        if (roadId != null) {
            lrr.setRoadId(roadId);
        }

        if ((from == null) && (length == null)) {
            // No LineOffset
        } else {

            LineOffset.Builder lo = LineOffset.newBuilder();
            if (from != null) {
                lo.setOffset(from);
            }
            if (length != null) {
                lo.setLength(length);
            }
            lrr.setLineOffset(lo.build());
        }

        RoadReference rr = RoadReference.newBuilder().setLineReference(lrr.build()).build();
        rrd.setRoadReference(rr);
        request.addRoadReference(rrd);
    }
	
    /**
     * 
     * This test converts a wide range of internal line and point locations to external references 
     * 
     */
    @Test 
    public void textConvertToExternalReference() throws Exception { 
        {
            ConvertToExternalReferenceRequest.Builder request = ConvertToExternalReferenceRequest.newBuilder();
            
            addRoadReferenceData( request, "1", "A57 N", 10002.0, 1000.0 );
            
            ConvertToExternalReferenceResult result = gmr.convertToExternalReference( request.build() );
            assertEquals( 1,  result.getExternalReferenceDataCount() );
        }
    }

}
