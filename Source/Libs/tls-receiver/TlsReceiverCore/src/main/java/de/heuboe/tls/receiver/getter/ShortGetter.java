package de.heuboe.tls.receiver.getter;

import java.util.Map;

import de.heuboe.tls.receiver.core.Expression;
import de.heuboe.tls.receiver.core.FunctionAbstract;
import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.item.IntegerItem;

/**
 * get an unsigned value from one Byte
 * 
 * @author ralfz
 *
 */
public class ShortGetter extends AbstractNumberGetter {

	private boolean signed;
	private boolean bigEndian;
	
	public ShortGetter(String name, boolean signed, boolean bigEndian, Expression expr, FunctionAbstract func) {
                super( name, expr, func );
		this.signed = signed;
		this.bigEndian = bigEndian;
	}
	
	@Override
	public DataItem get(byte[] data, int ofs, Map<String, DataItem> etelVars) {
		if (ofs+1 < data.length) {
			long value;
			if (bigEndian) {
				value = ((data[ofs  ] & 0xFF) << 8) & 0xFF00L;
			    value |= (data[ofs+1] & 0xFF);
			} else {
				value = ((data[ofs+1] & 0xFF) << 8) & 0xFF00;
			    value |= (data[ofs  ] & 0xFF);
			}
			if (signed && (value & 0x8000) != 0) {
				value |= 0xFFFFFFFFFFFF0000L;
			}
			DataItem soFar = new IntegerItem(name, value, 2);
			return handleCalculations( soFar, etelVars );
		}
		throw new IllegalArgumentException("DeBlock too short");
	}
}
