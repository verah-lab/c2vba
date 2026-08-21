package de.heuboe.asfinag.vmis2.tls.rcv.cfgmock;

import static com.google.protobuf.util.Timestamps.fromMillis;
import static org.springframework.kafka.test.utils.ContainerTestUtils.waitForAssignment;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import eu.vmis_ehe.vmis2.configservice.AreaChange;
import eu.vmis_ehe.vmis2.configservice.ItemChange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.EnableKafka;
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
import de.heuboe.tls.receiver.interfaces.SystemMessageManagement;
import de.heuboe.tls.tel.io.TeleSReceived;
import de.heuboe.tls.tel.io.TeleSReceived.Builder;
import de.heuboe.tls.tel.io.pojo.PTeleSReceived;
import de.heuboe.tls.tel.io.pojo.PTeleSReceived.PTeleSReceivedBuilder;
import de.heuboe.tls.tlstele.TlsDeBlock;
import de.heuboe.tls.tlstele.TlsETel;
import de.heuboe.tls.tlstele.TlsTele;
import eu.vmis_ehe.vmis2.configservice.DataChange;
import eu.vmis_ehe.vmis2.configservice.DataChanges;
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
        locations="classpath:testChgConf.properties"               // !!! properties
        )

@ContextConfiguration( classes = { 
      de.heuboe.asfinag.vmis2.tls.rcv.cfgmock.config.Vmis2SystemMessageManagementProperties.class,
      de.heuboe.tls.receiveconverter.InitAllInit.class,
      
      de.heuboe.tls.receiver.core.config.Config.class,
      de.heuboe.tls.receiver.core.config.ConfigTimeoffset.class,
      de.heuboe.tls.receiver.core.config.MetricsConfig.class,
      de.heuboe.tls.receiver.core.config.KafkaConfig.class,
      de.heuboe.tls.receiver.core.config.TransformerConfig.class,
      de.heuboe.tls.receiver.core.config.ConfigOsi7Config.class,
      
      de.heuboe.asfinag.vmis2.tls.rcv.cfgmock.config.ConfChg.class,
      de.heuboe.asfinag.vmis2.tls.rcv.cfgmock.MockedCfgGetterChg.class, // A2/A23
      de.heuboe.asfinag.vmis2.tls.rcv.cfgmock.config.TransformationRulesConfig.class
      } )

//==============

//@ContextConfiguration( classes = {
//        de.heuboe.tls.sender.test.config.ConfigChg.class,
//    
//        de.heuboe.tls.sender.test.config.ConfigSenderVmis2AlarmProperties.class,
//        de.heuboe.tls.sender.config.SenderConfig.class,
//        de.heuboe.tls.sender.config.ConfigProperties.class,
//        de.heuboe.tls.sender.config.KafkaConfig.class} )
//
//@SpringBootTest(
//        properties = {
//                "de.heuboe.tls.sender.config.aliveWhenNoData=true", // wins over property from file
//                "de.heuboe.tls.sender.config.forceAlive=true" // wins over property from file
//        }
//        )


@Slf4j
@EnableConfigurationProperties
//@SpringJUnitConfig
@EnableKafka
@EnableAutoConfiguration(exclude=MongoAutoConfiguration.class)
class TestCfgChg  {
    
    // Embedded Kafka ++ vvv
    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    MeterRegistry meterRegistry;
    // Embedded Kafka ++ ^^^

    @Autowired
    @Qualifier("uzId") String uzId;// = "UZ_Kaernten";
    
    @Value("#{changeTopic}")
    String chgTopic;
    
    @Autowired
    MockedCfgGetterChg cfgGetter; // old default config
    
    @Value("${de.heuboe.asfinag.tls.receiver.inputTopic}")
    String topicNameFromSpring;
    
    boolean initDone = false;
    double numTeleExpected = 0;
    
    @Autowired
    SystemMessageManagement smm;

    @BeforeEach
    public void init() throws IOException {
        for ( final MessageListenerContainer messageListenerContainer : this.registry.getListenerContainers() ) {
            int ecpectedPartitions = messageListenerContainer.getContainerProperties().getTopics().length * embeddedKafkaBroker.getPartitionsPerTopic();
            waitForAssignment( messageListenerContainer, ecpectedPartitions );
            if ( !initDone ) {
                initDone = true;
            }
        }
        log.info( "junit init done (BeforeEach)" );
    }
    
    private static int alarmCount = 0;

    // =========================================================================================== generateLVEAbrufPufferInhaltList()
    /*
     what we test here:
     sende teles with objects (one) that ist not known in the config at start
     load new config data with objects that lead to failure before
     all teles succeed in receive => good
     */
    
    @Test
    @DirtiesContext
    void chgCfgTest() throws Exception {
        String methodeName = new Throwable().getStackTrace()[0].getMethodName();
        log.info( "Start test {}", methodeName );
        
        List< TlsTele > telgrams = makeTeleListVirt();
        sendTeles( telgrams );
        double cnt1 = 0;
        double cnt2 = 0;
        double eps = 0.1;
        
        while(
                 ((4 - eps) > (cnt1 = meterRegistry.find( "TelegramsReceived" ).counter().count()))
                 ||
                 ((2 - eps) >
                  (cnt2 = meterRegistry.find( "DataObjects.sended" ).counter().count())) ) {
            System.out.println(
                     "Count of telegrams: " + cnt1 + " count of sended Objects " + cnt2 );
            Thread.sleep( 500 );
        }
        System.out.println( "-- Count of telegrams: " + cnt1 + " count of sended Objects " + cnt2 );
        
        cfgGetter.switchConfig( null, null, "tlsb-devs2.json", "tlsb-cabs2.json" ); // add a virtual de
        // now using another configuration
        
        /* init change */
        {
            eu.vmis_ehe.vmis2.configservice.DataChange.Builder dcBuilder = DataChange.newBuilder();
            dcBuilder.setRVmzId( "WIE" );
            
            dcBuilder.addRoadChanges(
                     AreaChange.newBuilder()
                              .addGeoChangesValue( 1 )
                              .addFeatureChanges(
                                       ItemChange.newBuilder().addAqTypesValue( 1 )
                                                .build() )
                              .build() );
            
            eu.vmis_ehe.vmis2.configservice.DataChanges.Builder b = DataChanges.newBuilder();
            b.addDataChanges( dcBuilder.build() );
            b.setIid( "HänselUndGretel" );
            
            DataChanges changes = b.build();
            
            kafkaTemplate.send(
                     MessageBuilder
                              .withPayload( changes )
                              .setHeader( KafkaHeaders.TOPIC, chgTopic )
                              .setHeader( KafkaHeaders.KEY, "WIE" )
                              .build() );
        }
        
        Thread.sleep( 5000 );
        
        log.info( "\n\nConfig should have changed\n" );
        sendTeles( telgrams );

        Thread.sleep( 500 );

        log.info( "\n\nNew teles were sended\n\n" );
        
        while(
                 ((8 - eps) > (cnt1 = meterRegistry.find( "TelegramsReceived" ).counter().count()))
                 ||
                 ((6 - eps) >
                  (cnt2 = meterRegistry.find( "DataObjects.sended" ).counter().count())) ) {
            System.out.println(
                     "Count of telegrams: " + cnt1 + " count of sended Objects " + cnt2 );
            Thread.sleep( 500 );
        }
        log.info( "All teles received as expected" );
        System.out.println( "-- Count of telegrams: " + cnt1 + " count of sended Objects " + cnt2 );

//        await()
//            .atMost(5, TimeUnit.SECONDS)
//            .untilAsserted(() -> assertEquals( 9 + countFromOtherTests, cntVirtReceives ));
        
        System.out.println( "--Done " + methodeName + "-- " );
    }
    
//    @Test
//    @DirtiesContext // Kafka
//    public void fg1TestAbruf() throws Exception {
//        log.info( "Start test {}", new Throwable().getStackTrace()[0].getMethodName() );
//        Map<String, Object> headerMap;
//        double count;
//        double eps = 0.1;
//        log.debug( "meterRegistry {} counter {}", System.identityHashCode(meterRegistry), System.identityHashCode(meterRegistry.find( "DataObjects.received.all" ).counter()) );
//        Thread.sleep( 2000 );
//        
//        // do some things that will not be right in default configuration
//        
//        {
//            headerMap = TestDataProvider.makeTopicHeader( "LVEAbrufPufferInhaltSoll" );
//            LVEAbrufPufferInhaltList apl = generateLVEAbrufPufferInhaltListChg1();
//            trac.setTeleName( "LVEAbrufPufferInhalt_chg1" );
//            log.debug( "send data for {}",trac.getTeleName());
//            kos.send( headerMap , apl, true );
//            numTeleExpected++;
//            TestDataProvider.reCheckReset();
//            
//            count = meterRegistry.find( "DataObjects.received.all" ).counter().count();
//            while ( !TestDataProvider.reCheckReached() && (numTeleExpected - eps) > count ) {
//                Thread.sleep( 500 );
//                count = meterRegistry.find( "DataObjects.received.all" ).counter().count();
//            }
//            
//            count = meterRegistry.find( "DataObjectsTransformedNormal.adrFail" ).counter().count();
//            while ( !TestDataProvider.reCheckReached() && (1.0 - eps) > count ) {
//                Thread.sleep( 500 );
//                count = meterRegistry.find( "DataObjects.received.all" ).counter().count();
//            }
//            
//            LVEAbrufPufferInhalt el = apl.getElements( 0 );
//            String sendId = el.getId();
//            log.info( "" );
//            log.info( "DataObjectsTransformedNormal.adrFail reached one. I.e. object {} was not found", sendId );
//            log.info( "" );
//            smm.sendMessage( String.format( "DataObjectsTransformedNormal.adrFail reached one. I.e. object %s was not found", sendId ) );
//            
//            assertTrue( count - eps > 0);
//        }
//
//        cfgGetter.switchConfig( null, null, "tlsb-devs2.json", "tlsb-cabs2.json" ); // add a virtual de
//        // now using another configuration
//        
//        /* init change */ {
//            eu.vmis_ehe.vmis2.configservice.DataChange.Builder b1 = DataChange.newBuilder();
//            Builder b = DataChanges.newBuilder();
//            b1.setRVmzId( "WIE" );
////            b1.setRVmzId( "UZ_Kaernten" );
//            b.addDataChanges( b1.build() );
//            b.setIid( "HänselUndGretel" );
//            DataChanges changes = b.build();
//            
//            headerMap = TestDataProvider.makeTopicHeader( chgTopic );
//            kos.send( headerMap, changes, true );
//        }
//        
//        Thread.sleep( 2000 );
//        
//        // the same things should be right now
//        
//        {
//
//            headerMap = TestDataProvider.makeTopicHeader( "LVEAbrufPufferInhaltSoll" );
//            LVEAbrufPufferInhaltList apl = generateLVEAbrufPufferInhaltListChg1();
//            trac.setTeleName( "LVEAbrufPufferInhalt_chg2" );
//            log.debug( "send data for {}",trac.getTeleName());
//            kos.send( headerMap , apl, true );
//            numTeleExpected++;
//            TestDataProvider.reCheckReset();
//            
//            count = meterRegistry.find( "DataObjects.received.all" ).counter().count();
//            while ( !TestDataProvider.reCheckReached() && (numTeleExpected - eps) > count ) {
//                Thread.sleep( 500 );
//                count = meterRegistry.find( "DataObjects.received.all" ).counter().count();
//            }
//        }
//
//
//        TestDataProvider.reCheckReset();
//        while ( !TestDataProvider.reCheckReached() && (1 > trac.getTelesReceived()) ) {
//            Thread.sleep( 500 );
//        }
//
//        assertEquals( 1, trac.getTelesReceived() );
//        log.info( "Done test" );
//
//        if ( trac.isDiffs() ) {
////            log.error( "There are differences in " + trac.getDifferencingBlocks() );
//            fail( "There are differences in " + trac.getDifferencingBlocks() );
//        }
//        int v = alarmCount;
//        Thread.sleep( 1 );
//    }
    
    // location/distance of a SM on A2/A23 and DEs existing there 
    static final int SM_A021_2_002_366 = 9308440;
    static final int SWS_A021_2_100_NS = 14;
    static final int SWS_A021_2_100_NI = 15;

    static final int SM_A23_0_010 = 11373307;
    static final int WTA_A23_1_010_B = 2;
    
    static final int I038_KRI_1 = 8436753;
   
    
    // do some adjustments to the telegrams in roder to have tests for FG1 in UZ Kaernten
    private TlsTele adjustSTel( TlsTele tel, int forNode, int forDE ) {
        tel.setLogAddress( forNode );
        tel.getEtels().forEach( etel -> adjustETel( etel, forDE ) );
        return tel;
    }
    
    // duplicate the given de block and add a timestamp
    private void adjustETel( TlsETel etel, int forDE ) {
//        if (1 != etel.getFg()) {
//            return;
//        }
        int id = etel.getTlsId();
        int fg = etel.getFg();
        if (1 == etel.getDeblockCount()) {
            TlsDeBlock deblock = etel.getDeblocks().get( 0 );
            
            TlsDeBlock deblockClone = new TlsDeBlock( deblock.getParent(), forDE, deblock.getDeTyp() );
            deblockClone.setContent( deblock.getContent() );
            int typ = deblock.getDeTyp();
            
//            TlsDeBlock deblockClone2 = new TlsDeBlock( deblock.getParent(), SM_A02_0_800_F2_DE, deblock.getDeTyp() );
//            deblockClone2.setContent( deblock.getContent() );
            
            // ACHTUNG - Hier nur Typ 48 Zeitstempel
            TlsDeBlock zst48 = new TlsDeBlock( deblock.getParent(), 255, 48 );
            TlsDeBlock zst30 = new TlsDeBlock( deblock.getParent(), 255, 30 );
            TlsDeBlock zst31 = new TlsDeBlock( deblock.getParent(), 255, 31 );
            
            GregorianCalendar cal = new GregorianCalendar();
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
                zst = zst48;
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
            log.info("Send tel fg {} id {} typ {}", etelInfo.getFg(), etelInfo.getTlsId(), typ );
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
//        log.info("Sending: {}", message)
        /*SendResult<String, Object> sendRes =*/ kafkaTemplate.send(
                MessageBuilder
                .withPayload(message)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .build())
                    .get();
//        sendRes.getProducerRecord()
//        log.info("Sending result: {}", sendRes)
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
            log.warn( "Testfile skipped: Not maching pattern '[1-9].*\\\\.json': '{}'", filename );
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
            log.warn( "Testfile skipped: Not maching pattern '[1-9].*\\\\.json': '{}'", filename );
        }
    }
    
}

