package de.heuboe.tls.grammar.sequencer;

/**
 * This enum defines all possible types of messages.
 */
public enum MessageType {

    ERROR("ErrorMessage"),
    SYSTEM("SystemMessage");

    private String keyWord;

    MessageType(String keyWord) {
        this.keyWord = keyWord;
    }

    /**
     * Get the {@link MessageType} that match the passed keyWord.
     *
     * @param keyWord The keyWord that should be matched to a {@link MessageType}.
     * @return the matched {@link MessageType} or null.
     */
    public static MessageType findByKeyWord(String keyWord) {
        for (MessageType f : values()) {
            if (f.keyWord.equalsIgnoreCase(keyWord)) {
                return f;
            }
        }
        return null;
    }
}
