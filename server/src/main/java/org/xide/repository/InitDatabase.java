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
			smt.execute(Example.createTable());
			smt.execute(Soluction.getTableCreate());
			smt.execute(Code.getTableCreate());
			smt.execute(Submission.getTableCreate());
			smt.execute(Submission.getTriggerInsert());
		}

		System.out.println("Tables create!");
	}

	/** Keeps databases created before the restriction column was added in sync. */
	private void addMissingProblemColumns(Statement smt) throws Exception {
		boolean hasRestriction;
		try (ResultSet columns = smt.executeQuery("SHOW COLUMNS FROM problem LIKE 'restriction'")) {
			hasRestriction = columns.next();
		}
		if (!hasRestriction) {
			smt.execute("ALTER TABLE problem ADD COLUMN restriction TEXT");
		}
	}

}
