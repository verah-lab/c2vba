package de.heuboe.tls.grammar.sequencer.flops;

/**
 * This enum defines all possible types of flops.
 */
public enum FlopType {

    EA("Eaweiser"),
    MONO("Einmaliger"),
    NODE("Knotenweiser"),
    CLUSTER("Clusterweiser");

    private String keyWord;

    FlopType(String keyWord) {
        this.keyWord = keyWord;
    }

    /**
     * Get the {@link FlopType} that match the passed keyWord.
     *
     * @param keyWord The keyWord that should be matched to a {@link FlopType}.
     * @return the matched {@link FlopType} or null.
     */
    public static FlopType findByKeyWord(String keyWord) {
        for (FlopType f : values()) {
            if (f.keyWord.equalsIgnoreCase(keyWord)) {
                return f;
            }
        }
        return null;
    }
}
