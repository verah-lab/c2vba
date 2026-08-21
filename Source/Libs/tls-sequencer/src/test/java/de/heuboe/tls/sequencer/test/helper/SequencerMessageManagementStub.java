package de.heuboe.tls.sequencer.test.helper;

import de.heuboe.tls.sequencer.services.SequencerMessageManagement;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
public class SequencerMessageManagementStub implements SequencerMessageManagement {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public SequencerMessageManagementStub(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void sendMessage(String message) {
        ProducerRecord<String, Object> record = new ProducerRecord<>("AlarmMessage", String.valueOf(Math.random()),
                message.getBytes());
        kafkaTemplate.send(record);
        log.info("Sending message to message management: '{}'", message);
    }

    @Override
    public void sendMessage(String message, String objectId) {
        ProducerRecord<String, Object> record = new ProducerRecord<>("AlarmMessage", String.valueOf(Math.random()),
                message.getBytes());
        kafkaTemplate.send(record);
        log.info("Sending message to message management: '{}' for affected object '{}'", message, objectId);
    }
}
