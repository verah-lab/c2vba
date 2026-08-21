package de.heuboe.tls.receiver.getter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

import de.heuboe.tls.receiver.core.Expression;
import de.heuboe.tls.receiver.core.FunctionAbstract;
import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.item.FloatItem;

public class FloatGetter extends AbstractNumberGetter {

	public FloatGetter(String name, Expression expr, FunctionAbstract func) {
                super( name, expr, func );
	}

	@Override
	public DataItem get(byte[] data, int ofs, Map<String, DataItem> etelVars) {
		if (ofs+4 > data.length) {
			throw new IllegalArgumentException("DeBlock too short");
		}
		double value = ByteBuffer.wrap(data, ofs, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
		DataItem soFar = new FloatItem(name, value, 4);
		return handleCalculations( soFar, etelVars );
	}

}
