package org.xide.repository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.xide.http.dto.ProblemDtos.DtoProblem;
import org.xide.models.Code;
import org.xide.models.Example;
import org.xide.models.Problem;
import org.xide.models.Soluction;
import org.xide.models.Submission;
import org.xide.models.User;
import org.xide.models.enums.BalloonColor;
import org.xide.models.enums.BigO;
import org.xide.models.enums.Lang;
import org.xide.models.enums.SubmissionState;

public class ProblemRepository {

	public static Problem insertProblem(String title, BalloonColor color, BigO bigo, Path pdf, String recommentation,
			Map<Lang, Double> times, List<Example> examples, List<Soluction> soluctions) throws Exception {
		java.sql.Connection conn = Connection.connect();
		boolean autoCommit = conn.getAutoCommit();
		conn.setAutoCommit(false);

		try {
			Long id = insertProblem(conn, title, color, bigo, pdf, recommentation);
			Problem problem = new Problem(id, examples, title, pdf, soluctions, List.of(), color, recommentation, bigo,
					times);

			for (Example example : examples) {
				example.setProblem(problem);
			}

			insertTimes(conn, times, id);
			insertSoluctions(conn, soluctions, id);
			insertExamples(conn, examples);

			conn.commit();
			return problem;
		}
		catch (Exception e) {
			conn.rollback();
			throw e;
		}
		finally {
			conn.setAutoCommit(autoCommit);
		}
	}

	public static void deleteProblemByid(long id) throws Exception {
		String sql = """
				DELETE FROM problem WHERE id = ?
				""";
		java.sql.Connection conn = Connection.connect();
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setLong(1, id);
		ps.execute();
	}

	private static Long insertProblem(java.sql.Connection conn, String title, BalloonColor color, BigO bigo, Path pdf,
			String recommentation) throws Exception {
		String sql = """
				INSERT INTO problem(titile, balao, big_o, restriction, pdf)
				VALUES(?,?,?,?,?)
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, title);
			ps.setString(2, color.cssColor());
			ps.setString(3, bigo.toString());
			ps.setString(4, recommentation);
			ps.setString(5, pdf.toString());
			ps.executeUpdate();

			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) {
					return rs.getLong(1);
				}
			}
		}

		throw new IllegalStateException("The database did not return the problem id.");
	}

	public static List<DtoProblem> findAllProblem() throws Exception {
		String sql = """
				SELECT id, titile, balao, big_o FROM problem;
				""";
		java.sql.Connection conn = Connection.connect();
		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		List<DtoProblem> all = new ArrayList<>();
		while (rs.next()) {
			String title = rs.getString("titile");
			BalloonColor balloonColor = BalloonColor.fromDatabase(rs.getString("balao"));
			BigO bigO = BigO.fromDatabase(rs.getString("big_o"));
			all.add(new DtoProblem(rs.getLong("id"), title, balloonColor.cssColor(), bigO));
		}
		return all;
	}

	public static boolean updateProblem(Long id, String title, BalloonColor color, BigO bigo, Path pdf,
			String recommentation, Map<Lang, Double> times, List<Example> examples, List<Soluction> soluctions)
			throws Exception {
		java.sql.Connection conn = Connection.connect();
		boolean autoCommit = conn.getAutoCommit();
		conn.setAutoCommit(false);
		try {
			if (!updateProblemRecord(conn, id, title, color, bigo, recommentation, pdf)) {
				conn.rollback();
				return false;
			}
			replaceProblemChildren(conn, id, times, examples, soluctions);
			conn.commit();
			return true;
		}
		catch (Exception e) {
			conn.rollback();
			throw e;
		}
		finally {
			conn.setAutoCommit(autoCommit);
		}
	}

	private static boolean updateProblemRecord(java.sql.Connection conn, Long id, String title, BalloonColor color,
			BigO bigo, String recommentation, Path pdf) throws Exception {
		String sql = pdf == null ? "UPDATE problem SET titile=?, balao=?, big_o=?, restriction=? WHERE id=?"
				: "UPDATE problem SET titile=?, balao=?, big_o=?, restriction=?, pdf=? WHERE id=?";
		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setString(1, title);
			statement.setString(2, color.cssColor());
			statement.setString(3, bigo.toString());
			statement.setString(4, recommentation);
			if (pdf == null) {
				statement.setLong(5, id);
			}
			else {
				statement.setString(5, pdf.toString());
				statement.setLong(6, id);
			}
			return statement.executeUpdate() > 0;
		}
	}

	private static void replaceProblemChildren(java.sql.Connection conn, Long id, Map<Lang, Double> times,
			List<Example> examples, List<Soluction> soluctions) throws Exception {
		deleteByProblemId(conn, "problem_times", id);
		deleteByProblemId(conn, "soluctions", id);
		deleteByProblemId(conn, "example", id);

		insertTimes(conn, times, id);
		insertSoluctions(conn, soluctions, id);
		Problem problem = new Problem();
		problem.setId(id);
		for (Example example : examples) {
			example.setProblem(problem);
		}
		insertExamples(conn, examples);
	}

	private static void deleteByProblemId(java.sql.Connection conn, String table, Long id) throws Exception {
		try (PreparedStatement statement = conn.prepareStatement("DELETE FROM " + table + " WHERE id_problem=?")) {
			statement.setLong(1, id);
			statement.executeUpdate();
		}
	}

	private static void insertTimes(java.sql.Connection conn, Map<Lang, Double> times, Long id) throws Exception {
		String sql = """
				INSERT INTO problem_times(id_problem, lang, time_ms) VALUES(?,?,?)
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			for (Map.Entry<Lang, Double> entry : times.entrySet()) {
				ps.setLong(1, id);
				ps.setString(2, entry.getKey().toString());
				ps.setDouble(3, entry.getValue());
				ps.addBatch();
			}
			ps.executeBatch();
		}
	}

	public static Problem selectProblemById(Long id) throws Exception {
		java.sql.Connection conn = Connection.connect();
		Problem problem = selectProblem(conn, id);
		if (problem == null) {
			return null;
		}

		problem.setSoluctions(selectSoluctions(conn, id));
		problem.setExamples(selectExamples(conn, id, problem));
		problem.setSubmissions(selectSubmissions(conn, id, problem));
		problem.setTimes(selectTimes(conn, id));

		return problem;
	}

	private static Problem selectProblem(java.sql.Connection conn, Long id) throws Exception {
		String sql = """
				SELECT id, titile, balao, big_o, restriction, pdf
				FROM problem
				WHERE id = ?
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return null;
				}

				Problem problem = new Problem();
				problem.setId(rs.getLong("id"));
				problem.setTitile(rs.getString("titile"));
				problem.setBalao(rs.getString("balao"));
				problem.setBigO(BigO.fromDatabase(rs.getString("big_o")));
				problem.setRestriction(rs.getString("restriction"));

				String pdf = rs.getString("pdf");
				problem.setPdf(pdf == null ? null : Paths.get(pdf));

				return problem;
			}
		}
	}

	private static List<Soluction> selectSoluctions(java.sql.Connection conn, Long id) throws Exception {
		String sql = "SELECT id, input, output FROM soluctions WHERE id_problem = ? ORDER BY id";

		List<Soluction> soluctions = new ArrayList<>();
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					soluctions.add(new Soluction(rs.getString("input"), rs.getString("output"), rs.getLong("id")));
				}
			}
		}
		return soluctions;
	}

	private static List<Example> selectExamples(java.sql.Connection conn, Long id, Problem problem) throws Exception {
		String sql = "SELECT id, code, lang FROM example WHERE id_problem = ? ORDER BY id";

		List<Example> examples = new ArrayList<>();
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Example example = new Example(rs.getLong("id"), rs.getString("code"),
							Lang.fromDatabase(rs.getString("lang")));
					example.setProblem(problem);
					examples.add(example);
				}
			}
		}
		return examples;
	}

	private static List<Submission> selectSubmissions(java.sql.Connection conn, Long id, Problem problem)
			throws Exception {
		String sql = """
				SELECT
				    s.id AS submission_id,
				    s.state AS submission_state,
				    s.date AS submission_date,
				    u.id AS user_id,
				    u.name AS user_name,
				    u.email AS user_email,
				    c.id AS code_id,
				    c.file AS code_file
				FROM submissions s
				JOIN code c ON c.id = s.code_id
				JOIN users u ON u.id = c.id_user
				WHERE s.id_problem = ?
				ORDER BY s.date DESC
				""";

		List<Submission> submissions = new ArrayList<>();
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					User user = new User(rs.getString("user_name"), null, rs.getString("user_email"),
							rs.getLong("user_id"));
					Code code = new Code(Paths.get(rs.getString("code_file")), problem, rs.getLong("code_id"), user);
					code.setUser(user);

					Submission submission = new Submission(user, code, problem,
							SubmissionState.valueOf(rs.getString("submission_state").toUpperCase(Locale.ROOT)),
							rs.getLong("submission_id"));
					Timestamp date = rs.getTimestamp("submission_date");
					if (date != null) {
						submission.setDate(date.toLocalDateTime());
					}
					submissions.add(submission);
				}
			}
		}
		return submissions;
	}

	private static Map<Lang, Double> selectTimes(java.sql.Connection conn, Long id) throws Exception {
		String sql = "SELECT lang, time_ms FROM problem_times WHERE id_problem = ?";

		Map<Lang, Double> times = new EnumMap<>(Lang.class);
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					times.put(Lang.fromDatabase(rs.getString("lang")), rs.getDouble("time_ms"));
				}
			}
		}
		return times;
	}

	private static void insertSoluctions(java.sql.Connection conn, List<Soluction> soluctions, Long id)
			throws Exception {
		String sql = """
				INSERT INTO soluctions(input, output, id_problem) VALUES (?,?,?)
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			for (Soluction soluction : soluctions) {
				ps.setString(1, soluction.getIn());
				ps.setString(2, soluction.getOut());
				ps.setLong(3, id);
				ps.addBatch();
			}
			ps.executeBatch();
		}
	}

	private static void insertExamples(java.sql.Connection conn, List<Example> examples) throws Exception {
		String sql = """
				INSERT INTO example(code, id_problem, lang) VALUES (?,?,?)
				""";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			for (Example example : examples) {
				ps.setString(1, example.getCode());
				ps.setLong(2, example.getProblem().getId());
				ps.setString(3, example.getLang().toString());
				ps.addBatch();
			}
			ps.executeBatch();
		}
	}

}
