ALTER TABLE MODULE_COURSE DROP COLUMN LEARN_COURSE_AVAILABLE;
INSERT INTO change_result (result_code, label, retry) VALUES ('UNAVAILABLE', 'Course is not available in Learn', '1');