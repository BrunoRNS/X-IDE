package org.xide.sockets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.websocket.Session;
import org.xide.protocol.SocketMessage;

/**
 * Tracks the client sessions currently connected to the WebSocket server and
 * the last pong one of them answered with, so server-side routes can push
 * messages to clients and introspect their answers.
 */
public final class ClientSessions {
    private static final Set<Session> SESSIONS = ConcurrentHashMap.newKeySet();
    private static volatile SocketMessage lastPong;

    private ClientSessions() {
    }

    public static void register(Session session) {
        SESSIONS.add(session);
    }

    public static void unregister(Session session) {
        SESSIONS.remove(session);
    }

    public static List<Session> all() {
        return List.copyOf(SESSIONS);
    }

    public static void recordPong(SocketMessage message) {
        lastPong = message;
    }

    public static SocketMessage lastPong() {
        return lastPong;
    }
}
