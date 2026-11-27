package org.xide;

import java.util.HashMap;
import java.util.Map;

/**
 * Command-line argument accepted by the X-IDE client. Each constant carries the flag
 * token used on the command line; {@link #from(String)} resolves a token back to its
 * constant.
 *
 * @since v1.0.0
 * @author BrunoRNS
 */
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
