package de.heuboe.tls.receiver.rdr.item;

import java.nio.charset.StandardCharsets;

import de.heuboe.tls.receiver.interfaces.DataItem;

public class StringItem extends AbstractDataItem {

	private String text;
	
	public StringItem(String name, String text, int size) {
		super(name, size);
//		this.text = StandardCharsets.UTF_8.encode(text).toString();
		this.text = text;
	}
	
	@Override
	public DataItemType getType() {
		return DataItemType.STRING;
	}

	@Override
	public String getAsString() {
		return text;
	}

	@Override
	public DataItem copy() {
		return new StringItem(text, text, consumedSize);
	}
}
