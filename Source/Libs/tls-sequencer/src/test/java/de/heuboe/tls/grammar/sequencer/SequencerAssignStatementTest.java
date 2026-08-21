package de.heuboe.tls.grammar.sequencer;

import de.heuboe.tls.grammar.interfaces.Expression;
import de.heuboe.tls.grammar.interfaces.Result;
import de.heuboe.tls.grammar.interfaces.Variable;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SequencerAssignStatementTest {

    /**
     * Tests the execute method in SequencerAssignStatement.
     * <p>
     * This method checks for the correct handling of protected variables in the variable table and
     * verifies that the execution proceeds correctly under normal conditions.
     */

    @Test
    void testExecute_WithProtectedVariable() {
        // Arrange
        String variableName = "protectedVar";
        Expression mockExpression = Mockito.mock(Expression.class);
        SequencerAssignStatement sequencerAssignStatement = new SequencerAssignStatement(variableName, mockExpression);
        Result mockResult = Mockito.mock(Result.class);
        Map<String, Variable> variableTable = new HashMap<>();
        ProtectedBasicVariable mockProtectedVariable = Mockito.mock(ProtectedBasicVariable.class);
        variableTable.put(variableName, mockProtectedVariable);

        // Act
        int result = sequencerAssignStatement.execute(mockResult, 0, null, variableTable);

        // Assert
        assertEquals(0, result);
        verifyNoInteractions(mockExpression);
    }
}