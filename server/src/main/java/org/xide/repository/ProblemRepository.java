package org.xide.repository;

import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.xide.models.Example;
import org.xide.models.Problem;
import org.xide.models.Soluction;
import org.xide.models.enums.BalloonColor;
import org.xide.models.enums.BigO;
import org.xide.models.enums.Lang;

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
