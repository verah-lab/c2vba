//package de.heuboe.tls.tele.recorder;
//
//import com.google.common.io.Files;
//import de.heuboe.tls.logtls.replay.cmd.Main;
//import de.heuboe.tls.tele.recorder.actors.SpringExtension;
//import de.heuboe.tls.tele.recorder.config.TeleRecorderIntegrationTestConfig;
//import de.heuboe.tls.tele.recorder.config.TeleRecorderProperties;
//import de.heuboe.tls.tele.recorder.server.TeleRecorderServer;
//import de.heuboe.tls.tele.recorder.services.TeleRecorderService;
//import de.heuboe.tls.tele.recorder.utils.TeleRecorderUtils;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.MethodOrderer;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestMethodOrder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Import;
//import org.springframework.kafka.annotation.EnableKafka;
//import org.springframework.kafka.test.context.EmbeddedKafka;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
//
//import java.io.File;
//import java.io.IOException;
//import java.net.URISyntaxException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//@DirtiesContext
//@EnableKafka
//@EmbeddedKafka(
//        partitions = 1,
//        ports = {50637},
//        brokerProperties = {"log.dir=target/kafka${random.int}"}
//)
//
//@EnableAutoConfiguration
//@EnableConfigurationProperties
//@ContextConfiguration(classes = {TeleRecorderProperties.class})
//@TestPropertySource(properties = {
//        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
//        "spring.kafka.consumer.group-id=EmbeddedKafkaTest",
//        "spring.kafka.listener.missing-topics-fatal=false"},
//        locations = "classpath:application.properties"
//)
//
//@Import({SpringExtension.class, TeleRecorderServer.class, TeleRecorderIntegrationTestConfig.class})
//@ComponentScan(basePackages = {"de.heuboe.tls.kafka.operator"})
//@SpringBootTest(classes = {TeleRecorderService.class})
//@SpringJUnitConfig
//@EnableScheduling
//@Slf4j
//@TestMethodOrder(MethodOrderer.Alphanumeric.class)
//public class TeleRecorderIntegrationTest {
//
//    private static final String INPUT_TELEGRAM = "inputTelegram.lts";
//
//    @Autowired
//    TeleRecorderProperties properties;
//
//    @Autowired
//    TeleRecorderUtils utils;
//
//    /**
//     * Loads a file via
//     * @throws URISyntaxException
//     * @throws IOException
//     */
//    @Test
//    public void integrationTest() throws URISyntaxException, IOException {
//
//        // create File objects for recorded and input binary files
//        File inputTelegramFile = new File(getClass().getClassLoader().getResource(INPUT_TELEGRAM).toURI());
//        String telegramResourcePath = inputTelegramFile.getAbsolutePath().replace(INPUT_TELEGRAM, "");
//
//        // build start arguments for tls-logtls-replay-cmd
//        List<String> argList = new ArrayList<>();
//        argList.add("replayToKafka");
//        argList.add("-topic=TeleSReceived");
//        argList.add("--erfasste-telegramme=true");
//        argList.add("--brief-summary");
//        argList.add("--time-correction=0");
//        argList.add("--time-fake=false");
//        argList.add("--no-repeat=true");
//        argList.add("--info-only=false");
//        argList.add("--sync=false");
//        argList.add("--condensed=true");
//        argList.add("--timezone=Europe/Berlin");
//        argList.add(inputTelegramFile.getAbsolutePath());
//
//        // update absolute telegram path
//        properties.setAbsolutLogPath(telegramResourcePath);
//
//        // start tls-logtls-replay-cmd
//        Main.main(argList.toArray(new String[0]));
//
//        while (true) {
//            List<String> telegramList = utils.getTelegramList();
//            if (telegramList.size() > 1) {
//                // wait some time until the tele-recorder writes all received telegrams into the file
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                break;
//            }
//        }
//
//        try {
//
//            File recordedTestFile = getLastModified(properties.getAbsolutLogPath());
//            log.info("Loading recorded file '{}'", recordedTestFile.getName());
//
//            // transform input telegram and recorded telegram files to byte array for comparison
//            byte[] recordedTestFileByteArray = Files.toByteArray(recordedTestFile);
//            byte[] inputTelegramFileByteArray = Files.toByteArray(inputTelegramFile);
//
//            // reset bytes at position 4, 5, 6 & 7 because the represent the creation timestamp of the telegram
//            recordedTestFileByteArray[4] = 0;
//            recordedTestFileByteArray[5] = 0;
//            recordedTestFileByteArray[6] = 0;
//            recordedTestFileByteArray[7] = 0;
//            inputTelegramFileByteArray[4] = 0;
//            inputTelegramFileByteArray[5] = 0;
//            inputTelegramFileByteArray[6] = 0;
//            inputTelegramFileByteArray[7] = 0;
//
//            assertTrue(Arrays.equals(inputTelegramFileByteArray, recordedTestFileByteArray));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private File getLastModified(String directoryFilePath)
//    {
//        File directory = new File(directoryFilePath);
//        File[] files = directory.listFiles(File::isFile);
//        long lastModifiedTime = Long.MIN_VALUE;
//        File chosenFile = null;
//
//        if (files != null)
//        {
//            for (File file : files)
//            {
//                if (file.lastModified() > lastModifiedTime && file.getName().endsWith(".lts"))
//                {
//                    chosenFile = file;
//                    lastModifiedTime = file.lastModified();
//                }
//            }
//        }
//
//        return chosenFile;
//    }
//}
