package de.heuboe.tls.tele.recorder.config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import de.heuboe.tls.kafka.operator.model.KafkaOperatorConstructorData;
import de.heuboe.tls.kafka.operator.model.KafkaOperatorTopicConfiguration;
import de.heuboe.tls.kafka.operator.services.KafkaOperatorService;
import de.heuboe.tls.tele.recorder.actors.SpringExtension;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * This is the main tele-recorder configuration class that handle all necessary initialization.
 */
@Configuration
@Slf4j
@ComponentScan(basePackages = {"de.heuboe.tls.kafka.operator", "de.heuboe.tls.tele.recorder"})
public class TeleRecorderConfig {

    @Getter
    private ActorRef actor;

    @Bean
    public ActorSystem actorSystem(ApplicationContext applicationContext, SpringExtension springExtension) {
        ActorSystem system = ActorSystem.create("tele-recorder");
        springExtension.initialize(applicationContext);
        return system;
    }

    @Bean
    public KafkaOperatorConstructorData kafkaOperatorConstructorData(TeleRecorderProperties properties) {
        KafkaOperatorConstructorData constructorData = new KafkaOperatorConstructorData("", "");

        if (!StringUtils.isEmpty(properties.getReceiveTopic())) {
            constructorData.addTopic(
                    KafkaOperatorTopicConfiguration.newBuilder().topic(properties.getReceiveTopic()).build());
        }

        if (!StringUtils.isEmpty(properties.getSendTopic())) {
            constructorData.addTopic(
                    KafkaOperatorTopicConfiguration.newBuilder().topic(properties.getSendTopic()).build());
        }

        if (constructorData.getTopics().isEmpty()) {
            log.error("One of the properties 'tls.tele.recorder.receiveTopic' or 'tls.tele.recorder.sendTopic' must " +
                    "be set!");
            System.exit(-1);
        }

        constructorData.setResponseRetries(properties.getResponseRetries());
        constructorData.setResponseTimeout(properties.getResponseTimeout());

        return constructorData;
    }

    @Bean
    public KafkaOperatorService kafkaOperatorService(SpringExtension springExtension,
            ActorSystem actorSystem,
            KafkaOperatorConstructorData constructorData) {
        this.actor = actorSystem.actorOf(springExtension.props("teleRecorderService"), "teleRecorder");
        return new KafkaOperatorService(actor, actorSystem, constructorData);
    }
}
