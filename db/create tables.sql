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
  description NVARCHAR2(2000) DEFAULT NULL,
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

CREATE TABLE run_result (
  result_code VARCHAR2(20) NOT NULL,
  result_label NVARCHAR2(80) NOT NULL,
  PRIMARY KEY (result_code)
);

CREATE TABLE synchronisation_run (
  run_id INTEGER NOT NULL,
  start_time DATE NOT NULL,
  cache_copy_completed DATE DEFAULT NULL,
  diff_completed DATE DEFAULT NULL,
  end_time DATE DEFAULT NULL,
  result_code VARCHAR2(20) DEFAULT NULL REFERENCES run_result(result_code),
  PRIMARY KEY (run_id)
);

CREATE TABLE synchronisation_run_prev (
  run_id INTEGER NOT NULL REFERENCES synchronisation_run(run_id),
  previous_run_id INTEGER NULL REFERENCES synchronisation_run(run_id),
  PRIMARY KEY(run_id),
  UNIQUE(previous_run_id)
);

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
  learn_user_id VARCHAR2(80) DEFAULT NULL,
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
