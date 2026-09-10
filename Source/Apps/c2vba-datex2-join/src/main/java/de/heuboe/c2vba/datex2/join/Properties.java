package de.heuboe.c2vba.datex2.join;

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
@ConfigurationProperties("de.heuboe.c2vba.datex2.join")
@Data
public class Properties {

    private String topicSued;
    private String topicNord;
    
    private String pubMode;
    private int pubInterval;
    private String pubFileDir;
    private int keepInterval = 6;
    private int outdatedInterval = 900;
    
    private String generalD2Schema = "";
    
    private String vmsTableId;
    private String vmsTableVersion;
    
    @Value("${spring.kafka.consumer.group-id:sdbby-service}")
    private String consumerGroupId;
}
