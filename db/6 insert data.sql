INSERT INTO yes_no (yn_code) VALUES ('Y');
INSERT INTO yes_no (yn_code) VALUES ('N');

INSERT INTO change_type (change_type) VALUES ('ADD');
INSERT INTO change_type (change_type) VALUES ('REMOVE');

INSERT INTO change_result (result_code, label, retry) VALUES ('SUCCESS', 'Success', '0');
INSERT INTO change_result (result_code, label, retry) VALUES ('ALREADY_REMOVED', 'Student has already been removed from the group', '0');
INSERT INTO change_result (result_code, label, retry) VALUES ('ALREADY_IN_GROUP', 'Student has already been added to the group', '0');
INSERT INTO change_result (result_code, label, retry) VALUES ('COURSE_MISSING', 'Course does not exist', '1');
INSERT INTO change_result (result_code, label, retry) VALUES ('GROUP_MISSING', 'Group does not exist', '1');
INSERT INTO change_result (result_code, label, retry) VALUES ('STUDENT_MISSING', 'Student does not exist', '1');
INSERT INTO change_result (result_code, label, retry) VALUES ('CANNOT_REMOVE_SAFELY', 'Cannot safely remove student from group as it appears to be in use already.', '1');
INSERT INTO change_result (result_code, label, retry) VALUES ('NOT_ON_COURSE', 'Student is not on the course', '1');
INSERT INTO change_result (result_code, label, retry) VALUES ('UNAVAILABLE', 'Course is not available in Learn', '1');

INSERT INTO run_result (result_code, result_label)
  VALUES ('SUCCESS', 'Synchronisation completed successfully.');
INSERT INTO run_result (result_code, result_label)
  VALUES ('FATAL', 'Synchronisation failed due to an unrecoverable error.');
INSERT INTO run_result (result_code, result_label)
  VALUES ('TIMEOUT', 'Synchronisation timed out.');
INSERT INTO run_result (result_code, result_label)
  VALUES ('ABANDONED', 'Synchronisation abadoned due to concurrent process.');
INSERT INTO run_result (result_code, result_label)
  VALUES ('THRESHOLD_EXCEEDED', 'Synchronisation aborted as number of removal operations exceeds threshold.');

INSERT INTO CONFIGURATION (RECORD_ID, REMOVE_THRESHOLD_COUNT) VALUES ('1', '2000');