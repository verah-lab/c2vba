package de.heuboe.tls.receiver.core;

import java.util.Map;

import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.DataItem.DataItemType;

/**
 * A class representing a condition
 * @author Ronald Nikel
 *
 */
public class Condition {

	private Expression expression; 
	
	/**
	 * Build a Condition from a expression
	 * @param expression Expression that has to yield true to fulfil condition
	 */
	public Condition(Expression expression) {
		this.expression = expression;
	}

	/**
	 * Evaluate the given expression
	 * @param etelVars Variables of the current context, that can influence the truth value
	 * @return The state of the condition
	 */
	public boolean isTrue(Map<String, DataItem> etelVars) {
		String name="";
		DataItem item = expression.eval(name, etelVars);
		if (item != null && item.getType() == DataItemType.INTEGER) {
			Long val = item.getAsLong();
			if (val != null) {
				return val != 0;
			}
		}
		return false;
	}

}
