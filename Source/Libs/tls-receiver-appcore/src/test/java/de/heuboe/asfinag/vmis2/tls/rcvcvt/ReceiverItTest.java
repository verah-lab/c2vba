package de.heuboe.asfinag.vmis2.tls.rcvcvt;

import static com.google.protobuf.util.Timestamps.fromMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.kafka.test.utils.ContainerTestUtils.waitForAssignment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import com.google.protobuf.ByteString;

import de.heuboe.idgenerator.generator.IDGenerator;
import de.heuboe.tls.receiver.core.config.Config;
import de.heuboe.tls.receiver.core.config.KafkaConfig;
import de.heuboe.tls.receiver.core.receiver.Receiver;
import de.heuboe.tls.tel.io.TeleSReceived;
import de.heuboe.tls.tel.io.TeleSReceived.Builder;
import de.heuboe.tls.tlstele.TlsDeBlock;
import de.heuboe.tls.tlstele.TlsETel;
import de.heuboe.tls.tlstele.TlsTele;
import io.micrometer.core.instrument.MeterRegistry;

@Disabled
@ComponentScan( {"de.heuboe.asfinag.tls.receiver"})
@EnableAutoConfiguration
@EnableKafka
@DirtiesContext
@SpringBootTest( classes = { KafkaConfig.class, Config.class }, webEnvironment = WebEnvironment.DEFINED_PORT )
@EmbeddedKafka(partitions = 1, controlledShutdown = false, brokerProperties = {"log.dir=target/kafka"})
//@TestPropertySource(properties = {
//        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
//        "spring.kafka.consumer.group-id=EmbeddedKafkaTest",
//        "spring.kafka.listener.missing-topics-fatal=false"})
//@TestPropertySource("classpath:allTeles.properties") // from src/test/resources !!!
@TestPropertySource(
        properties = {
            "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
            "spring.kafka.consumer.group-id=EmbeddedKafkaTest",
            "spring.kafka.listener.missing-topics-fatal=false"},
        locations="classpath:allTeles.properties"               // !!! properties
        )
public class ReceiverItTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiverItTest.class);

    // test assuming a local kafka server is running
    @Autowired
    Receiver receiver;

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    MeterRegistry meterRegistry;

    @BeforeAll
    static void init() {
        System.setProperty("java.io.tmpdir", "target");
    }

    @BeforeEach
    void waitForAssignmentSetup() {
        for (final MessageListenerContainer messageListenerContainer : this.registry.getListenerContainers()) {
            waitForAssignment(messageListenerContainer, this.embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }
    
    @Test
    @Disabled
    public void testReceive() throws InterruptedException, ExecutionException, IOException {
        assertNotNull( receiver );
        System.out.println( System.lineSeparator() + "receiver is started - telegrams will be sent" + System.lineSeparator() );
        sendTele();        
        Thread.sleep( 3000 ); // NOSONAR this is a faked test
        assertEquals( 204, meterRegistry.find( "TelegramsReceived" ).counter().count(), 0.01 );
        assertEquals( 20, meterRegistry.find( "DataObjects.failed.soft" ).counter().count(), 0.01 );
        assertEquals( 56, meterRegistry.find( "DataObjects.failed.hard" ).counter().count(), 0.01 );
    }
    
    // ================================================================
    

    // send the message to th kafka server
    public void send(final TeleSReceived message, String topic) throws InterruptedException, ExecutionException {
//        LOGGER.info("Sending: {}", message);
        /*SendResult<String, Object> sendRes =*/ kafkaTemplate.send(
                MessageBuilder
                .withPayload(message)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .build())
                    .get();
//        sendRes.getProducerRecord();
//        LOGGER.info("Sending result: {}", sendRes);
    }
    
    // build a list of tls telegrams according to the files in th subfolder 'TlsTelegramme'
    private List<TlsTele> makeTeleList() throws IOException {
        List<TlsTele> res = new ArrayList<>();
        String dirName = "src/test/resources/TlsTelegrammeUnitTest";
        File dir = new File( dirName );
        for ( File tlsFile : dir.listFiles() ) {
            String filename = tlsFile.getName();
            if ( filename.matches( "[1-9].*\\.json" ) ) {
                TlsTele newTele = TlsTele.loadJs( tlsFile );
                res.add( adjustSTel( newTele ) );
            }
        }
        return res;
    }
    
    // location/distance of a SM in Kaernten and DEs existing there 
    static final int SM_A02_0_800_OSI7 = 8431421;
    static final int SM_A02_0_800_F1_DE = 33;
    static final int SM_A02_0_800_F2_DE = 34;
    
    // do some adjustments to the telegrams in roder to have tests for FG1 in UZ Kaernten
    private TlsTele adjustSTel( TlsTele tel ) {
        tel.setLogAddress( SM_A02_0_800_OSI7 );
        tel.getEtels().forEach( etel -> adjustETel( etel ) );
        return tel;
    }
    
    // duplicate the given de block and add a timestamp
    private void adjustETel( TlsETel etel ) {
        if (1 != etel.getFg()) {
            return;
        }
        int id = etel.getTlsId();
        if (1 == etel.getDeblockCount()) {
            TlsDeBlock deblock = etel.getDeblocks().get( 0 );
            
            TlsDeBlock deblockClone = new TlsDeBlock( deblock.getParent(), SM_A02_0_800_F1_DE, deblock.getDeTyp() );
            deblockClone.setContent( deblock.getContent() );
            
            TlsDeBlock deblockClone2 = new TlsDeBlock( deblock.getParent(), SM_A02_0_800_F2_DE, deblock.getDeTyp() );
            deblockClone2.setContent( deblock.getContent() );
            
            // ACHTUNG - Hier nur Typ 48 Zeitstempel
            TlsDeBlock zst48 = new TlsDeBlock( deblock.getParent(), 255, 48 );
            TlsDeBlock zst30 = new TlsDeBlock( deblock.getParent(), 255, 30 );
            
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
            
            TlsDeBlock zst =null; 
            zst = zst30;
            if (132 == id) {
                zst = zst48;
            }
            
            etel.getDeblocks().set( 0, zst );
            etel.getDeblocks().add( deblockClone );
            etel.getDeblocks().add( deblockClone2 );
        }
    }
    
    // the whole set of telegrams is generated
    public void sendTele() throws InterruptedException, ExecutionException, IOException {
        IDGenerator idGenerator = new IDGenerator();
        
        // get the list of telegrams
        List<TlsTele> teleList = makeTeleList();
        
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
            send( telOut, "TeleSReceived" );
            Thread.sleep( 100 );
        }
        
        Thread.sleep( 1000 );
     }

}
