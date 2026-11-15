package org.xide.models;

public class Soluction {
    private Long id;
    private String in;
    private String out;

    public Soluction() {}

    public Soluction(String in, String out, Long id) {
        this.in = in;
        this.out = out;
        this.id = id;
    }

    public static String getTableCreate() {
        return """
                CREATE TABLE IF NOT EXISTS soluctions(
                    id INT NOT NULL AUTO_INCREMENT,
                    input TEXT NOT NULL,
                    output TEXT NOT NULL,
                    id_problem INT NOT NULL,
                    CONSTRAINT fk_problem_soluction FOREIGN KEY (id_problem) REFERENCES problem(id),
                    primary key(id)
                );
                """;
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
