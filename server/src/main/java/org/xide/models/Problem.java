package org.xide.models;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.xide.models.enums.BigO;
import org.xide.models.enums.Lang;

public class Problem {

    private String titile;
    private Path pdf;
    private List<Soluction> soluctions;
    private List<Submission> submissions;
    private List<String> restrictions;
    private BigO bigO;
    private Map<Lang, Double> times;

    public Problem(String titile, Path pdf, List<String> restrictions, BigO bigO,
            Map<Lang, Double> times) {
        this.titile = titile;
        this.pdf = pdf;
        this.soluctions = List.of();
        this.submissions = List.of();
        this.restrictions = restrictions;
        this.bigO = bigO;
        this.times = times;
    }

    public Problem() {
    }

    public static String getRestrictionTableCreate() {
        return """
                CREATE TABLE IF NOT EXISTS restriction(
                    id INT NOT NULL AUTO_INCREMENT,
                    message VARCHAR(100) NOT NULL,
                    id_problem INT NOT NULL,
                    CONSTRAINT fk_problem_restriction FOREIGN KEY (id_problem) REFERENCES problem(id),
                    PRIMARY KEY(id)
                );
                """;
    }

    public static String getProblemTableCreate() {
        return """
                CREATE TABLE IF NOT EXISTS problem(
                    id INT NOT NULL AUTO_INCREMENT,
                    titile TEXT NOT NULL,
                    big_o %s ,
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
                    CONSTRAINT fk_pt_problem FOREIGN KEY(id_problem)
                        REFERENCES problem(id) ON DELETE CASCADE
                );
                """.formatted(Lang.getTableCreate());
    }

    public String getTitile() {
        return titile;
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

    public List<String> getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(List<String> restrictions) {
        this.restrictions = restrictions;
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
}
