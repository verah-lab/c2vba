package de.heuboe.tls.sequencer.test;

import de.heuboe.tls.cfglib.Osi7Cfg;
import de.heuboe.tls.grammar.sequencer.ObjectDirection;
import de.heuboe.tls.grammar.sequencer.flops.FlopStorage;
import de.heuboe.tls.grammar.sequencer.flops.FlopType;
import de.heuboe.tls.kafka.operator.services.KafkaOperatorService;
import de.heuboe.tls.parser.proto.GenericProtoObject;
import de.heuboe.tls.received.*;
import de.heuboe.tls.received.pojo.*;
import de.heuboe.tls.sequencer.config.SequencerProperties;
import de.heuboe.tls.sequencer.parser.Parser;
import de.heuboe.tls.sequencer.services.SequencerSendingService;
import de.heuboe.tls.sequencer.test.helper.EmptySequencerMessageManagementStub;
import de.heuboe.tls.sequencer.test.helper.MockedCfgGetter;
import de.heuboe.tls.sequencer.utils.SequencerBeanContainer;
import de.heuboe.tls.sequencer.utils.SequencerUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.heuboe.tls.sequencer.utils.SequencerUtils.TOPIC_TARGET_KEY;
import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext
@EnableKafka
@EmbeddedKafka(
        partitions = 1,
        ports = {12345},
        brokerProperties = {"log.dir=target/kafka${random.int}"}
)

@EnableAutoConfiguration
@EnableConfigurationProperties
@ContextConfiguration(classes = {SequencerProperties.class})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=EmbeddedKafkaTest",
        "spring.kafka.listener.missing-topics-fatal=false"},
        locations = "classpath:application.properties"
)
@Import({SequencerBeanContainer.class,
        Parser.class,
        SequencerUtils.class,
        SequencerProperties.class,
        FlopStorage.class}
)
@SpringBootTest
@Slf4j
public class ParserTest {

    @Autowired
    private SequencerBeanContainer sequencerBeanContainer;

    @MockBean
    private KafkaOperatorService mockedKafkaOperatorService;

    @MockBean
    private SequencerSendingService mockedSequencerSendingService;

    @MockBean
    private EmptySequencerMessageManagementStub mockedSequencerMessageManagement;

    @Autowired
    private SequencerProperties properties;

    @Autowired
    private Parser parser;

    @Autowired
    private SequencerUtils utils;

    private final int FEHLERCODE_1_TESTARRAYELEMENTACCESSINSCRIPT = 1;

    @BeforeEach
    public void createParser() throws Exception {

        Mockito.when(mockedSequencerSendingService.getKafkaOperatorService()).thenReturn(mockedKafkaOperatorService);

        Osi7Cfg osi7Cfg = new Osi7Cfg();
        osi7Cfg.setCfgSvc(new MockedCfgGetter());
        osi7Cfg.buildUZConfig(properties.getUzid());

        // add Osi7Cfg to bean container to access config in grammar execution
        sequencerBeanContainer.setOsi7Cfg(osi7Cfg);
        sequencerBeanContainer.setFlopStorage(new FlopStorage());
        sequencerBeanContainer.setSequencerSendingService(mockedSequencerSendingService);
        sequencerBeanContainer.setSequencerMessageManagement(mockedSequencerMessageManagement);
        parser.setSequencerBeanContainer(sequencerBeanContainer);

        // initialize parser with Osi7Cfg
        parser.setOsi7Cfg(osi7Cfg);
    }

    // ----------------------------------------------------------------
    // Script seq-uz.txt
    // ----------------------------------------------------------------

    @Test
    public void testDue2DeAndTimeSyncInit_1() throws IOException {

        PSYSFehlerDUE pojo = PSYSFehlerDUE.builder()
                .id("KRI_PPP_EB_2C")
                .tlsTime(Instant.now())
                .processTime(Instant.now())
                .fehlercode(0)
                .hersteller(6)
                .build();

        SYSFehlerDUE messageV3 = PSYSFehlerDUE.to(pojo);

        GenericProtoObject gpo = new GenericProtoObject(messageV3.getClass().getName(), messageV3, Collections.emptyMap());

        List<GenericProtoObject> result = List.copyOf(parser.parse("SYSFehlerDUE", gpo, false));
        Set<String> resultClasses = result.stream().map(GenericProtoObject::getClassName).collect(Collectors.toSet());

        // assert on received object
        assertAll("Object content for test case due2de and time sync initialization",
                () -> assertEquals(77, result.size()),
                () -> assertEquals(4, resultClasses.size()),
                () -> assertTrue(resultClasses.containsAll(List.of("VLTDeFehler", "SteuerSequenz", "WZGDeFehler", "UFDDeFehler"))),
                () -> assertTrue(result.stream().filter(e -> e.getClassName().equals("SteuerSequenz")).anyMatch(e -> e.getIntegerValue("action") == 4002018)),
                () -> assertTrue(result.stream().filter(e -> e.getClassName().equals("SteuerSequenz")).anyMatch(e -> e.getIntegerValue("fg") == 254))
        );
    }

    @Test
    public void testDue2DeAndTimeSyncInit_2() throws IOException {

        PSYSFehlerDUE pojo = PSYSFehlerDUE.builder()
                .id("KRI_PPP_KB_2C")
                .tlsTime(Instant.now())
                .processTime(Instant.now())
                .fehlercode(0)
                .hersteller(6)
                .build();

        SYSFehlerDUE messageV3 = PSYSFehlerDUE.to(pojo);

        GenericProtoObject gpo = new GenericProtoObject(messageV3.getClass().getName(), messageV3, Collections.emptyMap());

        List<GenericProtoObject> result = List.copyOf(parser.parse("SYSFehlerDUE", gpo, false));
        Set<String> resultClasses = result.stream().map(GenericProtoObject::getClassName).collect(Collectors.toSet());

        // assert on received object
        assertAll("Object content for test case due2de and time sync initialization",
                () -> assertEquals(1, result.size()),
                () -> assertEquals(1, resultClasses.size()),
                () -> assertTrue(resultClasses.contains("SteuerSequenz")),
                () -> assertTrue(result.stream().filter(e -> e.getClassName().equals("SteuerSequenz")).anyMatch(e -> e.getIntegerValue("action") == 4002018)),
                () -> assertTrue(result.stream().filter(e -> e.getClassName().equals("SteuerSequenz")).anyMatch(e -> e.getIntegerValue("fg") == 254)),
                () -> assertEquals(ObjectDirection.OUT, result.getFirst().getMetaData().get(ObjectDirection.class.getSimpleName())),
                () -> assertEquals("SYSSteuerSequenz", result.getFirst().getMetaData().get(TOPIC_TARGET_KEY))
        );
    }

    @Test
    public void testWZGDeError_1() throws IOException {

        PWZGDeFehler pojo = PWZGDeFehler.builder()
                .id("WWW_S01_2_235.Cl4")
                .tlsTime(Instant.now())
                .processTime(Instant.now())
                .fehlercode(0)
                .hersteller(6)
                .build();

        WZGDeFehler messageV3 = PWZGDeFehler.to(pojo);

        GenericProtoObject gpo = new GenericProtoObject(messageV3.getClass().getName(), messageV3, Collections.emptyMap());

        List<GenericProtoObject> result = List.copyOf(parser.parse("WZGDeFehler", gpo, false));
        Set<String> resultClasses = result.stream().map(GenericProtoObject::getClassName).collect(Collectors.toSet());

        // assert on received object
        assertAll("Object content for test case FG 4 (DE-Gut-Meldung) in if case",
                () -> assertEquals(1, result.size()),
                () -> assertEquals(1, resultClasses.size()),
                () -> assertTrue(resultClasses.contains("SteuerSequenz")),
                () -> assertTrue(result.stream().filter(e -> e.getClassName().equals("SteuerSequenz")).anyMatch(e -> e.getStringValue("id").equals("WWW_S01_2_235.Cl4"))),
                () -> assertTrue(result.stream().filter(e -> e.getClassName().equals("SteuerSequenz")).anyMatch(e -> e.getIntegerValue("action") == 3002017)),
                () -> assertTrue(result.stream().filter(e -> e.getClassName().equals("SteuerSequenz")).anyMatch(e -> e.getIntegerValue("fg") == 4)),
                () -> assertEquals(ObjectDirection.OUT, result.getFirst().getMetaData().get(ObjectDirection.class.getSimpleName())),
                () -> assertEquals("WZGSteuerSequenz", result.getFirst().getMetaData().get(TOPIC_TARGET_KEY))
        );
    }

    @Test
    public void testWZGDeError_2() throws IOException {

        PWZGDeFehler pojo = PWZGDeFehler.builder()
                .id("SM_S01_2_235")
                .tlsTime(Instant.now())
                .processTime(Instant.now())
                .fehlercode(0)
                .hersteller(6)
                .build();

        WZGDeFehler messageV3 = PWZGDeFehler.to(pojo);

        GenericProtoObject gpo = new GenericProtoObject(messageV3.getClass().getName(), messageV3, Collections.emptyMap());

        List<GenericProtoObject> result = List.copyOf(parser.parse("WZGDeFehler", gpo, false));
        Set<String> resultClasses = result.stream().map(GenericProtoObject::getClassName).collect(Collectors.toSet());

        // assert on received object
        assertAll("Object content for test case FG 4 (DE-Gut-Meldung) in else case",
                () -> assertEquals(1, result.size()),
                () -> assertEquals(1, resultClasses.size()),
                () -> assertTrue(resultClasses.contains("SteuerSequenz")),
                () -> assertTrue(result.stream().filter(e -> e.getClassName().equals("SteuerSequenz")).anyMatch(e -> e.getStringValue("id").equals(pojo.getId()))),
                () -> assertTrue(result.stream().filter(e -> e.getClassName().equals("SteuerSequenz")).anyMatch(e -> e.getIntegerValue("action") == 3002029)),
                () -> assertTrue(result.stream().filter(e -> e.getClassName().equals("SteuerSequenz")).anyMatch(e -> e.getIntegerValue("fg") == 4)),
                () -> assertEquals(ObjectDirection.OUT, result.getFirst().getMetaData().get(ObjectDirection.class.getSimpleName())),
                () -> assertEquals("WZGSteuerSequenz", result.getFirst().getMetaData().get(TOPIC_TARGET_KEY))
        );
    }

    // ----------------------------------------------------------------
    // Script seq-uz_wie.txt
    // ----------------------------------------------------------------
    @Test
    public void testSwitchCase_1() throws IOException {

        PWZGHelligkeit pojo = PWZGHelligkeit.builder()
                .id("MQ_S01_1_840_F1")
                .tlsTime(Instant.now())
                .processTime(Instant.now())
                .helligkeit(10)
                .statusbyte(1)
                .build();

        WZGHelligkeit messageV3 = PWZGHelligkeit.to(pojo);

        GenericProtoObject gpo = new GenericProtoObject(messageV3.getClass().getName(), messageV3, Collections.emptyMap());

        List<GenericProtoObject> result = List.copyOf(parser.parse("WZGHelligkeit", gpo, false));
        Set<String> resultClasses = result.stream().map(GenericProtoObject::getClassName).collect(Collectors.toSet());

        // assert on received object
        assertAll("Object content for test case switch case with brightness 10",
                () -> assertEquals(1, result.size()),
                () -> assertEquals(1, resultClasses.size()),
                () -> assertTrue(resultClasses.contains("SteuerSequenz")),
                () -> assertTrue(result.stream().filter(e -> e.getClassName().equals("SteuerSequenz")).anyMatch(e -> e.getStringValue("id").equals(pojo.getId()))),
                () -> assertTrue(result.stream().filter(e -> e.getClassName().equals("SteuerSequenz")).anyMatch(e -> e.getIntegerValue("action") == 3005049)),
                () -> assertTrue(result.stream().filter(e -> e.getClassName().equals("SteuerSequenz")).anyMatch(e -> e.getIntegerValue("fg") == 4)),
                () -> assertEquals(ObjectDirection.OUT, result.getFirst().getMetaData().get(ObjectDirection.class.getSimpleName())),
                () -> assertEquals("WZGSteuerSequenz", result.getFirst().getMetaData().get(TOPIC_TARGET_KEY))
        );
    }

    @Test
    public void testSwitchCase_2() throws IOException {

        PWZGHelligkeit pojo = PWZGHelligkeit.builder()
                .id("MQ_S01_1_840_F1")
                .tlsTime(Instant.now())
                .processTime(Instant.now())
                .helligkeit(11)
                .statusbyte(1)
                .build();

        WZGHelligkeit messageV3 = PWZGHelligkeit.to(pojo);

        GenericProtoObject gpo = new GenericProtoObject(messageV3.getClass().getName(), messageV3, Collections.emptyMap());

        List<GenericProtoObject> result = List.copyOf(parser.parse("WZGHelligkeit", gpo, false));
        Set<String> resultClasses = result.stream().map(GenericProtoObject::getClassName).collect(Collectors.toSet());

        // assert on received object
        assertAll("Object content for test case switch case with brightness 11",
                () -> assertEquals(1, result.size()),
                () -> assertEquals(1, resultClasses.size()),
                () -> assertTrue(resultClasses.contains("SteuerSequenz")),
                () -> assertTrue(result.stream().filter(e -> e.getClassName().equals("SteuerSequenz")).anyMatch(e -> e.getStringValue("id").equals(pojo.getId()))),
                () -> assertTrue(result.stream().filter(e -> e.getClassName().equals("SteuerSequenz")).anyMatch(e -> e.getIntegerValue("action") == 3003036)),
                () -> assertTrue(result.stream().filter(e -> e.getClassName().equals("SteuerSequenz")).anyMatch(e -> e.getIntegerValue("fg") == 4)),
                () -> assertEquals(ObjectDirection.OUT, result.getFirst().getMetaData().get(ObjectDirection.class.getSimpleName())),
                () -> assertEquals("WZGSteuerSequenz", result.getFirst().getMetaData().get(TOPIC_TARGET_KEY))
        );
    }

    @Test
    public void testSwitchCase_3() throws IOException {

        PWZGHelligkeit pojo = PWZGHelligkeit.builder()
                .id("MQ_S01_1_840_F1")
                .tlsTime(Instant.now())
                .processTime(Instant.now())
                .helligkeit(12)
                .statusbyte(1)
                .build();

        WZGHelligkeit messageV3 = PWZGHelligkeit.to(pojo);

        GenericProtoObject gpo = new GenericProtoObject(messageV3.getClass().getName(), messageV3, Collections.emptyMap());

        List<GenericProtoObject> result = List.copyOf(parser.parse("WZGHelligkeit", gpo, false));
        Set<String> resultClasses = result.stream().map(GenericProtoObject::getClassName).collect(Collectors.toSet());

        // assert on received object
        assertAll("Object content for test case switch case with brightness 10",
                () -> assertEquals(1, result.size()),
                () -> assertEquals(1, resultClasses.size()),
                () -> assertTrue(resultClasses.contains("SteuerSequenz")),
                () -> assertTrue(result.stream().filter(e -> e.getClassName().equals("SteuerSequenz")).anyMatch(e -> e.getStringValue("id").equals(pojo.getId()))),
                () -> assertTrue(result.stream().filter(e -> e.getClassName().equals("SteuerSequenz")).anyMatch(e -> e.getIntegerValue("action") == 3002017)),
                () -> assertTrue(result.stream().filter(e -> e.getClassName().equals("SteuerSequenz")).anyMatch(e -> e.getIntegerValue("fg") == 4)),
                () -> assertEquals(ObjectDirection.OUT, result.getFirst().getMetaData().get(ObjectDirection.class.getSimpleName())),
                () -> assertEquals("WZGSteuerSequenz", result.getFirst().getMetaData().get(TOPIC_TARGET_KEY))
        );
    }

    // ----------------------------------------------------------------
    // Script seq-test.txt
    // ----------------------------------------------------------------
    @Test
    public void testFG254TimeSync() throws IOException {

        PSYSZeitsynchronisation pojo = PSYSZeitsynchronisation.builder()
                .id("KRI_PPP_KB_2C")
                .tlsTime(Instant.now())
                .processTime(Instant.now())
                .wochentag(2)
                .zeitstempel(Instant.now().minusSeconds(1000))
                .build();

        SYSZeitsynchronisation messageV3 = PSYSZeitsynchronisation.to(pojo);

        GenericProtoObject gpo = new GenericProtoObject(messageV3.getClass().getName(), messageV3, Collections.emptyMap());

        List<GenericProtoObject> result = List.copyOf(parser.parse("SYSZeitsynchronisation", gpo, false));

        String flopName = "MONO_7_RETRIGGERABLE_15_SYSZEITSYNCHRONISATION";

        // assert on received object
        assertAll("Object content for test case FG 254 time sync",
                () -> assertTrue(sequencerBeanContainer.getFlopStorage().exists(flopName)),
                () -> assertEquals(2, sequencerBeanContainer.getFlopStorage().getFlop(flopName).getActions().size()),
                () -> assertEquals(7, sequencerBeanContainer.getFlopStorage().getFlop(flopName).getParameter().getTimeout()),
                () -> assertEquals(15, sequencerBeanContainer.getFlopStorage().getFlop(flopName).getParameter().getMaxTimeout()),
                () -> assertEquals(FlopType.MONO.toString(), sequencerBeanContainer.getFlopStorage().getFlop(flopName).getType().name()),
                () -> assertEquals("ObjectAssignStatement", sequencerBeanContainer.getFlopStorage().getFlop(flopName).getActions().getFirst().getClassName()),
                () -> assertEquals("Message", sequencerBeanContainer.getFlopStorage().getFlop(flopName).getActions().get(1).getClassName())
        );
    }

    @Test
    public void testCommStateCopy() throws IOException {
        PSYSKommunikationsstatus pojo = PSYSKommunikationsstatus.builder()
                .id("KRI_PPP_KB_2C")
                .tlsTime(Instant.now())
                .processTime(Instant.now())
                .status(42)
                .build();

        SYSKommunikationsstatus messageV3 = PSYSKommunikationsstatus.to(pojo);

        GenericProtoObject gpo = new GenericProtoObject(messageV3.getClass().getName(), messageV3, Collections.emptyMap());

        List<GenericProtoObject> result = List.copyOf(parser.parse("SYSKommunikationsstatus", gpo, false));

        // assert on received object
        assertAll("Object content for test case comm state copy",
                () -> assertEquals(1, result.size()),
                () -> assertEquals("SYSFehlerDUE", result.getFirst().getClassName()),
                () -> assertEquals(pojo.getId(), result.getFirst().get("id")),
                () -> assertEquals(pojo.getStatus(), result.getFirst().get("fehlercode")),
                () -> assertEquals(6, result.getFirst().get("hersteller")),
                () -> assertEquals(ObjectDirection.IN, result.getFirst().getMetaData().get(ObjectDirection.class.getSimpleName())),
                () -> assertEquals(pojo.getTlsTime(), Instant.ofEpochSecond(result.getFirst().getTimestampValue("tlsTime").getSeconds(), result.getFirst().getTimestampValue("tlsTime").getNanos())),
                () -> assertTrue(pojo.getProcessTime().isBefore(Instant.ofEpochSecond(result.getFirst().getTimestampValue("processTime").getSeconds(), result.getFirst().getTimestampValue("processTime").getNanos())))
        );
    }

    @Test
    public void testWZGBilddefinitionCopy_1() throws IOException {
        PWZGBilddefinition pojo = PWZGBilddefinition.builder()
                .id("copy with tlsTime")
                .tlsTime(Instant.now())
                .processTime(Instant.now())
                .stellcode(13)
                .dateiname("EpicFile")
                .build();

        WZGBilddefinition messageV3 = PWZGBilddefinition.to(pojo);

        GenericProtoObject gpo = new GenericProtoObject(messageV3.getClass().getName(), messageV3, new HashMap<>());

        List<GenericProtoObject> result = List.copyOf(parser.parse("WZGBilddefinition", gpo, false));

        // assert on received object
        assertAll("Object content for test case comm state copy",
                () -> assertEquals(1, result.size()),
                () -> assertEquals("WZGBilddefinition", result.getFirst().getClassName()),
                () -> assertEquals(pojo.getId(), result.getFirst().get("id")),
                () -> assertEquals(pojo.getStellcode(), result.getFirst().get("stellcode")),
                () -> assertEquals(pojo.getDateiname(), result.getFirst().get("dateiname")),
                () -> assertEquals("WZGBilddefinitionIst", result.getFirst().getMetaData().get(TOPIC_TARGET_KEY)),
                () -> assertEquals(ObjectDirection.OUT, result.getFirst().getMetaData().get(ObjectDirection.class.getSimpleName())),
                () -> assertEquals(pojo.getTlsTime(), Instant.ofEpochSecond(result.getFirst().getTimestampValue("tlsTime").getSeconds(), result.getFirst().getTimestampValue("tlsTime").getNanos())),
                () -> assertTrue(pojo.getProcessTime().isBefore(Instant.ofEpochSecond(result.getFirst().getTimestampValue("processTime").getSeconds(), result.getFirst().getTimestampValue("processTime").getNanos())))
        );
    }

    @Test
    public void testWZGBilddefinitionCopy_2() throws IOException, InterruptedException {
        PWZGBilddefinition pojo = PWZGBilddefinition.builder()
                .id("copy without tlsTime")
                .processTime(Instant.now())
                .stellcode(13)
                .dateiname("EpicFile")
                .build();

        WZGBilddefinition messageV3 = PWZGBilddefinition.to(pojo);

        GenericProtoObject gpo = new GenericProtoObject(messageV3.getClass().getName(), messageV3, new HashMap<>());

        List<GenericProtoObject> result = List.copyOf(parser.parse("WZGBilddefinition", gpo, false));

        // convert time objects from result to instants for better comparison
        Instant tlsTime = Instant.ofEpochSecond(result.getFirst().getTimestampValue("tlsTime").getSeconds(), result.getFirst().getTimestampValue("tlsTime").getNanos());

        // assert on received object
        assertAll("Object content for test case comm state copy",
                () -> assertEquals(1, result.size()),
                () -> assertEquals("WZGBilddefinition", result.getFirst().getClassName()),
                () -> assertEquals(pojo.getId(), result.getFirst().get("id")),
                () -> assertEquals(pojo.getStellcode(), result.getFirst().get("stellcode")),
                () -> assertEquals(pojo.getDateiname(), result.getFirst().get("dateiname")),
                () -> assertEquals("WZGBilddefinitionIst", result.getFirst().getMetaData().get(TOPIC_TARGET_KEY)),
                () -> assertEquals(ObjectDirection.OUT, result.getFirst().getMetaData().get(ObjectDirection.class.getSimpleName())),
                () -> assertEquals(0L, tlsTime.getEpochSecond()),
                () -> assertEquals(0L, tlsTime.getNano()),
                () -> assertTrue(pojo.getProcessTime().isBefore(Instant.ofEpochSecond(result.getFirst().getTimestampValue("processTime").getSeconds(), result.getFirst().getTimestampValue("processTime").getNanos())))
        );
    }

    @Test
    public void testWZGBilddefinitionCopy_3() throws IOException {
        PWZGBilddefinition pojo = PWZGBilddefinition.builder()
                .id("MQ_S05_1_916_F2")
                .tlsTime(Instant.now())
                .processTime(Instant.now())
                .stellcode(13)
                .dateiname("EpicFile")
                .build();

        WZGBilddefinition messageV3 = PWZGBilddefinition.to(pojo);

        GenericProtoObject gpo = new GenericProtoObject(messageV3.getClass().getName(), messageV3, new HashMap<>());

        List<GenericProtoObject> result = List.copyOf(parser.parse("WZGBilddefinition", gpo, false));

        // assert on received object
        assertAll("Object content for test case comm state copy",
                () -> assertEquals(1, result.size()),
                () -> assertEquals("WZGBilddefinition", result.getFirst().getClassName()),
                () -> assertEquals(pojo.getId(), result.getFirst().get("id")),
                () -> assertEquals(0, result.getFirst().get("stellcode")),
                () -> assertEquals(pojo.getDateiname(), result.getFirst().get("dateiname")),
                () -> assertEquals(ObjectDirection.IN, result.getFirst().getMetaData().get(ObjectDirection.class.getSimpleName())),
                () -> assertEquals(pojo.getTlsTime(), Instant.ofEpochSecond(result.getFirst().getTimestampValue("tlsTime").getSeconds(), result.getFirst().getTimestampValue("tlsTime").getNanos())),
                () -> assertTrue(pojo.getProcessTime().isBefore(Instant.ofEpochSecond(result.getFirst().getTimestampValue("processTime").getSeconds(), result.getFirst().getTimestampValue("processTime").getNanos())))
        );
    }

    @Test
    public void testDateTimeFunction_1() throws IOException, InterruptedException {
        PWZGStellzustand pojo = PWZGStellzustand.builder()
                .id("currentTime")
                .processTime(Instant.now())
                .jobnummer(9)
                .folgenummer(123)
                .anzeigeprinzip(4)
                .stellcode(13)
                .funktionsbyte(1)
                .textzeichen("Test")
                .build();

        WZGStellzustand messageV3 = PWZGStellzustand.to(pojo);

        GenericProtoObject gpo = new GenericProtoObject(messageV3.getClass().getName(), messageV3, new HashMap<>());

        Instant beforeParse = Instant.now();
        Thread.sleep(100L); // wait to be sure that we have a time difference
        List<GenericProtoObject> result = List.copyOf(parser.parse("WZGStellzustandJob", gpo, false));
        Thread.sleep(100L); // wait to be sure that we have a time difference
        Instant afterParse = Instant.now();

        // convert time objects from result to instants for better comparison
        Instant tlsTime = Instant.ofEpochSecond(result.getFirst().getTimestampValue("tlsTime").getSeconds(), result.getFirst().getTimestampValue("tlsTime").getNanos());
        Instant processTime = Instant.ofEpochSecond(result.getFirst().getTimestampValue("processTime").getSeconds(), result.getFirst().getTimestampValue("processTime").getNanos());

        // assert on received object
        assertAll("Object content for test case dateTime function with current time creation",
                () -> assertEquals(1, result.size()),
                () -> assertEquals("WZGNegativeQuittung", result.getFirst().getClassName()),
                () -> assertEquals(pojo.getId(), result.getFirst().get("id")),
                () -> assertEquals(pojo.getJobnummer(), result.getFirst().get("jobnummer")),
                () -> assertTrue(pojo.getProcessTime().isBefore(processTime)),
                () -> assertTrue(tlsTime.isAfter(beforeParse)),
                () -> assertTrue(tlsTime.isBefore(afterParse)),
                () -> assertEquals(0, result.getFirst().get("fehlerursache")),
                () -> assertEquals(42, result.getFirst().get("hersteller")),
                () -> assertEquals(ObjectDirection.IN, result.getFirst().getMetaData().get(ObjectDirection.class.getSimpleName()))
        );
    }

    @Test
    public void testDateTimeFunction_2() throws IOException {
        PWZGStellzustand pojo = PWZGStellzustand.builder()
                .id("specificTime")
                .processTime(Instant.now())
                .jobnummer(9)
                .folgenummer(123)
                .anzeigeprinzip(4)
                .stellcode(13)
                .funktionsbyte(1)
                .textzeichen("Test")
                .build();

        WZGStellzustand messageV3 = PWZGStellzustand.to(pojo);

        GenericProtoObject gpo = new GenericProtoObject(messageV3.getClass().getName(), messageV3, new HashMap<>());

        List<GenericProtoObject> result = List.copyOf(parser.parse("WZGStellzustandJob", gpo, false));

        // convert time objects from result to instants for better comparison
        Instant tlsTime = Instant.ofEpochSecond(result.getFirst().getTimestampValue("tlsTime").getSeconds(), result.getFirst().getTimestampValue("tlsTime").getNanos());
        Instant processTime = Instant.ofEpochSecond(result.getFirst().getTimestampValue("processTime").getSeconds(), result.getFirst().getTimestampValue("processTime").getNanos());

        // assert on received object
        assertAll("Object content for test case dateTime function with a specific time creation",
                () -> assertEquals(1, result.size()),
                () -> assertEquals("WZGDeFehler", result.getFirst().getClassName()),
                () -> assertEquals(pojo.getId(), result.getFirst().get("id")),
                () -> assertEquals(pojo.getJobnummer(), result.getFirst().get("jobnummer")),
                () -> assertTrue(pojo.getProcessTime().isBefore(processTime)),
                () -> assertEquals(Instant.parse("2024-11-07T10:00:00Z"), tlsTime),
                () -> assertEquals(10, result.getFirst().get("folgenummer")),
                () -> assertEquals(1, result.getFirst().get("fehlercode")),
                () -> assertEquals(42, result.getFirst().get("hersteller")),
                () -> assertEquals(ObjectDirection.IN, result.getFirst().getMetaData().get(ObjectDirection.class.getSimpleName()))
        );
    }

    @Test
    public void testDateTimeFunction_3() throws IOException, InterruptedException {
        PWZGStellzustand pojo = PWZGStellzustand.builder()
                .id("wrongTimeFormat")
                .processTime(Instant.now())
                .jobnummer(9)
                .folgenummer(123)
                .anzeigeprinzip(4)
                .stellcode(13)
                .funktionsbyte(1)
                .textzeichen("Test")
                .build();

        WZGStellzustand messageV3 = PWZGStellzustand.to(pojo);

        GenericProtoObject gpo = new GenericProtoObject(messageV3.getClass().getName(), messageV3, new HashMap<>());

        Instant beforeParse = Instant.now();
        Thread.sleep(100L); // wait to be sure that we have a time difference
        List<GenericProtoObject> result = List.copyOf(parser.parse("WZGStellzustandJob", gpo, false));
        Thread.sleep(100L); // wait to be sure that we have a time difference
        Instant afterParse = Instant.now();

        // convert time objects from result to instants for better comparison
        Instant tlsTime = Instant.ofEpochSecond(result.getFirst().getTimestampValue("tlsTime").getSeconds(), result.getFirst().getTimestampValue("tlsTime").getNanos());
        Instant processTime = Instant.ofEpochSecond(result.getFirst().getTimestampValue("processTime").getSeconds(), result.getFirst().getTimestampValue("processTime").getNanos());

        // assert on received object
        assertAll("Object content for test case dateTime function with a specific time creation",
                () -> assertEquals(1, result.size()),
                () -> assertEquals("WZGDeFehler", result.getFirst().getClassName()),
                () -> assertEquals(pojo.getId(), result.getFirst().get("id")),
                () -> assertEquals(pojo.getJobnummer(), result.getFirst().get("jobnummer")),
                () -> assertTrue(pojo.getProcessTime().isBefore(processTime)),
                () -> assertTrue(tlsTime.isAfter(beforeParse)),
                () -> assertTrue(tlsTime.isBefore(afterParse)),
                () -> assertEquals(10, result.getFirst().get("folgenummer")),
                () -> assertEquals(1, result.getFirst().get("fehlercode")),
                () -> assertEquals(42, result.getFirst().get("hersteller")),
                () -> assertEquals(ObjectDirection.IN, result.getFirst().getMetaData().get(ObjectDirection.class.getSimpleName()))
        );
    }

    @Test
    public void testDateTimeFunction_4() throws IOException, InterruptedException {
        PWZGStellzustand pojo = PWZGStellzustand.builder()
                .id("wrongParameterCount")
                .processTime(Instant.now())
                .jobnummer(9)
                .folgenummer(123)
                .anzeigeprinzip(4)
                .stellcode(13)
                .funktionsbyte(1)
                .textzeichen("Test")
                .build();

        WZGStellzustand messageV3 = PWZGStellzustand.to(pojo);

        GenericProtoObject gpo = new GenericProtoObject(messageV3.getClass().getName(), messageV3, new HashMap<>());

        Instant beforeParse = Instant.now();
        Thread.sleep(100L); // wait to be sure that we have a time difference
        List<GenericProtoObject> result = List.copyOf(parser.parse("WZGStellzustandJob", gpo, false));
        Thread.sleep(100L); // wait to be sure that we have a time difference
        Instant afterParse = Instant.now();

        // convert time objects from result to instants for better comparison
        Instant tlsTime = Instant.ofEpochSecond(result.getFirst().getTimestampValue("tlsTime").getSeconds(), result.getFirst().getTimestampValue("tlsTime").getNanos());
        Instant processTime = Instant.ofEpochSecond(result.getFirst().getTimestampValue("processTime").getSeconds(), result.getFirst().getTimestampValue("processTime").getNanos());

        // assert on received object
        assertAll("Object content for test case dateTime function with a specific time creation",
                () -> assertEquals(1, result.size()),
                () -> assertEquals("WZGDeFehler", result.getFirst().getClassName()),
                () -> assertEquals(pojo.getId(), result.getFirst().get("id")),
                () -> assertEquals(pojo.getJobnummer(), result.getFirst().get("jobnummer")),
                () -> assertTrue(pojo.getProcessTime().isBefore(processTime)),
                () -> assertTrue(tlsTime.isAfter(beforeParse)),
                () -> assertTrue(tlsTime.isBefore(afterParse)),
                () -> assertEquals(10, result.getFirst().get("folgenummer")),
                () -> assertEquals(1, result.getFirst().get("fehlercode")),
                () -> assertEquals(42, result.getFirst().get("hersteller")),
                () -> assertEquals(ObjectDirection.IN, result.getFirst().getMetaData().get(ObjectDirection.class.getSimpleName()))
        );
    }

    @Test
    public void testDateTimeFunction_5() throws IOException, InterruptedException {
        PWZGStellzustand pojo = PWZGStellzustand.builder()
                .id("copy")
                .processTime(Instant.now())
                .jobnummer(9)
                .folgenummer(123)
                .anzeigeprinzip(4)
                .stellcode(13)
                .funktionsbyte(1)
                .textzeichen("Test")
                .build();

        WZGStellzustand messageV3 = PWZGStellzustand.to(pojo);

        GenericProtoObject gpo = new GenericProtoObject(messageV3.getClass().getName(), messageV3, new HashMap<>());

        Instant beforeParse = Instant.now();
        Thread.sleep(100L); // wait to be sure that we have a time difference
        List<GenericProtoObject> result = List.copyOf(parser.parse("WZGStellzustandJob", gpo, false));
        Thread.sleep(100L); // wait to be sure that we have a time difference
        Instant afterParse = Instant.now();

        // convert time objects from result to instants for better comparison
        Instant tlsTime = Instant.ofEpochSecond(result.getFirst().getTimestampValue("tlsTime").getSeconds(), result.getFirst().getTimestampValue("tlsTime").getNanos());
        Instant processTime = Instant.ofEpochSecond(result.getFirst().getTimestampValue("processTime").getSeconds(), result.getFirst().getTimestampValue("processTime").getNanos());

        // assert on received object
        assertAll("Object content for test case dateTime function with a specific time creation",
                () -> assertEquals(1, result.size()),
                () -> assertEquals("WZGStellzustand", result.getFirst().getClassName()),
                () -> assertEquals(pojo.getId(), result.getFirst().get("id")),
                () -> assertEquals(pojo.getJobnummer(), result.getFirst().get("jobnummer")),
                () -> assertTrue(pojo.getProcessTime().isBefore(processTime)),
                () -> assertTrue(tlsTime.isAfter(beforeParse)),
                () -> assertTrue(tlsTime.isBefore(afterParse)),
                () -> assertEquals(pojo.getFolgenummer(), result.getFirst().get("folgenummer")),
                () -> assertEquals(pojo.getAnzeigeprinzip(), result.getFirst().get("anzeigeprinzip")),
                () -> assertEquals(pojo.getStellcode(), result.getFirst().get("stellcode")),
                () -> assertEquals(pojo.getFunktionsbyte(), result.getFirst().get("funktionsbyte")),
                () -> assertEquals(pojo.getTextzeichen(), result.getFirst().get("textzeichen")),
                () -> assertEquals(ObjectDirection.IN, result.getFirst().getMetaData().get(ObjectDirection.class.getSimpleName()))
        );
    }

    @Test
    public void testDateTimeFunction_6() throws IOException {
        Instant currentTime = Instant.now();
        PWZGStellzustand pojo = PWZGStellzustand.builder()
                .id("copy")
                .processTime(currentTime)
                .tlsTime(currentTime)
                .jobnummer(9)
                .folgenummer(123)
                .anzeigeprinzip(4)
                .stellcode(13)
                .funktionsbyte(1)
                .textzeichen("Test")
                .build();

        WZGStellzustand messageV3 = PWZGStellzustand.to(pojo);

        GenericProtoObject gpo = new GenericProtoObject(messageV3.getClass().getName(), messageV3, new HashMap<>());

        List<GenericProtoObject> result = List.copyOf(parser.parse("WZGStellzustandJob", gpo, false));

        // convert time objects from result to instants for better comparison
        Instant tlsTime = Instant.ofEpochSecond(result.getFirst().getTimestampValue("tlsTime").getSeconds(), result.getFirst().getTimestampValue("tlsTime").getNanos());
        Instant processTime = Instant.ofEpochSecond(result.getFirst().getTimestampValue("processTime").getSeconds(), result.getFirst().getTimestampValue("processTime").getNanos());

        // assert on received object
        assertAll("Object content for test case dateTime function with a specific time creation",
                () -> assertEquals(1, result.size()),
                () -> assertEquals("WZGStellzustand", result.getFirst().getClassName()),
                () -> assertEquals(pojo.getId(), result.getFirst().get("id")),
                () -> assertEquals(pojo.getJobnummer(), result.getFirst().get("jobnummer")),
                () -> assertTrue(pojo.getProcessTime().isBefore(processTime)),
                () -> assertEquals(tlsTime, pojo.getTlsTime()),
                () -> assertEquals(pojo.getFolgenummer(), result.getFirst().get("folgenummer")),
                () -> assertEquals(pojo.getAnzeigeprinzip(), result.getFirst().get("anzeigeprinzip")),
                () -> assertEquals(pojo.getStellcode(), result.getFirst().get("stellcode")),
                () -> assertEquals(pojo.getFunktionsbyte(), result.getFirst().get("funktionsbyte")),
                () -> assertEquals(pojo.getTextzeichen(), result.getFirst().get("textzeichen")),
                () -> assertEquals(ObjectDirection.IN, result.getFirst().getMetaData().get(ObjectDirection.class.getSimpleName()))
        );
    }
    
    /**
     * Tests array element access in scripts by verifying that:
     * <ul>
     *   <li>Array elements can be accessed by index in script execution</li>
     *   <li>The parser correctly processes LVEErgebnisVersion4 objects with array fields</li>
     *   <li>Script logic can extract specific array elements (vKlassenPkwAeList[0]) and use them in field assignments</li>
     *   <li>The original array data is preserved and copied to the output object (vKlassenLkwAe)</li>
     *   <li>Two output objects are generated: LVEDeFehler with the extracted fehlercode and LVEErgebnisVersion4 with the array data</li>
     * </ul>
     * This test validates the ArrayAccessVariable and ArrayVariable functionality for accessing list elements within grammar scripts.
     */
    @Test
    void testArrayElementAccessInScript() throws IOException {
        
        PLVEErgebnisVersion4 pojo = PLVEErgebnisVersion4.builder()
                 .id("KRI_PPP_EB_2C")
                 .tlsTime(Instant.now())
                 .processTime(Instant.now())
                 .intervallArt(1)
                 .vKlassenPkwAeList( List.of( FEHLERCODE_1_TESTARRAYELEMENTACCESSINSCRIPT, 2, 3) )
                 .build();
        
        LVEErgebnisVersion4 messageV3 = PLVEErgebnisVersion4.to(pojo);
        
        GenericProtoObject gpo = new GenericProtoObject(messageV3.getClass().getName(), messageV3, Collections.emptyMap());
        
        List<GenericProtoObject> result = List.copyOf(parser.parse("LVEErgebnisVersion4", gpo, false));
        Set<String> resultClasses = result.stream().map(GenericProtoObject::getClassName).collect(Collectors.toSet());
        
        // BEWARE: order of results is NOT stable
        GenericProtoObject relevantResult = result.stream()
                 .filter( e -> e.getClassName().equals( "LVEDeFehler" ) )
                 .findFirst()
                 .orElseThrow( () -> new AssertionError( "LVEDeFehler not found in result list" ) );
        
        log.info( "Relevant Result Type: {} of {}", relevantResult.getClassName(), resultClasses );
        
        // asserts on the received object of LVEDeFehler
        assertAll("Object content for testArrayElementAccessInScript",
                 () -> assertEquals( 2, result.size())
                 , () -> assertEquals(2, resultClasses.size() )
                 , () -> assertTrue(resultClasses.contains("LVEErgebnisVersion4"))
                 , () -> assertTrue(resultClasses.contains("LVEDeFehler"))
                 , () -> assertEquals("LVEDeFehler", relevantResult.getClassName(), "LVEDeFehler should be the first " +
                                                                                    "element in the result list. It " +
                                                                                    "is: " + relevantResult.getClassName()  )
                 , () -> assertEquals( 6, relevantResult.get("hersteller") ) // from skript
                 , () -> assertEquals( FEHLERCODE_1_TESTARRAYELEMENTACCESSINSCRIPT, relevantResult.get("fehlercode") )
        );
        
        GenericProtoObject o = (relevantResult == result.get( 0 ) ? result.get( 1 ) : result.get( 0 ));
        Object oo = o.get( "vKlassenLkwAe" );

        // asserts on the received object of LVEErgebnisVersion4
        assertAll("Object content for testArrayElementAccessInScript part 2",
                 () -> assertEquals( "LVEErgebnisVersion4", o.getClassName() )
                 , () -> assertTrue( oo instanceof List<?> )
                 , () -> assertEquals(3, ((List<Integer>) oo).size() )
                 , () -> assertTrue( ((List<Integer>) oo).contains( FEHLERCODE_1_TESTARRAYELEMENTACCESSINSCRIPT ) )
                 , () -> assertTrue( ((List<Integer>) oo).contains( 2 ) )
                 , () -> assertTrue( ((List<Integer>) oo).contains( 3 ) )
        );
        log.debug( "done test" );
    }
}
