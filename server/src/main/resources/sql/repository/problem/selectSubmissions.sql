SELECT
    s.id AS submission_id,
    s.state AS submission_state,
    s.date AS submission_date,
    u.id AS user_id,
    u.name AS user_name,
    u.email AS user_email,
    c.id AS code_id,
    c.file AS code_file
FROM submissions s
JOIN code c ON c.id = s.code_id
JOIN users u ON u.id = c.id_user
WHERE s.id_problem = ?
ORDER BY s.date DESC