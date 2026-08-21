package de.heuboe.tls.receiver.interfaces;

import java.util.Map;

import de.heuboe.tls.receiver.interfaces.DataItem.DataItemType;

/**
 * Class representing the bas of functions. 
 * @author ronald
 *
 */
public interface FunctionInterface {
        
        /**
         * Basic method that has to be called to abtain a result
         * @param name Name of the resulting {@link DataItem}
         * @param etelVars A table of virables that exist while the function is evaluated 
         * @return The resulting data item of the eval call
         */
        public DataItem eval( String name, Map<String, DataItem> etelVars );
        
        /**
         * Get the resulting type of the function
         * @return the resulting type of the function 
         */
        public DataItemType getType();
        
        /**
         * Initialize the type value to be returned. Preferably internal use only.
         * @param name name of an resulting DataItem
         * @param typeMap map of types upto here
         */
        public void prepareType( String name, Map<String, DataItemType> typeMap );
}
