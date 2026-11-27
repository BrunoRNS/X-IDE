package org.xide.sockets.controllers.competition;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xide.states.CompetitionState;

class CompetitionSocketControllerTest {

	private final CompetitionSocketController controller = new CompetitionSocketController();

	@BeforeEach
	@SuppressWarnings("unused")
	void setUp() {
		reset();
	}

	@AfterEach
	@SuppressWarnings("unused")
	void tearDown() {
		reset();
	}

	@Test
	void timeToStartReturnsMinusOneWhenStartIsUnset() {
		assertEquals(-1L, controller.timeToStart(null));
	}

	@Test
	void timeToEndReturnsMinusOneWhenEndIsUnset() {
		assertEquals(-1L, controller.timeToEnd(null));
	}

	@Test
	void timeToStartCountsDownToAFutureStart() {
		CompetitionState.setStartDate(LocalDateTime.now().plusSeconds(120));

		long seconds = controller.timeToStart(null);

		assertTrue(seconds > 110 && seconds <= 120);
	}

	@Test
	void timeToStartReturnsZeroWhenStartIsPast() {
		CompetitionState.setStartDate(LocalDateTime.now().minusSeconds(60));

		assertEquals(0L, controller.timeToStart(null));
	}

	@Test
	void timeToEndCountsDownToAFutureEnd() {
		CompetitionState.setEndDate(LocalDateTime.now().plusSeconds(300));

		long seconds = controller.timeToEnd(null);

		assertTrue(seconds > 290 && seconds <= 300);
	}

	@Test
	void timeToEndReturnsZeroWhenEndIsPast() {
		CompetitionState.setEndDate(LocalDateTime.now().minusSeconds(60));

		assertEquals(0L, controller.timeToEnd(null));
	}

	private static void reset() {
		CompetitionState.stop();
		CompetitionState.setStartDate(null);
		CompetitionState.setEndDate(null);
	}

}