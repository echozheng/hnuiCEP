package cn.edu.hnu.icep.event.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//使用迭代器模式
public class Event implements Iterable<Attribute> , Cloneable{

	private final String eventType;
	private final Map<String,Attribute> attributes;
	private final long timestamp;
	
	public Event(String eventType, long timestamp, Set<Attribute> attributes) {
	    this.eventType = eventType;
	    this.timestamp = timestamp;

	    this.attributes = new HashMap<String, Attribute>();
	    for (Attribute attr : attributes) {
	    	this.attributes.put(attr.getName(), attr);
	    }
    }
	
	public Event(String eventType,long timestamp,Attribute ...attributes) {
		this.eventType = eventType;
		this.timestamp = timestamp;
		this.attributes = new HashMap<String,Attribute>();
		for(Attribute attr : attributes) {
			this.attributes.put(attr.getName(),attr);
		}
	}
	
	public String getEventType() {
		return eventType;
	}

	public long getTimestamp() {
		return timestamp;
	}
	
	@Override
	public Iterator<Attribute> iterator() {
		return attributes.values().iterator();
	}
	
	/**
	 * @author hduser
	 * 返回事件的属性与属性值,调试只用**/
	public String getAttrNameAndValue() {
		String attrNameAndValue = "";
		Iterator<Attribute> eventAttrs = iterator();
		while(eventAttrs.hasNext()) {
			Attribute attr = eventAttrs.next();
			attrNameAndValue += attr.toString()+";";
		}
		if(attrNameAndValue == "") {
			//属性为空
			return "null";
		}
		//去掉最后一个 ';'
		String newAttrNameAndValue =  attrNameAndValue.substring(0, attrNameAndValue.length()-1);
		
		//System.out.println("attrNameAndValue's value: " + newAttrNameAndValue);
		return newAttrNameAndValue;
	}
	
	public boolean hasAttribute(String name) {
		return attributes.containsKey(name);
	}

	public boolean isType(String type) {
	    return this.eventType.equals(type);
	}
	
   /**
   * Returns the value of the attribute with the given name.
   * 
   * Requires hasAttribute(name)==true
   * 
   * @param name the name of the attribute
   * @return the value of the attribute with the given name
   */
	public Value getValueFor(String name) {
		return attributes.get(name).getValue();
	}
	
	/**
	 * 返回 该原子事件 所对应的全部的 属性名集合。
	 * **/
	public Set<String> getAttrNamesSet() {
		Set<String> attrNames = new HashSet<String>();
		for (Attribute attr : this) {
			attrNames.add(attr.getName());
		}
		return attrNames;
	}
	
	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
	    result = prime * result + ((eventType == null) ? 0 : eventType.hashCode());
	    result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
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
		Event other = (Event) obj;
		if (attributes == null) {
			if (other.attributes != null)
				return false;
		} else if (!attributes.equals(other.attributes))
			return false;
		if (eventType == null) {
			if (other.eventType != null)
				return false;
		} else if (!eventType.equals(other.eventType))
			return false;
		if (timestamp != other.timestamp)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return eventType + "@" + timestamp + attributes.values();
	}
	
}
