package cn.edu.hnu.icep.history;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.NavigableMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import cn.edu.hnu.icep.event.model.Event;
import cn.edu.hnu.icep.rules.filtering.Predicate;

/**
 * 用于存储历史记录，包括 原子事件集合 与 复杂事件集合
 * 
 * @author hduser
 * **/
public class History {

	// Long表示事件发生的时间
	private final NavigableMap<Long, List<Event>> primitiveEvents;
	private final NavigableMap<Long, List<Event>> compositeEvents;
	private int numPrimitiveEvents;
	private int numCompositeEvents;

	public History() {
		primitiveEvents = new TreeMap<Long, List<Event>>();
		compositeEvents = new TreeMap<Long, List<Event>>();
		numPrimitiveEvents = 0;
		numCompositeEvents = 0;
	}

	public final void addPrimitiveEvent(Event e) {
		addEventToHistory(e, true);
		numPrimitiveEvents++;
	}

	public final void addCompositeEvent(Event e) {
		addEventToHistory(e, false);
		numCompositeEvents++;
	}

	public final int getNumPrimitiveEvents() {
		return numPrimitiveEvents;
	}

	public final int getNumCompositeEvents() {
		return numCompositeEvents;
	}

	public final Collection<Event> getPrimitiveEvents() {
		return getEvents(true);
	}

	public final Collection<Event> getCompositeEvents() {
		return getEvents(false);
	}

	public final boolean hasCompositeEventAt(long timestamp) {
		return compositeEvents.containsKey(timestamp);
	}
	
	/**
	 * 返回全部原子事件的时间戳。重复的时间 只计算一次。
	 * **/
	public final List<Long> getTimestampsWithPrimitiveEvents() {
		return getTimestampsWithEvents(true);
	}
	
	/**
	 * 返回全部复杂事件的时间戳。重复的时间 只计算一次。
	 * **/
	public final List<Long> getTimestampsWithCompositeEvents() {
		return getTimestampsWithEvents(false);
	}

	/**
	 * return all the primitive events in timestamp between
	 * [minTimestamp,maxTimestamp], doesn't contain any composite events
	 * 
	 * @author hduser
	 **/
	public final Collection<Event> getAllEventsInWindow(long minTimestamp,
			long maxTimestamp) {
		Collection<Event> result = new ArrayList<Event>();

		// subMap这个方法很好用啊。
		NavigableMap<Long, List<Event>> requiredMap = primitiveEvents.subMap(
				minTimestamp, true, maxTimestamp, true);

		if (requiredMap == null) {
			return result;
		}
		for (List<Event> eventList : requiredMap.values()) {
			result.addAll(eventList);
		}
		return result;
	}

	public final Set<String> getAllEventTypesInWindow(long minTimestamp,
			long maxTimestamp) {
		Set<String> result = new HashSet<String>();
		Collection<Event> events = getAllEventsInWindow(minTimestamp,
				maxTimestamp);
		for (Event event : events) {
			result.add(event.getEventType());
		}
		return result;
	}

	/**
	 * 返回原子事件 或者 复杂事件中最后发生的事件的时间戳
	 * @author hduser
	 * **/
	public final long getMaximumTimestamp() {
		long maxTimestamp = 0;
		if (!primitiveEvents.isEmpty()) {
			long maxPrimitiveTimestamp = primitiveEvents.lastKey();
			if (maxPrimitiveTimestamp > maxTimestamp) {
				maxTimestamp = maxPrimitiveTimestamp;
			}
		}

		if (!compositeEvents.isEmpty()) {
			long maxCompositeTimestamp = compositeEvents.lastKey();
			if (maxCompositeTimestamp > maxTimestamp) {
				maxTimestamp = maxCompositeTimestamp;
			}
		}
		return maxTimestamp;
	}

	/**
	 * Returns the list of events satisfying the given predicate.
	 * 
	 * @param p
	 *            the predicate
	 * @param minTimestamp
	 *            the lower bound of the time interval to consider
	 * @param maxTimestamp
	 *            the upper bound of the time interval to consider
	 * @return the list of events satisfying the filter
	 */
	public List<Event> getSatisfyingEvents(Predicate p, long minTimestamp,
			long maxTimestamp) {

		List<Event> results = new LinkedList<Event>();
		NavigableMap<Long, List<Event>> desiredMap = primitiveEvents.subMap(
				minTimestamp, true, maxTimestamp, true);

		if (desiredMap == null) {
			return null;
		}

		// key为事件发生的时间戳
		for (Long key : desiredMap.keySet()) {
			for (Event ev : desiredMap.get(key)) {
				if (p.isSatisfiedBy(ev)) {
					results.add(ev);
				}
			}
		}
		return results;
	}

	// 添加事件，添加何种事件根据primitive的true,false值来确定。
	private void addEventToHistory(Event e, boolean primitive) {
		NavigableMap<Long, List<Event>> eventsToAdd = primitive ? primitiveEvents
				: compositeEvents;
		long timestamp = e.getTimestamp();
		if (!eventsToAdd.containsKey(timestamp)) {
			List<Event> eventList = new ArrayList<Event>();
			eventList.add(e);
			eventsToAdd.put(timestamp, eventList);
		} else {
			eventsToAdd.get(timestamp).add(e);
		}
	}

	// 返回事件，根据primitive返回所需的事件类型
	private final Collection<Event> getEvents(boolean primitive) {
		NavigableMap<Long, List<Event>> eventsToAdd = primitive ? primitiveEvents
				: compositeEvents;
		Collection<Event> events = new ArrayList<Event>();
		for (List<Event> eventList : eventsToAdd.values()) {
			events.addAll(eventList);
		}
		return events;
	}

	/**
	 * 返回全部原子事件 或是 复杂事件的时间戳。重复的时间 只计算一次。
	 * **/
	private final List<Long> getTimestampsWithEvents(boolean primitive) {
		NavigableMap<Long, List<Event>> eventsToConsider = primitive ? primitiveEvents : compositeEvents;
		List<Long> timestamps = new ArrayList<Long>();
		Long lastTimestamp = Long.MIN_VALUE;

		for (Long timestamp : eventsToConsider.keySet()) {
			if (timestamp <= lastTimestamp) {
				continue;
			} else {
				lastTimestamp = timestamp;
				timestamps.add(timestamp);
			}
		}
		return timestamps;
	}

	@Override
	public String toString() {
		String result = "Primitive Events: [ ";
		for (Long ts : primitiveEvents.keySet()) {
			for (Event ev : primitiveEvents.get(ts)) {
				result += ev.getEventType() + "@" + ts + " ";
			}
		}
		result += "]\n";
		result += "Composite Events: [ ";
		for (Long ts : compositeEvents.keySet()) {
			for (Event ev : compositeEvents.get(ts)) {
				result += ev.getEventType() + "@" + ts + " ";
			}
		}
		result += "]";
		return result;
	}

}
