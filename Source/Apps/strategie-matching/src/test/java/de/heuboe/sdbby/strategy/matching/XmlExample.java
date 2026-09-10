package de.heuboe.sdbby.strategy.matching;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.heuboe.sdbby.strategy.matching.data.Condition;
import de.heuboe.sdbby.strategy.matching.data.DeContent;
import de.heuboe.sdbby.strategy.matching.data.Requirement;
import de.heuboe.sdbby.strategy.matching.data.Rule;
import de.heuboe.sdbby.strategy.matching.data.RuleSet;

public class XmlExample {

	private static final int SEITE_1 = 241;
	private static final int SEITE_2 = 242;
	private static final int SEITE_3 = 243;
	//	private static final int SEITE_4 = 244;

	@Test
	public void test() {
		RuleSet ruleSet = new RuleSet();
		Rule rule;
		Condition condition;
		List<Condition> conditionList;

		rule = new Rule();
		rule.setName("Rule1-S-05");
		rule.setStrategy("S-05");
		conditionList = new ArrayList<>();
		condition = new Condition("S-05-92_3720", null);
		condition.getDeContent().add(new DeContent("WZG_WWQ_92_3720_226", null,SEITE_3,null));
		condition.getDeContent().add(new DeContent("WZG_WWQ_92_3720_227", null, SEITE_2,null));
		condition.getDeContent().add(new DeContent("WZG_WWQ_92_3720_228", null, SEITE_3,null));
		conditionList.add(condition);
		rule.getRequirement().add(new Requirement("S-05-WWK_92_3720", 65, conditionList));

		conditionList = new ArrayList<>();
		condition = new Condition("S-05-99_589", null);
		condition.getDeContent().add(new DeContent("WZG_WWQ_99_589_1", null, SEITE_1,null));
		condition.getDeContent().add(new DeContent("WZG_WWQ_99_589_2", null, SEITE_2,null));
		conditionList.add(condition);
		condition = new Condition("S-05-99_587", null);
		condition.getDeContent().add(new DeContent("WZG_WWQ_99_587_1", null, SEITE_1,null));
		condition.getDeContent().add(new DeContent("WZG_WWQ_99_587_2", null, SEITE_2,null));
		conditionList.add(condition);
		rule.getRequirement().add(new Requirement("S-05-9250004", 65, conditionList));

		ruleSet.getRule().add(rule);

		new RuleMan().saveRuleSetXML(ruleSet, "src/test/resources/rules/rules2.xml");
		new RuleMan().saveRuleSetCSV(ruleSet, "rules2.txt");
	}

	@Test
	public void test3() throws IOException {
		RuleSet ruleSet = RuleReader.readRuleSetCSV( "src/main/resources/rules/StrategieRegeln_v0.19.txt" ).getRuleSet();
		assertEquals( 29,  ruleSet.getRule().size());
	}


}
