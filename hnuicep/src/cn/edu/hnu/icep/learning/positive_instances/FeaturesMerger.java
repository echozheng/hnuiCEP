package cn.edu.hnu.icep.learning.positive_instances;

import cn.edu.hnu.icep.learning.LearnedEventContent;
import cn.edu.hnu.icep.rules.EventConstraint;
import cn.edu.hnu.icep.zql.update.AssistDebug;
import cn.edu.hnu.icep.event.model.Value;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FeaturesMerger {

	/**
	 * 对由上面解析得到的 原子事件的属性约束(包含哪几种属性)进行取交集。
	 * **/
	public static final Map<String, Map<LearnedEventContent, Integer>> mergeEventContentCount(
			Collection<Map<String, Map<LearnedEventContent, Integer>>> evContentConstraints) {

		Map<String, Map<LearnedEventContent, Integer>> result = new HashMap<String, Map<LearnedEventContent, Integer>>();
		boolean first = true;

		//以一条 positive为单位进行计算
		for (Map<String, Map<LearnedEventContent, Integer>> onePostiveTraceMap : evContentConstraints) {
			if (first) {
				// (把onePostiveTraceMap 中的数据取出，再装入 result中)
				// type为事件类型，例如Event5
				for (String eventType : onePostiveTraceMap.keySet()) {
					Map<LearnedEventContent, Integer> innerMap = onePostiveTraceMap.get(eventType);
					Map<LearnedEventContent, Integer> resultInnerMap = new HashMap<LearnedEventContent, Integer>();
					
					// learnedEventContent实例：{Type=Event6, attributes=[]]=6}
					for (LearnedEventContent learnedContent : innerMap.keySet()) {
						int count = innerMap.get(learnedContent);
						resultInnerMap.put(learnedContent, count);
					}
					result.put(eventType, resultInnerMap);
				}
				first = false;
			} else {
				Map<String, Map<LearnedEventContent, Integer>> newResult = 
									new HashMap<String, Map<LearnedEventContent, Integer>>();

				// type为事件类型，例如Event6. 注意下 循环变量的范围发生了变化了，这回是result。
				// 也就是把第一次 获得到的 事件属性集合result 作为比较的原型
				for (String eventType : result.keySet()) {
					//map为一个positive trace中全部事件的属性约束集合。
					//innerMap为一个positive trace中某个具体事件的属性约束集合。
					 /*[Type=Event6, attributes=[]]=6, 
					   [Type=Event6,attributes=[Attribute0]]=2,
					   [Type=Event6,attributes=[Attribute1]]=2, 
					   [Type=Event6,attributes=[Attribute2]]=2,
					   [Type=Event6,attributes=[Attribute1, Attribute0]]=1,
					   [Type=Event6,attributes=[Attribute2, Attribute0]]=1,
					   [Type=Event6,attributes=[Attribute2, Attribute1]]=1,
					   [Type=Event6,attributes=[Attribute2, Attribute1, Attribute0]]=1*/
					Map<LearnedEventContent, Integer> innerMap = onePostiveTraceMap.get(eventType);
					if (innerMap == null) { continue;}
						
					/*
					 * resultInnerMap实例(注意上面的innerMap 与 resultInnerMap 所对应的eventType是一致的。)
					 * {[Type=Event6,attributes=[]]=6,
					 *  [Type=Event6,attributes=[Attribute0]]=2,
					 *  [Type=Event6,attributes=[Attribute1]]=2,
					 *  [Type=Event6,attributes=[Attribute2]]=2,
					 *  [Type=Event6,attributes=[Attribute1, Attribute0]]=1,
					 *  [Type=Event6,attributes=[Attribute2, Attribute0]]=1,
					 *  [Type=Event6,attributes=[Attribute2, Attribute1]]=1,
					 *  [Type=Event6,attributes=[Attribute2, Attribute1, Attribute0]]=1}
					 */
					Map<LearnedEventContent, Integer> resultsInnerMap = result.get(eventType);

					//innerMap 与 result中包含resultsInnerMap，进行取交集，把最后的结果放入newInnerMap
					Map<LearnedEventContent, Integer> newInnerMap = new HashMap<LearnedEventContent, Integer>();

					for (LearnedEventContent learnedContent : resultsInnerMap.keySet()) {
						Integer val = innerMap.get(learnedContent);
						if (val == null) { continue; }
						Integer resultsVal = resultsInnerMap.get(learnedContent);
						// 取属性出现次数的最小值
						int minCount = Math.min(resultsVal, val);
						newInnerMap.put(learnedContent, minCount);
					}
					if (!newInnerMap.isEmpty()) {
						newResult.put(eventType, newInnerMap);
					}
				}
				// 有时候，result的返回是空集。
				result = newResult;
			}
		}

		// result的值是:全部positive traces的事件 取交集之后 的事件集合，而且事件的属性出现的次数也是最少的。
		return result;
	}

	/**
	 * @param constraints
	 *            =>每一行数据是在特定的窗口下，每条positive trace中 包含参数eventContent的全部事件中，
	 *            与eventContent相同属性 的值的集合。
	 * @return 取交集之后的值集合
	 * **/
	public static final Map<String, Set<Value>> mergeContentConstraintValues(
			Collection<Map<String, Set<Value>>> constraints) {
		
		Map<String, Set<Value>> results = new HashMap<String, Set<Value>>();
		boolean first = true;
		// constrMap实例：
		// {Attribute2=[34], Attribute1=[54], Attribute0=[36]}
		// {Attribute2=[34, 97], Attribute1=[54, 82], Attribute0=[23, 25]}
		for (Map<String, Set<Value>> constrMap : constraints) {
			if (first) {
				for (String attrName : constrMap.keySet()) {
					Set<Value> valuesCopy = new HashSet<Value>(constrMap.get(attrName));
					results.put(attrName, valuesCopy);
				}
				first = false;
			} else {
				Set<String> namesToRemove = new HashSet<String>();
				// result第一次记录的是 参数constraints中的第一条数据
				// 例如：{Attribute2=[34], Attribute1=[54], Attribute0=[36]}
				for (String attrName : results.keySet()) {
					if (!constrMap.containsKey(attrName)) {
						namesToRemove.add(attrName);
					} else {
						Set<Value> resultValue = results.get(attrName);
						Set<Value> values = constrMap.get(attrName);
						// resultValue与values取交集
						resultValue.retainAll(values);
						
						if (resultValue.isEmpty()) {
							namesToRemove.add(attrName);
						}
					}
				}
				for (String nameToRemove : namesToRemove) {
					results.remove(nameToRemove);
				}
			}
		}
		/*System.out.println("results's value");
		System.out.println(results.toString());*/
		AssistDebug.takeABreak();
		return results;
	}

	/**
	 * 多所学习到的顺序约束 进行取交集
	 * @param seqConstraints,学习到的顺序约束
	 * @return 返回取交集之后的 顺序约束，也就是最后学习到的顺序约束
	 * **/
	public static Map<EventConstraint, Set<EventConstraint>> mergeSequenceConstraints(
			Collection<Map<EventConstraint, Set<EventConstraint>>> seqConstraints) {

		Map<EventConstraint, Set<EventConstraint>> result =
								new HashMap<EventConstraint, Set<EventConstraint>>();
		boolean first = true;
		for (Map<EventConstraint, Set<EventConstraint>> map : seqConstraints) {
			if (first) {
				//第一次
				for (EventConstraint constr1 : map.keySet()) {
					Set<EventConstraint> set = new HashSet<EventConstraint>(
							map.get(constr1));
					result.put(constr1, set);
				}
				first = false;
			} else {
				Set<EventConstraint> constraintsToRemove = new HashSet<EventConstraint>();
				for (EventConstraint constr1 : result.keySet()) {

					if (!map.containsKey(constr1)) {
						// 做的好！ remove掉数据,这才是 merge 的精髓所在
						constraintsToRemove.add(constr1);
					} else {
						Set<EventConstraint> constrSet = result.get(constr1);
						// 如果map.get(constr1)
						// 包含在constrSet中（要一模一样），就保留；否则，就清空原有的数据
						constrSet.retainAll(map.get(constr1));
						if (constrSet.isEmpty()) {
							constraintsToRemove.add(constr1);
						}
					} // else
				} // for
				
				for (EventConstraint constraintToRemove : constraintsToRemove) {
					result.remove(constraintToRemove);
				}
				// 如果某个时候，result的内容为空，就直接退出了。
				// 说明没有任何交集了。
				if (result.isEmpty()) {
					break;
				}
			}
		}
		
		if(result.size() == 0) {
			System.out.println("------- 没有学习到 任何的 顺序约束 -----------");
		}
		return result;
	}
}