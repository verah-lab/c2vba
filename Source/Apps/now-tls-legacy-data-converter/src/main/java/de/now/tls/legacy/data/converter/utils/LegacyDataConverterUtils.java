package de.now.tls.legacy.data.converter.utils;

import de.heuboe.tls.cfglib.Osi7Cfg;
import de.now.tls.legacy.data.converter.config.LegacyDataConverterProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * A utils class that holds several useful methods for this service.
 *
 * @author alexandero
 */
@Component
@Slf4j
public class LegacyDataConverterUtils {

    @Getter
    @Setter
    private Osi7Cfg osi7Cfg;

    @Autowired
    private LegacyDataConverterProperties properties;

    /**
     * Create a header for a Kafka message for a topic specified in the parameter.
     *
     * @param topic The full qualified name of the topic the header should be created for.
     * @param id    The id of the object that should be used as message key.
     * @return a map that contains the necessary header information.
     */
    public Map<String, Object> buildHeader(String topic, String id) {
        Map<String, Object> headerMap = new HashMap<>();

        // add target topic to header
        headerMap.put(KafkaHeaders.TOPIC, topic.getBytes());
        // add message key to header
        headerMap.put(KafkaHeaders.KEY, id);

        return headerMap;
    }

    /**
     * This will check if the device with the passed id is part of the config service. If the check was successful a
     * true will be returned else false.
     *
     * @param id The id of the device that should be checked.
     * @return true if the device is present in the config service else false.
     */
    public boolean checkDevicePresence(String id) {
        if (osi7Cfg.getOsi7IdOfEa(id) != null) {
            return true;
        }
        log.debug("Received message that should be send to the device with id '{}'. But this device is not present in " +
                "the current config service. Ignore handling this message.", id);
        return false;
    }
}
