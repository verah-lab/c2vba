package de.heuboe.tls.receiver.rdr.core;

import java.util.List;

import de.heuboe.tls.receiver.interfaces.DataItem.DataItemType;
import de.heuboe.tls.receiver.interfaces.FunctionInterface;

/**
 * Class handling function calls
 * @author Ronald Nikel
 *
 */
public abstract class FunctionAbstract implements FunctionInterface {

        protected String funcName;
        protected List<Expression> arglist;
        protected DataItemType resType = null;

        /**
         * Constructor defining a function
         * @param funcName The name of the function
         * @param arglist The list of arguments
         */
        public FunctionAbstract(String funcName, List<Expression> arglist) {
                this.funcName = funcName;
                this.arglist = arglist;
        }

        public String getFuncName() {
                return funcName;
        }

        public List<Expression> getArglist() {
                return arglist;
        }
        
        public DataItemType getType() {
                return resType;
        }
        
}
