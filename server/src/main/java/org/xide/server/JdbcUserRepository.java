package org.xide.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** MySQL-backed account lookup and bootstrap for the single shared default account. */
public final class JdbcUserRepository {
    private final String jdbcUrl, dbUser, dbPassword;
    public JdbcUserRepository(String jdbcUrl, String dbUser, String dbPassword) {
        this.jdbcUrl = jdbcUrl; this.dbUser = dbUser; this.dbPassword = dbPassword;
    }
    private Connection connection() throws SQLException { return DriverManager.getConnection(jdbcUrl, dbUser, dbPassword); }

    public void ensureDefaultUser(String username, char[] password) throws Exception {
        if (username == null || !username.matches("[A-Za-z0-9_.-]{3,64}"))
            throw new IllegalArgumentException("Default username must be 3-64 characters: letters, digits, dot, underscore or hyphen");
        try (Connection c = connection()) {
            try (var q = c.prepareStatement("SELECT password_salt,password_hash FROM xide_users WHERE username=?")) {
                q.setString(1, username);
                try (var rs = q.executeQuery()) {
                    if (rs.next()) {
                        if (!PasswordHasher.verify(password, rs.getString(1), rs.getString(2)))
                            throw new SecurityException("The supplied password does not match the existing MySQL account '" + username + "'. Use its current password or change it deliberately in MySQL.");
                        System.out.println("Validated existing default account in MySQL: " + username);
                        return;
                    }
                }
            }
            var encoded = PasswordHasher.hash(password);
            try (var insert = c.prepareStatement("INSERT INTO xide_users(username,password_salt,password_hash) VALUES (?,?,?)")) {
                insert.setString(1, username); insert.setString(2, encoded.salt()); insert.setString(3, encoded.hash()); insert.executeUpdate();
            }
            System.out.println("Created default account in MySQL: " + username);
        }
    }

    public boolean authenticate(String username, char[] password) {
        if (username == null || password == null) return false;
        try (Connection c = connection(); var q = c.prepareStatement("SELECT password_salt,password_hash FROM xide_users WHERE username=?")) {
            q.setString(1, username);
            try (var rs = q.executeQuery()) {
                return rs.next() && PasswordHasher.verify(password, rs.getString(1), rs.getString(2));
            }
        } catch (SQLException e) { System.err.println("MySQL authentication lookup failed: " + e.getMessage()); return false; }
    }
}
