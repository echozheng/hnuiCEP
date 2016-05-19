package cn.edu.hnu.icep.eval;

import cn.edu.hnu.icep.common.Consts;
import cn.edu.hnu.icep.common.Distribution;
import cn.edu.hnu.icep.learning.positive_instances.WindowLearningType;
import cn.edu.hnu.icep.workload.history_generation.HistoryGeneratorType;
import cn.edu.hnu.icep.workload.rule_generation.RuleGeneratorType;

import java.util.Random;

//final关键字修饰类，则此类不能被继承，而且该类的所有方法都默认被final修饰。
//final关键字修饰方法，且类没有被final修饰，则这个方法在此类的子类中不能被重写。
//对于一个final变量，如果是基本数据类型的变量，则其数值一旦在初始化之后便不能更改；
//如果是引用类型的变量，则在对其初始化之后便不能再让其指向另一个对象。
/**
 * 设置该系统的一些基本变量，例如： numEventTypes,minNumAttributes,maxNumAttribtes,
 * numValues,numEventContraints 等等
 * **/
public final class ParamHandler {

	public ParamHandler(int threadId) {
		random = new Random();

		this.threadId = threadId;

		resetToDefault();
	}

	private void resetToDefault() {
		// 事件类型个数
		numEventTypes = 25;
		// 每个event所拥有最少的属性
		minNumAttributes = 3;
		// 每个event所拥有最多的属性
		maxNumAttributes = 3;

		numValues = 100;

		valueDistribution = Distribution.UNIFORM;

		ruleGeneratorType = RuleGeneratorType.RANDOM;

		// 每条rule包含的事件类型约束的个数（也就是每条rule包含几种事件类型）
		numEventConstraints = 3;

		// 每条rule包含的 NegationConstraints的个数
		numNegationConstraints = 0;
		numAggregateConstraints = 0;

		numEventParameterConstraints = 0;
		numNegationParameterConstraints = 0;
		numAggregateParameterConstraints = 0;

		numConstraints = 3;

		// 与numAggregateConstraints有何不同？？？
		// 不同之处在于：numConstraintsInAggregates 是指一个AggregateConstraints 中包含几个约束。
		numConstraintsInAggregates = 0;

		numFilters = 1;
		numFiltersInAggregates = 0;

		// 最小窗口
		minWinSize = 8;
		// 最大窗口
		maxWinSize = 12;
		winDistribution = Distribution.UNIFORM;

		percEQ = 20;
		percLT = 40;
		percGT = 40;
		percDF = 0;

		seqProbability = 0;

		paramPercEQ = 100;
		paramPercLT = 0;
		paramPercGT = 0;
		paramPercDF = 0;

		aggPercEQ = 20;
		aggPercLT = 40;
		aggPercGT = 40;
		aggPercDF = 0;

		numEventsInHistory = Consts.HISTORY_SIZE;
		historyGeneratorType = HistoryGeneratorType.RANDOM;
		eventTypesDistribution = Distribution.UNIFORM;

		// 原子事件之间or复杂事件之间发生的最小间隙
		minDistanceBetweenEvents = 1;
		// 原子事件之间or复杂事件之间发生的最大间隙
		maxDistanceBetweenEvents = 2;

		distanceBetweenEventsDistribution = Distribution.UNIFORM;

		minDistanceBetweenEventsOfType = 1;
		maxDistanceBetweenEventsOfType = 50;

		distanceBetweenEventsOfTypeDistribution = Distribution.UNIFORM;
		numArtificialPrimitiveEventsInHistory = Consts.NUM_ARTIFICIAL_COMPOSITE_EVENTS;
		numArtificialNegationsInHistory = Consts.NUM_ARTIFICIAL_NEGATIONS;

		distanceBetweenNoiseEvents = 3;
		// ？？？
		minNumEventsPerAggregate = 2;
		maxNumEventsPerAggregate = 5;

		minWindowForEvaluation = 1;
		maxWindowForEvaluation = 50;

		// 窗口类型，默认取最大值
		winLearnerType = WindowLearningType.TAKE_MAXIMUM;
		winStepForEvaluation = 1;
		winStableAfter = 2;

		// 判断param约束是否是 rule中的一部分的 频度约束
		equalityParameterFrequencyThreshold = 0.95;
		equalityAggregateFrequencyThreshold = 0.95;
		negationFrequencyThreshold = 0.8;

		dublinkedTrainingFile = "Input/training.csv";
		dublinkedEvaluationFile = "Input/evaluation.csv";
		dublinkedNumLines = 10000;
	}

	public final void setSeed(int seed) {
		this.seed = seed;
		random.setSeed(seed);
	}

	public final int getSeed() {
		return seed;
	}

	public final int getThreadId() {
		return threadId;
	}

	public final Random getRandom() {
		return random;
	}

	public final int getNumEventTypes() {
		return numEventTypes;
	}

	public final void setNumEventTypes(int numEventTypes) {
		this.numEventTypes = numEventTypes;
	}

	public final int getMinNumAttributes() {
		return minNumAttributes;
	}

	public final void setMinNumAttributes(int minNumAttributes) {
		this.minNumAttributes = minNumAttributes;
	}

	public final int getMaxNumAttributes() {
		return maxNumAttributes;
	}

	public final void setMaxNumAttributes(int maxNumAttributes) {
		this.maxNumAttributes = maxNumAttributes;
	}

	public final Distribution getValueDistribution() {
		return valueDistribution;
	}

	public final void setValueDistribution(Distribution valueDistribution) {
		this.valueDistribution = valueDistribution;
	}

	public final int getNumValues() {
		return numValues;
	}

	public final void setNumValues(int numValues) {
		this.numValues = numValues;
	}

	public final RuleGeneratorType getRuleGeneratorType() {
		return ruleGeneratorType;
	}

	public final void setRuleGeneratorType(RuleGeneratorType ruleGeneratorType) {
		this.ruleGeneratorType = ruleGeneratorType;
	}

	public final int getNumEventConstraints() {
		return numEventConstraints;
	}

	public final void setNumEventConstraints(int numEventConstraints) {
		this.numEventConstraints = numEventConstraints;
	}

	public final int getNumNegationConstraints() {
		return numNegationConstraints;
	}

	public final void setNumNegationConstraints(int numNegationConstraints) {
		this.numNegationConstraints = numNegationConstraints;
	}

	public final int getNumAggregateConstraints() {
		return numAggregateConstraints;
	}

	public final void setNumAggregateConstraints(int numAggregateConstraints) {
		this.numAggregateConstraints = numAggregateConstraints;
	}

	public final int getNumEventParameterConstraints() {
		return numEventParameterConstraints;
	}

	public final void setNumEventParameterConstraints(
			int numEventParameterConstraints) {
		this.numEventParameterConstraints = numEventParameterConstraints;
	}

	public final int getNumNegationParameterConstraints() {
		return numNegationParameterConstraints;
	}

	public final void setNumNegationParameterConstraints(
			int numNegationParameterConstraints) {
		this.numNegationParameterConstraints = numNegationParameterConstraints;
	}

	public final int getNumAggregateParameterConstraints() {
		return numAggregateParameterConstraints;
	}

	public final void setNumAggregateParameterConstraints(
			int numAggregateParameterConstraints) {
		this.numAggregateParameterConstraints = numAggregateParameterConstraints;
	}

	public final int getNumConstraints() {
		return numConstraints;
	}

	public final void setNumConstraints(int numConstraints) {
		this.numConstraints = numConstraints;
	}

	public final int getNumConstraintsInAggregates() {
		return numConstraintsInAggregates;
	}

	public final void setNumConstraintsInAggregates(
			int numConstraintsInAggregates) {
		this.numConstraintsInAggregates = numConstraintsInAggregates;
	}

	public final int getNumFilters() {
		return numFilters;
	}

	public final void setNumFilters(int numFilters) {
		this.numFilters = numFilters;
	}

	public final int getNumFiltersInAggregates() {
		return numFiltersInAggregates;
	}

	public final void setNumFiltersInAggregates(int numFiltersInAggregates) {
		this.numFiltersInAggregates = numFiltersInAggregates;
	}

	public final long getMinWinSize() {
		return minWinSize;
	}

	public final void setMinWinSize(long minWinSize) {
		this.minWinSize = minWinSize;
	}

	public final long getMaxWinSize() {
		return maxWinSize;
	}

	public final void setMaxWinSize(long maxWinSize) {
		this.maxWinSize = maxWinSize;
		maxWindowForEvaluation = maxWinSize * 2;
	}

	public final Distribution getWinDistribution() {
		return winDistribution;
	}

	public final void setWinDistribution(Distribution winDistribution) {
		this.winDistribution = winDistribution;
	}

	public final int getPercEQ() {
		return percEQ;
	}

	public final void setPercEQ(int percEQ) {
		this.percEQ = percEQ;
	}

	public final int getPercLT() {
		return percLT;
	}

	public final void setPercLT(int percLT) {
		this.percLT = percLT;
	}

	public final int getPercGT() {
		return percGT;
	}

	public final void setPercGT(int percGT) {
		this.percGT = percGT;
	}

	public final int getPercDF() {
		return percDF;
	}

	public final void setPercDF(int percDF) {
		this.percDF = percDF;
	}

	public final int getSeqProbability() {
		return seqProbability;
	}

	public final int getParamPercEQ() {
		return paramPercEQ;
	}

	public final void setParamPercEQ(int paramPercEQ) {
		this.paramPercEQ = paramPercEQ;
	}

	public final int getParamPercLT() {
		return paramPercLT;
	}

	public final void setParamPercLT(int paramPercLT) {
		this.paramPercLT = paramPercLT;
	}

	public final int getParamPercGT() {
		return paramPercGT;
	}

	public final void setParamPercGT(int paramPercGT) {
		this.paramPercGT = paramPercGT;
	}

	public final int getParamPercDF() {
		return paramPercDF;
	}

	public final void setParamPercDF(int paramPercDF) {
		this.paramPercDF = paramPercDF;
	}

	public final int getAggPercEQ() {
		return aggPercEQ;
	}

	public final void setAggPercEQ(int aggPercEQ) {
		this.aggPercEQ = aggPercEQ;
	}

	public final int getAggPercLT() {
		return aggPercLT;
	}

	public final void setAggPercLT(int aggPercLT) {
		this.aggPercLT = aggPercLT;
	}

	public final int getAggPercGT() {
		return aggPercGT;
	}

	public final void setAggPercGT(int aggPercGT) {
		this.aggPercGT = aggPercGT;
	}

	public final int getAggPercDF() {
		return aggPercDF;
	}

	public final void setAggPercDF(int aggPercDF) {
		this.aggPercDF = aggPercDF;
	}

	public final void setSeqProbability(int seqProbability) {
		this.seqProbability = seqProbability;
	}

	public final int getNumEventsInHistory() {
		return numEventsInHistory;
	}

	public final void setNumEventsInHistory(int numEventsInHistory) {
		this.numEventsInHistory = numEventsInHistory;
	}

	public final HistoryGeneratorType getHistoryGeneratorType() {
		return historyGeneratorType;
	}

	public final void setHistoryGeneratorType(
			HistoryGeneratorType historyGeneratorType) {
		this.historyGeneratorType = historyGeneratorType;
	}

	public final Distribution getEventTypesDistribution() {
		return eventTypesDistribution;
	}

	public final void setEventTypesDistribution(
			Distribution eventTypesDistribution) {
		this.eventTypesDistribution = eventTypesDistribution;
	}

	public final long getMinDistanceBetweenEvents() {
		return minDistanceBetweenEvents;
	}

	public final void setMinDistanceBetweenEvents(long minDistanceBetweenEvents) {
		this.minDistanceBetweenEvents = minDistanceBetweenEvents;
	}

	public final long getMaxDistanceBetweenEvents() {
		return maxDistanceBetweenEvents;
	}

	public final void setMaxDistanceBetweenEvents(long maxDistanceBetweenEvents) {
		this.maxDistanceBetweenEvents = maxDistanceBetweenEvents;
	}

	public final Distribution getDistanceBetweenEventsDistribution() {
		return distanceBetweenEventsDistribution;
	}

	public final void setDistanceBetweenEventsDistribution(
			Distribution distanceBetweenEventsDistribution) {
		this.distanceBetweenEventsDistribution = distanceBetweenEventsDistribution;
	}

	public final long getMinDistanceBetweenEventsOfType() {
		return minDistanceBetweenEventsOfType;
	}

	public final void setMinDistanceBetweenEventsOfType(
			long minDistanceBetweenEventsOfType) {
		this.minDistanceBetweenEventsOfType = minDistanceBetweenEventsOfType;
	}

	public final long getMaxDistanceBetweenEventsOfType() {
		return maxDistanceBetweenEventsOfType;
	}

	public final void setMaxDistanceBetweenEventsOfType(
			long maxDistanceBetweenEventsOfType) {
		this.maxDistanceBetweenEventsOfType = maxDistanceBetweenEventsOfType;
	}

	public final Distribution getDistanceBetweenEventsOfTypeDistribution() {
		return distanceBetweenEventsOfTypeDistribution;
	}

	public final void setDistanceBetweenEventsOfTypeDistribution(
			Distribution distanceBetweenEventsOfType) {
		distanceBetweenEventsOfTypeDistribution = distanceBetweenEventsOfType;
	}

	public final int getNumArtificialPrimitiveEventsInHistory() {
		return numArtificialPrimitiveEventsInHistory;
	}

	public final void setNumArtificialPrimitiveEventsInHistory(
			int numArtificialPrimitiveEventsInHistory) {
		this.numArtificialPrimitiveEventsInHistory = numArtificialPrimitiveEventsInHistory;
	}

	public final int getNumArtificialNegationsInHistory() {
		return numArtificialNegationsInHistory;
	}

	public final void setNumArtificialNegationsInHistory(
			int numArtificialNegationsInHistory) {
		this.numArtificialNegationsInHistory = numArtificialNegationsInHistory;
	}

	public final int getDistanceBetweenNoiseEvents() {
		return distanceBetweenNoiseEvents;
	}

	public final void setDistanceBetweenNoiseEvents(
			int distanceBetweenNoiseEvents) {
		this.distanceBetweenNoiseEvents = distanceBetweenNoiseEvents;
	}

	public final long getMinWindowForEvaluation() {
		return minWindowForEvaluation;
	}

	public final void setMinWindowForEvaluation(long minWindowForEvaluation) {
		this.minWindowForEvaluation = minWindowForEvaluation;
	}

	public final int getMinNumEventsPerAggregate() {
		return minNumEventsPerAggregate;
	}

	public final void setMinNumEventsPerAggregate(int minNumEventsPerAggregate) {
		this.minNumEventsPerAggregate = minNumEventsPerAggregate;
	}

	public final int getMaxNumEventsPerAggregate() {
		return maxNumEventsPerAggregate;
	}

	public final void setMaxNumEventsPerAggregate(int maxNumEventsPerAggregate) {
		this.maxNumEventsPerAggregate = maxNumEventsPerAggregate;
	}

	public final long getMaxWindowForEvaluation() {
		return maxWindowForEvaluation;
	}

	public final void setMaxWindowForEvaluation(long maxWindowForEvaluation) {
		this.maxWindowForEvaluation = maxWindowForEvaluation;
	}

	public final WindowLearningType getWinLearnerType() {
		return winLearnerType;
	}

	public final void setWinLearnerType(WindowLearningType winLearnerType) {
		this.winLearnerType = winLearnerType;
	}

	public final long getWinStepForEvaluation() {
		return winStepForEvaluation;
	}

	public final void setWinStepForEvaluation(long winStepForEvaluation) {
		this.winStepForEvaluation = winStepForEvaluation;
	}

	public final int getWinStableAfter() {
		return winStableAfter;
	}

	public final void setWinStableAfter(int winStableAfter) {
		this.winStableAfter = winStableAfter;
	}

	public final double getEqualityParameterFrequencyThreshold() {
		return equalityParameterFrequencyThreshold;
	}

	public final void setEqualityParameterFrequencyThreshold(
			double equalityParameterFrequencyThreshold) {
		this.equalityParameterFrequencyThreshold = equalityParameterFrequencyThreshold;
	}

	public final double getEqualityAggregateFrequencyThreshold() {
		return equalityAggregateFrequencyThreshold;
	}

	public final void setEqualityAggregateFrequencyThreshold(
			double equalityAggregateFrequencyThreshold) {
		this.equalityAggregateFrequencyThreshold = equalityAggregateFrequencyThreshold;
	}

	public double getNegationFrequencyThreshold() {
		return negationFrequencyThreshold;
	}

	public void setNegationFrequencyThreshold(double negationFrequencyThreshold) {
		this.negationFrequencyThreshold = negationFrequencyThreshold;
	}

	public final String getDublinkedTrainingFile() {
		return dublinkedTrainingFile;
	}

	public final void setDublinkedTrainingFile(String dublinkedTrainingFile) {
		this.dublinkedTrainingFile = dublinkedTrainingFile;
	}

	public final String getDublinkedEvaluationFile() {
		return dublinkedEvaluationFile;
	}

	public final void setDublinkedEvaluationFile(String dublinkedEvaluationFile) {
		this.dublinkedEvaluationFile = dublinkedEvaluationFile;
	}

	public final int getDublinkedNumLines() {
		return dublinkedNumLines;
	}

	public final void setDublinkedNumLines(int dublinkedNumLines) {
		this.dublinkedNumLines = dublinkedNumLines;
	}

	/**
	 * Parameters of the simulation
	 */
	private int seed;
	private final int threadId;
	private final Random random;

	/**
	 * Parameters of events
	 */
	private int numEventTypes;
	private int minNumAttributes;
	private int maxNumAttributes;
	private int numValues;
	private Distribution valueDistribution;

	/**
	 * Parameters of rules
	 */
	private RuleGeneratorType ruleGeneratorType;
	private int numEventConstraints;
	private int numNegationConstraints;
	private int numAggregateConstraints;
	private int numEventParameterConstraints;
	private int numNegationParameterConstraints;
	private int numAggregateParameterConstraints;
	private int numConstraints;
	private int numConstraintsInAggregates;
	private int numFilters;
	private int numFiltersInAggregates;
	private long minWinSize;
	private long maxWinSize;
	private Distribution winDistribution;
	private int percEQ;
	private int percLT;
	private int percGT;
	private int percDF;
	private int seqProbability;
	private int paramPercEQ;
	private int paramPercLT;
	private int paramPercGT;
	private int paramPercDF;
	private int aggPercEQ;
	private int aggPercLT;
	private int aggPercGT;
	private int aggPercDF;

	/**
	 * Parameters of history
	 */
	private int numEventsInHistory;
	private Distribution eventTypesDistribution;
	private HistoryGeneratorType historyGeneratorType;
	/** RandomHistoryGenerator specific parameters */
	private long minDistanceBetweenEvents;
	private long maxDistanceBetweenEvents;
	private Distribution distanceBetweenEventsDistribution;
	/** PeriodicTypeHistoryGenerator specific parameters */
	private long minDistanceBetweenEventsOfType;
	private long maxDistanceBetweenEventsOfType;
	private Distribution distanceBetweenEventsOfTypeDistribution;

	/** RuleGuidedHistoryGenerator specific parameters */
	private int numArtificialPrimitiveEventsInHistory;

	private int numArtificialNegationsInHistory;
	private int distanceBetweenNoiseEvents;
	private int minNumEventsPerAggregate;
	private int maxNumEventsPerAggregate;

	/**
	 * Evaluation parameters
	 */
	private long minWindowForEvaluation;
	private long maxWindowForEvaluation;
	private long winStepForEvaluation;
	private WindowLearningType winLearnerType;
	private int winStableAfter;
	private double equalityParameterFrequencyThreshold;
	private double equalityAggregateFrequencyThreshold;
	private double negationFrequencyThreshold;

	/**
	 * Dublinked-specific parameters
	 */
	private String dublinkedTrainingFile;
	private String dublinkedEvaluationFile;
	private int dublinkedNumLines;
}