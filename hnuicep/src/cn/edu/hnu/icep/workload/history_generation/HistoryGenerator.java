package cn.edu.hnu.icep.workload.history_generation;

import cn.edu.hnu.icep.common.Distribution;
import cn.edu.hnu.icep.event.model.Attribute;
import cn.edu.hnu.icep.event.model.Value;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import cn.edu.hnu.icep.common.Consts;
import cn.edu.hnu.icep.event.model.Event;
import cn.edu.hnu.icep.history.History;
import cn.edu.hnu.icep.rules.Rule;
import cn.edu.hnu.icep.eval.ParamHandler;
import cn.edu.hnu.icep.workload.history_generation.RandomHistoryGenerator;
import cn.edu.hnu.icep.workload.history_generation.RuleGuidedHistoryGenerator;
import cn.edu.hnu.icep.workload.history_generation.RuleGuidedNegationHistoryGenerator;
import cn.edu.hnu.icep.workload.history_generation.TypePeriodicHistoryGenerator;

public abstract class HistoryGenerator {
	
	public abstract void decorateHistory(History history, ParamHandler ph);

	public static void generateHistory(History history, Rule rule,ParamHandler ph) {
		switch (ph.getHistoryGeneratorType()) {
		case RANDOM:
			new RandomHistoryGenerator().decorateHistory(history, ph);
			break;
		case TYPE_PERIODIC:
			new TypePeriodicHistoryGenerator().decorateHistory(history, ph);
			break;
		case RULE_GUIDED:
			new RuleGuidedHistoryGenerator(rule).decorateHistory(history, ph);
			break;
		case RULE_GUIDED_NEGATION:
			new RuleGuidedNegationHistoryGenerator(rule).decorateHistory(history, ph);
			break;
		default:
			assert false : ph.getHistoryGeneratorType();
			new RandomHistoryGenerator().decorateHistory(history, ph);
		}
	}

	/**
	 * 返回一个原子事件，时间为 timestamp，eventType 与 Attribute随机产生。
	 * **/
	public Event generateEvent(long timestamp, ParamHandler ph) {
		return new Event(getEventType(ph), timestamp, generateAttributes(ph));
	}

	/**
	 * 返回一个事件类型，Event+(数字)
	 * **/
	private String getEventType(ParamHandler ph) {
		int numTypes = ph.getNumEventTypes();
		// ph.getEventTypesDistribution() = UNIFORM
		int typeId = (int) getValue(0, numTypes,ph.getEventTypesDistribution(), ph.getRandom());
		return getEventType(typeId);
	}

	private String getEventType(int typeId) {
		return Consts.EVENT_TYPE_PREFIX + String.valueOf(typeId);
	}
	
	/**
	 * 返回minVal - maxVal之间的一个随机值。
	 * **/
	public long getValue(long minVal, long maxVal, Distribution distrib,Random r) {
		// 默认情况下，minVal = 0，maxVal是设定的事件类型的总数
		long value = 0;
		switch (distrib) {
		case UNIFORM:
			value = (minVal == maxVal) ? minVal : minVal + r.nextInt((int) (maxVal - minVal));
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

	/**
	 * @return 随机生成属性集合
	 * **/
	private Set<Attribute> generateAttributes(ParamHandler ph) {
		Set<Attribute> attributes = new HashSet<Attribute>();
		// ph.getMinNumAttributes() = 3；ph.getMaxNumAttributes() = 3
		long numAttributes = getValue(ph.getMinNumAttributes(),
				ph.getMaxNumAttributes(), Distribution.UNIFORM, ph.getRandom());
		for (int i = 0; i < numAttributes; i++) {
			Attribute attr = getAttribute(i, ph);
			attributes.add(attr);
		}
		return attributes;
	}

	/**
	 * 随机产生 attributeNum 的值，并封装成一个Attribute对象，然后返回。
	 * **/
	private Attribute getAttribute(int attributeNum, ParamHandler ph) {
		int numValues = ph.getNumValues();
		int value = (int) getValue(0, numValues, ph.getValueDistribution(),
				ph.getRandom());
		return new Attribute(Consts.ATTRIBUTE_TYPE_PREFIX + attributeNum,
				new Value(value));
	}
}