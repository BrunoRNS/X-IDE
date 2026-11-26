package org.xide.states;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.xide.models.Problem;

public class CompetitionState {

	private final static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	@SuppressWarnings("unused")
	private static ScheduledFuture<?> endTesk;

	@SuppressWarnings("unused")
	private static ScheduledFuture<?> startTesk;

	private static List<Problem> problems;

	private static boolean itStarted;

	private static LocalDateTime startDate;

	private static LocalDateTime endDate;

	public static void start(LocalDateTime startDate, LocalDateTime endDate) {

		CompetitionState.startDate = startDate;
		CompetitionState.endDate = endDate;

		LocalDateTime now = LocalDateTime.now();

		if (now.isAfter(endDate)) {
			itStarted = false;
			return;
		}

		Duration duractionStart = Duration.between(now, startDate);
		long delayStart = duractionStart.toMillis();

		if (delayStart < 0) {
			delayStart = 0;
		}

		startTesk = scheduler.schedule(() -> {
			itStarted = true;
		}, delayStart, TimeUnit.MILLISECONDS);
		endSchedule(endDate);
	}

	public static synchronized void stopStart() {
		if (CompetitionState.startTesk != null) {
			startTesk.cancel(false);
			startTesk = null;
		}
	}

	public static synchronized void stopEnd() {
		if (endTesk != null) {
			endTesk.cancel(false);
			endTesk = null;
		}
	}

	public static synchronized void stop() {
		stopStart();
		stopEnd();
		problems = List.of();
		itStarted = false;
	}

	private static void endSchedule(LocalDateTime endDate) {
		LocalDateTime now = LocalDateTime.now();

		Duration duractionStart = Duration.between(now, endDate);
		long delayStart = duractionStart.toMillis();

		if (delayStart < 0) {
			delayStart = 0;
		}

		endTesk = scheduler.schedule(() -> {
			itStarted = false;
		}, delayStart, TimeUnit.MILLISECONDS);
	}

	public static List<Problem> getProblems() {
		return problems;
	}

	public static void setProblems(List<Problem> problems) {
		CompetitionState.problems = problems;
	}

	public static boolean isItStarted() {
		return itStarted;
	}

	public static void setItStarted(boolean itStarted) {
		CompetitionState.itStarted = itStarted;
	}

	public static LocalDateTime getStartDate() {
		return startDate;
	}

	public static void setStartDate(LocalDateTime startDate) {
		CompetitionState.startDate = startDate;
	}

	public static LocalDateTime getEndDate() {
		return endDate;
	}

	public static void setEndDate(LocalDateTime endDate) {
		CompetitionState.endDate = endDate;
	}

}
