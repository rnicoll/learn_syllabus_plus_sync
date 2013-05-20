CREATE TABLE activity_template (
  tt_template_id VARCHAR2(32) NOT NULL,
  tt_template_name NVARCHAR2(255) DEFAULT NULL,
  tt_user_text_5 NVARCHAR2(255) DEFAULT NULL,
  learn_group_set_id VARCHAR2(80) DEFAULT NULL,
  PRIMARY KEY (tt_template_id)
);

CREATE TABLE activity_type (
  tt_type_id VARCHAR2(32) NOT NULL,
  tt_type_name VARCHAR2(255) DEFAULT NULL,
  PRIMARY KEY (tt_type_id)
);

CREATE TABLE module (
  tt_module_id VARCHAR2(32) NOT NULL,
  tt_course_code NVARCHAR2(20) DEFAULT NULL,
  tt_module_name NVARCHAR2(255) DEFAULT NULL,
  tt_academic_year NVARCHAR2(12) DEFAULT NULL,
  cache_semester_code VARCHAR2(6) DEFAULT NULL,
  cache_occurrence_code VARCHAR2(6) DEFAULT NULL,
  cache_course_code VARCHAR2(12) DEFAULT NULL,
  merge_course_code VARCHAR2(40) DEFAULT NULL,
  learn_academic_year VARCHAR2(6) DEFAULT NULL,
  learn_course_code VARCHAR2(40) DEFAULT NULL,
  learn_course_id VARCHAR2(80) DEFAULT NULL,
  webct_active CHAR(1) DEFAULT NULL,
  PRIMARY KEY (tt_module_id)  
);
CREATE INDEX cache_course_code  ON module(cache_course_code,cache_semester_code,cache_occurrence_code);

CREATE TABLE activity (
  tt_activity_id VARCHAR2(32) NOT NULL,
  tt_activity_name NVARCHAR2(255) DEFAULT NULL,
  tt_module_id VARCHAR2(32) DEFAULT NULL CONSTRAINT activity_module REFERENCES module (tt_module_id),
  tt_template_id VARCHAR2(32) DEFAULT NULL CONSTRAINT activity_template REFERENCES activity_template (tt_template_id),
  tt_type_id VARCHAR2(32) DEFAULT NULL,
  tt_scheduling_method NUMBER(10,0) DEFAULT NULL,
  learn_group_id VARCHAR2(80) DEFAULT NULL,
  learn_group_name NVARCHAR2(255) DEFAULT NULL,
  learn_group_created DATE DEFAULT NULL,
  description CLOB,
  PRIMARY KEY (tt_activity_id)
);

CREATE TABLE activity_parents (
    tt_activity_id VARCHAR2(32) NOT NULL CONSTRAINT parent_activity REFERENCES activity (tt_activity_id),
    tt_parent_activity_id VARCHAR2(32) NOT NULL CONSTRAINT parent_parent REFERENCES activity (tt_activity_id),
    tt_obsolete_from INTEGER DEFAULT NULL,
    tt_latest_transaction INTEGER DEFAULT NULL,
    PRIMARY KEY (tt_activity_id)
);

CREATE TABLE variantjtaacts (
    tt_activity_id VARCHAR2(32) NOT NULL CONSTRAINT variant_activity REFERENCES activity (tt_activity_id),
    tt_is_jta_parent NUMBER(3,0) NOT NULL,
    tt_is_jta_child NUMBER(3,0) NOT NULL,
    tt_is_variant_parent NUMBER(3,0) NOT NULL,
    tt_is_variant_child NUMBER(3,0) NOT NULL,
    tt_latest_transaction INTEGER DEFAULT NULL
);

CREATE TABLE synchronisation_run (
  run_id INTEGER NOT NULL,
  previous_run_id INTEGER DEFAULT NULL,
  start_time DATE NOT NULL,
  cache_copy_completed DATE DEFAULT NULL,
  diff_completed DATE DEFAULT NULL,
  end_time DATE DEFAULT NULL,
  PRIMARY KEY (run_id)
);
CREATE INDEX run_diff_uniq ON synchronisation_run(previous_run_id);

CREATE SEQUENCE SYNCHRONISATION_RUN_SEQ;

CREATE TABLE cache_enrolment (
  run_id INTEGER NOT NULL CONSTRAINT cache_run REFERENCES synchronisation_run(run_id),
  tt_student_set_id VARCHAR2(32) NOT NULL,
  tt_activity_id VARCHAR2(32) NOT NULL CONSTRAINT cache_activ REFERENCES activity (tt_activity_id),
  PRIMARY KEY (run_id,tt_student_set_id,tt_activity_id)
);

CREATE TABLE change_result (
  result_code VARCHAR2(20) NOT NULL,
  label NVARCHAR2(80) NOT NULL,
  retry NUMBER(1) DEFAULT '0' NOT NULL,
  PRIMARY KEY (result_code)
);

CREATE TABLE change_type (
  change_type VARCHAR2(12) NOT NULL,
  PRIMARY KEY (change_type)
);

CREATE TABLE student_set (
  tt_student_set_id VARCHAR2(32) NOT NULL,
  tt_host_key VARCHAR2(32) NOT NULL,
  learn_person_id VARCHAR2(80) DEFAULT NULL,
  PRIMARY KEY (tt_student_set_id)
);

CREATE TABLE enrolment_change (
  change_id INTEGER NOT NULL,
  run_id INTEGER NOT NULL CONSTRAINT enrol_change_run REFERENCES synchronisation_run(run_id),
  tt_activity_id VARCHAR2(32) NOT NULL CONSTRAINT enrol_change_activity REFERENCES activity (tt_activity_id),
  tt_student_set_id VARCHAR2(32) NOT NULL CONSTRAINT enrol_change_stu REFERENCES student_set (tt_student_set_id),
  change_type VARCHAR2(12) NOT NULL CONSTRAINT enrol_change_type REFERENCES change_type(change_type),
  result_code VARCHAR2(20) DEFAULT NULL CONSTRAINT enrol_change_result REFERENCES change_result(result_code),
  update_completed DATE DEFAULT NULL,
  PRIMARY KEY (change_id)
);

CREATE INDEX enrolment_diff_uniq ON enrolment_change(run_id,tt_activity_id,tt_student_set_id);
CREATE SEQUENCE ENROLMENT_CHANGE_SEQ;
set define off;
CREATE TRIGGER ENROLMENT_CHANGE_PK
  BEFORE INSERT ON ENROLMENT_CHANGE
    for each row
    begin
      select ENROLMENT_CHANGE_SEQ.nextval into :new.change_id from dual;
    end;
/
set define on;

CREATE VIEW template_set_size_vw AS
    (SELECT t.tt_template_id, COUNT(b.tt_activity_id) AS set_size
        FROM activity_template t
            LEFT JOIN activity b ON t.tt_template_id = b.tt_template_id
        GROUP BY t.tt_template_id
    );
    
CREATE VIEW jta_child_activities_vw AS
    (SELECT a.tt_activity_id
        FROM variantjtaacts a WHERE a.tt_is_jta_child='1');
CREATE VIEW jta_parent_activities_vw AS
    (SELECT a.tt_activity_id
        FROM variantjtaacts a WHERE a.tt_is_jta_parent='1');
CREATE VIEW variant_child_activities_vw AS
    (SELECT a.tt_activity_id
        FROM variantjtaacts a WHERE a.tt_is_variant_child='1');
CREATE VIEW variant_parent_activities_vw AS
    (SELECT a.tt_activity_id
        FROM variantjtaacts a WHERE a.tt_is_variant_parent='1');

CREATE VIEW sync_modules_vw AS
    (SELECT m.tt_module_id, m.tt_course_code, m.tt_module_name, m.tt_academic_year,
        m.merge_course_code, m.learn_course_code, m.learn_course_id,
        COALESCE(m.merge_course_code, m.learn_course_code) effective_course_code
        FROM module m
        WHERE m.webct_active = 'Y'
    );

CREATE VIEW sync_templates_vw AS
    (SELECT t.tt_template_id, t.tt_template_name, t.tt_user_text_5, t.learn_group_set_id, s.set_size
        FROM activity_template t
            JOIN template_set_size_vw s ON s.tt_template_id = t.tt_template_id
        WHERE (t.tt_user_text_5 IS NULL OR t.tt_user_text_5!='Not for VLE') 
            AND s.set_size > '1'
    );

CREATE VIEW sync_activities_vw AS
    (SELECT a.tt_activity_id, a.tt_activity_name, a.learn_group_id,
            a.learn_group_name, a.description, a.tt_type_id, a.tt_template_id,
            m.effective_course_code,
            m.learn_course_id, t.set_size
        FROM activity a
            JOIN sync_modules_vw m ON m.tt_module_id = a.tt_module_id
            JOIN sync_templates_vw t ON t.tt_template_id=a.tt_template_id
        WHERE a.tt_scheduling_method!='0'
            AND a.tt_activity_id NOT IN (SELECT tt_activity_id FROM variant_child_activities_vw)
    );

CREATE VIEW non_jta_sync_activities_vw AS
    (SELECT a.tt_activity_id, a.tt_activity_name, a.learn_group_id,
            a.learn_group_name, a.description, a.tt_type_id, a.tt_template_id,
            a.effective_course_code, a.learn_course_id
        FROM sync_activities_vw a
        WHERE a.tt_activity_id NOT IN (SELECT tt_activity_id FROM jta_child_activities_vw)
    );
CREATE VIEW jta_sync_activities_vw AS
    (SELECT a.tt_activity_id, p.tt_activity_name, p.learn_group_id,
            p.learn_group_name, p.description, p.tt_type_id, p.tt_template_id,
            p.effective_course_code, p.learn_course_id
        FROM sync_activities_vw a
            JOIN activity_parents ap ON ap.tt_activity_id=a.tt_activity_id
            JOIN sync_activities_vw p ON p.tt_activity_id=ap.tt_parent_activity_id
        WHERE a.tt_activity_id IN (SELECT tt_activity_id FROM jta_child_activities_vw)
    );

CREATE VIEW sync_student_set_vw AS
    (SELECT s.tt_student_set_id, s.tt_host_key username, s.learn_person_id
        FROM student_set s
        WHERE s.tt_host_key IS NOT NULL
            AND LEFT(s.tt_host_key, 6)!='#SPLUS'
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

SET DEFINE OFF;
CREATE OR REPLACE TRIGGER course_code_ins BEFORE INSERT OR UPDATE ON module
   FOR EACH ROW WHEN ( REGEXP_LIKE (new.tt_course_code, '^[A-Z][A-Z0-9]+_[A-Z][A-Z0-9]+_[A-Z][A-Z0-9]+$') )
   BEGIN
     :new.cache_course_code := SUBSTR(:new.tt_course_code, 0, INSTR(:new.tt_course_code, '_')-1);
     :new.cache_semester_code := SUBSTR(:new.tt_course_code, INSTR(:new.tt_course_code, '_', LENGTH(:new.cache_course_code)+2)+1);
     :new.cache_occurrence_code := SUBSTR(:new.tt_course_code, LENGTH(:new.cache_course_code)+2, (LENGTH(:new.tt_course_code) - LENGTH(:new.cache_course_code) - LENGTH(:new.cache_semester_code) - 2));
     :new.learn_academic_year := REPLACE(:new.tt_academic_year, '/', '-');
     :new.learn_course_code := :new.cache_course_code || :new.learn_academic_year || :new.cache_occurrence_code || :new.cache_semester_code;
   END;
/
SET DEFINE ON;
