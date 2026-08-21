package de.heuboe.tls.receiver.getter;

import java.util.Map;

import de.heuboe.tls.receiver.core.Expression;
import de.heuboe.tls.receiver.core.FunctionAbstract;
import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.item.IntegerItem;

/**
 * get a node number from thre Bytes.
 * They are always unsigned and little endian.
 * 
 * @author ralfz
 *
 */
public class NodeGetter extends AbstractNumberGetter {

	public NodeGetter(String name, Expression expr, FunctionAbstract func) {
                super( name, expr, func );
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
}
