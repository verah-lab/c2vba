package de.heuboe.sdbby.strategy.matching;

import de.heuboe.data.Data;

/**
 * 
 * Data record
 * 
 * @author peters
 *
 */
public interface Dataset {
	
	/**
	 * 
	 * Single attribute value
	 * 
	 * @author peters
	 *
	 */
	public static class Value {
		
		private Data data;
		
		/**
		 * 
		 * Constructor
		 * 
		 * @param data	Generic data item
		 */
		public Value( Data data ) {
			this.data = data;
		}
		
		public int getInt() {
			return data.getAsInt();
		}
		
		public long getLong() {
			return data.getAsLong();
		}

		public String getString() {
			return data.getAsString();
		}
		
		public boolean isNull() {
			return data.isNull();
		}
	}
	
	String getDatakind();

	/**
	 * 
	 * Returns value for attribute name
	 * 
	 * @param attribute  Attribute name
	 * @return			 Value
	 */
	Value value( String attribute ); 
}
