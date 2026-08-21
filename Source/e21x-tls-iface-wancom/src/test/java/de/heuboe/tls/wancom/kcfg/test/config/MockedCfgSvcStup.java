package de.heuboe.tls.wancom.kcfg.test.config;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import eu.vmis_ehe.vmis2.configservice.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.when;

@SpringBootTest

@Slf4j
@EnableConfigurationProperties
@SpringJUnitConfig
@EnableKafka
@EnableAutoConfiguration(exclude= MongoAutoConfiguration.class)

@ExtendWith( MockitoExtension.class )
public class MockedCfgSvcStup {
//    public class SpecialDevRootTest {

    public static class TestConsts {
        public static final String DIR_NAME = "src/test/resources/cfgData/UZ_Kaernten-4.0.1.1/";
        //public static final String DIR_NAME = "src/test/resources/data/a2/";
        public static final String FILE_NAME_DEVS = DIR_NAME + "csvc-devs.json";
        public static final String FILE_NAME_CABS = DIR_NAME + "csvc-cabs.json";
        public static final String FILE_NAME_UZ   = DIR_NAME + "csvc-uzen.json";
        public static final String FILE_NAME_VERS = DIR_NAME + "csvc-vers.json";

        static final String uzId = "UZ_Kaernten";

        public static final Charset utf8 = Charset.forName( "UTF-8" );
    }

    @Mock( lenient = true ) // important for usage of one method with different arguments many times
    private ConfigServiceGrpc.ConfigServiceBlockingStub cfgStub;

    @Bean
    ConfigServiceGrpc.ConfigServiceBlockingStub getCfgSvcStup() {
        return cfgStub;
    }

    // ==============================================
    public GetItemsReply  devs;
    public GetItemsReply  cabs;
    public GetItemsReply  uzen;
    public ServiceVersion vers;

    public String uzId = "UZ_Kaernten";
    public GetAllItemsRequest req0 =
             GetAllItemsRequest.newBuilder().setType( ConfigItemType.UZ ).build();
    public GetAllItemsRequest requestd =
             GetAllItemsRequest.newBuilder().setType( ConfigItemType.DEVICE ).setUzId( uzId ).build();
    public GetAllItemsRequest requestc =
             GetAllItemsRequest.newBuilder().setType( ConfigItemType.CABLE ).setUzId( uzId ).build();


//    @BeforeAll
//    public static void setDebug(){
//        System.setProperty("log4j.debug","");
//        System.setProperty("log4j.configurationFile","log4j2.xml");
//    }

    @BeforeEach
    public void init() throws IOException {
        MockitoAnnotations.initMocks( this );
        devs = (GetItemsReply) readMsg( GetItemsReply.newBuilder(), TestConsts.FILE_NAME_DEVS );
        cabs = (GetItemsReply) readMsg( GetItemsReply.newBuilder(), TestConsts.FILE_NAME_CABS );
        uzen = (GetItemsReply) readMsg( GetItemsReply.newBuilder(), TestConsts.FILE_NAME_UZ );
        vers = (ServiceVersion) readMsgV( ServiceVersion.newBuilder(), TestConsts.FILE_NAME_VERS );

        when( cfgStub.getServiceVersion( Empty.getDefaultInstance() ) ).thenReturn( vers );
        when( cfgStub.getAllItems( req0 ) ).thenReturn( uzen );
        when( cfgStub.getAllItems( requestd ) ).thenReturn( devs );
        when( cfgStub.getAllItems( requestc ) ).thenReturn( cabs );
    }

    // ===========================================================================================

//    @Test
//    public void cfgAnalysisTest() {
//        when( cfgStub.getServiceVersion( Empty.getDefaultInstance() ) ).thenReturn( vers );
//        when( cfgStub.getAllItems( req0 ) ).thenReturn( uzen );
//        when( cfgStub.getAllItems( requestd ) ).thenReturn( devs );
//        when( cfgStub.getAllItems( requestc ) ).thenReturn( cabs );
//    }
    // ==============================================

//    private TlsCfgServiceVersion cfgServiceVersion;
//    private List< TlsCfgUZInfo > cfgUZInfos;
//    private List< TlsCfgDevice > cfgDevices;
//    private List< TlsCfgCable > cfgCables;

//        public init( String uzId ) throws Exception {
//            String dir = null;
//            if ("UZ_A2".equals( uzId )) {
//                dir = "src/test/resources/cfgData/UZ_A2-4.0.1.1/";
//            }
//            if ("UZ_Kaernten".equals( uzId )) {
//                dir = "src/test/resources/cfgData/UZ_Kaernten-4.0.1.1/";
//            }
//
//            if (null == dir) {
//                throw new IllegalStateException("Bad uz id for config");
//            }
//
//            cfgServiceVersion = readVersionFile( dir + "tlsb-vers.json" );
//
//            // @formatter:off
//            cfgUZInfos = readListFile( dir + "tlsb-uzen.json",  TlsCfgUZInfo.class, new TypeReference<List<TlsCfgUZInfo>>() {} );
//            cfgDevices = readListFile( dir + "tlsb-devs.json", TlsCfgDevice.class, new TypeReference<List<TlsCfgDevice>>() {} );
//            cfgCables  = readListFile( dir + "tlsb-cabs.json", TlsCfgCable.class,  new TypeReference<List<TlsCfgCable>>()  {} );
//        }

    // ===========================================================================================

//    private <T> List<T> readListFile( String filename,  Class<T> c, TypeReference<List<T>> t ) throws Exception {
//        ObjectMapper objectMapper = new ObjectMapper();
//
//        URL url = new URL( "file:" + filename );
//
//        List<T> res = objectMapper.readValue(url, t );
//        return res;
//    }
//
//    // ===========================================================================================
//
//    private TlsCfgServiceVersion readVersionFile( String filename ) throws Exception {
//        ObjectMapper objectMapper = new ObjectMapper();
//
//        URL url = new URL( "file:" + filename );
//
//        TlsCfgServiceVersion res = objectMapper.readValue(url, TlsCfgServiceVersion.class );
//        return res;
//    }
//
//    private Message readMsg(com.google.protobuf.GeneratedMessageV3.Builder<GetItemsReply.Builder> b, String fName)  throws IOException {
//        JsonFormat jf = new JsonFormat();
//        FileInputStream fin = new FileInputStream( fName );
//        Message.Builder builder = b;
//        jf.merge( fin, builder );
//
//        return builder.build();
//    }
//
//    private Message readMsgV(com.google.protobuf.GeneratedMessageV3.Builder<eu.vmis_ehe.vmis2.configservice.ServiceVersion.Builder> b, String fName)  throws IOException {
//        JsonFormat jf = new JsonFormat();
//        FileInputStream fin = new FileInputStream( fName );
//        Message.Builder builder = b;
//        jf.merge( fin, builder );
//
//        return builder.build();
//    }


    private Message readMsg(com.google.protobuf.GeneratedMessageV3.Builder<eu.vmis_ehe.vmis2.configservice.GetItemsReply.Builder> b, String fName)  throws IOException {
        JsonFormat.Parser p = JsonFormat.parser();

        FileInputStream fis = new FileInputStream( fName );
        InputStreamReader isr = new InputStreamReader( fis, StandardCharsets.UTF_8 );
        BufferedReader reader = new BufferedReader( isr );

        p.merge( reader, b );

        return b.build();
    }

    private Message readMsgV(com.google.protobuf.GeneratedMessageV3.Builder<eu.vmis_ehe.vmis2.configservice.ServiceVersion.Builder> b,
                             String fName // NOSONAR
    )  throws IOException {
        JsonFormat.Parser p = JsonFormat.parser();

        FileInputStream fis = new FileInputStream( fName );
        InputStreamReader isr = new InputStreamReader( fis, StandardCharsets.UTF_8 );
        BufferedReader reader = new BufferedReader( isr );

        p.merge( reader, b );

        return b.build();
    }

}
