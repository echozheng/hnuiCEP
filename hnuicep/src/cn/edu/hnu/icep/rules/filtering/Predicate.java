package cn.edu.hnu.icep.rules.filtering;

import java.util.List;

import cn.edu.hnu.icep.event.model.Event;

public class Predicate {

	private final String eventType;

	/**与作者的不同之处，修改了Filter变量的个数。原文使用一个List，
	 *而按照我目前的观察，一个Filter对象即可。
	 **/
	private final Filter filter;

	public Predicate(String eventType, Filter filter) {
		this.eventType = eventType;
		this.filter = filter;
	}
	
	public Predicate(String eventType, List<Filter> filters) {
		this.eventType = eventType;
		//只取第一个 filter
		this.filter = filters.get(0);
	}

	public String getEventType() {
		return eventType;
	}
	
	public Filter getFilter() {
		return this.filter;
	}

	public final boolean isSatisfiedBy(Event event) {
		//首先判断事件类型 是否相同
		if (!event.getEventType().equals(eventType)) {
			return false;
		}
		
		//filter为空，返回true
		if(filter == null) {
			return true;
		}

		// 如果filter中没有任何约束，那么返回true
		if (!filter.hasConstraints()) {
			return true;
		}

		if (filter.isSatisfiedBy(event)) {
			return true;
		}
		
		return false;
	}

	public final boolean covers(Predicate pred) {
		if (this.eventType != pred.eventType) {
			return false;
		}

		if (this.filter.covers(pred.filter)) {
			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((eventType == null) ? 0 : eventType.hashCode());
		result = prime * result + ((filter == null) ? 0 : filter.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Predicate other = (Predicate) obj;
		if (eventType == null) {
			if (other.eventType != null)
				return false;
		} else if (!eventType.equals(other.eventType))
			return false;
		if (filter == null) {
			if (other.filter != null)
				return false;
		} else if (!filter.equals(other.filter))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String name = String.valueOf(eventType);
		name = name + "[";
		boolean first = true;
		if (filter.hasConstraints()) {
			if (!first) {
				name += " OR ";
			}
			first = false;
		}
		name += filter;
		name = name + "]";
		return name;
	}
}
