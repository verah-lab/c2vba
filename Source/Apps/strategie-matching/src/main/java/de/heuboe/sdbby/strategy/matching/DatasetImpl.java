package de.heuboe.sdbby.strategy.matching;

import java.util.Map;


/**
 * 
 * Implementation of Dataset
 * 
 * @author peters
 *
 */
public class DatasetImpl implements Dataset {
	
	private String datakind;
	private Map<String,Value> values;
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param datakind	Datakind name
	 * @param values	Values
	 */
	public DatasetImpl( String datakind, Map<String,Value> values ) {
		this.datakind = datakind;
		this.values = values;
	}
	
	public String getDatakind() {
		return this.datakind;
	}

	public Value value( String attribute ) {
		return values.get( attribute );
	}
}
