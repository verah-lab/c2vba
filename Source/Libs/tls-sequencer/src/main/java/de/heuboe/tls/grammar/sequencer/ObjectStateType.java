package de.heuboe.tls.grammar.sequencer;

/**
 * This enum defines the state types of an object that can be used within a sequencer script.
 */
public enum ObjectStateType {
    NEW("new"),
    OLD("old");

    private String keyWord;

    ObjectStateType(String keyWord) {
        this.keyWord = keyWord;
    }

    /**
     * Get a {@link ObjectStateType} that match the passed keyWord.
     *
     * @param keyWord The keyWord that should be matched to an {@link ObjectStateType}.
     * @return the matched {@link ObjectStateType}. Default value will be OLD.
     */
    public static ObjectStateType findByKeyWord(String keyWord) {
        if (keyWord == null) {
            return OLD;
        }
        for (ObjectStateType f : values()) {
            if (f.keyWord.equalsIgnoreCase(keyWord)) {
                return f;
            }
        }
        return OLD;
    }
}
