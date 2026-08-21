package de.heuboe.tls.receiver.getter;

import java.util.Map;

import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.item.SkipItem;

public class SkipGetter extends AbstractGetter {

	public SkipGetter() {
	        super( "skip" );
	}

	@Override
	public DataItem get(byte[] data, int ofs, Map<String, DataItem> etelVars) {
		return new SkipItem("SKIPITEM", 1);
	}


}
