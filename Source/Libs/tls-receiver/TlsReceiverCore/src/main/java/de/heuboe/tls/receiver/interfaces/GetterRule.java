package de.heuboe.tls.receiver.interfaces;

import java.util.Map;

/**
 * @author Ronald Nikel
 *
 */
public interface GetterRule {

	/**
	 * @param data Th e input. A byte array the contains at least one DeBlock
	 * @param ofs The offset into the input
	 * @param etelVars A map containing variables describing the current context of analysis
	 * @return A DataItem which is a piece of data. e.g. the value of a column of a db record. The DataItem will have the name of the GetterRule in effect
	 */
	public DataItem get(byte[] data, int ofs, Map<String, DataItem> etelVars);

        /**
         * @return The name of the rule. I most cases this will be the name of the target object. e.g. the name of a database column
         */
        public String getName();
}
