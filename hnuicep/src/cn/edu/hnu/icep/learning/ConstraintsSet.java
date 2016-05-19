package cn.edu.hnu.icep.learning;

import cn.edu.hnu.icep.rules.EventConstraint;
import cn.edu.hnu.icep.rules.NegationConstraint;
import cn.edu.hnu.icep.rules.RuleConstraint;
import cn.edu.hnu.icep.rules.SequenceConstraint;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ConstraintsSet {
	private final Set<RuleConstraint> allConstraints = new HashSet<RuleConstraint>();

	private final Set<EventConstraint> eventConstraints = new HashSet<EventConstraint>();
	private final Set<RuleConstraint> additionalConstraints = new HashSet<RuleConstraint>();

	private final Set<SequenceConstraint> sequenceConstraints = new HashSet<SequenceConstraint>();
	private final Set<NegationConstraint> negationConstraints = new HashSet<NegationConstraint>();

	public ConstraintsSet(){}

	public ConstraintsSet(Collection<RuleConstraint> constraints) {
		addAll(constraints);
	}

	public void addAll(Collection<? extends RuleConstraint> constraints) {
		for (RuleConstraint constraint : constraints) {
			add(constraint);
		}
	}

	public void add(RuleConstraint constraint) {
		allConstraints.add(constraint);
		if (constraint instanceof EventConstraint) {
			eventConstraints.add((EventConstraint) constraint);
		} else {
			additionalConstraints.add(constraint);
			
			if (constraint instanceof SequenceConstraint) {
				sequenceConstraints.add((SequenceConstraint) constraint);
			} else if (constraint instanceof NegationConstraint) {
				negationConstraints.add((NegationConstraint) constraint);
			}
		}
	}

	public void remove(RuleConstraint constraint) {
		allConstraints.remove(constraint);
		if (constraint instanceof EventConstraint) {
			eventConstraints.remove(constraint);
		} else {
			additionalConstraints.remove(constraint);
			if (constraint instanceof SequenceConstraint) {
				sequenceConstraints.remove(constraint);
			} else if (constraint instanceof NegationConstraint) {
				negationConstraints.remove(constraint);
			}
		}
	}

	public final Set<RuleConstraint> getAllConstraints() {
		return allConstraints;
	}

	public final int size() {
		return allConstraints.size();
	}

	public final Set<EventConstraint> getEventConstraints() {
		return eventConstraints;
	}

	public final Set<RuleConstraint> getAdditionalConstraints() {
		return additionalConstraints;
	}

	public final Set<SequenceConstraint> getSequenceConstraints() {
		return sequenceConstraints;
	}

	public final Set<NegationConstraint> getNegationConstraints() {
		return negationConstraints;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((allConstraints == null) ? 0 : allConstraints.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ConstraintsSet)) {
			return false;
		}
		ConstraintsSet other = (ConstraintsSet) obj;
		if (allConstraints == null) {
			if (other.allConstraints != null) {
				return false;
			}
		} else if (!allConstraints.equals(other.allConstraints)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ConstraintsSet [" + allConstraints + "]";
	}

}
