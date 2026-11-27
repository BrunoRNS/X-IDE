package org.xide;

import java.util.HashMap;
import java.util.Map;

public enum Argument {

	ENDPOINT("--endpoint"),

	MESSAGE("--message"),

	USER("--user"),

	PASSWORD("--password"),

	VERSION("--version");

	private static final Map<String, Argument> BY_FLAG = new HashMap<>();

	static {
		for (Argument argument : values()) {
			BY_FLAG.put(argument.flag, argument);
		}
	}

	private final String flag;

	Argument(String flag) {
		this.flag = flag;
	}

	public String flag() {
		return flag;
	}

	public static Argument from(String flag) {
		return BY_FLAG.get(flag);
	}

}
