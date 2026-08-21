package de.heuboe.tls.grammar.sequencer;

import de.heuboe.tls.grammar.interfaces.Expression;
import de.heuboe.tls.grammar.interfaces.Filler;
import de.heuboe.tls.grammar.interfaces.Result;
import de.heuboe.tls.grammar.interfaces.Variable;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class represents a switch case statement that contains one or more case statements.
 */
public class SwitchCaseStatement implements Filler {

    @Getter @Setter
    private Expression switchCondition;

    @Getter
    private List<CaseStatement> caseStatements = new ArrayList<>();

    /**
     * This method adds a case statement of the SwitchStatement.
     *
     * @param caseStatement The case statement as Filler object that should be added.
     */
    public void addCaseBlock(CaseStatement caseStatement) {
        this.caseStatements.add(caseStatement);
    }

    @Override
    public int execute(Result result, int ptr, Object inputData, Map<String, Variable> variableTable) {
        int adv = 0;
        for (CaseStatement caseStatement : caseStatements) {
            // if the case statement has a break -1 will be returned
            adv = caseStatement.execute(result, ptr, inputData, variableTable);
            if (adv == -1) {
                return adv;
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
        return "switch";
    }

}
