package de.heuboe.tls.receiver.getter;

import java.util.Map;

import de.heuboe.tls.receiver.core.Expression;
import de.heuboe.tls.receiver.core.FunctionAbstract;
import de.heuboe.tls.receiver.interfaces.DataItem;

public abstract class AbstractNumberGetter extends AbstractFuncExpr {
        
        public AbstractNumberGetter( String name, Expression expr, FunctionAbstract func) {
                super( name, expr, func );
        }
        
        DataItem handleCalculations( DataItem soFar, Map<String, DataItem> etelVars ) {
                etelVars.put( "$?", soFar );
                
                if (null != func) {
                        return func.eval( name, etelVars );
                }
                if (null != expr) {
                        return expr.eval( name, etelVars );
                }
                
                return soFar;

        }
}
