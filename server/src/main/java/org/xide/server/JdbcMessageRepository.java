package org.xide.server;
import java.sql.DriverManager;
public final class JdbcMessageRepository implements MessageRepository {
    private final String url, user, password;
    public JdbcMessageRepository(String url, String user, String password) { this.url=url; this.user=user; this.password=password; }
    @Override public void save(String id, String payload) throws Exception {
        try (var c=DriverManager.getConnection(url,user,password);
             var p=c.prepareStatement("INSERT INTO received_messages(request_id,payload) VALUES (?,?)")) {
            p.setString(1,id); p.setString(2,payload); p.executeUpdate();
        }
    }
}
