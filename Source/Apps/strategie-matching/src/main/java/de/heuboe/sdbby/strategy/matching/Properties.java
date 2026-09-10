package de.heuboe.sdbby.strategy.matching;

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
    
    private String strategyStateTopic;
    
    private String wzgBetriebsartTopic;
    private String wzgStellzustandTopic;
    private String wzgKanalSteuerungTopic;
    private String wzgDeFehlerTopic; 
    
    private String wlsHost;
    private int wlsPort;

    private int wsPort = 8123;
    
    private int precedingFontChars = 0;
    private String rulesFile;
    
    private int updateCycle = 1000;
    
    @Value("${spring.kafka.consumer.group-id:strategie-matching}")
    private String consumerGroupId;
}
