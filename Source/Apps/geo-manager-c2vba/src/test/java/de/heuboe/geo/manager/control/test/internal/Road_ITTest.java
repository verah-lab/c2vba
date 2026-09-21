package de.heuboe.geo.manager.control.test.internal;



import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import de.heuboe.geo.manager.base.road.RoadComponentBuilderControl;
import de.heuboe.geo.manager.control.test.base.BaseT;
import de.heuboe.wls.basic.util.DecimalPointFormatter;
import de.heuboe.wls.data.geomanager.GeoManagerRoadComponent;
import de.heuboe.wls.data.geomanager.LaneNumberSection;



/**
 *
 * Road test
 *
 * @author Peter Schmitz, Heusch Boesefeldt GmbH, peter.schmitz@heuboe.de
 */
@EnableAutoConfiguration
public class Road_ITTest extends BaseT {
	
    private static final DecimalPointFormatter KMF = new DecimalPointFormatter( "#0.000" );
	private RoadComponentBuilderControl rcbc;
    
    protected void postInit() {
        rcbc = webLocationServiceManager.getConfigurationObject( RoadComponentBuilderControl.class );
    }
	 
	
	@Test
	public void testRoads() throws Exception {
	    GeoManagerRoadComponent grc = (GeoManagerRoadComponent)rcbc.getRoadComponent( "B57 S" );
	    
	    for( LaneNumberSection lns : grc.getLaneNumberSection() ) {
	        System.out.println( KMF.format( lns.getSection().getOffset() ) + " - " + 
	                             KMF.format( lns.getSection().getOffset() + lns.getSection().getLength() ) + ": " + 
	                             lns.getLaneNumber() );
	    }
	    
	    checkResult( grc.getLaneNumberSection(), "LaneNum_B57_S" );
    }

    
}
