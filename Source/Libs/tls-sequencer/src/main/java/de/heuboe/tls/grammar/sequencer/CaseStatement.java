package de.heuboe.tls.grammar.sequencer;

import de.heuboe.tls.grammar.base.CompoundBlock;
import de.heuboe.tls.grammar.base.Condition;
import de.heuboe.tls.grammar.base.Operator;
import de.heuboe.tls.grammar.interfaces.Expression;
import de.heuboe.tls.grammar.interfaces.Filler;
import de.heuboe.tls.grammar.interfaces.Result;
import de.heuboe.tls.grammar.interfaces.Variable;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * This class represents a case statement that is part of a switch statement.
 */
public class CaseStatement implements Filler {

    @Setter
    private Condition condition;

    @Setter
    private CompoundBlock caseBlock;

    @Setter
    private boolean breakable;

    /**
     * The constructor that creates a CaseStatement with the required conditions and a list of Filler objects.
     *
     * @param switchExpr The expression from the switch context.
     * @param caseExpr   The expression from the case context.
     * @param caseBlock  The case block as a list of Filler objects.
     * @param breakable  A flag that determine if this case statement contains a break or not.
     */
    public CaseStatement(Expression switchExpr, Expression caseExpr, List<Filler> caseBlock, boolean breakable) {
        this.condition = new Condition(new Operator.RelOp.Equal(switchExpr, caseExpr));
        this.caseBlock = new CompoundBlock("caseBlock", caseBlock);
        this.breakable = breakable;
    }

    @Override
    public int execute(Result result, int ptr, Object inputData, Map<String, Variable> variableTable) {
        int adv = 0;
        if (condition.isTrue(inputData, variableTable)) {
            adv = caseBlock.execute(result, ptr, inputData, variableTable);
            if (breakable) {
                return -1;
            }
        }
        return adv;
    }

    @Override
    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getOperandName() {
        return "case";
    }
}
