package de.heuboe.tls.wancom.kcfg.test;

import static com.google.protobuf.util.Timestamps.fromMillis;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.google.protobuf.ByteString;

import de.heuboe.idgenerator.generator.IDGenerator;
import de.heuboe.tls.cfglib.Osi7Cfg;
import de.heuboe.tls.iface.lib.IfaceApp;
import de.heuboe.tls.tel.io.TeleSToSend;
import de.heuboe.tls.tel.io.TeleSToSend.Builder;
import de.heuboe.tls.tlstele.TlsBadTele;
import de.heuboe.tls.tlstele.TlsDeBlock;
import de.heuboe.tls.tlstele.TlsETel;
import de.heuboe.tls.tlstele.TlsTele;
import de.heuboe.tls.tlstele.meta.Direction;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest

@DirtiesContext // Kafka
// @formatter:off
@EmbeddedKafka(
        partitions = 1,
        controlledShutdown = false,
                ports = {55777},
                zookeeperPort = 55771,
//                ports = {50637},
        // ${random.int} => always begin with a fresh directory. i.e. no messages in broker
        // keep directory for instance tin order to preload
        brokerProperties = {"log.dir=target/kafka${random.int}"}
        ) // @formatter:on

@TestPropertySource(
        properties = {
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "spring.kafka.consumer.group-id=iface1EmbeddedKafkaTest",
                "spring.kafka.listener.missing-topics-fatal=false"},
        locations="classpath:test2.properties"               // !!! properties
        )

// @formatter:off
@ContextConfiguration( classes = {
        de.heuboe.tls.ifacewancom.config.Config.class,
        de.heuboe.tls.ifacewancom.config.ConfigIfaceApp.class,
        de.heuboe.tls.ifacewancom.config.JacksonConfig.class,
        de.heuboe.tls.ifacewancom.config.KafkaConfig.class,
        de.heuboe.tls.ifacewancom.config.MetricsConfig.class,

        de.heuboe.tls.wancom.kcfg.test.config.MockedCfgUZA2.class,
//        de.heuboe.tls.ifacewancom.config.Properties.class,
    } ) // @formatter:on


@Slf4j
@EnableConfigurationProperties
@SpringJUnitConfig
@EnableKafka
@EnableAutoConfiguration(exclude=MongoAutoConfiguration.class)
public class Test2 {

    @Autowired
    private IfaceApp app;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired @Qualifier( "sndTopic" )
    private String sendTopic;

    @Autowired
    Osi7Cfg cfg;

    // location/distance of a SM on A2/A23 and DEs existing there
    static final int SM_A021_2_002_366 = 9308440;
    static final int SWS_A021_2_100_NS = 14;
    static final int SWS_A021_2_100_NI = 15;

    static final int SM_A23_0_010 = 11373307;
    static final int WTA_A23_1_010_B = 2;

    static final int KRI_A23 = 32962*256 + 49;

    private static final byte WRONG_JOB_MAX = (byte) 201;
    private static final byte WRONG_JOB_MIN = (byte) 1;

    @Test
    public void theTest2() throws Exception {
//        cfg.printDevTree( System.out, 99,false, true );

        assertNotNull( app );

        Thread.sleep( 1500 );

        boolean keepRunningForDebug = false;

        for ( int i = 0; i < 20; ++i ) {
            Thread.sleep( 500 );
            if ( keepRunningForDebug ) {
                --i;
            }
        }

        System.out.println( "Success" );
    }

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
        return res;
    }

    // build a list of tls telegrams according to the files in the subfolder 'TlsTelegramme'
    private List<TlsTele> makeTeleListSubs() throws Exception {
        List<TlsTele> res = new ArrayList<>();
        addFileH( res, "src/test/resources/TlsTelegrammeUnitTest/4_1_1_WZGDeFehler.json", SM_A23_0_010, WTA_A23_1_010_B );
        return res;
    }

    // build a list of tls telegrams according to the files in the subfolder 'TlsTelegramme'
    private List<TlsTele> makeTeleListSubsWrongJobMax() throws Exception {
        List<TlsTele> res = new ArrayList<>();
        addFileHWJ( res, "src/test/resources/TlsTelegrammeUnitTest/4_1_1_WZGDeFehler.json", SM_A23_0_010, WTA_A23_1_010_B, true );
        return res;
    }

    // build a list of tls telegrams according to the files in the subfolder 'TlsTelegramme'
    private List<TlsTele> makeTeleListSubsWrongJobMin() throws Exception {
        List<TlsTele> res = new ArrayList<>();
        addFileHWJ( res, "src/test/resources/TlsTelegrammeUnitTest/4_1_1_WZGDeFehler.json", SM_A23_0_010, WTA_A23_1_010_B, false );
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

    // historic data
    private void addFileH( List<TlsTele> res, String filename, int forNode, int forDE ) throws IOException, TlsBadTele {
        File f = new File( filename );
        String name = f.getName();
        if ( name.matches( "[1-9].*\\.json" ) ) {
            TlsTele newTele = TlsTele.loadJs( f );
            newTele.getEtels().get( 0 ).setTlsId( newTele.getEtels().get( 0 ).getTlsId() + 32 );

            byte[] b = newTele.getBytes();
            TlsTele newTele2 = new TlsTele( new Date(), Direction.SEND, forNode, b, 0, b.length );

            res.add( adjustSTel( newTele2, forNode, forDE ) );
        } else {
            log.warn( "Testfile skipped: Not maching pattern '[1-9].*\\\\.json': '{}'", filename );
        }
    }

    // historic data
    private void addFileHWJ( List<TlsTele> res, String filename, int forNode, int forDE, boolean failMax ) throws IOException, TlsBadTele {
        File f = new File( filename );
        String name = f.getName();
        if ( name.matches( "[1-9].*\\.json" ) ) {
            TlsTele newTele = TlsTele.loadJs( f );
            int jobNr;
            if (failMax) {
                jobNr = WRONG_JOB_MAX;
            } else {
                jobNr = WRONG_JOB_MIN;
            }
            TlsTele sendTele = new TlsTele( new Date(), Direction.SEND, forNode, forNode/*newTele.getLogAddress()*/ );
            for (TlsETel etel : newTele.getEtels()) {
                TlsETel etel2 = new TlsETel( null, etel.getFg(), etel.getTlsId() + 32, jobNr ); // manip tls identifier !!!
                etel2.getDeblocks().addAll( etel.getDeblocks() );
                sendTele.addETel( etel2 );
            }

//            byte[] b = sendTele.getBytes();
//            TlsTele newTele2 = new TlsTele( new Date(), TlsTele.Direction.SEND, forNode, b, 0, b.length );

//            res.add( adjustSTel( newTele2, forNode, forDE ) );
            res.add( adjustSTel( sendTele, forNode, forDE ) );
        } else {
            log.warn( "Testfile skipped: Not maching pattern '[1-9].*\\\\.json': '{}'", filename );
        }
    }

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

    public void sendGlobalTimeSyncCommand() throws InterruptedException, ExecutionException {
        Builder builder = TeleSToSend.newBuilder();
        long t = System.currentTimeMillis();
        byte[] cmd = new byte[1];
        cmd[0] = 4; // time sync

        // @formatter:off
        builder
            .setIid( idGenerator.newID() )
            .setFlags( 0 )
            .setIfaceKey( 0 )
            .setRealAddress( 0 )
            .setOsi7Tel( ByteString.copyFrom( cmd ) )
            .setTimeSent( fromMillis( t ) )
            ;
        // @formatter:on

        TeleSToSend telOut = builder.build();
        log.info( "Send global time sync" );
        send( telOut, sendTopic );
    }

    IDGenerator idGenerator = new IDGenerator();

    // the whole set of telegrams is generated
    public void sendTeles( List<TlsTele> teleList ) throws InterruptedException, ExecutionException, IOException {


        for ( TlsTele tel : teleList ) {
            Builder builder = TeleSToSend.newBuilder();
            long t = System.currentTimeMillis();
            ByteString telBytes = ByteString.copyFrom( tel.getBytes() );

            TlsETel etelInfo = tel.getEtels().get( 0 );
            int typ = 999;
            if ( etelInfo.getDeblockCount() >= 2 ) {
                typ = etelInfo.getDeblocks().get( 1 ).getDeTyp();
            }

            int ra = tel.getRealAddress();

            // @formatter:off
            builder
                .setIid( idGenerator.newID() )
                .setFlags( 1 )
                .setIfaceKey( 2000 )
                .setRealAddress( ra )
                .setOsi7Tel( telBytes )
                .setTimeSent( fromMillis( t ) )
                ;
            // @formatter:on

            TeleSToSend telOut = builder.build();
            log.info( "Send tel fg {} id {} typ {} to topic {}", etelInfo.getFg(), etelInfo.getTlsId(), typ, sendTopic );
            send( telOut, sendTopic );
        }

        Thread.sleep( 1000 );
    }

    // send the message to the kafka server
    public void send( final TeleSToSend message, String topic ) throws InterruptedException, ExecutionException {
        // LOGGER.info("Sending: {}", message)
        /* SendResult<String, Object> sendRes = */
        kafkaTemplate.send( MessageBuilder.withPayload( message ).setHeader( KafkaHeaders.TOPIC, topic ).build() ).get();
        // sendRes.getProducerRecord()
        // LOGGER.info("Sending result: {}", sendRes)
    }

}
