package de.heuboe.tls.receiver.rdr.getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.DataObjectIf;
import de.heuboe.tls.receiver.interfaces.DataItem.DataItemType;
import de.heuboe.tls.receiver.interfaces.DeBlockDefinitionIf;
import de.heuboe.tls.receiver.interfaces.GetterRule;
import de.heuboe.tls.receiver.rdr.core.Condition;
import de.heuboe.tls.receiver.rdr.item.ListItem;

public class IfGetter extends AbstractGetter {

	private List<GetterRule> ifRules = new ArrayList<>();
	private List<GetterRule> elseRules = new ArrayList<>();
	private Condition condition;
	
	public IfGetter(Condition condition) {
		super( "if" );
		this.condition = condition;
	}

	public void addRule(GetterRule rule, boolean ifTrue) {
		if (ifTrue) {
			ifRules.add(rule);
		} else {
			elseRules.add(rule);
		}
	}
	
	@Override
	public DataItem get(byte[] data, int ofs, Map<String, DataItem> etelVars) {
		if (ofs > data.length) {
			throw new IllegalArgumentException("DeBlock too short");			
		}
		
		if (condition.isTrue(etelVars)) {
			return getItemList(data, ofs, etelVars, ifRules);
		} else if (elseRules != null) {
			return getItemList(data, ofs, etelVars, elseRules);
		}
		return null;
	}

	private DataItem getItemList(byte[] data, int ofs, Map<String, DataItem> etelVars, List<GetterRule> rules) {
		int size =0;
		ListItem listItem = new ListItem(size, new ArrayList<>());
		for(GetterRule rule : rules) {
			DataItem item = null;
			item = rule.get(data, ofs, etelVars);
			ofs += item.getConsumedSize();
			size += item.getConsumedSize();
			addI(item, listItem.getAsItemList(), etelVars);
		}
		listItem.setConsumedSize(size);
		return listItem;
	}
	
        private void addI(DataItem item, List<DataItem> itemList, Map<String, DataItem> etelVars) {
                if (item.getName().startsWith("$")) {
                        etelVars.put(item.getName(), item);
                } else {
                        itemList.add(item);
                }

        }

        // for analysis purposes
	public List<GetterRule> getIfRules() {
                return ifRules;
        }

	// for analysis purposes
        public List<GetterRule> getElseRules() {
                return elseRules;
        }

        @Override
        public DataItemType getType() {
                return DataItemType.COMPOUND;
        }

        @Override
        public void prepareType( String name, Map<String, DataItemType> typeMap ) {
                for (GetterRule r:ifRules) {
                        r.prepareType( name, typeMap );
                }
                for (GetterRule r:elseRules) {
                        r.prepareType( name, typeMap );
                }
        }
        
        @Override
        public void setLocationContext( boolean locationContext ) {
                ifRules.forEach( r -> ( (AbstractGetter) r ).setLocationContext( locationContext ) );
                elseRules.forEach( r -> ( (AbstractGetter) r ).setLocationContext( locationContext ) );
        }
        
        @Override
        public String getTargetType() {
                return "";
        }

}
