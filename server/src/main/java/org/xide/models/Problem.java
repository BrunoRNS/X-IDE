package org.xide.models;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.xide.models.enums.BigO;
import org.xide.models.enums.BalloonColor;
import org.xide.models.enums.Lang;

public class Problem {

	private Long id;

	private List<Example> examples;

	private String titile;

	private Path pdf;

	private List<Soluction> soluctions;

	private List<Submission> submissions;

	private BalloonColor balao;

	private String restriction;

	private BigO bigO;

	private Map<Lang, Double> times;

	public Problem(Long id, List<Example> examples, String titile, Path pdf, List<Soluction> soluctions,
			List<Submission> submissions, BalloonColor balao, String restriction, BigO bigO, Map<Lang, Double> times) {
		this.id = id;
		this.examples = examples;
		this.titile = titile;
		this.pdf = pdf;
		this.soluctions = soluctions;
		this.submissions = submissions;
		this.balao = balao;
		this.restriction = restriction;
		this.bigO = bigO;
		this.times = times;
	}

	public Problem(String titile, Path pdf, String restriction, BigO bigO, Map<Lang, Double> times) {
		this.titile = titile;
		this.pdf = pdf;
		this.soluctions = List.of();
		this.submissions = List.of();
		this.restriction = restriction;
		this.bigO = bigO;
		this.times = times;
	}

	public Problem(String titile, Path pdf, String restriction, BigO bigO, Map<Lang, Double> times, String balao) {
		this.titile = titile;
		this.pdf = pdf;
		this.soluctions = List.of();
		this.submissions = List.of();
		this.restriction = restriction;
		this.bigO = bigO;
		this.balao = BalloonColor.fromDatabase(balao);
		this.times = times;
	}

	public Problem() {
	}

	public static String getProblemTableCreate() {
		return """
				CREATE TABLE IF NOT EXISTS problem(
				    id INT NOT NULL AUTO_INCREMENT,
				    titile TEXT NOT NULL,
					balao TEXT,
				    big_o %s ,
					restriction TEXT,
				    pdf TEXT NOT NULL,
				    PRIMARY KEY(id)
				);
				""".formatted(BigO.getTableCreate());
	}

	public static String getProblemTimesTableCreate() {
		return """
				CREATE TABLE IF NOT EXISTS problem_times(
				    id_problem INT NOT NULL,
				    lang %s ,
				    time_ms DOUBLE NOT NULL,
				    PRIMARY KEY(id_problem, lang),
				    CONSTRAINT fk_problem_times
					FOREIGN KEY (id_problem)
					REFERENCES problem(id)
					ON DELETE CASCADE
				);
				""".formatted(Lang.getTableCreate());
	}

	public String getTitile() {
		return titile;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setTitile(String titile) {
		this.titile = titile;
	}

	public Path getPdf() {
		return pdf;
	}

	public void setPdf(Path pdf) {
		this.pdf = pdf;
	}

	public List<Soluction> getSoluctions() {
		return soluctions;
	}

	public void setSoluctions(List<Soluction> soluctions) {
		this.soluctions = soluctions;
	}

	public String getRestriction() {
		return restriction;
	}

	public void setRestrictions(String restriction) {
		this.restriction = restriction;
	}

	public BigO getBigO() {
		return bigO;
	}

	public void setBigO(BigO bigO) {
		this.bigO = bigO;
	}

	public Map<Lang, Double> getTimes() {
		return times;
	}

	public void setTimes(Map<Lang, Double> times) {
		this.times = times;
	}

	public List<Submission> getSubmissions() {
		return submissions;
	}

	public void setSubmissions(List<Submission> submissions) {
		this.submissions = submissions;
	}

	public BalloonColor getBalao() {
		return balao;
	}

	public void setBalao(String balao) {
		this.balao = BalloonColor.fromDatabase(balao);
	}

	public List<Example> getExamples() {
		return examples;
	}

	public void setExamples(List<Example> examples) {
		this.examples = examples;
	}

	public void setBalao(BalloonColor balao) {
		this.balao = balao;
	}

	public void setRestriction(String restriction) {
		this.restriction = restriction;
	}

}
