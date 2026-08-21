package de.heuboe.by.config.reader;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

//@SpringBootTest
@SpringBootTest
@TestPropertySource(properties = {
        "spring.config.location=classpath:application-test.properties"
})
@Slf4j
@Disabled
public class ConfigReaderMainITest {

    @Autowired
    ImportCommand importCommand;
//    @Test
//    public void runIT() {
//        assertEquals(0, ConfigReaderMain.execute("import", importDir));
//    }

    @Test
    public void runTest() {
        log.info("Starting ConfigReaderMainITest -> importCommand with application-test.properties");
        assertEquals(0, importCommand.call());
    }

}
