package org.xide.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.xide.models.User;

public class UserRepo {

	public static User getUser(String email, String password) throws Exception {
		String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
		java.sql.Connection conn = Connection.connect();
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, email);
		ps.setString(2, password);
		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Long id = rs.getLong("id");
				String name = rs.getString("name");
				User user = new User(name, password, email, id);
				return user;
			}
		}
		return null;
	}

	public static List<User> findAll() throws Exception {
		String sql = "SELECT id, name, email, password FROM users ORDER BY id";
		java.sql.Connection conn = Connection.connect();
		PreparedStatement ps = conn.prepareStatement(sql);
		try (ResultSet rs = ps.executeQuery()) {
			List<User> users = new ArrayList<>();
			while (rs.next()) {
				User user = new User();
				user.setId(rs.getLong("id"));
				user.setName(rs.getString("name"));
				user.setEmail(rs.getString("email"));
				user.setPassword(rs.getString("password"));
				users.add(user);
			}
			return users;
		}
	}

	public static User save(String email, String password, String name) throws Exception {
		String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
		java.sql.Connection conn = Connection.connect();
		PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
		ps.setString(1, name);
		ps.setString(2, email);
		ps.setString(3, password);
		ps.executeUpdate();
		long id = -1;
		java.sql.ResultSet rs = ps.getGeneratedKeys();
		if (rs.next()) {
			id = rs.getLong(1);
		}
		System.out.println(password);
		return new User(name, password, email, id);
	}

}
