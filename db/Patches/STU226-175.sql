ALTER TABLE MODULE_COURSE DROP COLUMN LEARN_COURSE_AVAILABLE;
INSERT INTO change_result (result_code, label, retry) VALUES ('COURSE_UNAVAILABLE', 'Course is not available in Learn', '1');