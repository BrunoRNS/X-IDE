CREATE TRIGGER IF NOT EXISTS submissions_before_insert
BEFORE INSERT ON submissions
FOR EACH ROW
SET NEW.date = NOW();
