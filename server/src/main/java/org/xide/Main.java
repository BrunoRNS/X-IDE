package org.xide;

import java.util.concurrent.CountDownLatch;

import org.xide.http.routers.Routers;
import org.xide.repository.Connection;
import org.xide.repository.InitDatabase;
import org.xide.sockets.WebSocketServer;

import io.javalin.Javalin;

public class Main {
    public static void main(String[] args)
            throws Exception {
        WebSocketServer server = WebSocketServer.getInstance();
        Javalin.create(config -> {
            config.staticFiles.add("/public");
            Routers.register(config);
        }).start(8081);
        server.start();
        try {
            java.sql.Connection c =  Connection.connect();
            InitDatabase initDatabase = new InitDatabase(c);
            initDatabase.init();
        } catch (NullPointerException e) {
            System.out.println("DB_NAME or DB_PASSWORD or DB_USER is invalid or not exist");
            return ;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop, "websocket-shutdown"));
        new CountDownLatch(1).await();
    }
}