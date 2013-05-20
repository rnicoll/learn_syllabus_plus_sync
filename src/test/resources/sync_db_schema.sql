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
  merge_course_code VARCHAR(40) DEFAULT NULL,
  learn_academic_year VARCHAR(6) DEFAULT NULL,
  learn_course_code VARCHAR(40) DEFAULT NULL,
  learn_course_id VARCHAR(80) DEFAULT NULL,
  webct_active CHAR(1) DEFAULT NULL,
  PRIMARY KEY (tt_module_id)
);

CREATE TABLE activity (
  tt_activity_id VARCHAR(32) NOT NULL,
  tt_activity_name VARCHAR(255) DEFAULT NULL,
  tt_module_id VARCHAR(32) DEFAULT NULL,
  tt_template_id VARCHAR(32) DEFAULT NULL,
  tt_type_id VARCHAR(32) DEFAULT NULL,
  tt_jta_activity_id VARCHAR(32) DEFAULT NULL,
  tt_scheduling_method INTEGER DEFAULT NULL,
  learn_group_id VARCHAR(80) DEFAULT NULL,
  learn_group_name VARCHAR(255) DEFAULT NULL,
  learn_group_created DATETIME DEFAULT NULL,
  description CLOB,
  PRIMARY KEY (tt_activity_id),
  CONSTRAINT activity_module FOREIGN KEY (tt_module_id) REFERENCES module (tt_module_id),
  CONSTRAINT activity_template FOREIGN KEY (tt_template_id) REFERENCES activity_template (tt_template_id)
);

CREATE TABLE variantjtaaccts (
    tt_activity_id VARCHAR(32) NOT NULL,
    tt_is_jta_parent INTEGER NOT NULL,
    tt_is_jta_child INTEGER NOT NULL,
    tt_is_variant_parent INTEGER NOT NULL,
    tt_is_variant_child INTEGER NOT NULL,
    tt_latest_transaction INTEGER DEFAULT NULL,
    CONSTRAINT variant_activity FOREIGN KEY (tt_activity_id) REFERENCES activity (tt_activity_id)
);

CREATE TABLE synchronisation_run (
  run_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  previous_run_id INTEGER DEFAULT NULL,
  start_time DATETIME NOT NULL,
  cache_copy_completed DATETIME DEFAULT NULL,
  diff_completed DATETIME DEFAULT NULL,
  end_time DATETIME DEFAULT NULL
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
  learn_person_id VARCHAR(80) DEFAULT NULL,
  PRIMARY KEY (tt_student_set_id)
);

CREATE TABLE enrolment_change (
  change_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  run_id INTEGER NOT NULL,
  tt_activity_id VARCHAR(32) NOT NULL,
  tt_student_set_id VARCHAR(32) NOT NULL,
  change_type VARCHAR(12) NOT NULL,
  result_code VARCHAR(20) DEFAULT NULL,
  update_completed DATETIME DEFAULT NULL,
  CONSTRAINT enrolment_change_activ FOREIGN KEY (tt_activity_id) REFERENCES activity (tt_activity_id),
  CONSTRAINT enrolment_change_res FOREIGN KEY (result_code) REFERENCES change_result (result_code),
  CONSTRAINT enrolment_change_run FOREIGN KEY (run_id) REFERENCES synchronisation_run (run_id),
  CONSTRAINT enrolment_change_stu FOREIGN KEY (tt_student_set_id) REFERENCES student_set (tt_student_set_id),
  CONSTRAINT enrolment_change_type FOREIGN KEY (change_type) REFERENCES change_type (change_type)
);

CREATE VIEW activity_set_size_vw AS
	(SELECT a.tt_activity_id, COUNT(b.tt_activity_id) AS set_size
		FROM activity a
			LEFT JOIN activity_template t ON t.tt_template_id = a.tt_template_id
			LEFT JOIN activity b ON t.tt_template_id = b.tt_template_id
		GROUP BY a.tt_activity_id
	);


CREATE VIEW sync_activities_vw AS
	(SELECT a.tt_activity_id, a.tt_activity_name, a.tt_jta_activity_id, 
			a.learn_group_id, a.description, a.tt_type_id, a.tt_template_id,
			m.learn_course_code, m.learn_course_id, s.set_size
		FROM activity a
			JOIN module m ON m.tt_module_id = a.tt_module_id
			JOIN activity_set_size_vw s ON s.tt_activity_id = a.tt_activity_id
		WHERE a.tt_scheduling_method!='0'
                        AND m.webct_active = 'Y'
			AND s.set_size > '1'
	);

CREATE VIEW sync_student_set_vw AS
	(SELECT s.tt_student_set_id, s.tt_host_key username, s.learn_person_id
		FROM student_set s
		WHERE LEFT(s.tt_host_key, 6)!='#SPLUS'
	);

CREATE VIEW added_enrolment_vw AS
	(SELECT a.run_id AS run_id,a.previous_run_id, ca.tt_student_set_id, ca.tt_activity_id, 'add' AS change_type
		FROM synchronisation_run a
			JOIN cache_enrolment ca ON ca.run_id = a.run_id
			JOIN sync_activities_vw act ON act.tt_activity_id = ca.tt_activity_id
			JOIN sync_student_set_vw stu ON stu.tt_student_set_id = ca.tt_student_set_id
			LEFT JOIN synchronisation_run b ON b.run_id = a.previous_run_id
			LEFT JOIN cache_enrolment cb ON cb.run_id = b.run_id AND cb.tt_student_set_id = ca.tt_student_set_id AND cb.tt_activity_id = ca.tt_activity_id
		WHERE cb.tt_student_set_id IS NULL);

CREATE VIEW removed_enrolment_vw AS
	(SELECT a.run_id AS run_id,a.previous_run_id AS previous_run_id,ca.tt_student_set_id AS tt_student_set_id,ca.tt_activity_id AS tt_activity_id,'remove' AS change_type
		FROM synchronisation_run a
			JOIN synchronisation_run b ON b.run_id = a.previous_run_id 
			JOIN cache_enrolment cb ON cb.run_id = b.run_id
			JOIN sync_activities_vw act ON act.tt_activity_id = cb.tt_activity_id
			JOIN sync_student_set_vw stu ON stu.tt_student_set_id = cb.tt_student_set_id
			LEFT JOIN cache_enrolment ca ON ca.run_id = a.run_id AND cb.tt_student_set_id = ca.tt_student_set_id AND cb.tt_activity_id = ca.tt_activity_id
		WHERE ca.tt_student_set_id IS NULL);

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
   
