package de.heuboe.tls.receiver.getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.heuboe.tls.receiver.impl.DataObject;
import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.GetterRule;
import de.heuboe.tls.receiver.item.ArrayItem;

public class ArrayGetter extends AbstractGetterWithSizeCol {

	private List<GetterRule> rules = new ArrayList<>();
	
	public ArrayGetter(String name) {
		super(name);
	}

	public void addRule(GetterRule rule) {
		rules.add(rule);
	}
	
	@Override
        public DataItem get( byte[] data, int ofs, Map<String, DataItem> etelVars ) {
                if ( ofs >= data.length ) {
                        throw new IllegalArgumentException( "DeBlock too short" );
                }

                List<DataObject> array = new ArrayList<>();
                int cnt = 0;
                int size = 0;
                if ( null == getSizeCol() ) {
                        cnt = data[ofs];
                        if ( cnt < 0 ) {
                                cnt += 256;
                        }
                        ++ofs;
                        size = 1;
                } else {
                        DataItem cntVar = etelVars.get( getSizeCol() );
                        if (null == cntVar) {
                                throw new IllegalArgumentException("No size variable: " + getSizeCol() );
                        }
                        cnt = cntVar.getAsLong().intValue();
                }

                for ( int i = 0; i < cnt; ++i ) {
                        DataObject obj = new DataObject();
                        for ( GetterRule rule : rules ) {
                                DataItem item = null;
                                item = rule.get( data, ofs, etelVars );
                                ofs += item.getConsumedSize();
                                size += item.getConsumedSize();
                                addItem( item, obj.getItems(), etelVars );
                        }
                        array.add( obj );
                }
                return new ArrayItem( name, size, array );
        }

	// for analysis purposes
        public List<GetterRule> getRules() {
                return rules;
        }

}
