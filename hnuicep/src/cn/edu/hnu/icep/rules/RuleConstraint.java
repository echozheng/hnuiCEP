package cn.edu.hnu.icep.rules;

public abstract class RuleConstraint implements Comparable<RuleConstraint> {

	public static long lastId;
	public final long id;

	public RuleConstraint() {
		id = lastId++;
	}

	@Override
	public int compareTo(RuleConstraint o) {
		if (o.id == id) {
			return 0;
		} else if (o.id > id) {
			return 1;
		} else {
			return -1;
		}
	}

	/**
	 * Returns true if and only if this is identical to constraint.
	 * 
	 * We use this instead of the equals() method, since we consider two
	 * identical instances of a RuleConstraint a and b to be distinct (i.e.,
	 * a.equals(b) is false).
	 * 
	 * @param constraint
	 *            the constraint to check.
	 * @return if the constraint is identical to this.
	 */
	public abstract boolean isIdenticalTo(RuleConstraint constraint);

}
