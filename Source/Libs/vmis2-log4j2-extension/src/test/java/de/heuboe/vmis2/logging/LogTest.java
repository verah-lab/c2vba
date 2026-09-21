package de.heuboe.vmis2.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.lookup.EventLookup;
import org.apache.logging.log4j.core.lookup.Interpolator;
import org.apache.logging.log4j.core.lookup.StrLookup;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class LogTest {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private static Throwable testException = newTestException();

    private static final CustomJSONLayout layout =
            new CustomJSONLayout(newConfiguration(), true, false, false, null);

    private static DefaultConfiguration newConfiguration() {
        final DefaultConfiguration config = new DefaultConfiguration();
        final Interpolator interpolator = new Interpolator();
        addLookup(interpolator, new EventLookup()); // Provided by log4j2-core via plugins
        addLookup(interpolator, new LogEventPlusLookup()); // our own
        config.getStrSubstitutor().setVariableResolver(interpolator);
        return config;
    }

    private static StrLookup addLookup(final Interpolator interpolator, final StrLookup lookup) {
        return interpolator.getStrLookupMap().put(lookup.getClass().getAnnotation(Plugin.class).name(), lookup);
    }

    private static RuntimeException newTestException() {
        return new RuntimeException("Test-Exception");
    }

    @BeforeAll
    static void setup() {
        // Generate a test exception with a short stacktrace that doesn't clutter the build logs that much
        final Thread thread = new Thread(() -> testException = newTestException(), "generate-Test-Exception");
        thread.start();
        try {
            thread.join();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to generate test exception!", e);
        }
    }

    @Test
    // Check the logs!
    public void testSimpleLogs() {
        LOGGER.trace("test");
        LOGGER.debug("test");
        LOGGER.info("test");
        LOGGER.warn("test");
        LOGGER.error("test");
        LOGGER.fatal("test");
        assertTrue(true);
    }

    @Test
    // Check the logs!
    public void testExceptions() {
        LOGGER.trace("test", testException);
        LOGGER.debug("test", testException);
        LOGGER.info("test", testException);
        LOGGER.warn("test", testException);
        LOGGER.error("test", testException);
        LOGGER.fatal("test", testException);
        assertTrue(true);
    }

    @Test
    public void testToSerializable() throws IOException {
        final String logLine = layout.toSerializable(createLogEvent());
        LOGGER.info("Pretty JsonLog:\n{}", logLine);
        assertNotNull(logLine);
        assertTrue(!logLine.trim().isEmpty(), "logline is empty or blank");

        // Derive the timestamp from the actual log line
        // This also ensures that the log line is valid JSON
        final String time = MAPPER.readValue(logLine, Map.class).get("time").toString();

        final String expectedLine = "{\n" +
                "  \"applicationName\" : \"N/A\",\n" +
                "  \"applicationInstance\" : \"N/A\",\n" +
                "  \"time\" : \"" + time + "\",\n" +
                "  \"level\" : \"INFO\",\n" +
                "  \"thread\" : \"main\",\n" +
                "  \"logger\" : \"test\",\n" +
                "  \"source\" : \"?:?\",\n" +
                "  \"message\" : \"Tada!\",\n" +
                "  \"call\" : \"?#?\",\n" +
                "  \"mdc\" : \"{}\",\n" +
                "  \"exception\" : \"\",\n" +
                "  \"callstack\" : \"\"\n" +
                "}\n";

        assertEquals(expectedLine, logLine.replace("\r", ""));
    }

    private Log4jLogEvent createLogEvent() {
        return new Log4jLogEvent("test", null, "test", Level.INFO, new SimpleMessage("Tada!"),
                Collections.emptyList(), null);
    }

}
