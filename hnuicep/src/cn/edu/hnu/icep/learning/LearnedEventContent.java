package cn.edu.hnu.icep.learning;

import cn.edu.hnu.icep.event.model.Event;

import java.util.HashSet;
import java.util.Set;

public class LearnedEventContent {

	private final String type;
	private final Set<String> attributes;

	public LearnedEventContent(String type, Set<String> attributes) {
		this.type = type;
		this.attributes = attributes;
	}

	public LearnedEventContent(String type, String... attributes) {
		this.type = type;
		this.attributes = new HashSet<String>();
		for (String attribute : attributes) {
			this.attributes.add(attribute);
		}
	}

	/**
	 * 先判断事件类型是否相同，如果不同，返回false。 再判断，前者的属性是否包含后者的全部属性。包含，则返回true，否则返回false
	 **/
	public final boolean implies(LearnedEventContent eventContent) {
		if (!type.equals(eventContent.type))
			return false;
		return (attributes.containsAll(eventContent.attributes));
	}

	public final Set<String> getAttributes() {
		return attributes;
	}

	/**
	 * 如果Event e 包含了调用者（事件类型相同，并且属性 >= 调用者），返回true， 否则返回false
	 * **/
	public final boolean matches(Event e) {
		if (!e.getEventType().equals(type))
			return false;
		for (String name : attributes) {
			if (!e.hasAttribute(name))
				return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		if (!(obj instanceof LearnedEventContent)) {
			return false;
		}
		LearnedEventContent other = (LearnedEventContent) obj;
		// 两者所包含的属性集合 完全相同(对属性的值，没有要求)，返回true
		return this.implies(other) && other.implies(this);
	}

	@Override
	public String toString() {
		return "[Type=" + type + ", attributes=" + attributes + "]";
	}
}
