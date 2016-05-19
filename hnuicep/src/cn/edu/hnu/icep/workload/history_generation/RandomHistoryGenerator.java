package cn.edu.hnu.icep.workload.history_generation;

import cn.edu.hnu.icep.event.model.Event;
import cn.edu.hnu.icep.history.History;
import cn.edu.hnu.icep.eval.ParamHandler;
import cn.edu.hnu.icep.workload.history_generation.HistoryGenerator;

/**
 * 用于随机产生历史记录
 * **/
public class RandomHistoryGenerator extends HistoryGenerator {
	/**
	 * 产生原子事件，事件的基本模型为： Event2@1[Attribute2 = 0, Attribute1 = 54, Attribute0 = 5]
	 **/
	@Override
	public void decorateHistory(History history, ParamHandler ph) {
		long currentTS = history.getMaximumTimestamp();
		
		//System.out.println("in the function of decorateHistory(),RandomHistoryGenerator.");

		for (int i = 0; i < ph.getNumEventsInHistory(); i++) {
			Event event = generateEvent(currentTS, ph);

			history.addPrimitiveEvent(event);
			currentTS += getValue(ph.getMinDistanceBetweenEvents(),ph.getMaxDistanceBetweenEvents(),
									ph.getDistanceBetweenEventsDistribution(), ph.getRandom());
		}
	}

}
