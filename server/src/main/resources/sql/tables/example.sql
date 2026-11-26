CREATE TABLE IF NOT EXISTS example(
    id INT NOT NULL AUTO_INCREMENT,
    code TEXT NOT NULL,
    id_problem INT NOT NULL,
    lang ENUM('python', 'kotlin', 'java', 'cpp', 'c', 'rust', 'go', 'groovy', 'javascript') NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_problem_example
        FOREIGN KEY (id_problem)
        REFERENCES problem(id)
        ON DELETE CASCADE
);
