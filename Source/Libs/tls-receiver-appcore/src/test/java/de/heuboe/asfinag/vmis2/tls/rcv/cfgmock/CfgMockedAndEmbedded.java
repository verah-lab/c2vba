package de.heuboe.asfinag.vmis2.tls.rcv.cfgmock;

import static com.google.protobuf.util.Timestamps.fromMillis;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.kafka.test.utils.ContainerTestUtils.waitForAssignment;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.google.protobuf.ByteString;

import de.heuboe.idgenerator.generator.IDGenerator;
import de.heuboe.tls.cfglib.Osi7Cfg;
import de.heuboe.tls.rcv.transf.impl.TransformerImpl;
import de.heuboe.tls.receiver.core.receiver.Receiver;
import de.heuboe.tls.receiver.interfaces.Transformer;
import de.heuboe.tls.receiver.rdr.core.FunctionInval;
import de.heuboe.tls.receiver.rdr.getter.TimeGetter;
import de.heuboe.tls.tel.io.TeleSReceived;
import de.heuboe.tls.tel.io.TeleSReceived.Builder;
import de.heuboe.tls.tel.io.pojo.PTeleSReceived;
import de.heuboe.tls.tel.io.pojo.PTeleSReceived.PTeleSReceivedBuilder;
import de.heuboe.tls.tlstele.TlsDeBlock;
import de.heuboe.tls.tlstele.TlsETel;
import de.heuboe.tls.tlstele.TlsTele;
import eu.vmis_ehe.vmis2.tls.received.LVEErgebnisVersion3;
import eu.vmis_ehe.vmis2.tls.received.LVEErgebnisVersion3List;
import eu.vmis_ehe.vmis2.tls.received.SYSFehlerDUEList;
//import eu.vmis_ehe.vmis2.configservice.ConfigItemType;
//import eu.vmis_ehe.vmis2.configservice.ConfigServiceGrpc.ConfigServiceBlockingStub;
//import eu.vmis_ehe.vmis2.configservice.GetAllItemsRequest;
//import eu.vmis_ehe.vmis2.configservice.GetItemsReply;
//import eu.vmis_ehe.vmis2.configservice.ServiceVersion;
import eu.vmis_ehe.vmis2.tls.received.UFDDeFehlerList;
import eu.vmis_ehe.vmis2.tls.received.UFDLufttemperatur;
import eu.vmis_ehe.vmis2.tls.received.UFDLufttemperaturList;
import eu.vmis_ehe.vmis2.tls.received.UFDNiederschlagsartList;
import eu.vmis_ehe.vmis2.tls.received.UFDStickstoffmonoxid;
import eu.vmis_ehe.vmis2.tls.received.UFDStickstoffmonoxidList;
import eu.vmis_ehe.vmis2.tls.received.WZGDeFehlerList;
import eu.vmis_ehe.vmis2.tls.received.WZGDefekteLEDKetten;
import eu.vmis_ehe.vmis2.tls.received.WZGDefekteLEDKettenList;
import eu.vmis_ehe.vmis2.tls.received.WZGDefekteLampen;
import eu.vmis_ehe.vmis2.tls.received.WZGDefekteLampenList;
import eu.vmis_ehe.vmis2.tls.received.WZGGrundeinstellung;
import eu.vmis_ehe.vmis2.tls.received.WZGGrundeinstellungList;
import eu.vmis_ehe.vmis2.tls.received.WZGStellzustand;
import eu.vmis_ehe.vmis2.tls.received.WZGStellzustandList;
import io.micrometer.core.instrument.Counter;
//import eu.vmis_ehe.vmis2.tls.tel.io.TeleSReceived;
//import eu.vmis_ehe.vmis2.tls.tel.io.TeleSReceived.Builder;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;


@SpringBootTest

//@DirtiesContext // Kafka
//@EmbeddedKafka(partitions = 1, controlledShutdown = false, brokerProperties = {"log.dir=target/kafka"})

@DirtiesContext // Kafka
//@formatter:off
@EmbeddedKafka(
     partitions = 1, 
     controlledShutdown = false, 
             ports = {55777},
             zookeeperPort = 55771,
//             ports = {50637},
     // ${random.int} => always begin with a fresh directory. i.e. no messages in broker
     // keep directory for instance tin order to preload
     brokerProperties = {"log.dir=target/kafka${random.int}"} 
) // @formatter:on

@TestPropertySource(
        properties = {
            "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
            "spring.kafka.consumer.group-id=EmbeddedKafkaTest",
            "spring.kafka.listener.missing-topics-fatal=false"},
        locations="classpath:embeddedTest.properties"               // !!! properties
        )

@ContextConfiguration( classes = { 
      de.heuboe.tls.receiver.core.config.Config.class,
      de.heuboe.tls.receiver.core.config.ConfigTimeoffset.class,
      de.heuboe.asfinag.vmis2.tls.rcv.cfgmock.config.MockCfgSvcCfg2UzA2.class, // A2/A23
//      de.heuboe.tls.rcvadrcvt.AddressConverterTls.class,
      de.heuboe.tls.receiver.core.config.MetricsConfig.class,
      de.heuboe.tls.receiver.core.config.KafkaConfig.class,
      de.heuboe.tls.receiver.core.config.TransformerConfig.class,
      de.heuboe.asfinag.vmis2.tls.rcv.cfgmock.config.TransformationRulesConfig.class,
//      de.heuboe.asfinag.vmis2.tls.rcv.cfgmock.config.Vmis2SystemMessageManagement.class,
      de.heuboe.asfinag.vmis2.tls.rcv.cfgmock.config.Vmis2SystemMessageManagementProperties.class,
      de.heuboe.tls.receiveconverter.InitAllInit.class
      } )

@EnableAutoConfiguration
@Slf4j
public class CfgMockedAndEmbedded {
    private static final Logger LOGGER = LoggerFactory.getLogger(CfgMockedAndEmbedded.class);
    
    
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
//            GetItemsReply devs = readMsg( GetItemsReply.newBuilder(), TestConstsA2.FILE_NAME_DEVS );
//            GetItemsReply cabs = readMsg( GetItemsReply.newBuilder(), TestConstsA2.FILE_NAME_CABS );
//            GetItemsReply uzen = readMsg( GetItemsReply.newBuilder(), TestConstsA2.FILE_NAME_UZ );
//            ServiceVersion vers = readMsgV( ServiceVersion.newBuilder(), TestConstsA2.FILE_NAME_VERS );
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
    
//    @BeforeAll
    @BeforeEach
    public /* static */ void setDebug() throws IOException{
        System.setProperty("log4j.debug","");
        System.setProperty("log4j.configurationFile","log4j2.xml");
        System.setProperty("java.io.tmpdir", "target");
    }

    @BeforeEach
    public void init() throws IOException {
        for (final MessageListenerContainer messageListenerContainer : this.registry.getListenerContainers()) {
            waitForAssignment(messageListenerContainer, this.embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    // ===========================================================================================

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    MeterRegistry meterRegistry;
    
    @Autowired
    Receiver receiver;
    
    @Value("${de.heuboe.asfinag.tls.receiver.inputTopic}")
    String topicNameFromSpring;

//    @Autowired  @Qualifier("topicPrefix")
//    private String topicPrefix;
//    @Autowired  @Qualifier("topicPostfix")
//    private String topicPostfix;
    final static private String topicPrefix = "A2_";
    final static private String topicPostfix = "";

//    @Test
//    public void receiverStartupTest() throws IOException {
//        
//        receiver.init();
//        
//        assertNotNull( receiver );
//
//        System.out.println( "--Done cfgAnalysisTest--" );
//    }
    
    private int cntVirtReceives = 0;
    private int cntUfdReceives = 0;
    private int cntLve3Receives = 0;
    private int cntUfdInvalidValues = 0;
    private int cntFehlerDue = 0;
    private static int countFromOtherTests = 0;
    
    private static boolean omitInterval = false;

    @Test
    @DirtiesContext
    public void virtualSensorTest() throws IOException, InterruptedException, ExecutionException {
        String methodeName = new Throwable().getStackTrace()[0].getMethodName();
        log.info( "Start test {}", methodeName );
        Thread.sleep( 3000 );
        
        
        cntVirtReceives = 0;
        
        sendTeles( makeTeleListVirt() );
        double cnt1 = 0;
        double cnt2 = 0;
        double eps = 0.1;
        
        while (
                ((4 - eps) > (cnt1 = meterRegistry.find( "TelegramsReceived" ).counter().count()))
                ||
                ((8 - eps) > (cnt2 = meterRegistry.find( "DataObjects.sended" ).counter().count()))) {
            System.out.println( "Count of telegrams: " + cnt1 + " count of sended Objects " + cnt2 );
            Thread.sleep( 500 );
        }
        Thread.sleep( 2000 );
        Thread.sleep( 2000 );
        System.out.println( "-- Count of telegrams: " + cnt1 + " count of sended Objects " + cnt2 );
        
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> assertTrue( cntVirtReceives >= 10 ) );
//            .untilAsserted(() -> assertEquals( 10 + countFromOtherTests, cntVirtReceives ));

        System.out.println( "--Done virtualSensorTest-- " + cntVirtReceives );
        countFromOtherTests += cntVirtReceives;
        
    }
    
    // the following two test are to verify the functionality of timetolerence
    // virtualSensorTest2_i_e_timeFuture_faulty results ind a timestep some seconds in the future which leads to a time one day back (timetolerance 0)
    // virtualSensorTest2_i_e_timeFuture_correct uses a default or property controlled timetolerance of > 0. This allows for timestamps in the future.

    @Test
    @DirtiesContext
    public void virtualSensorTest2_i_e_timeFuture_faulty() throws IOException, InterruptedException, ExecutionException {
        String methodeName = new Throwable().getStackTrace()[0].getMethodName();
        log.info( "Start test {}", methodeName );
        Thread.sleep( 3000 );
        
        cntVirtReceives = 0;
        
        TimeGetter.setTimetolerance( 0 );
        
        setFutureTime( 3 );
        
        sendTeles( makeTeleListVirt() );
        double cnt1 = 0;
        double cnt2 = 0;
        double eps = 0.1;
        
        Thread.sleep( 2000 );
        
        while (
                ((4 - eps) > (cnt1 = meterRegistry.find( "TelegramsReceived" ).counter().count()))
                ||
                ((8 - eps) > (cnt2 = meterRegistry.find( "DataObjects.sended" ).counter().count()))) {
            System.out.println( "Count of telegrams: " + cnt1 + " count of sended Objects " + cnt2 );
            Thread.sleep( 500 );
        }
        Thread.sleep( 2000 );
        System.out.println( "-- Count of telegrams: " + cnt1 + " count of sended Objects " + cnt2 );
        
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> assertTrue( cntVirtReceives >= 10 ) );
//            .untilAsserted(() -> assertEquals( 10 + countFromOtherTests, cntVirtReceives ));

        System.out.println( "--Done virtualSensorTest-- " + cntVirtReceives );
        countFromOtherTests += cntVirtReceives;
        
    }

    @Test
    @DirtiesContext
    public void virtualSensorTest2_i_e_timeFuture_correct() throws IOException, InterruptedException, ExecutionException {
        String methodeName = new Throwable().getStackTrace()[0].getMethodName();
        log.info( "Start test {}", methodeName );
        Thread.sleep( 3000 );
        
        cntVirtReceives = 0;
        
        setFutureTime( 0 );
        
        sendTeles( makeTeleListVirt() );
        double cnt1 = 0;
        double cnt2 = 0;
        double eps = 0.1;
        
        while (
                ((4 - eps) > (cnt1 = meterRegistry.find( "TelegramsReceived" ).counter().count()))
                ||
                ((8 - eps) > (cnt2 = meterRegistry.find( "DataObjects.sended" ).counter().count()))) {
            System.out.println( "Count of telegrams: " + cnt1 + " count of sended Objects " + cnt2 );
            Thread.sleep( 500 );
        }
        Thread.sleep( 2000 );
        System.out.println( "-- Count of telegrams: " + cnt1 + " count of sended Objects " + cnt2 );
        
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> assertTrue( cntVirtReceives >= 10 ) );
//            .untilAsserted(() -> assertEquals( 10 + countFromOtherTests, cntVirtReceives ));

        System.out.println( "--Done virtualSensorTest-- " + cntVirtReceives );
        countFromOtherTests += cntVirtReceives;
        
    }
    
    static final int SWS_A021_2_100_LT = 10;

    @Test
    @DirtiesContext
    public void floatInvalidTest() throws IOException, InterruptedException, ExecutionException {
        String methodeName = new Throwable().getStackTrace()[0].getMethodName();
        log.info( "Start test {}", methodeName );
        Thread.sleep( 3000 );
        
        cntUfdReceives = 0;
        cntUfdInvalidValues = 0;
        double cnt1 = 0;
        double cnt2 = 0;
        double eps = 0.1;
        Thread.sleep( 2000 );
        
        List<TlsTele> teleList = new ArrayList<>();
        
//        File f = new File( "src/test/resources/TlsTelegrammeUnitTest/3_4_48_UFDLufttemperatur-invalid.json" );
//        String name = f.getName();
//            TlsTele newTele = TlsTele.loadJs( f );
//            res.add( adjustSTel( newTele, forNode, forDE ) );

        addFile( teleList, "src/test/resources/TlsTelegrammeUnitTest/3_4_48_UFDLufttemperatur-invalid.json", SM_A021_2_002_366, SWS_A021_2_100_LT );
        
        sendTeles( teleList );
        Thread.sleep( 2000 );
        
        cnt1 = meterRegistry.find( "TelegramsReceived" ).counter().count();
        cnt2 = meterRegistry.find( "DataObjects.sended" ).counter().count();
//        while (
//                ((4 - eps) > (cnt1 = meterRegistry.find( "TelegramsReceived" ).counter().count()))
//                ||
//                ((8 - eps) > (cnt2 = meterRegistry.find( "DataObjects.sended" ).counter().count()))) {
//            System.out.println( "Count of telegrams: " + cnt1 + " count of sended Objects " + cnt2 );
//            Thread.sleep( 500 );
//        }
//        Thread.sleep( 2000 );
        System.out.println( "-- Count of telegrams: " + cnt1 + " count of sended Objects " + cnt2 );
        
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> assertEquals( 1, cntUfdInvalidValues ));

        System.out.println( "--Done " + methodeName + "-- " + cntUfdReceives );
        countFromOtherTests += cntUfdReceives;
        
    }

    @Test
    @DirtiesContext
    public void floatInvalid2Test() throws IOException, InterruptedException, ExecutionException {
        
        String methodeName = new Throwable().getStackTrace()[0].getMethodName();
        log.info( "Start test {}", methodeName );
        Thread.sleep( 3000 );
        
        cntLve3Receives = 0;
//        cntUfdInvalidValues = 0;
        double cnt1 = 0;
        double cnt2 = 0;
        double eps = 0.1;
        
        List<TlsTele> teleList = new ArrayList<>();
        
        boolean lastOmitInterval = omitInterval;
        omitInterval = true; // send fg1-de with timestamp type 30, i.e. no intervalType and -len
        // later when receiving (receive5) assure data has intervalTyp and -len
        addFile( teleList, "src/test/resources/TlsTelegrammeUnitTest/1_4_52_LVEErgebnisVersion3-tNetto1.json", SM_A021_2_002_366, MQ_A02_1_040_F1 );
        omitInterval = lastOmitInterval;
        
        sendTeles( teleList );
        Thread.sleep( 2000 );
        
        cnt1 = meterRegistry.find( "TelegramsReceived" ).counter().count();
        cnt2 = meterRegistry.find( "DataObjects.sended" ).counter().count();

        System.out.println( "-- Count of telegrams: " + cnt1 + " count of sended Objects " + cnt2 );
        
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> assertEquals( 1, cntLve3Receives ));

        System.out.println( "--Done " + methodeName + "-- " + cntUfdReceives );
        countFromOtherTests += cntUfdReceives;
        
    }
    
    double invalidValUfd = 0;

    @Test
    @DirtiesContext
    public void floatInvalid3Test() throws IOException, InterruptedException, ExecutionException {
        
        String methodeName = new Throwable().getStackTrace()[0].getMethodName();
        log.info( "Start test {}", methodeName );
        Thread.sleep( 3000 );
        FunctionInval.setFloatInvalid( "-99999" );
        
        cntUfdReceives = 0;
//        cntUfdInvalidValues = 0;
        double cnt1 = 0;
        double cnt2 = 0;
        double eps = 0.001;
        
        List<TlsTele> teleList = new ArrayList<>();
        
        addFile( teleList, "src/test/resources/TlsTelegrammeUnitTest/3_4_80_UFDStickstoffmonoxid-err.json", SM_A021_2_002_366, MQ_A02_1_040_F1 );
        
        sendTeles( teleList );
        Thread.sleep( 2000 );
        
        cnt1 = meterRegistry.find( "TelegramsReceived" ).counter().count();
        cnt2 = meterRegistry.find( "DataObjects.sended" ).counter().count();

        System.out.println( "-- Count of telegrams: " + cnt1 + " count of sended Objects " + cnt2 );
        
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> assertEquals( 1, cntUfdReceives ));
        
        assertTrue( (((double)-99999.0) - eps < invalidValUfd) && (((double)-99999.0) + eps > invalidValUfd) );

        System.out.println( "--Done " + methodeName + "-- " + cntUfdReceives );
        countFromOtherTests += cntUfdReceives;
        
    }

    @Test
    @DirtiesContext
    public void fg4UmlautTest() throws IOException, InterruptedException, ExecutionException {
        
        String methodeName = new Throwable().getStackTrace()[0].getMethodName();
        log.info( "Start test {}", methodeName );
        Thread.sleep( 3000 );
        
        Counter counter = meterRegistry.find( "getterErrors" ).counter();
        double val = counter.count();
        oeFound = false; // verify we receive 'Vösendorf'
        
        cntWzgReceives = 0;
//        cntUfdInvalidValues = 0;
        double cnt1 = 0;
        double cnt2 = 0;
        double eps = 0.1;
        
        List<TlsTele> teleList = new ArrayList<>();
        addFile( teleList, "src/test/resources/TlsTelegrammeUnitTest/4_5_55_WZGStellzustand_TextUmlaut.json", SM_A021_2_002_366, MQ_A02_1_040_F1 );
        
        sendTeles( teleList );
        Thread.sleep( 2000 );
        
        double val2 = counter.count();
        
        cnt1 = meterRegistry.find( "TelegramsReceived" ).counter().count();
        cnt2 = meterRegistry.find( "DataObjects.sended" ).counter().count();

        System.out.println( "-- Count of telegrams: " + cnt1 + " count of sended Objects " + cnt2 );
        
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> assertEquals( 1, cntWzgReceives ));
        assertEquals( val, val2 ); // no additional errors during getter
        
        assertTrue( oeFound );

        System.out.println( "--Done " + methodeName + "-- " + cntWzgReceives );
        countFromOtherTests += cntUfdReceives;
        
    }

    @Test
    @DirtiesContext
    public void fg4T33ShortTest() throws IOException, InterruptedException, ExecutionException {
        
        String methodeName = new Throwable().getStackTrace()[0].getMethodName();
        log.info( "Start test {}", methodeName );
        Thread.sleep( 3000 );
        
        Counter counter = meterRegistry.find( "getterErrors" ).counter();
        double val = counter.count();
        
        cntWzgReceives = 0;
//        cntUfdInvalidValues = 0;
        double cnt1 = 0;
        double cnt2 = 0;
        double eps = 0.1;
        
        List<TlsTele> teleList = new ArrayList<>();
        addFile( teleList, "src/test/resources/TlsTelegrammeUnitTest/4_3_33_WZGGrundeinstellung_1.json", SM_A021_2_002_366, MQ_A02_1_040_F1 );
        
        sendTeles( teleList );
        Thread.sleep( 2000 );
        
        double val2 = counter.count();
        
        cnt1 = meterRegistry.find( "TelegramsReceived" ).counter().count();
        cnt2 = meterRegistry.find( "DataObjects.sended" ).counter().count();

        System.out.println( "-- Count of telegrams: " + cnt1 + " count of sended Objects " + cnt2 );
        
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> assertEquals( 1, cntWzgReceives ));
        assertEquals( val, val2 ); // no additional errors during getter

        System.out.println( "--Done " + methodeName + "-- " + cntWzgReceives );
        countFromOtherTests += cntUfdReceives;
        
    }

    @Test
    @DirtiesContext
    public void fg4T33LongTest() throws IOException, InterruptedException, ExecutionException {
        
        String methodeName = new Throwable().getStackTrace()[0].getMethodName();
        log.info( "Start test {}", methodeName );
        Thread.sleep( 3000 );
        
        Counter counter = meterRegistry.find( "getterErrors" ).counter();
        double val = counter.count();
        
        cntWzgReceives = 0;
//        cntUfdInvalidValues = 0;
        double cnt1 = 0;
        double cnt2 = 0;
        double eps = 0.1;
        
        List<TlsTele> teleList = new ArrayList<>();
        addFile( teleList, "src/test/resources/TlsTelegrammeUnitTest/4_3_33_WZGGrundeinstellung_2.json", SM_A021_2_002_366, MQ_A02_1_040_F1 );
        
        sendTeles( teleList );
        Thread.sleep( 2000 );
        
        double val2 = counter.count();
        
        cnt1 = meterRegistry.find( "TelegramsReceived" ).counter().count();
        cnt2 = meterRegistry.find( "DataObjects.sended" ).counter().count();

        System.out.println( "-- Count of telegrams: " + cnt1 + " count of sended Objects " + cnt2 );
        
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> assertEquals( 1, cntWzgReceives ));
        assertEquals( val, val2 ); // no additional errors during getter

        System.out.println( "--Done " + methodeName + "-- " + cntWzgReceives );
        countFromOtherTests += cntUfdReceives;
        
    }

    @Test
    @DirtiesContext
    public void fg4T5aTest() throws IOException, InterruptedException, ExecutionException {
        
        String methodeName = new Throwable().getStackTrace()[0].getMethodName();
        log.info( "Start test {}", methodeName );
        Thread.sleep( 3000 );
        
        Counter counter = meterRegistry.find( "getterErrors" ).counter();
        double val = counter.count();
        
        cntWzgReceives = 0;
//        cntUfdInvalidValues = 0;
        double cnt1 = 0;
        double cnt2 = 0;
        double eps = 0.1;
        
        List<TlsTele> teleList = new ArrayList<>();
        addFile( teleList, "src/test/resources/TlsTelegrammeUnitTest/4_1_5_WZGDefekteLEDKetten_2.json", SM_A021_2_002_366, MQ_A02_1_040_F1 );
        
        sendTeles( teleList );
        Thread.sleep( 2000 );
        
        double val2 = counter.count();
        
        cnt1 = meterRegistry.find( "TelegramsReceived" ).counter().count();
        cnt2 = meterRegistry.find( "DataObjects.sended" ).counter().count();

        System.out.println( "-- Count of telegrams: " + cnt1 + " count of sended Objects " + cnt2 );
        
        await()
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> assertEquals( 1, cntWzgReceives ));
        assertEquals( val, val2 ); // no additional errors during getter

        System.out.println( "--Done " + methodeName + "-- " + cntWzgReceives );
        countFromOtherTests += cntUfdReceives;
        
    }

    @Test
    @DirtiesContext
    public void fg4T5bTest() throws IOException, InterruptedException, ExecutionException {
        
        String methodeName = new Throwable().getStackTrace()[0].getMethodName();
        log.info( "Start test {}", methodeName );
        Thread.sleep( 3000 );
        
        Counter counter = meterRegistry.find( "getterErrors" ).counter();
        double val = counter.count();
        
        cntWzgReceives = 0;
//        cntUfdInvalidValues = 0;
        double cnt1 = 0;
        double cnt2 = 0;
        double eps = 0.1;
        
        List<TlsTele> teleList = new ArrayList<>();
        addFile( teleList, "src/test/resources/TlsTelegrammeUnitTest/4_1_5_WZGDefekteLEDKetten_3.json", SM_A021_2_002_366, MQ_A02_1_040_F1 );
        
        sendTeles( teleList );
        Thread.sleep( 2000 );
        
        double val2 = counter.count();
        
        cnt1 = meterRegistry.find( "TelegramsReceived" ).counter().count();
        cnt2 = meterRegistry.find( "DataObjects.sended" ).counter().count();

        System.out.println( "-- Count of telegrams: " + cnt1 + " count of sended Objects " + cnt2 );
        
        await()
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> assertEquals( 1, cntWzgReceives ));
        assertEquals( val, val2 ); // no additional errors during getter

        System.out.println( "--Done " + methodeName + "-- " + cntWzgReceives );
        countFromOtherTests += cntUfdReceives;
        
    }

    @Test
    @DirtiesContext
    public void fg4T3aTest() throws IOException, InterruptedException, ExecutionException {
        
        String methodeName = new Throwable().getStackTrace()[0].getMethodName();
        log.info( "Start test {}", methodeName );
        Thread.sleep( 3000 );
        
        Counter counter = meterRegistry.find( "getterErrors" ).counter();
        double val = counter.count();
        
        cntWzgReceives = 0;
//        cntUfdInvalidValues = 0;
        double cnt1 = 0;
        double cnt2 = 0;
        double eps = 0.1;
        
        List<TlsTele> teleList = new ArrayList<>();
        addFile( teleList, "src/test/resources/TlsTelegrammeUnitTest/4_1_3_WZGDefekteLampen_2.json", SM_A021_2_002_366, MQ_A02_1_040_F1 );
        
        // this error is expected: Bad DE Block: DE Block too long: Node: 36361-24, Fg 4, Id 129, Typ 3, DE Block(including header): /06 0C 03 05 34 34 63 /
        sendTeles( teleList );
        Thread.sleep( 2000 );
        
        double val2 = counter.count();
        
        cnt1 = meterRegistry.find( "TelegramsReceived" ).counter().count();
        cnt2 = meterRegistry.find( "DataObjects.sended" ).counter().count();

        System.out.println( "-- Count of telegrams: " + cnt1 + " count of sended Objects " + cnt2 );
        
        await()
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> assertEquals( 4, cntWzgReceives ));
        assertEquals( val, val2 ); // no additional errors during getter

        System.out.println( "--Done " + methodeName + "-- " + cntWzgReceives );
        countFromOtherTests += cntUfdReceives;
        
    }

    @Test
    @DirtiesContext
    public void historyDataTest() throws IOException, InterruptedException, ExecutionException {
        String methodeName = new Throwable().getStackTrace()[0].getMethodName();
        log.info( "Start test {}", methodeName );
        cntVirtReceives = 0;
        Thread.sleep( 3000 );
        
        sendTeles( makeTeleListSubs() );
        double cnt1 = 0;
        double cnt2 = 0;
        double eps = 0.1;
        
        while (
                ((1 - eps) > (cnt1 = meterRegistry.find( "TelegramsReceived" ).counter().count()))
                ||
                ((1 - eps) > (cnt2 = meterRegistry.find( "DataObjects.sended" ).counter().count()))) {
            System.out.println( "Count of telegrams: " + cnt1 + " count of sended Objects " + cnt2 );
            Thread.sleep( 500 );
        }
        System.out.println( "-- Count of telegrams: " + cnt1 + " count of sended Objects " + cnt2 );
        
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> assertEquals( 1 + countFromOtherTests, cntVirtReceives ));

        System.out.println( "--Done historyDataTest-- " + cntVirtReceives );
        countFromOtherTests += cntVirtReceives;
        
    }
    
//    handleCommState( node, alive, queried, iid );
//    handleCommStateDetail( node, alive ? 1 : 0, iid );
//    handleCommStateDirect( node, alive, queried, iid );

    
    @Autowired
    Osi7Cfg cfg;
    
//    @Test
    public void printConfig() {
        String methodeName = new Throwable().getStackTrace()[0].getMethodName();
        log.info( "Start test {}", methodeName );

//        System.out.println( "--DevTree without EAs--" ); // enable Osi7Cfg for this code
//        cfg.printDevTree( System.out, 0, false, false );
        System.out.println( "--DevTree with EAs--" ); // enable Osi7Cfg for this code
        cfg.printDevTree( System.out, 0, true, false );
        
        log.info( "Done test {}", methodeName );
    }

    @Test// temporarily disabled - does not work in multiple run
    @DirtiesContext
    public void commStatusRstAliveTest() throws IOException, InterruptedException, ExecutionException {
        String methodeName = new Throwable().getStackTrace()[0].getMethodName();
        log.info( "Start test {}", methodeName );
        
        Thread.sleep( 3000 );
        
        cntFehlerDue = 0;

        int node = SM_A021_2_002_366; // I038_KRI_1 ~ 8436753
        boolean alive = true;
        boolean queried = false; // i.e. spontaneous
        Thread.sleep( 2000 );
        handleCommState( node, alive, queried );
        Thread.sleep( 2000 );
        handleCommStateDetail( node, alive ? 1 : 0 );
        
        Thread.sleep( 2000 );
        
        await()
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> assertEquals( 1, cntFehlerDue ));

//        Thread.sleep( 2000 );

        log.info( "Done test {}", methodeName );
    }

    @Test
    @DirtiesContext
    public void commStatusKriDeadTest() throws IOException, InterruptedException, ExecutionException { // kri dead => route dead
        String methodeName = new Throwable().getStackTrace()[0].getMethodName();
        log.info( "Start test {}", methodeName );
        Thread.sleep( 3000 );

        cntFehlerDue = 0;
        {
            int node = I038_KRI_1;
            boolean alive = false;
            boolean queried = false; // i.e. spontaneous
            
            handleCommState( node, alive, queried );
            handleCommStateDetail( node, alive ? 1 : 0 );
        }
        Thread.sleep( 2000 );
        
        await()
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() -> assertEquals( 48, cntFehlerDue ));

        Thread.sleep( 2000 );

        log.info( "Done test {}", methodeName );
    }

    @Test
    @DirtiesContext
    public void commStatusKriDeadQueriedTest() throws IOException, InterruptedException, ExecutionException { // kri dead => route dead
        String methodeName = new Throwable().getStackTrace()[0].getMethodName();
        log.info( "Start test {}", methodeName );
        Thread.sleep( 3000 );

        cntFehlerDue = 0;
        {
            int node = I038_KRI_1;
            boolean alive = false;
            boolean queried = true; // i.e.not spontaneous => no route dead settings
            
            handleCommState( node, alive, queried );
            handleCommStateDetail( node, alive ? 1 : 0 );
        }
        Thread.sleep( 1000 );
        
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> assertEquals( 0, cntFehlerDue ));

        Thread.sleep( 2000 );

        log.info( "Done test {}", methodeName );
    }
    
    @Autowired
    Transformer transformer;

    @Test
    @DirtiesContext
    public void gprsUzTest() throws IOException, InterruptedException, ExecutionException {
        String methodeName = new Throwable().getStackTrace()[0].getMethodName();
        log.info( "Start test {}", methodeName );
        Thread.sleep( 3000 );
        cntVirtReceives = 0;
        if (!(transformer instanceof TransformerImpl)) {
            fail("Transformer of unhandled tyep");
        }
        ((TransformerImpl)transformer).setGprsUZ( true );
        
        sendTeles( makeTeleListGprsSubs() );
        double cnt1 = 0;
        double cnt2 = 0;
        double eps = 0.1;
        
        while (
                ((1 - eps) > (cnt1 = meterRegistry.find( "TelegramsReceived" ).counter().count()))
                ||
                ((1 - eps) > (cnt2 = meterRegistry.find( "DataObjects.sended" ).counter().count()))) {
            System.out.println( "Count of telegrams: " + cnt1 + " count of sended Objects " + cnt2 );
            Thread.sleep( 500 );
        }
        System.out.println( "-- Count of telegrams: " + cnt1 + " count of sended Objects " + cnt2 );
        
        await()
            .atMost(5, TimeUnit.SECONDS)
                .untilAsserted( () -> assertEquals( 1 /* + countFromOtherTests */, cntVirtReceives ));

        System.out.println( "--Done historyDataTest-- " + cntVirtReceives );
        countFromOtherTests += cntVirtReceives;
        
    }
    
    @KafkaListener(topics = topicPrefix + de.heuboe.tls.receiver.core.datawriter.DataWriterImpl.TOPIC_PREFIX + "SYSFehlerDUE" + topicPostfix )
    public void receiveCS(final SYSFehlerDUEList dataIn) throws InterruptedException {
//        LOGGER.info("ThrId {} - received SYSFehlerDUEList payload='{}'", Thread.currentThread().getId(), dataIn );
        // @formatter:off
        LOGGER.info( "SYSFehlerDUE - Id: {} / Status: {} [0 is alive, 1 is dead, 2 is routeDead]", 
                dataIn.getElements( 0 ).getId(),
                dataIn.getElements( 0 ).getFehlercode() ); // @formatter:on
        cntFehlerDue++;
    }
    
    @KafkaListener(topics = topicPrefix + de.heuboe.tls.receiver.core.datawriter.DataWriterImpl.TOPIC_PREFIX + "UFDDeFehler" + topicPostfix )
    public void receive1(final UFDDeFehlerList dataIn) throws InterruptedException {
        LOGGER.info("ThrId {} - received UFDDeFehlerList payload='{}'", Thread.currentThread().getId(), dataIn );
        cntVirtReceives++;
    }
    
    @KafkaListener(topics = topicPrefix + de.heuboe.tls.receiver.core.datawriter.DataWriterImpl.TOPIC_PREFIX + "UFDNiederschlagsart" + topicPostfix )
    public void receive2(final UFDNiederschlagsartList dataIn) throws InterruptedException {
        LOGGER.info("ThrId {} - received UFDNiederschlagsartList payload='{}'", Thread.currentThread().getId(), dataIn );
        cntVirtReceives++;
    }
    
    // WZGDeFehlerNachg
    @KafkaListener(topics = topicPrefix + de.heuboe.tls.receiver.core.datawriter.DataWriterImpl.TOPIC_PREFIX + "WZGDeFehlerNachg" + topicPostfix )
    public void receive3(final WZGDeFehlerList dataIn) throws InterruptedException {
        LOGGER.info("ThrId {} - received WZGDeFehlerList payload='{}'", Thread.currentThread().getId(), dataIn );
        cntVirtReceives++;
    }
    
    // UFDLufttemperatur (invalid)
    @KafkaListener( topics = topicPrefix + de.heuboe.tls.receiver.core.datawriter.DataWriterImpl.TOPIC_PREFIX + "UFDLufttemperatur" + topicPostfix )
    public void receive4( final UFDLufttemperaturList dataIn ) throws InterruptedException {
        LOGGER.info( "ThrId {} - received UFDLufttemperaturList payload='{}'", Thread.currentThread().getId(), dataIn );
        if ( 1 == dataIn.getElementsCount() ) {
            UFDLufttemperatur ltObj = dataIn.getElements( 0 );
            if ( Float.MIN_VALUE + 1 > ltObj.getMesswert() ) {
                cntUfdInvalidValues++;
                LOGGER.warn( "Invalid value" );
            }
        }
        cntUfdReceives++;
    }
    
    // UFDLufttemperatur (invalid)
    @KafkaListener( topics = topicPrefix + de.heuboe.tls.receiver.core.datawriter.DataWriterImpl.TOPIC_PREFIX + "UFDStickstoffmonoxid" + topicPostfix )
    public void receive4b( final UFDStickstoffmonoxidList dataIn ) throws InterruptedException {
        LOGGER.info( "ThrId {} - received UFDStickstoffmonoxidList payload='{}'", Thread.currentThread().getId(), dataIn );
        if ( 1 == dataIn.getElementsCount() ) {
            UFDStickstoffmonoxid ltObj = dataIn.getElements( 0 );
            invalidValUfd = ltObj.getMesswert();
            if ( Float.MIN_VALUE + 1 > ltObj.getMesswert() ) {
                cntUfdInvalidValues++;
                LOGGER.warn( "Invalid value" );
            }
        }
        cntUfdReceives++;
    }
    
    // LVEErgebnisVersion3 (tNetto invalid)
    @KafkaListener( topics = topicPrefix + de.heuboe.tls.receiver.core.datawriter.DataWriterImpl.TOPIC_PREFIX + "LVEErgebnisVersion3" + topicPostfix )
    public void receive5( final LVEErgebnisVersion3List dataIn ) throws InterruptedException {
        LOGGER.info( "ThrId {} - received LVEErgebnisVersion3List payload='{}'", Thread.currentThread().getId(), dataIn );
        if ( 1 == dataIn.getElementsCount() ) {
            LVEErgebnisVersion3 ltObj = dataIn.getElements( 0 );
            assertEquals( 1, ltObj.getIntervallArt() );
            assertEquals( 4, ltObj.getIntervalllaenge() );
//            if ( Float.MIN_VALUE + 1 > ltObj.getMesswert() ) {
//                cntUfdInvalidValues++;
//                LOGGER.warn( "Invalid value" );
//            }
        }
        cntLve3Receives++;
    }
    
    private int cntWzgReceives = 0;
    
    // LVEErgebnisVersion3 (tNetto invalid)
    @KafkaListener( topics = topicPrefix + de.heuboe.tls.receiver.core.datawriter.DataWriterImpl.TOPIC_PREFIX + "WZGGrundeinstellung" + topicPostfix )
    public void receive6( final WZGGrundeinstellungList dataIn ) throws InterruptedException {
        LOGGER.info( "ThrId {} - received WZGGrundeinstellungList payload='{}'", Thread.currentThread().getId(), dataIn );
        if ( 1 == dataIn.getElementsCount() ) {
            WZGGrundeinstellung ltObj = dataIn.getElements( 0 );
        }
        cntWzgReceives++;
    }
    
    // LVEErgebnisVersion3 (tNetto invalid)
    @KafkaListener( topics = topicPrefix + de.heuboe.tls.receiver.core.datawriter.DataWriterImpl.TOPIC_PREFIX + "WZGDefekteLampen" + topicPostfix )
    public void receive6b( final WZGDefekteLampenList dataIn ) throws InterruptedException { 
        LOGGER.info( "ThrId {} - received WZGDefekteLampenList payload='{}'", Thread.currentThread().getId(), dataIn );
        if ( 1 == dataIn.getElementsCount() ) {
            WZGDefekteLampen ltObj = dataIn.getElements( 0 );
        }
        cntWzgReceives++;
    }
    
    // LVEErgebnisVersion3 (tNetto invalid)
    @KafkaListener( topics = topicPrefix + de.heuboe.tls.receiver.core.datawriter.DataWriterImpl.TOPIC_PREFIX + "WZGDefekteLEDKetten" + topicPostfix )
    public void receive6c( final WZGDefekteLEDKettenList dataIn ) throws InterruptedException {
        LOGGER.info( "ThrId {} - received WZGDefekteLEDKettenList payload='{}'", Thread.currentThread().getId(), dataIn );
        if ( 1 == dataIn.getElementsCount() ) {
            WZGDefekteLEDKetten ltObj = dataIn.getElements( 0 );
        }
        cntWzgReceives++;
    }
    
    boolean oeFound = false;
    
    @KafkaListener( topics = topicPrefix + de.heuboe.tls.receiver.core.datawriter.DataWriterImpl.TOPIC_PREFIX + "WZGStellzustand" + topicPostfix )
    public void receive6d( final WZGStellzustandList dataIn ) throws InterruptedException {
        LOGGER.info( "ThrId {} - received WZGStellzustandList payload='{}'", Thread.currentThread().getId(), dataIn );
        if ( 1 == dataIn.getElementsCount() ) {
            WZGStellzustand ltObj = dataIn.getElements( 0 );
            if ( "Vösendorf".equals( ltObj.getTextzeichen() ) ) {
                oeFound = true;
            }
            int i = 0; i = i + 1;
        }
        cntWzgReceives++;
    }
    
    // ===========================================================================================

    
    // location/distance of a SM on A2/A23 and DEs existing there 
    static final int SM_A021_2_002_366 = 9308440;
    static final int SWS_A021_2_100_NS = 14;
    static final int SWS_A021_2_100_NI = 15;
    
    static final int MQ_A02_1_040_F1 = 1;

    static final int SM_A23_0_010 = 11373307;
    static final int WTA_A23_1_010_B = 2;
    
    static final int I038_KRI_1 = 8436753;
   
    
    // do some adjustments to the telegrams in roder to have tests for FG1 in UZ Kaernten
    private TlsTele adjustSTel( TlsTele tel, int forNode, int forDE ) {
        tel.setLogAddress( forNode );
        tel.getEtels().forEach( etel -> adjustETel( etel, forDE ) );
        return tel;
    }
    
    private int futureSecs = 0;
    
    private void setFutureTime( int secs ) {
        futureSecs = secs;
    }
    
    // duplicate the given de block and add a timestamp
    private void adjustETel( TlsETel etel, int forDE ) {
        int id = etel.getTlsId();
        int fg = etel.getFg();
        if (1 == etel.getDeblockCount()) {
            TlsDeBlock deblock = etel.getDeblocks().get( 0 );
            
            TlsDeBlock deblockClone = new TlsDeBlock( deblock.getParent(), forDE, deblock.getDeTyp() );
            deblockClone.setContent( deblock.getContent() );
            int typ = deblock.getDeTyp();
            
            // ACHTUNG - Hier nur Typ 48 Zeitstempel
            TlsDeBlock zst48 = new TlsDeBlock( deblock.getParent(), 255, 48 );
            TlsDeBlock zst30 = new TlsDeBlock( deblock.getParent(), 255, 30 );
            TlsDeBlock zst31 = new TlsDeBlock( deblock.getParent(), 255, 31 );
            
            TimeZone defaultTimeZone = TimeZone.getTimeZone( "GMT+01:00" );
            GregorianCalendar cal = new GregorianCalendar( defaultTimeZone );
            if ( 0 != futureSecs) {
                long sec = cal.getTimeInMillis() + (futureSecs * 1000);
                cal.setTimeInMillis( sec );
            }
            {
                byte[] zstCont = new byte[5]; 
                zstCont[0] = (byte) cal.get( GregorianCalendar.HOUR_OF_DAY );
                zstCont[1] = (byte) cal.get( GregorianCalendar.MINUTE );
                zstCont[2] = (byte) cal.get( GregorianCalendar.SECOND );
                zstCont[3] = (byte) 1; // Art des Intervall
                zstCont[4] = (byte) 4; // Länge des Intervall
                zst48.setContent( zstCont );
            }
            {
                byte[] zstCont = new byte[3]; 
                zstCont[0] = (byte) cal.get( GregorianCalendar.HOUR_OF_DAY );
                zstCont[1] = (byte) cal.get( GregorianCalendar.MINUTE );
                zstCont[2] = (byte) cal.get( GregorianCalendar.SECOND );
                zst30.setContent( zstCont );
            }
            {
                byte[] zstCont = new byte[6]; 
                zstCont[0] = (byte) cal.get( GregorianCalendar.HOUR_OF_DAY );
                zstCont[1] = (byte) cal.get( GregorianCalendar.MINUTE );
                zstCont[2] = (byte) cal.get( GregorianCalendar.SECOND );
                zstCont[3] = (byte) cal.get( GregorianCalendar.DAY_OF_MONTH );
                zstCont[4] = (byte) 1; // Folgenummer low byte
                zstCont[5] = (byte) 0; // Folgenummer high byte
                zst31.setContent( zstCont );
            }
            
            TlsDeBlock zst =null; 
            zst = zst30;
            if (132 == id && 1 == fg) {
                if (!omitInterval) {
                    zst = zst48;
                } else {
                    zst = zst30;
                }
            } else if ( 4 == fg ) {
                id &= 0x7f;
                switch ( id ) {
                    case 1:
                    case 33:
                    case 2:
                    case 34:
                    case 5:
                    case 37:
                    case 3:
                    case 35:
                        zst = zst31;
                        break;
                    default:
                        break;
                }
                if ( (1 == id) && (3 == typ) ) {
                    zst = zst30;
                }
            }
            
            etel.getDeblocks().set( 0, zst );
            etel.getDeblocks().add( deblockClone );
//            etel.getDeblocks().add( deblockClone2 );
        }
    }
    
    private static IDGenerator idGenerator;
    static {
        idGenerator = new IDGenerator();
    }

    // the whole set of telegrams is generated
    public void sendTeles( List<TlsTele> teleList ) throws InterruptedException, ExecutionException, IOException {
        
        for ( TlsTele tel : teleList ) {
            Builder builder = TeleSReceived.newBuilder();
            long t = System.currentTimeMillis();
            ByteString telBytes = ByteString.copyFrom( tel.getBytes() );
            
            TlsETel etelInfo = tel.getEtels().get( 0 );
            int typ = 999;
            if (etelInfo.getDeblockCount() >= 2) {
                typ = etelInfo.getDeblocks().get( 1 ).getDeTyp();
            }
            
            // @formatter:off
            builder
                .setIid( idGenerator.newID() ) 
                .setTimeRcvd( fromMillis( t ) )
                .setFlags( 1 )
                .setIfaceKey( 1001 )
                .setRealAddress( 0x010203 )
                .setTlsSTel( telBytes )
                ;
            // @formatter:on

            TeleSReceived telOut = builder.build();
            LOGGER.info("Send tel fg {} id {} typ {}", etelInfo.getFg(), etelInfo.getTlsId(), typ );
//            send( telOut, "TeleSReceived" );
            send( telOut, topicNameFromSpring );
        }
        
        Thread.sleep( 1000 );
     }

    private void handleCommState( int node, boolean alive, boolean queried ) throws InterruptedException, ExecutionException {
        byte[] tele = new byte[6];
        tele[0] = (byte) (node & 0xff);
        tele[1] = (byte)((node & 0xff00) >> 8);
        tele[2] = (byte)((node & 0xff0000) >> 16);
        tele[3] = 3;                            // Kommunikationstatus
        tele[4] = (byte)(alive   ? 0 : 1);                  
        tele[5] = (byte)(queried ? 1 : 0);

        PTeleSReceivedBuilder builder = PTeleSReceived.builder();
        // @formatter:off
        builder
            .flags( 0 )
            .ifaceKey( 0 )
            .realAddress( 0 )
            .timeRcvd( Instant.now()  )
            .tlsSTel( ByteString.copyFrom( tele ) )
            .iid( idGenerator.newID() )
        ;
        // @formatter:on

        PTeleSReceived commStateReceived = builder.build();
        
        // send tele to topic
        send( PTeleSReceived.to( commStateReceived ), /*this.topic*/ topicNameFromSpring );
    }

    private void handleCommStateDetail( int node, int detail ) throws InterruptedException, ExecutionException {
        byte[] tele = new byte[6];
        tele[0] = (byte) (node & 0xff);
        tele[1] = (byte)((node & 0xff00) >> 8);
        tele[2] = (byte)((node & 0xff0000) >> 16);
        tele[3] = 5;                            // Kommunikationstatus Detail
        tele[4] = (byte) detail;                  
        tele[5] = 0;

        PTeleSReceivedBuilder builder = PTeleSReceived.builder();
        // @formatter:off
        builder
            .flags( 0 )
            .ifaceKey( 0 )
            .realAddress( 0 )
            .timeRcvd( Instant.now()  )
            .tlsSTel( ByteString.copyFrom( tele ) )
            .iid( idGenerator.newID() )
        ;
        // @formatter:on

        PTeleSReceived commStateReceived = builder.build();
        
        // send tele to topic
        send( PTeleSReceived.to( commStateReceived ), topicNameFromSpring );
    }

    // send the message to the kafka server
    public void send(final TeleSReceived message, String topic) throws InterruptedException, ExecutionException {
//        LOGGER.info("Sending: {}", message)
        /*SendResult<String, Object> sendRes =*/ kafkaTemplate.send(
                MessageBuilder
                .withPayload(message)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .build())
                    .get();
//        sendRes.getProducerRecord()
//        LOGGER.info("Sending result: {}", sendRes)
    }

/*
    static final int SM_A021_2_002_366 = 9308440;
    static final int SWS_A021_2_100_NS = 14;
 */
    
    // build a list of tls telegrams according to the files in the subfolder 'TlsTelegramme'
    private List<TlsTele> makeTeleList() throws IOException {
        List<TlsTele> res = new ArrayList<>();
        String dirName = "TlsTelegramme";
        File dir = new File( dirName );
        for ( File tlsFile : dir.listFiles() ) {
            String filename = tlsFile.getName();
            if ( filename.matches( "[1-9].*\\.json" ) ) {
                TlsTele newTele = TlsTele.loadJs( tlsFile );
                res.add( adjustSTel( newTele, SM_A021_2_002_366, SWS_A021_2_100_NS ) );
            }
        }
        return res;
    }
    
    // build a list of tls telegrams according to the files in the subfolder 'TlsTelegramme'
    private List<TlsTele> makeTeleListVirt() throws IOException {
        List<TlsTele> res = new ArrayList<>();
        addFile( res, "src/test/resources/TlsTelegrammeUnitTest/3_1_1_UFDDeFehler.json", SM_A021_2_002_366, SWS_A021_2_100_NS );
        addFile( res, "src/test/resources/TlsTelegrammeUnitTest/3_1_1_UFDDeFehler.json", SM_A021_2_002_366, SWS_A021_2_100_NI );
        addFile( res, "src/test/resources/TlsTelegrammeUnitTest/3_4_71_UFDNiederschlagsart.json", SM_A021_2_002_366, SWS_A021_2_100_NS );
        addFile( res, "src/test/resources/TlsTelegrammeUnitTest/3_1_1_UFDDeFehler-job.json", SM_A021_2_002_366, SWS_A021_2_100_NI );
        return res;
    }

    private void addFile( List<TlsTele> res, String filename, int forNode, int forDE ) throws IOException {
        File f = new File( filename );
        String name = f.getName();
        if ( name.matches( "[1-9].*\\.json" ) ) {
            TlsTele newTele = TlsTele.loadJs( f );
            res.add( adjustSTel( newTele, forNode, forDE ) );
        } else {
            LOGGER.warn( "Testfile skipped: Not maching pattern '[1-9].*\\\\.json': '{}'", filename );
        }
    }
    
    // build a list of tls telegrams according to the files in the subfolder 'TlsTelegramme'
    private List<TlsTele> makeTeleListSubs() throws IOException {
        List<TlsTele> res = new ArrayList<>();
        addFileH( res, "src/test/resources/TlsTelegrammeUnitTest/4_1_1_WZGDeFehler.json", SM_A23_0_010, WTA_A23_1_010_B );
        return res;
    }
    
    // historic data
    private void addFileH( List<TlsTele> res, String filename, int forNode, int forDE ) throws IOException {
        File f = new File( filename );
        String name = f.getName();
        if ( name.matches( "[1-9].*\\.json" ) ) {
            TlsTele newTele = TlsTele.loadJs( f );
            newTele.getEtels().get( 0 ).setTlsId( newTele.getEtels().get( 0 ).getTlsId() + 32 );
            res.add( adjustSTel( newTele, forNode, forDE ) );
        } else {
            LOGGER.warn( "Testfile skipped: Not maching pattern '[1-9].*\\\\.json': '{}'", filename );
        }
    }
    
    // build a list of tls telegrams according to the files in the subfolder 'TlsTelegramme'
    private List<TlsTele> makeTeleListGprsSubs() throws IOException {
        List<TlsTele> res = new ArrayList<>();
        addFileG( res, "src/test/resources/TlsTelegrammeUnitTest/4_1_1_WZGDeFehler.json", SM_A23_0_010, WTA_A23_1_010_B );
        return res;
    }
    
    // gprs uz data
    private void addFileG( List<TlsTele> res, String filename, int forNode, int forDE ) throws IOException {
        File f = new File( filename );
        String name = f.getName();
        if ( name.matches( "[1-9].*\\.json" ) ) {
            TlsTele newTele = TlsTele.loadJs( f );
            newTele.getEtels().get( 0 ).setTlsId( newTele.getEtels().get( 0 ).getTlsId() + 32 );
            TlsDeBlock debl = newTele.getEtels().get( 0 ).getDeblocks().get( 0 );
            byte[] cont = debl.getContent();
            byte[] newcont = Arrays.copyOf( cont, cont.length + 4); // 0x60F6F386 ~ Di 20.7.2021 18:02:14 localtime
            newcont[newcont.length-4] = 0x60;
            newcont[newcont.length-3] = (byte)0xf6;
            newcont[newcont.length-2] = (byte)0xf3;
            newcont[newcont.length-1] = (byte)0x86;
            debl.setContent( newcont );
            res.add( adjustSTel( newTele, forNode, forDE ) );
        } else {
            LOGGER.warn( "Testfile skipped: Not maching pattern '[1-9].*\\\\.json': '{}'", filename );
        }
    }
    
}
