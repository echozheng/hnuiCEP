package cn.edu.hnu.icep.eval;

class EvalStatistics {
	private final int realEvents;
	private final int detectedEvents;
	private final int falsePositives;
	private final int falseNegatives;
	private final double precision;
	private final double recall;

	EvalStatistics(int realEvents, int detectedEvents, int falsePositives,
			int falseNegatives) {
		this.realEvents = realEvents;
		this.detectedEvents = detectedEvents;
		this.falsePositives = falsePositives;
		this.falseNegatives = falseNegatives;

		int truePositives = detectedEvents - falsePositives;

		precision = detectedEvents > 0 ? (double) truePositives
				/ detectedEvents : 0;
		recall = realEvents > 0 ? (double) truePositives / realEvents : 0;
	}

	final int getRealEvents() {
		return realEvents;
	}

	final int getDetectedEvents() {
		return detectedEvents;
	}

	final int getFalsePositives() {
		return falsePositives;
	}

	final int getFalseNegatives() {
		return falseNegatives;
	}

	final double getPrecision() {
		return precision;
	}

	final double getRecall() {
		return recall;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + detectedEvents;
		result = prime * result + falseNegatives;
		result = prime * result + falsePositives;
		long temp;
		temp = Double.doubleToLongBits(precision);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + realEvents;
		temp = Double.doubleToLongBits(recall);
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
		if (!(obj instanceof EvalStatistics)) {
			return false;
		}
		EvalStatistics other = (EvalStatistics) obj;
		if (detectedEvents != other.detectedEvents) {
			return false;
		}
		if (falseNegatives != other.falseNegatives) {
			return false;
		}
		if (falsePositives != other.falsePositives) {
			return false;
		}
		if (Double.doubleToLongBits(precision) != Double
				.doubleToLongBits(other.precision)) {
			return false;
		}
		if (realEvents != other.realEvents) {
			return false;
		}
		if (Double.doubleToLongBits(recall) != Double
				.doubleToLongBits(other.recall)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Real Events=" + realEvents + ", Detected Events="
				+ detectedEvents + ", False Positives=" + falsePositives
				+ ", False Negatives=" + falseNegatives + ", Precision="
				+ precision + ", Recall=" + recall;
	}
}
