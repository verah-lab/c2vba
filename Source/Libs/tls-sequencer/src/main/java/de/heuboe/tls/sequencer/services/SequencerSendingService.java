package de.heuboe.tls.sequencer.services;

import de.heuboe.idgenerator.generator.IDGenerator;
import de.heuboe.tls.grammar.sequencer.ObjectDirection;
import de.heuboe.tls.kafka.operator.services.KafkaOperatorService;
import de.heuboe.tls.parser.proto.GenericProtoObject;
import de.heuboe.tls.sequencer.config.SequencerProperties;
import de.heuboe.tls.sequencer.utils.SequencerUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * This service handles the message sending to the {@link KafkaOperatorService}.
 */
@Component
@Slf4j
public class SequencerSendingService {

    @Getter
    @Setter
    private KafkaOperatorService kafkaOperatorService;

    private final SequencerProperties properties;

    private final IDGenerator idGenerator = new IDGenerator();

    /**
     * Default constructor that should only inform about successful start of the application.
     *
     * @param properties The sequencer properties from the application.properties.
     */
    public SequencerSendingService(SequencerProperties properties) {
        this.properties = properties;
        log.info("SequencerSendingService created.");
    }

    /**
     * Prepare the message and necessary headers for sending the modified object via {@link KafkaOperatorService}s send
     * method.
     *
     * @param object The modified object that should be send.
     */
    public void sendMessage(GenericProtoObject object) {
        // collect all additional header in a Map
        Map<String, Object> headerMap = new HashMap<>();

        // set default prefix and suffix
        String prefix = properties.getSendTopicPrefix();
        String suffix = properties.getSendTopicSuffix();

        // update suffix
        if (object.getMetaData().containsKey(ObjectDirection.class.getSimpleName()) &&
                (object.getMetaData().get(ObjectDirection.class.getSimpleName()) == ObjectDirection.IN)) {
            prefix = properties.getReceiveTopicPrefix();
            suffix = properties.getReceiveTopicSuffix();
        }

        // build target topic name with prefix and suffix
        StringBuilder target = new StringBuilder(prefix);

        // set target topic
        if (object.getMetaData().containsKey(SequencerUtils.TOPIC_TARGET_KEY) &&
                !isEmpty(object.getMetaData().get(SequencerUtils.TOPIC_TARGET_KEY))) {
            target.append(object.getMetaData().get(SequencerUtils.TOPIC_TARGET_KEY));
        } else {
            target.append(object.getClassName());
        }
        // create list object
        GenericProtoObject listObject = GenericProtoObject.objectToList(object);

        target.append(suffix);

        String messageKey = object.getStringValue("id");

        if (isEmpty(messageKey)) {
            messageKey = object.getStringValue("eaId");
        }

        // add target topic to header
        headerMap.put(KafkaHeaders.TOPIC, target.toString().getBytes());
        // add message key to header
        headerMap.put(KafkaHeaders.KEY, messageKey);

        // update iid before sending manipulated object
        listObject.updateValue("iid", idGenerator.newID());

        // add sequencer marker to header
        headerMap.put(properties.getHeaderSequencerMarker(), properties.getHeaderSequencerContent().getBytes());

        log.info("{} '{}' with id '{}' to topic '{}' with message key '{}'",
                generateLoggingPrefix(object), listObject.getClassName(), listObject.get("iid"), target.toString(),
                messageKey);
        log.trace("Sending object\n\r{}\n\r", listObject.toString());

        if (properties.isNoSendMode()) {
            log.info("No send mode activated. No message will be send to Kafka.");
            log.info("Header: {}", headerMap);
            log.info("Object: {}", listObject.getObject());
        } else {
            // send object to kafka
            kafkaOperatorService.send(headerMap, listObject.getObject(), true);
        }
    }

    /**
     * Assembles a prefix for a logging message depending on the name of the script block that is delivered by the
     * object's metadata.
     *
     * @param object The {@link GenericProtoObject} that contains the metadata that should be used for prefix generation.
     * @return a string that represents a suitable logging prefix.
     */
    private static String generateLoggingPrefix(GenericProtoObject object) {
        return (object.getMetaData().containsKey(SequencerUtils.SCRIPT_BLOCK_NAME) ? "Executing '" +
                object.getMetaData().get(SequencerUtils.SCRIPT_BLOCK_NAME) + "' -> s" : "S") + "ending";
    }
}
