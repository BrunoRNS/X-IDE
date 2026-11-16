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
		String sql = "SELECT id, name, email FROM users ORDER BY name";
		java.sql.Connection conn = Connection.connect();
		PreparedStatement ps = conn.prepareStatement(sql);
		try (ResultSet rs = ps.executeQuery()) {
			List<User> users = new ArrayList<>();
			while (rs.next()) {
				User user = new User();
				user.setId(rs.getLong("id"));
				user.setName(rs.getString("name"));
				user.setEmail(rs.getString("email"));
				users.add(user);
			}
			return users;
		}
	}

}
