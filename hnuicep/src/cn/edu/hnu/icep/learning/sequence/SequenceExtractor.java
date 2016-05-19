package cn.edu.hnu.icep.learning.sequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import cn.edu.hnu.icep.event.model.Attribute;
import cn.edu.hnu.icep.event.model.Event;
import cn.edu.hnu.icep.rules.EventConstraint;
import cn.edu.hnu.icep.rules.filtering.Filter;
import cn.edu.hnu.icep.rules.filtering.Predicate;

/**
 * 学习positive trace的
 * **/
public class SequenceExtractor {
	
	//节点操作对象
	private static TreeNodeDao treeNodeDao = new TreeNodeDao();
	
	//根节点
	private static TreeNode root = new TreeNode(null);
	
	//历史记录中 positive Trace 总条数
	private static long numOfPositiveTrace = 0;
	
	//顺序树中的总分枝
	private static long allBranchesInTree = 0;

	/**
	 *测试使用的主函数 
	 * **/
	public static void main(String args[]) {
		Filter filter = null;
		Predicate predicateE4 = new Predicate("event4",filter);
		EventConstraint eventConstraintE4 = new EventConstraint(predicateE4);
		Predicate predicateE5 = new Predicate("event5",filter);
		EventConstraint eventConstraintE5 = new EventConstraint(predicateE5);
		Predicate predicateE8 = new Predicate("event8",filter);
		EventConstraint eventConstraintE8 = new EventConstraint(predicateE8);
		
		Set<EventConstraint> eventConstraints = new HashSet<EventConstraint>();
		eventConstraints.add(eventConstraintE4);
		eventConstraints.add(eventConstraintE5);
		eventConstraints.add(eventConstraintE8);
		
		//////////////////////////////////////////////////////////
		Attribute attribute = new Attribute("attribute1",25);
		Event e1 = new Event("event1",1,attribute);
		Event e4 = new Event("event4",1,attribute);
		Event e5 = new Event("event5",1,attribute);
		Event e6 = new Event("event6",1,attribute);
		Event e8 = new Event("event8",1,attribute);
		Event e16 = new Event("event16",1,attribute);
		
		LinkedList<Event> positiveTrace0 = new LinkedList<Event>();
		LinkedList<Event> positiveTrace1 = new LinkedList<Event>();
		LinkedList<Event> positiveTrace2 = new LinkedList<Event>();
		LinkedList<Event> positiveTrace3 = new LinkedList<Event>();
		LinkedList<Event> positiveTrace4 = new LinkedList<Event>();
		
		positiveTrace0.add(e1);
		positiveTrace0.add(e4);
		positiveTrace0.add(e5);
		
		positiveTrace1.add(e1);
		positiveTrace1.add(e4);
		positiveTrace1.add(e5);
		positiveTrace1.add(e8);
		
		positiveTrace2.add(e4);
		positiveTrace2.add(e5);
		positiveTrace2.add(e6);
		
		positiveTrace3.add(e4);
		positiveTrace3.add(e5);
		positiveTrace3.add(e16);
		
		positiveTrace4.add(e8);
		positiveTrace4.add(e4);
		positiveTrace4.add(e5);
		
		Collection<Collection<Event>> positiveTraces = new ArrayList<Collection<Event>>();
		positiveTraces.add(positiveTrace0);
		positiveTraces.add(positiveTrace1);
		positiveTraces.add(positiveTrace2);
		positiveTraces.add(positiveTrace3);
		positiveTraces.add(positiveTrace4);
		
		Collection<Map<EventConstraint, Set<EventConstraint>>> extractedSequences = 
				new ArrayList<Map<EventConstraint, Set<EventConstraint>>>();
		
		learnSequceConstraint(positiveTraces,extractedSequences,eventConstraints);
	}
	
	/**
	 * 学习顺序约束
	 * @author hduser
	 * @param positiveTraces 所有 时间窗口为win 的positive trace。
	 * @param extractedSequences 用于 存放学习到的顺序约束。
	 * @param eventConstraints 之前系统 学习到的 EventConstraint集合。
	 * **/
	public static void learnSequceConstraint(
			Collection<Collection<Event>> positiveTraces,
			Collection<Map<EventConstraint, Set<EventConstraint>>> extractedSequences, 
			Set<EventConstraint> eventConstraints) {

		System.out.println("in the function of learnSequenceConstraint, SequenceExtractor ...");
		System.out.println("构建顺序树");
		//构建顺序树
		buildSequenceTree(positiveTraces,eventConstraints);
		
		System.out.println("positive trace的总条数 = " + positiveTraces.size());
		numOfPositiveTrace = (long)positiveTraces.size();
		
		//学习每一分支的顺序约束
		extractSequenceConstraint(extractedSequences);
		
		/*Map<EventConstraint, Set<EventConstraint>> seqConstraints = 
								FeaturesMerger.mergeSequenceConstraints(extractedSequences);
		
		for(EventConstraint referenceConstraint : seqConstraints.keySet()) {
			for(EventConstraint followingConstraint : seqConstraints.get(referenceConstraint)) {
				System.out.println("先后顺序: " + referenceConstraint.toString2() + "-->" +followingConstraint.toString2());
			}
		}*/
		
	}

	/**
	 * 构建顺序树
	 * @param positiveTraces 所有 时间窗口为win 的positive trace。
	 * @param eventConstraints 之前系统 学习到的 EventConstraint集合。
	 * **/
	public static void buildSequenceTree(Collection<Collection<Event>> positiveTraces,
			Set<EventConstraint> eventConstraints) {
		//移动指针，最初指向当前父节点
		TreeNode currentNode = new TreeNode(null);
		currentNode = root;
		//一条一条 positive trace处理
		for(Collection<Event> onePositiveTrace : positiveTraces) {
			//将一条positive trace 转换成 以 EventConstraint为单位的 链表
			LinkedList <EventConstraint> oneTrace = 
							transformTraceIntoConstaints(eventConstraints,onePositiveTrace);
			
			//将该positive trace 添加到 tree中
			for(EventConstraint eConstraint : oneTrace) {
				//addChildNode 返回当前添加的子节点
				currentNode = treeNodeDao.addChildNode(currentNode, eConstraint);
			}
			
			//返回的 root节点
			root = treeNodeDao.backToRoot(currentNode);
			//重置移动指针 到根节点
			currentNode = root;
		}
	}
	
	/**
	 * 将一条positive trace 转换成 以 EventConstraint为单位的 链表
	 * @param eventConstraints 学习到的 EventConstraint约束列表
	 * @param onePositiveTrace 一条positive trace 上的事件集合
	 * **/
	private static LinkedList<EventConstraint> transformTraceIntoConstaints(
			Set<EventConstraint> eventConstraints, Collection<Event> onePositiveTrace) {
		
		//存储一条 positive trace中 满足EventConstraint的原子事件，不改变其先后顺序
		LinkedList<EventConstraint> oneTrace = new LinkedList<EventConstraint> ();
		
		for(Event event : onePositiveTrace) {
			for(EventConstraint eventConstraint : eventConstraints) {
				if(eventConstraint.isEventSatisfied(event)) {
					//event 满足 EventConstraint,添加进链表，并退出内循环
					oneTrace.add(eventConstraint);
					continue;
				}
			}
			
		}
		return oneTrace;
	}
	
	/**
	 * 解析顺序树，学习每一分支的顺序约束，并添加到到extractedSequences中。
	 * **/
	public static void extractSequenceConstraint(
			Collection<Map<EventConstraint, Set<EventConstraint>>> extractedSequences) {
		
		//遍历顺序树，得到全部的分支
		HashMap<Integer,LinkedList<EventConstraint>> allBranch = treeNodeDao.traverseTree(root);
		
		System.out.println("all the branch's size = " + allBranch.size());
		allBranchesInTree = (long)allBranch.size();
		
		for(int i : allBranch.keySet()) {
			//得到一条分支
			LinkedList<EventConstraint> aBranch = allBranch.get(i);
			//分析一条分支的顺序约束
			Map<EventConstraint, Set<EventConstraint>> aBranchSequence = extractABranchSequence(aBranch);
			extractedSequences.add(aBranchSequence);
		}
	}

	/**
	 * 解析树的一条分支的 顺序约束
	 * @param aBranch 从顺序树中 解析出的 一条分支
	 * @return 从该分支中 学习到的一条约束
	 * **/
	public static Map<EventConstraint, Set<EventConstraint>> extractABranchSequence(
			LinkedList<EventConstraint> aBranch) {
		
		Map<EventConstraint, Set<EventConstraint>> aBranchSequence = 
							new HashMap<EventConstraint, Set<EventConstraint>>();
		
		for(int index = 0;index < aBranch.size() - 1;index++) {
			Set<EventConstraint> eConstraints = new HashSet<EventConstraint>();
			for(int inner = index + 1;inner < aBranch.size();inner++) {
				//顺序约束的 前后事件类型约束 应属于 不同的事件类型约束
				if(!aBranch.get(index).equals(aBranch.get(inner))) {
					eConstraints.add(aBranch.get(inner));
				}
			}
			aBranchSequence.put(aBranch.get(index), eConstraints);
		}
		
		return aBranchSequence;
	}
	
	public static long getNumOfPositiveTrace() {
		return numOfPositiveTrace;
	}

	public static void setNumOfPositiveTrace(long numOfPositiveTrace) {
		SequenceExtractor.numOfPositiveTrace = numOfPositiveTrace;
	}

	public static long getAllBranchesInTree() {
		return allBranchesInTree;
	}

	public static void setAllBranchesInTree(long allBranchesInTree) {
		SequenceExtractor.allBranchesInTree = allBranchesInTree;
	}
	
}
