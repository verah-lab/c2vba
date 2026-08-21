package de.heuboe.tls.receiver.rdr.getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.DataObjectIf;
import de.heuboe.tls.receiver.interfaces.DataItem.DataItemType;
import de.heuboe.tls.receiver.interfaces.GetterRule;
import de.heuboe.tls.receiver.rdr.impl.DataObject;
import de.heuboe.tls.receiver.rdr.item.ArrayItem;

public class ArrayGetter extends AbstractGetterWithSizeCol {

	private List<GetterRule> rules = new ArrayList<>();

	public ArrayGetter(String name, String targetType) {
		super(name, targetType);
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
                                addItem( item, obj, etelVars );
                        }
                        array.add( obj );
                }
                return new ArrayItem( name, size, array );
        }

        private void addItem(DataItem item, DataObjectIf dob, Map<String, DataItem> etelVars ) {
                if (item.getName().startsWith("$")) {
                        etelVars.put(item.getName(), item);
                } else {
                        if (item.getType() == DataItemType.LIST) {
//                                if (isLocationContext() && 1 == item.getAsItemList().size()) {
//                                        dob.addItem( item.getAsItemList().get( 0 ) );
//                                } else {
//                                        throw new IllegalStateException("Should not get here");
//                                }
                            for ( DataItem di : item.getAsItemList() ) {
                                dob.addItem( di );
                            }
//                              itemList.addAll(item.getAsItemList());
                        } else {
//                              itemList.add(item);
                                dob.addItem( item );
                        }
                }

        }

	// for analysis purposes
        public List<GetterRule> getRules() {
                return rules;
        }

        @Override
        public DataItemType getType() {
                return DataItemType.ARRAY;
        }

        @Override
        public void prepareType( String name, Map<String, DataItemType> typeMap ) {
                for (GetterRule r : rules) {
                        r.prepareType( name, typeMap );
                }
        }

    @Override
    public void setLocationContext( boolean locationContext ) {
        this.locationContext = true;
        rules.forEach( r -> ( (AbstractGetter) r ).setLocationContext( locationContext ) );
    }
}
