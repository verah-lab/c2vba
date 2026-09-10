package de.heuboe.wls.by.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import com.google.common.collect.Lists;

import de.heuboe.log.Logger;
import de.heuboe.wls.HERERoadmap;
import de.heuboe.wls.by.Main;
import de.heuboe.wls.by.MultiGetterPluginIgnoreBbox;
import de.heuboe.wls.by.QFileReader;
import de.heuboe.wls.by.SdbbyFeatureSource;
import de.heuboe.wls.by.StrategyGetterPlugin;
import de.heuboe.wls.by.TccPointFeaturePlugin;
import de.heuboe.wls.by.WlsGeoServerCfg;
import de.heuboe.wls.by.cfg.ConfigService;
import de.heuboe.wls.by.cfg.Q;
import de.heuboe.wls.by.cfg.SystemConfiguration;
import de.heuboe.wls.data.geofeature.DirectedGeoFeature;
import de.heuboe.wls.data.geofeature.GeoFeature;
import de.heuboe.wls.data.roadmap.RoadComponent;
import de.heuboe.wls.data.roadmap.RoadElement;
import de.heuboe.wls.data.sdbby.SdbbyRoute;
import de.heuboe.wls.data.sdbby.SdbbyStrategy;
import de.heuboe.wls.data.wls.GeoLocation;
import de.heuboe.wls.data.wls.LinearLocation;
import de.heuboe.wls.geofeature.GeoFeatureCache;
import de.heuboe.wls.geofeature.GeoFeatureGetterPlugin;
import de.heuboe.wls.geofeature.GeoFeatureSource;
import de.heuboe.wls.iface.WebLocationServer;
import de.heuboe.wls.persistence.PersistenceManager;
import de.heuboe.wls.plugin.openlr.OpenLRPlugin;
import de.heuboe.wls.plugin.roadmap.RoadComponentBuilderConnect;
import de.heuboe.wls.plugin.roadmap.RoadMapPlugin;
import de.heuboe.wls.plugin.roadmap.RoadNetPlugin;
import de.heuboe.wls.roadmap.LocationCreator;
import de.heuboe.wls.roadmap.RoadComponentBuilder;
import de.heuboe.wls.roadmap.RoadComponentBuilderStd;
import de.heuboe.wls.roadmap.RoadNetCache;
import de.heuboe.wls.srv.impl.WebLocationService;
import de.heuboe.wls.tmc.DirectedTmcTableCache;
import de.heuboe.wls.tmc.TmcFilter;
import de.heuboe.wls.tmc.TmcTableCache;
import de.heuboe.wls.tmc.csv.CsvTmcTable;
import de.heuboe.wls.tmc.plugin.DirectedTmcPlugin;
import de.heuboe.wls.tmc.plugin.TmcPlugin;
import de.heuboe.wls.tmc.roadnet.TmcRoadSegmentCache;
import de.heuboe.wls.tmc.roadnet.TmcRoadSegmentDirectedRoadsBuilder;
import de.heuboe.wls.tmc.roadnet.TmcRoadSegmentDirectedRoadsBuilder.TmcRoadProvider;
import de.heuboe.wls.tmc.roadnet.TmcRoadSegmentFactory;
import de.heuboe.wls.tmc.roadnet.TmcRoadSegmentSource;
import de.heuboe.wls.tmc.roadnet.plugin.TmcRoadnetPlugin;
import de.heuboe.wls.util.WlsException;
import de.heuboe.wls.util.WlsWebServiceClient;
import de.heuboe.wls.utils.IterableSource;
import de.heuboe.wls.utils.ListSource;
import de.heuboe.wls.utils.MultiGetterPlugin;

/**
 * 
 * Configuration
 * 
 * @author peters
 *
 */
@Configuration
@ImportResource("classpath:META-INF/cxf/cxf.xml")
public class Config {
    
	private static final Logger LOGGER = Logger.getLogger(Config.class);
	
    @Bean
    SystemStateProvider systemStateProvider() {
        return () -> Main.running;
    }
    
	@Bean(initMethod = "init")
	ConfigService configService() {
		return new ConfigService();
	}
    
    @Bean
    HERERoadmap roadMap( 
            @Value("${wls.srid}") int wlsSrid,
            @Value("${map.srid}") int sourceSrid,
            @Value("${map.pathname}") String filename ) {
    	HERERoadmap irm = new HERERoadmap();
        irm.setWlsSrid( wlsSrid );
        irm.setSourceSrid(sourceSrid  );
        irm.setFilename( filename );
        return irm;
    }
    
    
    @Bean(initMethod = "init")
    RoadNetCache roadNetCache( @Value("${wls.srid:0}") int wlsSrid,
    						   HERERoadmap roadMap ) {
        RoadNetCache rnc = new RoadNetCache( wlsSrid );
        
        List<RoadElement> res = new ArrayList<>();
        Iterable<RoadElement> source = roadMap;
        Iterator<RoadElement> iter = source.iterator();
        while( iter.hasNext() ) {
            RoadElement re = iter.next();
            List<String> rns = re.getRouteNums();
            List<String> nrns = new ArrayList<>();
            for( String rn : rns ) {
                nrns.add( rn );
                if( rn.equals( "B15 NEU" ) ) {
                    nrns.add( "B15n" );
                }
                if( rn.equals( "B2R" ) ) {
                    nrns.add( "B2r" );
                }
            }
            rns.clear();
            rns.addAll( nrns );
            
            res.add( re );
        }
        
        rnc.setSource( res );
        return rnc;
    }

    @Bean
    RoadMapPlugin roadMapPlugin( RoadNetCache roadNetCache ) {
        
        RoadMapPlugin rmp = new RoadMapPlugin();
        rmp.setRoadMap( roadNetCache );
        
        return rmp;
    }

    @Bean
    RoadNetPlugin roadNetPlugin(RoadNetCache roadNetCache, RoadComponentBuilderStd roadComponentBuilder) {
        RoadNetPlugin roadNetPlugin = new RoadNetPlugin();
        roadNetPlugin.setRoadNet(roadNetCache);
        roadNetPlugin.setRoadComponentBuilder(roadComponentBuilder);
        return roadNetPlugin;
    }
    
    public static IterableSource<GeoFeature> createGeoFeatureAQ( int wlsSrid,    // NOSONAR
            													 String suedFile,
            													 RoadMapPlugin roadMapPlugin,
            													 TmcRoadnetPlugin  tmcRoadnetPlugin,
            													 RoadComponentBuilder roadComponentBuilder,
            													 SystemConfiguration systemConfiguration ) throws WlsException {
        List<GeoFeature> qs = new ArrayList<>();

        QFileReader qfrSued = new QFileReader( wlsSrid, "FG4", "Sued", suedFile, 
                                               roadMapPlugin, tmcRoadnetPlugin, roadComponentBuilder, systemConfiguration );
        qfrSued.init();
        qs.addAll( qfrSued.getFeatureMap().values() );
        systemConfiguration.addCfgKeys( qfrSued.getCfgKeys() );

        systemConfiguration.detectMissingCoordinateData( 4, true ); 
        
        return new ListSource<>( qs );
    }

    @Bean 
    @Qualifier("AQ")
    IterableSource<GeoFeature> qFileReaderAQ( @Value("${wls.srid}") int wlsSrid, 
                                              @Value("${q.sued.filename}") String suedFile,
                                              RoadMapPlugin roadMapPlugin,
                                              TmcRoadnetPlugin  tmcRoadnetPlugin,
                                              RoadComponentBuilder roadComponentBuilder,
                                              SystemConfiguration systemConfiguration ) throws WlsException {
    	
    	return createGeoFeatureAQ( wlsSrid, 
								   suedFile,
								   roadMapPlugin,
								   tmcRoadnetPlugin,
								   roadComponentBuilder,
								   systemConfiguration );
    }
    
    
    public static IterableSource<GeoFeature> createGeoFeatureMQ( int wlsSrid, 			// NOSONAR
													   		     String suedFile,
													   		     String nordFile,
													   		     RoadMapPlugin roadMapPlugin,
													   		     TmcRoadnetPlugin  tmcRoadnetPlugin,
													   		     RoadComponentBuilder roadComponentBuilder,
													   		     SystemConfiguration systemConfiguration ) throws WlsException {
    	
        List<GeoFeature> qs = new ArrayList<>();
        
        systemConfiguration.clearCfgKeys();

        QFileReader qfrSued = new QFileReader( wlsSrid, "FG1", "Sued", suedFile, 
                                               roadMapPlugin, tmcRoadnetPlugin, roadComponentBuilder, systemConfiguration );
        qfrSued.init();
        qs.addAll( qfrSued.getFeatureMap().values() );
        systemConfiguration.addCfgKeys( qfrSued.getCfgKeys() );
        
        QFileReader qfrNord = new QFileReader( wlsSrid, "FG1", "Nord", nordFile, 
                                               roadMapPlugin, tmcRoadnetPlugin, roadComponentBuilder, systemConfiguration );
        qfrNord.init();
        qs.addAll( qfrNord.getFeatureMap().values() );
        systemConfiguration.addCfgKeys( qfrNord.getCfgKeys() );
        
        systemConfiguration.detectMissingCoordinateData( 1, true ); 
        
        LOGGER.info( "# MQ-GeoFeature: " + qs.size() );
        
        return new ListSource<>( qs );
    }
    
    @Bean 
    @Qualifier("MQ")
    IterableSource<GeoFeature> qFileReaderMQ( @Value("${wls.srid}") int wlsSrid, 
                                              @Value("${q.sued.filename}") String suedFile,
                                              @Value("${q.nord.filename}") String nordFile,
                                              RoadMapPlugin roadMapPlugin,
                                              TmcRoadnetPlugin  tmcRoadnetPlugin,
                                              RoadComponentBuilder roadComponentBuilder,
                                              SystemConfiguration systemConfiguration ) throws WlsException {
        
    	return createGeoFeatureMQ( wlsSrid, 
					               suedFile,
					               nordFile,
					               roadMapPlugin,
					               tmcRoadnetPlugin,
					               roadComponentBuilder,
					               systemConfiguration );
    }
    
    public static IterableSource<GeoFeature> createGeoFeatureUDF( int wlsSrid,  		// NOSONAR
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
        
        QFileReader qfrNord = new QFileReader( wlsSrid, "FG3", "Nord", nordFile, roadMapPlugin, 
                                               tmcRoadnetPlugin, roadComponentBuilder, systemConfiguration );
        qfrNord.init();
        qs.addAll( qfrNord.getFeatureMap().values() );
        
        systemConfiguration.addCfgKeys( qfrNord.getCfgKeys() );
        List<Q> cfgObjectsWithoutCoordinates = systemConfiguration.detectMissingCoordinateData( 3, false ); 

        qfrNord.addMissingGeoFeatures( 3, cfgObjectsWithoutCoordinates, qs );
        
        LOGGER.info( "# UFD-GeoFeature: " + qs.size() );
        
        return new ListSource<>( qs.stream().collect( Collectors.toList() ) );
    }
    
    @Bean 
    @Qualifier("UFD")
    IterableSource<GeoFeature> qFileReaderUFD( @Qualifier("MQ") IterableSource<GeoFeature> qFileReaderMQ,    // Ensures order of execution
    		                                   @Value("${wls.srid}") int wlsSrid, 
                                               @Value("${q.sued.filename}") String suedFile,
                                               @Value("${q.nord.filename}") String nordFile,
                                               RoadMapPlugin roadMapPlugin,
                                               TmcRoadnetPlugin  tmcRoadnetPlugin,
                                               RoadComponentBuilder roadComponentBuilder,
                                               SystemConfiguration systemConfiguration ) throws WlsException {
        return createGeoFeatureUDF( wlsSrid, 
					                suedFile,
					                nordFile,
					                roadMapPlugin,
					                tmcRoadnetPlugin,
					                roadComponentBuilder,
					                systemConfiguration );
    }
    
    
    @Bean 
    @Qualifier("SdbbyRoute")
    IterableSource<GeoFeature> routeSource( SdbbyFeatureSource sdbbyFeatureSource, MultiGetterPlugin sdbbyFeaturePlugin  ) {
        
        List<SdbbyStrategy> strategies = sdbbyFeatureSource.getStrategies();
        List<GeoFeature> routeLocs = new ArrayList<>();
        for( SdbbyStrategy strategy : strategies ) {
            for( SdbbyRoute route : strategy.getRoutes() ) {
                GeoFeature gf = new GeoFeature( route.getId(), route.getName(),
                                                route.getGeometry(), route.getSrid(), 
                                                "SdbbyRoute", new ArrayList<>() );
                routeLocs.add( gf );
            }
        }
        
        return new ListSource<>( routeLocs );
    }
    
    @Bean 
    @Qualifier("SdbbyTrigger")
    IterableSource<GeoFeature> routeTrigger( SdbbyFeatureSource sdbbyFeatureSource, MultiGetterPlugin sdbbyFeaturePlugin  ) {
        
        List<SdbbyStrategy> strategies = sdbbyFeatureSource.getStrategies();
        List<GeoFeature> routeTrigger = new ArrayList<>();
        for( SdbbyStrategy strategy : strategies ) {
            GeoFeature gf = new GeoFeature( strategy.getId(), 
            		                        strategy.getName(),
            							    ((GeoLocation)strategy.getTrigger()).getGeometry(), 
            							    ((GeoLocation)strategy.getTrigger()).getSrid(), 
                                            "SdbbyTrigger", 
                                            new ArrayList<>() );
            routeTrigger.add( gf );
        }
        
        return new ListSource<>( routeTrigger );
    }


    @Bean(initMethod = "init")
    StrategyGetterPlugin strategyGetterPlugin( SdbbyFeatureSource sdbbyFeatureSource ) {
        
        StrategyGetterPlugin strategyGetterPlugin = new StrategyGetterPlugin();
        strategyGetterPlugin.setSource( sdbbyFeatureSource );
        return strategyGetterPlugin;
    }
    
        
    
    @Bean(initMethod = "init")
    GeoFeatureSource geoFeatureSource( @Qualifier("MQ") IterableSource<GeoFeature> mqSource,
                                       @Qualifier("UFD") IterableSource<GeoFeature> ufdSource,
                                       SystemConfiguration systemConfiguration,
                                       //@Value("${cfg.check.nonCfgKeysFile:}") String nonCfgKeysFile,
                                       //@Value("${cfg.check.cfgWancomStatisticsFile:}") String cfgWancomStatisticsFile,
                                       @Qualifier("SdbbyRoute") IterableSource<GeoFeature> routeSource,
                                       @Qualifier("SdbbyTrigger") IterableSource<GeoFeature> routeTrigger ) {
        GeoFeatureSource geoFeatureSource = new GeoFeatureSource();
        
        List<IterableSource<GeoFeature>> readers = Arrays.asList( mqSource, ufdSource, routeSource, routeTrigger );
        geoFeatureSource.setReaders(readers);
        return geoFeatureSource;
    }
    
    @Bean
    GeoFeatureCache geoFeatureCache(GeoFeatureSource geoFeatureSource) {
        GeoFeatureCache cache = new GeoFeatureCache();
        cache.setSource(geoFeatureSource);
        return cache;
    }
    
    @Bean
    GeoFeatureGetterPlugin geoFeatureGetterPlugin(GeoFeatureCache geoFeatureCache) {
        GeoFeatureGetterPlugin plugin = new GeoFeatureGetterPlugin();
        plugin.setLocationCache(geoFeatureCache);
        return plugin;
    }

    
    @Bean
    SdbbyFeatureSource sdbbyFeatureSource( 
            @Value("${vms.filename}") String filenameAq,
            @Value("${www.filename}") String filenameWww,
            @Value("${sdb.filename}") String filenameStrg,
            @Value("${sdb.route.fileDir}") String fileDirStrategyRoutes,
            @Value("${aq.location.file}") String aqLocationFile,
            @Value("${wls.srid}") int srid,
            RoadMapPlugin roadMapPlugin,
            RoadNetPlugin roadNetPlugin,
            SystemConfiguration systemConfiguration,
            @Qualifier("AQ") IterableSource<GeoFeature> qFileReaderAQ ) {
        SdbbyFeatureSource sdbbyFeatureSource = new SdbbyFeatureSource();
        sdbbyFeatureSource.setFilenameAq( filenameAq );
        sdbbyFeatureSource.setFilenameWww( filenameWww );
        sdbbyFeatureSource.setFilenameStrg( filenameStrg );
        sdbbyFeatureSource.setFileDirStrategyRoutes( fileDirStrategyRoutes );
        sdbbyFeatureSource.setAqLocationFile( aqLocationFile );
        sdbbyFeatureSource.setSrid( srid );
        sdbbyFeatureSource.setRoadMapPlugin( roadMapPlugin );
        sdbbyFeatureSource.setRoadNetPlugin( roadNetPlugin );
        sdbbyFeatureSource.setSystemConfiguration( systemConfiguration );
        
        qFileReaderAQ.init();
        sdbbyFeatureSource.setAqGeoFeatures( Lists.newArrayList( (Iterable<GeoFeature>) qFileReaderAQ ) );
        
        return sdbbyFeatureSource;
    }

    @Bean
    MultiGetterPluginIgnoreBbox sdbbyFeaturePlugin( SdbbyFeatureSource sdbbyFeatureSource ) {
        
        MultiGetterPluginIgnoreBbox sdbbyFeaturePlugin = new MultiGetterPluginIgnoreBbox();
        sdbbyFeaturePlugin.setSource( sdbbyFeatureSource );
        return sdbbyFeaturePlugin;
    }
    
    @Bean(initMethod = "init")
    TccPointFeaturePlugin tccPointFeaturePlugin( SdbbyFeatureSource sdbbyFeatureSource ) {
        
        TccPointFeaturePlugin tccPointFeaturePlugin = new TccPointFeaturePlugin();
        tccPointFeaturePlugin.setSource( sdbbyFeatureSource );
        return tccPointFeaturePlugin;
    }
 
    @Bean(initMethod = "init")
    OpenLRPlugin openLrPlugin( 
            @Value("${wls.srid}") int srid,
            @Value("${openLR.encoder.config:openLR-encoder.xml}") String encoderConfig,
            @Value("${openLR.decoder.config:openLR-decoder.xml}") String decoderConfig,
            RoadNetCache roadNetCache ) {
        OpenLRPlugin openLrPlugin = new OpenLRPlugin();
        
        openLrPlugin.setWlsSrid( srid );
        openLrPlugin.setEncoderConfig( encoderConfig );
        openLrPlugin.setDecoderConfig( decoderConfig );
        openLrPlugin.setRoadMap( roadNetCache );
        
        return openLrPlugin;
    }
    
    @Bean(initMethod = "init")
    PersistenceManager wlsPersistenceManager( 
            @Value("${wls.system.version}") String wlsVersion,
            @Value("${wls.mongodb.uri}") String connectionUri ) {
        PersistenceManager wlsPersistenceManager = new PersistenceManager();
        
        wlsPersistenceManager.setWlsVersion( wlsVersion );
        wlsPersistenceManager.setConnectionUri( connectionUri );
        
        return wlsPersistenceManager;
    }
    
    @Bean
    CsvTmcTable tmcTable( 
            @Value("${wls.srid:0}") int wlsSrid,
            @Value("${tmc.srid:4326}") int tmcSrid,
            @Value("${tmc.filename:}") String tmcFileName,
            @Value("${tmc.version:}") String tmcVersion ) {
        CsvTmcTable tmcTable = new CsvTmcTable();
        
        tmcTable.setWlsSrid( wlsSrid );
        tmcTable.setSourceSrid( tmcSrid );
        tmcTable.setFilename( tmcFileName );
        tmcTable.setVersion( tmcVersion );
        tmcTable.setPatchByStaatsstrasseRouteNum( true );
        
        tmcTable.setCoordinateDivisor( 1.0 );

        
        return tmcTable;
    }

    @Bean(initMethod = "init")
    TmcTableCache tmcTableCache( CsvTmcTable tmcTable ) {
        
        TmcTableCache tmcTableCache = new TmcTableCache();
        tmcTableCache.setSource( tmcTable );
        return tmcTableCache;
    }
    
    @Bean
    TmcPlugin tmcPlugin( TmcTableCache tmcTableCache ) {
        
        TmcPlugin tmcPlugin = new TmcPlugin();
        tmcPlugin.setLocationCache( tmcTableCache );
        return tmcPlugin;
    }
    
    @Bean(initMethod = "init")
    DirectedTmcTableCache directedTmcTableCache( TmcTableCache tmcTableCache ) {
            
        return new DirectedTmcTableCache( tmcTableCache );
    }
    
    @Bean
    DirectedTmcPlugin directedTmcPlugin( DirectedTmcTableCache directedTmcTableCache ) {
        DirectedTmcPlugin directedTmcPlugin = new DirectedTmcPlugin();
        directedTmcPlugin.setLocationCache( directedTmcTableCache );
        return directedTmcPlugin;
    }
    
    class RoadProvider implements TmcRoadProvider {
        
        private RoadComponentBuilderConnect rcb;
        private Map<String,Map<String,LinearLocation> > routeNum2RoadComponents = new HashMap<>();
        
        RoadProvider( RoadComponentBuilderConnect rcb ) {
            this.rcb = rcb;
            
            for( RoadComponent rc : rcb.getRoadComponents().values() ) {
                String rn = rc.getRouteNum();
                if( rn != null ) {
                    routeNum2RoadComponents.computeIfAbsent( rn, r -> new HashMap<>() ).put( rc.getId(), rc.getLocation() );
                }
            }
        }

        @Override
        public Map<String, LinearLocation> getRoadComponents(String routeNum) {
            return routeNum2RoadComponents.get( routeNum );
        }

        @Override
        public Geometry getRoadComponentGeometry(String rcId) {
            return rcb.getGeometry( rcId );
        }

        @Override
        public Set<String> getRouteNums() {
            return routeNum2RoadComponents.keySet();
        }
    }

    @Bean
    TmcRoadSegmentFactory tmcRoadSegmentFactory(     // NOSONAR
                                TmcTableCache tmcTableCache, 
                                RoadNetCache roadNetCache, 
                                PersistenceManager wlsPersistenceManager,
                                TmcFilter tmcFilter,
                                @Value("${maxPointLocMatches:12}") int maxPointLocMatches,
                                @Value("${maxPointLocDistance:150}") int maxPointLocDistance,
                                RoadComponentBuilderConnect rcb  ) {
        
        
        LocationCreator lc = new LocationCreator( roadNetCache );
        TmcRoadSegmentDirectedRoadsBuilder trsdrb = new TmcRoadSegmentDirectedRoadsBuilder( tmcTableCache, new RoadProvider( rcb ), lc );
        
        TmcRoadSegmentFactory tmcRoadSegmentFactory = new TmcRoadSegmentFactory();
        tmcRoadSegmentFactory.setTmcTable(tmcTableCache);
        tmcRoadSegmentFactory.setRoadNet(roadNetCache);
        
        tmcRoadSegmentFactory.setPersistenceManager(wlsPersistenceManager);
        tmcRoadSegmentFactory.setTmcFilter(tmcFilter);
        tmcRoadSegmentFactory.setMaxPointLocMatches( maxPointLocMatches );
        tmcRoadSegmentFactory.setMaxPointLocDistance( maxPointLocDistance );
        tmcRoadSegmentFactory.setWithoutJunctions(true);
        //tmcRoadSegmentFactory.setMaxSegLenFactor( 10 );
        
        tmcRoadSegmentFactory.setTmcRoadSegmentBuilder( trsdrb );
        
        return tmcRoadSegmentFactory;
    }

  
    @Bean
    TmcRoadSegmentSource tmcRoadSegmentSource( TmcRoadSegmentFactory tmcRoadSegmentFactory ) {
        
        TmcRoadSegmentSource tmcRoadSegmentSource = new TmcRoadSegmentSource();
        tmcRoadSegmentSource.setFactory( tmcRoadSegmentFactory );
        return tmcRoadSegmentSource;
    }
    
    @Bean(initMethod = "init")
    TmcRoadSegmentCache tmcRoadSegmentCache( RoadMapPlugin roadMapPlugin, 
                                              TmcRoadSegmentSource tmcRoadSegmentSource ) {
        
        return new TmcRoadSegmentCache( roadMapPlugin, tmcRoadSegmentSource );
    }
    
    @Bean(initMethod = "init")
    TmcRoadnetPlugin tmcRoadnetPlugin( 
            TmcRoadSegmentCache tmcRoadSegmentCache,
            RoadNetCache roadNetCache,
            TmcTableCache  tmcTable ) {
        
        TmcRoadnetPlugin tmcRoadnetPlugin = new TmcRoadnetPlugin();
        
        tmcRoadnetPlugin.setLocationCache( tmcRoadSegmentCache );
        tmcRoadnetPlugin.setRoadNetCache( roadNetCache );
        tmcRoadnetPlugin.setTmcTable( tmcTable );
        
        return tmcRoadnetPlugin;
    }
    
    @Bean(initMethod = "init")
    SystemConfiguration configuration( ConfigService configService,
    		                           @Value("${cfg.check.kri2UzFile:}") String kri2UzFile, 
    		                           @Value("${cfg.check.wancomKeyFileDir:}") String wancomKeyFileDir ) throws IOException {
    	return new SystemConfiguration( configService, kri2UzFile, wancomKeyFileDir );
    }
    
    @Bean(destroyMethod="shutdownNow")
    ExecutorService executor( @Value("${wls.poolSize:10}") int poolSize ) {
        
        return Executors.newFixedThreadPool( poolSize );
    }
    
    @Bean
    WlsGeoServerCfg geoServer( 
            @Value("${wls.system.version}") String wlsVersion,
            @Value("${geo.service.port:4916}") int port,
            TmcTableCache tmcTableCache,
            PersistenceManager wlsPersistenceManager ) {
        
        return new WlsGeoServerCfg( wlsVersion, false, port, tmcTableCache, wlsPersistenceManager );
        
    }
    
    @Bean
    WebLocationService wls(   // NOSONAR
            @Value("${wls.srid:0}") int wlsSrid,
            ExecutorService executor,
            RoadMapPlugin roadMapPlugin,
            RoadNetPlugin roadNetPlugin,
            MultiGetterPluginIgnoreBbox sdbbyFeaturePlugin,
            //StrategyGetterPlugin strategyGetterPlugin,
            GeoFeatureGetterPlugin geoFeatureGetterPlugin,
            TccPointFeaturePlugin tccPointFeaturePlugin,
            OpenLRPlugin openLrPlugin,
            TmcPlugin tmcPlugin,
            DirectedTmcPlugin directedTmcPlugin,
            TmcRoadnetPlugin tmcRoadNetPlugin) {
        
        WebLocationService wls = new WebLocationService( wlsSrid, executor );
        wls.setPlugins( Arrays.asList( roadMapPlugin, 
                                       roadNetPlugin,
                                       sdbbyFeaturePlugin,
                                       //strategyGetterPlugin,
                                       geoFeatureGetterPlugin,
                                       tccPointFeaturePlugin,
                                       openLrPlugin, 
                                       tmcPlugin,
                                       directedTmcPlugin,
                                       tmcRoadNetPlugin ) );       
        
        return wls;
    }
    
    @Bean
    JAXBContext jaxbContext(@Value("${wls.jaxbContext:}") String jaxbContext) throws JAXBException  {
        
        if( jaxbContext.isEmpty() ) {
            try {
                jaxbContext = WlsWebServiceClient.getWlsJAXBContext();
            } catch( WlsException ex ) {
                return null;
            }
        }
        return JAXBContext.newInstance(jaxbContext);
    }

    @Bean
    Server endpoint(
            JAXBContext ctx, 
            WebLocationServer wls,
            @Value("${wls.protocol:http}://0.0.0.0:${wls.port:8085}/WebLocationServerSrv") String address) {
        JaxWsServerFactoryBean factory = new JaxWsServerFactoryBean();
        
        factory.setServiceClass(WebLocationServer.class);
        factory.setServiceBean(wls);
        factory.setWsdlLocation("classpath:wsdl/WLS.wsdl");
        JAXBDataBinding binding = new JAXBDataBinding();
        binding.setContext(ctx);
        factory.setDataBinding(binding);
        factory.setEndpointName(new QName("http://wls.heuboe.de/iface/", "WebLocationServerPort"));
        factory.setServiceName(new QName("http://wls.heuboe.de/iface/", "WebLocationServerSrv"));
        factory.setAddress(address);
        Map<String, Object> properties = new HashMap<>();
        properties.put("mtom-enabled", "true");
        factory.setProperties(properties);
        return factory.create();
    }
    
}
