package de.now.tls.legacy.data.converter.test;

import de.now.tls.legacy.data.converter.actors.SpringExtension;
import de.now.tls.legacy.data.converter.model.LegacyDataConverterDevices;
import de.now.tls.legacy.data.converter.services.LegacyDataConverterService;
import de.now.tls.legacy.data.converter.test.config.IntegrationTestConfig;
import de.now.tls.legacy.data.converter.test.helper.TestBase;
import de.now.tls.legacy.data.converter.utils.LegacyDataConverterUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=legacy-data-converter-config-test-group",
        "spring.kafka.listener.missing-topics-fatal=false",
        "spring.kafka.client-id=tls-legacy-data-converter-config-test",
        "spring.main.allow-bean-definition-overriding=true"},
        locations = "classpath:application.properties"
)
@Import({SpringExtension.class, LegacyDataConverterUtils.class})
@ComponentScan(basePackages = {"de.heuboe.tls.kafka.operator"})
@SpringBootTest(classes = {LegacyDataConverterService.class, IntegrationTestConfig.class})
@Slf4j
public class ConfigChangeTest extends TestBase {

    @Autowired
    private LegacyDataConverterDevices legacyDevices;

    @Autowired
    private LegacyDataConverterUtils utils;

    @PostConstruct
    public void init() {
        consumer = consumerFactory.createConsumer();
    }

    // -------------------------------- test LVEDeFehler --------------------------------
    @Test
    @DisplayName("Configuration change test")
    public void ConfigChangeTest() throws Exception {

        String id = "wzg.AQ_1D_35.h.de";

        assertAll("Check legacy devices before config change",
                () -> assertEquals(213, legacyDevices.getLegacyDeviceIds().size()),
                () -> assertTrue(utils.checkDevicePresence(id))
        );

        // change config -> legacy device wzg.AQ_1D_35.h.de removed from sst.sst_d_1.h.de
        switchConfig(0L);

        Thread.sleep(1000L);

        assertAll("Check legacy devices before config change",
                () -> assertEquals(212, legacyDevices.getLegacyDeviceIds().size()),
                () -> assertFalse(utils.checkDevicePresence(id))
        );
    }
}
