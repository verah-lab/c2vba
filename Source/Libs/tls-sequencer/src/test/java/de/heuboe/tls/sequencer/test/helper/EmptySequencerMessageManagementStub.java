package de.heuboe.tls.sequencer.test.helper;

import de.heuboe.tls.sequencer.services.SequencerMessageManagement;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmptySequencerMessageManagementStub implements SequencerMessageManagement {

    @Override
    public void sendMessage(String message) {
        log.info("Sending message to message management: '{}'", message);
    }

    @Override
    public void sendMessage(String message, String objectId) {
        log.info("Sending message to message management: '{}' for affected object '{}'", message, objectId);
    }
}
