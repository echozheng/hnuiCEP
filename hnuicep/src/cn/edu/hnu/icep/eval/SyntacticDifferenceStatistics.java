package cn.edu.hnu.icep.eval;

/**
 * Stores the syntactic differences between the hidden rule and the derived one.
 * Each difference is encoded as a real number. 0 indicates no differences.
 */
public class SyntacticDifferenceStatistics {

	private final double typeDistance;
	private final double attributeDistance;
	private final double predicateDistance;
	private final double winDistance;
	private final double parameterDistance;
	private final double aggregateDistance;
	private final double sequenceDistance;
	private final double negationDistance;

	public SyntacticDifferenceStatistics(double typeDistance,
			double attributeDistance, double predicateDistance,
			double winDistance, double parameterDistance,
			double aggregateDistance, double sequenceDistance,
			double negationDistance) {
		this.typeDistance = typeDistance;
		this.attributeDistance = attributeDistance;
		this.predicateDistance = predicateDistance;
		this.winDistance = winDistance;
		this.parameterDistance = parameterDistance;
		this.aggregateDistance = aggregateDistance;
		this.sequenceDistance = sequenceDistance;
		this.negationDistance = negationDistance;
	}

	public final double getTypeDistance() {
		return typeDistance;
	}

	public final double getAttributeDistance() {
		return attributeDistance;
	}

	public final double getPredicateDistance() {
		return predicateDistance;
	}

	public final double getWinDistance() {
		return winDistance;
	}

	public final double getParameterDistance() {
		return parameterDistance;
	}

	public final double getSequenceDistance() {
		return sequenceDistance;
	}

	public final double getNegationDistance() {
		return negationDistance;
	}

	public final String printValues() {
		return typeDistance + "\t" + attributeDistance + "\t"
				+ predicateDistance + "\t" + winDistance + "\t"
				+ parameterDistance + "\t" + aggregateDistance + "\t"
				+ sequenceDistance + "\t" + negationDistance;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(attributeDistance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(negationDistance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(parameterDistance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(predicateDistance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(sequenceDistance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(typeDistance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(winDistance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		if (!(obj instanceof SyntacticDifferenceStatistics)) {
			return false;
		}
		SyntacticDifferenceStatistics other = (SyntacticDifferenceStatistics) obj;
		if (Double.doubleToLongBits(attributeDistance) != Double
				.doubleToLongBits(other.attributeDistance)) {
			return false;
		}
		if (Double.doubleToLongBits(negationDistance) != Double
				.doubleToLongBits(other.negationDistance)) {
			return false;
		}
		if (Double.doubleToLongBits(parameterDistance) != Double
				.doubleToLongBits(other.parameterDistance)) {
			return false;
		}
		if (Double.doubleToLongBits(predicateDistance) != Double
				.doubleToLongBits(other.predicateDistance)) {
			return false;
		}
		if (Double.doubleToLongBits(sequenceDistance) != Double
				.doubleToLongBits(other.sequenceDistance)) {
			return false;
		}
		if (Double.doubleToLongBits(typeDistance) != Double
				.doubleToLongBits(other.typeDistance)) {
			return false;
		}
		if (Double.doubleToLongBits(winDistance) != Double
				.doubleToLongBits(other.winDistance)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "SyntacticDifferenceStatistics [typeDistance=" + typeDistance
				+ ", attributeDistance=" + attributeDistance
				+ ", predicateDistance=" + predicateDistance + ", winDistance="
				+ winDistance + ", parameterDistance=" + parameterDistance
				+ ", aggregateDistance=" + aggregateDistance
				+ ", sequenceDistance=" + sequenceDistance
				+ ", negationDistance=" + negationDistance + "]";
	}

}
