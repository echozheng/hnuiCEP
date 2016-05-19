package cn.edu.hnu.icep.workload.history_generation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.hnu.icep.event.model.Event;
import cn.edu.hnu.icep.rules.Rule;
import cn.edu.hnu.icep.zql.update.AssistDebug;
import cn.edu.hnu.icep.eval.ParamHandler;
import cn.edu.hnu.icep.history.History;
import cn.edu.hnu.icep.exceptions.ICEPException;
import cn.edu.hnu.icep.rules.EventConstraint;
import cn.edu.hnu.icep.rules.RuleConstraint;
import cn.edu.hnu.icep.rules.SequenceConstraint;
import cn.edu.hnu.icep.event.model.Attribute;
import cn.edu.hnu.icep.common.ConstraintOp;
import cn.edu.hnu.icep.event.model.Value;
import cn.edu.hnu.icep.rules.filtering.Constraint;
import cn.edu.hnu.icep.rules.filtering.Filter;
import cn.edu.hnu.icep.rules.filtering.Predicate;
import cn.edu.hnu.icep.common.Distribution;

public class RuleGuidedHistoryGenerator extends HistoryGenerator {

	protected final Rule rule;

	public RuleGuidedHistoryGenerator(Rule rule) {
		this.rule = rule;
	}

	@Override
	public void decorateHistory(History history, ParamHandler ph) {
		// ph.getMaxWinSize() = 12; ph.getNumEventConstraints = 3; 所以distance = 72;
		long distance = ph.getMaxWinSize() * ph.getNumEventConstraints() * 2;

		// 从Random产生的原子事件之后的时间戳开始。
		long startingFrom = history.getMaximumTimestamp();
		long currentTimestamp = startingFrom + distance;
		for (int i = 0; i < ph.getNumArtificialPrimitiveEventsInHistory(); i++) {
			makeSpecialEventOccurAt(ph, currentTimestamp, history);
			currentTimestamp += distance;
		}
		
		addNoise(history, ph, startingFrom);
	}

	/**
	 * 创建符合 EventConstraint约束的原子事件,并添加进历史记录中
	 * **/
	public final void makeSpecialEventOccurAt(ParamHandler ph,long timestamp, History history) {
		Collection<Event> events = generateEventsForRule(ph, timestamp);
		for (Event event : events) {
			history.addPrimitiveEvent(event);
		}
	}

	/**
	 * 根据 this.rule 产生规则符合 EventConstraint的原子事件。
	 * 注意，每一种EventConstraint所对应的原子事件各产生一个。
	 * **/
	private final Collection<Event> generateEventsForRule(ParamHandler ph,
			long referenceTimestamp) {

		long winTimestamp = referenceTimestamp - rule.getWin();
		if (winTimestamp < 0) {
			winTimestamp = 0;
		}
		Map<RuleConstraint, Event> events = new HashMap<RuleConstraint, Event>();

		while (true) {
			boolean sat = true;
			for (EventConstraint constr : rule.getEventConstraints()) {
				try {
					long minTS = getMinTimestamp(constr, events, winTimestamp);
					long maxTS = getMaxTimestamp(constr, events,referenceTimestamp);
					
					//System.out.println("in the function of generateEventsForRule, class RuleGuidedHistoryGenerator.java");
					//System.out.println("minTS = " + minTS + ", maxTS = " + maxTS);
					//AssistDebug.takeABreak();
					generateEventsForEventConstraint(ph, constr, events, minTS, maxTS);
				} catch (ICEPException e) {
					sat = false;
				}
			}

			if (!sat) {
				events.clear();
			} else {
				break;
			}
		}
		return events.values();
	}

	/**
	 * @return 返回顺序约束中的最小时间戳。 如果没有sequenceConstraints,就返回winTimestamp
	 * @param winTimestamp
	 *            = referenceTimestamp - rule.getWin();
	 **/
	private final long getMinTimestamp(EventConstraint evConstr,
			Map<RuleConstraint, Event> generatedEvents, long winTimestamp) {

		// Map<followingEvent,Map<referenceEvent,SequenceConstraint>>
		Map<EventConstraint, Map<EventConstraint, SequenceConstraint>> seqConstraints = rule.getSequenceConstraints();
		if (!seqConstraints.containsKey(evConstr)) {
			// 如果必需原子事件类型 不包含在 顺序约束里面
			return winTimestamp;
		}

		long min = winTimestamp;

		// Map<referenceEvent,SequenceConstraint> innerMap
		Map<EventConstraint, SequenceConstraint> innerMap = seqConstraints.get(evConstr);

		//在runDefault中，根本就没有 sequenceConstraint
		for (EventConstraint refConstr : innerMap.keySet()) {
			long ts = generatedEvents.get(refConstr).getTimestamp();
			System.out.println("long ts = generatedEvents.get(refConstr).getTimestamp() = " + ts);
			AssistDebug.takeABreak();
			
			if (ts > min) {
				min = ts;
			}
		}
		return min;
	}

	private final long getMaxTimestamp(EventConstraint evConstr,
			Map<RuleConstraint, Event> generatedEvents, long referenceTimestamp) {

		// Map<followingEvent,Map<referenceEvent,SequenceConstraint>>
		// seqConstraints
		Map<EventConstraint, Map<EventConstraint, SequenceConstraint>> seqConstraints = rule
				.getSequenceConstraints();
		if (!seqConstraints.containsKey(evConstr)) {
			return referenceTimestamp;
		}

		long max = referenceTimestamp;
		// Map<referenceEvent,SequenceConstraint> innerMap
		Map<EventConstraint, SequenceConstraint> innerMap = seqConstraints
				.get(evConstr);
		for (EventConstraint refConstr : innerMap.keySet()) {
			SequenceConstraint seqConstraint = innerMap.get(refConstr);
			if (seqConstraint.hasWindow()) {
				// 默认情况下，seqConstraints的win都为0
				long win = seqConstraint.getWin();
				long ts = generatedEvents.get(refConstr).getTimestamp() + win;

				if (ts < max) {
					max = ts;
				}
			}
		}
		return max;
	}

	/**
	 * @return 根据具体产生的规则 生成原子事件。(更具代表性)
	 * @param eventConstraint
	 *            ,从rule中循环出的一个 事件类型约束
	 * **/
	private final void generateEventsForEventConstraint(ParamHandler ph,
			EventConstraint eventConstraint,
			Map<RuleConstraint, Event> generatedEvents, long minTS, long maxTS)
			throws ICEPException {

		// constraints为 eventConstraint的解剖出来的 constraints
		Map<String, List<AttributeConstraint>> constraints = generateConstraintsFor(eventConstraint);

		Set<Attribute> attributes = new HashSet<Attribute>();
		for (String name : constraints.keySet()) {
			List<AttributeConstraint> constrList = constraints.get(name);
			Attribute attr = generateAttributeSatisfying(ph, name, constrList);
			attributes.add(attr);
		}

		Event event = generateEventWithAttributes(ph, eventConstraint
				.getPredicate().getEventType(), attributes, minTS, maxTS);
		generatedEvents.put(eventConstraint, event);
	}

	/**
	 * @param eventConstraint
	 *            从rule中循环出的一个 事件类型约束 取出 eventConstraint中的约束，装进result中，并返回。
	 * **/
	private final Map<String, List<AttributeConstraint>> generateConstraintsFor(
			EventConstraint eventConstraint) {

		Map<String, List<AttributeConstraint>> result = new HashMap<String, List<AttributeConstraint>>();
		addConstraintsForPredicate(eventConstraint.getPredicate(), result);

		return result;
	}

	/**
	 * @param pred
	 *            , 从rule中循环出的一个 EventConstraints的一个 predicate 把pred中filter中的
	 *            Constraint 内容取出，装进attributeConstraints.
	 * **/
	private final void addConstraintsForPredicate(Predicate pred,
			Map<String, List<AttributeConstraint>> attributeConstraints) {
		Filter filter = pred.getFilter();

		for (Constraint c : filter) {
			// name => attributeName
			String name = c.getName();
			List<AttributeConstraint> constraintsList = attributeConstraints
					.get(name);
			if (constraintsList == null) {
				constraintsList = new ArrayList<AttributeConstraint>();
				attributeConstraints.put(name, constraintsList);
			}
			constraintsList.add(new AttributeConstraint(c.getOperator(), c
					.getValue()));
		}
	}

	/**
	 * @param ph
	 *            ,系统的相关参数消息
	 * @param name
	 *            一个attribute name
	 * @param constraints
	 *            一个attribute 所对应的约束集合。（从rule中取出）
	 * 
	 *            按照constraints的约束条件，返回一个满足约束条件的 Attribute
	 * **/
	private final Attribute generateAttributeSatisfying(ParamHandler ph,
			String name, List<AttributeConstraint> constraints)
			throws ICEPException {
		long minValue = 0;
		long maxValue = ph.getNumValues();
		for (AttributeConstraint constraint : constraints) {
			long val = constraint.getValue().getLongValue();
			long newMin = 0;
			long newMax = ph.getNumValues();
			switch (constraint.getOp()) {
			case EQ:
				newMin = val;
				newMax = val;
				break;
			case LT_EQ:
				newMin = minValue;
				newMax = val;
				break;
			case GT_EQ:
				newMin = val;
				newMax = maxValue;
				break;
			case ANY:
				break;
			default:
				assert false : constraint.getOp();
			}

			if (newMin > maxValue || newMax < minValue) {
				throw new ICEPException(
						"It is impossible to satisfy all the constraints");
			}

			if (newMin > minValue) {
				minValue = newMin;
			}

			if (newMax < maxValue) {
				maxValue = newMax;
			}
		}

		long selectedValue = getValue(minValue, maxValue,
				ph.getValueDistribution(), ph.getRandom());

		return new Attribute(name, selectedValue);
	}

	/**
	 * 根据 @param attributes 与 @param eventType 返回一个有这俩构成的一个事件
	 * **/
	private final Event generateEventWithAttributes(ParamHandler ph,
			String eventType, Set<Attribute> attributes, long minTS, long maxTS) {
		long eventTimestamp = getValue(minTS, maxTS, Distribution.UNIFORM,
				ph.getRandom());
		return new Event(eventType, eventTimestamp, attributes);
	}
	
	/**
	 * 加入噪声事件, 从@param startFrom开始，到 按规则产生的原子事件的最大时间戳结束。
	 * @param startingFrom 随机产生原子事件的 最大时间戳
	 * **/
	public final void addNoise(History history, ParamHandler ph, long startingFrom) {
		for (long ts = startingFrom; ts < history.getMaximumTimestamp();) {
			Event event = generateEvent(ts, ph);
			history.addPrimitiveEvent(event);
			//ph.getDistanceBetweenNoiseEvents() = 3;
			ts += ph.getDistanceBetweenNoiseEvents();
		}
	}

	private final class AttributeConstraint {
		private final ConstraintOp op;
		private final Value value;
		AttributeConstraint(ConstraintOp op, Value value) {
			this.op = op;
			this.value = value;
		}
		ConstraintOp getOp() {
			return op;
		}

		Value getValue() {
			return value;
		}
	}

}
