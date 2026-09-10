package de.heuboe.sdbby.service.impl;

import javax.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

/**
 * A class that maps external configuration properties.
 */
@Configuration
@Validated
@ConfigurationProperties("de.heuboe.sdbby.service")
@Data
public class Properties {

    private String cfgFilename;
    
    @NotEmpty
    private String fehlerTopic;
    
    @NotEmpty
    private String commStatesTopic;

    private int wsPort = 8123;
    
    @Value("${spring.kafka.consumer.group-id:sdbby-service}")
    private String consumerGroupId;
}
