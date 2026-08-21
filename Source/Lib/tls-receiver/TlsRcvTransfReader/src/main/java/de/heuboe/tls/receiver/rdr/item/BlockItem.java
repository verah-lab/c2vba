package de.heuboe.tls.receiver.rdr.item;

import de.heuboe.tls.receiver.interfaces.DataItem;

public class BlockItem extends AbstractDataItem {

	private byte[] block;
	private long[] blockUnsigend;
	
        public BlockItem( String name, byte[] block, int size ) {
                super( name, size );
                this.block = block;

                blockUnsigend = new long[block.length];
                for ( int i = 0; i < block.length; ++i ) {
                        long value = block[i];
                        if ( value < 0 ) {
                                value += 256;
                        }
                        blockUnsigend[i] = value;
                }
        }
	
	@Override
	public DataItemType getType() {
		return DataItemType.BLOCK;
	}

	@Override
	public byte[] getAsBlockRaw() {
		return block;
	}

        @Override
        public long[] getAsBlock() {
                return blockUnsigend;
        }

	@Override
	public DataItem copy() {
		return new BlockItem(name, block, consumedSize);
	}
}
