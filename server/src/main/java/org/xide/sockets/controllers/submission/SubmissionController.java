package org.xide.sockets.controllers.submission;

import java.util.ArrayList;
import java.util.List;

import org.xide.models.Submission;
import org.xide.repository.SubmissionRepository;
import org.xide.sockets.annotations.WebSocketController;
import org.xide.sockets.annotations.WebSocketRoute;

import jakarta.websocket.Session;

@WebSocketController
public class SubmissionController {

	public static record Dto(Long id, String userName, String balao, String state) {
	}

	@WebSocketRoute("/submission/all")
	public List<Dto> getAll(Session session) {
		List<Dto> submissions = new ArrayList<>();
		try {
			List<Submission> datesOfSubmissions = SubmissionRepository.getSubmissionOrdemByDate();
			for (Submission s : datesOfSubmissions) {
				Dto dto = new Dto(s.getId(), s.getUser().getName(), s.getProblem().getBalao(),
						s.getState().toString().toLowerCase());
				submissions.add(dto);
			}
		}
		catch (Exception e) {
			throw new IllegalStateException("getall failed: could not check the user", e);
		}
		return submissions;
	}

}
