package de.heuboe.tls.receiver.getter;

import java.util.List;
import java.util.Map;

import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.GetterRule;
import de.heuboe.tls.receiver.interfaces.DataItem.DataItemType;

/**
 * @author Ronald Nikel
 * A class implementing some components of the interface @see GetterRule
 */
public abstract class AbstractGetter implements GetterRule {
        
        protected String name;

	protected AbstractGetter( String name ) {
	        this.name = name;
	}

	protected void addItem(DataItem item, List<DataItem> itemList, Map<String, DataItem> etelVars) {
		if (item.getName().startsWith("$")) {
			etelVars.put(item.getName(), item);
		} else {
			if (item.getType() == DataItemType.LIST) {
				itemList.addAll(item.getAsItemList());
			} else {
				itemList.add(item);
			}
		}

	}

        /* (non-Javadoc)
         * @see de.heuboe.tls.receiver.interfaces.GetterRule#getName()
         * Return the
         */
        public String getName() {
                return name;
        }
}
