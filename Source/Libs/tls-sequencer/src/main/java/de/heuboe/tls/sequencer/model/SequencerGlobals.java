package de.heuboe.tls.sequencer.model;

import de.heuboe.tls.grammar.base.BasicVariable;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * A model class for global variables that will be used in scripts.
 */
public class SequencerGlobals {

    @Getter
    private final Map<String, BasicVariable> global = new HashMap<>();

    /**
     * Add a global variable to the local map if an entry with the passed name is not already present.
     *
     * @param name  The name of the global variable.
     * @param value The value of the global variable.
     */
    public void addGlobal(String name, BasicVariable value) {
        global.computeIfAbsent(name, k -> global.put(k, value));
    }

}
