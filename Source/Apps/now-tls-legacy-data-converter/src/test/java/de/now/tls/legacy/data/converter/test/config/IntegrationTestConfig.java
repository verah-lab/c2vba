package de.now.tls.legacy.data.converter.test.config;

import de.heuboe.tls.cfgsv.bridge.interfaces.TlsCfgGetter;
import de.heuboe.tls.kafka.operator.model.KafkaOperatorConstructorData;
import de.now.tls.legacy.data.converter.config.LegacyDataConverterConfig;
import de.now.tls.legacy.data.converter.test.helper.MockedCfgGetter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;

/**
 * @author alexandero
 */
@Slf4j
@TestConfiguration
public class IntegrationTestConfig extends LegacyDataConverterConfig {

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
    public KafkaOperatorConstructorData kafkaOperatorConstructorData() {
        KafkaOperatorConstructorData constructorData = super.kafkaOperatorConstructorData();
        constructorData.setTestMode(true);
        return constructorData;
    }
}
