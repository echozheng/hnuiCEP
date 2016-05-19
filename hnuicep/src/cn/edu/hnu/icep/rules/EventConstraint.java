package cn.edu.hnu.icep.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.edu.hnu.icep.event.model.Event;
import cn.edu.hnu.icep.rules.filtering.Predicate;

/**
 * An EventConstraint requires a certain event to be present in the history of
 * events.
 * 
 * @author hduser
 */
public class EventConstraint extends RuleConstraint {

	private final Predicate predicate;

	public EventConstraint(Predicate predicate) {
		super();
		this.predicate = predicate;
	}

	public Predicate getPredicate() {
		return predicate;
	}

	public final boolean isSatisfiedBy(Event event) {
		return predicate.isSatisfiedBy(event);
	}

	public final List<Event> getSatisfyingEvents(Collection<Event> events) {
		List<Event> satisfyingEvents = new ArrayList<Event>();
		for (Event event : events) {
			if (predicate.isSatisfiedBy(event)) {
				satisfyingEvents.add(event);
			}
		}
		return satisfyingEvents;
	}

	/**
	 *@param event 将要做判断的事件
	 *判断event 是否满足 EventConstraint
	 * **/
	public final boolean isEventSatisfied(Event event) {
		if (this.predicate.isSatisfiedBy(event)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isIdenticalTo(RuleConstraint constraint) {
		if (!(constraint instanceof EventConstraint)) {
			return false;
		}
		
		EventConstraint other = (EventConstraint) constraint;
		return (predicate.equals(other.predicate));
	}

	@Override
	public String toString() {
		return predicate.toString();
	}
	
	//测试用的 输出方法
	public String toString2() {
		return predicate.getEventType();
	}
	
	/**
	 * 根据EventConstraint的 id 值（调用父类的 compare方法）
	 * 判断是否为同一个约束
	 * @return 如果compare的返回结果为0，返回true
	 * **/
	public boolean equals(EventConstraint eventConstraint) {
		int result = this.compareTo(eventConstraint);
		if(result == 0) {
			return true;
		}
		return false;
	}
}
