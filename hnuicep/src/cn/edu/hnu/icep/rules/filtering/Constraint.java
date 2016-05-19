package cn.edu.hnu.icep.rules.filtering;

import cn.edu.hnu.icep.common.ConstraintOp;
import cn.edu.hnu.icep.event.model.Event;
import cn.edu.hnu.icep.event.model.Value;

public class Constraint {

	// 属性的名字
	private final String name;
	
	//属性与值的关系
	private final ConstraintOp op;
	
	//属性值
	private final Value value;

	public Constraint(String name, ConstraintOp op, Value value) {
		this.name = name;
		this.op = op;
		this.value = value;
	}

	public Constraint(String name, ConstraintOp op, long value) {
		this(name, op, new Value(value));
	}

	public Constraint(String name, ConstraintOp op, double value) {
		this(name, op, new Value(value));
	}

	public Constraint(String name, ConstraintOp op, String value) {
		this(name, op, new Value(value));
	}

	public Constraint(String name) {
		this(name, ConstraintOp.ANY, new Value(0));
	}

	public final String getName() {
		return name;
	}

	public final Value getValue() {
		return value;
	}

	public final ConstraintOp getOperator() {
		return op;
	}

	/**
	 * @author hduser
	 * **/
	public boolean isSatisfiedBy(Event event) {
		if (!event.hasAttribute(name)) {
			return false;
		}
		Value val = event.getValueFor(name);
		if (!val.getValueType().equals(value.getValueType())) {
			return false;
		}

		switch (op) {
		case EQ:
			switch (val.getValueType()) {
			case LONG:
				return val.getLongValue() == value.getLongValue();
			case DOUBLE:
				return val.getDoubleValue() == value.getDoubleValue();
			case STRING:
				return val.getStringValue().equals(value.getStringValue());
			default:
				return false;
			}
		case GT_EQ:
			switch (val.getValueType()) {
			case LONG:
				return val.getLongValue() >= value.getLongValue();
			case DOUBLE:
				return val.getDoubleValue() >= value.getDoubleValue();
			case STRING:
				return false;
			default:
				return false;
			}
		case LT_EQ:
			switch (val.getValueType()) {
			case LONG:
				return val.getLongValue() <= value.getLongValue();
			case DOUBLE:
				return val.getDoubleValue() <= value.getDoubleValue();
			case STRING:
				return false;
			default:
				return false;
			}
		case DF:
			switch (val.getValueType()) {
			case LONG:
				return val.getLongValue() != value.getLongValue();
			case DOUBLE:
				return val.getDoubleValue() != value.getDoubleValue();
			case STRING:
				return !val.getStringValue().equals(value.getStringValue());
			default:
				return false;
			}
		case ANY:
			return true;
		default:
			return false;
		}
	}

	/**
	 * 判断一个约束 是否覆盖一个约束
	 * @author hduser
	 * **/
	public final boolean covers(Constraint c) {
		if (!name.equals(c.name)) {
			return false;
		}
		if (!value.getValueType().equals(c.value.getValueType())) {
			return false;
		}
		switch (op) {
		case EQ:
			if (c.op == ConstraintOp.EQ) {
				switch (value.getValueType()) {
				case LONG:
					return value.getLongValue() == c.value.getLongValue();
				case DOUBLE:
					return value.getDoubleValue() == c.value.getDoubleValue();
				case STRING:
					return value.getStringValue().equals(
							c.value.getStringValue());
				default:
					return false;
				}
			}
			// 如果c.op 不是EQ，直接返回一个false
			return false;
		case GT_EQ:
			if (c.op == ConstraintOp.GT_EQ) {
				switch (value.getValueType()) {
				case LONG:
					return c.value.getLongValue() >= value.getLongValue();
				case DOUBLE:
					return c.value.getDoubleValue() >= value.getDoubleValue();
				default:
					return false;
				}
			}
			// 如果c.op 不是GT_EQ，直接返回一个false
			return false;
		case LT_EQ:
			if (c.op == ConstraintOp.LT_EQ) {
				switch (value.getValueType()) {
				case LONG:
					return c.value.getLongValue() <= value.getLongValue();
				case DOUBLE:
					return c.value.getDoubleValue() <= value.getDoubleValue();
				default:
					return false;
				}
			}
			// 如果c.op 不是LT_EQ，直接返回一个false
			return false;
		case ANY:
			return true;
		default:
			return false;
		}
	}

	@Override
	public String toString() {
		if (op == ConstraintOp.ANY) {
			return name + " " + op;
		} else {
			return name + " " + op + " " + value;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((op == null) ? 0 : op.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		if (!(obj instanceof Constraint)) {
			return false;
		}
		Constraint other = (Constraint) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (op != other.op) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}
	
}
