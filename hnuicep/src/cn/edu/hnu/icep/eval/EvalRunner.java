package cn.edu.hnu.icep.eval;

import cn.edu.hnu.icep.common.Consts;

public class EvalRunner {

	// 修改，去掉原来实验中的 EvalRunner,所以，也不需要原来的构造函数
	public final void runAll() {
		int firstSeed = 1;
		int lastSeed = firstSeed + Consts.NUM_REPETITIONS - 1;

		System.out.println("lastSeed = " + lastSeed);
		//runDefault(firstSeed, lastSeed);

		// 对 事件类型约束 个数的训练
		//runSequenceWidth(firstSeed, lastSeed);

		// 对 顺序约束 个数的训练
		//runSequenceLength(firstSeed, lastSeed);

		// 对 事件的属性约束个数 的训练
		//runNumConstraints(0, firstSeed, lastSeed);
		//runNumConstraints(1, firstSeed, lastSeed);
		//runNumConstraints(2, firstSeed, lastSeed);

		// 窗口大小
		runWinSize(firstSeed, lastSeed);

		// ？？？？
		//runNumSamples(2, firstSeed, lastSeed);
		//runNumSamples(3, firstSeed, lastSeed);
		//runNumSamples(4, firstSeed, lastSeed);
		
		//对复杂事件个数的训练
		//runCompositeEvents(4, firstSeed, lastSeed);
		
		// 对 事件类型 的训练
		//runNumEventTypes(firstSeed, lastSeed);

		// 对 噪声数据 的训练
		//runNoise(firstSeed, lastSeed);

		// 对 否定约束 的训练
		//runNegation(firstSeed, lastSeed);

		//runNumNegationSamples(firstSeed, lastSeed);
		//runNegationThreshold(firstSeed, lastSeed);
	}

	private void runDefault(int minSeed, int maxSeed) {
		
		// 每一个线程执行10次ExperimentJob,也就是说，每一个实验跑10遍
		for (int seed = minSeed; seed <= maxSeed; seed++) {
			int threadId = seed % Consts.NUM_THREADS;
			
			// 初始化ParamHandler
			ParamHandler ph = new ParamHandler(threadId);
			
			ph.setSeed(seed);
			ph.setNumEventConstraints(4);
			ExperimentJob job = new ExperimentJob(ph, "1", "Default");
			job.execute();
		}
	}

	private void runSequenceWidth(int minSeed, int maxSeed) {
		//每轮都跑10遍
		for (int seqLen = 2; seqLen <= 7; seqLen++) {
			System.out.println();
			System.out.println("-----------------------------------------------------");
			// maxSeed = 10
			for (int seed = minSeed; seed <= maxSeed; seed++) {

				int threadId = seed % Consts.NUM_THREADS;

				ParamHandler ph = new ParamHandler(threadId);

				ph.setSeed(seed);

				// eventConstraints就是事件类型约束。（也就是说，一个positive trace中包含多少种事件类型）
				ph.setNumEventConstraints(seqLen);

				ph.setSeqProbability(0);

				ExperimentJob job = new ExperimentJob(ph,  String.valueOf(seqLen), "SeqWidth");
				job.execute();
			}
			System.out.println("-----------------------------------------------------");
			System.out.println();
		}
	}

	private void runSequenceLength(int minSeed, int maxSeed) {
		
		for (int seqLen = 1; seqLen <= 6; seqLen++) {
			//maxSeed=10，每轮跑10遍
			for (int seed = minSeed; seed <= maxSeed; seed++) {
				int threadId = seed % Consts.NUM_THREADS;
				ParamHandler ph = new ParamHandler(threadId);
				ph.setSeed(seed);
				ph.setNumEventConstraints(seqLen);
				ph.setSeqProbability(1);
				ExperimentJob job = new ExperimentJob(ph,  String.valueOf(seqLen), "SeqLen");
				job.execute();
			}
		}
	}

	private void runNumConstraints(int additionalAttributes, int minSeed, int maxSeed) {
		for (int numConstr = 1; numConstr <= 5; numConstr++) {
			for (int seed = minSeed; seed <= maxSeed; seed++) {
				int threadId = seed % Consts.NUM_THREADS;
				ParamHandler ph = new ParamHandler(threadId);
				ph.setSeed(seed);
				ph.setNumConstraints(numConstr);
				ph.setMinNumAttributes(numConstr + additionalAttributes);
				ph.setMaxNumAttributes(numConstr + additionalAttributes);
				ExperimentJob job = new ExperimentJob(ph,String.valueOf(numConstr),"Costr_" + additionalAttributes);
				job.execute();
			}
		}
	}

	private void runWinSize(int minSeed, int maxSeed) {

		for (int win = 8; win <= 16; win++) {
			//每轮跑10遍
			for (int seed = minSeed; seed <= maxSeed; seed++) {
			
				int threadId = seed % Consts.NUM_THREADS;
				ParamHandler ph = new ParamHandler(threadId);
				ph.setSeed(seed);
				
				/*修改
				int minWin = win - 1;
				int maxWin = win + 1;
				ph.setMinWinSize(minWin);
				ph.setMaxWinSize(maxWin);*/

				ph.setMinWinSize(win);
				ph.setMaxWinSize(win);
				ph.setNumEventConstraints(4);
				ExperimentJob job = new ExperimentJob(ph,String.valueOf(win),"Win");
				job.execute();
			}
		}
	}

	private final void runNumEventTypes(int minSeed, int maxSeed) {
		for (int numTypes = 4; numTypes <= 100;) {
			for (int seed = minSeed; seed <= maxSeed; seed++) {
				int threadId = seed % Consts.NUM_THREADS;
				ParamHandler ph = new ParamHandler(threadId);
				ph.setSeed(seed);
				ph.setNumEventTypes(numTypes);
				ExperimentJob job = new ExperimentJob(ph,String.valueOf(numTypes),"NumTypes");
				job.execute();
			}
			if (numTypes < 10)
				numTypes += 3;
			else
				numTypes += 30;
		}
	}

	private void runNumSamples(int numEvents, int minSeed, int maxSeed) {
		
		for (int numSamples = 1000; numSamples <= 3000;) {
			//每轮跑10遍
			for (int seed = minSeed; seed <= maxSeed; seed++) {
				int threadId = seed % Consts.NUM_THREADS;
				ParamHandler ph = new ParamHandler(threadId);
				
				ph.setSeed(seed);
				ph.setNumEventConstraints(numEvents);
				ph.setNumEventsInHistory(1000);
				
				ph.setNumArtificialPrimitiveEventsInHistory(numSamples);
				ExperimentJob job = new ExperimentJob(ph, String.valueOf(numSamples),"NumSamples_" + numEvents);
				job.execute();
			}

			if (numSamples < 100) {
				numSamples += 30;
			} else if (numSamples < 500) {
				numSamples += 100;
			} else if (numSamples < 1000) {
				numSamples += 250;
			} else {
				numSamples += 500;
			}
		}
	}

	//测试对历史记录中复杂事件的变化
	private void runCompositeEvents(int numEvents, int minSeed, int maxSeed) {
		
		/*for (int numSamples = 900; numSamples <= 3000;) {
			//每轮跑10遍
			for (int seed = minSeed; seed <= maxSeed; seed++) {
				int threadId = seed % Consts.NUM_THREADS;
				ParamHandler ph = new ParamHandler(threadId);
				
				ph.setSeed(seed);
				ph.setNumEventConstraints(numEvents);
				ph.setNumEventsInHistory(200000);
				
				ph.setNumArtificialPrimitiveEventsInHistory(numSamples);
				ExperimentJob job = new ExperimentJob(ph, String.valueOf(numSamples),"ArtiEvents_" + numSamples);
				job.execute();
			}
			
			numSamples += 300;
		}*/
		for (int seed = minSeed; seed <= maxSeed; seed++) {
			int threadId = seed % Consts.NUM_THREADS;
			ParamHandler ph = new ParamHandler(threadId);
			
			ph.setSeed(seed);
			ph.setNumEventConstraints(numEvents);
			ph.setNumEventsInHistory(200000);
			
			ph.setNumArtificialPrimitiveEventsInHistory(3300);
			ExperimentJob job = new ExperimentJob(ph, String.valueOf(3300),"ArtiEvents_" + 3300);
			job.execute();
		}
	}
	
	private final void runNoise(int minSeed, int maxSeed) {
		for (int noise = 1; noise <= 100;) {
			for (int seed = minSeed; seed <= maxSeed; seed++) {
				int threadId = seed % Consts.NUM_THREADS;
				ParamHandler ph = new ParamHandler(threadId);
				ph.setSeed(seed);
				ph.setDistanceBetweenNoiseEvents(noise);
				ExperimentJob job = new ExperimentJob(ph, String.valueOf(noise),"Noise");
				job.execute();
			}
			if (noise < 10)
				noise += 3;
			else
				noise += 30;
		}
	}

	private final void runNegation(int minSeed, int maxSeed) {
		for (int numNegations = 0; numNegations <= 2; numNegations++) {
			for (int seed = minSeed; seed <= maxSeed; seed++) {
				int threadId = seed % Consts.NUM_THREADS;
				ParamHandler ph = new ParamHandler(threadId);
				ph.setSeed(seed);
				// ph.setNumEventConstraints(2);
				ph.setNumNegationConstraints(numNegations);
				ExperimentJob job = new ExperimentJob(ph,String.valueOf(numNegations), "Neg");
				job.execute();
			}
		}
	}

	private final void runNegationThreshold(int minSeed, int maxSeed) {
		for (float negThreshold = 0.1f; negThreshold <= 1.1f; negThreshold += 0.1) {
			for (int seed = minSeed; seed <= maxSeed; seed++) {
				int threadId = seed % Consts.NUM_THREADS;
				ParamHandler ph = new ParamHandler(threadId);
				ph.setSeed(seed);
				// ph.setNumEventConstraints(2);
				ph.setNumNegationConstraints(1);
				ph.setNegationFrequencyThreshold(negThreshold);
				ExperimentJob job = new ExperimentJob(ph,String.format("%.1f", negThreshold), "NegThreshold");
				job.execute();
			}
		}
	}

	private final void runNumNegationSamples(int minSeed, int maxSeed) {
		for (int numSamples = 10; numSamples <= 3000;) {
			for (int seed = minSeed; seed <= maxSeed; seed++) {
				int threadId = seed % Consts.NUM_THREADS;
				ParamHandler ph = new ParamHandler(threadId);
				ph.setSeed(seed);
				// ph.setNumEventConstraints(2);
				ph.setNumNegationConstraints(1);
				ph.setNumArtificialPrimitiveEventsInHistory(numSamples);
				ph.setNumArtificialNegationsInHistory(numSamples);
				ExperimentJob job = new ExperimentJob(ph, String.valueOf(numSamples),"NumNegationSamples");
				job.execute();
			}
			if (numSamples < 100)
				numSamples += 30;
			else if (numSamples < 500)
				numSamples += 100;
			else if (numSamples < 1000)
				numSamples += 250;
			else
				numSamples += 2000;
		}
	}
}
