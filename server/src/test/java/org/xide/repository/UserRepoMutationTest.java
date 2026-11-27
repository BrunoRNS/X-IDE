package org.xide.repository;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xide.models.User;

/**
 * Unit tests for {@link org.xide.repository.UserRepo} update/delete SQL and parameter
 * binding using a fake JDBC driver.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
public class UserRepoMutationTest {

	private static final JdbcFixture JDBC = new JdbcFixture();

	@BeforeAll
	@SuppressWarnings("unused")
	static void installFixtureDriver() throws SQLException {
		DriverManager.registerDriver(JDBC);
	}

	@Test
	void deleteByIdBindsIdAndReportsWhetherARowWasDeleted() throws Exception {
		JDBC.updatedRows = 1;

		assertTrue(UserRepo.deleteById(42L));
		assertEquals("DELETE FROM users WHERE id = ?;", JDBC.sql);
		assertEquals(42L, JDBC.parameters.get(1));

		JDBC.updatedRows = 0;
		assertFalse(UserRepo.deleteById(43L));
	}

	@Test
	void updateBindsUserFieldsInSqlOrderAndReportsWhetherARowWasUpdated() throws Exception {
		JDBC.updatedRows = 1;
		User user = new User("Ada", "secret", "ada@example.com", 7L);

		assertTrue(UserRepo.update(user));
		assertEquals("UPDATE users SET name = ?, email = ?, password = ? WHERE id = ?", JDBC.sql);
		assertEquals("Ada", JDBC.parameters.get(1));
		assertEquals("ada@example.com", JDBC.parameters.get(2));
		assertEquals("secret", JDBC.parameters.get(3));
		assertEquals(7L, JDBC.parameters.get(4));

		JDBC.updatedRows = 0;
		assertFalse(UserRepo.update(user));
	}

	/**
	 * Fake {@link java.sql.Driver} and invocation handler capturing SQL and parameters
	 * and returning canned update counts.
	 *
	 * @since v1.0.0
	 * @author LuisOtavio13
	 */
	private static final class JdbcFixture implements Driver {

		private String sql;

		private java.util.Map<Integer, Object> parameters;

		private int updatedRows;

		@Override
		public Connection connect(String url, Properties info) {
			if (!acceptsURL(url)) {
				return null;
			}
			InvocationHandler connectionHandler = (proxy, method, args) -> {
				if (method.getName().equals("prepareStatement")) {
					sql = (String) args[0];
					parameters = new java.util.HashMap<>();
					InvocationHandler statementHandler = (statement, statementMethod, statementArgs) -> {
						String name = statementMethod.getName();
						if (name.startsWith("set") && statementArgs.length == 2) {
							parameters.put((Integer) statementArgs[0], statementArgs[1]);
							return null;
						}
						if (name.equals("executeUpdate")) {
							return updatedRows;
						}
						return defaultValue(statementMethod.getReturnType());
					};
					return Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(),
							new Class<?>[] { PreparedStatement.class }, statementHandler);
				}
				return defaultValue(method.getReturnType());
			};
			return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
					new Class<?>[] { Connection.class }, connectionHandler);
		}

		@Override
		public boolean acceptsURL(String url) {
			return url != null && url.startsWith("jdbc:mysql:");
		}

		@Override
		public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
			return new DriverPropertyInfo[0];
		}

		@Override
		public int getMajorVersion() {
			return 1;
		}

		@Override
		public int getMinorVersion() {
			return 0;
		}

		@Override
		public boolean jdbcCompliant() {
			return false;
		}

		@Override
		public Logger getParentLogger() {
			return Logger.getLogger("test");
		}

	}

	private static Object defaultValue(Class<?> type) {
		if (!type.isPrimitive() || type == void.class) {
			return null;
		}
		if (type == boolean.class) {
			return false;
		}
		if (type == char.class) {
			return '\0';
		}
		return 0;
	}

}
