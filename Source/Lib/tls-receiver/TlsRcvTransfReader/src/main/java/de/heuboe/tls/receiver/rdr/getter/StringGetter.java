package de.heuboe.tls.receiver.rdr.getter;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.DataItem.DataItemType;
import de.heuboe.tls.receiver.rdr.item.StringItem;

public class StringGetter extends BlockGetter/*AbstractGetterWithSizeCol*/ {

	public StringGetter(String name, int size, boolean toEnd, String targetType) {
		super( name, size, toEnd, targetType );
	}

	@Override
    public DataItem get( byte[] data, int ofs, Map<String, DataItem> etelVars ) {
        DataItem block = super.get( data, ofs, etelVars );
        String text = null;

        // this is a little hack. Later it should be replaced by different STRING-types.

        if ( !"textzeichen".equals( name ) ) {
            text = new String( block.getAsBlockRaw(), StandardCharsets.US_ASCII );
        } else {
            text = new String( block.getAsBlockRaw(), StandardCharsets.ISO_8859_1 );
        }
        return new StringItem( name, text, block.getConsumedSize() );

    }

    @Override
    public DataItemType getType() {
        return DataItemType.STRING;
    }
}
