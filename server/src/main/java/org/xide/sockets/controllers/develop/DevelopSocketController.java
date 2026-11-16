package org.xide.sockets.controllers.develop;

import jakarta.websocket.Session;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xide.protocol.SocketMessage;
import org.xide.protocol.SocketMessageCodec;
import org.xide.sockets.ClientSessions;
import org.xide.sockets.annotations.WebSocketController;
import org.xide.sockets.annotations.WebSocketRoute;

@WebSocketController
public final class DevelopSocketController {

	private static final Logger LOGGER = Logger.getLogger(DevelopSocketController.class.getName());

	@WebSocketRoute("/develop/ping")
	public String ping(SocketMessage request) {
		System.out.println("Pong");
		return SocketMessage.PONG;
	}

	/**
	 * Pushes a ping to every connected client; each one answers with a pong that the
	 * server records and {@link #lastPong(SocketMessage)} can read.
	 */
	@WebSocketRoute("/develop/ping-clients")
	public String pingClients(SocketMessage request) {
		for (Session session : ClientSessions.all()) {
			send(session, SocketMessage.text("/develop/ping", SocketMessage.PING));
		}
		return "pinged";
	}

	/** Returns the payload of the last pong a client sent back ("none" before that). */
	@WebSocketRoute("/develop/last-pong")
	public String lastPong(SocketMessage request) {
		SocketMessage last = ClientSessions.lastPong();
		return last == null ? "none" : last.payload().asText();
	}

	private static void send(Session session, SocketMessage message) {
		try {
			// Synchronous push so it reaches the client before the reply to
			// the request that triggered it.
			session.getBasicRemote().sendText(SocketMessageCodec.encode(message));
		}
		catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Failed to push WebSocket message", e);
		}
	}

}
