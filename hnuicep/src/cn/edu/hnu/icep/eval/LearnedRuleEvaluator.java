package cn.edu.hnu.icep.eval;

import cn.edu.hnu.icep.event.model.Event;
import cn.edu.hnu.icep.history.History;
import cn.edu.hnu.icep.rules.Rule;
import cn.edu.hnu.icep.eval.EvalStatistics;
import cn.edu.hnu.icep.eval.SyntacticDifferenceStatistics;
import cn.edu.hnu.icep.rules.EventConstraint;
import cn.edu.hnu.icep.rules.filtering.Predicate;
import cn.edu.hnu.icep.rules.filtering.Constraint;
import cn.edu.hnu.icep.rules.filtering.Filter;
import cn.edu.hnu.icep.common.ConstraintOp;
import cn.edu.hnu.icep.eval.ParamHandler;
import cn.edu.hnu.icep.rules.NegationConstraint;
import cn.edu.hnu.icep.rules.SequenceConstraint;
import cn.edu.hnu.icep.rules.RuleConstraint;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LearnedRuleEvaluator {
	
	//rule是制定的规则，learnedRule是学习之后的规则。
	public static final EvalStatistics evaluateLearnedRule(History history,Rule rule, Rule learnedRule) {

		int realEvents = 0;
		int detectedEvents = 0;
		int falsePositives = 0;
		int falseNegatives = 0;

		long ruleWin = rule.getWin();
		long learnedRuleWin = learnedRule.getWin();

		for (Event e : history.getPrimitiveEvents()) {

			long refTime = e.getTimestamp();

			long ruleStartTime = (refTime - ruleWin > 0) ? (refTime - ruleWin) : 0;
			long learnedRuleStartTime = (refTime - learnedRuleWin > 0) ? (refTime - learnedRuleWin) : 0;
			Collection<Event> ruleWinEvents = history.getAllEventsInWindow(ruleStartTime, refTime);
			Collection<Event> learnedRuleWinEvents = history.getAllEventsInWindow(learnedRuleStartTime, refTime);
			boolean real = rule.isSatisfiedBy(ruleWinEvents, refTime);
			boolean detected = learnedRule.isSatisfiedBy(learnedRuleWinEvents,refTime);

			if (real) {
				realEvents++;
				if (!detected) {
					falseNegatives++;
				}
			}

			if (detected) {
				detectedEvents++;
				if (!real) {
					falsePositives++;
				}
			}
		}

		return new EvalStatistics(realEvents, detectedEvents, falsePositives,falseNegatives);
	}
	
	public static final SyntacticDifferenceStatistics computeSyntacticDifferences(
							Rule rule, Rule derivedRule, ParamHandler ph) {
		double typeDistance = computeTypeDistance(rule, derivedRule);
		double attributeDistance = computeAttributeDistance(rule, derivedRule);
		double predicateDistance = computePredicateDistance(rule, derivedRule,ph);
		
		double winDistance = computeWinDistance(rule, derivedRule);
		double sequenceDistance = computeSequenceDistance(rule, derivedRule);
		double negationDistance = computeNegationDistance(rule, derivedRule);
		
		return new SyntacticDifferenceStatistics(typeDistance,
				attributeDistance, predicateDistance, winDistance,
				100.0, 100.0, sequenceDistance,
				negationDistance);
	}
	
	private static double computeTypeDistance(Rule rule, Rule derivedRule) {
		double result = 0;
		Set<String> ruleTypes = getAllTypesIn(rule);
		Set<String> derivedRuleTypes = getAllTypesIn(derivedRule);
		for (String ruleType : ruleTypes) {
			if (!derivedRuleTypes.contains(ruleType)) {
				result++;
			}
		}
		for (String derivedRuleType : derivedRuleTypes) {
			if (!ruleTypes.contains(derivedRuleType)) {
				result++;
			}
		}
		return result;
	}
	
	private static final Set<String> getAllTypesIn(Rule rule) {
		Set<String> result = new HashSet<String>();
		for (EventConstraint constr : rule.getEventConstraints()) {
			result.add(constr.getPredicate().getEventType());
		}
		for (NegationConstraint neg : rule.getNegationConstraints()) {
			result.add(neg.getPredicate().getEventType());
		}
		return result;
	}
	
	private static double computeAttributeDistance(Rule rule, Rule derivedRule) {
		double result = 0;
		Set<EventAttributes> ruleAttributes = getAllEventAttributesIn(rule);
		Set<EventAttributes> derivedRuleAttributes = getAllEventAttributesIn(derivedRule);
		for (EventAttributes ruleType : ruleAttributes) {
			if (!derivedRuleAttributes.contains(ruleType)) {
				result++;
			}
		}
		for (EventAttributes derivedRuleAttribute : derivedRuleAttributes) {
			if (!ruleAttributes.contains(derivedRuleAttribute))
				result++;
		}
		return result;
	}
	
	private static Set<EventAttributes> getAllEventAttributesIn(Rule rule) {
		Set<EventAttributes> result = new HashSet<EventAttributes>();
		for (EventConstraint ev : rule.getEventConstraints()) {
			getAllEventAttributedIn(ev.getPredicate(), result);
		}
		for (NegationConstraint neg : rule.getNegationConstraints()) {
			getAllEventAttributedIn(neg.getPredicate(), result);
		}
		return result;
	}
	
	private static void getAllEventAttributedIn(Predicate pred,
			Set<EventAttributes> eventAttributes) {
		String type = pred.getEventType();
		Set<String> attrNames = new HashSet<String>();
		for (Constraint c : pred.getFilter()) {
			attrNames.add(c.getName());
		}
		EventAttributes evAttrs = new EventAttributes(type, attrNames);
		eventAttributes.add(evAttrs);
	}
	
	private static double computePredicateDistance(ParamHandler ph, Predicate p1, Predicate p2) {
	    Filter f1 = p1.getFilter();
	    Filter f2 = p2.getFilter();
	    return computeFiltersDistance(ph, f1, f2);
    }
	
	private static double computeFiltersDistance(ParamHandler ph,Filter filter, Filter learnedFilter) {
		double commonArea = computeCommonArea(ph, filter, learnedFilter);
		if (commonArea == 0) {
			return 1;
		}
		double realArea = computeArea(ph, filter);
		double learnedArea = computeArea(ph, learnedFilter);
		double wrongArea = realArea + learnedArea - 2 * commonArea;
		return wrongArea / (realArea + learnedArea);
	}
	
	private static double computeCommonArea(ParamHandler ph,Filter filter, Filter learnedFilter) {
		Set<String> constrNames = extractConstraintsNames(filter);
		Set<String> learnedConstrNames = extractConstraintsNames(learnedFilter);
		if (constrNames.size() != learnedConstrNames.size()) {
			return 0;
		}
		if (!constrNames.containsAll(learnedConstrNames)) {
			return 0;
		}
		double result = 1;
		for (String name : constrNames) {
			long left = Math.max(getMin(filter, name),getMin(learnedFilter, name));
			long right = Math.min(getMax(ph, filter, name),getMax(ph, learnedFilter, name));
			if (left > right) {
				return 0;
			}
			result *= (right - left);
		}
		return result;
	}

	private static double computeArea(ParamHandler ph, Filter filter) {
		Set<String> constrNames = extractConstraintsNames(filter);
		double result = 1;
		for (String name : constrNames) {
			result *= getMax(ph, filter, name) - getMin(filter, name);
		}
		return result;
	}
	
	private static Set<String> extractConstraintsNames(Filter filter) {
		Set<String> names = new HashSet<String>();
		for (Constraint c : filter) {
			names.add(c.getName());
		}
		return names;
	}

	private static long getMax(ParamHandler ph, Filter filter, String attr) {
		for (Constraint c : filter) {
			if (c.getName().equals(attr) && c.getOperator() == ConstraintOp.LT_EQ) {
				return c.getValue().getLongValue();
			}
		}
		return ph.getNumValues();
	}

	private static long getMin(Filter filter, String attr) {
		for (Constraint c : filter) {
			if (c.getName().equals(attr) && c.getOperator() == ConstraintOp.GT_EQ) {
				return c.getValue().getLongValue();
			}
		}
		return 0;
	}
	
	// FIXME: what if I have multiple events with the same constraint?
	private static double computePredicateDistance(Rule rule, Rule derivedRule, ParamHandler ph) {
		double count = 0;
		double sum = 0;
		List<EventConstraint> ruleEvConstraints = rule.getEventConstraints();
		List<EventConstraint> derivedRuleEvConstraints = derivedRule.getEventConstraints();
		for (EventConstraint ruleEvConstraint : ruleEvConstraints) {
			Predicate rulePred = ruleEvConstraint.getPredicate();
			for (EventConstraint derivedRuleEvConstraint : derivedRuleEvConstraints) {
				Predicate derivedRulePred = derivedRuleEvConstraint.getPredicate();
				if (!areCompatible(rulePred, derivedRulePred)) {
					continue;
				}
				sum += computePredicateDistance(ph, rulePred, derivedRulePred);
				count++;
			}
		}
		return (count == 0) ? 0 : sum / count;
	}
	
	private static boolean areCompatible(Predicate p1, Predicate p2) {
		if (!p1.getEventType().equals(p2.getEventType())) {
			return false;
		}
		return true;
	}
	
	private static double computeWinDistance(Rule rule, Rule derivedRule) {
		return Math.abs(rule.getWin() - derivedRule.getWin());
	}
	
	private static double computeSequenceDistance(Rule rule, Rule derivedRule) {
		double result = 0;
		Set<SequenceConstraint> ruleSeqConstraints = getAllSequenceConstraints(rule);
		Set<SequenceConstraint> derivedRuleSeqConstraints = getAllSequenceConstraints(derivedRule);
		result += getNumElementsNotContained(ruleSeqConstraints,derivedRuleSeqConstraints);
		result += getNumElementsNotContained(derivedRuleSeqConstraints,ruleSeqConstraints);
		return result;
	}

	private static double computeNegationDistance(Rule rule, Rule derivedRule) {
		double result = 0;
		List<NegationConstraint> ruleNegs = rule.getNegationConstraints();
		List<NegationConstraint> derivedRuleNegs = derivedRule.getNegationConstraints();
		result += getNumNegationsNotContained(ruleNegs, derivedRuleNegs);
		result += getNumNegationsNotContained(derivedRuleNegs, ruleNegs);
		return result;
	}
	
	private static Set<SequenceConstraint> getAllSequenceConstraints(Rule rule) {
		Set<SequenceConstraint> result = new HashSet<SequenceConstraint>();
		Map<EventConstraint, Map<EventConstraint, SequenceConstraint>> seqMap = rule.getSequenceConstraints();
		for (EventConstraint c1 : seqMap.keySet()) {
			Map<EventConstraint, SequenceConstraint> innerMap = seqMap.get(c1);
			for (EventConstraint c2 : innerMap.keySet()) {
				result.add(innerMap.get(c2));
			}
		}
		return result;
	}
	
	private static double getNumElementsNotContained(
				Collection<? extends RuleConstraint> elementsToBeContained,
				Collection<? extends RuleConstraint> set) {
		double count = 0;
		for (RuleConstraint c : elementsToBeContained) {
			if (!isElementContainedInSet(c, set)) {
				count++;
			}
		}
		return count;
	}
	
	private static boolean isElementContainedInSet(
			RuleConstraint element, Collection<? extends RuleConstraint> set) {
		for (RuleConstraint c : set) {
			if (element.isIdenticalTo(c)) {
				return true;
			}
		}
		return false;
	}
	
	private static double getNumNegationsNotContained(
			Collection<NegationConstraint> negationsToBeContained,
			Collection<NegationConstraint> set) {
		double count = 0;
		for (NegationConstraint c : negationsToBeContained) {
			if (!isNegationContainedInSet(c, set)) {
				count++;
			}
		}
		return count;
	}
	
	private static boolean isNegationContainedInSet(
			NegationConstraint negation, Collection<NegationConstraint> set) {
		Predicate negPred = negation.getPredicate();
		for (NegationConstraint c : set) {
			Predicate cPred = c.getPredicate();
			if (areCompatible(negPred, cPred)) {
				return true;
			}
		}
		return false;
	}
	
}

class EventAttributes {
	private final String type;
	private final Set<String> attributesNames;

	EventAttributes(String type, Set<String> attributesNames) {
		this.type = type;
		this.attributesNames = attributesNames;
	}

	final String getType() {
		return type;
	}

	final Set<String> getAttributesNames() {
		return attributesNames;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributesNames == null) ? 0 : attributesNames.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof EventAttributes))
			return false;
		EventAttributes other = (EventAttributes) obj;
		if (!type.equals(other.type))
			return false;
		if (!attributesNames.equals(other.attributesNames))
			return false;
		return true;
	}

}

