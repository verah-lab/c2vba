package de.heuboe.tls.receiver.core;

import java.util.List;
import java.util.Map;

import de.heuboe.tls.receiver.core.Expression.ExprType;
import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.DataItem.DataItemType;
import de.heuboe.tls.receiver.item.IntegerItem;

public class FunctionInval extends FunctionAbstract {
        
        public FunctionInval( String name, List<Expression> arglist ) {
                super( name, arglist );
        }

        @Override
        public DataItem eval( String name, Map<String, DataItem> etelVars ) {
                Expression arg1 = this.arglist.get( 0 );
                Expression arg2 = this.arglist.get( 1 );
                Expression arg3 = null;
                if ( 2 < this.arglist.size() ) {
                        arg3 = this.arglist.get( 2 );
                }
                String varName = arg1.getVariable();
                DataItem variable = etelVars.get( varName );
                if ( null == variable ) {
                        throw new IllegalArgumentException( "Missing variable " + varName + " in variables in calculation for " + name );
                }
                if ( variable.getType() != DataItemType.INTEGER/* && DataItemType.Float != soFar.getType() */ ) {
                        throw new IllegalArgumentException( "Variable of wrong type for function inval for " + name );
                }
                int variableValue = variable.getAsLong().intValue();
                if ( ExprType.CONST == arg2.getExprType() ) {
                        Integer constVal = arg2.getConstVal();
                        if ( constVal.intValue() == variableValue ) {
                                return new IntegerItem( name, -1, variable.getConsumedSize() ); // mark as invalid value
                        }
                } else if ( ExprType.CONSTLIST == arg2.getExprType() ) {
                        List<Integer> constList = arg2.getConstValList();
                        for ( Integer lval : constList ) {
                                if ( lval.intValue() == variableValue ) {
                                        return new IntegerItem( name, -1, variable.getConsumedSize() ); // mark as invalid value
                                }
                        }
                } else {
                        throw new IllegalArgumentException( "Argument for illegal value(s) of wrong type for function inval for " + name );
                }
                if ( null != arg3 ) {
                        DataItem tmpRes = arg3.eval( name, etelVars );
                        tmpRes.setConsumedSize( variable.getConsumedSize() );
                        return tmpRes;
                }
                return new IntegerItem( name, variableValue, variable.getConsumedSize() );
        }

}
