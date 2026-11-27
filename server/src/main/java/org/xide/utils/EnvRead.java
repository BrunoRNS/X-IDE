package org.xide.utils;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Reads environment variables and {@code .env} entries via dotenv, ignoring a missing
 * {@code .env} file.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
public class EnvRead {

	private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

	public static String get(String key) {
		return dotenv.get(key);
	}

}
