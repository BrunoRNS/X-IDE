package org.xide.repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.xide.models.Code;
import org.xide.models.Example;
import org.xide.models.Problem;
import org.xide.models.Soluction;
import org.xide.models.Submission;
import org.xide.models.User;

/**
 * Creates database tables from bundled SQL scripts if absent and migrates older schemas
 * (e.g. renames legacy {@code titile} column, adds {@code restriction}). SQL loaded via
 * {@link org.xide.utils.SqlUtil}.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
public class InitDatabase {

	private static Connection databaseConection;

	public InitDatabase(Connection c) {
		InitDatabase.databaseConection = c;
	}

	public void init() throws Exception {
		try (Statement smt = databaseConection.createStatement()) {
			smt.execute(User.getTableCreate());
			smt.execute(Problem.getProblemTableCreate());
			addMissingProblemColumns(smt);
			smt.execute(Problem.getProblemTimesTableCreate());
			smt.execute(Example.getTableCreate());
			smt.execute(Soluction.getTableCreate());
			smt.execute(Code.getTableCreate());
			smt.execute(Submission.getTableCreate());
			smt.execute(Submission.getTriggerInsert());
		}

		System.out.println("Tables create!");
	}

	/**
	 * Keeps databases created with older schemas in sync: the title column was once
	 * created as `titile`, and `restriction` was added later.
	 */
	private void addMissingProblemColumns(Statement smt) throws Exception {
		if (!hasColumn(smt, "problem", "title")) {
			if (hasColumn(smt, "problem", "titile")) {
				smt.execute("ALTER TABLE problem CHANGE COLUMN titile title TEXT NOT NULL");
			}
			else {
				smt.execute("ALTER TABLE problem ADD COLUMN title TEXT NOT NULL");
			}
		}
		if (!hasColumn(smt, "problem", "restriction")) {
			smt.execute("ALTER TABLE problem ADD COLUMN restriction TEXT");
		}
	}

	private boolean hasColumn(Statement smt, String table, String column) throws Exception {
		try (ResultSet columns = smt.executeQuery("SHOW COLUMNS FROM " + table + " LIKE '" + column + "'")) {
			return columns.next();
		}
	}

}
