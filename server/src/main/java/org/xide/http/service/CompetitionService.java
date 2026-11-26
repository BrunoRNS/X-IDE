package org.xide.http.service;

import java.time.LocalDateTime;

import org.xide.states.CompetitionState;

public class CompetitionService {

	public static void start(LocalDateTime endDate, LocalDateTime startDate) {
		CompetitionState.start(startDate, endDate);
	}

}
