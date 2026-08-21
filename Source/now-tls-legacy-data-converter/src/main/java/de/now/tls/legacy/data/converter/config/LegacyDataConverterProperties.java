package de.now.tls.legacy.data.converter.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author alexandero
 */
@Configuration
@ConfigurationProperties("de.now.tls.legacy.data.converter")
@Getter
@Setter
public class LegacyDataConverterProperties {

    /* Defines the number of retries a received message will be resend from KafkaOperatorService via Akka if
   something fails. Default 0. */
    private int responseRetries = 0;

    /* Defines the response time in milliseconds the service must notify the KafkaOperatorService about successful
       message handling before resend the message if configured in responseRetries property. Default 5000. */
    private long responseTimeout = 5000;

    @Value("${de.now.tls.legacy.data.converter.topic.prefix.receive}")
    private String topicPrefixReceive;

    @Value("${de.now.tls.legacy.data.converter.topic.prefix.send}")
    private String topicPrefixSend;

    private String headerMarkerKey;
    private String headerMarkerContent;

    private String uzId;
}
