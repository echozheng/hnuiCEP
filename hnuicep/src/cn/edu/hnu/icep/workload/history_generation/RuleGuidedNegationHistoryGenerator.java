package cn.edu.hnu.icep.workload.history_generation;

import cn.edu.hnu.icep.event.model.Event;
import cn.edu.hnu.icep.rules.NegationConstraint;
import cn.edu.hnu.icep.common.Distribution;
import cn.edu.hnu.icep.event.model.Attribute;
import cn.edu.hnu.icep.rules.filtering.Constraint;
import cn.edu.hnu.icep.rules.filtering.Filter;
import cn.edu.hnu.icep.rules.filtering.Predicate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.edu.hnu.icep.rules.Rule;
import cn.edu.hnu.icep.eval.ParamHandler;
import cn.edu.hnu.icep.history.History;
import cn.edu.hnu.icep.workload.history_generation.RuleGuidedHistoryGenerator;
import cn.edu.hnu.icep.zql.update.AssistDebug;

public class RuleGuidedNegationHistoryGenerator extends RuleGuidedHistoryGenerator {

	RuleGuidedNegationHistoryGenerator(Rule rule) {
		super(rule);
	}

	public void decorateHistory(History history, ParamHandler ph) {

		System.out.println("history's maxTimestamp : " + history.getMaximumTimestamp());
		long distance = ph.getMaxWinSize() * ph.getNumEventConstraints() * 2;
		long startingFrom = history.getMaximumTimestamp();
		
		long currentTimestamp = startingFrom + distance;
		for (int i = 0; i < ph.getNumArtificialNegationsInHistory(); i++) {
			//调用父类的方法
			makeSpecialEventOccurAt(ph, currentTimestamp, history);
			
			addNegation(history, ph, currentTimestamp);
			currentTimestamp += distance;
		}
		
		System.out.println("in the function of decoreateHistory,RuleGuideNegationHistoryGenerator。");
		System.out.println("历史记录中 一共生成了  : " + history.getNumPrimitiveEvents() +" 个原子事件");
		AssistDebug.takeABreak();

		//调用父类的方法
		addNoise(history, ph, startingFrom);

		System.out.println("in the function of decoreateHistory,RuleGuideNegationHistoryGenerator。");
		System.out.println("加入了噪声事件后，历史记录中 一共生成了  : " + history.getNumPrimitiveEvents() +" 个原子事件");
	}

	/**
	 * 往history中，添加满足rule的 negationConstraint的原子事件。
	 * **/
	private void addNegation(History history, ParamHandler ph, long referenceTS) {
		List<NegationConstraint> negs = rule.getNegationConstraints();
		int size = negs.size();
		if (size == 0) {
			return;
		}
		
		int numNegs = 1 + ph.getRandom().nextInt(size);
		Set<Integer> negIndices = extractIndices(numNegs, size, ph);
		
		//添加的事件这么少？是否能其作用？而且时间戳都放在末尾？
		for (int negIndex : negIndices) {
			NegationConstraint negConstr = negs.get(negIndex);
			Event ev = generateEventForPredicate(negConstr.getPredicate(), ph,referenceTS);
			history.addPrimitiveEvent(ev);
		}
	}

	/**
	 * @param 
	 * size rule中定义的 negationConstraint个数
	 * @return 返回numIndices个 从[0,size) 的随机值 的集合。
	 * **/
	private Set<Integer> extractIndices(int numIndices, int size, ParamHandler ph) {
		
		Set<Integer> alreadyExtracted = new HashSet<Integer>();
		
		for (int i = 0; i < numIndices; i++) {
			extractIndices(alreadyExtracted, size, ph);
		}
		
		return alreadyExtracted;
	}

	/**
	 * 返回一个从 0～size 随机值，并且不在 alreadyExtracted中。
	 * **/
	private int extractIndices(Set<Integer> alreadyExtracted, int size, ParamHandler ph) {
		int val = 0;
		do {
			val = ph.getRandom().nextInt(size);
		} while (alreadyExtracted.contains(val));
		
		alreadyExtracted.add(val);
		return val;
	}

	/**
	 * 根据rule中定义的negationConstraints，生成满足其约束的原子事件，并返回。
	 * **/
	private final Event generateEventForPredicate(Predicate pred, ParamHandler ph, long referenceTS) {
		Set<Attribute> attrs = new HashSet<Attribute>();
		Filter filter = pred.getFilter();

		for (Constraint c : filter) {
			long val = 0;
			String name = c.getName();
			switch (c.getOperator()) {
			case EQ:
				val = c.getValue().getLongValue();
				break;
			case ANY:
				val = getValue(0, ph.getNumValues(), Distribution.UNIFORM, ph.getRandom());
				break;
			case LT_EQ:
				val = getValue(0, c.getValue().getLongValue(), Distribution.UNIFORM, ph.getRandom());
				break;
			case GT_EQ:
				val = getValue(c.getValue().getLongValue(), ph.getNumValues(), Distribution.UNIFORM, ph.getRandom());
				break;
			default:
				assert false : c.getOperator();
			}
			attrs.add(new Attribute(name, val));
		}

		long minTS = (referenceTS - rule.getWin()) > 0 ? (referenceTS - rule.getWin()) : 0;
		long timestamp = getValue(minTS, referenceTS, Distribution.UNIFORM,ph.getRandom());
		return new Event(pred.getEventType(), timestamp, attrs);
	}

}
