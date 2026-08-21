package de.heuboe.tls.receiver.getter;

import java.util.Map;

import de.heuboe.tls.receiver.core.Expression;
import de.heuboe.tls.receiver.core.FunctionAbstract;
import de.heuboe.tls.receiver.interfaces.DataItem;

/**
 * Intermediate class carrying functions or expressions
 * @author ronald
 *
 */
public abstract class AbstractFuncExpr extends AbstractGetter {
        
        protected Expression expr;
        protected FunctionAbstract func;
        
        /**
         * Constructor taking name expression and function
         * @param name Name of resulting DtaItem
         * @param expr expression to evaluate or null
         * @param func function to evaluate or null
         */
        public AbstractFuncExpr( String name, Expression expr, FunctionAbstract func) {
                super( name );
                this.expr = expr;
                this.func = func;
        }

        public Expression getExpr() {
                return expr;
        }

        public FunctionAbstract getFunc() {
                return func;
        }

}
