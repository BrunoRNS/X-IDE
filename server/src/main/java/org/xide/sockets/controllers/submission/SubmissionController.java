package org.xide.sockets.controllers.submission;

import java.util.List;

import jakarta.websocket.Session;
import org.xide.repository.SubmissionRepository;
import org.xide.sockets.annotations.WebSocketController;
import org.xide.sockets.annotations.WebSocketRoute;

/**
 * WebSocket controller for submissions. {@code /submission/all} returns full submission
 * history; {@code /submission/ranking} returns a {@link Board} with problems,
 * participants, and submissions for the scoreboard.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
@WebSocketController
public class SubmissionController {

	/**
	 * DTO of a problem on the scoreboard: id, title, balloon color.
	 *
	 * @since v1.0.0
	 * @author LuisOtavio13
	 */
	public static record ProblemDto(Long id, String title, String balloonColor) {
	}

	/**
	 * DTO of a competition participant: id, user name.
	 *
	 * @since v1.0.0
	 * @author LuisOtavio13
	 */
	public static record ParticipantDto(Long id, String userName) {
	}

	/**
	 * DTO of a submission on the scoreboard: user id, problem id, lowercase state, date.
	 *
	 * @since v1.0.0
	 * @author LuisOtavio13
	 */
	public static record SubmissionDto(Long userId, Long problemId, String state, String date) {
	}

	/**
	 * DTO of a history entry: id, user name, problem title, lowercase state, balloon
	 * color.
	 *
	 * @since v1.0.0
	 * @author LuisOtavio13
	 */
	public static record HistoryDto(Long id, String userName, String problemTitle, String state, String balloonColor) {
	}

	/**
	 * Scoreboard payload: problems, participants, and submissions.
	 *
	 * @since v1.0.0
	 * @author LuisOtavio13
	 */
	public static record Board(List<ProblemDto> problems, List<ParticipantDto> participants,
			List<SubmissionDto> submissions) {
	}

	@WebSocketRoute("/submission/all")
	public List<HistoryDto> getAll(Session session) {
		try {
			return SubmissionRepository.getSubmissionOrdemByDate()
				.stream()
				.map(s -> new HistoryDto(s.getId(), s.getUser().getName(), s.getProblem().getTitle(),
						s.getState().toString().toLowerCase(), s.getProblem().getBalao().cssColor()))
				.toList();
		}
		catch (Exception e) {
			throw new IllegalStateException("getall failed: could not check the user", e);
		}
	}

	@WebSocketRoute("/submission/ranking")
	public Board getRanking(Session session) {
		try {
			List<ProblemDto> problems = SubmissionRepository.getProblems()
				.stream()
				.map(p -> new ProblemDto(p.getId(), p.getTitle(), p.getBalao().cssColor()))
				.toList();
			List<ParticipantDto> participants = SubmissionRepository.getParticipants()
				.stream()
				.map(u -> new ParticipantDto(u.getId(), u.getName()))
				.toList();
			List<SubmissionDto> submissions = SubmissionRepository.getSubmissionOrdemByDate()
				.stream()
				.map(s -> new SubmissionDto(s.getUser().getId(), s.getProblem().getId(),
						s.getState().toString().toLowerCase(), s.getDate() == null ? null : s.getDate().toString()))
				.toList();
			return new Board(problems, participants, submissions);
		}
		catch (Exception e) {
			throw new IllegalStateException("ranking failed: could not load competition data", e);
		}
	}

}
