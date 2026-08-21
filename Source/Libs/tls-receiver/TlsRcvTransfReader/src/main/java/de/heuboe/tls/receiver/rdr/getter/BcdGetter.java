package de.heuboe.tls.receiver.rdr.getter;

import java.util.Map;

import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.DataItem.DataItemType;
import de.heuboe.tls.receiver.rdr.core.Expression;
import de.heuboe.tls.receiver.rdr.core.FunctionAbstract;
import de.heuboe.tls.receiver.rdr.item.IntegerItem;

/**
 * get an integer value from a single Byte.
 * 
 * @author ralfz
 *
 */
public class BcdGetter extends AbstractNumberGetter {

	private int size;

	public BcdGetter( String name, int size, Expression expr, FunctionAbstract func, String targetType ) {
                super( name, expr, func, targetType );
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

        @Override
        public void prepareType( String name, Map<String, DataItemType> typeMap ) {
                resType = DataItemType.INTEGER;
                basePrepareType( name, typeMap );
        }
        
        @Override
        public int getInputSize() {
                return size;
        }

}
