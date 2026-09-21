package de.heuboe.asfinag.control.base.services;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.google.protobuf.util.JsonFormat;
import de.heuboe.asfinag.control.base.actors.AbstractParameterActor;
import de.heuboe.asfinag.control.base.actors.ParameterActor;
import de.heuboe.asfinag.control.base.actors.RawParameterActor;
import de.heuboe.asfinag.control.base.actors.SpringExtension;
import de.heuboe.asfinag.vmis2.constants.KafkaConstants;
import de.heuboe.vmis2.kafka.converter.ProtoPojoKafkaMessageConverter;
import eu.vmis_ehe.vmis2.control.data.ConcreteControlSigns;
import eu.vmis_ehe.vmis2.control.data.pojo.PConcreteControlSigns;
import eu.vmis_ehe.vmis2.paramservice.ParameterSetList;
import eu.vmis_ehe.vmis2.paramservice.pojo.PParameterSetList;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.kafka.test.utils.ContainerTestUtils.waitForAssignment;

@Import({KafkaConfig.class})
@SpringBootTest(
    classes = {
        SpringExtension.class,
        ParameterActor.class,
        ReadParametersTest.TestConfig.class
    },
    properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=EmbeddedKafkaTest"
    }
)
@EnableAutoConfiguration
@ActiveProfiles({"test", "ReadParameterTest"})
@EmbeddedKafka(
    partitions = 1,
    controlledShutdown = true,
    brokerProperties = {"log.dir=target/kafka${random.int}"},
    topics = {"VRZ-Parameter-XYZ", "VRZ-Parameter-RAW"}
)
@DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Slf4j
public class ReadParametersTest {
    private static final String DATA_BASE_PATH = "src/test/resources/testData/";
    public static final String INSTANZ_1 = "INSTANZ1";

    @Autowired
    private ActorSystem system;

    @Autowired
    private SpringExtension springExtension;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @BeforeEach
    void init() throws InterruptedException, IOException, ExecutionException {

        for (final MessageListenerContainer messageListenerContainer : this.registry.getListenerContainers()) {
            waitForAssignment(messageListenerContainer, this.embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @Test
    void parameterActorTest() throws Exception {
        // PARAM PLCHECK BELEGUNG
        ParameterSetList.Builder param1Builder = ParameterSetList.newBuilder();
        String jsonParam1 = new String(Files.readAllBytes(
            Paths.get(DATA_BASE_PATH + "ParaList-INSTANZ1-PLBelegung-UZ_A2-A23_1-1568969398049.json")));
        JsonFormat.parser().merge(jsonParam1, param1Builder);
        PParameterSetList paramBelegung = PParameterSetList.from(param1Builder.setIid("4711").build());
        assertNotNull(paramBelegung);

        kafkaTemplate.send(MessageBuilder.withPayload(paramBelegung)
                                         .setHeader(KafkaHeaders.TOPIC, "VRZ-Parameter-XYZ")
                                         .setHeader(KafkaHeaders.KEY, "A23_1")
                                         .setHeader(KafkaConstants.KAFKA_HEADER_DEFINITION_SET_ID, "PLBelegung")
                                         .setHeader(KafkaConstants.KAFKA_HEADER_ROAD_ID, "A23_1")
                                         .setHeader(KafkaConstants.KAFKA_HEADER_SYSTEM, "UZ_A2")
                                         .setHeader(KafkaConstants.KAFKA_HEADER_INSTANCE, INSTANZ_1)
                                         .build()).get();

        new TestKit(system) {
            {

                system.eventStream().subscribe(getRef(), AbstractParameterActor.Parameters.class);
                system.eventStream().subscribe(getRef(), AbstractParameterActor.InitialParametersRead.class);
                Set<Tuple2<String, String>> algos2Handle = Set.of(
                    Tuple.of("PLBelegung", INSTANZ_1)
                );

                ParameterActor.InstanceHandler<PParameterSetList> instanceHandler = new ParameterActor.DefaultInstanceHandler("UZ_A2", "^A23_1$", algos2Handle);
                ActorRef parameterActor = system.actorOf(
                    springExtension.props("parameterActor", instanceHandler, Collections.singletonList("VRZ-Parameter-XYZ"), "ParameterActor"),
                    "ParameterActor"
                );

                log.info("ParameterActorActor created!");
                expectMsgClass(Duration.ofMinutes(1), AbstractParameterActor.Parameters.class);
                log.info("Got Parameter message !");
                AbstractParameterActor.InitialParametersRead initialParametersRead = expectMsgClass(Duration.ofMinutes(1),
                                                                                                    AbstractParameterActor.InitialParametersRead.class);
                assertEquals("ParameterActor", initialParametersRead.name());
                log.info("Got InitialParametersRead message");

                kafkaTemplate.send(MessageBuilder.withPayload(paramBelegung)
                                                 .setHeader(KafkaHeaders.TOPIC, "VRZ-Parameter-XYZ")
                                                 .setHeader(KafkaHeaders.KEY, "A23_1")
                                                 .setHeader(KafkaConstants.KAFKA_HEADER_DEFINITION_SET_ID, "PLBelegung")
                                                 .setHeader(KafkaConstants.KAFKA_HEADER_ROAD_ID, "A23_1")
                                                 .setHeader(KafkaConstants.KAFKA_HEADER_SYSTEM, "UZ_A2")
                                                 .setHeader(KafkaConstants.KAFKA_HEADER_INSTANCE, INSTANZ_1)
                                                 .build()).get();

                expectMsgClass(Duration.ofMinutes(1), AbstractParameterActor.Parameters.class);
                parameterActor.tell(new AbstractParameterActor.PublishParameters("A23_1", "PLBelegung", INSTANZ_1), this.getRef());
                expectMsgClass(Duration.ofMinutes(1), AbstractParameterActor.Parameters.class);
                expectMsgClass(Duration.ofMinutes(1), AbstractParameterActor.InitialParametersRead.class);

                // Test empty parameter message
                parameterActor.tell(new AbstractParameterActor.PublishParameters("A5_1", "PLBelegung", INSTANZ_1), this.getRef());
                expectMsgClass(Duration.ofMinutes(1), AbstractParameterActor.MissingParameter.class);

                system.stop(parameterActor);
            }
        };
    }

    @Test
    void uzDefaultParameterActorTest() throws Exception {
        // PARAM Zeitsynchronisation
        ParameterSetList.Builder param1Builder = ParameterSetList.newBuilder();
        String jsonParam1 = new String(Files.readAllBytes(
            Paths.get(DATA_BASE_PATH + "ZeitSyncParameter.json")));
        JsonFormat.parser().merge(jsonParam1, param1Builder);
        PParameterSetList paramZeitsynch = PParameterSetList.from(param1Builder.setIid("4711").build());
        assertNotNull(paramZeitsynch);

        kafkaTemplate.send(MessageBuilder.withPayload(paramZeitsynch)
                                         .setHeader(KafkaHeaders.TOPIC, "VRZ-Parameter-XYZ")
                                         .setHeader(KafkaHeaders.KEY, "A23_1")
                                         .setHeader(KafkaConstants.KAFKA_HEADER_DEFINITION_SET_ID, "Zeitsynchronisation")
                                         .setHeader(KafkaConstants.KAFKA_HEADER_SYSTEM, "VRZ")
                                         .setHeader(KafkaConstants.KAFKA_HEADER_INSTANCE, INSTANZ_1)
                                         .build()).get();

        new TestKit(system) {
            {

                system.eventStream().subscribe(getRef(), AbstractParameterActor.Parameters.class);
                system.eventStream().subscribe(getRef(), AbstractParameterActor.InitialParametersRead.class);
                Set<Tuple2<String, String>> algos2Handle = Set.of(
                    Tuple.of("Zeitsynchronisation", INSTANZ_1)
                );

                ParameterActor.InstanceHandler<PParameterSetList> instanceHandler = new ParameterActor.UZDefaultInstanceHandler("VRZ", algos2Handle);
                ActorRef parameterActor = system.actorOf(
                    springExtension.props("parameterActor", instanceHandler, Collections.singletonList("VRZ-Parameter-XYZ"), "UZDefaultParameterActor"),
                    "UZDefaultParameterActor"
                );

                log.info("ParameterActorActor created!");
                expectMsgClass(Duration.ofMinutes(1), AbstractParameterActor.Parameters.class);
                log.info("Got Parameter message !");
                AbstractParameterActor.InitialParametersRead initialParametersRead = expectMsgClass(Duration.ofMinutes(1),
                                                                                                    AbstractParameterActor.InitialParametersRead.class);
                assertEquals("UZDefaultParameterActor", initialParametersRead.name());
                log.info("Got InitialParametersRead message");

                kafkaTemplate.send(MessageBuilder.withPayload(paramZeitsynch)
                                                 .setHeader(KafkaHeaders.TOPIC, "VRZ-Parameter-XYZ")
                                                 .setHeader(KafkaHeaders.KEY, "A23_1")
                                                 .setHeader(KafkaConstants.KAFKA_HEADER_DEFINITION_SET_ID, "Zeitsynchronisation")
                                                 .setHeader(KafkaConstants.KAFKA_HEADER_SYSTEM, "VRZ")
                                                 .setHeader(KafkaConstants.KAFKA_HEADER_INSTANCE, INSTANZ_1)
                                                 .build()).get();

                expectMsgClass(Duration.ofMinutes(1), AbstractParameterActor.Parameters.class);
                parameterActor.tell(new AbstractParameterActor.PublishParameters("", "Zeitsynchronisation", INSTANZ_1), this.getRef());
                expectMsgClass(Duration.ofMinutes(1), AbstractParameterActor.Parameters.class);
                expectMsgClass(Duration.ofMinutes(1), AbstractParameterActor.InitialParametersRead.class);

                system.stop(parameterActor);
            }
        };
    }

    @Test
    void rawParameterActorTest() throws Exception {
        // PARAM Zeitsynchronisation
        ConcreteControlSigns.Builder ccsBuilder = ConcreteControlSigns.newBuilder();
        String jsonParam1 = new String(Files.readAllBytes(
            Paths.get(DATA_BASE_PATH + "ConcreteControlSigns.json")));
        JsonFormat.parser().merge(jsonParam1, ccsBuilder);
        PConcreteControlSigns paramConcreteControlSigns = PConcreteControlSigns.from(ccsBuilder.setIid("4711").build());
        assertNotNull(paramConcreteControlSigns);

        kafkaTemplate.send(MessageBuilder.withPayload(paramConcreteControlSigns)
                                         .setHeader(KafkaHeaders.TOPIC, "VRZ-Parameter-RAW")
                                         .setHeader(KafkaHeaders.KEY, "jam")
                                         .build()).get();

        new TestKit(system) {
            {

                system.eventStream().subscribe(getRef(), AbstractParameterActor.Parameters.class);
                system.eventStream().subscribe(getRef(), AbstractParameterActor.InitialParametersRead.class);
                Set<Tuple2<String, String>> algos2Handle = Set.of(
                    Tuple.of("", "VRZ-Parameter-RAW")
                );
                RawParameterActor.SystemDefaultInstanceHandler instanceHandler = new RawParameterActor.SystemDefaultInstanceHandler(Collections.singletonList("VRZ-Parameter-RAW"), algos2Handle);
                ActorRef parameterActor = system.actorOf(
                    springExtension.props("parameterActor", instanceHandler, Collections.singletonList("VRZ-Parameter-RAW"), "RawParameterActor"),
                    "RawParameterActor"
                );

                log.info("ParameterActorActor created!");
                expectMsgClass(Duration.ofMinutes(1), AbstractParameterActor.Parameters.class);
                log.info("Got Parameter message !");
                AbstractParameterActor.InitialParametersRead initialParametersRead = expectMsgClass(Duration.ofMinutes(1),
                                                                                                    AbstractParameterActor.InitialParametersRead.class);
                assertEquals("RawParameterActor", initialParametersRead.name());
                log.info("Got InitialParametersRead message");

                kafkaTemplate.send(MessageBuilder.withPayload(paramConcreteControlSigns)
                                                 .setHeader(KafkaHeaders.TOPIC, "VRZ-Parameter-RAW")
                                                 .setHeader(KafkaHeaders.KEY, "jam")
                                                 .build()).get();

                expectMsgClass(Duration.ofMinutes(1), AbstractParameterActor.Parameters.class);
                parameterActor.tell(new AbstractParameterActor.PublishParameters("", ConcreteControlSigns.class.getName(), ""), this.getRef());
                expectMsgClass(Duration.ofMinutes(1), AbstractParameterActor.Parameters.class);
                expectMsgClass(Duration.ofMinutes(1), AbstractParameterActor.InitialParametersRead.class);

                system.stop(parameterActor);
            }
        };
    }

    @TestConfiguration
    @EnableKafka
    static class TestConfig {
        @Autowired
        private ApplicationContext applicationContext;

        @Autowired
        private SpringExtension springExtension;

        @Bean
        Clock clock() {
            return Clock.systemDefaultZone();
        }

        @Bean
        ActorSystem actorSystem() {
            ActorSystem actorSystem = ActorSystem.create();
            springExtension.initialize(applicationContext);
            return actorSystem;
        }

        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }

        /**
         * Returns a ProtoPojoKafkaMessageConverter.
         *
         * @return ProtoPojoKafkaMessageConverter
         */
        @Bean
        @Profile("ReadParameterTest")
        public ProtoPojoKafkaMessageConverter messageConverter() {
            return new ProtoPojoKafkaMessageConverter();
        }

    }

}
