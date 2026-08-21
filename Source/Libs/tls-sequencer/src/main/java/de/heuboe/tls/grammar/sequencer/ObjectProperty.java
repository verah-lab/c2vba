package de.heuboe.tls.grammar.sequencer;

import java.util.Arrays;
import java.util.InputMismatchException;

/**
 * This enum defines the possible properties that can be used within a sequencer script.
 */
public enum ObjectProperty {
    SELF("self", Boolean.class),
    NAME("name", String.class),
    AUTO_FILL_TLS_TIME("autoFillTlsTime", Boolean.class);

    private final String keyWord;
    private final Class<?> clazz;

    ObjectProperty(String keyWord, Class<?> clazz) {
        this.keyWord = keyWord;
        this.clazz = clazz;
    }

    /**
     * Get a {@link ObjectProperty} that match the passed keyWord.
     *
     * @param keyWord The keyWord that should be matched to an {@link ObjectProperty}.
     * @return the matched {@link ObjectProperty} or null.
     */
    public static ObjectProperty findByKeyWord(String keyWord) {
        if (keyWord != null) {
            for (ObjectProperty f : values()) {
                if (f.keyWord.equalsIgnoreCase(keyWord)) {
                    return f;
                }
            }
        }
        throw new InputMismatchException("Property '" + keyWord + "' is not supported. Use one of the following " +
                "properties: " + Arrays.stream(values()).map(v -> v.keyWord).toList());
    }

    /**
     * Checks if the string value of an {@link ObjectProperty} matches the defined type class. This can be helpful to
     * match e.g. boolean values.
     *
     * @param option The {@link ObjectProperty} that should be checked.
     * @param value  The string value of the property that should be checked.
     * @return the string value or an alternative if the check fails.
     */
    public static String checkDataType(ObjectProperty option, String value) {
        // map for boolean properties all values that are not 'true' to 'false'
        if ((option.clazz == Boolean.class) && (!Boolean.parseBoolean(value))) {
            return "false";
        }
        return value;
    }
}
