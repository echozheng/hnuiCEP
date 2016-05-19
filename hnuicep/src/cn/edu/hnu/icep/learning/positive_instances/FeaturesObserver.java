package cn.edu.hnu.icep.learning.positive_instances;

import cn.edu.hnu.icep.event.model.Event;
import cn.edu.hnu.icep.history.History;
import cn.edu.hnu.icep.rules.EventConstraint;
import cn.edu.hnu.icep.rules.filtering.Predicate;
import cn.edu.hnu.icep.learning.ConstraintsSet;
import cn.edu.hnu.icep.learning.LearnedEventContent;
import cn.edu.hnu.icep.learning.positive_instances.FeaturesExtractor;
import cn.edu.hnu.icep.learning.positive_instances.FeaturesMerger;
import cn.edu.hnu.icep.learning.sequence.SequenceExtractor;
import cn.edu.hnu.icep.common.Bound;
import cn.edu.hnu.icep.common.ConstraintOp;
import cn.edu.hnu.icep.event.model.Value;
import cn.edu.hnu.icep.rules.filtering.Constraint;
import cn.edu.hnu.icep.rules.filtering.Filter;
import cn.edu.hnu.icep.rules.SequenceConstraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FeaturesObserver {

	public static final void extractEventConstraints(History history, long win,
			boolean includeContentConstraints, ConstraintsSet results) {

		// 第一次传送过来的窗口大小，win = 1;
		// positiveExamples的值是： 所有复杂事件 在窗口大小为win之前的所有原子事件的集合。包括首尾的两个时间点。
		// 矩阵的每行都分别代表一个复杂事件对应的原子事件集合，也就是 一条 positive trace
		Collection<Collection<Event>> positiveExamples = getPositiveExamples(history, win);

		// eventContentCounts = return
		// FeaturesMerger.mergeEventContentCount(extractedEventContentConstraints);
		// eventContentCounts 是窗口大小为win的 所有positive trace中全部事件，
		// 进行取交集，也就是获得复杂事件发生 所需的事件类型，以及事件的属性。
		Map<String, Map<LearnedEventContent, Integer>> eventContentCounts = extractEventContentCount(positiveExamples);

		// ////////////////学习属性约束/////////////////////////////////////////////////
		// for循环中的type 为 event type，eg: Event12
		for (String eventType : eventContentCounts.keySet()) {
			Map<LearnedEventContent, Integer> contentCount = eventContentCounts.get(eventType);

			// [Type=Event12, attributes=[Attribute0]]=2
			for (LearnedEventContent evContent : contentCount.keySet()) {
				int count = getEventContentCount(evContent, contentCount);

				Predicate p = null;
				
				if (count <= 0) {
					continue;
					// 默认情况下，includeContentConstraints为true
				} else if (count == 1 && includeContentConstraints) {
					//count == 1 表示当前的evContent是 这个事件类型中 attributes最多的一个。
					// evContent例如:[Type=Event12, attributes=[Attribute2,Attribute1, Attribute0]]=1
					//System.out.println("evContent's content: " + evContent.toString());

					// 此处返回的predicate
					// 是事件类型type约束（属性值的约束，包括上下界与精确值，如果一个属性有精确值，那就没有了上下界）
					p = extractPredicateWithContentConstraints(eventType, evContent,positiveExamples);
				} else {
					p = extractPredicate(eventType, evContent);
				}

				// eventConstraint的内容是事件类型为 type(参数)的属性值约束集合。
				EventConstraint eventConstraint = new EventConstraint(p);
				// results的例子：
				// [[Event7[(Attribute2 <= 99 AND Attribute1 <= 99 AND Attribute0 <= 99 
				// 			 AND Attribute2 >= 0 AND Attribute1 >= 0 AND Attribute0 >= 0)],
				// Event12[(Attribute1 <= 99 AND Attribute0 <= 99 AND Attribute2 = 63 
				//			 AND Attribute1 >= 0 AND Attribute0 >= 0)],
				// Event6[(Attribute0 <= 99 AND Attribute1 = 54 AND Attribute2 = 34 
				//			AND Attribute0 >= 0)]]]
				results.add(eventConstraint);
			}
		}
		
		removeRedundantEventConstraints(results);
	}

	/**
	 * return all the positive traces in the history at the window size of win,
	 * all the traces are load into a matrix,positiveExamples
	 */
	private static final Collection<Collection<Event>> getPositiveExamples(
			History history, long win) {
		// 矩阵
		Collection<Collection<Event>> positiveExamples = new ArrayList<Collection<Event>>();
		// history.getTimestampsWithCompositeEvents()，得到复杂事件的时间戳
		for (Long ts : history.getTimestampsWithCompositeEvents()) {
			long minTs = (ts - win > 0) ? ts - win : 0;
			// 此处的events，是从一个复杂事件发送的事件[ts-1,ts]的全部事件。 !!!注意包括两个边界点的事件
			Collection<Event> events = history.getAllEventsInWindow(minTs, ts);
			positiveExamples.add(events);
		}
		return positiveExamples;
	}

	private static final Map<String, Map<LearnedEventContent, Integer>> extractEventContentCount(
			Collection<Collection<Event>> positiveExamples) {

		//extractedEventContentConstraints => 存储所有positive trace中的每一个原子事件的属性子集集合。
		Collection<Map<String, Map<LearnedEventContent, Integer>>> extractedEventContentConstraints = 
												new ArrayList<Map<String, Map<LearnedEventContent, Integer>>>();

		for (Collection<Event> events : positiveExamples) {
			Map<String, Map<LearnedEventContent, Integer>> evContentConstraints = 
												FeaturesExtractor.extractEventContentCount(events);
			
			extractedEventContentConstraints.add(evContentConstraints);
		}

		// 把全部positive trace中的events解析而成的 属性集合进行 取交集。以获得最佳最少属性约束集合。
		// 论文的创新所在！！！
		return FeaturesMerger.mergeEventContentCount(extractedEventContentConstraints);
	}

	/**
	 * @return 返回一个常数，内容是【contentCounts.get(content) - count】 这个表达式的第一个值，显而易见。
	 *         表达式的第二个值，是contentCounts中“直接”包含(不包括等于)content的全部key的value 的集合之和。
	 **/
	private static final int getEventContentCount(LearnedEventContent content,
			Map<LearnedEventContent, Integer> contentCounts) {
		/*
		 * 返回contentCounts集合中 “直接” 包含 content这个key的其它key值。注意，是直接包含，例子如下：
		 * [E6（空集=6,S0=2,S1=2,S2=2,S10=1,S20=1;S21=1;S210=1）]
		 * 1. 第一次传入是空集=6，那么“直接”包含空集的，得到返回的是 (S0,S1,S2)
		 * 2. 第二次传入是S0=2，那么“直接”包含S0=2的，得到返回的是(S10,S20)
		 * 注意，虽然S210同样包含了S0，可惜地是，它并没有“直接”包含S0，所以会被删除。
		 */
		Set<LearnedEventContent> directlyImplyingEventContents = 
									getDirectlyImplyingEventContents(content, contentCounts);

		int count = 0;
		for (LearnedEventContent directlyImplyingContent : directlyImplyingEventContents) {
			assert (contentCounts.containsKey(directlyImplyingContent));
			int contentCount = contentCounts.get(directlyImplyingContent);
			count += contentCount;
		}
		assert (contentCounts.containsKey(content));
		return contentCounts.get(content) - count;
	}

	/**
	 * @return 返回contentCounts集合中 “直接” 包含 content这个key的其它key值。注意，是直接包含，例子如下：
	 *         [E6（空集=6,S0=2,S1=2,S2=2,S10=1,S20=1;S21=1;S210=1）] 1. 第一次传入是
	 *         空集=6，那么“直接”包含空集的，得到返回的是 (S0,S1,S2) 2. 第二次传入是
	 *         S0=2，那么“直接”包含S0=2的，得到返回的是(S10,S20)
	 *         注意，虽然S210同样包含了S0，可惜地是，它并没有“直接”包含S0，所以会被删除
	 * */
	private static final Set<LearnedEventContent> getDirectlyImplyingEventContents(
			LearnedEventContent content,
			Map<LearnedEventContent, Integer> evContentsCount) {

		Set<LearnedEventContent> implyingEventContents = getImplyingEventContents(
				content, evContentsCount);

		// Remove all the contents that imply other contents (they are not
		// *directly* implying)
		Set<LearnedEventContent> contentsToRemove = new HashSet<LearnedEventContent>();

		for (LearnedEventContent implyingEventContent : implyingEventContents) {
			getImplyingEventContents(implyingEventContent, evContentsCount,
					contentsToRemove);
			// getImplyingEventContents(implyingEventContent,implyingEventContents,
			// contentsToRemove);
		}

		implyingEventContents.removeAll(contentsToRemove);
		/*
		 * System.out.println(
		 * "in the function of getDirectlyImplyingEventContents,the value of implyingEventContents: "
		 * ); System.out.println(implyingEventContents.toString());
		 * AssistDebug.takeABreak(); System.out.println();
		 */

		// 返回的implyingEventContents，例子如：（返回值带有一个属性的learnEventContent）
		// [[Type=Event6, attributes=[Attribute0]], [Type=Event6,
		// attributes=[Attribute1]], [Type=Event6, attributes=[Attribute2]]]
		return implyingEventContents;
	}

	private static final Set<LearnedEventContent> getImplyingEventContents(
			LearnedEventContent content,
			Map<LearnedEventContent, Integer> evContentsCount) {

		Set<LearnedEventContent> results = new HashSet<LearnedEventContent>();
		getImplyingEventContents(content, evContentsCount, results);
		return results;
	}

	/**
	 * @retrun results最终结果是返回这个 evContentsCount集合中 包含(不包括等于)content的集合。
	 *         通常是属性最多的一个或者 几个。
	 * **/
	private static final void getImplyingEventContents(
			LearnedEventContent content,
			Map<LearnedEventContent, Integer> evContentsCount,
			Set<LearnedEventContent> results) {
		// results最终结果是返回这个 evContentsCount集合中 包含(不包括等于)content的集合。通常是属性最多的一个 或者
		// 几个。
		for (LearnedEventContent evContent : evContentsCount.keySet()) {
			// 当evContent的 事件类型 == content，并且content的属性集合
			// 是evContent属性集合的子集(不包含真子集)的时候，
			// 就会把 evContent加入results中。
			if (evContent.implies(content)) {
				if (!evContent.equals(content)) {
					results.add(evContent);
				}
			}
		}
	}

	/**
	 * @param eventType
	 *            => 事件类型，例如<Event6>
	 * @param eventContent
	 *            => 此事件类型下的 一个原子事件中的一个最大属性集合， 
	 *            例如[Type=Event6,attributes=[Attribute2, Attribute1, Attribute0]]
	 * @param positiveExamples
	 *            =>在所有复杂事件发生之前，窗口太小为win的所有原子事件的集合(包括左右两个事件段)。
	 *            矩阵的每一行都代表一个复杂事件的原子事件集合。
	 * @return a predicate
	 * **/
	private static final Predicate extractPredicateWithContentConstraints(
			String eventType, LearnedEventContent eventContent,
			Collection<Collection<Event>> positiveExamples) {

		// existingValues 例子:{Attribute2=[34], Attribute1=[54]}
		Map<String, Set<Value>> existingValues = extractEqualityEventContent(eventContent, positiveExamples);

		// bounds的例子：{Attribute2={UPPER=98, LOWER=0}, Attribute1={UPPER=99,
		// LOWER=0}, Attribute0={UPPER=99, LOWER=0}}
		Map<String, Map<Bound, Value>> bounds = extractEventContentBound(eventContent, positiveExamples);

		Set<Constraint> constraints = new HashSet<Constraint>();

		outerloop: for (String attrName : bounds.keySet()) {

			// If all instances include a single shared value, we use it
			// 如果有一个属性，既在existingValues 又在 bounds，那么我们就取existingValues中的值
			// 为这个属性的值。
			if (existingValues.containsKey(attrName)) {
				Set<Value> valuesSet = existingValues.get(attrName);

				if (valuesSet.size() == 1) {
					for (Value val : valuesSet) {
						constraints.add(new Constraint(attrName,ConstraintOp.EQ, val));
						// 此处 continue outerloop的作用 与 continue
						// 的作用一致，就是不执行下面代码，跳出此次循环。
						continue outerloop;
					}
				}
			}

			//bounds的例子：{Attribute2={UPPER=98, LOWER=0}, Attribute1={UPPER=99,LOWER=0},
			//Attribute0={UPPER=99, LOWER=0}}
			Map<Bound, Value> valuesMap = bounds.get(attrName);
			Value minValue = valuesMap.get(Bound.LOWER);
			Value maxValue = valuesMap.get(Bound.UPPER);

			if (minValue.equals(maxValue)) {
				constraints.add(new Constraint(attrName, ConstraintOp.EQ, minValue));
			} else {
				constraints.add(new Constraint(attrName, ConstraintOp.GT_EQ, minValue));
				constraints.add(new Constraint(attrName, ConstraintOp.LT_EQ, maxValue));
			}
		}
		// contraints的内容：
		// [Attribute0 <= 99, Attribute1 = 54, Attribute2 = 34, Attribute0 >= 0]

		Filter f = new Filter(constraints);
		
		// 首次eventType=Event12
		return new Predicate(eventType, f);
	}

	/**
	 * @param eventContent
	 *            => 此事件类型下的 一个原子事件中的一个最大属性集合， 例如
	 *            [Type=Event6,attributes=[Attribute2, Attribute1, Attribute0]]
	 * @param positiveExamples
	 *            =>在所有复杂事件发生之前，窗口太小为win的所有原子事件的集合。矩阵的每一行都代表一个复杂事件的原子事件集合。
	 * @return 返回(Attribute 等于 某个值) 的集合. 例如{Attribute2=[34], Attribute1=[54]}
	 */
	private static final Map<String, Set<Value>> extractEqualityEventContent(
			LearnedEventContent eventContent,
			Collection<Collection<Event>> positiveExamples) {

		Collection<Map<String, Set<Value>>> values = new ArrayList<Map<String, Set<Value>>>();
		for (Collection<Event> events : positiveExamples) {
			Map<String, Set<Value>> valMap = new HashMap<String, Set<Value>>();

			FeaturesExtractor.extractContentConstraintValues(events,eventContent, valMap);

			// values中的每一行数据是一条positive trace里面 包含参数eventContent的全部事件中，与
			// eventContent相同属性 的值的集合。
			values.add(valMap);
		}

		return FeaturesMerger.mergeContentConstraintValues(values);
	}

	/**
	 * @return 返回属性值的上下界，例如：{Attribute2={UPPER=98, LOWER=0},
	 *         Attribute1={UPPER=99, LOWER=0}, Attribute0={UPPER=99, LOWER=0}}
	 *         比较的对象 是 全部的positive trace中包含的全部对象。
	 * */
	private static final Map<String, Map<Bound, Value>> extractEventContentBound(
			LearnedEventContent eventContent,
			Collection<Collection<Event>> positiveExamples) {

		Map<String, Map<Bound, Value>> bounds = new HashMap<String, Map<Bound, Value>>();

		for (Collection<Event> events : positiveExamples) {
			FeaturesExtractor.updateContentConstraintBounds(events,
					eventContent, bounds);
		}
		return bounds;
	}

	private static final Predicate extractPredicate(String eventType,LearnedEventContent eventContent) {
		Set<Constraint> constraints = new HashSet<Constraint>();
		for (String attrName : eventContent.getAttributes()) {
			Constraint c = new Constraint(attrName);
			constraints.add(c);
		}
		Filter f = new Filter(constraints);
		return new Predicate(eventType, f);
	}

	/**
	 * 删除重复的 EventConstraint
	 * **/
	private static final void removeRedundantEventConstraints(ConstraintsSet constraints) {

		List<EventConstraint> constraintsList = new ArrayList<EventConstraint>(constraints.getEventConstraints());

		outerloop: for (int i = 0; i < constraintsList.size() - 1; i++) {
			EventConstraint evConstr1 = constraintsList.get(i);
			// 内循环
			for (int j = i + 1; j < constraintsList.size(); j++) {
				EventConstraint evConstr2 = constraintsList.get(j);
				if (evConstr2.getPredicate().covers(evConstr1.getPredicate())) {
					constraints.remove(evConstr1);
					continue outerloop;
				}
			}
		}
		
	}
	
	/**
	 * 解析顺序约束,并把习得的约束集合 添加进 @param constraints中
	 * @param history => 系统生成的历史记录
	 * @param win => 先前代码学习到的 窗口大小
	 * @param constraints => 学习得到的约束集合
	 * */
	public static final void extractSequenceConstraints(History history,
			long win, ConstraintsSet constraints) {

		Map<EventConstraint, Set<EventConstraint>> seqConstraints = 
						extractSequenceConstraintsMap(history, win, constraints);
		
		/*for(EventConstraint referenceConstraint : seqConstraints.keySet()) {
			for(EventConstraint followingConstraint : seqConstraints.get(referenceConstraint)) {
				System.out.println("先后顺序: " + referenceConstraint.toString2() + "-->" +followingConstraint.toString2());
			}
		}*/
		
		// 将学习到的 sequence约束，从map中解析出，封装成一个个的SequenceConstraint
		for (EventConstraint constr1 : seqConstraints.keySet()) {
			Set<EventConstraint> set = seqConstraints.get(constr1);
			for (EventConstraint constr2 : set) {
				SequenceConstraint seqConstr = new SequenceConstraint(constr1,constr2);
				constraints.add(seqConstr);
			}
		}
	}
	
	/**
	 * @param history
	 *            => 系统生成的历史记录
	 * @param win
	 *            => 先前代码学习到的 窗口大小
	 * @param constraints
	 *            => 约束集合，目前包括事件类型约束，参数约束（目前只包含EqualityParameterConstraint）
	 * 
     *返回学习到的 sequenceConstraint，保存到map中返回。 Map<EventConstraint,Set<EventConstraint>>，
     *表示前一个EventConstraint在后面一堆的Set<EventConstraint> 之前发生。
	 * */
	private static final Map<EventConstraint, Set<EventConstraint>> extractSequenceConstraintsMap (
			History history, long win, ConstraintsSet constraints) {

		Collection<Map<EventConstraint, Set<EventConstraint>>> extractedSequences 
							= new ArrayList<Map<EventConstraint, Set<EventConstraint>>>();

		// extractedSequences 集合中 每一个map中的内容，都是从一条positive trace中学习到的 顺序约束
		//（自己写的类）
		SequenceExtractor.learnSequceConstraint(getPositiveExamples(history, win),
								extractedSequences,constraints.getEventConstraints());
			
		return FeaturesMerger.mergeSequenceConstraints(extractedSequences);
	}

}
