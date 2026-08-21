package de.heuboe.tls.sequencer.test;

import de.heuboe.tls.grammar.sequencer.flops.FlopStorage;
import de.heuboe.tls.received.pojo.PSYSDeFehlerList;
import de.heuboe.tls.received.pojo.PVLTDeFehlerList;
import de.heuboe.tls.received.pojo.PWZGDeFehlerList;
import de.heuboe.tls.sequencer.actors.SpringExtension;
import de.heuboe.tls.sequencer.config.SequencerProperties;
import de.heuboe.tls.sequencer.parser.Parser;
import de.heuboe.tls.sequencer.services.SequencerService;
import de.heuboe.tls.sequencer.test.config.IntegrationTestConfig;
import de.heuboe.tls.sequencer.test.helper.TestBase;
import de.heuboe.tls.sequencer.utils.SequencerBeanContainer;
import de.heuboe.tls.sequencer.utils.SequencerUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@EnableAutoConfiguration
@ContextConfiguration(classes = {SequencerProperties.class})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=SEQUENCER-CONFIG-CHANGE-TEST",
        "spring.kafka.listener.missing-topics-fatal=false"},
        locations = "classpath:application.properties"
)
@Import({SpringExtension.class, SequencerBeanContainer.class, SequencerUtils.class, FlopStorage.class,
        IntegrationTestConfig.class, Parser.class})
@ComponentScan(basePackages = {"de.heuboe.tls.kafka.operator"})
@SpringBootTest(classes = {SequencerService.class}, properties = {
        "spring.kafka.client-id=tls-sequencer-test-config-change"
})
@SpringJUnitConfig
@EnableScheduling
@Slf4j
public class ConfigChangeTest extends TestBase {

    @PostConstruct
    public void init() {
        consumer = consumerFactory.createConsumer();
    }

    // -------------------------------- test LVEDeFehler --------------------------------
    @Test
    @DisplayName("Configuration change test")
    public void ConfigChangeTest() throws Exception {

        ConsumerRecords<String, byte[]> resultList = handleSYSDeFehler(
                "SYSDeFehler3.json",
                "SYSDeFehlerList.json",
                1000L,
                properties.getReceiveTopicPrefix() + "WZGDeFehler" + properties.getReceiveTopicSuffix(),
                properties.getReceiveTopicPrefix() + "VLTDeFehler" + properties.getReceiveTopicSuffix());

        HashMap<String, ConsumerRecord<String, byte[]>> result = extractMessages(resultList);

        ConsumerRecord<String, byte[]> originMessage = result.get(
                properties.getReceiveTopicPrefix() + "SYSDeFehler" + properties.getReceiveTopicSuffix());
        ConsumerRecord<String, byte[]> sequencerMessage1 = result.get(
                properties.getReceiveTopicPrefix() + "WZGDeFehler" + properties.getReceiveTopicSuffix());
        ConsumerRecord<String, byte[]> sequencerMessage2 = result.get(
                properties.getReceiveTopicPrefix() + "VLTDeFehler" + properties.getReceiveTopicSuffix());

        // assert on received object
        assertAll("Object content for test case SYSDeFehler 1",
                () -> assertNotEquals("SM_A04_0_610_2", PSYSDeFehlerList.fromBytes(originMessage.value()).getIid()),
                () -> assertEquals(0, PSYSDeFehlerList.fromBytes(originMessage.value())
                        .getElementsList().get(0).getJobnummer()),
                () -> assertEquals(6, PSYSDeFehlerList.fromBytes(originMessage.value())
                        .getElementsList().get(0).getHersteller()),
                () -> assertEquals(1, PSYSDeFehlerList.fromBytes(originMessage.value())
                        .getElementsList().get(0).getFehlercode()),
                () -> assertNull(sequencerMessage1),
                () -> assertNull(sequencerMessage2)
        );

        // change config (added SM_A04_0_610_2)
        switchConfig(0L);

        ConsumerRecords<String, byte[]> resultListAfterChange = handleSYSDeFehler(
                "SYSDeFehler3.json",
                "SYSDeFehlerList.json",
                0L,
                properties.getReceiveTopicPrefix() + "WZGDeFehler" + properties.getReceiveTopicSuffix(),
                properties.getReceiveTopicPrefix() + "VLTDeFehler" + properties.getReceiveTopicSuffix());

        HashMap<String, ConsumerRecord<String, byte[]>> resultAfterChange = extractMessages(resultListAfterChange);


        ConsumerRecord<String, byte[]> originMessageAfterChange = resultAfterChange.get(
                properties.getReceiveTopicPrefix() + "SYSDeFehler" + properties.getReceiveTopicSuffix());
        ConsumerRecord<String, byte[]> sequencerMessage1AfterChange = resultAfterChange.get(
                properties.getReceiveTopicPrefix() + "WZGDeFehler" + properties.getReceiveTopicSuffix());
        ConsumerRecord<String, byte[]> sequencerMessage2AfterChange = resultAfterChange.get(
                properties.getReceiveTopicPrefix() + "VLTDeFehler" + properties.getReceiveTopicSuffix());

        // assert on received object
        assertAll("Object content for test case SYSDeFehler 1",
                () -> assertNotEquals("SM_A04_0_610_2", PSYSDeFehlerList.fromBytes(originMessageAfterChange.value()).getIid()),
                () -> assertEquals(0, PSYSDeFehlerList.fromBytes(originMessageAfterChange.value())
                        .getElementsList().get(0).getJobnummer()),
                () -> assertEquals(6, PSYSDeFehlerList.fromBytes(originMessageAfterChange.value())
                        .getElementsList().get(0).getHersteller()),
                () -> assertEquals(1, PSYSDeFehlerList.fromBytes(originMessageAfterChange.value())
                        .getElementsList().get(0).getFehlercode()),
                () -> assertEquals(255, PWZGDeFehlerList.fromBytes(sequencerMessage1AfterChange.value())
                        .getElementsList().get(0).getFehlercode()),
                () -> assertEquals(6, PWZGDeFehlerList.fromBytes(sequencerMessage1AfterChange.value())
                        .getElementsList().get(0).getHersteller()),
                () -> assertEquals(255, PVLTDeFehlerList.fromBytes(sequencerMessage2AfterChange.value())
                        .getElementsList().get(0).getFehlercode()),
                () -> assertEquals(5, PVLTDeFehlerList.fromBytes(sequencerMessage2AfterChange.value())
                        .getElementsList().get(0).getHersteller()),
                () -> assertEquals(3, originMessage.headers().toArray().length),
                () -> assertEquals(4, sequencerMessage1AfterChange.headers().toArray().length),
                () -> assertEquals(4, sequencerMessage2AfterChange.headers().toArray().length),
                () -> assertEquals(properties.getHeaderSequencerContent(), new String(sequencerMessage1AfterChange.headers()
                        .headers(properties.getHeaderSequencerMarker()).iterator().next().value())),
                () -> assertEquals(properties.getHeaderSequencerContent(), new String(sequencerMessage2AfterChange.headers()
                        .headers(properties.getHeaderSequencerMarker()).iterator().next().value()))
        );
    }
}
