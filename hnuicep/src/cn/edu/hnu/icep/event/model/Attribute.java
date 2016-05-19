package cn.edu.hnu.icep.event.model;

public class Attribute implements Cloneable{
	private final String name;
	private final Value value;
	
	public Attribute(String name,Value value) {
		this.name = name;
		this.value = value;
	}
	
	public Attribute(String name,String value) {
		this.name = name;
		this.value = new Value(value);
	}
	
	public Attribute(String name,double value) {
		this.name = name;
		this.value = new Value(value);
	} 
	
	public Attribute(String name,long value) {
		this.name = name;
		this.value = new Value(value);
	}
	
	public String getName() {
		return name;
	}
	
	public Value getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		Attribute other = (Attribute) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return name + "=" + value.toString();
	}
	
	@Override
	public Object clone() {
		Attribute o = null;
		try{
			o = (Attribute)super.clone();
		} catch(CloneNotSupportedException ex) {
			ex.printStackTrace();
		}
		return o;
	}
}
