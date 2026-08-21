package de.heuboe.tls.receiver.getter;

import java.util.Map;

import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.item.StringItem;

public class StringGetter extends BlockGetter/*AbstractGetterWithSizeCol*/ {

	public StringGetter(String name, int size, boolean toEnd) {
		super( name, size, toEnd );
	}

	@Override
	public DataItem get(byte[] data, int ofs, Map<String, DataItem> etelVars) {
		DataItem block = super.get(data, ofs, etelVars);
		String text = new String(block.getAsBlockRaw());
		return new StringItem(name, text, block.getConsumedSize());
	}
}
