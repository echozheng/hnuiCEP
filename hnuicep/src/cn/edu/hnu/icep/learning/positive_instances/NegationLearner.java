package cn.edu.hnu.icep.learning.positive_instances;

import cn.edu.hnu.icep.event.model.Event;
import cn.edu.hnu.icep.history.History;
import cn.edu.hnu.icep.rules.NegationConstraint;
import cn.edu.hnu.icep.rules.Rule;
import cn.edu.hnu.icep.rules.filtering.Predicate;
import cn.edu.hnu.icep.eval.ParamHandler;
import cn.edu.hnu.icep.learning.ConstraintsSet;
import cn.edu.hnu.icep.rules.filtering.Constraint;
import cn.edu.hnu.icep.rules.filtering.Filter;
import cn.edu.hnu.icep.common.ConstraintOp;
import cn.edu.hnu.icep.event.model.Value;
import cn.edu.hnu.icep.event.model.Attribute;
import cn.edu.hnu.icep.common.Bound;
import cn.edu.hnu.icep.event.model.ValueType;
import cn.edu.hnu.icep.rules.EventConstraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NegationLearner {
	
	/**
	 * @param learnedRule
	 *            => 上面代码学习到的rule，包含事件约束，参数约束，顺序约束，集合约束
	 * @param trainingHistory
	 *            => 历史记录
	 * @param ph
	 *            => icep系统的参数配置
	 * @param positiveConstraints
	 *            => 上面代码学习到的 “正” 约束
	 * */
	public static Set<NegationConstraint> extractNegationConstraints(
						Rule rule, History history, ParamHandler ph,
						ConstraintsSet positiveConstraints) {

		Collection<Collection<Event>> events = getAllNegativeInstancesSatisfyingTheRule(rule, history);
		Set<Predicate> commonPredicates = getCommonPredicates(events, ph);
		
		return extractNegationConstraints(commonPredicates, positiveConstraints);
	}
	
	/**
	 * @param rule => 上面代码学习到的rule，包含事件约束，参数约束，顺序约束，集合约束
	 * @param history => 历史记录
	 * */
	private static Collection<Collection<Event>> getAllNegativeInstancesSatisfyingTheRule(
			Rule rule, History history) {
		
		Collection<Collection<Event>> results = new ArrayList<Collection<Event>>();
		List<Long> compositeEvTimestamps = history.getTimestampsWithCompositeEvents();
		
		//ts => 原子事件的时间 (为什么以原子事件为导向？？？)
		//也就是有多少个原子事件，就有多少个 positive trace.., 这与之前的学习方法都不符合。难道又是作者的失误？？？
		for (long ts : history.getTimestampsWithPrimitiveEvents()) {
			//选择原子事件发生的时候，没有复杂事件发生。以这个原子事件为首，向后推win窗口大小的时候，形成一条否定路径。
			//(negative trace)
			if (compositeEvTimestamps.contains(ts)) {
				continue;
			}
			long win = rule.getWin();
			long minTs = (ts - win > 0) ? (ts - win) : 0;
			Collection<Event> events = history.getAllEventsInWindow(minTs, ts);
			
			if (rule.isSatisfiedBy(events, ts)) {
				results.add(events);
			}
		}
		return results;
	}
	
	private static Set<Predicate> getCommonPredicates(Collection<Collection<Event>> events, ParamHandler ph) {
		Set<String> commonTypes = getCommonTypes(events, ph);
		removeCollectionsNotIncludingTypes(events, commonTypes);
		Set<Predicate> predicates = getCommonPredicates(events, commonTypes);
		return predicates;
	}
	
	/**
	 *@param events矩阵中的每一行都是 一条negative trace。 这些否定路径拥有相同的事件类型。
	 *@param types => events中包含的全部相同的事件类型。
	 * **/
	private static final Set<Predicate> getCommonPredicates(
			Collection<Collection<Event>> events, Set<String> types) {
		
		Set<Predicate> predicates = new HashSet<Predicate>();
		Map<String, Set<Constraint>> equalityConstraints = generateEqualityConstraints(events, types);
		Map<String, Set<Constraint>> minMaxConstraints = generateMinMaxConstraints(events, types);
		Set<String> allConstraintTypes = new HashSet<String>();
		
		allConstraintTypes.addAll(equalityConstraints.keySet());
		allConstraintTypes.addAll(minMaxConstraints.keySet());
		
		//type为事件类型
		for (String type : allConstraintTypes) {
			Set<Constraint> constraints = new HashSet<Constraint>();
			
			if (equalityConstraints.containsKey(type)) {
				constraints.addAll(equalityConstraints.get(type));
			}
			if (minMaxConstraints.containsKey(type)) {
				constraints.addAll(minMaxConstraints.get(type));
			}
			Filter f = new Filter(constraints);
			predicates.add(new Predicate(type, f));
		}
		return predicates;
	}
	
	/**
	 * 得到events矩阵 中 满足一定阈值的 事件类型,并返回
	 * */
	private static Set<String> getCommonTypes(Collection<Collection<Event>> events, ParamHandler ph) {

		Set<String> result = new HashSet<String>();
		Map<String, Integer> typesCount = new HashMap<String, Integer>();

		for (Collection<Event> eventCollection : events) {
			Set<String> types = new HashSet<String>();
			for (Event e : eventCollection) {
				types.add(e.getEventType());
			}
			addTypesToMap(types, typesCount);
		}

		int size = events.size();
		for (String type : typesCount.keySet()) {
			int typeCount = typesCount.get(type);
			double occurrencePerc = (double) typeCount / size;
			if (occurrencePerc > ph.getNegationFrequencyThreshold()) {
				result.add(type);
			}
		}
		return result;
	}
	
	private static void addTypesToMap(Set<String> types, Map<String, Integer> count) {
		for (String type : types) {
			Integer currentCount = count.get(type);
			if (currentCount == null) {
				count.put(type, 1);
			} else {
				count.remove(currentCount);
				count.put(type, currentCount + 1);
			}
		}
	}
	
	private static void removeCollectionsNotIncludingTypes(
			Collection<Collection<Event>> events, Set<String> types) {
		
		Iterator<Collection<Event>> it = events.iterator();
		while (it.hasNext()) {
			Collection<Event> evCollection = it.next();
		
			//如果evCollection 中 不全部 包含 types，就把这个evCollection 删除。
			if (!isCollectionContainingAllTypes(evCollection, types)) {
				it.remove();
			}
		}
	}
	
	private static boolean isCollectionContainingAllTypes(
			Collection<Event> events, Set<String> types) {
		Set<String> containingTypes = new HashSet<String>();
		for (Event ev : events) {
			containingTypes.add(ev.getEventType());
		}
		return containingTypes.containsAll(types);
	}
	
	private static Map<String, Set<Constraint>> generateEqualityConstraints(
			Collection<Collection<Event>> events, Set<String> types) {
		
		Map<String, Set<Constraint>> result = new HashMap<String, Set<Constraint>>();
		Map<String, Map<String, Set<Value>>> equalityValues = getEqualityValues(events, types);
		
		for (String type : equalityValues.keySet()) {
			Map<String, Set<Value>> innerMap = equalityValues.get(type);
			for (String attrName : innerMap.keySet()) {
				Set<Value> valuesSet = innerMap.get(attrName);
				assert (valuesSet.size() == 1);
				for (Value val : valuesSet) {
					Constraint c = new Constraint(attrName, ConstraintOp.EQ,val);
					Set<Constraint> constraints = result.get(attrName);
					if (constraints == null) {
						constraints = new HashSet<Constraint>();
						result.put(type, constraints);
					}
					constraints.add(c);
				}
			}
		}
		return result;
	}
	
	private static Map<String, Map<String, Set<Value>>> getEqualityValues(
			Collection<Collection<Event>> events, Set<String> types) {
		
		Map<String, Map<String, Set<Value>>> equalityValues = new HashMap<String, Map<String, Set<Value>>>();
		
		boolean first = true;
		for (Collection<Event> evCollection : events) {
			if (first) {
				equalityValues = extractEqualityValues(evCollection, types);
				first = false;
			} else {
				Map<String, Map<String, Set<Value>>> newEqualityValues = extractEqualityValues(
						evCollection, types);
				updateEqualityMap(equalityValues, newEqualityValues);
			}
		}
		return equalityValues;
	}
	
	private static Map<String, Map<String, Set<Value>>> extractEqualityValues(
			Collection<Event> events, Set<String> types) {
		
		Map<String, Map<String, Set<Value>>> values = new HashMap<String, Map<String, Set<Value>>>();
		for (Event ev : events) {
			String type = ev.getEventType();
			if (!types.contains(type)) {
				continue;
			}
			
			for (Attribute attr : ev) {
				addValueToEqualityMap(ev.getEventType(), attr.getName(),attr.getValue(), values);
			}
		}
		return values;
	}
	
	private static void addValueToEqualityMap(String type,String attrName, Value val,
							Map<String, Map<String, Set<Value>>> values) {
		Map<String, Set<Value>> innerMap = values.get(type);
		if (innerMap == null) {
			innerMap = new HashMap<String, Set<Value>>();
			values.put(type, innerMap);
		}
		Set<Value> valuesSet = innerMap.get(attrName);
		if (valuesSet == null) {
			valuesSet = new HashSet<Value>();
			innerMap.put(attrName, valuesSet);
		}
		valuesSet.add(val);
	}
	
	private static void updateEqualityMap(Map<String, Map<String, Set<Value>>> map,
			Map<String, Map<String, Set<Value>>> newMap) {
		
		Set<String> typesToRemove = new HashSet<String>();
		for (String type : map.keySet()) {
			if (!newMap.containsKey(type)) {
				typesToRemove.add(type);
				continue;
			}
			Map<String, Set<Value>> innerMap = map.get(type);
			Map<String, Set<Value>> innerNewMap = newMap.get(type);
			Set<String> attrNamesToRemove = new HashSet<String>();
			for (String attrName : innerMap.keySet()) {
				if (!innerNewMap.containsKey(attrName)) {
					attrNamesToRemove.add(attrName);
					continue;
				}
				Set<Value> mapValues = innerMap.get(attrName);
				Set<Value> newMapValues = innerNewMap.get(attrName);
				mapValues.retainAll(newMapValues);
				if (mapValues.isEmpty()) {
					attrNamesToRemove.add(attrName);
				}
			}
			for (String nameToRemove : attrNamesToRemove) {
				innerMap.remove(nameToRemove);
			}
			if (innerMap.isEmpty()) {
				typesToRemove.add(type);
			}
		}
		for (String typeToRemove : typesToRemove) {
			map.remove(typeToRemove);
		}
	}
	
	private static final Map<String, Set<Constraint>> generateMinMaxConstraints(
			Collection<Collection<Event>> events, Set<String> types) {
		
		Map<String, Set<Constraint>> result = new HashMap<String, Set<Constraint>>();
		Map<String, Map<String, Map<Bound, Value>>> bounds = getBounds(events,types);
		for (String type : bounds.keySet()) {
			Map<String, Map<Bound, Value>> innerMap = bounds.get(type);
			Set<Constraint> constraints = new HashSet<Constraint>();
			for (String attrName : innerMap.keySet()) {
				Map<Bound, Value> valuesMap = innerMap.get(attrName);
				Value minVal = valuesMap.get(Bound.LOWER);
				Value maxVal = valuesMap.get(Bound.UPPER);
				Constraint c1 = new Constraint(attrName, ConstraintOp.GT_EQ,minVal);
				Constraint c2 = new Constraint(attrName, ConstraintOp.LT_EQ,maxVal);
				constraints.add(c1);
				constraints.add(c2);
			}
			result.put(type, constraints);
		}
		return result;
	}
	
	private static Map<String, Map<String, Map<Bound, Value>>> getBounds(
			Collection<Collection<Event>> events, Set<String> types) {
		Map<String, Map<String, Map<Bound, Value>>> bounds = 
								new HashMap<String, Map<String, Map<Bound, Value>>>();
		
		boolean first = true;
		for (Collection<Event> evCollection : events) {
			if (first) {
				bounds = extractBounds(evCollection, types);
				if (bounds.isEmpty())
					return bounds;
				first = false;
			} else {
				Map<String, Map<String, Map<Bound, Value>>> newBounds = extractBounds(
						evCollection, types);
				updateBoundMap(bounds, newBounds);
			}
		}
		return bounds;
	}
	
	private static final Map<String, Map<String, Map<Bound, Value>>> extractBounds(
			Collection<Event> events, Set<String> types) {
		
		Map<String, Map<String, Map<Bound, Value>>> values = 
								new HashMap<String, Map<String, Map<Bound, Value>>>();
		for (Event ev : events) {
			String type = ev.getEventType();
			if (!types.contains(type)) {
				continue;
			}
			for (Attribute attr : ev) {
				if ((attr.getValue().getValueType() == ValueType.LONG) || 
										(attr.getValue().getValueType() == ValueType.DOUBLE)) {
					addValueToBoundMap(ev.getEventType(), attr.getName(),attr.getValue(), values);
				}
			}
		}
		return values;
	}
	
	private static void addValueToBoundMap(String type, String attrName,
			Value val, Map<String, Map<String, Map<Bound, Value>>> values) {
		assert (val.getValueType() == ValueType.LONG || val.getValueType() == ValueType.DOUBLE);
		Map<String, Map<Bound, Value>> innerMap = values.get(type);
		if (innerMap == null) {
			innerMap = new HashMap<String, Map<Bound, Value>>();
			values.put(type, innerMap);
		}
		Map<Bound, Value> valuesMap = innerMap.get(attrName);
		if (valuesMap == null) {
			valuesMap = new HashMap<Bound, Value>();
			innerMap.put(attrName, valuesMap);
		}
		Value minVal = valuesMap.get(Bound.LOWER);
		Value maxVal = valuesMap.get(Bound.UPPER);
		
		if (minVal == null) {
			valuesMap.put(Bound.LOWER, val);
		} else {
			double minValNum = (minVal.getValueType() == ValueType.LONG) ? 
									minVal.getLongValue() : minVal.getDoubleValue();
			switch (val.getValueType()) {
			case LONG:
				if (val.getLongValue() < minValNum) {
					valuesMap.remove(Bound.LOWER);
					valuesMap.put(Bound.LOWER, val);
				}
				break;
			case DOUBLE:
				if (val.getDoubleValue() < minValNum) {
					valuesMap.remove(Bound.LOWER);
					valuesMap.put(Bound.LOWER, val);
				}
				break;
			default:
				assert false : val.getValueType();
			}

		}
		if (maxVal == null) {
			valuesMap.put(Bound.UPPER, val);
		} else {
			double maxValNum = (maxVal.getValueType() == ValueType.LONG) ? maxVal
					.getLongValue() : maxVal.getDoubleValue();
			switch (val.getValueType()) {
			case LONG:
				if (val.getLongValue() > maxValNum) {
					valuesMap.remove(Bound.UPPER);
					valuesMap.put(Bound.UPPER, val);
				}
				break;
			case DOUBLE:
				if (val.getDoubleValue() > maxValNum) {
					valuesMap.remove(Bound.UPPER);
					valuesMap.put(Bound.UPPER, val);
				}
				break;
			default:
				assert false : val.getLongValue();
			}
			valuesMap.remove(Bound.UPPER);
			valuesMap.put(Bound.UPPER, val);
		}
	}
	
	private static void updateBoundMap(Map<String, Map<String, Map<Bound, Value>>> map,
							Map<String, Map<String, Map<Bound, Value>>> newMap) {
		
		Set<String> typesToRemove = new HashSet<String>();
		for (String type : map.keySet()) {
			if (!newMap.containsKey(type)) {
				typesToRemove.add(type);
				continue;
			}
			Map<String, Map<Bound, Value>> innerMap = map.get(type);
			Map<String, Map<Bound, Value>> innerNewMap = newMap.get(type);
			Set<String> attrNamesToRemove = new HashSet<String>();
			for (String attrName : innerMap.keySet()) {
				if (!innerNewMap.containsKey(attrName)) {
					attrNamesToRemove.add(attrName);
					continue;
				}
				Map<Bound, Value> mapValues = innerMap.get(attrName);
				Value currentMin = mapValues.get(Bound.LOWER);
				Value currentMax = mapValues.get(Bound.UPPER);
				assert (currentMin.getValueType() == ValueType.LONG || currentMin.getValueType() == ValueType.DOUBLE);
				assert (currentMax.getValueType() == ValueType.LONG || currentMax.getValueType() == ValueType.DOUBLE);
				double currentMinNum = (currentMin.getValueType() == ValueType.LONG) ? 
										currentMin.getLongValue() : currentMin.getDoubleValue();
										
				double currentMaxNum = (currentMax.getValueType() == ValueType.LONG) ? 
										currentMax.getLongValue() : currentMax.getDoubleValue();
				Map<Bound, Value> newMapValues = innerNewMap.get(attrName);
				Value newMin = newMapValues.get(Bound.LOWER);
				Value newMax = newMapValues.get(Bound.UPPER);
				
				switch (newMin.getValueType()) {
				case LONG:
					if (newMin.getLongValue() < currentMinNum) {
						mapValues.remove(Bound.LOWER);
						mapValues.put(Bound.LOWER, newMin);
					}
					break;
				case DOUBLE:
					if (newMin.getDoubleValue() < currentMinNum) {
						mapValues.remove(Bound.LOWER);
						mapValues.put(Bound.LOWER, newMin);
					}
					break;
				default:
					assert false : newMin.getValueType();
				}
				switch (newMax.getValueType()) {
				case LONG:
					if (newMax.getLongValue() > currentMaxNum) {
						mapValues.remove(Bound.UPPER);
						mapValues.put(Bound.UPPER, newMax);
					}
					break;
				case DOUBLE:
					if (newMax.getDoubleValue() > currentMaxNum) {
						mapValues.remove(Bound.UPPER);
						mapValues.put(Bound.UPPER, newMax);
					}
					break;
				default:
					assert false : newMax.getValueType();
				}
			}
			for (String nameToRemove : attrNamesToRemove) {
				innerMap.remove(nameToRemove);
			}
			if (innerMap.isEmpty()) {
				typesToRemove.add(type);
			}
		}
		for (String typeToRemove : typesToRemove) {
			map.remove(typeToRemove);
		}
	}
	
	private static Set<NegationConstraint> extractNegationConstraints(
			Set<Predicate> predicates, ConstraintsSet positiveConstraints) {
		
		Set<NegationConstraint> negConstraints = new HashSet<NegationConstraint>();
		
		outerloop: for (Predicate p : predicates) {
			for (EventConstraint positiveEvConstraint : positiveConstraints.getEventConstraints()) {
				if (areCompatible(p, positiveEvConstraint.getPredicate())) {
					continue outerloop;
				}
			}
			NegationConstraint neg = new NegationConstraint(p);
			negConstraints.add(neg);
		}
		
		return negConstraints;
	}
	
	/**
	 * note : 注意我修改了 Predicate类，其中只有一个 Filter对象。
	 * **/
	private static boolean areCompatible(Predicate p1, Predicate p2) {
		if (!p1.getEventType().equals(p2.getEventType())) {
			return false;
		}
		if (areCompatible(p1.getFilter(), p2.getFilter())) {
			return true;
		}
		return false;
	}

	private static boolean areCompatible(Filter f1, Filter f2) {
		Set<String> names1 = getAllNamesIn(f1);
		Set<String> names2 = getAllNamesIn(f2);
		if (!names1.containsAll(names2))
			return false;
		if (!names2.containsAll(names1))
			return false;
		return true;
	}
	
	private static Set<String> getAllNamesIn(Filter f) {
		Set<String> names = new HashSet<String>();
		for (Constraint c : f) {
			names.add(c.getName());
		}
		return names;
	}
}
