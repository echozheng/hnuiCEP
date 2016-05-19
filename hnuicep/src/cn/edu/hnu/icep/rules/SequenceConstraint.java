package cn.edu.hnu.icep.rules;

/**
 * 表示规则中必须原子事件 发生的时间的先后关系
 * 
 * @author hduser
 * **/
public class SequenceConstraint extends RuleConstraint {

	private final EventConstraint referenceEvent;
	private final EventConstraint followingEvent;
	private final long win;

	public SequenceConstraint(EventConstraint referenceEvent,
			EventConstraint followingEvent, long win) {
		this.referenceEvent = referenceEvent;
		this.followingEvent = followingEvent;
		this.win = win;
	}

	public SequenceConstraint(EventConstraint referenceEvent,EventConstraint followingEvent) {
		this(referenceEvent, followingEvent, 0);
	}

	public final EventConstraint getReferenceEvent() {
		return referenceEvent;
	}

	public final EventConstraint getFollowingEvent() {
		return followingEvent;
	}

	public final boolean hasWindow() {
		return (win != 0);
	}

	public final long getWin() {
		return win;
	}

	@Override
	public boolean isIdenticalTo(RuleConstraint constraint) {
		// TODO Auto-generated method stub
		if (!(constraint instanceof SequenceConstraint)) {
			return false;
		}
		SequenceConstraint s = (SequenceConstraint) constraint;
		if (!referenceEvent.isIdenticalTo(s.referenceEvent)) {
			return false;
		}
		if (!followingEvent.isIdenticalTo(s.followingEvent)) {
			return false;
		}
		if (win != s.win) {
			return false;
		}
		return true;
	}

}
