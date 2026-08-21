package de.heuboe.tls.grammar.sequencer;

import lombok.Getter;

/**
 * This enum defines the EA target types an object that can be used within a sequencer script.
 */
public enum EaTargetType {
    EA("Ea"),
    DE("De"),
    DES_OF_CLUSTER("DEs des Cluster"),
    DES_OF_NODE("DEs des Knoten"),
    DES_OF_KRI("DEs der KRI"),
    CLUSTER_DE("Cluster des DEs"),
    NODE_DE("Knoten des DEs"),
    NODE_KRI("Knoten der KRI");

    @Getter
    private String keyWord;

    EaTargetType(String keyWord) {
        this.keyWord = keyWord;
    }

    /**
     * Get a {@link EaTargetType} that match the passed keyWord.
     *
     * @param keyWord The keyWord that should be matched to an {@link EaTargetType}.
     * @return the matched {@link EaTargetType} or null.
     */
    public static EaTargetType findByKeyWord(String keyWord) {
        for (EaTargetType f : values()) {
            if (f.keyWord.equalsIgnoreCase(keyWord)) {
                return f;
            }
        }
        return null;
    }
}
