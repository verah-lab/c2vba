package de.heuboe.tls.receiver.item;

import java.util.List;

import de.heuboe.tls.receiver.impl.DataObject;
import de.heuboe.tls.receiver.interfaces.DataItem;

public class ArrayItem extends AbstractDataItem {

	private List<DataObject> array;
	
	public ArrayItem(String name, int size, List<DataObject> array) {
		super(name, size);
		this.array = array;
	}

	@Override
	public DataItemType getType() {
		return DataItemType.ARRAY;
	}

	@Override
	public List<DataObject> getAsArray() {
		return array;
	}

	@Override
	public DataItem copy() {
		return new ArrayItem(name, consumedSize, array);
	}
}
