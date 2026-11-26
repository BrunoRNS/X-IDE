CREATE TABLE IF NOT EXISTS problem(
    id INT NOT NULL AUTO_INCREMENT,
    title TEXT NOT NULL,
    balao TEXT,
    big_o ENUM('n1', 'logn', 'n', 'nlogn', 'ne2', 'ne3') NOT NULL,
    restriction TEXT,
    pdf TEXT NOT NULL,
    PRIMARY KEY(id)
);
