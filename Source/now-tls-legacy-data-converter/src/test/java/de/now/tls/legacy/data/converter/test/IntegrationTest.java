package de.now.tls.legacy.data.converter.test;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import de.heuboe.idgenerator.generator.IDGenerator;
import de.now.tls.legacy.data.converter.actors.SpringExtension;
import de.now.tls.legacy.data.converter.config.LegacyDataConverterProperties;
import de.now.tls.legacy.data.converter.services.LegacyDataConverterService;
import de.now.tls.legacy.data.converter.test.config.IntegrationTestConfig;
import de.now.tls.legacy.data.converter.test.helper.TestBase;
import eu.vmis_ehe.vmis2.tls.received.*;
import eu.vmis_ehe.vmis2.tls.received.pojo.*;
import eu.vmis_ehe.vmis2.tls.send.SteuerSequenz;
import eu.vmis_ehe.vmis2.tls.send.SteuerSequenzList;
import eu.vmis_ehe.vmis2.tls.send.pojo.PSteuerSequenz;
import eu.vmis_ehe.vmis2.tls.send.pojo.PSteuerSequenzList;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=legacy-data-converter-integration-test-group",
        "spring.kafka.listener.missing-topics-fatal=false",
        "spring.kafka.client-id=tls-legacy-data-converter-integration-test",
        "spring.main.allow-bean-definition-overriding=true"},
        locations = "classpath:application.properties"
)
@Import({SpringExtension.class})
@ComponentScan(basePackages = {"de.heuboe.tls.kafka.operator"})
@SpringBootTest(classes = {LegacyDataConverterService.class, IntegrationTestConfig.class})
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IntegrationTest extends TestBase {

    @Autowired
    private IDGenerator idGenerator;

    @Autowired
    private LegacyDataConverterProperties properties;

    @Test
    @Order(1)
    @DisplayName("Convert received WVZStellzustand48 to WZGStellzustand")
    public void testReceivedWVZStellzustand() throws ExecutionException, InterruptedException, InvalidProtocolBufferException {
        Instant now = Instant.now();
        Timestamp time = Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build();

        // build WVZStellzustand object
        WVZStellzustand48 wvzStellzustand48 = WVZStellzustand48.newBuilder()
                .setId("WVZStellzustand1")
                .setTlsTime(time)
                .setProcessTime(time)
                .setStellzustand(0)
                .setFolgenummer(0)
                .setFunktionsbyte(1)
                .build();

        WVZStellzustand48List wvzStellzustand48List = WVZStellzustand48List.newBuilder()
                .setIid(idGenerator.newID())
                .addElements(wvzStellzustand48)
                .build();

        ConsumerRecords<String, byte[]> resultList = sendToKafka(
                wvzStellzustand48.getId(),
                wvzStellzustand48List.getIid(),
                wvzStellzustand48List.toByteArray(),
                WVZStellzustandList.class.getName(),
                1500L,
                properties.getTopicPrefixReceive() + "WVZStellzustand48",
                properties.getTopicPrefixReceive() + "WZGStellzustand");

        // process result
        List<ConsumerRecord<String, byte[]>> result = extractMessages(resultList, properties.getTopicPrefixReceive() + "WZGStellzustand");

        PWZGStellzustandList resultObjectList = PWZGStellzustandList.fromBytes(result.get(0).value());
        PWZGStellzustand resultObject = resultObjectList.getElementsList().get(0);

        // assert on received object
        assertAll("Compare conversion of WVZStellzustand48 to WZGStellzustand",
                () -> assertEquals(wvzStellzustand48.getId(), resultObject.getId()),
                () -> assertEquals(1, resultObject.getAnzeigeprinzip()),
                () -> assertEquals(wvzStellzustand48.getFunktionsbyte(), resultObject.getFunktionsbyte()),
                () -> assertEquals(wvzStellzustand48.getJobnummer(), resultObject.getJobnummer()),
                () -> assertEquals(wvzStellzustand48.getFolgenummer(), resultObject.getFolgenummer()),
                () -> assertEquals("", resultObject.getTextzeichen()),
                () -> assertEquals(Instant.ofEpochSecond(wvzStellzustand48.getTlsTime().getSeconds(), wvzStellzustand48.getTlsTime().getNanos()),
                        resultObject.getTlsTime()),
                () -> assertTrue(Instant.ofEpochSecond(wvzStellzustand48.getProcessTime().getSeconds(), wvzStellzustand48.getProcessTime().getNanos()).isBefore(
                        resultObject.getProcessTime()))
        );
    }

    @Test
    @Order(2)
    @DisplayName("Convert received WZGStellzustand55 to WZGStellzustand")
    public void testReceivedWZGStellzustand() throws ExecutionException, InterruptedException, InvalidProtocolBufferException {
        Instant now = Instant.now();
        Timestamp time = Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build();

        // build WZGStellzustand object
        WZGStellzustand55 wzgStellzustand55 = WZGStellzustand55.newBuilder()
                .setId("WZGStellzustand1")
                .setTlsTime(time)
                .setProcessTime(time)
                .setAnzeigeprinzip(2)
                .setFolgenummer(0)
                .setFunktionsbyte(1)
                .setTextzeichen("toller Text")
                .addPrismen(WZGStellzustand55.Prisma.newBuilder().setFunktionsbyte(1).setWvzCode(2).build())
                .addPrismen(WZGStellzustand55.Prisma.newBuilder().setFunktionsbyte(8).setWvzCode(9).build())
                .build();

        WZGStellzustand55List wzgStellzustand55List = WZGStellzustand55List.newBuilder()
                .setIid(idGenerator.newID())
                .addElements(wzgStellzustand55)
                .build();

        ConsumerRecords<String, byte[]> resultList = sendToKafka(
                wzgStellzustand55.getId(),
                wzgStellzustand55List.getIid(),
                wzgStellzustand55List.toByteArray(),
                WZGStellzustandList.class.getName(),
                1500L,
                properties.getTopicPrefixReceive() + "WZGStellzustand55",
                properties.getTopicPrefixReceive() + "WZGStellzustand");

        // process result
        List<ConsumerRecord<String, byte[]>> result = extractMessages(resultList, properties.getTopicPrefixReceive() + "WZGStellzustand");

        PWZGStellzustandList resultObjectList = PWZGStellzustandList.fromBytes(result.get(0).value());
        PWZGStellzustand resultObject = resultObjectList.getElementsList().get(0);

        // assert on received object
        assertAll("Compare conversion of WZGStellzustand55 to WZGStellzustand",
                () -> assertEquals(wzgStellzustand55.getId(), resultObject.getId()),
                () -> assertEquals(wzgStellzustand55.getAnzeigeprinzip(), resultObject.getAnzeigeprinzip()),
                () -> assertEquals(wzgStellzustand55.getFunktionsbyte(), resultObject.getFunktionsbyte()),
                () -> assertEquals(wzgStellzustand55.getJobnummer(), resultObject.getJobnummer()),
                () -> assertEquals(wzgStellzustand55.getFolgenummer(), resultObject.getFolgenummer()),
                () -> assertEquals(wzgStellzustand55.getTextzeichen(), resultObject.getTextzeichen()),
                () -> assertEquals(wzgStellzustand55.getPrismenList().size(), resultObject.getPrismenList().size()),
                () -> assertEquals(Instant.ofEpochSecond(wzgStellzustand55.getTlsTime().getSeconds(), wzgStellzustand55.getTlsTime().getNanos()),
                        resultObject.getTlsTime()),
                () -> assertTrue(Instant.ofEpochSecond(wzgStellzustand55.getProcessTime().getSeconds(), wzgStellzustand55.getProcessTime().getNanos()).isBefore(
                        resultObject.getProcessTime()))
        );
    }

    @Test
    @Order(3)
    @DisplayName("Convert received WVZGrundeinstellung32 to WZGGrundeinstellung")
    public void testReceivedWVZGrundeinstellung() throws ExecutionException, InterruptedException, InvalidProtocolBufferException {
        Instant now = Instant.now();
        Timestamp time = Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build();

        // build WVZGrundeinstellung object
        WVZGrundeinstellung32 wvzGrundeinstellung32 = WVZGrundeinstellung32.newBuilder()
                .setId("WVZGrundeinstellung1")
                .setTlsTime(time)
                .setProcessTime(time)
                .setStellzustand(0)
                .setFolgenummer(0)
                .setFunktionsbyte(1)
                .build();

        WVZGrundeinstellung32List wvzGrundeinstellung32List = WVZGrundeinstellung32List.newBuilder()
                .setIid(idGenerator.newID())
                .addElements(wvzGrundeinstellung32)
                .build();

        ConsumerRecords<String, byte[]> resultList = sendToKafka(
                wvzGrundeinstellung32.getId(),
                wvzGrundeinstellung32List.getIid(),
                wvzGrundeinstellung32List.toByteArray(),
                WVZStellzustandList.class.getName(),
                1500L,
                properties.getTopicPrefixReceive() + "WVZGrundeinstellung32",
                properties.getTopicPrefixReceive() + "WZGGrundeinstellung");

        // process result
        List<ConsumerRecord<String, byte[]>> result = extractMessages(resultList, properties.getTopicPrefixReceive() + "WZGGrundeinstellung");

        PWZGGrundeinstellungList resultObjectList = PWZGGrundeinstellungList.fromBytes(result.get(0).value());
        PWZGGrundeinstellung resultObject = resultObjectList.getElementsList().get(0);

        // assert on received object
        assertAll("Compare conversion of WVZGrundeinstellung32 to WZGGrundeinstellung",
                () -> assertEquals(wvzGrundeinstellung32.getId(), resultObject.getId()),
                () -> assertEquals(1, resultObject.getAnzeigeprinzip()),
                () -> assertEquals(wvzGrundeinstellung32.getFunktionsbyte(), resultObject.getFunktionsbyte()),
                () -> assertEquals(wvzGrundeinstellung32.getJobnummer(), resultObject.getJobnummer()),
                () -> assertEquals(wvzGrundeinstellung32.getFolgenummer(), resultObject.getFolgenummer()),
                () -> assertEquals("", resultObject.getTextzeichen()),
                () -> assertEquals(Instant.ofEpochSecond(wvzGrundeinstellung32.getTlsTime().getSeconds(), wvzGrundeinstellung32.getTlsTime().getNanos()),
                        resultObject.getTlsTime()),
                () -> assertTrue(Instant.ofEpochSecond(wvzGrundeinstellung32.getProcessTime().getSeconds(), wvzGrundeinstellung32.getProcessTime().getNanos()).isBefore(
                        resultObject.getProcessTime()))
        );
    }

    @Test
    @Order(4)
    @DisplayName("Convert received WZGGrundeinstellung33 to WZGGrundeinstellung")
    public void testReceivedWZGGrundeinstellung() throws ExecutionException, InterruptedException, InvalidProtocolBufferException {
        Instant now = Instant.now();
        Timestamp time = Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build();

        // build WZGGrundeinstellung object
        WZGGrundeinstellung33 wzgGrundeinstellung33 = WZGGrundeinstellung33.newBuilder()
                .setId("WZGGrundeinstellung1")
                .setTlsTime(time)
                .setProcessTime(time)
                .setAnzeigeprinzip(2)
                .setFolgenummer(0)
                .setFunktionsbyte(1)
                .setTextzeichen("toller Text")
                .build();

        WZGGrundeinstellung33List wzgGrundeinstellung33List = WZGGrundeinstellung33List.newBuilder()
                .setIid(idGenerator.newID())
                .addElements(wzgGrundeinstellung33)
                .build();

        ConsumerRecords<String, byte[]> resultList = sendToKafka(
                wzgGrundeinstellung33.getId(),
                wzgGrundeinstellung33List.getIid(),
                wzgGrundeinstellung33List.toByteArray(),
                WZGGrundeinstellungList.class.getName(),
                1500L,
                properties.getTopicPrefixReceive() + "WZGGrundeinstellung33",
                properties.getTopicPrefixReceive() + "WZGGrundeinstellung");

        // process result
        List<ConsumerRecord<String, byte[]>> result = extractMessages(resultList, properties.getTopicPrefixReceive() + "WZGGrundeinstellung");

        PWZGGrundeinstellungList resultObjectList = PWZGGrundeinstellungList.fromBytes(result.get(0).value());
        PWZGGrundeinstellung resultObject = resultObjectList.getElementsList().get(0);

        // assert on received object
        assertAll("Compare conversion of WZGGrundeinstellung33 to WZGGrundeinstellung",
                () -> assertEquals(wzgGrundeinstellung33.getId(), resultObject.getId()),
                () -> assertEquals(wzgGrundeinstellung33.getAnzeigeprinzip(), resultObject.getAnzeigeprinzip()),
                () -> assertEquals(wzgGrundeinstellung33.getFunktionsbyte(), resultObject.getFunktionsbyte()),
                () -> assertEquals(wzgGrundeinstellung33.getJobnummer(), resultObject.getJobnummer()),
                () -> assertEquals(wzgGrundeinstellung33.getFolgenummer(), resultObject.getFolgenummer()),
                () -> assertEquals(wzgGrundeinstellung33.getTextzeichen(), resultObject.getTextzeichen()),
                () -> assertEquals(Instant.ofEpochSecond(wzgGrundeinstellung33.getTlsTime().getSeconds(), wzgGrundeinstellung33.getTlsTime().getNanos()),
                        resultObject.getTlsTime()),
                () -> assertTrue(Instant.ofEpochSecond(wzgGrundeinstellung33.getProcessTime().getSeconds(), wzgGrundeinstellung33.getProcessTime().getNanos()).isBefore(
                        resultObject.getProcessTime()))
        );
    }

    @Test
    @Order(5)
    @DisplayName("Convert sent WZGStellzustandSoll to WVZStellzustand48Soll")
    public void testSentWZGStellzustand48() throws ExecutionException, InterruptedException, InvalidProtocolBufferException {
        Instant now = Instant.now();
        Timestamp time = Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build();

        // build WZGStellzustand object
        WZGStellzustand wzgStellzustand1 = WZGStellzustand.newBuilder()
                .setId("wzg.AQ_10D_33.h.de")
                .setTlsTime(time)
                .setProcessTime(time)
                .setAnzeigeprinzip(2)
                .setFolgenummer(0)
                .setFunktionsbyte(1)
                .setTextzeichen("toller Text")
                .build();

        WZGStellzustand wzgStellzustand2 = WZGStellzustand.newBuilder()
                .setId("wzg.AQ_10D_34.h.de")
                .setTlsTime(time)
                .setProcessTime(time)
                .setAnzeigeprinzip(2)
                .setFolgenummer(0)
                .setFunktionsbyte(1)
                .setTextzeichen("toller Text")
                .build();

        WZGStellzustand wzgStellzustand3 = WZGStellzustand.newBuilder()
                .setId("wzg.AQ_10D_35.h.de")
                .setTlsTime(time)
                .setProcessTime(time)
                .setAnzeigeprinzip(2)
                .setFolgenummer(0)
                .setFunktionsbyte(1)
                .setTextzeichen("toller Text")
                .build();

        WZGStellzustandList wzgStellzustandList = WZGStellzustandList.newBuilder()
                .setIid("aq.aq_10d.h.de")
                .addElements(wzgStellzustand1)
                .addElements(wzgStellzustand2)
                .addElements(wzgStellzustand3)
                .build();

        ConsumerRecords<String, byte[]> resultList = sendToKafka(
                wzgStellzustand1.getId(),
                wzgStellzustandList.getIid(),
                wzgStellzustandList.toByteArray(),
                WZGStellzustandList.class.getName(),
                3000L,
                properties.getTopicPrefixSend() + "WZGStellzustandSoll",
                properties.getTopicPrefixSend() + "WVZStellzustand48Soll");

        // process result
        List<ConsumerRecord<String, byte[]>> result48 = extractMessages(resultList, properties.getTopicPrefixSend() + "WVZStellzustand48Soll");
        List<ConsumerRecord<String, byte[]>> result55 = extractMessages(resultList, properties.getTopicPrefixSend() + "WZGStellzustand55Soll");

        PWVZStellzustand48List resultObjectList = PWVZStellzustand48List.fromBytes(result48.get(0).value());

        // assert on received object
        assertAll("Compare conversion of WZGStellzustandSoll to WVZStellzustand48Soll",
                () -> assertTrue(result55.isEmpty()),
                () -> assertEquals(wzgStellzustandList.getElementsCount(), resultObjectList.getElementsList().size()),
                () -> assertEquals(wzgStellzustand1.getId(), resultObjectList.getElementsList().get(0).getId()),
                () -> assertEquals(wzgStellzustand2.getId(), resultObjectList.getElementsList().get(1).getId()),
                () -> assertEquals(wzgStellzustand3.getId(), resultObjectList.getElementsList().get(2).getId()),
                () -> assertEquals(wzgStellzustand1.getStellcode(), resultObjectList.getElementsList().get(0).getStellzustand()),
                () -> assertEquals(wzgStellzustand1.getFunktionsbyte(), resultObjectList.getElementsList().get(0).getFunktionsbyte()),
                () -> assertEquals(wzgStellzustand1.getJobnummer(), resultObjectList.getElementsList().get(0).getJobnummer()),
                () -> assertEquals(wzgStellzustand1.getFolgenummer(), resultObjectList.getElementsList().get(0).getFolgenummer()),
                () -> assertEquals(Instant.ofEpochSecond(wzgStellzustand1.getTlsTime().getSeconds(), wzgStellzustand1.getTlsTime().getNanos()),
                        resultObjectList.getElementsList().get(0).getTlsTime()),
                () -> assertTrue(Instant.ofEpochSecond(wzgStellzustand1.getProcessTime().getSeconds(), wzgStellzustand1.getProcessTime().getNanos()).isBefore(
                        resultObjectList.getElementsList().get(0).getProcessTime()))
        );
    }

    @Test
    @Order(6)
    @DisplayName("Convert sent WZGStellzustandSoll to WZGStellzustand55Soll")
    public void testSentWZGStellzustand55() throws ExecutionException, InterruptedException, InvalidProtocolBufferException {
        Instant now = Instant.now();
        Timestamp time = Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build();

        // build WZGStellzustand object
        WZGStellzustand wzgStellzustand = WZGStellzustand.newBuilder()
                .setId("wzg.AQ_12DM_161.h.de")
                .setTlsTime(time)
                .setProcessTime(time)
                .setAnzeigeprinzip(2)
                .setFolgenummer(0)
                .setFunktionsbyte(1)
                .setTextzeichen("toller Text")
                .build();

        WZGStellzustandList wzgStellzustandList = WZGStellzustandList.newBuilder()
                .setIid(idGenerator.newID())
                .addElements(wzgStellzustand)
                .build();

        ConsumerRecords<String, byte[]> resultList = sendToKafka(
                wzgStellzustand.getId(),
                wzgStellzustandList.getIid(),
                wzgStellzustandList.toByteArray(),
                WZGStellzustandList.class.getName(),
                2000L,
                properties.getTopicPrefixSend() + "WZGStellzustandSoll",
                properties.getTopicPrefixSend() + "WZGStellzustand55Soll");

        // process result
        List<ConsumerRecord<String, byte[]>> result48 = extractMessages(resultList, properties.getTopicPrefixSend() + "WVZStellzustand48Soll");
        List<ConsumerRecord<String, byte[]>> result55 = extractMessages(resultList, properties.getTopicPrefixSend() + "WZGStellzustand55Soll");

        PWZGStellzustandList resultObjectList = PWZGStellzustandList.fromBytes(result55.get(0).value());
        PWZGStellzustand resultObject = resultObjectList.getElementsList().get(0);

        // assert on received object
        assertAll("Compare conversion of WZGStellzustandSoll to WZGStellzustandSoll55",
                () -> assertTrue(result48.isEmpty()),
                () -> assertEquals(wzgStellzustandList.getElementsCount(), resultObjectList.getElementsList().size()),
                () -> assertEquals(wzgStellzustand.getId(), resultObject.getId()),
                () -> assertEquals(wzgStellzustand.getAnzeigeprinzip(), resultObject.getAnzeigeprinzip()),
                () -> assertEquals(wzgStellzustand.getFunktionsbyte(), resultObject.getFunktionsbyte()),
                () -> assertEquals(wzgStellzustand.getJobnummer(), resultObject.getJobnummer()),
                () -> assertEquals(wzgStellzustand.getFolgenummer(), resultObject.getFolgenummer()),
                () -> assertEquals(wzgStellzustand.getTextzeichen(), resultObject.getTextzeichen()),
                () -> assertEquals(Instant.ofEpochSecond(wzgStellzustand.getTlsTime().getSeconds(), wzgStellzustand.getTlsTime().getNanos()),
                        resultObject.getTlsTime()),
                () -> assertTrue(Instant.ofEpochSecond(wzgStellzustand.getProcessTime().getSeconds(), wzgStellzustand.getProcessTime().getNanos()).isBefore(
                        resultObject.getProcessTime()))
        );
    }

    @Test
    @Order(7)
    @DisplayName("Convert sent WZGGrundeinstellungSoll to WVZGrundeinstellung32Soll")
    public void testSentWZGGrundeinstellung32() throws ExecutionException, InterruptedException, InvalidProtocolBufferException {
        Instant now = Instant.now();
        Timestamp time = Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build();

        // build WZGGrundeinstellung object
        WZGGrundeinstellung wzgGrundeinstellung1 = WZGGrundeinstellung.newBuilder()
                .setId("wzg.AQ_10D_33.h.de")
                .setTlsTime(time)
                .setProcessTime(time)
                .setAnzeigeprinzip(2)
                .setFolgenummer(0)
                .setFunktionsbyte(1)
                .setTextzeichen("toller Text")
                .build();

        WZGGrundeinstellung wzgGrundeinstellung2 = WZGGrundeinstellung.newBuilder()
                .setId("wzg.AQ_10D_34.h.de")
                .setTlsTime(time)
                .setProcessTime(time)
                .setAnzeigeprinzip(2)
                .setFolgenummer(0)
                .setFunktionsbyte(1)
                .setTextzeichen("toller Text")
                .build();

        WZGGrundeinstellung wzgGrundeinstellung3 = WZGGrundeinstellung.newBuilder()
                .setId("wzg.AQ_10D_35.h.de")
                .setTlsTime(time)
                .setProcessTime(time)
                .setAnzeigeprinzip(2)
                .setFolgenummer(0)
                .setFunktionsbyte(1)
                .setTextzeichen("toller Text")
                .build();

        WZGGrundeinstellungList wzgGrundeinstellungList = WZGGrundeinstellungList.newBuilder()
                .setIid(idGenerator.newID())
                .addElements(wzgGrundeinstellung1)
                .addElements(wzgGrundeinstellung2)
                .addElements(wzgGrundeinstellung3)
                .build();

        ConsumerRecords<String, byte[]> resultList = sendToKafka(
                "aq.aq_10d.h.de",
                wzgGrundeinstellungList.getIid(),
                wzgGrundeinstellungList.toByteArray(),
                WZGGrundeinstellungList.class.getName(),
                3000L,
                properties.getTopicPrefixSend() + "WZGGrundeinstellungSoll",
                properties.getTopicPrefixSend() + "WVZGrundeinstellung32Soll");

        // process result
        List<ConsumerRecord<String, byte[]>> result32 = extractMessages(resultList, properties.getTopicPrefixSend() + "WVZGrundeinstellung32Soll");
        List<ConsumerRecord<String, byte[]>> result33 = extractMessages(resultList, properties.getTopicPrefixSend() + "WZGGrundeinstellung33Soll");

        PWVZGrundeinstellung32List resultObjectList = PWVZGrundeinstellung32List.fromBytes(result32.get(0).value());

        // assert on received object
        assertAll("Compare conversion of WZGGrundeinstellungSoll to WVZGrundeinstellung32Soll",
                () -> assertTrue(result33.isEmpty()),
                () -> assertEquals(wzgGrundeinstellungList.getElementsCount(), resultObjectList.getElementsList().size()),
                () -> assertEquals(wzgGrundeinstellung1.getId(), resultObjectList.getElementsList().get(0).getId()),
                () -> assertEquals(wzgGrundeinstellung2.getId(), resultObjectList.getElementsList().get(1).getId()),
                () -> assertEquals(wzgGrundeinstellung3.getId(), resultObjectList.getElementsList().get(2).getId()),
                () -> assertEquals(wzgGrundeinstellung1.getId(), resultObjectList.getElementsList().get(0).getId()),
                () -> assertEquals(wzgGrundeinstellung1.getStellcode(), resultObjectList.getElementsList().get(0).getStellzustand()),
                () -> assertEquals(wzgGrundeinstellung1.getFunktionsbyte(), resultObjectList.getElementsList().get(0).getFunktionsbyte()),
                () -> assertEquals(wzgGrundeinstellung1.getJobnummer(), resultObjectList.getElementsList().get(0).getJobnummer()),
                () -> assertEquals(wzgGrundeinstellung1.getFolgenummer(), resultObjectList.getElementsList().get(0).getFolgenummer()),
                () -> assertEquals(Instant.ofEpochSecond(wzgGrundeinstellung1.getTlsTime().getSeconds(), wzgGrundeinstellung1.getTlsTime().getNanos()),
                        resultObjectList.getElementsList().get(0).getTlsTime()),
                () -> assertTrue(Instant.ofEpochSecond(wzgGrundeinstellung1.getProcessTime().getSeconds(), wzgGrundeinstellung1.getProcessTime().getNanos()).isBefore(
                        resultObjectList.getElementsList().get(0).getProcessTime()))
        );
    }

    @Test
    @Order(8)
    @DisplayName("Convert sent WZGGrundeinstellungSoll to WZGGrundeinstellung33Soll")
    public void testSentWZGGrundeinstellung33() throws ExecutionException, InterruptedException, InvalidProtocolBufferException {
        Instant now = Instant.now();
        Timestamp time = Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build();

        // build WZGGrundeinstellung object
        WZGGrundeinstellung wzgGrundeinstellung = WZGGrundeinstellung.newBuilder()
                .setId("wzg.AQ_12DM_161.h.de")
                .setTlsTime(time)
                .setProcessTime(time)
                .setAnzeigeprinzip(2)
                .setFolgenummer(0)
                .setFunktionsbyte(1)
                .setTextzeichen("toller Text")
                .build();

        WZGGrundeinstellungList wzgGrundeinstellungList = WZGGrundeinstellungList.newBuilder()
                .setIid(idGenerator.newID())
                .addElements(wzgGrundeinstellung)
                .build();

        ConsumerRecords<String, byte[]> resultList = sendToKafka(
                wzgGrundeinstellung.getId(),
                wzgGrundeinstellungList.getIid(),
                wzgGrundeinstellungList.toByteArray(),
                WZGGrundeinstellungList.class.getName(),
                2000L,
                properties.getTopicPrefixSend() + "WZGGrundeinstellungSoll",
                properties.getTopicPrefixSend() + "WZGGrundeinstellung33Soll");

        // process result
        List<ConsumerRecord<String, byte[]>> result32 = extractMessages(resultList, properties.getTopicPrefixSend() + "WVZGrundeinstellung32Soll");
        List<ConsumerRecord<String, byte[]>> result33 = extractMessages(resultList, properties.getTopicPrefixSend() + "WZGGrundeinstellung33Soll");

        PWZGGrundeinstellungList resultObjectList = PWZGGrundeinstellungList.fromBytes(result33.get(0).value());
        PWZGGrundeinstellung resultObject = resultObjectList.getElementsList().get(0);

        // assert on received object
        assertAll("Compare conversion of WZGGrundeinstellungSoll to WZGGrundeinstellungSoll55",
                () -> assertTrue(result32.isEmpty()),
                () -> assertEquals(wzgGrundeinstellung.getId(), resultObject.getId()),
                () -> assertEquals(wzgGrundeinstellung.getAnzeigeprinzip(), resultObject.getAnzeigeprinzip()),
                () -> assertEquals(wzgGrundeinstellung.getFunktionsbyte(), resultObject.getFunktionsbyte()),
                () -> assertEquals(wzgGrundeinstellung.getJobnummer(), resultObject.getJobnummer()),
                () -> assertEquals(wzgGrundeinstellung.getFolgenummer(), resultObject.getFolgenummer()),
                () -> assertEquals(wzgGrundeinstellung.getTextzeichen(), resultObject.getTextzeichen()),
                () -> assertEquals(Instant.ofEpochSecond(wzgGrundeinstellung.getTlsTime().getSeconds(), wzgGrundeinstellung.getTlsTime().getNanos()),
                        resultObject.getTlsTime()),
                () -> assertTrue(Instant.ofEpochSecond(wzgGrundeinstellung.getProcessTime().getSeconds(), wzgGrundeinstellung.getProcessTime().getNanos()).isBefore(
                        resultObject.getProcessTime()))
        );
    }

    @Test
    @Order(9)
    @DisplayName("Convert sent WZGSteuerSequenz with action 3005055 to WZGSteuerSequenz with 3005348")
    public void testSentWZGSteuerSequenz48() throws ExecutionException, InterruptedException, InvalidProtocolBufferException {
        Instant now = Instant.now();
        Timestamp time = Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build();

        // build WZGSteuerSequenz object
        SteuerSequenz steuerSequenz = SteuerSequenz.newBuilder()
                .setId("wzg.AQ_12D_106.h.de")
                .setTlsTime(time)
                .setProcessTime(time)
                .setAction(3005055)
                .setFg(4)
                .build();

        SteuerSequenzList steuerSequenzList = SteuerSequenzList.newBuilder()
                .setIid(idGenerator.newID())
                .addElements(steuerSequenz)
                .build();

        ConsumerRecords<String, byte[]> resultList = sendToKafka(
                steuerSequenz.getId(),
                steuerSequenzList.getIid(),
                steuerSequenzList.toByteArray(),
                SteuerSequenzList.class.getName(),
                2000L,
                properties.getTopicPrefixSend() + "WZGSteuerSequenz");

        // process result
        List<ConsumerRecord<String, byte[]>> result = extractMessages(resultList, properties.getTopicPrefixSend() + "WZGSteuerSequenz");

        PSteuerSequenzList resultObjectList = PSteuerSequenzList.fromBytes(result.get(0).value());
        PSteuerSequenz resultObject = resultObjectList.getElementsList().get(0);

        // assert on received object
        assertAll("Compare conversion of WZGSteuerSequenz with action 3005055 to WZGSteuerSequenz with 3005348",
                () -> assertEquals(steuerSequenz.getId(), resultObject.getId()),
                () -> assertEquals(3005348, resultObject.getAction()),
                () -> assertEquals(Instant.ofEpochSecond(steuerSequenz.getTlsTime().getSeconds(), steuerSequenz.getTlsTime().getNanos()),
                        resultObject.getTlsTime()),
                () -> assertTrue(Instant.ofEpochSecond(steuerSequenz.getProcessTime().getSeconds(), steuerSequenz.getProcessTime().getNanos()).isBefore(
                        resultObject.getProcessTime()))
        );
    }

    @Test
    @Order(10)
    @DisplayName("Convert sent WZGSteuerSequenz with action 3005055 to WZGSteuerSequenz with 3005355")
    public void testSentWZGSteuerSequenz55() throws ExecutionException, InterruptedException, InvalidProtocolBufferException {
        Instant now = Instant.now();
        Timestamp time = Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build();

        // build WZGSteuerSequenz object
        SteuerSequenz steuerSequenz = SteuerSequenz.newBuilder()
                .setId("wzg.AQ_12DM_161.h.de")
                .setTlsTime(time)
                .setProcessTime(time)
                .setAction(3005055)
                .setFg(4)
                .build();

        SteuerSequenzList steuerSequenzList = SteuerSequenzList.newBuilder()
                .setIid(idGenerator.newID())
                .addElements(steuerSequenz)
                .build();

        ConsumerRecords<String, byte[]> resultList = sendToKafka(
                steuerSequenz.getId(),
                steuerSequenzList.getIid(),
                steuerSequenzList.toByteArray(),
                SteuerSequenzList.class.getName(),
                2000L,
                properties.getTopicPrefixSend() + "WZGSteuerSequenz");

        // process result
        List<ConsumerRecord<String, byte[]>> result = extractMessages(resultList, properties.getTopicPrefixSend() + "WZGSteuerSequenz");

        PSteuerSequenzList resultObjectList = PSteuerSequenzList.fromBytes(result.get(0).value());
        PSteuerSequenz resultObject = resultObjectList.getElementsList().get(0);

        // assert on received object
        assertAll("Compare conversion of WZGSteuerSequenz with action 3005055 to WZGSteuerSequenz with 3005355",
                () -> assertEquals(steuerSequenz.getId(), resultObject.getId()),
                () -> assertEquals(3005355, resultObject.getAction()),
                () -> assertEquals(Instant.ofEpochSecond(steuerSequenz.getTlsTime().getSeconds(), steuerSequenz.getTlsTime().getNanos()),
                        resultObject.getTlsTime()),
                () -> assertTrue(Instant.ofEpochSecond(steuerSequenz.getProcessTime().getSeconds(), steuerSequenz.getProcessTime().getNanos()).isBefore(
                        resultObject.getProcessTime()))
        );
    }

    @Test
    @Order(11)
    @DisplayName("Convert sent WZGSteuerSequenz with action 3003033 to WZGSteuerSequenz with 3003332")
    public void testSentWZGSteuerSequenz32() throws ExecutionException, InterruptedException, InvalidProtocolBufferException {
        Instant now = Instant.now();
        Timestamp time = Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build();

        // build WZGSteuerSequenz object
        SteuerSequenz steuerSequenz = SteuerSequenz.newBuilder()
                .setId("wzg.AQ_12D_106.h.de")
                .setTlsTime(time)
                .setProcessTime(time)
                .setAction(3003033)
                .setFg(4)
                .build();

        SteuerSequenzList steuerSequenzList = SteuerSequenzList.newBuilder()
                .setIid(idGenerator.newID())
                .addElements(steuerSequenz)
                .build();

        ConsumerRecords<String, byte[]> resultList = sendToKafka(
                steuerSequenz.getId(),
                steuerSequenzList.getIid(),
                steuerSequenzList.toByteArray(),
                SteuerSequenzList.class.getName(),
                2000L,
                properties.getTopicPrefixSend() + "WZGSteuerSequenz");

        // process result
        List<ConsumerRecord<String, byte[]>> result = extractMessages(resultList, properties.getTopicPrefixSend() + "WZGSteuerSequenz");

        PSteuerSequenzList resultObjectList = PSteuerSequenzList.fromBytes(result.get(0).value());
        PSteuerSequenz resultObject = resultObjectList.getElementsList().get(0);

        // assert on received object
        assertAll("Compare conversion of WZGSteuerSequenz with action 3003033 to WZGSteuerSequenz with 3003332",
                () -> assertEquals(steuerSequenz.getId(), resultObject.getId()),
                () -> assertEquals(3003332, resultObject.getAction()),
                () -> assertEquals(Instant.ofEpochSecond(steuerSequenz.getTlsTime().getSeconds(), steuerSequenz.getTlsTime().getNanos()),
                        resultObject.getTlsTime()),
                () -> assertTrue(Instant.ofEpochSecond(steuerSequenz.getProcessTime().getSeconds(), steuerSequenz.getProcessTime().getNanos()).isBefore(
                        resultObject.getProcessTime()))
        );
    }

    @Test
    @Order(12)
    @DisplayName("Convert sent WZGSteuerSequenz with action 3003033 to WZGSteuerSequenz with 3003333")
    public void testSentWZGSteuerSequenz33() throws ExecutionException, InterruptedException, InvalidProtocolBufferException {
        Instant now = Instant.now();
        Timestamp time = Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build();

        // build WZGSteuerSequenz object
        SteuerSequenz steuerSequenz = SteuerSequenz.newBuilder()
                .setId("wzg.AQ_12DM_161.h.de")
                .setTlsTime(time)
                .setProcessTime(time)
                .setAction(3003033)
                .setFg(4)
                .build();

        SteuerSequenzList steuerSequenzList = SteuerSequenzList.newBuilder()
                .setIid(idGenerator.newID())
                .addElements(steuerSequenz)
                .build();

        ConsumerRecords<String, byte[]> resultList = sendToKafka(
                steuerSequenz.getId(),
                steuerSequenzList.getIid(),
                steuerSequenzList.toByteArray(),
                SteuerSequenzList.class.getName(),
                2000L,
                properties.getTopicPrefixSend() + "WZGSteuerSequenz");

        // process result
        List<ConsumerRecord<String, byte[]>> result = extractMessages(resultList, properties.getTopicPrefixSend() + "WZGSteuerSequenz");

        PSteuerSequenzList resultObjectList = PSteuerSequenzList.fromBytes(result.get(0).value());
        PSteuerSequenz resultObject = resultObjectList.getElementsList().get(0);

        // assert on received object
        assertAll("Compare conversion of WZGSteuerSequenz with action 3003033 to WZGSteuerSequenz with 3003333",
                () -> assertEquals(steuerSequenz.getId(), resultObject.getId()),
                () -> assertEquals(3003333, resultObject.getAction()),
                () -> assertEquals(Instant.ofEpochSecond(steuerSequenz.getTlsTime().getSeconds(), steuerSequenz.getTlsTime().getNanos()),
                        resultObject.getTlsTime()),
                () -> assertTrue(Instant.ofEpochSecond(steuerSequenz.getProcessTime().getSeconds(), steuerSequenz.getProcessTime().getNanos()).isBefore(
                        resultObject.getProcessTime()))
        );
    }
}

