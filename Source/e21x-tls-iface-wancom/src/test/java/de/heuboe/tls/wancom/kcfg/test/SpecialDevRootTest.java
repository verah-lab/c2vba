package de.heuboe.tls.wancom.kcfg.test;

import de.heuboe.tls.cfglib.Osi7Cfg;
import de.heuboe.tls.iface.lib.IfaceApp;
import de.heuboe.tls.ifacewancom.config.ConfigGetTLSOsi7Config;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest

//@DirtiesContext // Kafka
//// @formatter:off
//@EmbeddedKafka(
//        partitions = 1,
//        controlledShutdown = false,
//                ports = {55777},
//                zookeeperPort = 55771,
////                ports = {50637},
//        // ${random.int} => always begin with a fresh directory. i.e. no messages in broker
//        // keep directory for instance tin order to preload
//        brokerProperties = {"log.dir=target/kafka${random.int}"}
//        ) // @formatter:on

@TestPropertySource(
//        properties = {
//                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
//                "spring.kafka.consumer.group-id=iface1EmbeddedKafkaTest",
//                "spring.kafka.listener.missing-topics-fatal=false"},
        locations="classpath:specialDevRoot.properties"               // !!! properties
        )

// @formatter:off
@ContextConfiguration( classes = {
        de.heuboe.tls.ifacewancom.config.Config.class,
        de.heuboe.tls.ifacewancom.config.ConfigChanges.class,
        ConfigGetTLSOsi7Config.class,
        de.heuboe.tls.ifacewancom.config.ConfigIfaceApp.class,
        de.heuboe.tls.ifacewancom.config.JacksonConfig.class,
//        de.heuboe.tls.ifacewancom.config.KafkaConfig.class,
//        de.heuboe.tls.ifacewancom.config.MetricsConfig.class,

        de.heuboe.tls.wancom.kcfg.test.config.MockedCfgGetterBean.class,
//        de.heuboe.tls.wancom.kcfg.test.config.MockedCfgUZA2.class,
    } ) // @formatter:on


@Slf4j
@EnableConfigurationProperties
@SpringJUnitConfig
//@EnableKafka
@EnableAutoConfiguration(exclude=MongoAutoConfiguration.class)
public class SpecialDevRootTest {

//    @Mock( lenient = true ) // important for usage of one method with different arguments many times
//    @Autowired
//    private ConfigServiceGrpc.ConfigServiceBlockingStub cfgStub;

    @Autowired
    private IfaceApp app;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired @Qualifier( "sndTopic" )
    private String sendTopic;

    @Autowired
    Osi7Cfg cfg;

    @Test
    public void SpecialDevRootTest_Test() throws Exception {
        cfg.printDevTree( System.out, 99,false, true );

        List< Osi7Cfg.RoutingPart > route = cfg.getRouteToDev( "SM_A02_0_800" );

        log.info( "raw routing info: {}", route );

        assertEquals( 3, route.size() ); // Normally we have 2 routing hops: KRI -> SM, now the artificial dummy device is prepended
        // Note/keep in mind! KRIs aren't accessible by this modification

        log.info( "Success" );
    }

}
