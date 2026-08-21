package de.heuboe.tls.receiver.getter;

import java.util.Arrays;
import java.util.Map;

import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.item.BlockItem;
import de.heuboe.tls.receiver.item.SkipItem;

public class BlockGetter extends AbstractGetterWithSizeCol {

	private int size;
	private boolean toEnd;
	private boolean skip;

	public BlockGetter(String name, int size, boolean toEnd) {
	        super( name );
		this.size = size;
		this.toEnd = toEnd;
	}

	@Override
	public DataItem get(byte[] data, int ofs, Map<String, DataItem> etelVars) {
		int offset = 0;
		int len = size;
		if (ofs >= data.length) {
			throw new IllegalArgumentException("DeBlock too short");
		}
		if (toEnd) {
			len = data.length - ofs;
		} else if (size == 0) {
			len = data[ofs];
			if (null != getSizeCol()) {
			        DataItem sizeVar = etelVars.get( getSizeCol() );
			        if (null == sizeVar) {
			                throw new IllegalArgumentException("No size variable: " + getSizeCol() );
			        }
			        len = sizeVar.getAsLong().intValue();
			} else {
	                        offset = 1;
			}
			if (len < 0) {
				len += 256;
			}
		}
		if (ofs + offset + len > data.length) {
			throw new IllegalArgumentException("DeBlock too short");
		}
		if (skip) {
		        return new SkipItem(name, offset+len);		        
		}
		byte[] block;
		try {
			block = Arrays.copyOfRange(data, ofs+offset, ofs+offset+len);
		} catch(ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Oops, this should not happen - Index out of bounds while copying");
		}
	        return new BlockItem(name, block, offset+len);
	}
	
	public void doSkip() {
	        skip = true;
	}
}
