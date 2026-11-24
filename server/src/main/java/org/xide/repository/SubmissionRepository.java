package org.xide.repository;

import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.xide.models.Code;
import org.xide.models.Problem;
import org.xide.models.Submission;
import org.xide.models.User;
import org.xide.models.enums.SubmissionState;

public class SubmissionRepository {

	public static List<Problem> getProblems() throws Exception {
		String sql = "SELECT id, title, balao FROM problem ORDER BY id";
		java.sql.Connection conn = Connection.connect();
		try (PreparedStatement statement = conn.prepareStatement(sql); ResultSet result = statement.executeQuery()) {
			List<Problem> problems = new ArrayList<>();
			while (result.next()) {
				Problem problem = new Problem();
				problem.setId(result.getLong("id"));
				problem.setTitle(result.getString("title"));
				problem.setBalao(result.getString("balao"));
				problems.add(problem);
			}
			return problems;
		}
	}

	public static List<User> getParticipants() throws Exception {
		String sql = "SELECT id, name, email FROM users ORDER BY id";
		java.sql.Connection conn = Connection.connect();
		try (PreparedStatement statement = conn.prepareStatement(sql); ResultSet result = statement.executeQuery()) {
			List<User> users = new ArrayList<>();
			while (result.next()) {
				users.add(new User(result.getString("name"), null, result.getString("email"), result.getLong("id")));
			}
			return users;
		}
	}

	public static List<Submission> getSubmissionOrdemByDate() throws Exception {
		String sql = """
				SELECT
				    s.id AS submission_id,
				    s.state AS submission_state,
				    s.date AS submission_date,
				    u.id AS user_id,
				    u.name AS user_name,
				    u.email AS user_email,
				    c.id AS code_id,
				    c.file AS code_file,
				    p.id AS problem_id,
				    p.title AS problem_title,
				    p.pdf AS problem_pdf,
					p.balao AS problem_balao
				FROM submissions s
				JOIN code c ON c.id = s.code_id
				JOIN users u ON u.id = c.id_user
				JOIN problem p ON p.id = s.id_problem
				ORDER BY s.date DESC
				""";

		java.sql.Connection conn = Connection.connect();
		try (PreparedStatement statement = conn.prepareStatement(sql); ResultSet result = statement.executeQuery()) {
			List<Submission> submissions = new ArrayList<>();
			while (result.next()) {
				submissions.add(getSubmission(result));
			}
			return submissions;
		}
	}

	private static Submission getSubmission(ResultSet result) throws SQLException {
		User user = getUser(result);
		Problem problem = getProblem(result);
		Code code = getCode(result, user, problem);

		String stateValue = result.getString("submission_state");
		SubmissionState state = SubmissionState.valueOf(stateValue.toUpperCase(Locale.ROOT));
		Submission submission = new Submission(user, code, problem, state, result.getLong("submission_id"));
		Timestamp timestamp = result.getTimestamp("submission_date");
		LocalDateTime date = timestamp == null ? null : timestamp.toLocalDateTime();
		submission.setDate(date);
		return submission;
	}

	private static User getUser(ResultSet result) throws SQLException {
		return new User(result.getString("user_name"), null, result.getString("user_email"), result.getLong("user_id"));
	}

	private static Problem getProblem(ResultSet result) throws SQLException {
		Problem problem = new Problem();
		problem.setId(result.getLong("problem_id"));
		problem.setTitle(result.getString("problem_title"));
		String pdf = result.getString("problem_pdf");
		String balao = result.getString("problem_balao");
		if (pdf != null) {
			problem.setPdf(Paths.get(pdf));
		}
		problem.setBalao(balao);
		return problem;
	}

	private static Code getCode(ResultSet result, User user, Problem problem) throws SQLException {
		String file = result.getString("code_file");
		Code code = new Code(Paths.get(file), problem, result.getLong("code_id"), user);
		code.setUser(user);
		return code;
	}

}
