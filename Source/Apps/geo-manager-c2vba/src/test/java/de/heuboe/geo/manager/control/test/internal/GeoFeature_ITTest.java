package de.heuboe.geo.manager.control.test.internal;



import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import de.heuboe.geo.manager.control.test.base.BaseT;
import eu.vmis_ehe.vmis2.geomanager.features.GeoFeatures;
import eu.vmis_ehe.vmis2.geomanager.features.GeoFilter;
import eu.vmis_ehe.vmis2.geomanager.features.Type;
import eu.vmis_ehe.vmis2.geomanager.features.pojo.PGeoFeatures;



/**
 *
 * Road test
 *
 * @author Peter Schmitz, Heusch Boesefeldt GmbH, peter.schmitz@heuboe.de
 */
@EnableAutoConfiguration
public class GeoFeature_ITTest extends BaseT {
	
	@Test
	public void testGeoFeatureMQ() throws Exception {
	
        GeoFilter filter = GeoFilter.newBuilder().addTypes( Type.MQ ).build();
        GeoFeatures features = gmr.getFeatures( filter );
        
        assertTrue( features.getFeaturesCount() > 10 );
        
        System.out.println( "" );
        System.out.println( objectWriter.writeValueAsString( PGeoFeatures.from(features) ) ) ;
	    
	}
    
    @Test
    public void testGeoFeatureAQ() throws Exception {
    
        GeoFilter filter = GeoFilter.newBuilder().addTypes( Type.AQ ).build();
        GeoFeatures features = gmr.getFeatures( filter );
        
        assertTrue( features.getFeaturesCount() > 10 );
        
        System.out.println( "" );
        System.out.println( objectWriter.writeValueAsString( PGeoFeatures.from(features) ) ) ;
        
    }
}
