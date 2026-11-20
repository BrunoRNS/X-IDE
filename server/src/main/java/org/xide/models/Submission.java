package org.xide.models;

import org.xide.models.enums.SubmissionState;

public class Submission {

	private Long id;

	private User user;

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
		return """
				CREATE TABLE IF NOT EXISTS submissions(
				    id INT NOT NULL AUTO_INCREMENT,
				    state %s ,
				    code_id INT NOT NULL,
				    id_problem INT NOT NULL,
				    CONSTRAINT uq_submission_code UNIQUE (code_id),
				    CONSTRAINT fk_submission_code FOREIGN KEY(code_id)
				    REFERENCES code(id)
				    ON DELETE CASCADE
				    ON UPDATE CASCADE,
				    CONSTRAINT fk_submission_problem FOREIGN KEY(id_problem) REFERENCES problem(id),
				    PRIMARY KEY (id)
				);
				""".formatted(SubmissionState.getTableCreate());
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

}
