package de.heuboe.tls.receiver.getter;

import java.util.Map;

import de.heuboe.tls.receiver.core.Expression;
import de.heuboe.tls.receiver.core.FunctionAbstract;
import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.item.IntegerItem;

/**
 * get an integer value from a single Byte.
 * 
 * @author ralfz
 *
 */
public class ByteGetter extends AbstractNumberGetter {

	private Boolean signed;
	
	public ByteGetter(String name, Boolean signed, Expression expr, FunctionAbstract func) {
		super( name, expr, func );
		this.signed = signed;
	}
	
	@Override
	public DataItem get(byte[] data, int ofs, Map<String, DataItem> etelVars) {
		if (ofs < data.length) {
			long value = data[ofs];
			if (!signed && value < 0) {
				value += 256;
			}
			DataItem tmpReceived = new IntegerItem(name, value, 1);
			return handleCalculations( tmpReceived, etelVars );
		}
		throw new IllegalArgumentException("DeBlock too short");
	}
}
