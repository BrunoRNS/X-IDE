package org.xide.repository;

import java.sql.Connection;
import java.sql.Statement;
import org.xide.models.*;
import org.xide.models.User;

public class InitDatabase {

	private static Connection databaseConection;

	public InitDatabase(Connection c) {
		InitDatabase.databaseConection = c;
	}

	public void init() throws Exception {
		Statement smt = databaseConection.createStatement();
		smt.execute(User.getTableCreate());
		smt.execute(Problem.getProblemTableCreate());
		smt.execute(Problem.getRestrictionTableCreate());
		smt.execute(Problem.getProblemTimesTableCreate());
		smt.execute(Soluction.getTableCreate());
		smt.execute(Code.getTableCreate());
		smt.execute(Submission.getTableCreate());
		smt.execute(Submission.getTriggerInsert());

		System.out.println("Tables create!");
	}

}
