package org.xide.repository;

import java.sql.DriverManager;

import org.xide.utils.EnvRead;

public class Connection {

	private static String url;

	private static String user;

	private static String password;

	private static java.sql.Connection connection;

	public static java.sql.Connection connect() throws Exception {
		if (connection == null) {
			String host = System.getenv("DB_HOST");
			if (host == null || host.isBlank()) {
				host = "mysql";
			}
			String port = System.getenv("DB_PORT");
			if (port == null || port.isBlank()) {
				port = "3306";
			}
			url = "jdbc:mysql://" + host + ":" + port + "/" + EnvRead.get("DB_NAME");
			user = EnvRead.get("DB_USER");
			password = EnvRead.get("DB_PASSWORD");
			connection = DriverManager.getConnection(url, user, password);
		}
		return connection;
	}

}
