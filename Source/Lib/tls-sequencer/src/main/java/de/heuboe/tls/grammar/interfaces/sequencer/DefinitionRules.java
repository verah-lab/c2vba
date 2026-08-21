package de.heuboe.tls.grammar.interfaces.sequencer;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;

/**
 * This interface represents objects that are used to store all TLS analysis descriptions of a given context.
 *
 * @author Ronald Nikel
 */

public interface DefinitionRules {

    /**
     * Retrieve an analysis definition to a matching argument.
     *
     * @param definitionName The name of a definition.
     * @return A DeBlock synthesis description used to build a DeBlock of the name.
     */
    BlockDefinition getDefinition(String definitionName);

    /**
     * Add analysis definition to a global table.
     *
     * @param def The DeBlock analysis description that should be stored.
     * @param ctx A piece of information from the parser describing the current context of parsing.
     */
    void addDefinition(BlockDefinition def, ParserRuleContext ctx);

    /**
     * Retrieve a list of all definition names.
     *
     * @return A list of all definition names.
     */
    List<String> getDefinitionNames();
}
