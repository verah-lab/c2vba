package de.heuboe.tls.receiver.getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.GetterRule;
import de.heuboe.tls.receiver.item.ListItem;

public class OptionalGetter extends AbstractGetter {

	private List<GetterRule> rules = new ArrayList<>();
	
	public OptionalGetter() {
		super( "optional" );
	}

	public void addRule(GetterRule rule) {
		rules.add(rule);
	}
	
	@Override
	public DataItem get(byte[] data, int ofs, Map<String, DataItem> etelVars) {
		if (ofs > data.length) {
			throw new IllegalArgumentException("DeBlock too short");			
		}
		if (ofs == data.length) {
			return new ListItem(0, new ArrayList<>());
		}
		return getItemList(data, ofs, etelVars);
	}

	private DataItem getItemList(byte[] data, int ofs, Map<String, DataItem> etelVars) {
		List<DataItem> itemList = new ArrayList<>();		
		int size =0;
		
		for(GetterRule rule : rules) {
			DataItem item = null;
			item = rule.get(data, ofs, etelVars);
			ofs += item.getConsumedSize();
			size += item.getConsumedSize();
			addItem(item, itemList, etelVars);
		}
		return new ListItem(size, itemList);
	}
        
        // for analysis purposes
        public List<GetterRule> getRules() {
                return rules;
        }
}
