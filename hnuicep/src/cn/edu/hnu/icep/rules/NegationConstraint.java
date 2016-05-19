package cn.edu.hnu.icep.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.edu.hnu.icep.event.model.Event;
import cn.edu.hnu.icep.rules.filtering.Predicate;

/**
 * A negation constraint requires an event not to be present in the history of
 * events.
 * 
 * @author hduser
 */
public class NegationConstraint extends RuleConstraint {

	private final Predicate predicate;

	public NegationConstraint(Predicate predicate) {
		super();
		this.predicate = predicate;
	}

	public final Predicate getPredicate() {
		return predicate;
	}

	/**
	 * 从一条positive事件中返回满足该 negationConstraint约束条件的事件
	 * @param events => 一条positive trace中的原子事件集合
	 * */
	public final List<Event> getConstraintViolatingEvents(Collection<Event> events) {

		List<Event> constraintViolatingEvents = new ArrayList<Event>();
		for (Event event : events) {
			if (predicate.isSatisfiedBy(event)) {
				constraintViolatingEvents.add(event);
			}
		}
		return constraintViolatingEvents;
	}

	@Override
	public boolean isIdenticalTo(RuleConstraint constraint) {
		if (!(constraint instanceof NegationConstraint)) {
			return false;
		}
		NegationConstraint neg = (NegationConstraint) constraint;
		return predicate.equals(neg.predicate);
	}

	@Override
	public String toString() {
		return predicate.toString();
	}

}
