package org.xide.client.sockets;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import org.xide.protocol.SocketMessage;
import org.xide.protocol.SocketMessageCodec;
import org.xide.sockets.annotations.WebSocketRouter;

/**
 * Client-side Jakarta WebSocket endpoint. Decodes incoming server frames, forwards them
 * to a sink, dispatches known routes through the shared
 * {@link org.xide.sockets.annotations.WebSocketRouter}, and synchronously sends handler
 * responses back to the server.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
@ClientEndpoint
public class WebSocketEndpoint {

	private static final Logger LOGGER = Logger.getLogger(WebSocketEndpoint.class.getName());

	private final WebSocketRouter router;

	private final Consumer<SocketMessage> messageSink;

	private volatile Session session;

	public WebSocketEndpoint(WebSocketRouter router) {
		this(router, message -> {
		});
	}

	public WebSocketEndpoint(WebSocketRouter router, Consumer<SocketMessage> messageSink) {
		this.router = router;
		this.messageSink = messageSink;
	}

	@OnOpen
	public void onOpen(Session session) {
		this.session = session;
	}

	@OnMessage
	public void onMessage(String json) {
		try {
			SocketMessage message = SocketMessageCodec.decode(json);
			messageSink.accept(message);
			// Server responses to our requests reach the client controllers
			// just like pushes do; unknown routes are only recorded for receive().
			if (!router.hasRoute(message.route())) {
				return;
			}
			Optional<SocketMessage> response = router.dispatch(message);
			response.ifPresent(this::send);
		}
		catch (Exception e) {
			LOGGER.log(Level.WARNING, "Invalid WebSocket message from server", e);
		}
	}

	private void send(SocketMessage message) {
		Session active = session;
		if (active != null && active.isOpen()) {
			try {
				// Synchronous send so a reply is always flushed before the
				// next inbound frame is processed (or before the client exits).
				active.getBasicRemote().sendText(SocketMessageCodec.encode(message));
			}
			catch (Exception e) {
				LOGGER.log(Level.WARNING, "Failed to send WebSocket response", e);
			}
		}
	}

}
