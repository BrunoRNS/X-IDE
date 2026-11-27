package org.xide.models;

import java.nio.file.Path;

import org.xide.utils.SqlUtil;

/**
 * Model of a source-code submission artifact: id, path to the source file, related
 * {@link Problem}, and submitting {@link User}. Provides the SQL schema for the
 * {@code code} table.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
public class Code {

	private Long id;

	private Path pathFile;

	private Problem problem;

	private User user;

	public Code(Path pathFile, Problem problem, Long id, User user) {
		this.pathFile = pathFile;
		this.problem = problem;
		this.id = id;
	}

	public static String getTableCreate() {
		return SqlUtil.readSqlToString("sql/tables/code.sql");
	}

	public Code() {
	}

	public Path getPathFile() {
		return pathFile;
	}

	public void setPathFile(Path pathFile) {
		this.pathFile = pathFile;
	}

	public Problem getProblem() {
		return problem;
	}

	public void setProblem(Problem problem) {
		this.problem = problem;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
