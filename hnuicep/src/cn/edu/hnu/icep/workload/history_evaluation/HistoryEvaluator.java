package cn.edu.hnu.icep.workload.history_evaluation;

import java.util.Collection;

import cn.edu.hnu.icep.common.Consts;
import cn.edu.hnu.icep.event.model.Event;
import cn.edu.hnu.icep.history.History;
import cn.edu.hnu.icep.rules.Rule;

public class HistoryEvaluator {

	public static void evaluateHistory(History history, Rule rule) {

		// 共12700个primitive event，包含timestamp原子事件为125917个。
		System.out.println("in the function of evaluateHistory,HistoryEvaluator. the size of "
							+ " history.getTimestampsWithPrimitiveEvents :"
							+ history.getTimestampsWithPrimitiveEvents().size());

		for (Long ts : history.getTimestampsWithPrimitiveEvents()) {
			long win = rule.getWin();
			long minTS = (ts - win > 0) ? ts - win : 0;
			Collection<Event> events = history.getAllEventsInWindow(minTS, ts);

			// isSatisfiedBy()的目的 是判断在此events事件集合，是否满足rule。如果不满足，就不生成复杂事件
			// 这是符合客观规律的，如果不判断就生成了复杂事件，明显是矛盾的。
			// 所以，复杂事件生成的个数，也是不能确定的。带有一定的随机性。
			if (rule.isSatisfiedBy(events, ts)) {
				Event compositeEvent = new Event(Consts.COMPOSITE_EVENT_TYPE,ts);
				history.addCompositeEvent(compositeEvent);
			}
		}
		
		System.out.println("共产生了 " + history.getNumCompositeEvents() + " 个复杂事件");
	}
}
