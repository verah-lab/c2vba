package de.heuboe.geo.manager.control.test.base;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import de.heuboe.asfinag.vmis2.geomanager.kafka.config.MapActivationReceiver;
import de.heuboe.geo.manager.base.config.ConfigBase;
import de.heuboe.geo.manager.control.Main;
import de.heuboe.test.result.comparator.ResultComparator;
import de.heuboe.test.result.comparator.ResultComparator.CompareResult;
import de.heuboe.vmis2.geomanager.exceptions.GMSException;
import de.heuboe.vmis2.geomanager.repository.geomanager.GeoManagerRepository;
import de.heuboe.vmis2.geomanager.repository.mapmanager.MapManagerRepository;
import de.heuboe.wls.basic.util.DecimalPointFormatter;
import de.heuboe.wls.map.supply.MapActivationPostProcessing;
import de.heuboe.wls.map.supply.MapContextManager;
import de.heuboe.wls.srv.impl.WebLocationService;
import de.heuboe.wls.srv.manager.WebLocationServiceManager;
import de.heuboe.wls.util.LocationVisualizer;
import de.heuboe.wls.util.WlsException;
import de.heuboe.wls.util.WlsGeoServer;
import eu.vmis_ehe.vmis2.geo.notifier.data.Component;
import eu.vmis_ehe.vmis2.geo.notifier.data.Notification;
import eu.vmis_ehe.vmis2.mapmanager.ComponentType;
import eu.vmis_ehe.vmis2.mapmanager.MapImportRequest;
import eu.vmis_ehe.vmis2.mapmanager.MapImportResult;
import eu.vmis_ehe.vmis2.mapmanager.MapManagerGrpc;
import eu.vmis_ehe.vmis2.mapmanager.MapPartVersion;
import eu.vmis_ehe.vmis2.mapmanager.MapPartVersionId;
import eu.vmis_ehe.vmis2.mapmanager.MapPartVersions;

@ExtendWith(SpringExtension.class)
@Configuration
@ComponentScan(basePackages = { "de.heuboe.geo.manager.control.config" })
//@TestPropertySource(locations="classpath:/springCfgTestSwiss/application.properties")
@TestPropertySource(locations="classpath:/springCfgTest/application.properties")
public class BaseT {

    protected static DecimalPointFormatter kmFormat = new DecimalPointFormatter( "#0.000" );
    
    public static final String BASE_SUPPLY_DIR = "map";
    
    protected static ResultComparator trc = null;
    protected ObjectMapper objectMapper = new ObjectMapper();
    protected ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
    
    
    @Autowired
    private ConfigurableApplicationContext primaryCtx;

    @Autowired
    protected WebLocationServiceManager webLocationServiceManager;
    @Autowired
    protected MapActivationPostProcessing   mapa;
    
    @Autowired
    protected MapContextManager mcm;

    @Autowired
    protected GeoManagerRepository gmr = null;
    
    private LocationVisualizer locationVisualizer = null;

    protected static boolean isInitialized = false;
    
    protected boolean initWls() {
        return true;
    }
    
    protected void postInit()  throws Exception {
    }

    protected void preInit() throws Exception  {
    }

    @BeforeAll
    public static void setUp() throws Exception {
        
        Main.setRouteSystem();
        
        System.setProperty( "spring.config.location", "classpath:/springCfgTest/application.properties" );
        
        ResultComparator.createFolders( true );
        trc = ResultComparator.createComparator(
                 System.getProperty("user.dir") + "/src/test/resources/checkResult",
                 System.getProperty("user.dir") + "/src/test/resources/result",
                 5 );
        
        MapActivationReceiver.setEnabled( false );
    }
    
    @BeforeEach
    public void init() throws Exception {

        if ( initWls() && !isInitialized ) {
            try {
               
               webLocationServiceManager.setPrimaryCtx( primaryCtx );
               preInit(); 
               
               webLocationServiceManager.setMapContextManager( mcm );
               webLocationServiceManager.firstInit( mapa, false );
               
               postInit();
               
            } catch ( WlsException ex ) {
                throw new WlsException( "Initialisation of GeoManagerRepository failed!", ex );
            }
            isInitialized = true;
        } else {
            preInit(); 
            postInit();
        }
    }
    
    protected LocationVisualizer getLocationVisualizer() {
        if( locationVisualizer == null ) {
            locationVisualizer = createLocationVisualizer();
        }
        
        return locationVisualizer;
    }
    
    private LocationVisualizer createLocationVisualizer() {
        
        WebLocationService wls = webLocationServiceManager.getConfigurationObject( WebLocationService.class );
        WlsGeoServer geoServer = webLocationServiceManager.getConfigurationObject( WlsGeoServer.class );       
        
        LocationVisualizer locationVisualizer = new LocationVisualizer( wls, ConfigBase.getWlsSrid(), -1, null );
        locationVisualizer.setGeoServer( geoServer );
        
        return locationVisualizer;
    }
    
    
    
    protected void checkResult( String result, String id ) throws Exception {
        
        CompareResult cr = trc.checkResult( result, id, "txt" );
        if( cr.hasDifference() )
        {
            System.out.println( "Difference for <" + id + ">" );
            System.out.println( cr.getDifference() );
            fail();
        }
    }
    
    protected void checkResult( Object result, String id ) throws Exception {
        
        CompareResult cr = trc.checkResult( result, id );
        if( cr.hasDifference() )
        {
            System.out.println( "Difference for <" + id + ">" );
            System.out.println( cr.getDifference() );
            fail();
        }
    }
    
    public static void deleteVersion( MapManagerRepository mmr, Component component, String version ) throws GMSException {
        MapPartVersionId.Builder mpvi = MapPartVersionId.newBuilder();
        mpvi.setType( component );
        mpvi.setVersion( version );
        mmr.deleteMapSupplyPartVersion( mpvi.build() );
    }
    
    public static void deleteVersion( MapManagerGrpc.MapManagerBlockingStub mmr, Component component, String version ) {
        MapPartVersionId.Builder mpvi = MapPartVersionId.newBuilder();
        mpvi.setType( component );
        mpvi.setVersion( version );
        mmr.deleteMapPartVersion( mpvi.build() );
    }
    
    public static boolean importMapPartIfNotExists( MapManagerGrpc.MapManagerBlockingStub stub, 
                                                    Component component, 
                                                    String definedVersion,
                                                    String resultVersion, 
                                                    String description,
                                                    String srcPropertyName, String srcPropertyValue ) {
        
        ComponentType ct = ComponentType.newBuilder().setComponent( component ).build();
        MapPartVersions mpvs = stub.getAllMapPartVersions( ct );
        for( MapPartVersion mpv : mpvs.getMapPartVersionList() ) {
            if( mpv.getVersion().getVersion().equals( resultVersion ) ) {
                return true;
            }
        }
        
        MapImportRequest.Builder mirb = MapImportRequest.newBuilder(); 
        mirb.setDescription( description );
        mirb.setType( component );
        mirb.setVersion( definedVersion );
        
        if( ( srcPropertyName != null ) && !srcPropertyName.isBlank() ) {
            eu.vmis_ehe.vmis2.geomanager.base.Property property = 
                    eu.vmis_ehe.vmis2.geomanager.base.Property.newBuilder()
                        .setName( srcPropertyName )
                        .setValue( srcPropertyValue).build();
            mirb.addSupplyFileLocations( property );
        }
        
        MapImportResult mir = stub.importMapPart( mirb.build() );

        if( mir.getFailed() ) {
            System.out.println( "" );
            System.out.println( "Import failed" );
            System.out.println( "Errors:" );
            for( Notification n : mir.getNotificationsList() ) {
                System.out.println( "   " + n.getMessage().getValue() );
            }
            
            return false;
        }
        
        return true;
    }
   
}
