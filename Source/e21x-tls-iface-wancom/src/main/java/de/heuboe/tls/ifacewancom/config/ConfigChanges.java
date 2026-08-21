package de.heuboe.tls.ifacewancom.config;

import de.heuboe.asfinag.tls.cfgchgdetector.ConfigChangeDetector;
import de.heuboe.tls.cfgsv.bridge.interfaces.ConfigChangeProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration providing beans for kafka topic and groupid for changes and a {@link ConfigChangeProvider} using these beans
 */
@Configuration
@EnableAutoConfiguration(exclude=MongoAutoConfiguration.class)
@Slf4j
public class ConfigChanges {

    /**
     * provide a change topic (string) for the handling of changes with respect to kafka
     * @param changeTopicName the name of the topic where changes are handled
     * @return the passed string will become the bean
     */
    @Bean( name = "changeTopic" )
    public String getChgTopic( @Value( "${de.heuboe.tls.ifacewancom.config.chg.changetopic}" ) String changeTopicName ) {
        return changeTopicName;
    }

    /**
     * provide a groupid for the subscription of changes with respect to kafka
     * @param changeTopicGroupId Spring wired name of groupid
     * @return the passed string will become the bean
     */
    @Bean( name = "changeTopicGroupId" )
    public String getChgTopicGrpId( @Value( "${de.heuboe.tls.ifacewancom.config.chg.changetopicgroupid}" ) String changeTopicGroupId ) {
        return changeTopicGroupId;
    }

    @Bean( name = "configChangeProvider" )
    public ConfigChangeProvider getChgProvider() {
        return new ConfigChangeDetector();
    }

}
