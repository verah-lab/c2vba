package de.heuboe.geo.manager.control.config.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import de.heuboe.geo.manager.base.config.WlsMapSupplyProviderSource;
import de.heuboe.geo.manager.base.config.WlsPluginSource;
import de.heuboe.geo.manager.base.road.RoadComponentBuilderControl;
import de.heuboe.geo.manager.control.c2vba.strategy.StrategyGetterPlugin;
import de.heuboe.geo.manager.control.c2vba.strategy.StrategyProvider;
import de.heuboe.geo.manager.control.c2vba.strategy.StrategyRouteGetterPlugin;
import de.heuboe.geo.manager.control.c2vba.strategy.StrategyTriggerGetterPlugin;
import de.heuboe.geo.manager.dyn.plugin.GeoManagerGeoFeatureGetterPlugin;
import de.heuboe.geo.manager.dyn.plugin.ValiditySectionPlugin;
import de.heuboe.geo.manager.map.importer.KilometrageProvider;
import de.heuboe.geo.manager.map.importer.SegmentedAttrDataProvider;
import de.heuboe.geo.manager.map.importer.TmcCoordinateProvider;
import de.heuboe.geo.manager.map.importer.TmcTableProvider;
import de.heuboe.geo.manager.map.importer.TurnRestrictionProvider;
import de.heuboe.geo.manager.segment.SegmentPlugin;
import de.heuboe.wls.data.geofeature.DirectedGeoFeature;
import de.heuboe.wls.data.roadmap.MilePostSection;
import de.heuboe.wls.data.roadmap.RoadElement;
import de.heuboe.wls.geofeature.GeoFeatureGetterPlugin;
import de.heuboe.wls.map.supply.MapSupplyProvider;
import de.heuboe.wls.plugin.Plugin;
import de.heuboe.wls.plugin.linref.LinRefPrefixPartialPlugin;
import de.heuboe.wls.plugin.openlr.OpenLRPlugin;
import de.heuboe.wls.plugin.roadmap.RoadGeoFeatureConverterPlugin;
import de.heuboe.wls.plugin.roadmap.RoadMapPlugin;
import de.heuboe.wls.plugin.roadmap.RoadNetPlugin;
import de.heuboe.wls.roadmap.road.SubordinateRoadFeaturePlugin;
import de.heuboe.wls.roadmap.segmented.attr.HardShoulderData;
import de.heuboe.wls.tmc.plugin.DirectedTmcPlugin;
import de.heuboe.wls.tmc.plugin.TmcPlugin;
import de.heuboe.wls.tmc.roadnet.plugin.TmcRoadnetPlugin;

/**
 * 
 * Verwendete Plugins und MapSupplyProvider
 * 
 * @author peters
 *
 */
@Configuration
@Profile("WLS") 
public class ConfigControlModules {
    
    @Bean
    WlsMapSupplyProviderSource wlsMapSupplyProviderSource( MapSupplyProvider<RoadElement> roadMapProvider,  // NOSONAR
                                                           TurnRestrictionProvider turnRestrictionProvider,
                                                           @Autowired(required = false) MapSupplyProvider<MilePostSection> roadSectionProvider,
                                                           @Autowired(required = false) SegmentedAttrDataProvider<HardShoulderData> hardShoulderDataProvider,
                                                           TmcTableProvider tmcTableProvider,
                                                           TmcCoordinateProvider tmcCoordinateProvider,
                                                           MapSupplyProvider<DirectedGeoFeature> infrastructureReader,
                                                           KilometrageProvider kilometrageProvider,
                                                           StrategyProvider strategyProvider ) {
        return () -> {
            List<MapSupplyProvider<?>> msps = new ArrayList<>();
            msps.addAll( Arrays.asList( roadMapProvider, turnRestrictionProvider, tmcTableProvider, 
                                        tmcCoordinateProvider, infrastructureReader, kilometrageProvider,
                                        strategyProvider ) );      
            
            if( roadSectionProvider != null ) {
                msps.add( roadSectionProvider );
            }
            
            if( hardShoulderDataProvider !=null ) {
                msps.add( hardShoulderDataProvider );
            }

            return msps;
        };
    }
    
    @Bean 
    WlsPluginSource wlsPluginSource( RoadMapPlugin roadMapPlugin,    // NOSONAR
                                     RoadNetPlugin roadNetPlugin,
                                     LinRefPrefixPartialPlugin linRefPlugin,
                                     RoadComponentBuilderControl rcb,
                                    
                                     SubordinateRoadFeaturePlugin subordinateRoadFeaturePlugin,
                                     GeoFeatureGetterPlugin geoFeatureGetterPlugin,
                                     RoadGeoFeatureConverterPlugin roadGeoFeatureConverterPlugin,
                                     GeoManagerGeoFeatureGetterPlugin vmis2GeoFeatureSectionPlugin,
                                     SegmentPlugin segmentPlugin,
                                     StrategyRouteGetterPlugin strategyRoutePlugin,
                                     StrategyGetterPlugin strategyPlugin,
                                     StrategyTriggerGetterPlugin strategyTriggerPlugin,
                                     OpenLRPlugin openLRPlugin,
                                     @Autowired(required = false) TmcPlugin tmcPlugin,
                                     @Autowired(required = false) DirectedTmcPlugin directedTmcPlugin,
                                     @Autowired(required = false) TmcRoadnetPlugin tmcRoadnetPlugin,
                                     ValiditySectionPlugin validitySectionPlugin
 ) {
        return () -> {
            List<Plugin> plugins = new LinkedList<>(Arrays.asList(
                    roadMapPlugin, 
                    roadNetPlugin,
                    linRefPlugin,
                    rcb,
                    subordinateRoadFeaturePlugin,
                    geoFeatureGetterPlugin,
                    roadGeoFeatureConverterPlugin,
                    vmis2GeoFeatureSectionPlugin,
                    segmentPlugin,
                    strategyRoutePlugin,
                    strategyPlugin,
                    strategyTriggerPlugin,
                    validitySectionPlugin,
                    openLRPlugin
            ));
            
            if( tmcPlugin != null ) {
                plugins.add( tmcPlugin );
            }
            
            if( directedTmcPlugin != null ) {
                plugins.add( directedTmcPlugin );
            }

            if( tmcRoadnetPlugin != null ) {
                plugins.add( tmcRoadnetPlugin );
            }
            
            return plugins;    
        };
        
    }

    
    
    
}
