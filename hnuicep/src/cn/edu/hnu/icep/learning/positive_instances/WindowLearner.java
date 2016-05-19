package cn.edu.hnu.icep.learning.positive_instances;

import cn.edu.hnu.icep.learning.positive_instances.FeaturesObserver;
import cn.edu.hnu.icep.history.History;
import cn.edu.hnu.icep.eval.ParamHandler;
import cn.edu.hnu.icep.learning.ConstraintsSet;

public class WindowLearner {

	
	public static final long learnWindow(ParamHandler ph, History history,
			boolean includeContentConstraints, ConstraintsSet result) {
		switch (ph.getWinLearnerType()) {
		case TAKE_STABLE:
			return learnWindowStable(ph, history, includeContentConstraints,result);
		case TAKE_MAXIMUM:
			return learnWindowMaximum(ph, history, includeContentConstraints,result);
		default:
			assert false : ph.getWinLearnerType();
			return learnWindowStable(ph, history, includeContentConstraints,result);
		}
	}

	private static final long learnWindowStable(ParamHandler ph,History history,
				boolean includeContentConstraints,ConstraintsSet result) {
		long minWin = ph.getMinWindowForEvaluation();
		long maxWin = ph.getMaxWindowForEvaluation();
		long winStep = ph.getWinStepForEvaluation();
		int winStableAfter = ph.getWinStableAfter();

		long selectedWin = maxWin;
		int stableFor = 0;
		int lastCount = 0;
		for (long win = minWin; win <= maxWin; win += winStep) {
			ConstraintsSet constraints = new ConstraintsSet();
			FeaturesObserver.extractEventConstraints(history, win,includeContentConstraints, constraints);
			int currentCount = constraints.getAllConstraints().size();
			if (currentCount > 0 && currentCount == lastCount) {
				stableFor++;
			} else {
				stableFor = 0;
			}
			if (stableFor == winStableAfter) {
				result.addAll(constraints.getAllConstraints());
				selectedWin = win;
				break;
			}
			lastCount = currentCount;
		}
		return selectedWin;
	}

	/** 
	 * 学习窗口大小
	 * 方法中 includeContentConstraints 的值为true
	 * **/
	private static final long learnWindowMaximum(ParamHandler ph,History history,
			boolean includeContentConstraints,ConstraintsSet result) {

		// minWindowForEvaluation = 1;
		long minWin = ph.getMinWindowForEvaluation();

		// maxWindowForEvaluation = 50;
		long maxWin = ph.getMaxWindowForEvaluation();

		// winStepForEvaluation = 1;
		long winStep = ph.getWinStepForEvaluation();

		long selectedWin = maxWin;
		int maximum = 0;
		ConstraintsSet selectedConstraints = new ConstraintsSet();

		// 这个循环的作用是什么？ 答：线性地增大窗口，学习此复杂事件包含最多约束。取约束最多的窗口大小为 生成rule的窗口大小。
		// 修改成从10开始循环，只循环两次，2015/09/09
		 for (long win = 8; win <= 16; win += winStep) {
		//for (long win = minWin; win <= maxWin; win += winStep) {
			System.out.println("窗口值大小为 ： " + win);
			ConstraintsSet constraints = new ConstraintsSet();

			// 调用下面方法之后的constraints的实例:（只含有 EventConstraint,解决了 event/attribute learner,constraints learner）
			// [[Event7[(Attr2 >= 0 AND Attr2 <= 99 AND Attr1 >= 0 AND Attr1 <= 99 AND Atte0 >= 0 AND Attr0 <=99)],
			//   Event12[(Attr2 = 63 AND Attr1 >= 0 AND Attr1 <= 99 AND Attr0 >= 0 AND Attr0 <= 99 )],
			//   Event6[(Attr2 = 34 AND Attr1 = 54 AND Attr0 >= 0 AND Attr0 <= 99)]]
			FeaturesObserver.extractEventConstraints(history, win,includeContentConstraints, constraints);
			
			int currentCount = constraints.getAllConstraints().size();
			System.out.println("constraints's size =: " + currentCount);

			if (currentCount > maximum) {
				maximum = currentCount;
				selectedWin = win;
				selectedConstraints = constraints;
			}

			System.out.println("in the function of learnWindowMaximum,WindowLearner. 一次循环结束，查看输出的值。");
			System.out.println("in the function of learnWindowMaximum,WindowLearner. "
										+ "the value of the selectedWin = : " + selectedWin);
			System.out.println();
		}

		result.addAll(selectedConstraints.getAllConstraints());
		return selectedWin;
	}

}
