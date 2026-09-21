package de.heuboe.geo.manager.control.c2vba.wls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import de.heuboe.geo.manager.base.config.ConfigBase;
import de.heuboe.geo.manager.base.config.Properties;
import de.heuboe.geo.manager.map.importer.RoadMapProvider;
import de.heuboe.geo.manager.map.importer.RoadMapSourceProvider;
import de.heuboe.wls.map.supply.MapSupply;
import de.heuboe.wls.map.supply.MapSupplyPartManager;
import de.heuboe.wls.plugin.openlr.OpenLRPlugin;
import de.heuboe.wls.roadmap.RoadMapCache;
import de.heuboe.wls.roadmap.StrategicNetFilter;
import de.heuboe.wls.srv.impl.WlsEnvironment;
import de.heuboe.wls.util.WlsException;

/**
 * 
 * HERE map configuration
 * 
 * @author peters
 *
 */
@Configuration
@Profile("WLS") 
public class ConfigRoadC2VBA {
    
    @Bean
    TmcFilterSdbby tmcFilter( Properties props ) {
        return new TmcFilterSdbby( props.getAddRouteNums() );
    }

    @Bean
    RoadMapSourceProvider roadMapSourceProvider() {
        return new RoadMapSourceProviderHERE();
    }
    
    @Bean
    RoadMapProvider roadMapImporter( Properties props, 
                                     WlsEnvironment wlsEnvironment,
                                     RoadMapSourceProvider roadMapSourceProvider,
                                     MapSupplyPartManager mapSupplyPartManager,
                                     StrategicNetFilter strategicNetFilter,
                                     @Autowired(required = false)  MapSupply mapSupply )  throws WlsException {
        
        return RoadMapProvider.createRoadMapImporter( props.getShapeDir(), 
                                                      false, wlsEnvironment, 
                                                      roadMapSourceProvider, 
                                                      mapSupplyPartManager, 
                                                      strategicNetFilter, 
                                                      mapSupply );                    
        
    }

    @Bean
    OpenLRPlugin openLRPlugin( Properties props, 
                                      RoadMapCache roadMapCache ) throws Exception {   // NOSONAR
        OpenLRPlugin openLRPlugin = new OpenLRPlugin();
        
        openLRPlugin.setWlsSrid( ConfigBase.getWlsSrid() );
        openLRPlugin.setRoadMap( roadMapCache );
        openLRPlugin.setDecoderConfig( props.getOpenLrPluginDecoderCfgFile() );
        openLRPlugin.setEncoderConfig( props.getOpenLrPluginEncoderCfgFile() );
        
        openLRPlugin.init();
        
        return openLRPlugin;
    }
     

}
