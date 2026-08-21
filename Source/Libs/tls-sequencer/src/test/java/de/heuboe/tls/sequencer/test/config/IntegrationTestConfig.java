package de.heuboe.tls.sequencer.test.config;

import de.heuboe.tls.cfgsv.bridge.interfaces.ConfigChangeProvider;
import de.heuboe.tls.cfgsv.bridge.interfaces.TlsCfgGetter;
import de.heuboe.tls.kafka.operator.model.KafkaOperatorConstructorData;
import de.heuboe.tls.kafka.operator.model.KafkaOperatorTopicConfiguration;
import de.heuboe.tls.sequencer.config.SequencerConfig;
import de.heuboe.tls.sequencer.config.SequencerProperties;
import de.heuboe.tls.sequencer.parser.Parser;
import de.heuboe.tls.sequencer.services.SequencerMessageManagement;
import de.heuboe.tls.sequencer.test.helper.MockedCfgGetter;
import de.heuboe.tls.sequencer.test.helper.SequencerMessageManagementStub;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
public class IntegrationTestConfig extends SequencerConfig {

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public TlsCfgGetter tlsCfgGetter() {
        try {
            return new MockedCfgGetter();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public SequencerMessageManagement sequencerMessageManagement() {
        return new SequencerMessageManagementStub(kafkaTemplate);
    }

    @Bean
    @Primary
    @Override
    public Parser initParser(Parser parser, SequencerProperties properties, TlsCfgGetter tlsCfgGetter,
                             ConfigChangeProvider configChangeProvider) {
        return super.initParser(parser, properties, tlsCfgGetter, configChangeProvider);
    }

    @Override
    public KafkaOperatorConstructorData kafkaOperatorConstructorData(SequencerProperties properties, Parser parser) {
        KafkaOperatorConstructorData constructorData = super.kafkaOperatorConstructorData(properties, parser);

        constructorData.addTopic(
                KafkaOperatorTopicConfiguration.newBuilder()
                        .topic("SteuerSequenz")
                        .topicPrefix(properties.getSendTopicPrefix())
                        .topicSuffix(properties.getSendTopicSuffix())
                        .build());
        constructorData.addTopic(
                KafkaOperatorTopicConfiguration.newBuilder()
                        .topic("AXLAbrufPufferInhalt")
                        .build());

        constructorData.setTestMode(true);

        return constructorData;
    }
}
