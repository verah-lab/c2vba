package de.heuboe.geo.manager.control.config.module;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import de.heuboe.geo.manager.base.config.ConfigRoad;
import de.heuboe.geo.manager.base.config.ConfigSubordinateRoad;
import de.heuboe.geo.manager.base.config.ConfigTmc;
import de.heuboe.geo.manager.base.config.Properties;
import de.heuboe.geo.manager.base.system.SystemStateProvider;
import de.heuboe.geo.manager.config.infra.ConfigInfraCfg;
import de.heuboe.geo.manager.config.infra.ConfigInfraFile;
import de.heuboe.geo.manager.config.road.ConfigRoadProvider;
import de.heuboe.geo.manager.config.tmc.ConfigTmcProvider;
import de.heuboe.geo.manager.control.Main;
import de.heuboe.geo.manager.control.c2vba.wls.ConfigInfraC2VBA;
import de.heuboe.geo.manager.control.c2vba.wls.ConfigRoadC2VBA;
import de.heuboe.geo.manager.control.c2vba.wls.ConfigStrategyC2VBA;
import de.heuboe.geo.manager.control.config.wls.Config;
import de.heuboe.geo.manager.control.config.wls.ConfigControl;
import de.heuboe.geo.manager.control.config.wls.ConfigRoadControl;
import de.heuboe.geo.manager.control.config.wls.ConfigTmcControl;
import de.heuboe.geo.manager.plugin.ConfigPlugin;
import de.heuboe.wls.srv.manager.WebLocationServiceManager;

/**
 * 
 * Spring configuration
 * 
 * @author peters
 *
 */
@Configuration
@ComponentScan( basePackages = { "de.heuboe.geo.manager.control.c2vba.wls" } )               
public class ConfigControlWlsm {
    
    @Bean
    SystemStateProvider systemStateProvider() {
        return () -> Main.running;
    }
    

    @Bean 
    WebLocationServiceManager webLocationServiceManager( Properties props ) {
        
        WebLocationServiceManager webLocationServiceManager = new WebLocationServiceManager( props.getWlsPort(), props.getJaxbContext() );
        
        Class<?>[] cfgClasses = { Config.class, 
                                  ConfigControl.class,
                                  ConfigRoad.class,
                                  ConfigRoadProvider.class,
                                  ConfigRoadControl.class,
                                  ConfigSubordinateRoad.class,
                                  ConfigInfraCfg.class, 
                                  ConfigInfraFile.class, 
                                  ConfigTmc.class,
                                  ConfigTmcProvider.class,
                                  ConfigTmcControl.class,
                                  ConfigPlugin.class,
                                  ConfigRoadC2VBA.class,
                                  ConfigInfraC2VBA.class,
                                  ConfigStrategyC2VBA.class,
                                  ConfigControlWlsm.class,
                                  ConfigControlModules.class };
        
        webLocationServiceManager.setSpringCfgClasses( cfgClasses );
        
        return webLocationServiceManager;
    }

}
