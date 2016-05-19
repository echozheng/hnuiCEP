package cn.edu.hnu.icep.eval;

import cn.edu.hnu.icep.rules.SequenceConstraint;
import cn.edu.hnu.icep.rules.NegationConstraint;
import cn.edu.hnu.icep.rules.EventConstraint;
import cn.edu.hnu.icep.rules.Rule;
import cn.edu.hnu.icep.learning.ConstraintsSet;

public class LearnedRuleGenerator {
	
	public static Rule generateRuleFromConstraints(ConstraintsSet constraints, long win) {
		Rule rule = new Rule(win);
		addEventConstraints(rule, constraints);
		addNegationConstraints(rule, constraints);
		addSequenceConstaints(rule, constraints);
		return rule;
	}

	public static void addEventConstraints(Rule rule,ConstraintsSet constraints) {
		for (EventConstraint eventConstraint : constraints.getEventConstraints()) {
			rule.addEvent(eventConstraint);
		}
	}
	
	private static void addNegationConstraints(Rule rule,ConstraintsSet constraints) {
		for (NegationConstraint negConstraint : constraints.getNegationConstraints()) {
			rule.addNegation(negConstraint);
		}
	}
	
	private static void addSequenceConstaints(Rule rule,ConstraintsSet constraints) {

		for (SequenceConstraint seqConstraint : constraints.getSequenceConstraints()) {
			rule.addSequenceConstraint(seqConstraint.getReferenceEvent(),
										seqConstraint.getFollowingEvent(),
										seqConstraint.getWin());
		}
	}
}
