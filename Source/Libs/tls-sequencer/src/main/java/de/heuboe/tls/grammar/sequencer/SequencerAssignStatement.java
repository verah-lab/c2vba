package de.heuboe.tls.grammar.sequencer;

import de.heuboe.tls.grammar.base.AssignStatement;
import de.heuboe.tls.grammar.interfaces.Expression;
import de.heuboe.tls.grammar.interfaces.Result;
import de.heuboe.tls.grammar.interfaces.Variable;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * This class will handle the simple statement assignment for the sequencer.
 */
@Slf4j
public class SequencerAssignStatement extends AssignStatement {

    /**
     * Constructs an {@link SequencerAssignStatement} with a property name and right-handed statement {@link Expression}.
     *
     * @param varName The name of the objects property as string.
     * @param rhs     A right-handed statement {@link Expression}.
     */
    public SequencerAssignStatement(String varName, Expression rhs) {
        super(varName, rhs);
    }

    @Override
    public int execute(Result result, int ptr, Object inputData, Map<String, Variable> variableTable) {

        // if we have any other variable check for write protection
        if (variableTable.get(super.getVarName()) instanceof ProtectedBasicVariable) {
            log.warn("Variable '{}' is not writable!", super.getVarName());
            return 0;
        }
        // change variable
        return super.execute(result, ptr, inputData, variableTable);
    }
}
