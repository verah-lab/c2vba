package de.heuboe.tls.receiver.rdr.item;

import de.heuboe.tls.receiver.interfaces.DataItem;

/**
 * An integer item is a data item that represents an integer value derived from some bytes.
 * 
 * @author ralfz
 *
 */
public class FloatItem extends AbstractDataItem {

	private double value;

	public FloatItem(String name, double value, int size) {
		super(name, size);
		this.value = value;
	}
	
	@Override
	public Double getAsDouble() {
		return value;
	}
	
	@Override
	public DataItemType getType() {
		return DataItemType.FLOAT;
	}

	@Override
	public DataItem copy() {
		return new FloatItem(name, value, consumedSize);
	}
    
    @Override
    public Long getAsLong() {
        return Long.valueOf( (long) ( Math.floor( value ) ) );
    }

}
