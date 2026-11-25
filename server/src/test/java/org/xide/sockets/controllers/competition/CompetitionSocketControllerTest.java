package org.xide.sockets.controllers.competition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xide.states.CompetitionState;

public class CompetitionSocketControllerTest {

	private final CompetitionSocketController controller = new CompetitionSocketController();

	@Before
	public void setUp() {
		reset();
	}

	@After
	public void tearDown() {
		reset();
	}

	private static void reset() {
		CompetitionState.stop();
		CompetitionState.setStartDate(null);
		CompetitionState.setEndDate(null);
	}

	@Test
	public void timeToStartReturnsMinusOneWhenStartIsUnset() {
		assertEquals(-1L, controller.timeToStart(null));
	}

	@Test
	public void timeToEndReturnsMinusOneWhenEndIsUnset() {
		assertEquals(-1L, controller.timeToEnd(null));
	}

	@Test
	public void timeToStartCountsDownToAFutureStart() {
		CompetitionState.setStartDate(LocalDateTime.now().plusSeconds(120));

		long seconds = controller.timeToStart(null);

		assertTrue(seconds > 110 && seconds <= 120);
	}

	@Test
	public void timeToStartReturnsZeroWhenStartIsPast() {
		CompetitionState.setStartDate(LocalDateTime.now().minusSeconds(60));

		assertEquals(0L, controller.timeToStart(null));
	}

	@Test
	public void timeToEndCountsDownToAFutureEnd() {
		CompetitionState.setEndDate(LocalDateTime.now().plusSeconds(300));

		long seconds = controller.timeToEnd(null);

		assertTrue(seconds > 290 && seconds <= 300);
	}

	@Test
	public void timeToEndReturnsZeroWhenEndIsPast() {
		CompetitionState.setEndDate(LocalDateTime.now().minusSeconds(60));

		assertEquals(0L, controller.timeToEnd(null));
	}

}