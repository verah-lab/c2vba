package de.heuboe.asfinag.tls.tele.in;

import static com.google.protobuf.util.Timestamps.fromMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.google.protobuf.ByteString;

import de.heuboe.asfinag.tls.replay.testlve.config.JacksonConfig;
import de.heuboe.asfinag.tls.replay.testlve.config.KafkaConfig;
import de.heuboe.asfinag.vmis2.tls.rcv.cfgmock.config.MockCfgSvcCfg;
import de.heuboe.idgenerator.generator.IDGenerator;
import de.heuboe.tls.receiver.core.telein.TlsKafkaTelgramReceiver;
import de.heuboe.tls.tel.io.TeleSReceived;
import de.heuboe.tls.tel.io.TeleSReceived.Builder;
import de.heuboe.tls.tlstele.TlsTele;

/*

message TeleSReceived {
    google.protobuf.Timestamp time_rcvd = 1; // the time the telegram was received
    int32 iface_key = 2;    // ifacekey of corresponding iface process who received this telegram
    int32 realAddress = 3;  // the real address (node number ~Knotennummer) of remaining data
    int32 flags = 4;        // legacy compatibility
    bytes tls_s_tel = 5;    // TLS-Sammeltelegramm
}

 */

@EnableAutoConfiguration
@EnableKafka
@SpringBootTest(classes = {JacksonConfig.class, KafkaConfig.class, TlsKafkaTelgramReceiver.class, SpringConfig.class, MockCfgSvcCfg.class})
//@ComponentScan( basePackageClasses= {KafkaConfig.class} )

@ContextConfiguration( classes = { 
      de.heuboe.asfinag.vmis2.tls.rcv.cfgmock.config.Vmis2SystemMessageManagementProperties.class,
      de.heuboe.tls.receiveconverter.InitAllInit.class,
      de.heuboe.tls.receiver.core.config.Config.class,
//      de.heuboe.asfinag.vmis2.tls.rcv.cfgmock.config.ConfChg.class,
//      de.heuboe.asfinag.vmis2.tls.rcv.cfgmock.MockedCfgGetterChg.class, // A2/A23
      de.heuboe.tls.receiver.core.config.MetricsConfig.class,
//      de.heuboe.tls.receiver.core.config.KafkaConfig.class,
      de.heuboe.tls.receiver.core.config.TransformerConfig.class,
      de.heuboe.asfinag.vmis2.tls.rcv.cfgmock.config.TransformationRulesConfig.class
      } )

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

//@EmbeddedKafka(partitions = 1, controlledShutdown = false,
//               brokerProperties = { "log.dir=target/kafka" })
//@TestPropertySource(properties = { "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
//                                    "spring.kafka.consumer.group-id=EmbeddedKafkaTest"})
//@SpringBootTest( properties = { "spring.kafka.bootstrap-servers=vmis2-kafka1.dmz1.heuboe.hbintern:9092",
//        "spring.kafka.consumer.group-id=rn_test_tel_in",
//        "spring.kafka.consumer.properties.value.deserializer=org.apache.kafka.common.serialization.ByteArrayDeserializer",
//        "spring.kafka.producer.properties.value.serializer=org.apache.kafka.common.serialization.ByteArraySerializer" } )
//@Disabled
public class TlsKafkaTelgramReceiverTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TlsKafkaTelgramReceiverTest.class);
    
    @Autowired
    TlsKafkaTelgramReceiver receiver = null;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
//    /**
//     * The full value constructor which will lead to a valid object (instance) of this class.
//     * 
//     * @param kafkaTemplate the template that is used for high-level Kafka operations
//     */
//    @Autowired
//    public TlsKafkaTelgramReceiverTest(final KafkaTemplate<String, Object> kafkaTemplate) {
//        this.kafkaTemplate = kafkaTemplate;
//    }
    
    @Autowired
    private Map<String, KafkaTemplate<?, ?>> kafkaTemplates;

    @BeforeAll
    static void init() {
        System.setProperty("java.io.tmpdir", "target");
    }

    @BeforeEach
    void waitForAssignment() {
        for (MessageListenerContainer messageListenerContainer : kafkaListenerEndpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer,
                                                 1);
        }
    }

    private TlsTele getTele_1_4_49() {
        TlsTeleBuilder builder = new TlsTeleBuilder( 1, 132, 8431421 ); // id = 4 + 128
        builder.addDeBlockHeader( 33, 49 ).addByte( 147 ) // qKfz
                .addByte( 68 ) // qLkwAe
                .addByte( 39 ) // vPkwAe
                .addByte( 175 ); // vLkwAe

        return builder.getAsTlsTele();
    }
    
//    private static Timestamp toTimestamp( GregorianCalendar cal ) { // NOSONAR generated code - may be unused
//        return fromMillis( cal.getTimeInMillis() );
//    }

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
    
    @Value("${de.heuboe.asfinag.tls.receiver.inputTopic}")
    String topicNameFromSpring;
    
//    @Test
//    @Disabled // the prerequisites are no longer given
//    public void sendTele() throws InterruptedException, ExecutionException {
//        Thread.sleep( 5000 );
//        Builder builder = TeleSReceived.newBuilder();
//        long t = System.currentTimeMillis();
//        ByteString telBytes = ByteString.copyFrom( getTele_1_4_49().getBytes() );
//        int sendSize = telBytes.size();
//        IDGenerator idGenerator = new IDGenerator();
//        
//        // @formatter:off
//        builder
//            .setTimeRcvd( fromMillis( t ) )
//            .setFlags( 1 )
//            .setIfaceKey( 1001 )
//            .setRealAddress( 0x010203 )
//            .setTlsSTel( telBytes )
//            .setIid( idGenerator.newID() )
//            ;
//        // @formatter:on
//
//        TeleSReceived tel = builder.build();
//        
//        LOGGER.info("---------------------------------------------------------------------------" );
//        LOGGER.info("Send tel-0" );
//        send( tel, topicNameFromSpring );
//        
//        tel = builder.setRealAddress( 0x010204 ).setIid( idGenerator.newID() ).build();
//        LOGGER.info( "Send tel-1" );
//        send( tel, topicNameFromSpring );
//        
//        tel = builder.setRealAddress( 0x010205 ).setIid( idGenerator.newID() ).build();
//        LOGGER.info("Send tel-2" );
//        send( tel, topicNameFromSpring );
////        for ( int i = 0; i < 50; ++i) {
////            Thread.sleep( 100 );
////        }
//        LOGGER.info("Sended" );
//        int sent = 3;
//        int rcvd = 0;
//        
//        // telegram should have bee received by now
////        for ( int i = 0; i < 50; ++i) {
////            Thread.sleep( 10000 );
////        }
//        
//        List<TlsTele> allTele = new LinkedList<>();
//        List<TlsTele> telesIn = receiver.receive();
//        rcvd += telesIn.size();
//        allTele.addAll( telesIn );
//        LOGGER.info("Received {}", rcvd );
//        int retries = 0;
//        while ( (telesIn.isEmpty() || (rcvd < sent)) && retries++ < 50 ) {
//            LOGGER.info("> receive" );
//            telesIn = receiver.receive();
//            LOGGER.info("< receive" );
//            LOGGER.info("Rpt {}", telesIn.size() );
//            rcvd += telesIn.size();
//            allTele.addAll( telesIn );
//            Thread.sleep( 10 ); // NOSONAR test code intended that way
//        }
//
//        LOGGER.info("done receive loop" );
//
//        assertEquals( 3, allTele.size() );
//        
//        TlsTele telCheck;
//        telCheck = allTele.get( 0 );
//        assertEquals( 0x010203, telCheck.getRealAddress() );
//        assertEquals( sendSize, telCheck.getSize() );
//        telCheck = allTele.get( 1 );
//        assertEquals( 0x010204, telCheck.getRealAddress() );
//        telCheck = allTele.get( 2 );
//        assertEquals( 0x010205, telCheck.getRealAddress() );
//        
//        receiver.stopReceive();
//        
//        telesIn = receiver.receive();
//        assertEquals( 0, telesIn.size() );
//        
//        assertEquals( null, receiver.receive() );
////        assertThrows( IllegalStateException.class, () -> { receiver.receive(); } );
//        Thread.sleep( 10 ); // NOSONAR test code intended that way
//    }

}
