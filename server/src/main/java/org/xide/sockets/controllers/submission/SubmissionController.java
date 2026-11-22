package org.xide.sockets.controllers.submission;

import java.util.List;

import org.xide.models.Submission;
import org.xide.models.Problem;
import org.xide.models.User;
import org.xide.repository.SubmissionRepository;
import org.xide.sockets.annotations.WebSocketController;
import org.xide.sockets.annotations.WebSocketRoute;

import jakarta.websocket.Session;

@WebSocketController
public class SubmissionController {

	public static record ProblemDto(Long id, String title, String balloonColor) {
	}

	public static record ParticipantDto(Long id, String userName) {
	}

	public static record SubmissionDto(Long userId, Long problemId, String state, String date) {
	}

	public static record HistoryDto(Long id, String userName, String problemTitle, String state, String balloonColor) {
	}

	public static record Board(List<ProblemDto> problems, List<ParticipantDto> participants,
			List<SubmissionDto> submissions) {
	}

	@WebSocketRoute("/submission/all")
	public List<HistoryDto> getAll(Session session) {
		try {
			return SubmissionRepository.getSubmissionOrdemByDate()
				.stream()
				.map(s -> new HistoryDto(s.getId(), s.getUser().getName(), s.getProblem().getTitile(),
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
				.map(p -> new ProblemDto(p.getId(), p.getTitile(), p.getBalao().cssColor()))
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
