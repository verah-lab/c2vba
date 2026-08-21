package de.heuboe.tls.grammar.sequencer;

import de.heuboe.tls.grammar.base.BasicVariable;
import de.heuboe.tls.grammar.interfaces.Value;
import de.heuboe.tls.grammar.interfaces.Variable;

import java.util.Map;

/**
 * This is just another representation of the {@link BasicVariable} that determines that this variable should be write
 * protected.
 */
public class ProtectedBasicVariable extends BasicVariable {

    /**
     * The constructor that creates a BasicVariable with the required name and value of the variable.
     *
     * @param name  The name of the variable.
     * @param value The value of the variable.
     */
    public ProtectedBasicVariable(String name, Value value) {
        super(name, value);
    }

    @Override
    public Value eval(Object dataFromBroker, Map<String, Variable> variableTable) {
        return super.getValue();
    }

}
