package de.heuboe.tls.receiver.rdr.getter;

import java.util.Map;

import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.DataItem.DataItemType;
import de.heuboe.tls.receiver.rdr.item.SkipItem;

public class SkipGetter extends AbstractGetter {
        
        static final String NAME = "SKIPPER";

	public SkipGetter() {
	        super( NAME );
	}

	@Override
	public DataItem get(byte[] data, int ofs, Map<String, DataItem> etelVars) {
		return new SkipItem( NAME , 1);
	}


        @Override
        public DataItemType getType() {
                return DataItemType.SKIP;
        }

        @Override
        public void prepareType( String name, Map<String, DataItemType> typeMap ) {
                // NOSONAR nothing to do here
        }
        
        @Override
        public String getTargetType() {
                return "";
        }
}
