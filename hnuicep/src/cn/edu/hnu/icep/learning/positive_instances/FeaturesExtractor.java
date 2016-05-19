package cn.edu.hnu.icep.learning.positive_instances;

import cn.edu.hnu.icep.event.model.Event;
import cn.edu.hnu.icep.learning.LearnedEventContent;
import cn.edu.hnu.icep.event.model.Value;
import cn.edu.hnu.icep.common.Bound;
import cn.edu.hnu.icep.event.model.ValueType;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FeaturesExtractor {

	/**
	 * @param events 是产生了一个复杂事件的positive trace的全部事件
	 * @return 返回这条positive trace中全部事件的 属性子集集合
	 **/
	public static final Map<String, Map<LearnedEventContent, Integer>> extractEventContentCount(
			Collection<Event> events) {

		Map<String, Map<LearnedEventContent, Integer>> results = 
						new HashMap<String, Map<LearnedEventContent, Integer>>();
		for (Event event : events) {
			addLearnedEventContentCount(event, results);
		}
		return results;
	}

	/**
	 * 将event的分解成 其子集的集合，并添加到 @param learnedEventContentMap中
	 * **/
	private static final void addLearnedEventContentCount(Event event,
			Map<String, Map<LearnedEventContent, Integer>> learnedEventContentMap) {

		String type = event.getEventType();
		Set<String> attrNames = event.getAttrNamesSet();
		getSubsetsCount(type, attrNames, learnedEventContentMap);
	}

	/**
	 * 原子事件： Event12@100070[Attribute2 = 63, Attribute1 = 40, Attribute0 = 27]
	 * 子集如下： {Event12={[Type=Event12, attributes=[]]=6, 
	 * 			          [Type=Event12,attributes=[Attribute0]]=2,
	 * 					  [Type=Event12,attributes=[Attribute1]]=2,
	 * 					  [Type=Event12,attributes=[Attribute2]]=2, 
	 * 					  [Type=Event12,attributes=[Attribute1, Attribute0]]=1,
	 * 					  [Type=Event12,attributes=[Attribute2, Attribute0]]=1,
	 * 					  [Type=Event12,attributes=[Attribute2, Attribute1]]=1,
	 * 					  [Type=Event12,attributes=[Attribute2, Attribute1, Attribute0]]=1 }}
	 * map中最后存储的 内容是 每个事件的子集集合，如果positive trace中包含了 重复的事件，那么就把count相加
	 * 可以看出，FeaturesExtractor.extractEventContentCount(events)这个方法 它的返回结果是
	 * 这条positive trace路径上 所有事件的 子集的集合内容（包括重复的）。
	 * **/
	private static final void getSubsetsCount(String type, Set<String> set,
			Map<String, Map<LearnedEventContent, Integer>> map) {
		LearnedEventContent evContent = new LearnedEventContent(type, set);
		Map<LearnedEventContent, Integer> innerMap = map.get(type);
		if (innerMap == null) {
			innerMap = new HashMap<LearnedEventContent, Integer>();
			map.put(type, innerMap);
		}

		Integer count = innerMap.get(evContent);
		if (count == null) {
			innerMap.put(evContent, 1);
		} else {
			innerMap.remove(evContent);
			innerMap.put(evContent, count + 1);
		}
		for (String name : set) {
			Set<String> names = new HashSet<String>(set);
			names.remove(name);
			// 递归调用
			getSubsetsCount(type, names, map);
		}
	}

	/**
	 * @param events => 一个复杂事件的一条positive trace的事件集合 
	 * @param eventContent =>一个原子事件中的属性集合，
	 * 			例如[Type=Event6, attributes=[Attribute2, Attribute1,Attribute0]]
	 * @param valuesMap => 空集合
	 * @return valuesMap => positive trace里面
	 * 包含参数eventContent的全部事件中，与eventContent相同属性 的值的集合。
	 */
	public static final void extractContentConstraintValues(
			Collection<Event> events, LearnedEventContent eventContent,
			Map<String, Set<Value>> valuesMap) {

		// getEventsMatchingContentConstraint(events, eventContent),返回的原子事件
		// 是(包含eventContent事件的)。
		for (Event e : getEventsMatchingContentConstraint(events, eventContent)) {
			for (String name : eventContent.getAttributes()) {

				// 返回属性name的值。
				Value val = e.getValueFor(name);
				Set<Value> values = valuesMap.get(name);

				if (values == null) {
					values = new HashSet<Value>();

					// valuesMap是positive trace里面
					// 包含参数eventContent的全部事件中，与eventContent相同属性 的值的集合。
					valuesMap.put(name, values);
				}
				values.add(val);
			}
		}
	}

	/** @return 返回positive trace中含有eventContent的event **/
	private static final Set<Event> getEventsMatchingContentConstraint(
			Collection<Event> events, LearnedEventContent eventContent) {
		Set<Event> results = new HashSet<Event>();
		for (Event event : events) {
			// Event 是否包含 eventContent
			if (eventContent.matches(event)) {
				results.add(event);
			}
		}
		return results;
	}

	/**
	 * @param events
	 *            => those events which a positive trace contains at a special
	 *            window.
	 * @param eventContent
	 *            => 满足了事件类型约束的某个事件 的全部属性集合。
	 * @param bounds
	 *            => at now is null.
	 * **/
	static final void updateContentConstraintBounds(Collection<Event> events,
			LearnedEventContent eventContent,
			Map<String, Map<Bound, Value>> bounds) {

		for (Event e : getEventsMatchingContentConstraint(events, eventContent)) {

			for (String name : eventContent.getAttributes()) {
				Value val = e.getValueFor(name);
				ValueType type = val.getValueType();

				// Bounds are meaningful only in the case of numeric values
				if (type != ValueType.LONG && type != ValueType.DOUBLE) {
					continue;
				}

				// 第一次bounds 是空集
				Map<Bound, Value> valuesMap = bounds.get(name);

				if (valuesMap == null) {
					valuesMap = new HashMap<Bound, Value>();
					valuesMap.put(Bound.LOWER, val);
					valuesMap.put(Bound.UPPER, val);
					bounds.put(name, valuesMap);
				} else {
					Value lowerOld = valuesMap.get(Bound.LOWER);
					Value upperOld = valuesMap.get(Bound.UPPER);

					double lowerOldNum = (lowerOld.getValueType() == ValueType.LONG) ? lowerOld
							.getLongValue() : lowerOld.getDoubleValue();
					double upperOldNum = (upperOld.getValueType() == ValueType.LONG) ? upperOld
							.getLongValue() : upperOld.getDoubleValue();
					switch (val.getValueType()) {
					case LONG:
						if (val.getLongValue() < lowerOldNum) {
							valuesMap.remove(Bound.LOWER);
							valuesMap.put(Bound.LOWER, val);
						}
						if (val.getLongValue() > upperOldNum) {
							valuesMap.remove(Bound.UPPER);
							valuesMap.put(Bound.UPPER, val);
						}
						break;
					case DOUBLE:
						if (val.getDoubleValue() < lowerOldNum) {
							valuesMap.remove(Bound.LOWER);
							valuesMap.put(Bound.LOWER, val);
						}
						if (val.getDoubleValue() > upperOldNum) {
							valuesMap.remove(Bound.UPPER);
							valuesMap.put(Bound.UPPER, val);
						}
						break;
					default:
						assert false : val.getValueType();
					}
				}
			}
		}
	}

}
