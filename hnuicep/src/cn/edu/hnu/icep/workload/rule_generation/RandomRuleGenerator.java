package cn.edu.hnu.icep.workload.rule_generation;

import cn.edu.hnu.icep.rules.EventConstraint;
import cn.edu.hnu.icep.rules.NegationConstraint;
import cn.edu.hnu.icep.rules.SequenceConstraint;
import cn.edu.hnu.icep.rules.filtering.Predicate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.edu.hnu.icep.workload.rule_generation.RuleGenerator;
import cn.edu.hnu.icep.eval.ParamHandler;
import cn.edu.hnu.icep.rules.Rule;

public class RandomRuleGenerator extends RuleGenerator {

	/**
	 * 产生复杂事件规则
	 * **/
	@Override
	public Rule generateRule(ParamHandler ph) {
		Set<String> alreadySelectedTypes = new HashSet<String>();
		long win = getWinSize(ph);
		System.out.println("rule win = " + win);
		Rule rule = new Rule(win);

		List<EventConstraint> evConstraints = new ArrayList<EventConstraint>();
		List<NegationConstraint> negConstraints = new ArrayList<NegationConstraint>();
		List<SequenceConstraint> seqConstraints = new ArrayList<SequenceConstraint>();

		// ph.getNumEventConstraints()中numEventConstraints的大小，通常会在EvalRunner的
		// run***()方法中设置。
		for (int i = 0; i < ph.getNumEventConstraints(); i++) {
			createEventConstraint(ph, evConstraints, alreadySelectedTypes);
		}
		
		createSequenceConstraints(ph, evConstraints, seqConstraints);

		// 默认情况下，getNumNegationConstraints()的值为0。
		for (int i = 0; i < ph.getNumNegationConstraints(); i++) {
			// 注意点：创建出的否定约束中 所包含的事件，一定不能包含 上面所创建约束中已有的事件，否则就矛盾。
			createNegationConstraint(ph, negConstraints, alreadySelectedTypes);
		}

		addEventConstraints(rule, evConstraints);
		addNegConstraints(rule, negConstraints);
		addSequenceConstraint(rule, seqConstraints);
		return rule;
	}

	/**
	 * 创建事件类型 约束规则
	 * **/
	private void createEventConstraint(ParamHandler ph,
			List<EventConstraint> evConstraints,
			Set<String> alreadySelectedTypes) {
		
		Predicate pred = getPredicate(ph, alreadySelectedTypes);

		EventConstraint constr = new EventConstraint(pred);
		evConstraints.add(constr);
	}

	/**
	 * @return 返回规则的窗口时间大小 根据RandomRuleGenerator的父类RuleGenerator的getValue()，默认返回窗口的大小为： 
	 * value = (minVal == maxVal) ? minVal : minVal + r.nextInt((int) (maxVal - minVal));
	 **/
	private final long getWinSize(ParamHandler ph) {
		System.out.println("ph.getMinWinSize() = " + ph.getMinWinSize());
		System.out.println("ph.getMaxWinSize() = " + ph.getMaxWinSize());
		return getValue(ph.getMinWinSize(), ph.getMaxWinSize(),ph.getWinDistribution(), ph.getRandom());
	}
	
	/**
	 * 根据之前创建的 EventConstraint的链表，来创建出 顺序约束
	 * @author hduser
	 * **/
	private final void createSequenceConstraints(ParamHandler ph,
			List<EventConstraint> evConstraints,
			List<SequenceConstraint> seqConstraints) {
		// TODO: introduce windows => 暂时还未引入win，默认为0
		
		// ph.getSeqProbability() 在EvalRunner的 run***()方法中具体设置。
		//在目前系统中设定的 seqProbability 的值都很小，默认为0，最大是1.
		int seqProbability = ph.getSeqProbability();
		
		for (int i = 0; i < evConstraints.size() - 1; i++) {
			if (ph.getRandom().nextInt(100) < seqProbability) {
				EventConstraint refEvent = evConstraints.get(i);
				EventConstraint followingEvent = evConstraints.get(i + 1);
				SequenceConstraint constr = new SequenceConstraint(refEvent,followingEvent);
				seqConstraints.add(constr);
			}
		}
	}
	
	/**
	 * 创建一个否定约束
	 * @author hduser
	 * **/
	private final void createNegationConstraint(ParamHandler ph,
			List<NegationConstraint> negConstraints,
			Set<String> alreadySelectedTypes) {
		
		Predicate pred = getPredicate(ph, alreadySelectedTypes);
		
		NegationConstraint constr = new NegationConstraint(pred);
		negConstraints.add(constr);
	}
	
	/**
	 * 向rule添加入 EventConstraint 约束
	 * **/
	private final void addEventConstraints(Rule rule,
			List<EventConstraint> evConstraints) {
		for (EventConstraint constr : evConstraints) {
			rule.addEvent(constr);
		}
	}
	
	/**
	 * 向rule添加入 NegationConstraint 约束
	 * **/
	private final void addNegConstraints(Rule rule,
			List<NegationConstraint> negConstraints) {
		for (NegationConstraint constr : negConstraints) {
			rule.addNegation(constr);
		}
	}
	
	/**
	 * 向rule添加入 NegationConstraint 约束
	 * **/
	private final void addSequenceConstraint(Rule rule,
			List<SequenceConstraint> seqConstraints) {
		for (SequenceConstraint constr : seqConstraints) {
			rule.addSequenceConstraint(constr.getReferenceEvent(),
					constr.getFollowingEvent(), constr.getWin());
		}
	}
	
}
