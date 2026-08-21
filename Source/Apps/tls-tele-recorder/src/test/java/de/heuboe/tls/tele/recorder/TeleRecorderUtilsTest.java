//package de.heuboe.tls.tele.recorder;
//
//import com.google.protobuf.ByteString;
//import com.google.protobuf.Timestamp;
//import de.heuboe.tls.tel.io.TeleSReceived;
//import de.heuboe.tls.tele.recorder.config.TeleRecorderProperties;
//import de.heuboe.tls.tele.recorder.config.TeleRecorderUtilsTestConfig;
//import de.heuboe.tls.tele.recorder.utils.TeleRecorderUtils;
//import de.heuboe.tls.tlstele.TlsTele;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.TestPropertySource;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.sql.Time;
//import java.util.Collections;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@EnableAutoConfiguration
//@EnableConfigurationProperties
//@ContextConfiguration(classes = {TeleRecorderProperties.class})
//@TestPropertySource(locations = "classpath:application.properties")
//@Import({TeleRecorderUtilsTestConfig.class})
//@SpringBootTest
//public class TeleRecorderUtilsTest {
//
//    @Autowired
//    TeleRecorderUtils teleRecorderUtils;
//
//    @Autowired
//    TeleRecorderProperties properties;
//
//    @Test
//    @DisplayName("Creating TlsTele with NULL")
//    public void createTelegramNullTest() {
//        TlsTele tele = teleRecorderUtils.createTelegram(null, properties.getReceiveTopic());
//        // assert on received object
//        assertAll("Created TlsTele",
//                () -> assertNull(tele)
//        );
//    }
//
//    @Test
//    @DisplayName("Creating TlsTele with content")
//    public void createTelegramContentTest() throws IOException {
//        TlsTele tel = TlsTele.loadJs( new File( "src/test/resources/testLoadTele.json" ) );
//        TlsTele tele = teleRecorderUtils.createTelegram(tel.getBytes(), properties.getReceiveTopic());
//        // assert on received object
//        assertAll("Created TlsTele",
//                () -> assertNull(tele)
//        );
//    }
//
//    @Test
//    @DisplayName("Getting telegram list with content")
//    public void getTelegramListWithContentTest() {
//        // get absolute path to current telegram folder in test resources
//        properties.setAbsolutLogPath(new File("src\\test\\resources").getAbsolutePath() + "\\telegrams");
//
//        List<String> list = teleRecorderUtils.getTelegramList();
//        // assert on received object
//        assertAll("Created TlsTele",
//                () -> assertNotNull(list),
//                () -> assertEquals(1, list.size()),
//                () -> assertEquals(properties.getAbsolutLogPath() + "\\1_1_1_LVEDeFehler.lts", list.get(0))
//        );
//    }
//
//    @Test
//    @DisplayName("Getting telegram list without content")
//    public void getTelegramListWithoutContentTest() {
//        // get absolute path to current telegram folder in test resources
//        properties.setAbsolutLogPath(new File("src\\test\\resources").getAbsolutePath() + "\\telegrams\\blub");
//
//        List<String> list = teleRecorderUtils.getTelegramList();
//        // assert on received object
//        assertAll("Created TlsTele",
//                () -> assertEquals(Collections.emptyList(), list)
//        );
//    }
//}
