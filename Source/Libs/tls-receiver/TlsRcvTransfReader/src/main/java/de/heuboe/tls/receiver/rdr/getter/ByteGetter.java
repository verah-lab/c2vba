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
public class ByteGetter extends AbstractNumberGetter {

	private Boolean signed;
	
	public ByteGetter(String name, Boolean signed, Expression expr, FunctionAbstract func, String resulttype) {
		super( name, expr, func, resulttype );
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

        @Override
        public void prepareType( String name, Map<String, DataItemType> typeMap ) {
                resType = DataItemType.INTEGER;
                basePrepareType( name, typeMap );
        }
        
        @Override
        public int getInputSize() {
                return 1;
        }
}
