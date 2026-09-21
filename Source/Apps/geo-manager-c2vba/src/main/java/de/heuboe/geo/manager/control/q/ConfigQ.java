package de.heuboe.geo.manager.control.q;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import de.heuboe.data.Data;
import de.heuboe.data.DataReader;
import de.heuboe.data.DataStore;
import de.heuboe.data.csv.CsvDataStore;
import de.heuboe.geo.manager.base.cfgsvc.ConfigServiceClient;
import de.heuboe.geo.manager.base.cfgsvc.GeoManagerConfigService;
import de.heuboe.geo.manager.base.config.ConfigBase;
import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;
import de.heuboe.wls.data.geofeature.DirectedGeoFeature;
import de.heuboe.wls.data.geofeature.GeoFeature;
import de.heuboe.wls.data.wls.CompassPoint;
import de.heuboe.wls.data.wls.Property;
import de.heuboe.wls.geofeature.GeoFeatureUtil;
import de.heuboe.wls.iface.Fault;
import de.heuboe.wls.plugin.roadmap.RoadMapPlugin;
import de.heuboe.wls.roadmap.RoadComponentBuilder;
import de.heuboe.wls.tmc.roadnet.plugin.TmcRoadnetPlugin;
import de.heuboe.wls.util.WlsException;
import de.heuboe.wls.utils.CoordinateTransformer;
import de.heuboe.wls.utils.GeometryTools;
import de.heuboe.wls.utils.IterableSource;
import de.heuboe.wls.utils.ListSource;
import eu.vmis_ehe.vmis2.configservice.CfgAq;
import eu.vmis_ehe.vmis2.configservice.CfgAq.AqType;

/**
 * 
 * Configuration 
 * 
 * @author peters
 *
 */
public class ConfigQ {

    @lombok.Data
    private static class AqLocation {
        private String id;
        private String type;
        private String roadNr;
        private String direction;   // 'S', 'N', 'W', 'O'
        private Double km = null;
        private Double latitude = null;
        private Double longitude = null;
        private Double bearing = null;
    }
    
    private static final Logger LOGGER = Logger.getLogger(ConfigQ.class);
    private static final String FAHRTRICHTUNG_PROPERTY = "FAHRTRICHTUNG";
    private static final String BABNR_PROPERTY = "BAB";
    private static final String BABKM_PROPERTY = "BABKM";
    private static final String WINKEL_PROPERTY = "WINKEL";
    private static final String BEARING_PROPERTY = "BEARING";
    private static final String AQTYPE_PROPERTY = "AQTYPE";
    
    
    private ConfigQ() {
    }
    
    
    /**
     * 
     * Creates infrastructure objects
     * 
     * @param suedFile                  Koordinaten Bayern-Nord
     * @param nordFile                  Koordinaten Bayern-Süd
     * @param aqLocationFile            Locations aus SDBBY
     * @param configService             GeoManagerConfigService
     * @param roadMapPlugin             RoadMapPlugin
     * @param tmcRoadnetPlugin          DirectedGeoFeature
     * @param roadComponentBuilder      DirectedGeoFeature
     * @return   DirectedGeoFeatures      
     * @throws WlsException             Error
     */
    public static List<DirectedGeoFeature> createGeoFeature( String suedFile,
                                                             String nordFile,
                                                             String aqLocationFile,
                                                             GeoManagerConfigService configService,
                                                             RoadMapPlugin roadMapPlugin,
                                                             TmcRoadnetPlugin  tmcRoadnetPlugin,
                                                             RoadComponentBuilder roadComponentBuilder ) throws WlsException {
        
        
        SystemConfiguration systemConfiguration = new SystemConfiguration( configService.getConfigServiceClient() );
        systemConfiguration.init();
        
        int wlsSrid = ConfigBase.getWlsSrid();
        
        IterableSource<DirectedGeoFeature> aqs = createGeoFeatureAQ( wlsSrid,    
                                                                     suedFile,
                                                                     roadMapPlugin,
                                                                     tmcRoadnetPlugin,
                                                                     roadComponentBuilder,
                                                                     systemConfiguration );
        
        Map<String,DirectedGeoFeature> gfsMap = new HashMap<>();
        aqs.init();
        while( aqs.hasNext() ) {
            DirectedGeoFeature dgf = aqs.next();
            gfsMap.put( dgf.getId(), dgf );
        }
        
        List<DirectedGeoFeature> gfs = applyAqsFromConfigService( configService.getConfigServiceClient(), aqLocationFile, gfsMap );
        
        IterableSource<DirectedGeoFeature> mqs = createGeoFeatureMQ( wlsSrid,    
                                                                     suedFile,
                                                                     nordFile,    
                                                                     roadMapPlugin,
                                                                     tmcRoadnetPlugin,
                                                                     roadComponentBuilder,
                                                                     systemConfiguration );

        mqs.init();
        while( mqs.hasNext() ) {
            gfs.add( mqs.next() );
        }
        
        IterableSource<DirectedGeoFeature> ufds = createGeoFeatureUFD( wlsSrid,    
                                                                       suedFile,
                                                                       nordFile,    
                                                                       roadMapPlugin,
                                                                       tmcRoadnetPlugin,
                                                                       roadComponentBuilder,
                                                                       systemConfiguration );

        ufds.init();
        while( ufds.hasNext() ) {
            gfs.add( ufds.next() );
        }
        
        return gfs;
    }
    
    private static IterableSource<DirectedGeoFeature> createGeoFeatureAQ( int wlsSrid,    // NOSONAR
                                                                         String suedFile,
                                                                         RoadMapPlugin roadMapPlugin,
                                                                         TmcRoadnetPlugin  tmcRoadnetPlugin,
                                                                         RoadComponentBuilder roadComponentBuilder,
                                                                         SystemConfiguration systemConfiguration ) throws WlsException {
        List<DirectedGeoFeature> qs = new ArrayList<>();
        
        QFileReader qfrSued = new QFileReader( wlsSrid, "FG4", "Sued", suedFile, 
                                               roadMapPlugin, tmcRoadnetPlugin, roadComponentBuilder, 
                                               systemConfiguration );
        qfrSued.init();
        qs.addAll( qfrSued.getFeatureMap().values() );
        systemConfiguration.addCfgKeys( qfrSued.getCfgKeys() );
        
        systemConfiguration.detectMissingCoordinateData( 4, true ); 
        
        return new ListSource<>( qs );
}

    private static IterableSource<DirectedGeoFeature> createGeoFeatureMQ( int wlsSrid,           // NOSONAR
                                                                         String suedFile,
                                                                         String nordFile,
                                                                         RoadMapPlugin roadMapPlugin,
                                                                         TmcRoadnetPlugin  tmcRoadnetPlugin,
                                                                         RoadComponentBuilder roadComponentBuilder,
                                                                         SystemConfiguration systemConfiguration ) throws WlsException {

        List<DirectedGeoFeature> qs = new ArrayList<>();
        
        systemConfiguration.clearCfgKeys();
        
        QFileReader qfrSued = new QFileReader( wlsSrid, "FG1", "Sued", suedFile, 
                                               roadMapPlugin, tmcRoadnetPlugin, roadComponentBuilder, 
                                               systemConfiguration );
        qfrSued.init();
        qs.addAll( qfrSued.getFeatureMap().values() );
        systemConfiguration.addCfgKeys( qfrSued.getCfgKeys() );
        
//        QFileReader qfrNord = new QFileReader( wlsSrid, "FG1", "Nord", nordFile, 
//        roadMapPlugin, tmcRoadnetPlugin, roadComponentBuilder, systemConfiguration );
//        qfrNord.init();
//        qs.addAll( qfrNord.getFeatureMap().values() );
//        systemConfiguration.addCfgKeys( qfrNord.getCfgKeys() );
        
        systemConfiguration.detectMissingCoordinateData( 1, true ); 
        
        LOGGER.info( "# MQ-GeoFeature: " + qs.size() );
        
        return new ListSource<>( qs );
    }

    private static IterableSource<DirectedGeoFeature> createGeoFeatureUFD( int wlsSrid,          // NOSONAR
                                                                          String suedFile,
                                                                          String nordFile,
                                                                          RoadMapPlugin roadMapPlugin,
                                                                          TmcRoadnetPlugin  tmcRoadnetPlugin,
                                                                          RoadComponentBuilder roadComponentBuilder,
                                                                          SystemConfiguration systemConfiguration ) throws WlsException {

        List<DirectedGeoFeature> qs = new ArrayList<>();
        
        systemConfiguration.clearCfgKeys();
        
        QFileReader qfrSued = new QFileReader( wlsSrid, 
                                                "FG3", 
                                                "Sued", 
                                                suedFile, 
                                                roadMapPlugin, 
                                                tmcRoadnetPlugin, 
                                                roadComponentBuilder, 
                                                systemConfiguration );
        qfrSued.init();
        qs.addAll( qfrSued.getFeatureMap().values() );
        systemConfiguration.addCfgKeys( qfrSued.getCfgKeys() );
        
//        QFileReader qfrNord = new QFileReader( wlsSrid, "FG3", "Nord", nordFile, roadMapPlugin, 
//                                               tmcRoadnetPlugin, roadComponentBuilder, systemConfiguration );
//        qfrNord.init();
//        qs.addAll( qfrNord.getFeatureMap().values() );
//        systemConfiguration.addCfgKeys( qfrNord.getCfgKeys() );
        
        /*List<Q> cfgObjectsWithoutCoordinates = */systemConfiguration.detectMissingCoordinateData( 3, false ); 
        
        //qfrNord.addMissingGeoFeatures( 3, cfgObjectsWithoutCoordinates, qs );
        
        LOGGER.info( "# UFD-GeoFeature: " + qs.size() );
        
        return new ListSource<>( qs.stream().collect( Collectors.toList() ) );
    }

    private static Map<String,AqLocation> readAqLocations( String aqLocationFile ) {
        
        LOGGER.info( "Read " +  aqLocationFile + " ... ");
        
        Map<String,AqLocation> id2AqLocation = new HashMap<>();
        
        Properties props = new Properties();
        props.setProperty( CsvDataStore.SEPARATOR_KEY, ";" );
        props.setProperty( CsvDataStore.CHARSET_KEY, StandardCharsets.ISO_8859_1.name() );
        DataStore store = new CsvDataStore(null, aqLocationFile, props);
        DataReader reader = store.getReader();
        int n = 0;
        try {
            while (reader.hasNext()) {
                n++;
                Data record = reader.next();
                
                AqLocation aqLocation = new AqLocation();
                aqLocation.setId( record.getMember("ID").getAsString() );
                aqLocation.setType( record.getMember("TYPE").getAsString() );
                aqLocation.setRoadNr( record.getMember("ROADNR").getAsString() );
                aqLocation.setDirection( record.getMember("FR").getAsString() );
                
                if( !record.getMember("KM").isNull() && !record.getMember("KM").getAsString().isEmpty() ) {
                    aqLocation.setKm( record.getMember("KM").getAsDouble() );
                }
                if( !record.getMember("LAT").isNull() && !record.getMember("LAT").getAsString().isEmpty() ) {
                    aqLocation.setLatitude( record.getMember("LAT").getAsDouble() );
                }
                if( !record.getMember("LON").isNull() && !record.getMember("LON").getAsString().isEmpty() ) {
                    aqLocation.setLongitude( record.getMember("LON").getAsDouble() );
                }
                if( !record.getMember(BEARING_PROPERTY).isNull() && !record.getMember(BEARING_PROPERTY).getAsString().isEmpty() ) {
                    aqLocation.setBearing( record.getMember(BEARING_PROPERTY).getAsDouble() );
                }
                
                id2AqLocation.put( aqLocation.getId(), aqLocation );
            }
            
            LOGGER.info( aqLocationFile + ": " + n + " records" );

            return id2AqLocation;
            
        } catch (Exception ex ) {
            LOGGER.error( ex.toString() );
            LOGGER.fatal( CallStack.getStackTraceAsString( ex ) );
            throw new IllegalArgumentException( "SdbbyFeatureSource.init: error reading AQ locations from " + 
                                                aqLocationFile + ", line " + n + ": " + ex.toString() );            
        }
    }
    
    

    private static List<DirectedGeoFeature> applyAqsFromConfigService( ConfigServiceClient configService,    // NOSONAR
                                                                       String aqLocationFile,
                                                                       Map<String,DirectedGeoFeature> id2AqGeoFeature ) {     
        
        List<DirectedGeoFeature> features = new ArrayList<>();
        
        Map<String,AqLocation> id2AqLocation = readAqLocations( aqLocationFile );
        
        int locFromFileCount = 0;
        
        List<CfgAq> cfgAqs = configService.getAllAQs( null );
        for( CfgAq aq : cfgAqs ) {
            
            String id = aq.getId();
            double bearing = -1.0;
            
            aq.getClusterIdsList();
            
            String name = aq.getName();
            List<Property> properties = new ArrayList<>();
            
            double lat = aq.getLocation().getLatitude();
            double lon = aq.getLocation().getLongitude();
            
            AqLocation aqLocation = id2AqLocation.get( id );
            if( aqLocation != null ) {
                
                locFromFileCount++;
                
                lat = aqLocation.getLatitude();
                lon = aqLocation.getLongitude();
                
                String t = aqLocation.getType(); 
                if( ( t != null ) && !t.isBlank() ) {
                    properties.add(new Property(AQTYPE_PROPERTY, t ));
                }
                
                String road = aqLocation.getRoadNr();
                if( ( road != null ) && !road.isBlank() ) {
                    properties.add(new Property(BABNR_PROPERTY, road ));
                } else {
                    road = aq.getLocation().getRoadId();
                    if( road != null ) {
                        properties.add(new Property(BABNR_PROPERTY, road));
                    }
                }
                
                String dir = aqLocation.getDirection();
                if( ( dir != null ) && !dir.isBlank() ) {
                    properties.add(new Property(FAHRTRICHTUNG_PROPERTY, dir ));
                }

                Double km = aqLocation.getKm();
                if( km != null ) {
                    properties.add(new Property(BABKM_PROPERTY , "" + km ));
                }
                
                Double b = aqLocation.getBearing();
                if( b != null ) {
                    bearing = b;
                    properties.add(new Property(WINKEL_PROPERTY , "" + b ));
                }

                GeoFeature gf = id2AqGeoFeature.get( id );
                if( gf != null ) {
                    if( road == null || road.isBlank() ) {
                        road = GeoFeatureUtil.getProperty( gf, "Road" );
                        if( ( road != null ) && !road.isBlank() ) {
                            properties.add(new Property(BABNR_PROPERTY, road ));
                        }
                    }
                    
                    if( km == null ) {
                        String kilometer = GeoFeatureUtil.getProperty( gf, "Kilometer" );
                        if( kilometer != null ) {
                            properties.add(new Property(BABKM_PROPERTY , "" + kilometer ));
                        }
                    }

                    if( ( dir == null ) || dir.isBlank() ) {
                        dir = GeoFeatureUtil.getProperty( gf, "RoadDir" );
                        if( ( dir != null ) && !dir.isBlank() ) {
                            properties.add(new Property(FAHRTRICHTUNG_PROPERTY, dir ));
                        }
                    }

                }
            } else {
                GeoFeature gf = id2AqGeoFeature.get( id );
                if( gf != null ) {
                    try {
                        Geometry wgs84Geom = GeometryTools.transformGeometry( gf.getGeometry(), 
                                                                              ConfigBase.getWlsSrid(), 
                                                                              CoordinateTransformer.WGS84_ID);
                        lon = wgs84Geom.getCoordinate().getY();
                        lat = wgs84Geom.getCoordinate().getX();

                        String r = GeoFeatureUtil.getProperty( gf, "Road" );
                        if( ( r != null ) && !r.isBlank() ) {
                            properties.add(new Property(BABNR_PROPERTY, r ));
                        } else {                        
                            String road = aq.getLocation().getRoadId();
                            if( road != null ) {
                                properties.add(new Property(BABNR_PROPERTY, road));
                            }
                        }
                        
                        AqType type = aq.getType();
                        if( type != null && !type.name().equals( "UNKNOWN" )) {
                            properties.add(new Property(AQTYPE_PROPERTY, type.name() ));
                        }
                        
                        String d = GeoFeatureUtil.getProperty( gf, "RoadDir" );
                        if( ( d != null ) && !d.isBlank() ) {
                            properties.add(new Property(FAHRTRICHTUNG_PROPERTY, d ));
                        }

                        
                        String b = GeoFeatureUtil.getProperty( gf, BEARING_PROPERTY );
                        if( b != null ) {
                            bearing = Double.parseDouble( b );
                            properties.add(new Property(WINKEL_PROPERTY , "" + b ));
                        }

                        String k = GeoFeatureUtil.getProperty( gf, "Kilometer" );
                        if( k != null ) {
                            properties.add(new Property(BABKM_PROPERTY , "" + k ));
                        }

                        
                    } catch (Fault ex ) {
                        LOGGER.warn("Error comverting coordinate " + gf.getGeometry().getCoordinate().toString() + ": "  +ex.toString() );
                    }
                } else {
                    AqType type = aq.getType();
                    if( type != null && !type.name().equals( "UNKNOWN" )) {
                        properties.add(new Property(AQTYPE_PROPERTY, type.name() ));
                    }
                    
                    String roadId = aq.getLocation().getRoadId();
                    if( roadId != null && !roadId.isBlank() ) {
                        properties.add(new Property(BABNR_PROPERTY, roadId ));
                    }
                }
            }
            
            if (!id.isEmpty() && lat > 0. && lon > 0.) {
                try {
                    Geometry geometry = GeometryTools.createPoint( CoordinateTransformer.WGS84_ID, new Coordinate( lat, lon ) );
                    geometry = GeometryTools.transformGeometry( geometry, 
                                                                CoordinateTransformer.WGS84_ID,
                                                                ConfigBase.getWlsSrid() );
                    
                    CompassPoint cp = GeometryTools.getCompassPoint( (int)Math.round( bearing ) );
                    
                    DirectedGeoFeature feature = new DirectedGeoFeature( id, 
                                                                         name, 
                                                                         geometry, 
                                                                         ConfigBase.getWlsSrid(), 
                                                                         "AQ",
                                                                         properties,
                                                                         bearing,
                                                                         cp );
                    features.add(feature);
                } catch (Fault ex ) {
                    LOGGER.warn("Error comverting WGS84 coordinates (" + lat + "," + lon + "): "  +ex.toString() );
                }
            } else {
                LOGGER.warn("No location (coordinates) for AQ " + id );
            }
            
        }
        
        LOGGER.info( locFromFileCount + " AQs found in previous configuration" );
        
        return features;
    }

}
