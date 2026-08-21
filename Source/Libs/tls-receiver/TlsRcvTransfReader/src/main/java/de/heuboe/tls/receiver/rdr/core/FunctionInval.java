package de.heuboe.tls.receiver.rdr.core;

import java.util.List;
import java.util.Map;

import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.DataItem.DataItemType;
import de.heuboe.tls.receiver.interfaces.ExpressionInterface.ExprType;
import de.heuboe.tls.receiver.rdr.getter.FloatGetter;
import de.heuboe.tls.receiver.rdr.item.FloatItem;
import de.heuboe.tls.receiver.rdr.item.IntegerItem;

public class FunctionInval extends FunctionAbstract {
    
    private static boolean haveInvalidReplacement = false;
    private static double invalidReplacement = -99999; // this should be used if ereryone is informed and ready
        
        public FunctionInval( String name, List<Expression> arglist ) {
                super( name, arglist );
        }
        
        
        /**
         * Allow a value to be set which indicates that floating point values are invalid
         * @param invalidValue string representing the invalid value. Will be parsed by Double.parseDouble.
         */
        public static void setFloatInvalid( String invalidValue ) {
            FloatGetter.setFloatInvalid( invalidValue ); // TODO rework later
            if ( (null == invalidValue) || ( (null != invalidValue) && (invalidValue.trim().length() == 0) ) ) { // NOSONAR stress non-null value of invalidValue
                return;
            }
            invalidReplacement = Double.parseDouble( invalidValue );
            haveInvalidReplacement = true;
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
                                return returnInvalidValue( name, variable );
                        }
                } else if ( ExprType.CONSTLIST == arg2.getExprType() ) {
                        List<Integer> constList = arg2.getConstValList();
                        for ( Integer lval : constList ) {
                                if ( lval.intValue() == variableValue ) {
                                        return returnInvalidValue( name, variable );
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
        
        DataItem returnInvalidValue( String name, DataItem variable ) {
                if (DataItemType.INTEGER == this.resType) {
                        return new IntegerItem( name, -1, variable.getConsumedSize() ); // mark as invalid value
                } else if (DataItemType.FLOAT == this.resType) {
                    if (!haveInvalidReplacement) { // retain old behaviour
                        return new FloatItem( name, Float.MIN_VALUE, variable.getConsumedSize() ); // mark as invalid value
                    } else {
                        return new FloatItem( name, invalidReplacement, variable.getConsumedSize() ); // mark as invalid value
                    }
                }
                throw new IllegalArgumentException( "Illegal result type for function inval for " + name );
        }

        @Override
        public void prepareType( String name, Map<String, DataItemType> typeMap ) {
                if (null != resType) {
                        return;
                }
                
                Expression arg1 = this.arglist.get( 0 );
                Expression arg2 = this.arglist.get( 1 );
                Expression arg3 = null;
                if ( 2 < this.arglist.size() ) {
                        arg3 = this.arglist.get( 2 );
                }
                String varName = arg1.getVariable();
                DataItemType variableType = typeMap.get( varName );
                if ( null == variableType ) {
                        throw new IllegalArgumentException( "Missing variable " + varName + " in variables in calculation for " + name );
                }
                
                if ( variableType != DataItemType.INTEGER/* && DataItemType.Float != soFar.getType() */ ) {
                        throw new IllegalArgumentException( "Variable of wrong type for function inval for " + name );
                }
                resType = DataItemType.INTEGER;
                
                if ( ExprType.CONST != arg2.getExprType() && ExprType.CONSTLIST != arg2.getExprType() ) {
                        throw new IllegalArgumentException( "Argument for illegal value(s) of wrong type for function inval for " + name );
                }
                
                if ( null != arg3 ) {
                        arg3.prepareType( name, typeMap );
                        resType = arg3.getType();
                }
        }

}
