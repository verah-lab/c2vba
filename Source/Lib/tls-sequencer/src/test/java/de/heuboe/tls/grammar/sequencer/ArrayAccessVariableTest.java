package de.heuboe.tls.grammar.sequencer;

import de.heuboe.tls.grammar.base.BasicVariable;
import de.heuboe.tls.grammar.base.ValueCollection;
import de.heuboe.tls.grammar.interfaces.Value;
import de.heuboe.tls.grammar.interfaces.Variable;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ArrayAccessVariableTest {

    /**
     * Tests for the eval method in the ArrayAccessVariable class.
     * <p>
     * The eval method evaluates a variable stored in a variable table.
     * If the variable is an instance of ArrayVariable, it sets the index for accessing the array,
     * then evaluates and returns the result. Otherwise, it returns null.
     */

    @Test
    void testEval_WithValidArrayVariable() {
        // Arrange
        String variableName = "arrayVar";
        String index = "2";
        String resultValue = "result";

        ArrayAccessVariable arrayAccessVariable = new ArrayAccessVariable(variableName, index);
        Map<String, Variable> mockVariableTable = mock(Map.class);
        ArrayVariable mockArrayVariable = mock(ArrayVariable.class);
        Value mockValue = mock(Value.class);

        when(mockVariableTable.get(variableName)).thenReturn(mockArrayVariable);
        when(mockVariableTable.get(ArrayVariable.INDEX)).thenReturn(new BasicVariable(ArrayVariable.INDEX, new ValueCollection.StringValue(index)));
        when(mockArrayVariable.getClassName()).thenReturn(ArrayVariable.class.getSimpleName());
        when(mockArrayVariable.eval(any(), eq(mockVariableTable))).thenReturn(mockValue);
        when(mockValue.toString()).thenReturn(resultValue);

        // Act
        Value result = arrayAccessVariable.eval(null, mockVariableTable);

        // Assert
        assertNotNull(result);
        assertEquals(resultValue, result.toString());
        verify(mockVariableTable).put(eq(ArrayVariable.INDEX), any(BasicVariable.class));
    }

    @Test
    void testEval_NonArrayVariable() {
        // Arrange
        String variableName = "nonArrayVar";
        String index = "0";

        ArrayAccessVariable arrayAccessVariable = new ArrayAccessVariable(variableName, index);
        Map<String, Variable> mockVariableTable = mock(Map.class);
        Variable mockVariable = mock(Variable.class);

        when(mockVariableTable.get(variableName)).thenReturn(mockVariable);
        when(mockVariable.getClassName()).thenReturn("NonArrayVariable");

        // Act
        Value result = arrayAccessVariable.eval(null, mockVariableTable);

        // Assert
        assertNull(result);
        verify(mockVariableTable, never()).put(eq(ArrayVariable.INDEX), any(BasicVariable.class));
    }

    @Test
    void testEval_WithNullIndexInVariableTable() {
        // Arrange
        String variableName = "arrayVar";
        String index = null;

        ArrayAccessVariable arrayAccessVariable = new ArrayAccessVariable(variableName, "");
        Map<String, Variable> mockVariableTable = mock(Map.class);
        ArrayVariable mockArrayVariable = mock(ArrayVariable.class);

        when(mockVariableTable.get(variableName)).thenReturn(mockArrayVariable);
        when(mockArrayVariable.getClassName()).thenReturn(ArrayVariable.class.getSimpleName());
        doThrow(NullPointerException.class).when(mockVariableTable).put(eq(ArrayVariable.INDEX), any(BasicVariable.class));

        // Act
        Value result = arrayAccessVariable.eval(null, mockVariableTable);

        // Assert
        assertNull(result);
    }

    @Test
    void testConstructor_NullIndexThrowsException() {
        // Arrange
        String variableName = "arrayVar";

        // Act & Assert
        assertThrows(NullPointerException.class, () -> new ArrayAccessVariable(variableName, null));
    }
}