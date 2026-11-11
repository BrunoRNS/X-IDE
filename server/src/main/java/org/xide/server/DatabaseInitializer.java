package org.xide.server;

import java.sql.DriverManager;

final class DatabaseInitializer {
    private DatabaseInitializer() {}
    static void initialize(String url, String user, String password) throws Exception {
        try (var c = DriverManager.getConnection(url, user, password); var s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS received_messages (" +
                    "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, request_id VARCHAR(128) NOT NULL, " +
                    "payload TEXT, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, INDEX idx_request_id(request_id))");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS xide_users (" +
                    "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, username VARCHAR(64) NOT NULL UNIQUE, " +
                    "password_salt VARCHAR(64) NOT NULL, password_hash VARCHAR(128) NOT NULL, " +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        }
    }
}
