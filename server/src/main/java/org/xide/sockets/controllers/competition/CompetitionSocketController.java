package org.xide.sockets.controllers.competition;

import java.time.Duration;
import java.time.LocalDateTime;

import jakarta.websocket.Session;
import org.xide.sockets.annotations.WebSocketController;
import org.xide.sockets.annotations.WebSocketRoute;
import org.xide.states.CompetitionState;

@WebSocketController
public class CompetitionSocketController {

	@WebSocketRoute("/competition/time-to-start")
	public long timeToStart(Session session) {
		return secondsUntil(CompetitionState.getStartDate());
	}

	@WebSocketRoute("/competition/time-to-end")
	public long timeToEnd(Session session) {
		return secondsUntil(CompetitionState.getEndDate());
	}

	private static long secondsUntil(LocalDateTime target) {
		if (target == null) {
			return -1;
		}
		return Math.max(0, Duration.between(LocalDateTime.now(), target).toSeconds());
	}

}