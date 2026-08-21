package de.heuboe.tls.receiver.rdr.item;

import java.util.LinkedList;
import java.util.List;

import de.heuboe.tls.receiver.interfaces.DataItem;

public class IntegerListItem extends AbstractDataItem {

        private List<Integer> constValList;

        public IntegerListItem( String name, List<Integer> constValList ) {
                super( name, 0 );
                this.constValList = constValList;
        }

        @Override
        public DataItemType getType() {
                return DataItemType.ILIST;
        }

        @Override
        public DataItem copy() {
                return new IntegerListItem( getName(), new LinkedList<Integer>( constValList ) );
        }

}
