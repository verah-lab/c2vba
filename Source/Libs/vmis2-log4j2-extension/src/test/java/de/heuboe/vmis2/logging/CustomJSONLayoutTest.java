package de.heuboe.vmis2.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.builder.impl.DefaultConfigurationBuilder;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.util.KeyValuePair;
import org.apache.logging.log4j.message.StringFormattedMessage;
import org.junit.jupiter.api.Test;

/**
 * {@link CustomJSONLayout} related unit tests.
 */
class CustomJSONLayoutTest {

    @Test
    void eventMessageLookup() {

        // given
        @SuppressWarnings("rawtypes")
        Configuration config = new DefaultConfigurationBuilder().build();
        KeyValuePair[] outputPairs = {new KeyValuePair("message", "${event:Message}")};
        CustomJSONLayout layout =
                CustomJSONLayout.createLayout(config, false, false, false, outputPairs);

        String msgTextWithLookup = "message text with a date lookup: ${date:MM-dd-yyyy}";
        final LogEvent event = new Log4jLogEvent(
                "LoggerName",
                null,
                "LoggerFQCN",
                null,
                Level.WARN,
                new StringFormattedMessage("%s", msgTextWithLookup),
                new ArrayList<>(),
                null);

        // when
        String layoutedMessage = layout.toSerializable(event);

        // then - the serialized JSON text must still contain the '${' lookup placeholder
        // because it is not expected that a lookup in a message text is resolved
        assertTrue(layoutedMessage.contains(msgTextWithLookup),
                "JSON must still contain lookup placeholder.");

    }

    @Test
    void dateLookup() {

        // given
        @SuppressWarnings("rawtypes")
        Configuration config = new DefaultConfigurationBuilder().build();
        KeyValuePair[] outputPairs = {new KeyValuePair("date", "current date: ${date:MM-dd-yyyy}")};
        CustomJSONLayout layout =
                CustomJSONLayout.createLayout(config, false, false, false, outputPairs);

        final LogEvent event = new Log4jLogEvent(
                "LoggerName",
                null,
                "LoggerFQCN",
                null,
                Level.WARN,
                null,
                new ArrayList<>(),
                null);

        // when
        String layoutedMessage = layout.toSerializable(event);

        // then - the serialized JSON text must not contain any placeholder because any
        // not ${event:Message} placeholder is expected to be resolved
        assertFalse(layoutedMessage.contains("${"),
                "JSON must not contain date lookup placeholder.");
        assertTrue(layoutedMessage.contains("current date: "),
                "JSON must contain date element with resolved placeholder");

    }

    @Test
    void fixKeyValueWithNoLookup() {

        // given
        @SuppressWarnings("rawtypes")
        Configuration config = new DefaultConfigurationBuilder().build();
        KeyValuePair fixText = new KeyValuePair("fixText", "this text is fix");
        KeyValuePair[] outputPairs = {fixText};
        CustomJSONLayout layout =
                CustomJSONLayout.createLayout(config, false, false, false, outputPairs);

        // when
        String layoutedMessage = layout.toSerializable(null);

        // then
        assertTrue(layoutedMessage.contains("\"" + fixText.getKey()
                + "\":\"" + fixText.getValue() + "\""),
                "JSON must contain 'fixText' element");

    }

    @Test
    void recursiveLookup() {

        // given
        @SuppressWarnings("rawtypes")
        Configuration config = new DefaultConfigurationBuilder().build();

        // first: test with flat lookup
        String lookupText = "${lower:WORLD}";
        KeyValuePair lookup = new KeyValuePair("lookup", "Hello " + lookupText);
        KeyValuePair[] outputPairs = {lookup};
        CustomJSONLayout layout =
                CustomJSONLayout.createLayout(config, false, false, false, outputPairs);
        // when
        String layoutedMessage = layout.toSerializable(null);
        // then
        assertEquals("{\"lookup\":\"Hello world\"}\n", layoutedMessage);

        // second: given nested lookup inside a System property
        // this should not be recursively looked up
        System.setProperty("nestedLookup", lookupText);
        KeyValuePair nestedlookup = new KeyValuePair("lookup", "Hello ${sys:nestedLookup}");
        KeyValuePair[] nestedOutputPairs = {nestedlookup};
        CustomJSONLayout nestedLayout =
                CustomJSONLayout.createLayout(config, false, false, false, nestedOutputPairs);

        // when
        String nestedLayoutedMessage = nestedLayout.toSerializable(null);
        // then
        assertEquals("{\"lookup\":\"Hello ${lower:WORLD}\"}\n", nestedLayoutedMessage);
    }

}
