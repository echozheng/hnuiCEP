package cn.edu.hnu.icep.learning.sequence;

import java.util.LinkedList;

import cn.edu.hnu.icep.rules.EventConstraint;

/**
 * 代表一个树节点
 * **/
public class TreeNode {
	
	//当前节点存储的事件类型约束
	private EventConstraint eventConstraint;
	
	//当前节点的父节点
	private TreeNode parent;
	
	//当前节点的子节点集合(可以考虑重写 LinkedList，)
	private LinkedList<TreeNode> childrenList;
	
	//当前节点的子节点计数
	private int childCount;
	
	//当前节点未访问过的 子节点
	private LinkedList<TreeNode> unVisitedList;
	
	//构造函数
	public TreeNode(EventConstraint constraint) {
		this.eventConstraint = constraint;
		this.parent = null;
		this.childrenList = new LinkedList<TreeNode>();
		this.childCount = 0;
		this.unVisitedList = new LinkedList<TreeNode>();
	}

	public TreeNode getParent() {
		return parent;
	}

	public void setParent(TreeNode parent) {
		this.parent = parent;
	}

	public LinkedList<TreeNode> getChildrenList() {
		return childrenList;
	}

	public void setChildrenList(LinkedList<TreeNode> childrenList) {
		this.childrenList = childrenList;
	}

	public int getChildCount() {
		return childCount;
	}

	public void setChildCount(int childCount) {
		this.childCount = childCount;
	}

	public LinkedList<TreeNode> getUnVisitedList() {
		return unVisitedList;
	}

	public void setUnVisitedList(TreeNode childNode) {
		this.unVisitedList.add(childNode);
	}

	public EventConstraint getEventConstraint() {
		return eventConstraint;
	}

	public void setEventConstraint(EventConstraint eventConstraint) {
		this.eventConstraint = eventConstraint;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + childCount;
		result = prime * result
				+ ((childrenList == null) ? 0 : childrenList.hashCode());
		result = prime * result
				+ ((eventConstraint == null) ? 0 : eventConstraint.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result
				+ ((unVisitedList == null) ? 0 : unVisitedList.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TreeNode other = (TreeNode) obj;
		if (childCount != other.childCount)
			return false;
		if (childrenList == null) {
			if (other.childrenList != null)
				return false;
		} else if (!childrenList.equals(other.childrenList))
			return false;
		if (eventConstraint == null) {
			if (other.eventConstraint != null)
				return false;
		} else if (!eventConstraint.equals(other.eventConstraint))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (unVisitedList == null) {
			if (other.unVisitedList != null)
				return false;
		} else if (!unVisitedList.equals(other.unVisitedList))
			return false;
		return true;
	}
}
