package de.heuboe.tls.receiver.rdr.item;

import de.heuboe.tls.receiver.interfaces.DataItem;

public class SkipItem extends AbstractDataItem {

	public SkipItem(String name, int size) {
		super(name, size);
	}

	@Override
	public DataItemType getType() {
		return DataItemType.SKIP;
	}

	@Override
	public DataItem copy() {
		return new SkipItem(name, consumedSize);
	}

}
