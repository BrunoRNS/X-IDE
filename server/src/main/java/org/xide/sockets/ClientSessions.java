package org.xide.sockets;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.websocket.Session;
import org.xide.models.User;
import org.xide.protocol.SocketMessage;

/**
 * Tracks the client sessions currently connected to the WebSocket server and the last
 * pong one of them answered with, so server-side routes can push messages to clients and
 * introspect their answers.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
public final class ClientSessions {

	private static final Set<Session> SESSIONS = ConcurrentHashMap.newKeySet();

	private static final Map<Session, User> SESSIONSLOGIN = new ConcurrentHashMap<>();

	private static volatile SocketMessage lastPong;

	private ClientSessions() {
	}

	public static void register(Session session) {
		SESSIONS.add(session);
	}

	public static void registerLogin(User t, Session session) {
		SESSIONSLOGIN.put(session, t);
	}

	public static void unregisterLogin(Session session) {
		SESSIONSLOGIN.remove(session);
	}

	public static void unregister(Session session) {
		SESSIONS.remove(session);
		unregisterLogin(session);
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

	public static boolean isLogged(Session session) {
		return SESSIONSLOGIN.containsKey(session);
	}

	public static User getUser(Session s) {
		return SESSIONSLOGIN.get(s);
	}

}
