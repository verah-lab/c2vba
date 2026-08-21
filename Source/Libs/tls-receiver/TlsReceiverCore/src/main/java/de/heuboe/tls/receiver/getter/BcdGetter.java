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
public class BcdGetter extends AbstractNumberGetter {

	private int size;

	public BcdGetter(String name, int size, Expression expr, FunctionAbstract func) {
                super( name, expr, func );
		this.size = size;
	}


	@Override
	public DataItem get(byte[] data, int ofs, Map<String, DataItem> etelVars) {
		long value = 0;
		if (ofs +size > data.length) {
			throw new IllegalArgumentException("DeBlock too short");
		}
		for(int i=0; i<size; ++i) {
			long bcd = data[ofs];
			if (bcd < 0) {
				bcd += 256;
			}
			int exponent = (size-i-1) * 2;
			int factor = getFactor(exponent);
			long wert = (bcd & 0x0F) * factor;
			value += wert;
			factor *= 10;
			wert = ((bcd & 0xF0) >> 4) * factor;
			value += wert;
			++ofs;
		}
		DataItem soFar = new IntegerItem(name, value, size);
		return handleCalculations( soFar, etelVars );
	}


	private int getFactor(int exponent) {
		if (exponent == 0) {
			return 1;
		}
		int f = 10;
		for(int i=1; i<exponent; ++i) {
			f *= 10;
		}
		return f;
	}
}
