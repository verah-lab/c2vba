package de.heuboe.vmis2.logging;

import java.time.Instant;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.lookup.AbstractLookup;
import org.apache.logging.log4j.core.lookup.StrLookup;

/**
 * An lookup for the events itself. You can use this lookup via <code>$${event-plus:time}</code> where
 * <code>time</code> is the lookup key.
 *
 * <p>
 * Currently the following keys are supported:
 * </p>
 * <ul>
 * <li>time</li>
 * <li>logger</li>
 * <li>message</li>
 * <li>mdc</li>
 * <li>threadId</li>
 * <li>threadName</li>
 * <li>exception</li>
 * <li>stacktrace</li>
 * <li>simpleClass</li>
 * <li>class</li>
 * <li>method</li>
 * <li>file</li>
 * <li>line</li>
 * </ul>
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Plugin(name = "event-plus", category = StrLookup.CATEGORY)
public class LogEventPlusLookup extends AbstractLookup {

    private static final String UNKNOWN = "?";
    private static final String EMPTY = "";

    @Override
    public String lookup(final LogEvent event, final String key) {
        if (key == null) {
            return null;
        }
        // Other lookups use ':' as separator for a "format". Shall we add that as well?
        switch (key) {
            case "time":
                return extractTime(event);
            case "mdc":
                return event.getContextData().toString();
            case "exception":
                return extractException(event);
            case "stacktrace":
                return extractStacktrace(event);
            case "simpleClass":
                return extractSimpleClass(event);
            case "class":
                return extractClass(event);
            case "method":
                return extractMethod(event);
            case "file":
                return extractFile(event);
            case "line":
                return extractLine(event);
            default:
                return "Unknown: " + key;
        }
    }

    private String extractTime(final LogEvent event) {
        final org.apache.logging.log4j.core.time.Instant instant = event.getInstant();
        return Instant.ofEpochSecond(instant.getEpochSecond(), instant.getNanoOfSecond()).toString();
    }

    private String extractException(final LogEvent event) {
        final Throwable t = event.getMessage().getThrowable();
        if (t == null) {
            return EMPTY;
        }
        String message = t.getMessage();
        if (message == null || message.isEmpty()) {
            message = t.getClass().getSimpleName() + ": No error message available";
        }
        return message;
    }

    private String extractStacktrace(final LogEvent event) {
        final ThrowableProxy proxy = event.getThrownProxy();
        if (proxy == null) {
            return EMPTY;
        }
        return proxy.getExtendedStackTraceAsString();
    }

    private String extractSimpleClass(final LogEvent event) {
        final StackTraceElement source = event.getSource();
        if (source == null) {
            return UNKNOWN;
        }
        final String className = source.getClassName();
        final int index = className.lastIndexOf('.');
        if (index > 0) {
            return className.substring(index + 1);
        }
        return className;
    }

    private String extractClass(final LogEvent event) {
        final StackTraceElement source = event.getSource();
        if (source == null) {
            return UNKNOWN;
        }
        return source.getClassName();
    }

    private String extractMethod(final LogEvent event) {
        final StackTraceElement source = event.getSource();
        if (source == null) {
            return UNKNOWN;
        }
        return source.getMethodName();
    }

    private String extractFile(final LogEvent event) {
        final StackTraceElement source = event.getSource();
        if (source == null) {
            return UNKNOWN;
        }
        return source.getFileName();
    }

    private String extractLine(final LogEvent event) {
        final StackTraceElement source = event.getSource();
        if (source == null) {
            return UNKNOWN;
        }
        return Integer.toString(source.getLineNumber());
    }

}
