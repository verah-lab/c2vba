package de.heuboe.asfinag.vmis2.tls.rcv.cfgmock;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import java.io.FileInputStream;
import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;

import de.heuboe.asfinag.vmis2.tls.rcv.cfgmock.config.TestConsts;
import de.heuboe.tls.receiver.core.receiver.Receiver;
//import eu.vmis_ehe.vmis2.configservice.ConfigItemType;
//import eu.vmis_ehe.vmis2.configservice.ConfigServiceGrpc.ConfigServiceBlockingStub;
//import eu.vmis_ehe.vmis2.configservice.GetAllItemsRequest;
//import eu.vmis_ehe.vmis2.configservice.GetItemsReply;
//import eu.vmis_ehe.vmis2.configservice.GetItemsReply.Builder;
//import eu.vmis_ehe.vmis2.configservice.ServiceVersion;
import lombok.extern.slf4j.Slf4j;

@ExtendWith(MockitoExtension.class)
@Slf4j
@Configuration
public class CfgMocked {
    private static final Logger LOGGER = LoggerFactory.getLogger(CfgMocked.class);

    @Mock(lenient = true) // important for usage of one method with different arguments many times
//    private ConfigServiceBlockingStub cfgStub;
//
//    public GetItemsReply  devs;
//    public GetItemsReply  cabs;
//    public GetItemsReply  uzen;
//    public ServiceVersion vers;
    
    public String uzId = "UZ_Kaernten";
//    public GetAllItemsRequest req0 = GetAllItemsRequest.newBuilder().setType( ConfigItemType.UZ ).build();
//    public GetAllItemsRequest requestd =
//     GetAllItemsRequest.newBuilder().setType( ConfigItemType.DEVICE ).setUzId( uzId ).build();
//    public GetAllItemsRequest requestc =
//     GetAllItemsRequest.newBuilder().setType( ConfigItemType.CABLE ).setUzId( uzId ).build();
//    
//    @Bean
//    ConfigServiceBlockingStub getCfgSvc() {
//        return cfgStub;
//    }
 
    
    @BeforeAll
    public static void setDebug(){
        System.setProperty("log4j.debug","");
        System.setProperty("log4j.configurationFile","log4j2.xml");
    }

//    @BeforeEach
//    public void init() throws IOException {
//        MockitoAnnotations.initMocks( this );
//        devs = (GetItemsReply) readMsg( GetItemsReply.newBuilder(), TestConsts.FILE_NAME_DEVS );
//        cabs = (GetItemsReply) readMsg( GetItemsReply.newBuilder(), TestConsts.FILE_NAME_CABS );
//        uzen = (GetItemsReply) readMsg( GetItemsReply.newBuilder(), TestConsts.FILE_NAME_UZ );
//        vers = (ServiceVersion) readMsgV( ServiceVersion.newBuilder(), TestConsts.FILE_NAME_VERS );
//    }

    // ===========================================================================================
    
    @Autowired
    Receiver rcv;
    
    @Test
    @Disabled
    public void cfgMockedTest() {
//        when( cfgStub.getServiceVersion( Empty.getDefaultInstance() ) ).thenReturn( vers );
//        when( cfgStub.getAllItems( req0 ) ).thenReturn( uzen );
//        when( cfgStub.getAllItems( requestd ) ).thenReturn( devs );
//        when( cfgStub.getAllItems( requestc ) ).thenReturn( cabs );
        
//        Config.setCfgSvc( cfgStub );
//        
//        SpringApplication app = new SpringApplication( Config.class );
//        ConfigurableApplicationContext ctx = app.run( "-Dlogging.config=src/test/resources/log4j2.xml" );
//
//        Receiver rcv = (Receiver) ctx.getBean( "myWuppi" );
        assertNotNull( rcv );

        System.out.println( "--Done cfgAnalysisTest--" );
    }
    
    // ===========================================================================================
    
//    private Message readMsg(com.google.protobuf.GeneratedMessageV3.Builder<Builder> b, String fName)  throws IOException {
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

}
