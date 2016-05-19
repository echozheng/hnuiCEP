package cn.edu.hnu.icep.learning.negative_instances;

import cn.edu.hnu.icep.history.History;
import cn.edu.hnu.icep.rules.EventConstraint;
import cn.edu.hnu.icep.learning.ConstraintsSet;
import cn.edu.hnu.icep.event.model.Event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

public class NegativeInstancesHelper {
	private final NavigableMap<Long, List<EventConstraint>> constraintsMap = new TreeMap<Long, List<EventConstraint>>();
	private final NavigableMap<Long, Set<EventConstraint>> winConstraintsMap = new TreeMap<Long, Set<EventConstraint>>();

	public NegativeInstancesHelper(History history, ConstraintsSet constraints) {
		populateConstraintsMap(history, constraints.getEventConstraints());
	}
	
	public void populateConstraintsMap(History history,Set<EventConstraint> constraints) {
		Collection<Event> primitiveEvents = history.getPrimitiveEvents();
		for (Event primitiveEvent : primitiveEvents) {
			if (history.hasCompositeEventAt(primitiveEvent.getTimestamp())) {
				continue;
			}
			addMatchingConstraints(primitiveEvent, constraints);
		}
	}
	
	public void addMatchingConstraints(Event event,Set<EventConstraint> constraints) {
		for (EventConstraint constraint : constraints) {
			addConstraintIfMatching(event, constraint);
		}
	}
	
	public void addConstraintIfMatching(Event event,EventConstraint constraint) {
		if (!constraint.isSatisfiedBy(event)) {
			return;
		}
		Long timestamp = event.getTimestamp();
		List<EventConstraint> constraintsList = constraintsMap.get(timestamp);
		if (constraintsList == null) {
			constraintsList = new ArrayList<EventConstraint>();
			constraintsMap.put(timestamp, constraintsList);
		}
		constraintsList.add(constraint);
	}
	
	public void finalizeWithWin(long win) {
		winConstraintsMap.clear();
		for (Long timestamp : constraintsMap.keySet()) {
			Set<EventConstraint> constraintsSet = getEventConstraintsFor(timestamp, win);
			winConstraintsMap.put(timestamp, constraintsSet);
		}
	}
	
	public Set<EventConstraint> getEventConstraintsFor(long timestamp,long win) {
		long minTs = (timestamp - win > 0) ? (timestamp - win) : 0;
		Set<EventConstraint> result = new HashSet<EventConstraint>();
		for (List<EventConstraint> list : constraintsMap.subMap(minTs, true,timestamp, true).values()) {
			result.addAll(list);
		}
		return result;
	}
	
	public List<Long> getSatisfyingTimestamps(Set<EventConstraint> constraints) {
		List<Long> result = new ArrayList<Long>();
		for (Long timestamp : winConstraintsMap.keySet()) {
			Set<EventConstraint> satisfiedConstraint = winConstraintsMap.get(timestamp);
			if (satisfiedConstraint.equals(constraints)) {
				result.add(timestamp);
			}
		}
		return result;
	}
	

}
