package de.heuboe.tls.receiver.rdr.item;

import java.util.LinkedList;
import java.util.List;

import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.DataObjectIf;
import de.heuboe.tls.receiver.rdr.impl.DataObject;

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
	public List<DataObjectIf> getAsArray() {
	        List<DataObjectIf> res = new LinkedList<DataObjectIf>();
	        res.addAll( array );
		return res;
	}

	@Override
	public DataItem copy() {
		return new ArrayItem(name, consumedSize, array);
	}
}
