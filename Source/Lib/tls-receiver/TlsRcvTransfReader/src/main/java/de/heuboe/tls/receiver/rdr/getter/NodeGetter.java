package de.heuboe.tls.receiver.rdr.getter;

import java.util.Map;

import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.DataItem.DataItemType;
import de.heuboe.tls.receiver.rdr.core.Expression;
import de.heuboe.tls.receiver.rdr.core.FunctionAbstract;
import de.heuboe.tls.receiver.rdr.item.IntegerItem;

/**
 * get a node number from thre Bytes.
 * They are always unsigned and little endian.
 * 
 * @author ralfz
 *
 */
public class NodeGetter extends AbstractNumberGetter {

	public NodeGetter(String name, Expression expr, FunctionAbstract func, String targetType ) {
                super( name, expr, func, targetType );
	}
	
	@Override
	public DataItem get(byte[] data, int ofs, Map<String, DataItem> etelVars) {
		if (ofs+2 < data.length) {
			long value = ((data[ofs+2] & 0xFF) << 16) & 0xFF0000;
		    value |= ((data[ofs+1] & 0xFF) << 8);
		    value |= (data[ofs] & 0xFF);
		    DataItem soFar = new IntegerItem(name, value, 3);
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
                return 3;
        }
}
