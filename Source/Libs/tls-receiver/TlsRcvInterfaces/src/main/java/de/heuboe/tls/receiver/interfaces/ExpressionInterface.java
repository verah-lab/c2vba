package de.heuboe.tls.receiver.interfaces;

import java.util.Map;

import de.heuboe.tls.receiver.interfaces.DataItem.DataItemType;

/**
 * interface class for expressions. Expression are constructs like add '+', shift '&lt;&lt;' and so on.
 * @author ronald
 *
 */
public interface ExpressionInterface {
        
        /**
         * ExprType determines the type of an expression
         */
        public enum ExprType {
                BINOP,
                VARIABLE,
                CONST,
                CONSTLIST
        }
        
        /**
         * Get the kind of an expression
         * @return the kind of an expression
         */
        public ExprType getExprType();
        
        /**
         * Method to evaluate the defined expression
         * @param name The name for the resulting DataItem
         * @param etelVars The map of variables in the current context
         * @return The resulting DataItem
         */
        public DataItem eval( String name, Map<String, DataItem> etelVars );
        
        /**
         * returns the the type (i.e. integer, float, ...) of the resulting {@link DataItem} when {@link eval} was used
         * @return The datatype
         */
        public DataItemType getType();
        
        /**
         * Initialize the type value to be returned. Preferably internal use only.
         * @param name name of an resulting DataItem
         * @param typeMap map of types upto here
         */
        public void prepareType( String name, Map<String, DataItemType> typeMap );

}
