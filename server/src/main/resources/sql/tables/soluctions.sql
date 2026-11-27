CREATE TABLE IF NOT EXISTS soluctions(
    id INT NOT NULL AUTO_INCREMENT,
    input TEXT NOT NULL,
    output TEXT NOT NULL,
    id_problem INT NOT NULL,
    PRIMARY KEY(id),
    CONSTRAINT fk_problem_soluction
        FOREIGN KEY (id_problem)
        REFERENCES problem(id)
        ON DELETE CASCADE
);
