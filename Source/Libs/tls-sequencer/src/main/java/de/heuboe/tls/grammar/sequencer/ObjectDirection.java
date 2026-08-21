package de.heuboe.tls.grammar.sequencer;

/**
 * This enum defines the direction of an object that can be used within a sequencer script. The direction defines the
 * prefix of the topic that is associated to the object.
 */
public enum ObjectDirection {
    IN("in"),
    OUT("out");

    private String keyWord;

    ObjectDirection(String keyWord) {
        this.keyWord = keyWord;
    }

    /**
     * Get a {@link ObjectStateType} that match the passed keyWord.
     *
     * @param keyWord The keyWord that should be matched to an {@link ObjectStateType}.
     * @return the matched {@link ObjectStateType}. Default value will be OLD.
     */
    public static ObjectDirection findByKeyWord(String keyWord) {
        if (keyWord == null) {
            return IN;
        }
        for (ObjectDirection f : values()) {
            if (f.keyWord.equalsIgnoreCase(keyWord)) {
                return f;
            }
        }
        return IN;
    }
}
