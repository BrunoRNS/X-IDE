CREATE TABLE IF NOT EXISTS submissions(
    id INT NOT NULL AUTO_INCREMENT,
    state ENUM('wa', 'accepted', 'presentation_error', 'time_limite_exceeded', 'memory_exceeded') NOT NULL,
    code_id INT NOT NULL,
    id_problem INT NOT NULL,
    date DATETIME,
    CONSTRAINT uq_submission_code UNIQUE (code_id),
    CONSTRAINT fk_submission_code FOREIGN KEY(code_id)
    REFERENCES code(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT fk_submission_problem FOREIGN KEY(id_problem) REFERENCES problem(id),
    PRIMARY KEY (id)
);
