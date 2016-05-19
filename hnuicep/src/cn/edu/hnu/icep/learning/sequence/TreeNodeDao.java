package cn.edu.hnu.icep.learning.sequence;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

import cn.edu.hnu.icep.rules.EventConstraint;

/**
 * 节点的基本操作
 * **/
public class TreeNodeDao {
	
	/**
	 * 判断要添加的子节点是否已经存在
	 *@param parent 父节点
	 *@param  event 要查询的子节点原子事件
	 *@return 如果已经存在，则返回该节点 在 当前节点的 childrenList中的索引, 否则返回-1。
	 * */
	public int isChildNodeAlreadyExist(TreeNode parent,EventConstraint eventConstraint) {
		for(int index = 0;index < parent.getChildCount();index++) {
			TreeNode childNode = parent.getChildrenList().get(index);
			//equals方法 是自己写的。
			if(childNode.getEventConstraint().equals(eventConstraint)) {
				//找到parent子节点 已经包含该事件，立即返回
				return index;
			}
		}
		return -1;
	}
	
	/**
	 * 向当前节点 添加 子节点
	 * @param parent 当前节点
	 * @param eventConstraint 要添加的子节点原子事件 所对应的约束
	 * @return 返回添加之后节点的 对象。
	 * **/
	public TreeNode addChildNode(TreeNode parent,EventConstraint eventConstraint) {
		int index = isChildNodeAlreadyExist(parent,eventConstraint);
		if(index != -1) {
			//如果当前节点下 已经存在 该类型的子节点，不添加event，把当前指针移动到 其相应的子节点上。
			return parent.getChildrenList().get(index);
		}
		
		//如果当前节点下 不存在 该类型的子节点，添加event，把当前指针移动到 该子节点上。
		//封装成 TreeNode
		TreeNode childNode = new TreeNode(eventConstraint);
		//设置其父节点
		childNode.setParent(parent);
		
		//父节点添加子节点
		parent.getChildrenList().add(childNode);
		//父节点添加未访问子节点
		parent.getUnVisitedList().add(childNode);
		//子节点计数 加 1
		parent.setChildCount(parent.getChildCount() + 1);
		
		//返回刚刚添加子节点的对象
		return childNode; 
	}
	
	/**
	 * 从当前节点 回溯到 root节点，为下一条 positive trace的添加 做准备
	 * @author hduser
	 * **/
	public TreeNode backToRoot(TreeNode currentNode) {
		TreeNode pointNode = currentNode;
		while(pointNode.getParent() != null) {
			//如果父节点不为空，则指针指向父节点
			pointNode = pointNode.getParent();
		}
		
		return pointNode;
	}
	
	/**
	 * 判断当前节点 的子节点 是否已经全部访问完毕. 
	 * @return 如果节点全部访问完毕 ? true : false
	 * **/
	public boolean isAllVisited(TreeNode currentNode) {
		if(currentNode.getUnVisitedList().size() == 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * 从当前节点 未访问过的子节点中，选出第一个。
	 * 如果子节点已经全部访问过，则返回 null
	 * **/
	public TreeNode getAUnVisitedChildNode(TreeNode currentNode) {
		if(isAllVisited(currentNode)) {
			return null;
		}
		TreeNode childNode = currentNode.getUnVisitedList().getFirst();
		
		//把该子节点 从 “未访问子节点”链表中 删去
		//System.out.println("返回的下一个 子节点 = " + childNode.getEventConstraint().toString2());
		removeVisitedNode(currentNode,childNode);
		return childNode;
	}
	
	/**
	 * 移除已经访问过的 子节点
	 * **/
	public void removeVisitedNode(TreeNode currentNode,TreeNode childNode) {
		//System.out.println("remove childnode of " + childNode.getEventConstraint().toString2());
		currentNode.getUnVisitedList().remove(childNode);
	}
	
	/**
	 * 遍历多叉树，将遍历结果 装入 相应数据结构中，并返回。
	 * 深度遍历
	 * **/
	public HashMap<Integer,LinkedList<EventConstraint>>traverseTree(TreeNode root) {
		//保存树中的所有路径
		HashMap<Integer,LinkedList<EventConstraint>> traces = new HashMap<Integer,LinkedList<EventConstraint>>();
		
		Stack<TreeNode> nodeStack = new Stack<TreeNode>();
		nodeStack.push(root);
		//System.out.println("root.getChildCount = " + root.getChildCount());
		//System.out.println("root.getChildCount 0 = " + root.getChildrenList().get(0).getEventConstraint().toString2());
		//System.out.println("root.getChildCount 1 = " + root.getChildrenList().get(1).getEventConstraint().toString2());
		
		while(!nodeStack.empty()) {
			//得到栈顶节点的 第一个未访问子节点
			TreeNode childNode = getAUnVisitedChildNode(nodeStack.peek());
			if(childNode == null) {
				//如果没有子节点，则当前节点的所有子节点已经访问过了，进行退栈操作
				nodeStack.pop();
				continue;
			}

			//子节点 入栈
			nodeStack.push(childNode);
			if(nodeStack.peek().getChildCount() == 0) {
				//如果某个子节点 没有 任何子节点，则该节点就是叶子节点，返回路径
				increaseATrace(nodeStack,traces);
			}
		}
		return traces;
	}
	
	/**
	 * 取出 stack中的内容，封装成 一条路径。
	 * **/
	public void increaseATrace(Stack<TreeNode> nodeStack,HashMap<Integer,LinkedList<EventConstraint>> traces) {
		
		//outputATrace(nodeStack);
		
		//traces中 当前的路径条数
		int count = traces.size();
		LinkedList<EventConstraint> trace = new LinkedList<EventConstraint>();
		
		//int index = 1 剔除root节点
		for(int index = 1;index < nodeStack.size();index++) {
			trace.add(nodeStack.get(index).getEventConstraint());
		}
		traces.put(count++, trace);
	}
	
	/**
	 * 输出一条路径
	 * **/
	public void outputATrace(Stack<TreeNode> nodeStack) {
		for(int index = 1;index < nodeStack.size();index++) {
			if(index != (nodeStack.size() -1)) {
				System.out.print(nodeStack.get(index).getEventConstraint().toString2() + "-->");
			} else {
				System.out.print(nodeStack.get(index).getEventConstraint().toString2());
			}
		}
		System.out.println("\n");
	}

}
