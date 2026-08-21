package de.heuboe.tls.grammar.sequencer;

import de.heuboe.tls.grammar.base.BasicVariable;
import de.heuboe.tls.grammar.interfaces.Value;
import de.heuboe.tls.grammar.interfaces.Variable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is just another representation of the {@link BasicVariable} as array.
 */
@Slf4j
public class ArrayVariable implements Variable {

    @Setter
    private List<BasicVariable> value = new ArrayList<>();

    @Getter
    @Setter
    private String name;

    public static final String INDEX = "##_ARRAY_INDEX_##";

    /**
     * The constructor that creates a ArrayVariable with the required name and value of the variable.
     *
     * @param name      The name of the variable.
     * @param valueList The value of the variable.
     */
    public ArrayVariable(String name, List<Value> valueList) {
        this.name = name;
        valueList.forEach(v -> value.add(new BasicVariable(name, v)));
    }

    @Override
    public Value eval(Object dataFromBroker, Map<String, Variable> variableTable) {
        String indexString = variableTable.get(INDEX).getValue().getStringValue();

        int index;

        try {
            index = Integer.parseInt(indexString);
        } catch (NumberFormatException nfe) {
            index = variableTable.get(indexString).getValue().getIntValue();
        }

        if ((index > -1) && (index < value.size())) {
            return value.get(variableTable.get(variableTable.get(INDEX).getValue().getStringValue())
                    .getValue().getIntValue()).getValue();
        }
        log.warn("Tried to access the array '{}' at index {} but this array only contains {} values!",
                name, index, value.size()-1);
        return null;
    }

    @Override
    public ExprType getExprType() {
        return ExprType.VARIABLE;
    }

    @Override
    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Value getValue() {
        return null;
    }
    
    /**
     * Retrieves the {@link Value} at the specified index from the array of variables.
     *
     * @param index The zero-based position of the desired {@link Value} in the array.
     * @return The {@link Value} located at the specified index, or null if the index is invalid or out of bounds.
     */
    public Value getAtIndex( int index) {
        return value.get( index ).getValue();
    }
}
