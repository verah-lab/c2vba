package de.heuboe.tls.receiver.item;

import java.util.List;

import de.heuboe.tls.receiver.interfaces.DataItem;

public class ListItem extends AbstractDataItem {

	private List<DataItem> itemList;
	
	public ListItem(int size, List<DataItem> array) {
		super("ListItem", size);
		this.itemList = array;
	}

	@Override
	public DataItemType getType() {
		return DataItemType.LIST;
	}

	@Override
	public List<DataItem> getAsItemList() {
		return itemList;
	}

	@Override
	public DataItem copy() {
		return new ListItem(consumedSize, itemList);
	}
}
