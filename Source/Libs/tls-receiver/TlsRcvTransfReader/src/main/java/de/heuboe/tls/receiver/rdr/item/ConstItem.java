package de.heuboe.tls.receiver.rdr.item;

import de.heuboe.tls.receiver.interfaces.DataItem;

public class ConstItem extends AbstractDataItem {

	private long value;
	
	public ConstItem(String name, long value) {
		super(name, 0);
		this.value = value;
	}

	@Override
	public DataItemType getType() {
		return DataItemType.INTEGER;
	}

	@Override
	public Long getAsLong() {
		return value;
	}

	@Override
	public DataItem copy() {
		return new ConstItem(name, value);
	}
}
