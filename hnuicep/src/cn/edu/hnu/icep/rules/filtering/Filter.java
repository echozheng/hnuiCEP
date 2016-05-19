package cn.edu.hnu.icep.rules.filtering;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import cn.edu.hnu.icep.event.model.Event;

public class Filter implements Iterable<Constraint> {

	private final Set<Constraint> constraints;

	public Filter(Set<Constraint> constraints) {
		this.constraints = constraints;
	}

	public Filter(Constraint... constraints) {
		this.constraints = new HashSet<Constraint>();
		for (Constraint constr : constraints) {
			this.constraints.add(constr);
		}
	}

	public final boolean isSatisfiedBy(Event event) {
		for (Constraint c : constraints) {
			if (!c.isSatisfiedBy(event))
				return false;
		}
		return true;
	}

	public final boolean hasConstraints() {
		return !constraints.isEmpty();
	}

	public final int getNumConstraints() {
		return constraints.size();
	}

	public final boolean covers(Filter filter) {
		for (Constraint filterConstr : filter) {

			for (Constraint myConstr : constraints) {
				if (!filterConstr.covers(myConstr)) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public Iterator<Constraint> iterator() {
		// TODO Auto-generated method stub
		return constraints.iterator();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((constraints == null) ? 0 : constraints.hashCode());
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
		Filter other = (Filter) obj;
		if (constraints == null) {
			if (other.constraints != null)
				return false;
		} else if (!constraints.equals(other.constraints))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String name = "";
		boolean first = true;
		for (Constraint c : constraints) {
			if (first) {
				name = name + "(";
				first = false;
			} else {
				name = name + " AND ";
			}
			name += c;
		}
		if (!first) {
			name += ")";
		}
		return name;
	}
}
