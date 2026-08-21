package de.heuboe.tls.sequencer.test;

import de.heuboe.tls.grammar.sequencer.flops.FlopStorage;
import de.heuboe.tls.received.pojo.PSYSFehlerDUEList;
import de.heuboe.tls.received.pojo.PSYSKommunikationsstatusList;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext
@EnableAutoConfiguration
@ContextConfiguration(classes = {SequencerProperties.class})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=SEQUENCER-INTEGRATION-TEST",
        "spring.kafka.listener.missing-topics-fatal=false"},
        locations = "classpath:application.properties"
)
@Import({SpringExtension.class, SequencerBeanContainer.class, SequencerUtils.class, FlopStorage.class,
        IntegrationTestConfig.class, Parser.class})
@ComponentScan(basePackages = {"de.heuboe.tls.kafka.operator"})
@SpringBootTest(classes = {SequencerService.class}, properties = {
        "spring.kafka.client-id=tls-sequencer-test-integration"
})
@SpringJUnitConfig
@EnableScheduling
@Slf4j
public class IntegrationTest extends TestBase {

    @PostConstruct
    public void init() {
        consumer = consumerFactory.createConsumer();
    }

    @Test
    @DisplayName("in: SYSKommunikationsstatus / out: SYSFehlerDUE")
    public void testSYSKommunikationsstatus() throws InterruptedException, IOException, ExecutionException {

        ConsumerRecords<String, byte[]> resultList = handleSYSKommunikationsstatus(
                "SYSKommunikationsstatus1.json",
                "SYSKommunikationsstatusList.json",
                2000L,
                properties.getReceiveTopicPrefix() + "SYSFehlerDUE" + properties.getReceiveTopicSuffix());

        HashMap<String, ConsumerRecord<String, byte[]>> result = extractMessages(resultList);

        ConsumerRecord<String, byte[]> originMessage = result.get(
                properties.getReceiveTopicPrefix() + "SYSKommunikationsstatus" + properties.getReceiveTopicSuffix());
        ConsumerRecord<String, byte[]> sequencerMessage = result.get(
                properties.getReceiveTopicPrefix() + "SYSFehlerDUE" + properties.getReceiveTopicSuffix());

        assertNotNull(originMessage);
        assertNotNull(sequencerMessage);

        // assert on received object
        assertAll("Object content for test case SteuerSequenz 1",
                () -> assertNotEquals(PSYSKommunikationsstatusList.fromBytes(originMessage.value()).getIid(),
                        PSYSFehlerDUEList.fromBytes(sequencerMessage.value()).getIid()),
                () -> assertEquals(
                        PSYSKommunikationsstatusList.fromBytes(originMessage.value()).getElementsList().get(0).getId(),
                        PSYSFehlerDUEList.fromBytes(sequencerMessage.value()).getElementsList().get(0).getId()),
                () -> assertEquals(
                        PSYSKommunikationsstatusList.fromBytes(originMessage.value()).getElementsList().get(0)
                                .getJobnummer(),
                        PSYSFehlerDUEList.fromBytes(sequencerMessage.value()).getElementsList().get(0).getJobnummer()),
                () -> assertEquals(
                        PSYSKommunikationsstatusList.fromBytes(originMessage.value()).getElementsList().get(0)
                                .getStatus(),
                        PSYSFehlerDUEList.fromBytes(sequencerMessage.value()).getElementsList().get(0).getFehlercode()),
                () -> assertEquals(6, PSYSFehlerDUEList.fromBytes(sequencerMessage.value())
                        .getElementsList().get(0).getHersteller()),
                () -> assertEquals(3, originMessage.headers().toArray().length),
                () -> assertEquals(4, sequencerMessage.headers().toArray().length),
                () -> assertEquals(properties.getHeaderSequencerContent(), new String(sequencerMessage.headers()
                        .headers(properties.getHeaderSequencerMarker()).iterator().next().value())),
                () -> assertEquals(
                        PSYSKommunikationsstatusList.fromBytes(originMessage.value()).getElementsList().get(0).getTlsTime(),
                        PSYSFehlerDUEList.fromBytes(sequencerMessage.value()).getElementsList().get(0).getTlsTime()),
                () -> assertTrue(
                        PSYSKommunikationsstatusList.fromBytes(originMessage.value()).getElementsList().get(0).getProcessTime().isBefore(
                                PSYSFehlerDUEList.fromBytes(sequencerMessage.value()).getElementsList().get(0).getProcessTime()))
        );
    }
}

