package de.heuboe.tls.receiver.rdr.getter;

import java.util.Map;

import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.DataItem.DataItemType;
import de.heuboe.tls.receiver.rdr.core.Expression;
import de.heuboe.tls.receiver.rdr.core.FunctionAbstract;
import de.heuboe.tls.receiver.rdr.item.IntegerItem;

/**
 * get an unsigned value from one Byte
 * 
 * @author ralfz
 *
 */
public class LongGetter extends AbstractNumberGetter {

	private boolean signed;
	private boolean bigEndian;
	
	public LongGetter( String name, boolean signed, boolean bigEndian, Expression expr, FunctionAbstract func, String targetType ) {
                super( name, expr, func, targetType );
		this.signed = signed;
		this.bigEndian = bigEndian;
	}
	
	@Override
	public DataItem get(byte[] data, int ofs, Map<String, DataItem> etelVars) {
		if (ofs+3 < data.length) {
			long value;
			if (bigEndian) {
				value  = ((data[ofs  ] & 0xFF) << 24) & 0xFF000000L;
				value |= ((data[ofs+1] & 0xFF) << 16);
				value |= ((data[ofs+2] & 0xFF) <<  8);
			    value |=  (data[ofs+3] & 0xFF);
			} else {
				value  = ((data[ofs+3] & 0xFF) << 24) & 0xFF000000L;	
				value |= ((data[ofs+2] & 0xFF) << 16);
				value |= ((data[ofs+1] & 0xFF) <<  8);
			    value |=  (data[ofs  ] & 0xFF);
			}
			if (signed && (value & 0x80000000) != 0) {
				value |= 0xFFFFFFFF00000000L;
			}
			DataItem soFar = new IntegerItem(name, value, 4);
			return handleCalculations( soFar, etelVars );
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
                return 4;
        }
}
