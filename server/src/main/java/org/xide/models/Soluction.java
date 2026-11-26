package org.xide.models;

import org.xide.utils.SqlUtil;

public class Soluction {

	private Long id;

	private String in;

	private String out;

	public Soluction() {
	}

	public Soluction(String in, String out, Long id) {
		this.in = in;
		this.out = out;
		this.id = id;
	}

	public static String getTableCreate() {
		return SqlUtil.readSqlToString("sql/tables/soluctions.sql");
	}

	public String getIn() {
		return in;
	}

	public void setIn(String in) {
		this.in = in;
	}

	public String getOut() {
		return out;
	}

	public void setOut(String out) {
		this.out = out;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
