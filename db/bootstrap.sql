INSERT INTO change_type (change_type) VALUES ('add');
INSERT INTO change_type (change_type) VALUES ('remove');

INSERT INTO change_result (result_code, label, retry) VALUES ('success', 'Success', '0');
INSERT INTO change_result (result_code, label, retry) VALUES ('course_missing', 'Course does not exist', '1');
INSERT INTO change_result (result_code, label, retry) VALUES ('not_on_course', 'Student is not on the course', '1');
INSERT INTO change_result (result_code, label, retry) VALUES ('group_missing', 'Group does not exist', '0');
INSERT INTO change_result (result_code, label, retry) VALUES ('already_removed', 'Student has already been removed from the group', '0');
INSERT INTO change_result (result_code, label, retry) VALUES ('already_in_group', 'Student has already been added to the group', '0');


INSERT INTO run_result (result_code, result_label)
  VALUES ('success', 'Synchronisation completed successfully.');
INSERT INTO run_result (result_code, result_label)
  VALUES ('fatal', 'Synchronisation failed due to an unrecoverable error.');
INSERT INTO run_result (result_code, result_label)
  VALUES ('timeout', 'Synchronisation timed out.');