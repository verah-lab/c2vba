package de.heuboe.tls.sequencer.model;

import de.heuboe.tls.grammar.sequencer.ObjectDirection;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A model class for data type descriptions.
 */
@AllArgsConstructor
@Getter
public class SequencerDataType {

    private final String name;
    private final boolean history;
    private final ObjectDirection direction;
    private final String targetTopic;

}
