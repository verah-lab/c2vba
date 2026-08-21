package de.heuboe.tls.receiver.interfaces;

import java.util.Map;

import de.heuboe.tls.receiver.interfaces.DataItem.DataItemType;

/**
 * @author Ronald Nikel
 *
 */
public interface GetterRule {

	/**
	 * @param data The input. A byte array the contains at least one DeBlock
	 * @param ofs The offset into the input
	 * @param etelVars A map containing variables describing the current context of analysis
	 * @return A DataItem which is a piece of data. e.g. the value of a column of a db record. The DataItem will have the name of the GetterRule in effect
	 */
	public DataItem get(byte[] data, int ofs, Map<String, DataItem> etelVars);

        /**
         * @return The name of the rule. I most cases this will be the name of the target object. e.g. the name of a database column
         */
        public String getName();
        
        /**
         * returns the the type (i.e. integer, float, ...) of the resulting {@link DataItem} when {@link get} was used
         * @return The datatype
         */
        public DataItemType getType();
        
         /* 
         * evaluate the resulting type for the given rule 
         * @param name name of a potentially resulting DataItem
         * @param typeMap map containing variable types upto now
         */
        public void prepareType( String name, Map<String, DataItemType> typeMap );

        /**
         * get the target type of a getter rule (for single values) if one is defined
         * @return an explicitly defined target type such as 'as( milesPerOur )' or an empty string
         */
        public String getTargetType();
}
