import de.heuboe.wls.by.cfg.PhysCfg.CfgObject;
import de.heuboe.wls.data.geofeature.GeoFeature;

for( CfgObject co : systemConfiguration.getCfgObjects().values() ) {
        	if( co.getType() != 10 ) {
	        	boolean found = false;
	        	for( GeoFeature gf : qs ) {
	        		if( gf.getId().equals( co.getPermId() )) {
	        			found = true;
	        			break;
	        		}
	        	}
	        	if( !found ) {
	        		System.out.println( co.getKnNr() + " " + co.getDeNr() );
	        		int k = 1;
	        	}
        	}
        }
        

        
