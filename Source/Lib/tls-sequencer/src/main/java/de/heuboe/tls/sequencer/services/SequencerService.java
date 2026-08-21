package de.heuboe.tls.sequencer.services;

import akka.actor.AbstractActor;
import de.heuboe.tls.kafka.operator.messages.KafkaOperatorMessage;
import de.heuboe.tls.kafka.operator.services.KafkaOperatorService;
import de.heuboe.tls.parser.proto.GenericProtoObject;
import de.heuboe.tls.sequencer.config.SequencerProperties;
import de.heuboe.tls.sequencer.parser.Parser;
import de.heuboe.tls.sequencer.utils.SequencerUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

/**
 * This is the {@link SequencerService} that holds the main logic for handling messages.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class SequencerService extends AbstractActor {

    private final SequencerUtils sequencerUtils;
    private final SequencerProperties properties;
    private final Parser parser;
    private final SequencerSendingService sequencerSendingService;
    private final SequencerMessageManagement sequencerMessageManagement;

    /**
     * Default constructor that should only inform about successful start of the application.
     *
     * @param sequencerUtils             The utility class of the sequencer.
     * @param properties                 The sequencer properties from the application.properties.
     * @param parser                     The initialized {@link Parser}.
     * @param sequencerSendingService    The {@link SequencerSendingService} for sending messages to kafka.
     * @param sequencerMessageManagement The {@link SequencerMessageManagement} for sending log messages to a message
     *                                   management system.
     */
    public SequencerService(SequencerUtils sequencerUtils, SequencerProperties properties, Parser parser,
                            SequencerSendingService sequencerSendingService,
                            SequencerMessageManagement sequencerMessageManagement) {
        this.sequencerUtils = sequencerUtils;
        this.properties = properties;
        this.parser = parser;
        this.sequencerSendingService = sequencerSendingService;
        this.sequencerMessageManagement = sequencerMessageManagement;
        log.info("SequencerService created.");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(KafkaOperatorMessage.class, msg -> {
                    if (handleMessage(msg)) {
                        sender().tell("Message received and processed.", self());
                        log.debug("Sender informed about successful message processing.");
                    } else {
                        String errMsg = "Message processing failed. Sender will not be informed.";
                        sequencerMessageManagement.sendMessage(errMsg);
                        log.warn(errMsg);
                    }
                })
                .matchAny(msg -> {
                    String errMsg = "Unknown message received: " + msg;
                    sequencerMessageManagement.sendMessage(errMsg);
                    log.error(errMsg);
                })
                .build();
    }

    /**
     * Handles a received message.
     *
     * @param message A {@link KafkaOperatorMessage} that holds the content from the {@link KafkaOperatorService}.
     * @return true if nothing seems to go wrong else false.
     */
    private boolean handleMessage(KafkaOperatorMessage message) {
        log.debug("Message with object '{}' from topic '{}' received.", message.newObject().getClassName(),
                message.topic());
        log.trace("Received message from Akka\n\r{}\n\r", message);

        // load header for configured header type
        String header = sequencerUtils.extractHeader(message.messageHeader(), properties.getHeaderType());
        String sequencerHeader = sequencerUtils.extractHeader(message.messageHeader(),
                properties.getHeaderSequencerMarker());

        boolean sequencerMessageReceived = false;

        if (!StringUtils.isEmpty(sequencerHeader)) {
            if (properties.getHeaderSequencerContent().equals(sequencerHeader)) {
                sequencerMessageReceived = true;
                log.debug("Message with sequencer header received");
            } else {
                log.debug("Message with sequencer header received but the content does not fit to the current " +
                        "sequencer instance. Maybe another sequencer instance has sent this message. Skip message " +
                        "handling with message key '{}' from topic '{}!'",
                        message.messageHeader(), message.topic());
                log.trace("Received sequencer header content: {}", sequencerHeader);
                log.trace("Sequencer header content of this sequencer instance: {}", properties.getHeaderSequencerContent());
                return true;
            }
        }

        if (StringUtils.isEmpty(header)) {
            String errMsg = "The header value for the key '" + properties.getHeaderType() +
                    "' is empty. Skip message content parsing.";
            sequencerMessageManagement.sendMessage(errMsg);
            log.error(errMsg);
        } else {
            try {
                Set<GenericProtoObject> antlrResult = parser.parse(message.topic(), message.newObject(),
                        sequencerMessageReceived);

                if (!antlrResult.isEmpty()) {
                    antlrResult.forEach(sequencerSendingService::sendMessage);
                } else {
                    log.debug("No message sending necessary for object '{}'.",
                            message.newObject().getClassName());
                }
                return true;
            } catch (IOException e) {
                // something went wrong while parsing the current object
                String errMsg = "An IOException occurred while parsing the following object\n\r" +
                        message.newObject().getClassName();
                sequencerMessageManagement.sendMessage(errMsg);
                log.error(errMsg);
                log.error(e.getLocalizedMessage());
            } catch (NullPointerException e) {
                // something went wrong while parsing the current object
                String errMsg = "Parsing the following object returned null: '" +
                        message.newObject().getClassName() + "'. See log for further details.";
                sequencerMessageManagement.sendMessage(errMsg);
                log.error(errMsg);
                log.error(e.getLocalizedMessage());
            }
        }
        return false;
    }
}
