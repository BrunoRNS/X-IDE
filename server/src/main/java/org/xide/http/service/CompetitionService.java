package org.xide.http.service;

import java.time.LocalDateTime;

import org.xide.states.CompetitionState;

/**
 * Facade starting a competition by delegating to
 * {@link org.xide.states.CompetitionState}.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
public class CompetitionService {

	public static void start(LocalDateTime endDate, LocalDateTime startDate) {
		CompetitionState.start(startDate, endDate);
	}

}
