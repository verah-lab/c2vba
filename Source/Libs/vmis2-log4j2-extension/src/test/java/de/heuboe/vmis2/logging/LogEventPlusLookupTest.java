/**
 *
 */
package de.heuboe.vmis2.logging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.StringFormattedMessage;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LogEventPlusLookup}.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
class LogEventPlusLookupTest {

    /**
     * Tests for {@link LogEventPlusLookup#lookup(LogEvent, String)}.
     */
    @Test
    void testLookup() {
        final LogEventPlusLookup lookup = new LogEventPlusLookup();
        final RuntimeException ex = new RuntimeException();
        // Event without source
        final LogEvent event = new Log4jLogEvent(
                "LoggerName",
                null,
                "LoggerFQCN",
                null,
                Level.WARN,
                new StringFormattedMessage("Message", ex),
                new ArrayList<>(),
                ex);

        // Event with source
        final RuntimeException ex2 = new RuntimeException("TestEx");
        final StackTraceElement ste2 = ex2.getStackTrace()[0];
        final LogEvent event2 = new Log4jLogEvent(
                "LoggerName",
                null,
                "LoggerFQCN",
                ste2,
                Level.WARN,
                new StringFormattedMessage("Message", ex2),
                new ArrayList<>(),
                ex2);

        // Event with class without package
        final RuntimeException ex3 = new RuntimeException("");
        final StackTraceElement ste3 = new StackTraceElement("Class", "method", "File.java", 1337);
        final LogEvent event3 = new Log4jLogEvent(
                "LoggerName",
                null,
                "LoggerFQCN",
                ste3,
                Level.WARN,
                new StringFormattedMessage("Message", ex3),
                new ArrayList<>(),
                ex3);

        assertThat(lookup.lookup(event, null)).isNullOrEmpty();
        assertThat(lookup.lookup(event, "time"))
                .matches("^202\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d(.\\d+)?Z$");
        assertThat(lookup.lookup(event, "mdc"))
                .isEqualTo("{}");
        assertThat(lookup.lookup(event, "exception"))
                .isEqualTo("RuntimeException: No error message available");
        assertThat(lookup.lookup(event, "stacktrace"))
                .contains("RuntimeException")
                .contains("LogEventPlusLookupTest.testLookup");
        assertThat(lookup.lookup(event, "simpleClass"))
                .isEqualTo("?");
        assertThat(lookup.lookup(event, "class"))
                .isEqualTo("?");
        assertThat(lookup.lookup(event, "method"))
                .isEqualTo("?");
        assertThat(lookup.lookup(event, "file"))
                .isEqualTo("?");
        assertThat(lookup.lookup(event, "line"))
                .isEqualTo("?");
        assertThat(lookup.lookup(event, "default"))
                .isEqualTo("Unknown: default");

        assertThat(lookup.lookup(event2, "exception"))
                .isEqualTo("TestEx");
        assertThat(lookup.lookup(event2, "stacktrace"))
                .contains("RuntimeException")
                .contains("TestEx")
                .contains("LogEventPlusLookupTest.testLookup");
        assertThat(lookup.lookup(event2, "simpleClass"))
                .isEqualTo("LogEventPlusLookupTest");
        assertThat(lookup.lookup(event2, "class"))
                .isEqualTo("de.heuboe.vmis2.logging.LogEventPlusLookupTest");
        assertThat(lookup.lookup(event2, "method"))
                .isEqualTo("testLookup");
        assertThat(lookup.lookup(event2, "file"))
                .startsWith("LogEventPlusLookupTest")
                .isEqualTo(ste2.getFileName());
        assertThat(lookup.lookup(event2, "line"))
                .isEqualTo(Integer.toString(ste2.getLineNumber()));

        assertThat(lookup.lookup(event3, "exception"))
                .isEqualTo("RuntimeException: No error message available");
        assertThat(lookup.lookup(event3, "stacktrace"))
                .contains("RuntimeException")
                .contains("LogEventPlusLookupTest.testLookup");
        assertThat(lookup.lookup(event3, "simpleClass"))
                .isEqualTo("Class");
        assertThat(lookup.lookup(event3, "class"))
                .isEqualTo("Class");
        assertThat(lookup.lookup(event3, "method"))
                .isEqualTo("method");
        assertThat(lookup.lookup(event3, "file"))
                .startsWith("File.java");
        assertThat(lookup.lookup(event3, "line"))
                .isEqualTo("1337");
    }

}
