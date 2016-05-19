package cn.edu.hnu.icep.common;

/**
 * 定义约束的运算符
 * @author hduser
 * **/
public enum ConstraintOp {
	EQ {
		@Override
		public String toString() {
			return "=";
		}
	},
	LT_EQ {
		@Override
		public String toString() {
			return "<=";
		}
	},
	GT_EQ {
		@Override
		public String toString() {
			return ">=";
		}
	},
	DF {
		@Override
		public String toString() {
			return "!=";
		}
	},
	ANY {
		@Override
		public String toString() {
			return "any";
		}
	}
}
