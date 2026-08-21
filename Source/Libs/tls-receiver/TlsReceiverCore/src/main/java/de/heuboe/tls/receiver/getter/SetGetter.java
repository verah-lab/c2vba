package de.heuboe.tls.receiver.getter;

import java.util.ArrayList;
import java.util.Map;

import de.heuboe.tls.receiver.core.Expression;
import de.heuboe.tls.receiver.core.FunctionAbstract;
import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.item.ListItem;

public class SetGetter extends AbstractFuncExpr {
		
	public SetGetter(String name, Expression expression) {
		super( name, expression, null );
	}
        
        public SetGetter(String name, FunctionAbstract function) {
                super( name, null, function );
        }


	@Override
	public DataItem get(byte[] data, int ofs, Map<String, DataItem> etelVars) {
		DataItem item = null;
		if (null != expr) {
		        item = expr.eval(name, etelVars);
		} 
                if (null != func) {
                        item = func.eval(name, etelVars);
                } 
		if (item != null) {
			if (!name.startsWith("$")) {
				item = item.copy();
				item.setConsumedSize(0);
				return item;
			}
			etelVars.put(name, item);
		}
		return new ListItem(0, new ArrayList<>());
	}

}
