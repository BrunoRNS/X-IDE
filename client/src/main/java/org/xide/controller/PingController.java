package org.xide.controller;

import org.xide.protocol.SocketMessage;
import org.xide.sockets.annotations.WebSocketController;
import org.xide.sockets.annotations.WebSocketRoute;

/**
 * Client-side WebSocket controller answering server pings on {@code /develop/ping} with
 * {@code pong}.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
@WebSocketController
public class PingController {

	@WebSocketRoute("/develop/ping")
	public String ping(SocketMessage request) {
		System.out.println("Pong");
		return SocketMessage.PONG;
	}

}
