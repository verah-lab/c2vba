package de.heuboe.tls.receiver.rdr.getter;

import java.util.Map;

import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.DataItem.DataItemType;
import de.heuboe.tls.receiver.rdr.core.Expression;
import de.heuboe.tls.receiver.rdr.core.FunctionAbstract;
import de.heuboe.tls.receiver.rdr.item.SkipItem;

public class SetGetter extends AbstractFuncExpr {
        private String targetType = "";
		
	public SetGetter(String name, Expression expression, String targetType ) {
		super( name, expression, null );
		this.targetType = targetType;
	}
        
        public SetGetter(String name, FunctionAbstract function, String targetType) {
                super( name, null, function );
                this.targetType = targetType;
        }


        @Override
        public DataItem get( byte[] data, int ofs, Map<String, DataItem> etelVars ) {
                DataItem item = null;
                if ( null != expr ) {
                        item = expr.eval( name, etelVars );
                }
                if ( null != func ) {
                        item = func.eval( name, etelVars );
                }
                if ( item != null ) {
                        if ( !name.startsWith( "$" ) ) {
                                item = item.copy();
                                item.setConsumedSize( 0 );
                                return item;
                        }
                        etelVars.put( name, item );
                }
                return new SkipItem( "Set" + name + "Skip", 0 ); // should not be used
        }

        @Override
        public void prepareType( String name, Map<String, DataItemType> typeMap ) {
                DataItemType itemType = null;
                if (null != expr) {
                        expr.prepareType( name, typeMap );
                        itemType = expr.getType();
                } 
                if (null != func) {
                        func.prepareType( name, typeMap );
                        itemType = func.getType();
                } 
                if (itemType != null) {
                        if (!name.startsWith("$")) {
                                resType = itemType;
                                return;
                        }
                        typeMap.put(name, itemType);
                }
                resType = DataItemType.NONE;
        }
        
        @Override
        public String getTargetType() {
                return targetType;
        }

}
