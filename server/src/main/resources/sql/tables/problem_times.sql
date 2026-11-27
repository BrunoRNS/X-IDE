CREATE TABLE IF NOT EXISTS problem_times(
    id_problem INT NOT NULL,
    lang ENUM('python', 'kotlin', 'java', 'cpp', 'c', 'rust', 'go', 'groovy', 'javascript') NOT NULL,
    time_ms DOUBLE NOT NULL,
    PRIMARY KEY(id_problem, lang),
    CONSTRAINT fk_problem_times
    FOREIGN KEY (id_problem)
    REFERENCES problem(id)
    ON DELETE CASCADE
);
