CREATE TABLE activity_template (
    tt_template_id VARCHAR(32) NOT NULL,
    tt_template_name VARCHAR(255) DEFAULT NULL,
    tt_user_text_5 VARCHAR(255) DEFAULT NULL,
    learn_group_set_id VARCHAR(80) DEFAULT NULL,
    PRIMARY KEY (tt_template_id)
);

CREATE TABLE activity_type (
    tt_type_id VARCHAR(32) NOT NULL,
    tt_type_name VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (tt_type_id)
);

CREATE TABLE module (
    tt_module_id VARCHAR(32) NOT NULL,
    tt_course_code VARCHAR(20) DEFAULT NULL,
    tt_module_name VARCHAR(255) DEFAULT NULL,
    tt_academic_year VARCHAR(12) DEFAULT NULL,
    cache_semester_code VARCHAR(6) DEFAULT NULL,
    cache_occurrence_code VARCHAR(6) DEFAULT NULL,
    cache_course_code VARCHAR(12) DEFAULT NULL,
    learn_academic_year VARCHAR(6) DEFAULT NULL,
    learn_course_code VARCHAR(40) DEFAULT NULL,
    webct_active CHAR(1) DEFAULT NULL,
    PRIMARY KEY (tt_module_id)
);

CREATE TABLE module_course (
    module_course_id INTEGER GENERATED BY DEFAULT AS IDENTITY,
    tt_module_id VARCHAR(40) NOT NULL,
    merged_course CHAR(1) NOT NULL,
    learn_course_code VARCHAR(40) NOT NULL,
    learn_course_id VARCHAR(40) DEFAULT NULL,
    learn_course_available CHAR(1) DEFAULT NULL,
    PRIMARY KEY (module_course_id)
);

CREATE TABLE activity (
    tt_activity_id VARCHAR(32) NOT NULL,
    tt_activity_name VARCHAR(255) DEFAULT NULL,
    tt_module_id VARCHAR(32) DEFAULT NULL,
    tt_template_id VARCHAR(32) DEFAULT NULL,
    tt_type_id VARCHAR(32) DEFAULT NULL,
    tt_scheduling_method INTEGER DEFAULT NULL,
    learn_group_name VARCHAR(255) DEFAULT NULL,
    description VARCHAR(2000) DEFAULT NULL,
    PRIMARY KEY (tt_activity_id),
    CONSTRAINT activity_module FOREIGN KEY (tt_module_id) REFERENCES module (tt_module_id),
    CONSTRAINT activity_template FOREIGN KEY (tt_template_id) REFERENCES activity_template (tt_template_id)
);

CREATE TABLE activity_group (
    activity_group_id INTEGER GENERATED BY DEFAULT AS IDENTITY,
    tt_activity_id VARCHAR(32) NOT NULL,
    module_course_id INTEGER NOT NULL,
    learn_group_id VARCHAR(40) DEFAULT NULL,
    learn_group_created DATETIME DEFAULT NULL,
    PRIMARY KEY(activity_group_id),
    CONSTRAINT activity_group_course FOREIGN KEY (module_course_id) REFERENCES module_course(module_course_id),
    CONSTRAINT learn_group_activity FOREIGN KEY (tt_activity_id) REFERENCES activity(tt_activity_id)
);

CREATE TABLE learn_merged_course (
  learn_source_course_code VARCHAR(40) NOT NULL,
  learn_target_course_code VARCHAR(40) NOT NULL,
  PRIMARY KEY (learn_source_course_code, learn_target_course_code)
);

CREATE TABLE activity_parents (
    tt_activity_id VARCHAR(32) NOT NULL,
    tt_parent_activity_id VARCHAR(32) NOT NULL,
    tt_obsolete_from INTEGER DEFAULT NULL,
    tt_latest_transaction INTEGER DEFAULT NULL,
    PRIMARY KEY (tt_activity_id),
    CONSTRAINT parent_activity FOREIGN KEY (tt_activity_id) REFERENCES activity (tt_activity_id),
    CONSTRAINT parent_parent FOREIGN KEY (tt_parent_activity_id) REFERENCES activity (tt_activity_id)
);

CREATE TABLE variantjtaacts (
    tt_activity_id VARCHAR(32) NOT NULL,
    tt_is_jta_parent INTEGER NOT NULL,
    tt_is_jta_child INTEGER NOT NULL,
    tt_is_variant_parent INTEGER NOT NULL,
    tt_is_variant_child INTEGER NOT NULL,
    tt_latest_transaction INTEGER DEFAULT NULL,
    CONSTRAINT variant_activity FOREIGN KEY (tt_activity_id) REFERENCES activity (tt_activity_id)
);

CREATE TABLE run_result (
    result_code VARCHAR(20) NOT NULL,
    result_label VARCHAR(80) NOT NULL,
    PRIMARY KEY (result_code)
);

CREATE SEQUENCE SYNCHRONISATION_RUN_SEQ;
CREATE TABLE synchronisation_run (
    run_id INTEGER PRIMARY KEY,
    start_time DATETIME NOT NULL,
    cache_copy_completed DATETIME DEFAULT NULL,
    diff_completed DATETIME DEFAULT NULL,
    end_time DATETIME DEFAULT NULL,
    result_code VARCHAR(20) DEFAULT NULL,
    CONSTRAINT sync_result FOREIGN KEY (result_code) REFERENCES run_result(result_code)
);

CREATE TABLE synchronisation_run_prev (
    run_id INTEGER NOT NULL,
    previous_run_id INTEGER NULL,
    PRIMARY KEY(run_id),
    CONSTRAINT sync_prev_run FOREIGN KEY (run_id) REFERENCES synchronisation_run(run_id),
    CONSTRAINT sync_prev_prev FOREIGN KEY (previous_run_id) REFERENCES synchronisation_run(run_id),
    UNIQUE(previous_run_id)
);

CREATE TABLE cache_enrolment (
    run_id INTEGER NOT NULL,
    tt_student_set_id VARCHAR(32) NOT NULL,
    tt_activity_id VARCHAR(32) NOT NULL,
    PRIMARY KEY (run_id,tt_student_set_id,tt_activity_id),
    CONSTRAINT cache_enrolment_ibfk_1 FOREIGN KEY (tt_activity_id) REFERENCES activity (tt_activity_id),
    CONSTRAINT cache_enrolment_ibfk_2 FOREIGN KEY (run_id) REFERENCES synchronisation_run (run_id)
);

CREATE TABLE change_result (
    result_code VARCHAR(20) NOT NULL,
    label VARCHAR(80) NOT NULL,
    retry TINYINT DEFAULT '0' NOT NULL,
    PRIMARY KEY (result_code)
);

CREATE TABLE change_type (
    change_type VARCHAR(12) NOT NULL,
    PRIMARY KEY (change_type)
);

CREATE TABLE student_set (
  tt_student_set_id VARCHAR(32) NOT NULL,
  tt_host_key VARCHAR(32) NOT NULL,
  learn_user_id VARCHAR(40) DEFAULT NULL,
  PRIMARY KEY (tt_student_set_id)
);

CREATE TABLE enrolment_change (
  change_id INTEGER GENERATED BY DEFAULT AS IDENTITY,
  run_id INTEGER NOT NULL,
  tt_activity_id VARCHAR(32) NOT NULL,
  tt_student_set_id VARCHAR(32) NOT NULL,
  change_type VARCHAR(12) NOT NULL,
  PRIMARY KEY(change_id),
  CONSTRAINT enrolment_change_activ FOREIGN KEY (tt_activity_id) REFERENCES activity (tt_activity_id),
  CONSTRAINT enrolment_change_run FOREIGN KEY (run_id) REFERENCES synchronisation_run (run_id),
  CONSTRAINT enrolment_change_stu FOREIGN KEY (tt_student_set_id) REFERENCES student_set (tt_student_set_id),
  CONSTRAINT enrolment_change_type FOREIGN KEY (change_type) REFERENCES change_type (change_type)
);

CREATE TABLE enrolment_change_part (
  part_id INTEGER GENERATED BY DEFAULT AS IDENTITY,
  change_id INTEGER NOT NULL,
  learn_course_code VARCHAR(40) NOT NULL,
  result_code VARCHAR(20) DEFAULT NULL,
  update_completed DATETIME DEFAULT NULL,
  PRIMARY KEY (part_id),
  constraint enrolment_change_res FOREIGN KEY (result_code) REFERENCES change_result (result_code)
);


CREATE VIEW template_set_size_vw AS
    (SELECT t.tt_template_id, COUNT(b.tt_activity_id) AS set_size
        FROM activity_template t
            LEFT JOIN activity b ON t.tt_template_id = b.tt_template_id
        GROUP BY t.tt_template_id
    );

CREATE VIEW jta_child_activity_vw AS
    (SELECT a.tt_activity_id
        FROM variantjtaacts a WHERE a.tt_is_jta_child='1');
CREATE VIEW jta_parent_activity_vw AS
    (SELECT a.tt_activity_id
        FROM variantjtaacts a WHERE a.tt_is_jta_parent='1');
CREATE VIEW variant_child_activity_vw AS
    (SELECT a.tt_activity_id
        FROM variantjtaacts a WHERE a.tt_is_variant_child='1');
CREATE VIEW variant_parent_activity_vw AS
    (SELECT a.tt_activity_id
        FROM variantjtaacts a WHERE a.tt_is_variant_parent='1'
    );

CREATE VIEW sync_module_vw AS
    (SELECT m.tt_module_id, m.tt_course_code, m.tt_module_name, m.tt_academic_year,
        m.learn_course_code
        FROM module m
        WHERE m.webct_active = 'Y'
    );

CREATE VIEW sync_template_vw AS
    (SELECT t.tt_template_id, t.tt_template_name, t.tt_user_text_5, t.learn_group_set_id, s.set_size
        FROM activity_template t
            JOIN template_set_size_vw s ON s.tt_template_id = t.tt_template_id
        WHERE (t.tt_user_text_5 IS NULL OR t.tt_user_text_5!='Not for VLE') 
            AND s.set_size > '1'
    );

CREATE VIEW sync_activity_vw AS
    (SELECT a.tt_activity_id, a.tt_activity_name, a.tt_module_id,
            a.learn_group_name, a.description, a.tt_type_id, a.tt_template_id
        FROM activity a
            JOIN sync_module_vw m ON m.tt_module_id = a.tt_module_id
            JOIN sync_template_vw t ON t.tt_template_id=a.tt_template_id
        WHERE a.tt_scheduling_method!='0'
            AND a.tt_activity_id NOT IN (SELECT tt_activity_id FROM variant_child_activity_vw)
            AND t.set_size>'1'
    );

CREATE VIEW non_jta_sync_activity_vw AS
    (SELECT a.tt_activity_id, a.tt_activity_name,
            a.learn_group_name, a.description, a.tt_type_id, a.tt_template_id
        FROM sync_activity_vw a
        WHERE a.tt_activity_id NOT IN (SELECT tt_activity_id FROM jta_child_activity_vw)
    );
CREATE VIEW jta_sync_activity_vw AS
    (SELECT a.tt_activity_id, p.tt_activity_name,
            p.learn_group_name, p.description, p.tt_type_id, p.tt_template_id
        FROM sync_activity_vw a
            JOIN activity_parents ap ON ap.tt_activity_id=a.tt_activity_id
            JOIN sync_activity_vw p ON p.tt_activity_id=ap.tt_parent_activity_id
        WHERE a.tt_activity_id IN (SELECT tt_activity_id FROM jta_child_activity_vw)
    );

CREATE VIEW sync_student_set_vw AS
    (SELECT s.tt_student_set_id, s.tt_host_key username, s.learn_user_id
        FROM student_set s
        WHERE s.tt_host_key IS NOT NULL
            AND SUBSTR(s.tt_host_key, 1, 6)!='#SPLUS'
    );

CREATE VIEW added_enrolment_vw AS
    (SELECT a.run_id AS run_id,a.previous_run_id, ca.tt_student_set_id, ca.tt_activity_id,
        'ADD' AS change_type
        FROM synchronisation_run_prev a
            JOIN cache_enrolment ca ON ca.run_id = a.run_id
            JOIN sync_activity_vw act ON act.tt_activity_id = ca.tt_activity_id
            JOIN sync_student_set_vw stu ON stu.tt_student_set_id = ca.tt_student_set_id
            LEFT JOIN synchronisation_run_prev b ON b.run_id = a.previous_run_id
            LEFT JOIN cache_enrolment cb ON cb.run_id = b.run_id AND cb.tt_student_set_id = ca.tt_student_set_id AND cb.tt_activity_id = ca.tt_activity_id
        WHERE cb.tt_student_set_id IS NULL
    );

CREATE VIEW removed_enrolment_vw AS
    (SELECT a.run_id AS run_id, a.previous_run_id AS previous_run_id,
        ca.tt_student_set_id AS tt_student_set_id, ca.tt_activity_id AS tt_activity_id,
        'REMOVE' AS change_type
        FROM synchronisation_run_prev a
            JOIN synchronisation_run_prev b ON b.run_id = a.previous_run_id 
            JOIN cache_enrolment cb ON cb.run_id = b.run_id
            JOIN sync_activity_vw act ON act.tt_activity_id = cb.tt_activity_id
            JOIN sync_student_set_vw stu ON stu.tt_student_set_id = cb.tt_student_set_id
            LEFT JOIN cache_enrolment ca ON ca.run_id = a.run_id AND cb.tt_student_set_id = ca.tt_student_set_id AND cb.tt_activity_id = ca.tt_activity_id
        WHERE ca.tt_student_set_id IS NULL
    );
    
CREATE VIEW change_part_vw AS
    (SELECT c.run_id, c.change_id, mc.learn_course_code, mc.learn_course_id
        FROM enrolment_change c
            JOIN activity a ON a.tt_activity_id=c.tt_activity_id
            JOIN module m on m.tt_module_id=a.tt_module_id
            JOIN module_course mc ON mc.tt_module_id=m.tt_module_id
    );

CREATE TRIGGER course_code_ins BEFORE INSERT ON module 
   REFERENCING NEW ROW AS newrow
   FOR EACH ROW WHEN (REGEXP_MATCHES (newrow.tt_course_code, '^[A-Z][A-Z0-9]+_[A-Z][A-Z0-9]+_[A-Z][A-Z0-9]+$'))
   BEGIN ATOMIC
     SET newrow.cache_course_code=LEFT(newrow.tt_course_code, LOCATE('_', newrow.tt_course_code)-1);
     SET newrow.cache_semester_code=SUBSTRING(newrow.tt_course_code FROM LOCATE('_', newrow.tt_course_code, LENGTH(newrow.cache_course_code)+2)+1);
     SET newrow.cache_occurrence_code=SUBSTRING(newrow.tt_course_code FROM LENGTH(newrow.cache_course_code)+2 FOR (LENGTH(newrow.tt_course_code) - LENGTH(newrow.cache_course_code) - LENGTH(newrow.cache_semester_code) - 2));
     SET newrow.learn_academic_year=REPLACE(newrow.tt_academic_year, '/', '-');
     SET newrow.learn_course_code=CONCAT(newrow.cache_course_code, newrow.learn_academic_year, newrow.cache_occurrence_code, newrow.cache_semester_code);
   END;

CREATE TRIGGER course_code_upd BEFORE UPDATE ON module 
   REFERENCING NEW ROW AS newrow
   FOR EACH ROW WHEN (REGEXP_MATCHES (newrow.tt_course_code, '^[A-Z][A-Z0-9]+_[A-Z][A-Z0-9]+_[A-Z][A-Z0-9]+$'))
   BEGIN ATOMIC
     SET newrow.cache_course_code=LEFT(newrow.tt_course_code, LOCATE('_', newrow.tt_course_code)-1);
     SET newrow.cache_semester_code=SUBSTRING(newrow.tt_course_code FROM LOCATE('_', newrow.tt_course_code, LENGTH(newrow.cache_course_code)+2)+1);
     SET newrow.cache_occurrence_code=SUBSTRING(newrow.tt_course_code FROM LENGTH(newrow.cache_course_code)+2 FOR (LENGTH(newrow.tt_course_code) - LENGTH(newrow.cache_course_code) - LENGTH(newrow.cache_semester_code) - 2));
     SET newrow.learn_academic_year=REPLACE(newrow.tt_academic_year, '/', '-');
     SET newrow.learn_course_code=CONCAT(newrow.cache_course_code, newrow.learn_academic_year, newrow.cache_occurrence_code, newrow.cache_semester_code);
   END;
   
INSERT INTO change_type (change_type) VALUES ('ADD');
INSERT INTO change_type (change_type) VALUES ('REMOVE');

INSERT INTO change_result (result_code, label, retry) VALUES ('SUCCESS', 'Success', '0');
INSERT INTO change_result (result_code, label, retry) VALUES ('COURSE_MISSING', 'Course does not exist', '1');
INSERT INTO change_result (result_code, label, retry) VALUES ('GROUP_MISSING', 'Group does not exist', '0');
INSERT INTO change_result (result_code, label, retry) VALUES ('STUDENT_MISSING', 'Student does not exist', '1');
INSERT INTO change_result (result_code, label, retry) VALUES ('NOT_ON_COURSE', 'Student is not on the course', '1');
INSERT INTO change_result (result_code, label, retry) VALUES ('ALREADY_REMOVED', 'Student has already been removed from the group', '0');
INSERT INTO change_result (result_code, label, retry) VALUES ('ALREADY_IN_GROUP', 'Student has already been added to the group', '0');

INSERT INTO run_result (result_code, result_label)
  VALUES ('SUCCESS', 'Synchronisation completed successfully.');
INSERT INTO run_result (result_code, result_label)
  VALUES ('FATAL', 'Synchronisation failed due to an unrecoverable error.');
INSERT INTO run_result (result_code, result_label)
  VALUES ('TIMEOUT', 'Synchronisation timed out.');
INSERT INTO run_result (result_code, result_label)
  VALUES ('ABANDONED', 'Synchronisation abadoned due to concurrent process.');
