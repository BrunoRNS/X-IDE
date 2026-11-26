package org.xide.models;

import org.xide.utils.SqlUtil;

import jakarta.websocket.Session;

public class User {

	private Long id;

	private String name;

	private String password;

	private String email;

	private Session session;

	public User(String name, String password, String email, Session session) {
		this.name = name;
		this.password = password;
		this.email = email;
		this.session = session;
	}

	public User(String name, String password, String email, Long id) {
		this.name = name;
		this.password = password;
		this.email = email;
		this.id = id;
	}

	public User() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Session getSession() {
		return session;
	}

	public static String getTableCreate() {
		return SqlUtil.readSqlToString("sql/tables/users.sql");
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setSession(Session session) {
		this.session = session;
	}

}
