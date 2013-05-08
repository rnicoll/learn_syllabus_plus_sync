INSERT INTO change_type (change_type) VALUES ('add');
INSERT INTO change_type (change_type) VALUES ('remove');

INSERT INTO change_result (result_code, label, retry) VALUES ('not_on_course', 'Student is not on the course', '1');
INSERT INTO change_result (result_code, label, retry) VALUES ('already_removed', 'Student has already been removed from the group', '0');
INSERT INTO change_result (result_code, label, retry) VALUES ('already_in_group', 'Student has already been added to the group', '0');
