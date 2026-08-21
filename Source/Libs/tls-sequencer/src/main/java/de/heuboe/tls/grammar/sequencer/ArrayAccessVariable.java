package de.heuboe.tls.grammar.sequencer;

import de.heuboe.tls.grammar.base.BasicVariable;
import de.heuboe.tls.grammar.base.ValueCollection;
import de.heuboe.tls.grammar.interfaces.Value;
import de.heuboe.tls.grammar.interfaces.Variable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * This class represents an array variable with access index and its property.
 */
@Slf4j
public class ArrayAccessVariable extends BasicVariable {

    @Getter
    @Setter
    private String index;

    /**
     * Constructs an {@link ArrayAccessVariable}.
     *
     * @param name  The name of the array property.
     * @param index The index for the array access as string.
     */
    public ArrayAccessVariable(String name, String index) {
        super(name, null);
        if (index == null) {
            throw new NullPointerException("Index variable '" + name + "' is null!");
        }
        this.index = index;
    }

    @Override
    public Value eval(Object dataFromBroker, Map<String, Variable> variableTable) {
        if (variableTable.get(super.getName()).getClassName().equals(ArrayVariable.class.getSimpleName())) {
            try {
                // simply add the index value as string to the global variable map
                variableTable.put(ArrayVariable.INDEX,
                        new BasicVariable(ArrayVariable.INDEX, new ValueCollection.StringValue(index)));
            } catch (NullPointerException npe) {
                log.error("Failed to determine an integer value from the index variable '{}'!", index);
                return null;
            }
            // get the real value for the index from the array
            return variableTable.get(super.getName()).eval(dataFromBroker, variableTable);
        }
        return null;
    }
}
