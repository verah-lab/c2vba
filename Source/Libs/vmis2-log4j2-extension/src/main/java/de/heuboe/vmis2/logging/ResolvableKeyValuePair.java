package de.heuboe.vmis2.logging;

import static java.util.Objects.requireNonNull;

import org.apache.logging.log4j.core.util.KeyValuePair;

/**
 * Helper class that stores whether the value needs extra lookups to improve performance.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
final class ResolvableKeyValuePair {

    final String key;
    final String value;
    final boolean valueIsEventMessage;
    final boolean valueNeedsLookup;

    public ResolvableKeyValuePair(final KeyValuePair pair) {
        this.key = requireNonNull(pair.getKey(), "pair.key");
        this.value = requireNonNull(pair.getValue(), "pair.value");
        this.valueIsEventMessage = this.value.toLowerCase().contains("${event:message}");
        this.valueNeedsLookup = !this.valueIsEventMessage && this.value.contains("${");
    }

}
