package de.heuboe.tls.grammar.interfaces.sequencer;

import de.heuboe.tls.grammar.interfaces.Filler;
import de.heuboe.tls.grammar.sequencer.ObjectProperty;

import java.util.EnumMap;
import java.util.List;

/**
 * These objects hold the analysis definition of one block.
 *
 * @author ronald
 */
public interface BlockDefinition {

    /**
     * Get the name of the block data.
     *
     * @return The name of data to transform.
     */
    String getName();

    /**
     * Get the options HashMap of the block data.
     *
     * @return A HashMap that contains options for the current block.
     */
    EnumMap<ObjectProperty, String> getOptions();

    /**
     * Get the sequence of instructions to analyse a DeBlock.
     *
     * @return The sequence of rules that should be applied to a DeBlock.
     */
    List<Filler> getFillerRules();

    /**
     * Add a rule to the definition of one DebBock.
     *
     * @param rule The rule that should be added to the definition.
     */
    void add(Filler rule);

    /**
     * This method should be called, when a definition for one DeBlock is complete.
     */
    void complete();
}
