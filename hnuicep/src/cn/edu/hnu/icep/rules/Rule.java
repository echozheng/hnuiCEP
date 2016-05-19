package cn.edu.hnu.icep.rules;

import cn.edu.hnu.icep.rules.EventConstraint;
import cn.edu.hnu.icep.rules.SequenceConstraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import cn.edu.hnu.icep.event.model.Event;

public class Rule {

	// 规则所对应的时间窗口大小
	private long win;

	// 原子事件类型约束
	private List<EventConstraint> eventConstraints = new ArrayList<EventConstraint>();

	// 原子事件 否定约束(也只含有事件类型约束，事件顺序约束)
	private List<NegationConstraint> negationConstraints = new ArrayList<NegationConstraint>();

	// 原子事件顺序约束
	private Map<EventConstraint, Map<EventConstraint, SequenceConstraint>> sequenceConstraints = new HashMap<EventConstraint, Map<EventConstraint, SequenceConstraint>>();

	// 构造函数
	public Rule(long win) {
		this.win = win;
	}

	public final long getWin() {
		return win;
	}

	public final List<EventConstraint> getEventConstraints() {
		return eventConstraints;
	}

	public final List<NegationConstraint> getNegationConstraints() {
		return negationConstraints;
	}

	public final Map<EventConstraint, Map<EventConstraint, SequenceConstraint>> getSequenceConstraints() {
		return sequenceConstraints;
	}

	public final void addEvent(EventConstraint event) {
		eventConstraints.add(event);
	}

	public final void addNegation(NegationConstraint negation) {
		negationConstraints.add(negation);
	}

	public final void addSequenceConstraint(EventConstraint referenceEvent,
			EventConstraint followingEvent) {
		addSequenceConstraint(referenceEvent, followingEvent, 0);
	}

	/**
	 * 添加一个顺序约束
	 *
	 * @param referenceEvent
	 *            , the former event
	 * @param followingEvent
	 *            , the following event
	 * @param win
	 *            窗口大小
	 */
	public final void addSequenceConstraint(EventConstraint referenceEvent,
			EventConstraint followingEvent, long win) {

		SequenceConstraint seqConstraint = new SequenceConstraint(
				referenceEvent, followingEvent, win);

		Map<EventConstraint, SequenceConstraint> innerMap = sequenceConstraints
				.get(followingEvent);

		if (innerMap == null) {
			innerMap = new HashMap<EventConstraint, SequenceConstraint>();
			sequenceConstraints.put(followingEvent, innerMap);
		}

		// 这条assert语句，要求innerMap中原本不包含referenceEvent。
		assert (!innerMap.containsKey(referenceEvent));
		innerMap.put(referenceEvent, seqConstraint);
	}

	/**
	 * @param refTimestamp
	 *            =>复杂事件发生的时间
	 * @param events
	 *            =>在某个窗口大小(win)，复杂事件发生的之前(时间包括两个节点)的原子事件集合。
	 * 
	 *            判断原子事件集合events 是否符合rule
	 * **/
	public boolean isSatisfiedBy(Collection<Event> events, long refTimestamp) {

		NavigableMap<RuleConstraint, List<Event>> filteredEvents = new TreeMap<RuleConstraint, List<Event>>();
		if (!removeOldEvents(events, refTimestamp)) {
			return false;
		}

		if (!filterEvents(events, filteredEvents)) {
			return false;
		}

		Map<RuleConstraint, List<Event>> negatedEvents = getNegatedEvents(events);

		if (!removeEventsThatCannotSatisfyNegations(filteredEvents,
				negatedEvents)) {
			return false;
		}

		SelectionGenerator gen = new SelectionGenerator(filteredEvents);

		while (gen.hasNext()) {
			Map<RuleConstraint, Event> selectedEvents = gen.next();
			if (!isSelectionSatisfyingSequences(selectedEvents)) {
				continue;
			}
			return true;
		}
		return false;
	}

	/**
	 * 删除掉events中不满足在时间范围[refTimestamp-win,refTimestamp]内的原子事件去掉。
	 * 
	 * @param refTimestamp
	 *            =>复杂事件发生的时间
	 * @param events
	 *            =>在某个窗口大小(win)，复杂事件发生的之前(时间包括两个节点)的原子事件集合。
	 * **/
	private final boolean removeOldEvents(Collection<Event> events,
			long refTimestamp) {
		long minTimestamp = refTimestamp - win;
		Iterator<Event> it = events.iterator();
		while (it.hasNext()) {
			Event ev = it.next();
			long timestamp = ev.getTimestamp();
			// 把不在[minTimestamp,refTimestamp]时间内的原子事件去掉
			// 其实在生成 实参events的时候，已经判断过一次了。
			if (timestamp < minTimestamp || timestamp > refTimestamp) {
				it.remove();
			}
		}
		// 如果不为空，返回true
		return !events.isEmpty();
	}

	/**
	 * 返回 这条positive trace中 符合 最初生成rule中的 negationConstraints的事件。
	 * 
	 * @param events
	 *            => 一条positive trace中的原子事件集合。
	 * */
	private final Map<RuleConstraint, List<Event>> getNegatedEvents(
			Collection<Event> events) {

		Map<RuleConstraint, List<Event>> result = new HashMap<RuleConstraint, List<Event>>();

		// 在学习negationLearner约束的时候，negationConstraints的内容为空。（注意）
		for (NegationConstraint constr : negationConstraints) {
			List<Event> negatedEvents = constr
					.getConstraintViolatingEvents(events);
			result.put(constr, negatedEvents);
		}
		return result;
	}

	/**
	 * 使用之前生成的规则去过滤 不符合EventConstraints的事件。
	 * 
	 * @return 如果events 中含有满足rule中的EventConstraints的事件， 并把事件放入 @param
	 *         filteredEvents,返回true.
	 * **/
	private final boolean filterEvents(Collection<Event> events,
			Map<RuleConstraint, List<Event>> filteredEvents) {

		for (EventConstraint constr : eventConstraints) {
			List<Event> satisfyingEvents = constr.getSatisfyingEvents(events);

			if (satisfyingEvents.isEmpty()) {
				// 一旦不符合某个 EventConstraint,说明已经不满足规则，直接返回false。
				return false;
			} else {
				filteredEvents.put(constr, satisfyingEvents);
			}
		}
		return true;
	}

	/**
	 * 根据自己的需求，进行了修改。 zql 2016-04-13
	 * 
	 * @param filteredEvents
	 *            满足 rule的EventConstraint的原子事件。
	 * @param negatedEvents
	 *            满足 rule的NegationConstraint的原子事件。
	 */
	private final boolean removeEventsThatCannotSatisfyNegations(
			NavigableMap<RuleConstraint, List<Event>> filteredEvents,
			Map<RuleConstraint, List<Event>> negatedEvents) {

		// 满足否定规则的全部事件(在一条positive trace中)
		List<Event> allNegatedEvents = new LinkedList<Event>();

		// 满足事件类型约束规则的全部事件(在一条positive trace中)
		List<Event> allEvents = new LinkedList<Event>();

		// 当在学习 negationConstraint时，negationConstraints是为空的，所以函数返回true。
		for (NegationConstraint negConstr : negationConstraints) {
			assert (negatedEvents.containsKey(negConstr));
			List<Event> negatedEventsList = negatedEvents.get(negConstr);
			// If no negated event is found, move to the next negation
			if (negatedEventsList.isEmpty()) {
				continue;
			}
			allNegatedEvents.addAll(negatedEventsList);
		}

		for (EventConstraint constr : eventConstraints) {
			assert (filteredEvents.containsKey(constr));
			List<Event> eventsList = filteredEvents.get(constr);
			if (eventsList.isEmpty()) {
				continue;
			}
			allEvents.addAll(eventsList);
		}

		// 把allEvents中，并且出现在allNegatedEvents中的原子事件删除
		Iterator<Event> allEventsIterator = allEvents.iterator();
		while (allEventsIterator.hasNext()) {
			Event event = allEventsIterator.next();
			if (allNegatedEvents.contains(event)) {
				// 删除当前游标指向的事件,根据对象应用一致性，当然也删除了 filteredEvents中的相应事件。
				allEventsIterator.remove();
			}
		}

		if (allEvents.size() >= 1) {
			// Some events are still available
			return true;
		}
		// all事件全部被删除了，返回false.
		return false;
	}

	/**
	 * 判断 selectedEvents 是否满足rule中定义的顺序约束
	 * **/
	private final boolean isSelectionSatisfyingSequences(
			Map<RuleConstraint, Event> selectedEvents) {

		for (EventConstraint followingConstr : sequenceConstraints.keySet()) {
			Map<EventConstraint, SequenceConstraint> innerMap = sequenceConstraints
					.get(followingConstr);

			Event followingEvent = selectedEvents.get(followingConstr);

			for (EventConstraint refConstr : innerMap.keySet()) {

				SequenceConstraint seqConstr = innerMap.get(refConstr);

				Event refEvent = selectedEvents.get(refConstr);

				long refTimestamp = refEvent.getTimestamp();

				long followingTimestamp = followingEvent.getTimestamp();

				if (followingTimestamp < refTimestamp) {
					return false;
				}

				if (seqConstr.hasWindow()) {
					long win = seqConstr.getWin();
					if (followingTimestamp > refTimestamp + win) {
						return false;
					}
				}

			}// for2
		}// for1
		return true;
	}

	/**
	 * 返回所有的 顺序约束
	 * **/
	private Set<SequenceConstraint> getAllSequenceConstraints() {
		Set<SequenceConstraint> result = new HashSet<SequenceConstraint>();
		for (EventConstraint e1 : sequenceConstraints.keySet()) {
			Map<EventConstraint, SequenceConstraint> innerMap = sequenceConstraints.get(e1);
			for (EventConstraint e2 : innerMap.keySet()) {
				SequenceConstraint s = innerMap.get(e2);
				result.add(s);
			}
		}
		return result;
	}

	@Override
	public String toString() {
		String result = "";
		result += "Rule [win: " + win + "\n";
		result += "Event Constraints: " + eventConstraints;
		Set<SequenceConstraint> seqConstraints = getAllSequenceConstraints();
		
		if (!seqConstraints.isEmpty()) {
			result += "\nSequence Constraints: " + seqConstraints;
		}

		if (!negationConstraints.isEmpty()) {
			result += "\nNegation Constraints: " + negationConstraints;
		}
		result += "]";
		return result;
	}

	/**
	 * 创建一个内部类
	 * **/
	private class SelectionGenerator {
		private final NavigableMap<RuleConstraint, List<Event>> events;
		private final NavigableMap<RuleConstraint, Integer> currentPositions = new TreeMap<RuleConstraint, Integer>();
		private boolean hasNext;

		// events 为满足 rule的EventConstraint的事件集合
		public SelectionGenerator(
				NavigableMap<RuleConstraint, List<Event>> events) {
			this.events = events;
			for (RuleConstraint constr : events.keySet()) {
				currentPositions.put(constr, 0);
			}
			hasNext = !events.isEmpty();
		}

		boolean hasNext() {
			return hasNext;
		}

		/**
		 * 把NavigableMap<RuleConstraint, List<Event>> events 转换成
		 * NavigableMap<RuleConstraint, Event> result 形式。注意，result中
		 * 包含每一类的约束的一个事件。
		 * 
		 * 例如， events => (Ec1, e1@100），（Ec2,e2@100,e2@105),(Ec3,e3@100,e3@15)
		 * 返回的内容如下: result => (Ec1,e1@100),(Ec2,e2@100),(Ec3,e3@100) result =>
		 * (Ec1,e1@100),(Ec2,e2@105),(Ec3,e3@105)
		 * **/
		public Map<RuleConstraint, Event> next() {
			hasNext = false;
			boolean foundConstraintToAdvance = false;
			NavigableMap<RuleConstraint, Event> result = new TreeMap<RuleConstraint, Event>();

			for (RuleConstraint constr : events.keySet()) {
				// eventsList 一条positive trace中，满足该约束的原子事件
				List<Event> eventsList = events.get(constr);
				int lastPosition = eventsList.size() - 1;
				int currentPosition = currentPositions.get(constr);
				result.put(constr, eventsList.get(currentPosition));

				// 如果 第一个 constr只有一个原子事件，后面的constr有两个以上的原子事件，它们取不出来啊。？？？
				if (!foundConstraintToAdvance) {
					if (currentPosition < lastPosition) {
						hasNext = true;
					}
					currentPosition++;
					if (currentPosition > lastPosition) {
						currentPosition = 0;
					} else {
						// 在currentPosition++ == lastPosition时候
						foundConstraintToAdvance = true;
					}
					currentPositions.remove(constr);
					currentPositions.put(constr, currentPosition);
				}
			}
			return result;
		}
	}
}
