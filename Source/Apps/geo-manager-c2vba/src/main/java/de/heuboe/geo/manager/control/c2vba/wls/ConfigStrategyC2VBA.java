package de.heuboe.geo.manager.control.c2vba.wls;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import de.heuboe.geo.manager.base.map.update.MapSupplyProviderUtil;
import de.heuboe.geo.manager.base.road.RoadManager;
import de.heuboe.geo.manager.control.c2vba.strategy.StrategyGetterPlugin;
import de.heuboe.geo.manager.control.c2vba.strategy.StrategyLocator;
import de.heuboe.geo.manager.control.c2vba.strategy.StrategyProvider;
import de.heuboe.geo.manager.control.c2vba.strategy.StrategyRouteGetterPlugin;
import de.heuboe.geo.manager.control.c2vba.strategy.StrategyTriggerGetterPlugin;
import de.heuboe.geo.manager.map.MapSupplyProviderBaseTypes;
import de.heuboe.log.Logger;
import de.heuboe.wls.iface.Fault;
import de.heuboe.wls.map.supply.MapSupply;
import de.heuboe.wls.map.supply.MapSupplyPartManager;
import de.heuboe.wls.map.supply.MapDataVersion.Version;
import de.heuboe.wls.plugin.roadmap.RoadMapPlugin;
import de.heuboe.wls.plugin.roadmap.RoadNetPlugin;
import de.heuboe.wls.srv.impl.WlsEnvironment;
import de.heuboe.wls.util.WlsException;

/**
 * 
 * Strategy configuration
 * 
 * @author peters
 *
 */
@Configuration
@Profile("WLS") 
public class ConfigStrategyC2VBA {

    private static final Logger LOGGER = Logger.getLogger(ConfigStrategyC2VBA.class);
    
    private MapSupplyProviderUtil readerUtil = new MapSupplyProviderUtil();
    
    /**
     * Create bean ConfigInfrastructureReader
     *
     * @param mapSupply     one map with its components
     * @param configService delivers configuration environment
     * @return instance of InfrastructureReader
     * @throws Exception in case of error
     */
    @Bean
    StrategyProvider strategyProvider( WlsEnvironment wlsEnvironment, 
                                       MapSupplyPartManager mapSupplyPartManager,
                                       MapSupply mapSupply, 
                                       @Value("${de.heuboe.geo.manager.strategyFile}") String strategyFile ) throws WlsException {
        
        Version version = ( mapSupply != null ) ? 
                         mapSupply.getVersion( MapSupplyProviderBaseTypes.STRATEGY ) : 
                         MapSupplyProviderBaseTypes.INITIAL_NET_VERSION; 
                         
        StrategyProvider strategyProvider = new StrategyProvider( wlsEnvironment, 
                                                                  mapSupply,
                                                                  MapSupplyProviderBaseTypes.STRATEGY, 
                                                                  version,
                                                                  strategyFile );
        
        
        if( !strategyProvider.initMapData() ) {
            readerUtil.throwError( strategyProvider );
        }
        
        LOGGER.info( "StrategyProvider created");
        
        try {
            mapSupplyPartManager.registerProvider( strategyProvider );
        } catch( Fault ex ) {
           throw new WlsException( "Cannot register StrategyProvider.", ex );
        }
        
        return strategyProvider;
    }

    @Bean
    StrategyRouteGetterPlugin strategyRouteGetterPlugin( StrategyLocator strategyProvider ) {
        
        return new StrategyRouteGetterPlugin( strategyProvider.getStrategies() );
    }
    
    @Bean
    StrategyTriggerGetterPlugin strategyTriggerGetterPlugin( StrategyLocator strategyProvider ) {
        
        return new StrategyTriggerGetterPlugin( strategyProvider.getStrategies() );
    }

    @Bean
    StrategyGetterPlugin strategyGetterPlugin( StrategyLocator strategyProvider ) {
        
        return new StrategyGetterPlugin( strategyProvider.getStrategies() );
    }

    @Bean
    StrategyLocator strategyLocator( RoadMapPlugin roadMapPlugin, 
                                     RoadNetPlugin roadNetPlugin, 
                                     RoadManager roadManager, 
                                     StrategyProvider strategyProvider,
                                     @Value("${de.heuboe.geo.manager.strategyRouteLocationsDir}") String fileDirStrategyRoutes ) {
        
        StrategyLocator sl = new StrategyLocator( roadMapPlugin, 
                                                  roadNetPlugin, 
                                                  roadManager, 
                                                  strategyProvider.getRecords(), 
                                                  fileDirStrategyRoutes );
        sl.addLinearLocations();
        
        return sl;
    }
}
