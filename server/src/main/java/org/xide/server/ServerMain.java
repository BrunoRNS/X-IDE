package org.xide.server;

import org.glassfish.tyrus.server.Server;
import java.sql.DriverManager;
import java.util.Arrays;

public final class ServerMain {
    private ServerMain() {}
    public static void main(String[] args) throws Exception {
        String host=env("XIDE_HOST","0.0.0.0"), portText=env("XIDE_PORT","8081");
        String dbHost=requiredEnv("DB_HOST"), dbPort=env("DB_PORT","3306");
        String db=requiredEnv("DB_NAME"), dbUser=requiredEnv("DB_USER"), dbPassword=requiredEnv("DB_PASSWORD");
        String defaultUser=requiredEnv("XIDE_DEFAULT_USER");
        char[] defaultPassword=requiredEnv("XIDE_DEFAULT_PASSWORD").toCharArray();
        String url="jdbc:mysql://"+dbHost+":"+dbPort+"/"+db+"?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        boolean connected=false;
        for(int i=1;i<=30;i++) {
            try(var c=DriverManager.getConnection(url,dbUser,dbPassword)) { connected=true; break; }
            catch(Exception e) { System.err.println("Waiting for MySQL ("+i+"/30): "+e.getMessage()); Thread.sleep(2000); }
        }
        if(!connected) throw new IllegalStateException("MySQL did not become available");
        DatabaseInitializer.initialize(url,dbUser,dbPassword);
        JdbcUserRepository users=new JdbcUserRepository(url,dbUser,dbPassword);
        users.ensureDefaultUser(defaultUser,defaultPassword);
        Arrays.fill(defaultPassword,'\0');
        AuthConfigurator.setUserRepository(users);
        XideEndpoint.setRepository(new JdbcMessageRepository(url,dbUser,dbPassword));
        Server server=new Server(host,Integer.parseInt(portText),"/",null,XideEndpoint.class);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { try { server.stop(); } catch(Exception ignored) {} }));
        server.start();
        System.out.println("X-IDE WebSocket listening internally on ws://"+host+":"+portText+"/ws; public endpoint is WSS through Nginx (TLS 1.3)");
        Thread.currentThread().join();
    }
    private static String requiredEnv(String key) {
        String value=System.getenv(key);
        if(value==null||value.isBlank()) throw new IllegalStateException("Required environment variable is missing: "+key);
        return value;
    }
    private static String env(String key,String fallback) { String value=System.getenv(key); return value==null||value.isBlank()?fallback:value; }
}
