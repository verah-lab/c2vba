package de.heuboe.asfinag.vmis2.tls.rcvadrcvt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import de.heuboe.asfinag.vmis2.tls.rcv.cfgmock.config.MockCfgSvcCfg2UzA2;
import de.heuboe.tls.receiver.interfaces.AddressConverter;
//import eu.vmis_ehe.vmis2.configservice.ConfigItemType;
//import eu.vmis_ehe.vmis2.configservice.ConfigServiceGrpc.ConfigServiceBlockingStub;
//import eu.vmis_ehe.vmis2.configservice.GetAllItemsRequest;
//import eu.vmis_ehe.vmis2.configservice.GetItemsReply;
//import eu.vmis_ehe.vmis2.configservice.GetItemsReply.Builder;
//import eu.vmis_ehe.vmis2.configservice.ServiceVersion;


@SpringBootTest

//@DirtiesContext // Kafka
//@EmbeddedKafka(partitions = 1, controlledShutdown = false, brokerProperties = {"log.dir=target/kafka"})

@TestPropertySource(
        properties = {
            "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
            "spring.kafka.consumer.group-id=EmbeddedKafkaTest",
            "spring.kafka.listener.missing-topics-fatal=false"},
        locations="classpath:testAdrCvt.properties"               // !!! properties
        )

@ContextConfiguration( classes = { 
//        de.heuboe.tls.receiver.config.Config.class,
        de.heuboe.asfinag.vmis2.tls.rcv.cfgmock.config.MockCfgSvcCfg.class, // Kaernten
        de.heuboe.tls.receiver.core.adrcvt.AddressConverterTls.class,
//        de.heuboe.tls.receiver.config.MetricsConfig.class,
//        de.heuboe.tls.receiver.config.KafkaConfig.class,
        } )

@EnableAutoConfiguration

public class TestCvt {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestCvt.class);

//    @TestConfiguration
//    @SpringBootConfiguration
//    @Import({Config.class, JacksonConfig.class, KafkaConfig.class })
//    public static class MockCfgSvcCfg {
//        
//        @Autowired
//        public String uzId;
// 
//        @Bean
//        public ConfigServiceBlockingStub cfgSvcStub() throws IOException {
////          alternate way
////            Client client = new Client( @Value( "${grpc.client.cfgsvc.address:static://oper-w7v.heuboe.hbintern:9890" )
////            return client.getStub()
//            GetAllItemsRequest req0 = GetAllItemsRequest.newBuilder().setType( ConfigItemType.UZ ).build();
//            GetAllItemsRequest requestd =
//                    GetAllItemsRequest.newBuilder().setType( ConfigItemType.DEVICE ).setUzId( uzId ).build();
//            GetAllItemsRequest requestc =
//                    GetAllItemsRequest.newBuilder().setType( ConfigItemType.CABLE ).setUzId( uzId ).build();
//
//            ConfigServiceBlockingStub cfgSvc = Mockito.mock( ConfigServiceBlockingStub.class );
//            GetItemsReply devs = readMsg( GetItemsReply.newBuilder(), TestConsts.FILE_NAME_DEVS );
//            GetItemsReply cabs = readMsg( GetItemsReply.newBuilder(), TestConsts.FILE_NAME_CABS );
//            GetItemsReply uzen = readMsg( GetItemsReply.newBuilder(), TestConsts.FILE_NAME_UZ );
//            ServiceVersion vers = readMsgV( ServiceVersion.newBuilder(), TestConsts.FILE_NAME_VERS );
//
//            when( cfgSvc.getServiceVersion( Empty.getDefaultInstance() ) ).thenReturn( vers );
//            when( cfgSvc.getAllItems( req0 ) ).thenReturn( uzen );
//            when( cfgSvc.getAllItems( requestd ) ).thenReturn( devs );
//            when( cfgSvc.getAllItems( requestc ) ).thenReturn( cabs );
//            LOGGER.info( "Config: cfgSvcStub {} mocked", cfgSvc );
//            return cfgSvc;
//        }
//        
//        private static GetItemsReply readMsg(GetItemsReply.Builder builder, String fName)  throws IOException {
//            JsonFormat jf = new JsonFormat();
//            try (FileInputStream fin = new FileInputStream( fName )) {
//                jf.merge( fin, builder );
//            }
//            return builder.build();
//        }
//        
//        private static ServiceVersion readMsgV(ServiceVersion.Builder builder, String fName)  throws IOException {
//            JsonFormat jf = new JsonFormat();
//            try ( FileInputStream fin = new FileInputStream( fName ) ) {
//                jf.merge( fin, builder );
//            }
//            return builder.build();
//        }
//        
//    }
    
    /*
    }, {
      "id" : "SM_A02_0_800",
      "name" : "SM_A02_2_305,030",
      "type" : "RST",
>     "osi7_address" : 8431421,
      "partner_id" : 6,
      "fgs" : [ {
>       "number" : 1,
        "eas" : [ {
          "eaid" : "MQ_A02_2_800.Cl1",
          "de_nummer" : 193
        }, {
          "eaid" : "MQ_A02_2_800_F1",
          "de_nummer" : 33
        }, {
          "eaid" : "MQ_A02_2_800_F2",
          "de_nummer" : 34
        } ],
        "clusters" : [ {
          "number" : 50,
          "cluster_ea" : {
            "eaid" : "MQ_A02_2_800.Cl1",
            "de_nummer" : 193
          },
          "grouped_eas" : [ {
>>          "eaid" : "MQ_A02_2_800_F1",
>           "de_nummer" : 33
          }, {
            "eaid" : "MQ_A02_2_800_F2",
            "de_nummer" : 34
          } ]
        } ]
        
    }, {
      "id" : "SM_A02_0_905",
      "name" : "SM_A02_2_344,600",
      "type" : "RST",
      "osi7_address" : 8433430,
      "partner_id" : 6,
      "fgs" : [ {
        "number" : 1,
        "eas" : [ {
          "eaid" : "MQ_A02_1_905.Cl1",
          "de_nummer" : 193
        }, {
          "eaid" : "MQ_A02_1_905_F1",
          "de_nummer" : 1
        }, {
          "eaid" : "MQ_A02_1_905_F2",
          "de_nummer" : 2
        }, {
          "eaid" : "MQ_A02_2_905.Cl1",
          "de_nummer" : 194
        }, {
          "eaid" : "MQ_A02_2_905_F1",
          "de_nummer" : 33
        }, {
          "eaid" : "MQ_A02_2_905_F2",
          "de_nummer" : 34
        } ],
     */
    @Autowired
    AddressConverter cvt;

    @Test
    public void testAdrCvt() {
        LOGGER.info( "--- Test address converter ---" );
//        AddressConverterVMIS2 cvt = new AddressConverterVMIS2(cfgStub, uzId);
        assertEquals( "MQ_A02_2_800_F1", cvt.convert( 8431421, 1, 33 ) );
        assertEquals( "MQ_A02_2_800_F2", cvt.convert( 8431421, 1, 34 ) );
        

        assertEquals( "MQ_A02_1_905.Cl1", cvt.convert( 8433430, 1, 193 ) );
        assertEquals( "MQ_A02_1_905_F1", cvt.convert( 8433430, 1, 1 ) );
        assertEquals( "MQ_A02_1_905_F2", cvt.convert( 8433430, 1, 2 ) );

        assertEquals( "MQ_A02_2_905.Cl1", cvt.convert( 8433430, 1, 194 ) );
        assertEquals( "MQ_A02_2_905_F1", cvt.convert( 8433430, 1, 33 ) );
        assertEquals( "MQ_A02_2_905_F2", cvt.convert( 8433430, 1, 34 ) );
        
        assertThrows( IllegalArgumentException.class, () -> cvt.convert( -1, 1, 34 ) );
        assertThrows( IllegalArgumentException.class, () -> cvt.convert( 8433430, 280, 34 ) );
        assertThrows( IllegalArgumentException.class, () -> cvt.convert( 8433430, 1, -1 ) );

        assertThrows( IllegalArgumentException.class, () -> cvt.convert( (256*256*256+1), 1, 34 ) );
        assertThrows( IllegalArgumentException.class, () -> cvt.convert( 8433430, -1, 34 ) );
        assertThrows( IllegalArgumentException.class, () -> cvt.convert( 8433430, 1, 270 ) );
        LOGGER.info( "--- Done test address converter ---" );
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
