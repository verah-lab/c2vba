package de.now.tls.legacy.data.converter.services;

import akka.actor.AbstractActor;
import com.google.protobuf.Timestamp;
import de.heuboe.idgenerator.generator.IDGenerator;
import de.heuboe.tls.kafka.operator.messages.KafkaOperatorMessage;
import de.heuboe.tls.kafka.operator.services.KafkaOperatorService;
import de.heuboe.tls.parser.proto.GenericProtoObject;
import de.now.tls.legacy.data.converter.config.LegacyDataConverterProperties;
import de.now.tls.legacy.data.converter.model.LegacyDataConverterDevices;
import de.now.tls.legacy.data.converter.utils.LegacyDataConverterUtils;
import eu.vmis_ehe.vmis2.tls.received.*;
import eu.vmis_ehe.vmis2.tls.send.SteuerSequenz;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is the main service class that receives Kafka messages from {@link KafkaOperatorService}, convert them in the
 * appropriate data type and send them to the corresponding topic.
 *
 * @author alexandero
 */
@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LegacyDataConverterService extends AbstractActor {

    @Autowired
    private KafkaOperatorService kafkaOperatorService;

    @Autowired
    private IDGenerator idGenerator;

    @Autowired
    private LegacyDataConverterProperties properties;

    @Autowired
    private LegacyDataConverterUtils utils;

    @Autowired
    private LegacyDataConverterDevices legacyDevices;

    private static final String SUFFIX_SOLL = "Soll";
    private static final String TLS_TIME = "tlsTime";
    private static final String PROCESS_TIME = "processTime";
    private static final String JOBNUMMER = "jobnummer";
    private static final String FOLGENUMMER = "folgenummer";
    private static final String FUNKTIONSBYTE = "funktionsbyte";
    private static final String STELLCODE = "stellcode";
    private static final String STELLZUSTAND = "stellzustand";
    private static final String ANZEIGEPRINZIP = "anzeigeprinzip";
    private static final String TEXTZEICHEN = "textzeichen";
    private static final String PRISMEN = "prismen";

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(KafkaOperatorMessage.class, msg -> {
                    if (handleMessage(msg)) {
                        sender().tell("Message received and processed.", self());
                        log.debug("Sender informed about successful message processing.");
                    } else {
                        log.warn("Message processing failed. Sender will not be informed.");
                    }
                })
                .matchAny(msg -> log.error("Unknown message received: {}", msg))
                .build();
    }

    /**
     * Handles the message that was received via the actor from the {@link KafkaOperatorService}. Here we decide what
     * should happen depending on the type of the object inside the received Kafka message.
     *
     * @param msg The {@link KafkaOperatorMessage} that was received from Kafka and contain the object we must handle.
     * @return true if everything was handled without any exception.
     */
    private boolean handleMessage(KafkaOperatorMessage msg) {
        log.debug("<<< Received message from {} with id '{}'", msg.topic(),
                msg.newObject().getStringValue("iid"));
        log.trace("{}", msg);

        // handle data from detector loops
        if (msg.topic().equals(properties.getTopicPrefixReceive() + WVZStellzustand48.class.getSimpleName())) {
            handleReceivedWVZStellzustand48(msg);
        } else if (msg.topic().equals(properties.getTopicPrefixReceive() + WZGStellzustand55.class.getSimpleName())) {
            handleReceivedWZGStellzustand55(msg);
        } else if (msg.topic().equals(properties.getTopicPrefixReceive() + WVZGrundeinstellung32.class.getSimpleName())) {
            handleReceivedWVZGrundeinstellung32(msg);
        } else if (msg.topic().equals(properties.getTopicPrefixReceive() + WZGGrundeinstellung33.class.getSimpleName())) {
            handleReceivedWZGGrundeinstellung33(msg);
        } else if (msg.topic().equals(properties.getTopicPrefixSend() + WZGStellzustand.class.getSimpleName() + SUFFIX_SOLL)) {
            handleSentWZGStellzustand(msg);
        } else if (msg.topic().equals(properties.getTopicPrefixSend() + WZGGrundeinstellung.class.getSimpleName() + SUFFIX_SOLL)) {
            handleSentWZGGrundeinstellung(msg);
        } else if (msg.topic().equals(properties.getTopicPrefixSend() + "WZG" + SteuerSequenz.class.getSimpleName())) {
            handleSentWZGSteuersequenz(msg);
        } else {
            log.error("Handling of messages from topic '{}' is not supported.", msg.topic());
        }

        return true;
    }

    /**
     * Messages of the type {@link WVZStellzustand48} are mapped to a {@link WZGStellzustand} object. The parameter
     * Anzeigeprinzip will always be mapped to 1 which means B in the TLS 2012 definition.
     *
     * @param msg The received {@link KafkaOperatorMessage}, with the {@link WVZStellzustand48} object that should be
     *            converted to {@link WZGStellzustand}.
     */
    private void handleReceivedWVZStellzustand48(KafkaOperatorMessage msg) {
        // get list of objects contained in this message
        List<GenericProtoObject> objects = GenericProtoObject.listToObjects(msg.newObject());
        List<GenericProtoObject> resultObjects = new ArrayList<>();

        for (GenericProtoObject object : objects) {
            // create the new WZGStellzustand object and copy the data from the received WVZStellzustand48 object
            WZGStellzustand pojo = WZGStellzustand.newBuilder()
                    .setId(object.getStringValue("id"))
                    .setTlsTime(object.getTimestampValue(TLS_TIME))
                    .setProcessTime(object.getTimestampValue(PROCESS_TIME))
                    .setJobnummer(object.getIntegerValue(JOBNUMMER))
                    .setFolgenummer(object.getIntegerValue(FOLGENUMMER))
                    .setStellcode(object.getIntegerValue(STELLZUSTAND))
                    .setFunktionsbyte(object.getIntegerValue(FUNKTIONSBYTE))
                    .setAnzeigeprinzip(1)
                    .build();

            resultObjects.add(new GenericProtoObject(WZGStellzustand.class.getName(), pojo, null));
        }

        if (!resultObjects.isEmpty()) {
            sendMessage(resultObjects, properties.getTopicPrefixReceive() + WZGStellzustand.class.getSimpleName(), msg);
        }
    }

    /**
     * Messages of the type {@link WZGStellzustand55} are simply passed through to the origin WZGStellzustand topic.
     * Only the process time and the iid will be updated.
     *
     * @param msg The received {@link KafkaOperatorMessage}, with the {@link WZGStellzustand55} object that should be
     *            passed through.
     */
    @SuppressWarnings("unchecked")
    private void handleReceivedWZGStellzustand55(KafkaOperatorMessage msg) {
        // get list of objects contained in this message
        List<GenericProtoObject> objects = GenericProtoObject.listToObjects(msg.newObject());
        List<GenericProtoObject> resultObjects = new ArrayList<>();

        for (GenericProtoObject object : objects) {
            // create the new WZGStellzustand object and copy the data from the received WVZStellzustand48 object
            WZGStellzustand pojo = WZGStellzustand.newBuilder()
                    .setId(object.getStringValue("id"))
                    .setTlsTime(object.getTimestampValue(TLS_TIME))
                    .setProcessTime(object.getTimestampValue(PROCESS_TIME))
                    .setJobnummer(object.getIntegerValue(JOBNUMMER))
                    .setFolgenummer(object.getIntegerValue(FOLGENUMMER))
                    .setStellcode(object.getIntegerValue(STELLCODE))
                    .setFunktionsbyte(object.getIntegerValue(FUNKTIONSBYTE))
                    .setAnzeigeprinzip(object.getIntegerValue(ANZEIGEPRINZIP))
                    .setTextzeichen(object.getStringValue(TEXTZEICHEN))
                    .addAllPrismen((List<WZGStellzustand.Prisma>) object.get(PRISMEN))
                    .build();

            resultObjects.add(new GenericProtoObject(WZGStellzustand.class.getName(), pojo, null));
        }

        if (!resultObjects.isEmpty()) {
            sendMessage(resultObjects, properties.getTopicPrefixReceive() + WZGStellzustand.class.getSimpleName(), msg);
        }
    }

    /**
     * Messages of the type {@link WVZGrundeinstellung32} are mapped to a {@link WZGGrundeinstellung} object. The
     * parameter Anzeigeprinzip will always be mapped to 1 which means B in the TLS 2012 definition.
     *
     * @param msg The received {@link KafkaOperatorMessage}, used for logging.
     */
    private void handleReceivedWVZGrundeinstellung32(KafkaOperatorMessage msg) {
        // get list of objects contained in this message
        List<GenericProtoObject> objects = GenericProtoObject.listToObjects(msg.newObject());
        List<GenericProtoObject> resultObjects = new ArrayList<>();

        for (GenericProtoObject object : objects) {
            // create the new WZGGrundeinstellung object and copy the data from the received WVZGrundeinstellung32 object
            WZGGrundeinstellung pojo = WZGGrundeinstellung.newBuilder()
                    .setId(object.getStringValue("id"))
                    .setTlsTime(object.getTimestampValue(TLS_TIME))
                    .setProcessTime(object.getTimestampValue(PROCESS_TIME))
                    .setJobnummer(object.getIntegerValue(JOBNUMMER))
                    .setFolgenummer(object.getIntegerValue(FOLGENUMMER))
                    .setStellcode(object.getIntegerValue(STELLZUSTAND))
                    .setFunktionsbyte(object.getIntegerValue(FUNKTIONSBYTE))
                    .setAnzeigeprinzip(1)
                    .build();

            resultObjects.add(new GenericProtoObject(WZGGrundeinstellung.class.getName(), pojo, null));
        }

        if (!resultObjects.isEmpty()) {
            sendMessage(resultObjects, properties.getTopicPrefixReceive() + WZGGrundeinstellung.class.getSimpleName(), msg);
        }

    }

    /**
     * Messages of the type {@link WZGGrundeinstellung33} are simply passed through to the origin WZGStellzustand topic.
     * Only the process time and the iid will be updated.
     *
     * @param msg The received {@link KafkaOperatorMessage}, used for logging.
     */
    @SuppressWarnings("unchecked")
    private void handleReceivedWZGGrundeinstellung33(KafkaOperatorMessage msg) {
        // get list of objects contained in this message
        List<GenericProtoObject> objects = GenericProtoObject.listToObjects(msg.newObject());
        List<GenericProtoObject> resultObjects = new ArrayList<>();

        for (GenericProtoObject object : objects) {

            WZGGrundeinstellung pojo = WZGGrundeinstellung.newBuilder()
                    .setId(object.getStringValue("id"))
                    .setTlsTime(object.getTimestampValue(TLS_TIME))
                    .setProcessTime(object.getTimestampValue(PROCESS_TIME))
                    .setJobnummer(object.getIntegerValue(JOBNUMMER))
                    .setFolgenummer(object.getIntegerValue(FOLGENUMMER))
                    .setStellcode(object.getIntegerValue(STELLCODE))
                    .setFunktionsbyte(object.getIntegerValue(FUNKTIONSBYTE))
                    .setAnzeigeprinzip(object.getIntegerValue(ANZEIGEPRINZIP))
                    .setTextzeichen(object.getStringValue(TEXTZEICHEN))
                    .addAllPrismen((List<WZGGrundeinstellung.Prisma>) object.get(PRISMEN))
                    .build();

            resultObjects.add(new GenericProtoObject(WZGGrundeinstellung.class.getName(), pojo, null));
        }

        if (!resultObjects.isEmpty()) {
            sendMessage(resultObjects, properties.getTopicPrefixReceive() + WZGGrundeinstellung.class.getSimpleName(), msg);
        }
    }

    /**
     * Messages of the type {@link WZGStellzustand} that were received from the topic WZGStellzustandSoll must be split
     * up to WZGStellzustand48Soll and WZGStellzustand55Soll topics. The target topic will be determined by analyzing
     * the device type.
     *
     * @param msg The received {@link KafkaOperatorMessage}, with the {@link WZGStellzustand} object that should be sent
     *            to a specific Soll topic.
     */
    @SuppressWarnings("unchecked")
    private void handleSentWZGStellzustand(KafkaOperatorMessage msg) {

        // get list of objects contained in this message
        List<GenericProtoObject> objects = GenericProtoObject.listToObjects(msg.newObject());
        List<GenericProtoObject> resultObjects = new ArrayList<>();
        String targetTopic = "";

        for (GenericProtoObject object : objects) {

            String id = object.getStringValue("id");

            if (utils.checkDevicePresence(id)) {

                GenericProtoObject newObject;

                if (legacyDevices.getLegacyDeviceIds().contains(id)) {
                    WVZStellzustand48 wvzStellzustand48 = WVZStellzustand48.newBuilder()
                            .setId(id)
                            .setTlsTime(object.getTimestampValue(TLS_TIME))
                            .setProcessTime(object.getTimestampValue(PROCESS_TIME))
                            .setJobnummer(object.getIntegerValue(JOBNUMMER))
                            .setFolgenummer(object.getIntegerValue(FOLGENUMMER))
                            .setStellzustand(object.getIntegerValue(STELLCODE))
                            .setFunktionsbyte(object.getIntegerValue(FUNKTIONSBYTE))
                            .build();

                    newObject = new GenericProtoObject(WVZStellzustand48.class.getName(), wvzStellzustand48, null);
                    targetTopic = WVZStellzustand48.class.getSimpleName();
                } else {
                    WZGStellzustand55 wzgStellzustand55 = WZGStellzustand55.newBuilder()
                            .setId(id)
                            .setTlsTime(object.getTimestampValue(TLS_TIME))
                            .setProcessTime(object.getTimestampValue(PROCESS_TIME))
                            .setJobnummer(object.getIntegerValue(JOBNUMMER))
                            .setFolgenummer(object.getIntegerValue(FOLGENUMMER))
                            .setAnzeigeprinzip(object.getIntegerValue(ANZEIGEPRINZIP))
                            .setFunktionsbyte(object.getIntegerValue(FUNKTIONSBYTE))
                            .setStellcode(object.getIntegerValue(STELLCODE))
                            .setTextzeichen(object.getStringValue(TEXTZEICHEN))
                            .addAllPrismen((List<WZGStellzustand55.Prisma>) object.get(PRISMEN))
                            .build();
                    newObject = new GenericProtoObject(WZGStellzustand55.class.getName(), wzgStellzustand55, null);
                    targetTopic = WZGStellzustand55.class.getSimpleName();
                }
                resultObjects.add(newObject);
            }
        }

        if (!resultObjects.isEmpty()) {
            sendMessage(resultObjects, properties.getTopicPrefixSend() + targetTopic + SUFFIX_SOLL, msg);
        }
    }

    /**
     * Messages of the type {@link WZGGrundeinstellung} that were received from the topic WZGGrundeinstellungSoll must
     * be split up to WVZGrundeinstellung32Soll and WZGGrundeinstellung33Soll topics. The target topic will be
     * determined by analyzing the device type.
     *
     * @param msg The received {@link KafkaOperatorMessage}, with the {@link WZGGrundeinstellung} object that should be
     *            sent to a specific Soll topic.
     */
    @SuppressWarnings("unchecked")
    private void handleSentWZGGrundeinstellung(KafkaOperatorMessage msg) {

        // get list of objects contained in this message
        List<GenericProtoObject> objects = GenericProtoObject.listToObjects(msg.newObject());
        List<GenericProtoObject> resultObjects = new ArrayList<>();
        String targetTopic = "";

        for (GenericProtoObject object : objects) {

            String id = object.getStringValue("id");

            if (utils.checkDevicePresence(id)) {

                GenericProtoObject newObject;

                if (legacyDevices.getLegacyDeviceIds().contains(id)) {
                    WVZGrundeinstellung32 wvzGrundeinstellung32 = WVZGrundeinstellung32.newBuilder()
                            .setId(id)
                            .setTlsTime(object.getTimestampValue(TLS_TIME))
                            .setProcessTime(object.getTimestampValue(PROCESS_TIME))
                            .setJobnummer(object.getIntegerValue(JOBNUMMER))
                            .setFolgenummer(object.getIntegerValue(FOLGENUMMER))
                            .setStellzustand(object.getIntegerValue(STELLCODE))
                            .setFunktionsbyte(object.getIntegerValue(FUNKTIONSBYTE))
                            .build();

                    newObject = new GenericProtoObject(WVZGrundeinstellung32.class.getName(), wvzGrundeinstellung32, null);
                    targetTopic = WVZGrundeinstellung32.class.getSimpleName();
                } else {
                    WZGGrundeinstellung33 wzgGrundeinstellung33 = WZGGrundeinstellung33.newBuilder()
                            .setId(id)
                            .setTlsTime(object.getTimestampValue(TLS_TIME))
                            .setProcessTime(object.getTimestampValue(PROCESS_TIME))
                            .setJobnummer(object.getIntegerValue(JOBNUMMER))
                            .setFolgenummer(object.getIntegerValue(FOLGENUMMER))
                            .setAnzeigeprinzip(object.getIntegerValue(ANZEIGEPRINZIP))
                            .setFunktionsbyte(object.getIntegerValue(FUNKTIONSBYTE))
                            .setStellcode(object.getIntegerValue(STELLCODE))
                            .setTextzeichen(object.getStringValue(TEXTZEICHEN))
                            .addAllPrismen((List<WZGGrundeinstellung33.Prisma>) object.get(PRISMEN))
                            .build();
                    newObject = new GenericProtoObject(WZGGrundeinstellung33.class.getName(), wzgGrundeinstellung33, null);
                    targetTopic = WZGGrundeinstellung33.class.getSimpleName();
                }
                resultObjects.add(newObject);
            }
        }

        if (!resultObjects.isEmpty()) {
            sendMessage(resultObjects, properties.getTopicPrefixSend() + targetTopic + SUFFIX_SOLL, msg);
        }
    }

    /**
     * Messages of the type {@link SteuerSequenz} that were received from the topic WZGSteuerSequenz must be modified if
     * a special action code is present. The modification depends on the device. Legacy devices will get another action
     * code than non legacy devices.<br/>
     * <br/>
     * The following action changes take place:
     * <list>
     * <li>3005055 -> 3005348 (if legacy device)</li>
     * <li>3005055 -> 3005355 (if no legacy device)</li>
     * </list>
     *
     * @param msg The received {@link KafkaOperatorMessage}, with the {@link SteuerSequenz} object that should be sent
     *            to a specific Soll topic.
     */
    private void handleSentWZGSteuersequenz(KafkaOperatorMessage msg) {
        convertAndSend(msg, 3005055, 3005355, 3005348);
        convertAndSend(msg, 3003033, 3003333, 3003332);
    }

    /**
     * This will check the action code of the input object against a specific action code. If the action code matches
     * it will be converted to a new action code depending on the legacy state of the corresponding device.
     *
     * @param msg          The received {@link KafkaOperatorMessage}, used for logging.
     * @param sourceAction The action code the object must contain to get modified.
     * @param normalAction The action code that should be set if the device is a non legacy device.
     * @param legacyAction The action code that should be set if the device is a legacy device.
     */
    private void convertAndSend(KafkaOperatorMessage msg, int sourceAction, int normalAction, int legacyAction) {

        // get list of objects contained in this message
        List<GenericProtoObject> objects = GenericProtoObject.listToObjects(msg.newObject());
        List<GenericProtoObject> resultObjects = new ArrayList<>();

        for (GenericProtoObject object : objects) {

            int action = object.getIntegerValue("action");
            String id = object.getStringValue("id");

            if (utils.checkDevicePresence(id) && (action == sourceAction)) {
                int newAction = normalAction;
                if (legacyDevices.getLegacyDeviceIds().contains(id)) {
                    newAction = legacyAction;
                }
                object.updateValue("action", newAction);
                log.info("Action code modified from '{}' to '{}'", action, newAction);
                resultObjects.add(object);
            }
        }

        if (!resultObjects.isEmpty()) {
            sendMessage(resultObjects, properties.getTopicPrefixSend() + "WZG" + SteuerSequenz.class.getSimpleName(), msg);
        }
    }

    /**
     * This method will send the input object to the determined topic. Before sending the necessary header will and an
     * iid will be created. Further the processTime of the object will be updated and the object will be embedded into
     * its corresponding list object. Finally, this list object will be sent to the topic.
     *
     * @param objectList  A list with {@link GenericProtoObject} (non list objects) that should be sent to the topic.
     * @param targetTopic The topic the object should be sent to.
     * @param msg         The received {@link KafkaOperatorMessage}, used for logging.
     */
    private void sendMessage(List<GenericProtoObject> objectList, String targetTopic, KafkaOperatorMessage msg) {
        Instant now = Instant.now();
        GenericProtoObject listObject = null;

        // create header information for new message with target topic
        Map<String, Object> headerMap = utils.buildHeader(targetTopic, msg.messageKey());

        for (GenericProtoObject object : objectList) {
            // update process time for each object
            object.updateValue(PROCESS_TIME,
                    Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build());

            // add each object to the overall container object
            if (listObject == null) {
                listObject = GenericProtoObject.objectToList(object);
            } else {
                listObject.addListValue("elements", object);
            }
        }

        if (listObject != null) {
            // update iid before sending manipulated container object
            listObject.updateValue("iid", idGenerator.newID());

            kafkaOperatorService.send(headerMap, listObject.getObject(), true);

            log.info("Message Key: {} - {} -> {}", msg.messageKey(), msg.topic(), targetTopic);

            log.debug("{}", listObject);
        }
    }
}
