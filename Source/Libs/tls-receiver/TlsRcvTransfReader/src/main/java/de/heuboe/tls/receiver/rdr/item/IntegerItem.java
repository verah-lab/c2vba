package de.heuboe.tls.receiver.rdr.item;

import de.heuboe.tls.receiver.interfaces.DataItem;

/**
 * An integer item is a data item that represents an integer value derived from some bytes.
 * 
 * @author ralfz
 *
 */
public class IntegerItem extends AbstractDataItem {

	private long value;

	public IntegerItem(String name, long value, int size) {
		super(name, size);
		this.value = value;
	}
	
	@Override
	public Long getAsLong() {
		return value;
	}
	
	@Override
	public DataItemType getType() {
		return DataItemType.INTEGER;
	}

	@Override
	public DataItem copy() {
		return new IntegerItem(name, value, consumedSize);
	}

}
