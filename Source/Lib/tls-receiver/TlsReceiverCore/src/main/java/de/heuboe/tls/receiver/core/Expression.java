package de.heuboe.tls.receiver.core;

import java.util.List;
import java.util.Map;

import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.DataItem.DataItemType;
import de.heuboe.tls.receiver.item.FloatItem;
import de.heuboe.tls.receiver.item.IntegerItem;
import de.heuboe.tls.receiver.item.IntegerListItem;


/**
 * Class handling arithmetic and logical expressions
 * @author Ralf Zobel / Ronald Nikel
 *
 */
public class Expression {

	private Expression ex1;
	private Expression ex2;
	private String operator;

	public Expression getEx1() {
                return ex1;
        }

        public Expression getEx2() {
                return ex2;
        }

        public String getOperator() {
                return operator;
        }

        public String getVariable() {
                return variable;
        }

        public Integer getConstVal() {
                return constVal;
        }

        public List<Integer> getConstValList() {
                return constValList;
        }

        private String variable;
	private Integer constVal;
	private List<Integer> constValList;
	
	public enum ExprType {
	        BINOP,
	        VARIABLE,
	        CONST,
	        CONSTLIST
	}
	
	private ExprType exprType;
        
        static final String ONLY_INTEGERS = "only integers expected";
	
	/**
	 * General constructor. All other constructors use this one.
	 * @param ex1 Left hand expression
	 * @param ex2 Right hand expression
	 * @param operator Operator as in Java/C++ - syntax
	 * @param variable If non-null the name of a variable in the current context
	 * @param value for constant expressions
	 */
	private Expression(Expression ex1, Expression ex2, String operator,
			String variable, Integer value, List<Integer> constValList) {
		super();
		this.ex1 = ex1;
		this.ex2 = ex2;
		this.operator = operator;
		this.variable = variable;
		this.constVal = value;
		this.constValList = constValList;
	}
	
	/**
	 * Constructor for a binary expression. E.g a + b
         * @param ex1 Left hand expression
         * @param ex2 Right hand expression
         * @param operator Operator as in Java/C++ - syntax
	 */
	public Expression(Expression ex1, Expression ex2, String operator) {
		this(ex1, ex2, operator, null, null, null);
		exprType = ExprType.BINOP;
	}

	/**
	 * Constructor which uses a variable (~ $<name>) as expression. e.g. 5 + $AnzETel
	 * @param variable The name of the variable in the current context
	 */
	public Expression(String variable) {
		this(null, null, null, variable, null, null);
		exprType = ExprType.VARIABLE;
	}
	
	/**
	 * Constructor for a constant expression
	 * @param value A constant value
	 */
	public Expression(int value) {
		this(null, null, null, null, value, null);
		exprType = ExprType.CONST;
	}
        
        /**
         * Constructor for a list of constant values
         * @param value A list constant values
         */
        public Expression(List<Integer> valList) {
                this(null, null, null, null, null, valList);
                exprType = ExprType.CONSTLIST;
        }
        
        public ExprType getExprType() {
                return exprType;
        }
	
        /**
         * Method to evaluate the defined expression
         * @param name The name for the resulting DataItem
         * @param etelVars The map of variables in the current context
         * @return The resulting DataItem
         */
        public DataItem eval( String name, Map<String, DataItem> etelVars ) {
                if ( constVal != null ) {
                        return new IntegerItem( name, constVal, 0 );
                }
                if ( variable != null ) {
                        DataItem item = etelVars.get( variable );
                        if ( item == null ) {
                                return null;
                        }
                        item = item.copy();
                        item.setName( name );
                        return item;
                }
                if (this.constValList != null) {
                        return new IntegerListItem( name, constValList );
                }
                DataItem leftItem = ex1.eval( name, etelVars );
                DataItem rightItem = ex2.eval( name, etelVars );
                if ( leftItem == null || rightItem == null ) {
                        throw new IllegalArgumentException( "unresolved expression" );
                }
                switch ( operator ) {
                case "+":
                        return addItems( leftItem, rightItem );
                case "-":
                        return subtractItems( leftItem, rightItem );
                case "*":
                        return multiplyItems( leftItem, rightItem );
                case "/":
                        return divideItems( leftItem, rightItem );
                case "&":
                        return maskItems( leftItem, rightItem );
                case "|":
                        return joinItems( leftItem, rightItem );
                case "<<":
                        return leftShiftItems( leftItem, rightItem );
                case ">>":
                        return rightShiftItems( leftItem, rightItem );
                case "==":
                        return compareItems( leftItem, rightItem, true );
                case "!=":
                        return compareItems( leftItem, rightItem, false );
                case "&&":
                        return logicAndItems( leftItem, rightItem );
                case "||":
                        return logicOrItems( leftItem, rightItem );
                case ">":
                        return greaterItems( leftItem, rightItem );
                case ">=":
                        return greaterEqualItems( leftItem, rightItem );
                case "<":
                        return lessItems( leftItem, rightItem );
                case "<=":
                        return lessEqualItems( leftItem, rightItem );
                default:
                        throw new IllegalArgumentException( "Not a valid operator: " + operator );
                }
        }

	private DataItem addItems(DataItem leftItem, DataItem rightItem) {
		check(leftItem, rightItem);
		if (leftItem.getType() == DataItemType.INTEGER && rightItem.getType() == DataItemType.INTEGER) {
			long value = leftItem.getAsLong() + rightItem.getAsLong();
			return new IntegerItem(leftItem.getName(), value, 0);
		}
		double s1 = leftItem.getType() == DataItemType.INTEGER ? (double) leftItem.getAsLong() : leftItem.getAsDouble(); 
		double s2 = rightItem.getType() == DataItemType.INTEGER ? (double) rightItem.getAsLong() : rightItem.getAsDouble(); 
		return new FloatItem(leftItem.getName(), s1+s2, 0);
	}

	private DataItem subtractItems(DataItem leftItem, DataItem rightItem) {
		check(leftItem, rightItem);
		if (leftItem.getType() == DataItemType.INTEGER && rightItem.getType() == DataItemType.INTEGER) {
			long value = leftItem.getAsLong() - rightItem.getAsLong();
			return new IntegerItem(leftItem.getName(), value, 0);
		}
		double s1 = leftItem.getType() == DataItemType.INTEGER ? (double) leftItem.getAsLong() : leftItem.getAsDouble(); 
		double s2 = rightItem.getType() == DataItemType.INTEGER ? (double) rightItem.getAsLong() : rightItem.getAsDouble(); 
		return new FloatItem(leftItem.getName(), s1-s2, 0);
	}

	private DataItem multiplyItems(DataItem leftItem, DataItem rightItem) {
		check(leftItem, rightItem);
		if (leftItem.getType() == DataItemType.INTEGER && rightItem.getType() == DataItemType.INTEGER) {
			long value = leftItem.getAsLong() * rightItem.getAsLong();
			return new IntegerItem(leftItem.getName(), value, 0);
		}
		double s1 = leftItem.getType() == DataItemType.INTEGER ? (double) leftItem.getAsLong() : leftItem.getAsDouble(); 
		double s2 = rightItem.getType() == DataItemType.INTEGER ? (double) rightItem.getAsLong() : rightItem.getAsDouble(); 
		return new FloatItem(leftItem.getName(), s1*s2, 0);
	}

	private DataItem divideItems(DataItem leftItem, DataItem rightItem) {
		check(leftItem, rightItem);
		double s1 = leftItem.getType() == DataItemType.INTEGER ? (double) leftItem.getAsLong() : leftItem.getAsDouble(); 
		double s2 = rightItem.getType() == DataItemType.INTEGER ? (double) rightItem.getAsLong() : rightItem.getAsDouble(); 
		return new FloatItem(leftItem.getName(), s1/s2, 0);
	}

	private DataItem maskItems(DataItem leftItem, DataItem rightItem) {
		check(leftItem, rightItem);
		if (leftItem.getType() != DataItemType.INTEGER || rightItem.getType() != DataItemType.INTEGER) {
			throw new IllegalArgumentException(ONLY_INTEGERS);
		}
		long s1 = leftItem.getAsLong(); 
		long s2 = rightItem.getAsLong(); 
		return new IntegerItem(leftItem.getName(), s1&s2, 0);
	}

	private DataItem joinItems(DataItem leftItem, DataItem rightItem) {
		check(leftItem, rightItem);
		if (leftItem.getType() != DataItemType.INTEGER || rightItem.getType() != DataItemType.INTEGER) {
			throw new IllegalArgumentException(ONLY_INTEGERS);
		}
		long s1 = leftItem.getAsLong(); 
		long s2 = rightItem.getAsLong(); 
		return new IntegerItem(leftItem.getName(), s1|s2, 0);
	}

	private DataItem leftShiftItems(DataItem leftItem, DataItem rightItem) {
		check(leftItem, rightItem);
		if (leftItem.getType() != DataItemType.INTEGER || rightItem.getType() != DataItemType.INTEGER) {
			throw new IllegalArgumentException(ONLY_INTEGERS);
		}
		long s1 = leftItem.getAsLong(); 
		long s2 = rightItem.getAsLong(); 
		return new IntegerItem(leftItem.getName(), s1 << s2, 0);
	}

	private DataItem rightShiftItems(DataItem leftItem, DataItem rightItem) {
		check(leftItem, rightItem);
		if (leftItem.getType() != DataItemType.INTEGER || rightItem.getType() != DataItemType.INTEGER) {
			throw new IllegalArgumentException(ONLY_INTEGERS);
		}
		long s1 = leftItem.getAsLong(); 
		long s2 = rightItem.getAsLong(); 
		return new IntegerItem(leftItem.getName(), s1 >> s2, 0);
	}

	private DataItem compareItems(DataItem leftItem, DataItem rightItem, boolean equal) {
		check(leftItem, rightItem);
		if (leftItem.getType() != DataItemType.INTEGER || rightItem.getType() != DataItemType.INTEGER) {
			throw new IllegalArgumentException(ONLY_INTEGERS);
		}
		long s1 = leftItem.getAsLong(); 
		long s2 = rightItem.getAsLong(); 
		long value = (s1 == s2) ? 1 : 0;
		if (!equal) {
			value = (s1 != s2) ? 1 : 0;
		}
		return new IntegerItem(leftItem.getName(), value, 0);
	}

	private DataItem logicAndItems(DataItem leftItem, DataItem rightItem) {
		check(leftItem, rightItem);
		if (leftItem.getType() != DataItemType.INTEGER || rightItem.getType() != DataItemType.INTEGER) {
			throw new IllegalArgumentException(ONLY_INTEGERS);
		}
		long s1 = leftItem.getAsLong(); 
		long s2 = rightItem.getAsLong(); 
		long value = (s1 != 0 && s2 != 0) ? 1 : 0;
		return new IntegerItem(leftItem.getName(), value, 0);
	}

	private DataItem logicOrItems(DataItem leftItem, DataItem rightItem) {
		check(leftItem, rightItem);
		if (leftItem.getType() != DataItemType.INTEGER || rightItem.getType() != DataItemType.INTEGER) {
			throw new IllegalArgumentException(ONLY_INTEGERS);
		}
		long s1 = leftItem.getAsLong(); 
		long s2 = rightItem.getAsLong(); 
		long value = (s1 != 0 || s2 != 0) ? 1 : 0;
		return new IntegerItem(leftItem.getName(), value, 0);
	}
	
// ------------------------------------

        private DataItem lessItems(DataItem leftItem, DataItem rightItem) {
                check(leftItem, rightItem);
                if (leftItem.getType() == DataItemType.INTEGER && rightItem.getType() == DataItemType.INTEGER) {
                        long s1 = leftItem.getAsLong(); 
                        long s2 = rightItem.getAsLong(); 
                        long value = (s1 < s2) ? 1 : 0;
                        return new IntegerItem(leftItem.getName(), value, 0);
                }
                double s1 = leftItem.getType() == DataItemType.INTEGER ? (double) leftItem.getAsLong() : leftItem.getAsDouble(); 
                double s2 = rightItem.getType() == DataItemType.INTEGER ? (double) rightItem.getAsLong() : rightItem.getAsDouble(); 
                long value = (s1 < s2) ? 1 : 0;
                return new IntegerItem(leftItem.getName(), value, 0);
        }

        private DataItem lessEqualItems(DataItem leftItem, DataItem rightItem) {
                check(leftItem, rightItem);
                if (leftItem.getType() == DataItemType.INTEGER && rightItem.getType() == DataItemType.INTEGER) {
                        long s1 = leftItem.getAsLong(); 
                        long s2 = rightItem.getAsLong(); 
                        long value = (s1 <= s2) ? 1 : 0;
                        return new IntegerItem(leftItem.getName(), value, 0);
                }
                double s1 = leftItem.getType() == DataItemType.INTEGER ? (double) leftItem.getAsLong() : leftItem.getAsDouble(); 
                double s2 = rightItem.getType() == DataItemType.INTEGER ? (double) rightItem.getAsLong() : rightItem.getAsDouble(); 
                long value = (s1 <= s2) ? 1 : 0;
                return new IntegerItem(leftItem.getName(), value, 0);
        }

        private DataItem greaterItems(DataItem leftItem, DataItem rightItem) {
                check(leftItem, rightItem);
                if (leftItem.getType() == DataItemType.INTEGER && rightItem.getType() == DataItemType.INTEGER) {
                        long s1 = leftItem.getAsLong(); 
                        long s2 = rightItem.getAsLong(); 
                        long value = (s1 > s2) ? 1 : 0;
                        return new IntegerItem(leftItem.getName(), value, 0);
                }
                double s1 = leftItem.getType() == DataItemType.INTEGER ? (double) leftItem.getAsLong() : leftItem.getAsDouble(); 
                double s2 = rightItem.getType() == DataItemType.INTEGER ? (double) rightItem.getAsLong() : rightItem.getAsDouble(); 
                long value = (s1 > s2) ? 1 : 0;
                return new IntegerItem(leftItem.getName(), value, 0);
        }

        private DataItem greaterEqualItems(DataItem leftItem, DataItem rightItem) {
                check(leftItem, rightItem);
                if (leftItem.getType() == DataItemType.INTEGER && rightItem.getType() == DataItemType.INTEGER) {
                        long s1 = leftItem.getAsLong(); 
                        long s2 = rightItem.getAsLong(); 
                        long value = (s1 >= s2) ? 1 : 0;
                        return new IntegerItem(leftItem.getName(), value, 0);
                }
                double s1 = leftItem.getType() == DataItemType.INTEGER ? (double) leftItem.getAsLong() : leftItem.getAsDouble(); 
                double s2 = rightItem.getType() == DataItemType.INTEGER ? (double) rightItem.getAsLong() : rightItem.getAsDouble(); 
                long value = (s1 >= s2) ? 1 : 0;
                return new IntegerItem(leftItem.getName(), value, 0);
        }

//-------------------------------------	
	
	
	private void check(DataItem leftItem, DataItem rightItem) {
		if (leftItem.getType() != DataItemType.INTEGER && leftItem.getType() == DataItemType.FLOAT) {
			throw new IllegalArgumentException("integers or float values expected");
		}
		if (rightItem.getType() != DataItemType.INTEGER && rightItem.getType() == DataItemType.FLOAT) {
			throw new IllegalArgumentException("integers or float values expected");
		}
	}
}
