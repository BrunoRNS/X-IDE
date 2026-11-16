package org.xide.models;

import java.nio.file.Path;

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
		return """
				CREATE TABLE IF NOT EXISTS code(
				    id INT NOT NULL AUTO_INCREMENT,
				    file TEXT NOT NULL,
				    id_user INT NOT NULL,
				    id_problem INT NOT NULL,
				    PRIMARY KEY(id),
				    CONSTRAINT fk_code_user FOREIGN KEY(id_user)
				    REFERENCES users(id)
				    ON DELETE CASCADE
				    ON UPDATE CASCADE,
				    CONSTRAINT fk_code_problem FOREIGN KEY(id_problem)
				    REFERENCES problem(id) ON DELETE CASCADE ON UPDATE CASCADE

				);
				""";
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
