package org.xide.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
}
