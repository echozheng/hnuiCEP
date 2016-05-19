package cn.edu.hnu.icep.event.model;

public class Value {
	
	private final ValueType valueType;
	private final String stringValue;
	private final double doubleValue;
	private final long longValue;
	
	public Value(Value value) {
		this.valueType = value.valueType;
		this.stringValue = value.stringValue;
		this.doubleValue = value.doubleValue;
		this.longValue = value.longValue;
	}
	
	public Value(String stringValue) {
		this.valueType = ValueType.STRING;
		this.stringValue = stringValue;
		
		this.doubleValue = 0.0;
		this.longValue = 0;
	}
	
	public Value(Double doubleValue) {
		this.valueType = ValueType.DOUBLE;
		this.doubleValue = doubleValue;
		
		this.stringValue = null;
		this.longValue = 0;
	}
	
	public Value(long longValue) {
		this.valueType = ValueType.LONG;
		this.longValue = longValue;
		
		this.stringValue = null;
		this.doubleValue = 0.0;
	}
	
	//因为变量用 final关键字修饰，所以没有setter方法
	public ValueType getValueType() {
		return valueType;
	}

	public String getStringValue() {
		return stringValue;
	}

	public double getDoubleValue() {
		return doubleValue;
	}

	public long getLongValue() {
		return longValue;
	}

	@Override
	public String toString() {
		switch(valueType) {
		case LONG:
			return String.valueOf(this.longValue);
		case DOUBLE:
			return String.valueOf(this.doubleValue);
		case STRING:
			return String.valueOf(this.stringValue);
		default:
			return "typeErr!!!";
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(doubleValue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (longValue ^ (longValue >>> 32));
		result = prime * result + ((stringValue == null) ? 0 : stringValue.hashCode());
		result = prime * result + ((valueType == null) ? 0 : valueType.hashCode());
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
		Value other = (Value) obj;
		if (Double.doubleToLongBits(doubleValue) != Double.doubleToLongBits(other.doubleValue))
			return false;
		if (longValue != other.longValue)
			return false;
		if (stringValue == null) {
			if (other.stringValue != null)
				return false;
		} else if (!stringValue.equals(other.stringValue))
			return false;
		if (valueType != other.valueType)
			return false;
		return true;
	}
	
}
