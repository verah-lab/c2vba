package de.heuboe.tls.receiver.core;

import java.util.List;

/**
 * Class handling function calls
 * @author Ronald Nikel
 *
 */
public abstract class FunctionAbstract implements FunctionInterface {

        protected String funcName;
        protected List<Expression> arglist;

        /**
         * Constructor which uses a variable (~ $<name>) as expression. e.g. 5 + $AnzETel
         * @param variable The name of the variable in the current context
         */
        public FunctionAbstract(String funcName, List<Expression> arglist) {
                this.funcName = funcName;
                this.arglist = arglist;
        }
}
