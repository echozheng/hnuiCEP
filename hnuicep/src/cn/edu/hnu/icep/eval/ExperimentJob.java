package cn.edu.hnu.icep.eval;

import cn.edu.hnu.icep.eval.ParamHandler;
import cn.edu.hnu.icep.common.Consts;
import cn.edu.hnu.icep.history.History;
import cn.edu.hnu.icep.rules.Rule;
import cn.edu.hnu.icep.eval.EvalStatistics;
import cn.edu.hnu.icep.eval.SyntacticDifferenceStatistics;
import cn.edu.hnu.icep.learning.ConstraintsSet;
import cn.edu.hnu.icep.workload.rule_generation.RuleGenerator;
import cn.edu.hnu.icep.workload.history_generation.HistoryGeneratorType;
import cn.edu.hnu.icep.workload.use_cases.dublinked.DublinkedWLGen;
import cn.edu.hnu.icep.workload.history_generation.HistoryGenerator;
import cn.edu.hnu.icep.workload.history_evaluation.HistoryEvaluator;
import cn.edu.hnu.icep.learning.positive_instances.WindowLearner;
import cn.edu.hnu.icep.learning.positive_instances.FeaturesObserver;
import cn.edu.hnu.icep.learning.negative_instances.NegativeInstancesDetector;
import cn.edu.hnu.icep.eval.LearnedRuleGenerator;
import cn.edu.hnu.icep.rules.NegationConstraint;
import cn.edu.hnu.icep.learning.positive_instances.NegationLearner;
import cn.edu.hnu.icep.learning.sequence.SequenceExtractor;
import cn.edu.hnu.icep.eval.LearnedRuleEvaluator;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class ExperimentJob {

	private static final boolean considerEventContentConstraints = true;
	private static final boolean considerSequenceConstraints = true;
	private static final boolean considerNegationConstraints = true;
	private static final boolean examineNegativeExamples = false;

	private final ParamHandler ph;
	private final Logger logger;
	private final String label;
	private final String filename;

	public ExperimentJob(ParamHandler ph, String label, String filename) {
		this.ph = ph;
		logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		this.label = label;
		this.filename = filename;
	}

	public final void execute() {
		// 生成规则
		Rule rule = generateRule();
		logger.info("Real Rule: " + rule);

		// 产生原子事件历史记录
		History trainingHistory = generateTrainingHistory(rule);

		// 生成符合规则的 复杂事件
		evaluateHistory(rule, trainingHistory);

		logger.info("Running Learning Algorithms");
		ConstraintsSet constraints = new ConstraintsSet();

		// win为包含最多 (原子事件约束) 的窗口大小。
		long win = learnWindowSize(trainingHistory, constraints);

		// ////到此行代码为止，系统已经学习到 （事件类型约束，属性值约束，窗口大小）///////////////////
		/**
		 *在learnWindow()方法之后，约束类型只有EventConstraints
		 *learnedRule实例：
		 * Rule [win: 10 Event Constraints:[ 
		 * 	Event6[(Attribute2 = 34 AND Attribute1 = 54 AND Attribute0 >= 0 AND Attribute0 <= 99 )],
		 * 	Event12[(Attribute2 = 63 AND Attribute1 <= 99 AND Attribute1 >= 0 AND Attribute0 >= 0 AND Attribute0 <= 99)],
		 * 	Event7[(Attribute2 <= 99 AND Attribute2 >= 0 AND Attribute1 <= 99 AND Attribute1 >= 0 And Attribute0 <= 99 AND Attribute0 >= 0)]]]
		 **/

		// examinePositiveExamples方法，学习其它模块。
		// 1. Sequence => 事件顺序
		examinePositiveExamples(win, trainingHistory, constraints);

		// 默认examineNegativeExamples == false,选择后一个结果（这个方法未看,zql-2016-04-25）
		Set<ConstraintsSet> minConstraintsSets = examineNegativeExamples ?
				examineNegativeExamples(win, trainingHistory, constraints) : new HashSet<ConstraintsSet>();
				
		Rule learnedRule = generateLearnedRule(win, constraints);

		System.out.println("在调用generateLearnedRule(win,constraints)方法之后，产生的rule是:");
		System.out.println(learnedRule.toString());
		System.out.println();

		// 学习否定规则
		extractNegationConstraints(learnedRule, trainingHistory, constraints);
		System.out.println("在调用extractNegationConstraints()方法之后，产生的rule是:");
		logger.info(learnedRule.toString());
		
		// /////////////////////////对生成的规则进行评估/////////////////////////////////////////
		History evaluationHistory = generateEvaluationHistory(rule);
		EvalStatistics stats = evaluateLearnedRule(rule, learnedRule,evaluationHistory);
		//SyntacticDifferenceStatistics syntacticStats = getSynthacticDifferencesBetweenRules(rule, learnedRule, ph);

		/*try {
			FileOutputStream fos = new FileOutputStream(new File(Consts.RESULTS_PATH + filename + "_" + ph.getSeed()), true);
			*//** \\ 反斜杠 \a 警告 \b 退格 \f 换页 \n 换行 \r 回车 \t 跳格(tab) \v 垂直跳格 **//*
			String results = label + "\t" + stats.getRealEvents() + "\t"
								+ stats.getDetectedEvents() + "\t"
								+ stats.getFalsePositives() + "\t"
								+ stats.getFalseNegatives() + "\t" 
								+ stats.getPrecision() + "\t"
								+ stats.getRecall() + "\n";
			fos.write(results.getBytes());
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		
		//历史记录中的positive trace条数 与 合并之后positive trace的条数
		try {
			FileOutputStream fos = new FileOutputStream(new File(Consts.RESULTS_PATH + filename + "_" + label), true);
			/** \\ 反斜杠 \a 警告 \b 退格 \f 换页 \n 换行 \r 回车 \t 跳格(tab) \v 垂直跳格 **/
			String results = label + "\t" + SequenceExtractor.getAllBranchesInTree()+ "\t"
								+ SequenceExtractor.getNumOfPositiveTrace() + "\n";
			fos.write(results.getBytes());
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		/*try {
			FileOutputStream fos = new FileOutputStream(new File(Consts.RESULTS_PATH + filename + "_Log_" + ph.getSeed()),true);
			String results = label + "\t" + trainingHistory.getNumCompositeEvents() + "\t" + (endTime - startTime) + "\t" + minConstraintsSets.size() + "\n";
			fos.write(results.getBytes());
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			FileOutputStream fos = new FileOutputStream(new File(Consts.RESULTS_PATH + filename + "_Syntactic_" + ph.getSeed()), true);
			String results = label + "\t" + syntacticStats.printValues() + "\n";
			fos.write(results.getBytes());
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		  
	}

	private Rule generateRule() {
		logger.fine("Generating Rule");
		return RuleGenerator.getRule(ph);
	}

	private History generateTrainingHistory(Rule rule) {
		logger.info("Generating Training History");
		if (ph.getHistoryGeneratorType() == HistoryGeneratorType.DUBLINKED) {
			return DublinkedWLGen.generateTraningHistory(ph);
		} else {
			return generateHistory(rule);
		}
	}

	/**
	 * 生成历史记录,包括随机原子事件， rule_guided 与 rule_guided_negation 指导下生成的事件。
	 * **/
	private final History generateHistory(Rule rule) {
		History history = new History();

		// 产生随机原子事件
		logger.info("Adding random Events");
		ph.setHistoryGeneratorType(HistoryGeneratorType.RANDOM);
		HistoryGenerator.generateHistory(history, rule, ph);

		// 产生满足先前制定规则的原子事件，其中会加入噪声事件。
		logger.info("Adding Artificially rule guided Events");
		ph.setHistoryGeneratorType(HistoryGeneratorType.RULE_GUIDED);
		HistoryGenerator.generateHistory(history, rule, ph);

		// 产生满足 否定约束的事件
		if (rule.getNegationConstraints().size() != 0) {
			logger.info("Adding Artificially Generated Negated Events");
			ph.setHistoryGeneratorType(HistoryGeneratorType.RULE_GUIDED_NEGATION);
			HistoryGenerator.generateHistory(history, rule, ph);
		}

		return history;
	}

	private final void evaluateHistory(Rule rule, History trainingHistory) {
		logger.fine("Evaluating History");
		HistoryEvaluator.evaluateHistory(trainingHistory, rule);
	}

	private final long learnWindowSize(History trainingHistory,ConstraintsSet constraints) {
		logger.fine("Learning Window Size");
		// considerEventContentConstraints = true;
		// constraints.size() = 0
		return WindowLearner.learnWindow(ph, trainingHistory,considerEventContentConstraints, constraints);
	}

	/**
	 * @param win
	 *            => 学习到的窗口大小
	 * @param trainingHistory
	 *            => 历史记录
	 * @param constraintsSet
	 *            => 学习到的约束，包括事件约束与事件相对应的属性约束
	 * **/
	private ConstraintsSet examinePositiveExamples(long win,History trainingHistory, ConstraintsSet constraintsSet) {
		if (considerSequenceConstraints) {
			logger.fine("Considering Sequence Constraints");
			FeaturesObserver.extractSequenceConstraints(trainingHistory, win,constraintsSet);
		}
		return constraintsSet;
	}
	
	/**
	 * 未知? ? ?
	 * **/
	private Set<ConstraintsSet> examineNegativeExamples(long win,History trainingHistory, ConstraintsSet constraints) {
		logger.fine("Examining Negative Examples");
		NegativeInstancesDetector negDetector = new NegativeInstancesDetector(trainingHistory, constraints);
		negDetector.finalizeWithWin(win);
		
		Set<ConstraintsSet> resultsSet = negDetector.getConstraintsSets(constraints);
		return resultsSet;
	}
	
	private Rule generateLearnedRule(long win, ConstraintsSet constraints) {
		logger.fine("Generating Learned Rule");
		return LearnedRuleGenerator.generateRuleFromConstraints(constraints,win);
	}
	
	/**
	 * @param learnedRule
	 *            => 上面代码学习到的rule，包含事件约束，参数约束，顺序约束，集合约束
	 * @param trainingHistory
	 *            => 历史记录
	 * @param positiveConstraints
	 *            => 上面代码学习到的 “正” 约束
	 * */
	private void extractNegationConstraints(Rule learnedRule,
			History trainingHistory, ConstraintsSet positiveConstraints) {

		if (considerNegationConstraints) {
			logger.fine("Considering Negation Constraints");
			Set<NegationConstraint> negConstraints = 
					NegationLearner.extractNegationConstraints(learnedRule, trainingHistory, ph, positiveConstraints);

			for (NegationConstraint neg : negConstraints) {
				learnedRule.addNegation(neg);
			}
		}
	}
	
	private History generateEvaluationHistory(Rule rule) {
		logger.fine("Generating Evaluation History");
		if (ph.getHistoryGeneratorType() == HistoryGeneratorType.DUBLINKED) {
			return DublinkedWLGen.generateEvaluationHistory(ph);
		} else {
			return generateHistory(rule);
		}
	}
	
	private EvalStatistics evaluateLearnedRule(Rule rule,Rule learnedRule, History evaluationHistory) {
		logger.fine("Evaluating Derived Rule on Evaluation History");
		EvalStatistics stats = LearnedRuleEvaluator.evaluateLearnedRule(evaluationHistory, rule, learnedRule);
		logger.info(stats.toString());
		return stats;
	}
	
	private SyntacticDifferenceStatistics getSynthacticDifferencesBetweenRules(
							Rule rule, Rule learnedRule, ParamHandler ph) {
		logger.fine("Comparing the Rule and the Derived Rule Syntactically");
		SyntacticDifferenceStatistics stats = LearnedRuleEvaluator.computeSyntacticDifferences(rule, learnedRule, ph);
		logger.fine("Syntactical Comparison of Rules DONE!");
		logger.info(stats.toString());
		return stats;
	}
	
}
