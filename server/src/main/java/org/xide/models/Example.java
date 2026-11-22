package org.xide.models;

import org.xide.models.enums.Lang;

public class Example {

	private Long id;

	private String code;

	private Lang lang;

	private Problem problem;

	public Example(Long id, String code, Lang lang, Problem problem) {
		this.id = id;
		this.code = code;
		this.lang = lang;
		this.problem = problem;
	}

	public Example(Long id, String code, Lang lang) {
		this.id = id;
		this.code = code;
		this.lang = lang;
	}

	public Example() {
	}

	public static String createTable() {
		return """
				CREATE TABLE IF NOT EXISTS example(
				    id INT NOT NULL AUTO_INCREMENT,
				    code TEXT NOT NULL,
				    id_problem INT NOT NULL,
				    lang %s ,
				    PRIMARY KEY (id),
				    CONSTRAINT fk_problem_example FOREIGN KEY (id_problem) REFERENCES problem(id) ON DELETE CASCADE
				)
				""".formatted(Lang.getTableCreate());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Lang getLang() {
		return lang;
	}

	public void setLang(Lang lang) {
		this.lang = lang;
	}

	public Problem getProblem() {
		return problem;
	}

	public void setProblem(Problem problem) {
		this.problem = problem;
	}

}
