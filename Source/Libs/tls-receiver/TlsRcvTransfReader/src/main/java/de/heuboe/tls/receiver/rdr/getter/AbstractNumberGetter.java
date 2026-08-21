package de.heuboe.tls.receiver.rdr.getter;

import java.util.Map;

import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.DataItem.DataItemType;
import de.heuboe.tls.receiver.rdr.core.Expression;
import de.heuboe.tls.receiver.rdr.core.FunctionAbstract;

public abstract class AbstractNumberGetter extends AbstractFuncExpr {
        private String storageType = ""; // the type of data the analysed value should be converted to.
        
        public AbstractNumberGetter( String name, Expression expr, FunctionAbstract func, String resultType) {
                super( name, expr, func );
                this.storageType = resultType;
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
        
        /**
         * evaluate the resulting type for the given rule 
         * @param pname Name of potentially produced {@link DataItem}
         * @param typeMap map containing variable types upto now
         */
        protected void basePrepareType( String pname, Map<String, DataItemType> typeMap ) {
                typeMap.put( "$?", resType );
                if (getName().startsWith("$")) {
                        typeMap.put(getName(), resType);
                }
                
                if (null != func) {
                        func.prepareType( getName(), typeMap );
                        resType = func.getType();
                }
                if (null != expr) {
                        expr.prepareType( getName(), typeMap );
                        resType = expr.getType();
                }
        }
        
        /**
         * Get the number of bytes that will be consumed
         * @return The number of bytes that will be consumed or -1 if not known
         */
        public int getInputSize() {
                return -1;
        }

        @Override
        public String getTargetType() {
                return storageType;
        }
        
}
