package org.xide.models;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.xide.models.enums.Lang;
import org.xide.utils.SqlUtil;

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

	public static String getTableCreate() {
		return SqlUtil.readSqlToString("sql/tables/example.sql");
	}

	public static Example createExampleBySql(String prefix, ResultSet rs) throws SQLException {
		return new Example(rs.getLong(prefix + ".id"), rs.getString(prefix + ".code"),
				Lang.fromDatabase(rs.getString(prefix + ".lang")));
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
