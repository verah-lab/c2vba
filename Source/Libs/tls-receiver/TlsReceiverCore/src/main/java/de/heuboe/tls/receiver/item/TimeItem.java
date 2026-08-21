package de.heuboe.tls.receiver.item;

import java.util.Date;

import de.heuboe.tls.receiver.interfaces.DataItem;

/**
 * An time item is a data item that represents an time value derived from some bytes.
 * 
 * @author ralfz
 *
 */

public class TimeItem extends AbstractDataItem {

	private Date value;

	public TimeItem(String name, Date value, int size) {
		super(name, size);
		this.value = value;
	}
	
	@Override
	public DataItemType getType() {
		return DataItemType.DATE;
	}

	@Override
	public Long getAsLong() {
		return value.getTime();
	}

	@Override
	public Date getAsDate() {
		return value;
	}

	@Override
	public DataItem copy() {
		return new TimeItem(name, value, consumedSize);
	}

}
