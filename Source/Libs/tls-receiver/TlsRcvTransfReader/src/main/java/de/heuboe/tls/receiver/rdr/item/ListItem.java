package de.heuboe.tls.receiver.rdr.item;

import java.util.ArrayList;
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
                List<DataItem> res = new ArrayList<>();
                for ( DataItem item : itemList ) {
                        res.add( item.copy() );
                }
                return new ListItem( consumedSize, res );
        }
}
