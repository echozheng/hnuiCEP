package cn.edu.hnu.icep.learning.negative_instances;

import cn.edu.hnu.icep.rules.EventConstraint;
import cn.edu.hnu.icep.rules.RuleConstraint;
import cn.edu.hnu.icep.event.model.Event;
import cn.edu.hnu.icep.rules.NegationConstraint;
import cn.edu.hnu.icep.rules.SequenceConstraint;
import cn.edu.hnu.icep.rules.filtering.Predicate;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import cn.edu.hnu.icep.learning.negative_instances.NegativeInstancesHelper;
import cn.edu.hnu.icep.history.History;
import cn.edu.hnu.icep.learning.ConstraintsSet;

public class NegativeInstancesDetector {

	private final NegativeInstancesHelper negHelper;
	private final History history;
	private long win = 0;
	
	public NegativeInstancesDetector(History history, ConstraintsSet constraints) {
		this.history = history;
		negHelper = new NegativeInstancesHelper(history, constraints);
	}

	public void finalizeWithWin(long win) {
		this.win = win;
		negHelper.finalizeWithWin(win);
	}
	
	public Set<ConstraintsSet> getConstraintsSets(ConstraintsSet constraints) {
		int size = constraints.size();
		Set<ConstraintsSet> initialConstraints = new HashSet<ConstraintsSet>();
		initialConstraints.add(constraints);
		return getConstraintsSets(initialConstraints, size);
	}
	
	// TODO: check first iteration. What if we already have a negative example?
	private Set<ConstraintsSet> getConstraintsSets(Set<ConstraintsSet> initialConstraints, int size) {
		Set<ConstraintsSet> allConstraints = new HashSet<ConstraintsSet>();
		Set<ConstraintsSet> currentConstraints = initialConstraints;
		Set<ConstraintsSet> nextConstraints = null;
		for (int i = size; i > 1; i--) {
			nextConstraints = getNextConstraintsSets(currentConstraints);
			if (nextConstraints.isEmpty()) {
				break;
			}
			allConstraints.addAll(nextConstraints);
			currentConstraints = nextConstraints;
		}
		return allConstraints;
	}
	
	private final Set<ConstraintsSet> getNextConstraintsSets(Set<ConstraintsSet> currentConstraints) {
		Set<ConstraintsSet> nextConstraints = new HashSet<ConstraintsSet>();
		boolean firstIteration = true;
		for (ConstraintsSet constraints : currentConstraints) {
			getNextConstraintsSets(constraints, nextConstraints, firstIteration);
			firstIteration = false;
		}
		return nextConstraints;
	}

	private final void getNextConstraintsSets(ConstraintsSet constraints,
				Set<ConstraintsSet> nextConstraints, boolean firstIteration) {
		
		// First, remove one additional constraint at a time
		if (!firstIteration) {
			List<Long> satisfyingTimestamps = negHelper.getSatisfyingTimestamps(constraints.getEventConstraints());
			for (RuleConstraint additionalConstraintToRemove : constraints.getAdditionalConstraints()) {

				ConstraintsSet set = new ConstraintsSet(constraints.getAdditionalConstraints());
				set.remove(additionalConstraintToRemove);
				
				if (satisfyingTimestamps.isEmpty() || !satisfiesAdditionalConstraints(satisfyingTimestamps, set)) {
					set.addAll(constraints.getEventConstraints());
					nextConstraints.add(set);
				}
			}
		}

		// Second, remove one selection constraint at a time
		for (EventConstraint constrToRemove : constraints.getEventConstraints()) {
			if (isImplied(constrToRemove, constraints.getEventConstraints())) {
				continue;
			}
			ConstraintsSet fullSet = new ConstraintsSet(
					constraints.getAllConstraints());
			fullSet.remove(constrToRemove);
			if (nextConstraints.contains(fullSet)) {
				continue;
			}
			Set<EventConstraint> evConstrSet = new HashSet<EventConstraint>(
					constraints.getEventConstraints());
			evConstrSet.remove(constrToRemove);
			List<Long> satisfyingTimestamps = negHelper
					.getSatisfyingTimestamps(evConstrSet);
			if (satisfyingTimestamps.isEmpty()
					|| !satisfiesAdditionalConstraints(satisfyingTimestamps,
							constraints)) {
				nextConstraints.add(fullSet);
			}
		}
	}
	
	private boolean satisfiesAdditionalConstraints(Collection<Long> satisfyingTimestamps, 
									ConstraintsSet constraints) {
		for (Long satisfyingTimestamp : satisfyingTimestamps) {
			if (satisfiesAdditionalConstraints(satisfyingTimestamp, constraints)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean satisfiesAdditionalConstraints(
					long satisfyingTimestamp, ConstraintsSet constraints) {
		
		long minTimestamp = (satisfyingTimestamp - win > 0) ? (satisfyingTimestamp - win) : 0;
		Collection<Event> events = history.getAllEventsInWindow(minTimestamp,satisfyingTimestamp);
		
		for (RuleConstraint constr : constraints.getAdditionalConstraints()) {
			if (constr instanceof SequenceConstraint) {
				if (!satisfySequenceConstraint(events,(SequenceConstraint) constr)) {
					return false;
				}
			} else if (constr instanceof NegationConstraint) {
				if (!satisfyNegationConstraint(events,(NegationConstraint) constr)) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}
	
	private boolean satisfySequenceConstraint(Collection<Event> events,SequenceConstraint constraint) {
		EventConstraint firstEvConstraint = constraint.getReferenceEvent();
		EventConstraint secondEvConstraint = constraint.getFollowingEvent();
		long win = constraint.getWin();
		NavigableSet<Long> firstTimestamps = new TreeSet<Long>();
		NavigableSet<Long> secondTimestamps = new TreeSet<Long>();
		for (Event event : events) {
			long eventTs = event.getTimestamp();
			
			if (firstEvConstraint.isSatisfiedBy(event)) {
				long minTs = eventTs;
				if (win > 0) {
					long maxTs = eventTs + win;
					if (!secondTimestamps.subSet(minTs, true, maxTs, true).isEmpty())
						return true;
				} else {
					if (secondTimestamps.ceiling(minTs) != null) {
						return true;
					}
				}
				firstTimestamps.add(eventTs);
			}
			
			if (secondEvConstraint.isSatisfiedBy(event)) {
				long maxTs = eventTs;
				if (win > 0) {
					long minTs = eventTs - win;
					if (!secondTimestamps.subSet(minTs, true, maxTs, true).isEmpty())
						return true;
				} else {
					if (firstTimestamps.floor(eventTs) != null) {
						return true;
					}
				}
				secondTimestamps.add(eventTs);
			}
		}
		return false;
	}
	
	private boolean satisfyNegationConstraint(Collection<Event> events, NegationConstraint constraint) {
		Predicate negPred = constraint.getPredicate();
		for (Event event : events) {
			if (negPred.isSatisfiedBy(event)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isImplied(EventConstraint constraint,Set<EventConstraint> constraints) {
		Predicate constrPred = constraint.getPredicate();
		for (EventConstraint c : constraints) {
			if (c.getPredicate().covers(constrPred)) {
				return true;
			}
		}
		return false;
	}
	
}
