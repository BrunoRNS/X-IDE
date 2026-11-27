package org.xide.models;

import java.time.LocalDateTime;

import org.xide.models.enums.SubmissionState;
import org.xide.utils.SqlUtil;

/**
 * Model of a user submission to a problem: id, {@link User}, {@link Code},
 * {@link Problem}, {@link org.xide.models.enums.SubmissionState}, and submission date.
 * Provides the SQL schema for the {@code submissions} table and its trigger.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
public class Submission {

	private Long id;

	private User user;

	private LocalDateTime date;

	private Code code;

	private Problem problem;

	private SubmissionState state;

	public Submission(User user, Code code, Problem problem, SubmissionState state, Long id) {
		this.user = user;
		this.code = code;
		this.problem = problem;
		this.state = state;
		this.id = id;
	}

	public Submission() {
	}

	public static String getTableCreate() {
		return SqlUtil.readSqlToString("sql/tables/submissions.sql");
	}

	public static String getTriggerInsert() {
		return SqlUtil.readSqlToString("sql/tables/submissions_before_insert.sql");
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Code getCode() {
		return code;
	}

	public void setCode(Code code) {
		this.code = code;
	}

	public Problem getProblem() {
		return problem;
	}

	public void setProblem(Problem problem) {
		this.problem = problem;
	}

	public SubmissionState getState() {
		return state;
	}

	public void setState(SubmissionState state) {
		this.state = state;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

}
