package cn.edu.hnu.icep.workload.rule_generation;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import cn.edu.hnu.icep.rules.filtering.Filter;
import cn.edu.hnu.icep.rules.filtering.Constraint;
import cn.edu.hnu.icep.common.ConstraintOp;
import cn.edu.hnu.icep.common.Consts;
import cn.edu.hnu.icep.event.model.Value;
import cn.edu.hnu.icep.common.Distribution;
import cn.edu.hnu.icep.workload.rule_generation.RandomRuleGenerator;
import cn.edu.hnu.icep.workload.use_cases.dublinked.DublinkedWLGen;
import cn.edu.hnu.icep.eval.ParamHandler;
import cn.edu.hnu.icep.rules.Rule;
import cn.edu.hnu.icep.rules.filtering.Predicate;

public abstract class RuleGenerator {

	public abstract Rule generateRule(ParamHandler ph);

	public static Rule getRule(ParamHandler ph) {
		switch (ph.getRuleGeneratorType()) {
		case RANDOM:
			return new RandomRuleGenerator().generateRule(ph);
		case DUBLINKED:
			return DublinkedWLGen.generateRule(ph);
		default:
			assert false : ph.getRuleGeneratorType();
			return new RandomRuleGenerator().generateRule(ph);
		}
	}

	public long getValue(long minVal, long maxVal, Distribution distrib, Random r) {
		long value = 0;
		switch (distrib) {
		case UNIFORM:
			value = (minVal == maxVal) ? minVal : minVal + r.nextInt((int)(maxVal - minVal));
			break;
		case NORMAL:
			break;
		case ZIPF:
			break;
		default:
			assert false : distrib;
		}
		return value;
	}
	
	public Predicate getPredicate(ParamHandler ph,
			Set<String> alreadySelectedTypes) {
		List<Filter> filters = new LinkedList<Filter>();
		// ph.getNumFilters(),默认为1
		for (int i = 0; i < ph.getNumFilters(); i++) {
			Filter f = getFilter(ph);
			filters.add(f);
		}

		// predicateType的值，例如：Event12 => 其中Event是事件前缀，12是根据事件类型的个数，随机产生出来。
		String predicateType = getPredicateType(ph, alreadySelectedTypes);

		// filter是生成的 属性约束 集合。
		Predicate predicate = new Predicate(predicateType, filters);
		return predicate;
	}
	
	/** @return Filter是属性约束的集合。
	 * getFilter就是返回生成的属性约束集合。每条约束的内容是: (属性名+运算符+属性值) 
	 * **/
	private Filter getFilter(ParamHandler ph) {
		Set<Constraint> constraints = new HashSet<Constraint>();
		for (int i = 0; i < ph.getNumConstraints(); i++) {
			Constraint c = getConstraint(i, ph);
			constraints.add(c);
		}
		Filter filter = new Filter(constraints);
		return filter;
	}
	
	/** @return 返回一条约束(Constraint类对象)。
	 * 这条约束的内容包括： 属性名+运算符+属性值。
	 *  **/
	private Constraint getConstraint(int constraintNum, ParamHandler ph) {

		// ph.getNumValues()默认为100
		int numValues = ph.getNumValues();
		ConstraintOp op = createOperator(ph);

		//返回0 - numValues之间的随机数
		int value = (int) getValue(0, numValues, ph.getValueDistribution(),
				ph.getRandom());

		return new Constraint(Consts.ATTRIBUTE_TYPE_PREFIX + constraintNum, op,
				new Value(value));
	}
	
	/**
	 * @return 按照20:40:40的比例，随机返回 =，>=,<=
	 * **/
	private ConstraintOp createOperator(ParamHandler ph) {
		int perc = ph.getRandom().nextInt(100);

		/*默认设定：
		 * percEQ = 20;
		 * percLT = 40;
		 * percGT = 40;*/
		int percEQ = ph.getPercEQ();
		int percGT = ph.getPercGT();
		int percLT = ph.getPercLT();
		
		if (perc < percEQ)
			return ConstraintOp.EQ;
		if (perc < percEQ + percGT)
			return ConstraintOp.GT_EQ;
		if (perc < percEQ + percGT + percLT)
			return ConstraintOp.LT_EQ;
		return ConstraintOp.DF;
	}
	
	/**
	 * @return 随机返回一个predicate 所对应的事件类型。
	 * **/
	private String getPredicateType(ParamHandler ph,Set<String> alreadySelectedTypes) {
		String type = "";
		// do-while循环 => 
		// 先执行语句，在判断while条件。条件为假时(这个事件类型 不属于 alreadySelectedTypes)，退出循环。
		do {
			// ph.getNumEventTypes() 默认情况下等于25
			int numTypes = ph.getNumEventTypes();
			int typeId = (int) getValue(0, numTypes,ph.getEventTypesDistribution(), ph.getRandom());
			/*System.out.println();
			System.out.println("ph.getNumEventTypes() = " + numTypes);
			System.out.println("in the function of getPredicateType,class RuleGenerator, the typeId = " + typeId);
			System.out.println();*/
			type = Consts.EVENT_TYPE_PREFIX + String.valueOf(typeId);
		} while (alreadySelectedTypes.contains(type));
		alreadySelectedTypes.add(type);
		return type;
	}
	
}
