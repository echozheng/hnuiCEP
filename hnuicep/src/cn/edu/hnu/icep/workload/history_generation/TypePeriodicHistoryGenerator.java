package cn.edu.hnu.icep.workload.history_generation;

import cn.edu.hnu.icep.event.model.Event;
import cn.edu.hnu.icep.history.History;
import cn.edu.hnu.icep.eval.ParamHandler;

/**
 * 周期性的产生事件，也就是前后事件的 时间差 是一定的。
 * **/
public class TypePeriodicHistoryGenerator extends HistoryGenerator {

	@Override
	public void decorateHistory(History history, ParamHandler ph) {
		
		//默认情况下，ph.getNumEventsInHistory() = 100000，ph.getNumEventTypes() = 25
		int numEventsPerType = Math.max(1,ph.getNumEventsInHistory() / ph.getNumEventTypes());
		
		for (int typeId = 0; typeId < ph.getNumEventTypes(); typeId++) {
			
			long period = getValue(ph.getMinDistanceBetweenEventsOfType(),
									ph.getMaxDistanceBetweenEventsOfType(),
									ph.getDistanceBetweenEventsOfTypeDistribution(),
									ph.getRandom());
			
			long currentTS = getValue(ph.getMinDistanceBetweenEventsOfType(),
									ph.getMaxDistanceBetweenEventsOfType(),
									ph.getDistanceBetweenEventsOfTypeDistribution(),
									ph.getRandom());
			
			for (int i = 0; i < numEventsPerType; i++) {
				Event event = generateEvent(currentTS, ph);
				history.addPrimitiveEvent(event);
				currentTS += period;
			}
			
		}
	}

}
