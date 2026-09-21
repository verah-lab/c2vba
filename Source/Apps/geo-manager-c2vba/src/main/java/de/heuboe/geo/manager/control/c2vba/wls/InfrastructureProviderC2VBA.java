package de.heuboe.geo.manager.control.c2vba.wls;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.heuboe.geo.manager.base.cfgsvc.GeoManagerConfigService;
import de.heuboe.geo.manager.control.q.ConfigQ;
import de.heuboe.geo.manager.map.importer.MapSupplyProviderImporter;
import de.heuboe.geo.manager.reader.infra.InfrastructureReaderConfigBase;
import de.heuboe.log.Logger;
import de.heuboe.wls.data.geofeature.DirectedGeoFeature;
import de.heuboe.wls.map.supply.MapDataVersion.Version;
import de.heuboe.wls.map.supply.MapSupply;
import de.heuboe.wls.map.supply.WlsSystem;
import de.heuboe.wls.plugin.roadmap.RoadMapPlugin;
import de.heuboe.wls.roadmap.RoadComponentBuilder;
import de.heuboe.wls.srv.impl.WlsEnvironment;
import de.heuboe.wls.tmc.roadnet.plugin.TmcRoadnetPlugin;
import de.heuboe.wls.util.WlsException;


/**
 * 
 * Replaces InfrastructureReaderConfig
 * 
 * @author peters
 *
 */
public class InfrastructureProviderC2VBA extends InfrastructureReaderConfigBase {    // NOSONAR

    private static final Logger LOGGER = Logger.getLogger(InfrastructureProviderC2VBA.class);
    
    private String intialSuedFile;
    private String initialNordFile; 
    private String aqLocationFile;
    private GeoManagerConfigService configService;
    private RoadMapPlugin initialRoadMapPlugin;
    private TmcRoadnetPlugin  initialTmcRoadnetPlugin;
    private RoadComponentBuilder initialRoadComponentBuilder;

    
    /**
     * 
     * Constructor
     * 
     * @param wlsEnvironment                WlsEnvironment
     * @param type                          ServiceInstanceManager
     * @param mapDataVersion                MapSupplyPartManager
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
    public InfrastructureProviderC2VBA( WlsEnvironment wlsEnvironment,     // NOSONAR
                                        String type, 
                                        Version mapDataVersion, 
                                        String suedFile,
                                        String nordFile, 
                                        String aqLocationFile, 
                                        GeoManagerConfigService configService,
                                        RoadMapPlugin roadMapPlugin,
                                        TmcRoadnetPlugin  tmcRoadnetPlugin,
                                        RoadComponentBuilder roadComponentBuilder )
            throws WlsException {
        super(wlsEnvironment, type, mapDataVersion);
        
        this.intialSuedFile = suedFile;
        this.initialNordFile = nordFile;
        this.aqLocationFile = aqLocationFile;
        this.configService = configService;
        this.initialRoadMapPlugin = roadMapPlugin;
        this.initialTmcRoadnetPlugin = tmcRoadnetPlugin;
        this.initialRoadComponentBuilder = roadComponentBuilder;
        
    }

    @Override
    public MapSupplyProviderImporter<DirectedGeoFeature> createImporter( WlsSystem wlsSystem,
                                                                         MapSupply mapSupply, 
                                                                         Version version,
                                                                         Map<String, String> props, 
                                                                         String supplySource) throws WlsException {
        
        
        RoadMapPlugin roadMapPlugin = wlsSystem.getSystemObject( RoadMapPlugin.class );
        TmcRoadnetPlugin  tmcRoadnetPlugin= wlsSystem.getSystemObject( TmcRoadnetPlugin.class );
        RoadComponentBuilder roadComponentBuilder = wlsSystem.getSystemObject( RoadComponentBuilder.class );
        
        String suedFile = props.get( "");
        String nordFile = props.get( "");
        
        return new InfrastructureProviderC2VBA(  wlsEnvironment, 
                type, 
                version, 
                suedFile,
                nordFile, 
                aqLocationFile, 
                configService,
                roadMapPlugin,
                tmcRoadnetPlugin,
                roadComponentBuilder );
    }
    
    @Override
    public MapSupplyProviderImporter<DirectedGeoFeature> createImporter( MapSupply mapSupply, 
                                                                         Version version,
                                                                         Map<String, String> props, 
                                                                         String supplySource) throws WlsException {
        String error = "Should not be called, use variant with WlsSystem parameter!"; 
        LOGGER.error( error );
        throw new WlsException( error );
    }


    @Override
    public String getMapSupplyErrorMsg() {
        return "Fehler beim Lesen der Infrastruktur-Objekte.";
    }

    @Override
    public boolean importMapData() {

        try {
            records = ConfigQ.createGeoFeature( intialSuedFile, 
                                                initialNordFile, 
                                                aqLocationFile,
                                                configService, 
                                                initialRoadMapPlugin, 
                                                initialTmcRoadnetPlugin, 
                                                initialRoadComponentBuilder );
        } catch (WlsException e) {
            return false;
        }
        return true;
    }

    @Override
    public String getRecordTypeName() {
        return "Betriebsmittel";
    }

    @Override
    public List<DirectedGeoFeature> getFeatures(String type) {
        return records.stream().filter( gf -> gf.getType().equals(type) ).collect(Collectors.toList() );
    }

    @Override
    public boolean roadLocated(String type) {
        return true;
    }
}
