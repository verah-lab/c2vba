package de.heuboe.vmis2.logging;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.logging.log4j.core.util.KeyValuePair;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * A customizable json layout that allows logging json.
 *
 * <p>
 * You can use it like this (inside the <code>Appenders</code> section of your config):
 * </p>
 *
 * <pre>
 * <code>&lt;File name=&quot;JsonFile&quot; fileName=&quot;logs/app.log&quot;&gt;
 *     &lt;CustomJSONLayout&gt;
 *         &lt;KeyValuePair key="applicationName" value="$${env:application.name:-N/A}" /&gt;
 *         &lt;KeyValuePair key="applicationInstance" value="$${env:application.instance:-N/A}" /&gt;
 *         &lt;KeyValuePair key=&quot;time&quot; value=&quot;$${date:yyyy-MM-dd'T'HH:mm:ss.SSSZ}&quot; /&gt;
 *         &lt;!-- &lt;KeyValuePair key=&quot;time&quot; value=&quot;$${event-plus:time}&quot; /&gt; --&gt;
 *         &lt;KeyValuePair key=&quot;level&quot; value=&quot;$${event:Level}&quot; /&gt;
 *         &lt;KeyValuePair key=&quot;threadName&quot; value=&quot;$${event:ThreadName}&quot; /&gt;
 *         &lt;KeyValuePair key=&quot;logger&quot; value=&quot;$${event:Logger}&quot; /&gt;
 *         &lt;KeyValuePair key=&quot;mdc&quot; value=&quot;$${event-plus:mdc}&quot; /&gt;
 *         &lt;KeyValuePair key=&quot;call&quot; value=&quot;$${event-plus:class}#$${event:method}&quot; /&gt;
 *         &lt;KeyValuePair key=&quot;source&quot; value=&quot;$${event-plus:file}:$${event:line}&quot; /&gt;
 *         &lt;KeyValuePair key=&quot;message&quot; value=&quot;$${event:Message}&quot; /&gt;
 *     &lt;/CustomJSONLayout&gt;
 * &lt;/File&gt;</code>
 * </pre>
 *
 * <p>
 * <b>Note:</b> All <a href="https://logging.apache.org/log4j/2.0/manual/lookups.html">log4j2
 * lookups</a> are supported, not just the custom <code>event</code> one.
 * </p>
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Plugin(name = "CustomJSONLayout", category = "Core", elementType = Layout.ELEMENT_TYPE, printObject = true)
public class CustomJSONLayout extends AbstractStringLayout {

    private static final String EMPTY_STRING = "";
    private static final String DEFAULT_FOOTER = EMPTY_STRING;
    private static final String DEFAULT_HEADER = EMPTY_STRING;
    private static final String DEFAULT_SPLITTER = "\n";
    private static final KeyValuePair[] DEFAULT_OUTPUT_PAIRS = {
            new KeyValuePair("applicationName", "${env:application.name:-N/A}"),
            new KeyValuePair("applicationInstance", "${env:application.instance:-N/A}"),
            new KeyValuePair("time", "${date:yyyy-MM-dd'T'HH:mm:ss.SSSZ}"),
            new KeyValuePair("level", "${event:Level}"),
            new KeyValuePair("thread", "${event:ThreadName}"),
            new KeyValuePair("logger", "${event:Logger}"),
            new KeyValuePair("source", "${event-plus:file}:${event-plus:line}"),
            new KeyValuePair("message", "${event:Message}"),
            new KeyValuePair("call", "${event-plus:class}#${event-plus:method}"),
            new KeyValuePair("mdc", "${event-plus:mdc}"),
            new KeyValuePair("exception", "${event-plus:exception}"),
            new KeyValuePair("callstack", "${event-plus:stacktrace}"),
    };

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final ResolvableKeyValuePair[] fields;
    private final boolean includeContextMap;
    private final boolean includeMarker;

    /**
     * Creates a new json layout using the given properties.
     *
     * @param config The Config to use.
     * @param pretty Whether the json should be pretty printed (default false).
     * @param includeContextMap Whether the ContextMap should be included (default true).
     * @param includeMarker Whether the Makers should be included (default true).
     * @param fields The fields to include in the output or null to use the defaults.
     * @return The newly created json layout.
     *
     * @see PluginFactory
     */
    @PluginFactory
    public static CustomJSONLayout createLayout(
            @PluginConfiguration final Configuration config,
            @PluginAttribute(value = "pretty", defaultBoolean = false) final boolean pretty,
            @PluginAttribute(value = "includeContextMap", defaultBoolean = true) final boolean includeContextMap,
            @PluginAttribute(value = "includeMarker", defaultBoolean = true) final boolean includeMarker,
            @PluginElement("fields") final KeyValuePair[] fields) {
        return new CustomJSONLayout(config, pretty, includeContextMap, includeMarker, fields);
    }

    private static Serializer serializerFrom(final Configuration config, final String pattern) {
        return PatternLayout.newSerializerBuilder().setConfiguration(config).setDefaultPattern(pattern).build();
    }

    protected CustomJSONLayout(final Configuration config, final boolean pretty, final boolean includeContextMap,
            final boolean includeMarker, final KeyValuePair[] fields) {
        super(config,
                StandardCharsets.UTF_8,
                serializerFrom(config, DEFAULT_HEADER),
                serializerFrom(config, DEFAULT_FOOTER));
        requireNonNull(config, "config");
        this.fields = prepareFields(fields);
        if (pretty) {
            this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
        this.includeContextMap = includeContextMap;
        this.includeMarker = includeMarker;
    }

    /**
     * Convert the given fields to a internal class that executes some tasks to improve runtime
     * performance.
     *
     * @param fields The fields to initialize. Null or empty to use the default settings.
     * @return The array containing the key value pairs that desribe the content to be included in the
     *         JSON.
     */
    private static ResolvableKeyValuePair[] prepareFields(KeyValuePair[] fields) {
        if (fields == null || fields.length == 0) {
            fields = DEFAULT_OUTPUT_PAIRS;
        }
        final ResolvableKeyValuePair[] resolvableFields = new ResolvableKeyValuePair[fields.length];
        for (int i = 0; i < fields.length; i++) {
            resolvableFields[i] = new ResolvableKeyValuePair(fields[i]);
        }
        return resolvableFields;
    }

    private Map<String, Object> resolveFields(final LogEvent logEvent) {
        final Map<String, Object> output = new LinkedHashMap<>(this.fields.length);
        // get the StrSubstitutor; this will return a RuntimeStrSubstitutor that does not
        // support recursive evaluation of lookups (CVE-2021-45105)
        final StrSubstitutor strSubstitutor = this.configuration.getStrSubstitutor();

        for (final ResolvableKeyValuePair pair : this.fields) {
            if (pair.valueIsEventMessage) {
                // do not lookup placeholder in event messages (#log4shell / CVE-2021-44228)
                output.put(pair.key, logEvent.getMessage().getFormattedMessage());
            } else if (pair.valueNeedsLookup) {
                output.put(pair.key, Objects.toString(strSubstitutor.replace(logEvent, pair.value), ""));
            } else {
                output.put(pair.key, pair.value);
            }
        }
        if (this.includeContextMap) {
            try {
                output.put("contextMap", this.objectMapper.writeValueAsString(logEvent.getContextData().toMap()));
            } catch (final JsonProcessingException e) {
                // DO NOT LOG HERE!
                output.put("contextMap", "Failed to convert to JSON: " + e.getMessage());
            }
        }
        if (this.includeMarker) {
            try {
                output.put("marker", this.objectMapper.writeValueAsString(logEvent.getMarker()));
            } catch (final JsonProcessingException e) {
                // DO NOT LOG HERE!
                output.put("marker", "Failed to convert to JSON: " + e.getMessage());
            }
        }
        return output;
    }

    @Override
    public String toSerializable(final LogEvent event) {
        try {
            final StringWriter writer = new StringWriter();
            this.objectMapper.writeValue(writer, resolveFields(event));
            writer.write(DEFAULT_SPLITTER);
            return writer.toString();
        } catch (final IOException e) {
            // Logging here would result in a recursion
            return EMPTY_STRING;
        }
    }

}
