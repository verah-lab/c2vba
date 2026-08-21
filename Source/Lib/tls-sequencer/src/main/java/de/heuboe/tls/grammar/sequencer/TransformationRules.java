package de.heuboe.tls.grammar.sequencer;

import de.heuboe.tls.grammar.interfaces.sequencer.BlockDefinition;
import de.heuboe.tls.grammar.interfaces.sequencer.DefinitionRules;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.*;

/**
 * This class is used to store all TLS analysis descriptions of a given context.
 *
 * @author Ronald Nikel
 */
public class TransformationRules implements DefinitionRules {

    private final Map<String, BlockDefinition> definitions = new LinkedHashMap<>();

    @Override
    public BlockDefinition getDefinition(String definitionName) {
        return definitions.get(definitionName);
    }

    @Override
    public void addDefinition(BlockDefinition def, ParserRuleContext ctx) {
        definitions.put(def.getName(), def);
    }

    @Override
    public List<String> getDefinitionNames() {
        Set<String> ks = definitions.keySet();
        return new ArrayList<>(ks);
    }
}
