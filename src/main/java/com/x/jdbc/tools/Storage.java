package com.x.jdbc.tools;

import com.x.jdbc.Updatable;

public abstract class Storage implements Updatable {
	private volatile int op = 0;

	public void onOption(Option op) {
		this.op |= op.code;
	}

	public boolean isInsert() {
		return Option.INSERT.match(this.op);
	}

	public boolean isDelete() {
		return Option.DELETE.match(this.op);
	}

	public boolean updatable() {
		return this.op > 0;
	}

	public boolean updatable(int option) {
		return (this.op & option) > 0;
	}

	public int update(int option) {
		int ret = this.op & option;
		this.op ^= ret;
		return ret;
	}

	public void commit(int op) {
		this.op = op;
	}

	public void cancel(int op, int cause) {
		if (Option.INSERT.match(op) && cause == 1062) {
			op &= 65535 ^ Option.INSERT.code;
		}
		this.op |= op;
	}

	public static enum Option {
		NONE(0), INSERT(1), UPDATE(2), DELETE(4);

		public final int code;

		private Option(int code) {
			this.code = code;
		}

		public boolean match(int op) {
			return (this.code & op) != 0;
		}
	}

}