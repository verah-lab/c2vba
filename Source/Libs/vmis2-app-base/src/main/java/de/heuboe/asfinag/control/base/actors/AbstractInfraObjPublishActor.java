package de.heuboe.asfinag.control.base.actors;

import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorSystem;
import de.heuboe.asfinag.control.base.messages.InfrastructureMessage;
import de.heuboe.asfinag.vmis2.infrastructure.base.InfrastructureObject;
import de.heuboe.idgenerator.generator.IDGenerator;
import eu.vmis_ehe.vmis2.control.data.pojo.PInfrastructureObjectMsg;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;


/**
 * actor class to publish infrastructure objects.
 */
@Component
@Slf4j
public abstract class AbstractInfraObjPublishActor extends AbstractActorWithTimers {

    public static final String ERROR = "ERROR";
    /**
     * marker for logging
     */
    public static Marker logMarker = MarkerFactory.getMarker("InfraObjPublishActor");
    /**
     * actor system
     */
    public ActorSystem system;


    /**
     * idGenerator
     */
    public IDGenerator idGenerator;
    /**
     * kafka template
     */
    public KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * infrastructur Manager holds infrastructure
     */
    List<InfrastructureObject> infrastructureObjects;
    /**
     * publishTopic
     */
    public String publishTopic;
    /**
     * appName
     */
    public String appName;


    /**
     * constructor
     *
     * @param idGenerator   id generator
     * @param kafkaTemplate kafka template
     * @param system        actor system
     */
    public AbstractInfraObjPublishActor(IDGenerator idGenerator, KafkaTemplate<String, Object> kafkaTemplate, ActorSystem system) {


        this.idGenerator = idGenerator;
        this.kafkaTemplate = kafkaTemplate;
        this.system = system;

    }

    /**
     * Init Class which stores the main information data for the Actor.
     * @param infrastructureObjects Infrastructure objects.
     * @param publishTopic Topic to publish into.
     * @param appName Application name.
     */
    public record Init(List<InfrastructureObject> infrastructureObjects, String publishTopic, String appName) {
    }

    /*
     * (non-Javadoc)
     */
    @Override
    public void preStart() throws Exception {
        system.eventStream().subscribe(this.getSelf(), Init.class);
        // only to test exception
    }


    /**
     * (re-)init
     *
     * @param i Init class with road to use
     */
    public void printInfrastructureObjectToKafka(Init i) {
        if (idGenerator == null || kafkaTemplate == null || system == null) {
            log.info("Initialization with null in idGenerator {}, kafkaTemplate {}, system {}.",
                    idGenerator == null, kafkaTemplate == null, system == null);
        }


        if (i.infrastructureObjects() != null) {
            infrastructureObjects = i.infrastructureObjects();
        } else {
            log.info("Infrastructure objects are null, can't publish to kafkaQ");
        }

        publishTopic = i.publishTopic();
        appName = i.appName();


        if (infrastructureObjects != null) {
            infrastructureObjects.forEach(object ->
                // publish infra obj to broker
                object.getAttachedData(InfrastructureMessage.InfrastructureObjectToPublish.class)
                        .ifPresent(this::publishInfrastructure)
            );
        }

    }

    /**
     * Publish the given infrastructure to kafka.
     * @param infrastructureObjectToPublish Infrastructure object to be published.
     */
    private void publishInfrastructure(
            InfrastructureMessage.InfrastructureObjectToPublish infrastructureObjectToPublish) {

        for (Map.Entry<String, List<InfrastructureObject>> stringListEntry : infrastructureObjectToPublish.infrastructureObjects()
                .entrySet()) {

            PInfrastructureObjectMsg publishMsg = PInfrastructureObjectMsg.builder()
                    .infrastrutureObjectsList(
                            stringListEntry.getValue().stream()
                                    .map(InfrastructureObject::getInfrastructureObject)
                                    .toList()
                    )
                    .iid(idGenerator.newID())
                    .build();

            try {
                CompletableFuture<SendResult<String, Object>> completable = kafkaTemplate
                        .send(MessageBuilder.withPayload(publishMsg)
                                .setHeader(KafkaHeaders.TOPIC, publishTopic)
                                .setHeader(KafkaHeaders.KEY, appName + "_" + stringListEntry.getKey()).build());
                completable.whenComplete((r, e) -> completableLogging(publishMsg.getIid(), e,
                        publishTopic, " PInfrastructureObjectMsg "));
            } catch (Exception exception) {
                kafkaTemplate
                        .send(MessageBuilder.withPayload(PInfrastructureObjectMsg.builder().iid(idGenerator.newID()).build())
                                .setHeader(KafkaHeaders.TOPIC, publishTopic)
                                .setHeader(KafkaHeaders.EXCEPTION_MESSAGE, exception + "____" + Arrays.toString(exception.getStackTrace()))
                                .setHeader(KafkaHeaders.KEY, appName + "_" + ERROR + "_" + stringListEntry.getKey()).build());
                log.error(exception + "____" + Arrays.toString(exception.getStackTrace()));
            }
        }
    }

    /**
     * method to log publish result
     *
     * @param iid          iid
     * @param e            exception
     * @param publishTopic topic to publish to
     * @param topicLabel   topic name
     */
    public static void completableLogging(String iid, Throwable e, String publishTopic, String topicLabel) {

        if (e != null) {
            log.error(logMarker, " Error writing P {} " + topicLabel + " to kafka on topic {}", e, publishTopic);
        } else {
            log.debug(logMarker, "Publishing new " + topicLabel + " for Msg: {} to topic {}", iid,
                    publishTopic);
        }
    }



}
