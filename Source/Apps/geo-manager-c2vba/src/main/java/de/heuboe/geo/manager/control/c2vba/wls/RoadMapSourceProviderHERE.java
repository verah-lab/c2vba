package de.heuboe.geo.manager.control.c2vba.wls;

import de.heuboe.geo.manager.map.importer.RoadMapSource;
import de.heuboe.geo.manager.map.importer.RoadMapSource.RoadElementSource;
import de.heuboe.geo.manager.map.importer.RoadMapSourceProvider;
import de.heuboe.wls.HERERoadmap;
import de.heuboe.wls.data.roadmap.RoadElement;
import de.heuboe.wls.utils.IterableSource;


/**
 * 
 * RoadMapSourceProvider for HERE map
 * 
 * @author peters
 *
 */
public class RoadMapSourceProviderHERE implements RoadMapSourceProvider {

    @Override
    public RoadMapSource createRoadMapSource() {
        
        RoadMapSource roadMapSource = ( fileLocation, wlsSrid, mapSrid ) -> {   // NOSONAR
            
            return new RoadElementSource() {
                
                private HERERoadmap roadMap = null;

                @Override
                public IterableSource<RoadElement> getSource() {
                    roadMap = new HERERoadmap();
                    roadMap.setWlsSrid( wlsSrid );
                    roadMap.setSourceSrid(mapSrid  );
                    roadMap.setFilename( fileLocation );
                    roadMap.init();
                    return roadMap;
                }

                @Override
                public void close() {
                    if( roadMap != null ) {
                        roadMap.close();
                    }
                }
            };
        };
        
        return roadMapSource;
        
    }

}
