package de.heuboe.asfinag.control.base.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.util.Timeout;
import com.google.protobuf.InvalidProtocolBufferException;
import de.heuboe.asfinag.control.base.messages.InfrastructureMessage;
import de.heuboe.asfinag.control.base.services.KafkaConfig;
import de.heuboe.asfinag.control.base.services.Utils;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureObject;
import de.heuboe.asfinag.vmis2.infrastructure.types.DisplayPanel;
import de.heuboe.asfinag.vmis2.infrastructure.types.LineReference;
import de.heuboe.asfinag.vmis2.infrastructure.types.Road;
import de.heuboe.idgenerator.generator.IDGenerator;
import de.heuboe.test.KafkaUtils;
import de.heuboe.vmis2.kafka.converter.ProtoPojoKafkaMessageConverter;
import eu.vmis_ehe.vmis2.control.data.pojo.PInfrastructureObjectMsg;
import eu.vmis_ehe.vmis2.control.data.pojo.PValiditySection;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.kafka.test.utils.ContainerTestUtils.waitForAssignment;

@Import({KafkaConfig.class})
@EmbeddedKafka(partitions = 1, controlledShutdown = true, brokerProperties = {"log.dir=target/kafka${random.int}"}, topics = {"VRZ-Apps_Infra_Objects"})
@EnableAutoConfiguration
@TestPropertySource(properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}", "spring.kafka.consumer.group-id=EmbeddedKafkaTest2", "spring.kafka.listener.missing-topics-fatal=false", "spring.kafka.consumer.properties.value.deserializer=org.apache.kafka.common.serialization.ByteArrayDeserializer", "spring.kafka.consumer.max.partition.fetch.bytes=104857600", "spring.kafka.consumer.fetch.max.bytes=104857600", "spring.kafka.broker.message.max.bytes =104857600", "spring.kafka.config.message.max.bytes =104857600", "spring.kafka.producer.properties.max.request.size=104857600", "spring.kafka.consumer.enable-auto-commit=false", "spring.kafka.producer.properties.value.serializer=org.apache.kafka.common.serialization.ByteArraySerializer"})
@SpringJUnitConfig
@Component
@Slf4j
class AbstractInfraObjPublishActorTest {


    public static final String INFRA_OBJ_TOPIC = "VRZ-Apps_Infra_Objects";
    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;


    @Autowired
    protected KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    protected ConsumerFactory<String, byte[]> consumerFactory;


    private Consumer<String, byte[]> consumerInfraObjTopic = null;

    @Autowired
    private AdminClient kafkaAdminClient;


    @Autowired
    private ActorSystem actorSystem;


    @TestConfiguration
    static class Configuration {

        @Bean
        ActorSystem actorSystem() {
            return ActorSystem.create();
        }

        @Bean
        ProtoPojoKafkaMessageConverter protoPojoKafkaMessageConverter() {
            return new ProtoPojoKafkaMessageConverter();
        }
    }

    @BeforeEach
    void init() throws InterruptedException, IOException, ExecutionException {

        for (final MessageListenerContainer messageListenerContainer : this.registry.getListenerContainers()) {
            waitForAssignment(messageListenerContainer, this.embeddedKafkaBroker.getPartitionsPerTopic());
        }

        // add topic
        //Only works for topics with one partition
        Utils.createTopicIfNeeded(kafkaAdminClient, "VRZ-Apps_Infra_Objects", true, 1, (short) 1);

    }

    static class InfraPublisher extends AbstractInfraObjPublishActor {


        /**
         * constructor
         *
         * @param idGenerator   id generator
         * @param kafkaTemplate kafka template
         * @param system        actor system
         */
        public InfraPublisher(IDGenerator idGenerator, KafkaTemplate<String, Object> kafkaTemplate, ActorSystem system) {
            super(idGenerator, kafkaTemplate, system);
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder().match(Init.class, this::printInfrastructureObjectToKafka).build();
        }
    }


    @Test
    void initInfraPublish() throws TimeoutException, InvalidProtocolBufferException {

        new TestKit(actorSystem) {
            {
                ActorRef publisher = actorSystem.actorOf(Props.create(InfraPublisher.class, new IDGenerator(), kafkaTemplate, actorSystem));

                assertNotNull(publisher);

                DisplayPanel panel = new DisplayPanel("123", "aq123", "aq123", "aq123", new LineReference(PValiditySection.builder().build()), new HashMap<>(), new HashMap<>());

                Map<String, List<InfrastructureObject>> infraObjectsToPublsih = new HashMap<>();
                infraObjectsToPublsih.put("aqs", List.of(panel));
                Map<String, Object> attachDataOfInfraObjToPublish = new HashMap<>();
                attachDataOfInfraObjToPublish.put(InfrastructureMessage.InfrastructureObjectToPublish.class.getName(), new InfrastructureMessage.InfrastructureObjectToPublish(new HashMap<>(infraObjectsToPublsih)));


                Road road = new Road("123", "road123", "road123", "road123", null, null, attachDataOfInfraObjToPublish);

                Instant now = Instant.now();
                publisher.tell(new AbstractInfraObjPublishActor.Init(List.of(road), INFRA_OBJ_TOPIC, ""), ActorRef.noSender());
                Instant finalNow = now;

                consumerInfraObjTopic = consumerFactory.createConsumer("InfraObj");
                TopicPartition partitionInfraObj = new TopicPartition(INFRA_OBJ_TOPIC, 0);
                List<TopicPartition> topicPartitionsInfraObj = Collections.singletonList(partitionInfraObj);
                consumerInfraObjTopic.assign(topicPartitionsInfraObj);
                consumerInfraObjTopic.seekToEnd(topicPartitionsInfraObj);
                ConsumerRecord<String, byte[]> message = KafkaUtils.getMessageAfterTimestampWithTimeoutIncreaseOffset(consumerInfraObjTopic, INFRA_OBJ_TOPIC, "_aqs", finalNow, Timeout.ofSeconds(60));
                PInfrastructureObjectMsg receivedInfraObjectMsg = PInfrastructureObjectMsg.fromBytes(message.value());


                assertNotNull(receivedInfraObjectMsg);
                assertEquals("aq123", receivedInfraObjectMsg.getInfrastrutureObjectsList().get(0).getName());

            }
        };
    }

}
