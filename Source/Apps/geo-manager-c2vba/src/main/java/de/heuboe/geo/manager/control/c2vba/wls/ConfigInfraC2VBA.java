package de.heuboe.geo.manager.control.c2vba.wls;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import de.heuboe.geo.manager.base.cfgsvc.GeoManagerConfigService;
import de.heuboe.geo.manager.base.map.update.MapSupplyProviderUtil;
import de.heuboe.geo.manager.base.system.ServiceInstanceManager;
import de.heuboe.geo.manager.config.infra.ConfigInfraCfg;
import de.heuboe.geo.manager.map.MapSupplyProviderBaseTypes;
import de.heuboe.log.Logger;
import de.heuboe.wls.iface.Fault;
import de.heuboe.wls.map.supply.MapDataVersion;
import de.heuboe.wls.map.supply.MapDataVersion.Version;
import de.heuboe.wls.map.supply.MapSupply;
import de.heuboe.wls.map.supply.MapSupplyPartManager;
import de.heuboe.wls.plugin.roadmap.RoadMapPlugin;
import de.heuboe.wls.roadmap.RoadComponentBuilder;
import de.heuboe.wls.srv.impl.WlsEnvironment;
import de.heuboe.wls.tmc.roadnet.plugin.TmcRoadnetPlugin;
import de.heuboe.wls.util.WlsException;

/**
 * 
 * Infrastructure configuration
 * 
 * @author peters
 *
 */
@Configuration
@Profile("WLS") 
public class ConfigInfraC2VBA {
    
    private static final Logger LOGGER = Logger.getLogger(ConfigInfraC2VBA.class);
    
    private MapSupplyProviderUtil readerUtil = new MapSupplyProviderUtil();

    
    /**
     * Create bean ConfigInfrastructureReader
     *
     * @param mapSupply     one map with its components
     * @param configService GeoManagerConfigService
     * @return instance of InfrastructureReader
     * @throws Exception in case of error
     */
    
    
    /**
     * 
     * Create bean ConfigInfrastructureReader
     *      
     * @param wlsEnvironment                WlsEnvironment
     * @param serviceInstanceManager        ServiceInstanceManager
     * @param mapSupplyPartManager          MapSupplyPartManager
     * @param mapSupply                     Map supply
     * @param suedFile                      Coordinates of south Bavarian objects
     * @param nordFile                      Coordinates of north Bavarian objects
     * @param aqLocationFile                AQ locations from SDBBY system   
     * @param configService                 GeoManagerConfigService
     * @param roadMapPlugin                 RoadMapPlugin
     * @param tmcRoadnetPlugin              TmcRoadnetPlugin
     * @param roadComponentBuilder          RoadComponentBuilder
     * @return                              InfrastructureProvider
     * @throws WlsException                 Error
     */
    @Bean
    InfrastructureProviderC2VBA infrastructureReaderConfig( WlsEnvironment wlsEnvironment,     // NOSONAR
                                                            ServiceInstanceManager serviceInstanceManager,
                                                            MapSupplyPartManager mapSupplyPartManager,
                                                            MapSupply mapSupply, 
                                                            @Value("${q.sued.filename}") String suedFile,
                                                            @Value("${q.nord.filename}") String nordFile, 
                                                            @Value("${aq.location.file}") String aqLocationFile,
                                                            GeoManagerConfigService configService,
                                                            RoadMapPlugin roadMapPlugin,
                                                            TmcRoadnetPlugin  tmcRoadnetPlugin,
                                                            RoadComponentBuilder roadComponentBuilder ) throws WlsException {
        
        String instanceType = serviceInstanceManager.getInstanceType().name();
        String v = ConfigInfraCfg.getInitialCfgVersion( instanceType );
        Version version = ( mapSupply != null ) ? 
                         mapSupply.getVersion( MapSupplyProviderBaseTypes.INFRASTRUCTURE ) : 
                             new MapDataVersion.Version( v, "1" ); 
                         
        InfrastructureProviderC2VBA ispc = new InfrastructureProviderC2VBA( wlsEnvironment, MapSupplyProviderBaseTypes.INFRASTRUCTURE, version,
                                                suedFile,
                                                nordFile, 
                                                aqLocationFile,
                                                configService,
                                                roadMapPlugin,
                                                tmcRoadnetPlugin,
                                                roadComponentBuilder );
        
        
        if( !ispc.initMapData() ) {
            readerUtil.throwError( ispc );
        }
        
        LOGGER.info( "InfrastructureReaderConfig created");
        
        try {
            mapSupplyPartManager.registerProvider( ispc );
        } catch( Fault ex ) {
           throw new WlsException( "Cannot register InfrastructureProviderC2VBA.", ex );
        }
        
        return ispc;

    }
 
}
