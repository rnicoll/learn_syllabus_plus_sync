DROP DATABASE learn_group;
CREATE DATABASE learn_group CHARSET=utf8;
USE learn_group;

CREATE TABLE activity_template (
  tt_template_id VARCHAR(32) NOT NULL,
  tt_template_name VARCHAR(255) DEFAULT NULL,
  tt_user_text_5 VARCHAR(255) DEFAULT NULL,
  learn_group_set_id VARCHAR(80) DEFAULT NULL,
  PRIMARY KEY (tt_template_id)
) ENGINE=InnoDB;

CREATE TABLE activity_type (
  tt_type_id VARCHAR(32) NOT NULL,
  tt_type_name VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (tt_type_id)
) ENGINE=InnoDB;

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
) ENGINE=InnoDB;

CREATE TABLE activity (
  tt_activity_id VARCHAR(32) NOT NULL,
  tt_activity_name VARCHAR(255) DEFAULT NULL,
  tt_module_id VARCHAR(32) DEFAULT NULL,
  tt_template_id VARCHAR(32) DEFAULT NULL,
  tt_type_id VARCHAR(32) DEFAULT NULL,
  tt_scheduling_method INTEGER DEFAULT NULL,
  learn_group_id VARCHAR(80) DEFAULT NULL,
  learn_group_name VARCHAR(255) DEFAULT NULL,
  learn_group_created DATE DEFAULT NULL,
  description TEXT,
  PRIMARY KEY (tt_activity_id),
  CONSTRAINT FOREIGN KEY (tt_module_id) REFERENCES module (tt_module_id),
  CONSTRAINT FOREIGN KEY (tt_type_id) REFERENCES activity_type (tt_type_id),
  CONSTRAINT FOREIGN KEY (tt_template_id) REFERENCES activity_template (tt_template_id)
) ENGINE=InnoDB;

CREATE TABLE activity_parents (
	tt_activity_id VARCHAR(32) NOT NULL,
	tt_parent_activity_id VARCHAR(32) NOT NULL,
	tt_obsolete_from INTEGER DEFAULT NULL,
	tt_latest_transaction INTEGER DEFAULT NULL,
	PRIMARY KEY (tt_activity_id),
	CONSTRAINT FOREIGN KEY (tt_activity_id) REFERENCES activity (tt_activity_id),
	CONSTRAINT FOREIGN KEY (tt_parent_activity_id) REFERENCES activity (tt_activity_id)
) ENGINE=InnoDB;

CREATE TABLE variantjtaacts (
    tt_activity_id VARCHAR(32) NOT NULL,
    tt_is_jta_parent SMALLINT NOT NULL,
    tt_is_jta_child SMALLINT NOT NULL,
    tt_is_variant_parent SMALLINT NOT NULL,
    tt_is_variant_child SMALLINT NOT NULL,
    tt_latest_transaction INTEGER DEFAULT NULL,
    CONSTRAINT FOREIGN KEY (tt_activity_id) REFERENCES activity (tt_activity_id)
) ENGINE=InnoDB;

CREATE TABLE run_result (
  result_code VARCHAR(20) NOT NULL,
  result_label VARCHAR(80) NOT NULL,
  PRIMARY KEY (result_code)
) ENGINE=InnoDB;

CREATE TABLE synchronisation_run (
  run_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
  start_time DATE NOT NULL,
  cache_copy_completed DATE DEFAULT NULL,
  diff_completed DATE DEFAULT NULL,
  end_time DATE DEFAULT NULL,
  result_code VARCHAR(20) DEFAULT NULL,
  CONSTRAINT FOREIGN KEY (result_code) REFERENCES run_result(result_code)
) ENGINE=InnoDB;

CREATE TABLE synchronisation_run_prev (
  run_id INTEGER NOT NULL,
  previous_run_id INTEGER NULL,
  UNIQUE(previous_run_id),
  PRIMARY KEY (run_id),
  CONSTRAINT FOREIGN KEY (run_id) REFERENCES synchronisation_run(run_id),
  CONSTRAINT FOREIGN KEY (previous_run_id) REFERENCES synchronisation_run(run_id)
) ENGINE=InnoDB;

CREATE TABLE student_set (
  tt_student_set_id VARCHAR(32) NOT NULL,
  tt_host_key VARCHAR(32) NOT NULL,
  learn_person_id VARCHAR(80) DEFAULT NULL,
  PRIMARY KEY (tt_student_set_id)
) ENGINE=InnoDB;

CREATE TABLE cache_enrolment (
  run_id INTEGER NOT NULL,
  tt_student_set_id VARCHAR(32) NOT NULL,
  tt_activity_id VARCHAR(32) NOT NULL,
  PRIMARY KEY (run_id,tt_student_set_id,tt_activity_id),
  CONSTRAINT FOREIGN KEY (run_id) REFERENCES synchronisation_run(run_id),
  CONSTRAINT FOREIGN KEY (tt_student_set_id) REFERENCES student_set (tt_student_set_id),
  CONSTRAINT FOREIGN KEY (tt_activity_id) REFERENCES activity (tt_activity_id)
) ENGINE=InnoDB;

CREATE TABLE change_result (
  result_code VARCHAR(20) NOT NULL,
  label VARCHAR(80) NOT NULL,
  retry TINYINT DEFAULT '0' NOT NULL,
  PRIMARY KEY (result_code)
) ENGINE=InnoDB;

CREATE TABLE change_type (
  change_type VARCHAR(12) NOT NULL,
  PRIMARY KEY (change_type)
) ENGINE=InnoDB;

CREATE TABLE enrolment_change (
  change_id INTEGER NOT NULL,
  run_id INTEGER NOT NULL,
  tt_activity_id VARCHAR(32) NOT NULL,
  tt_student_set_id VARCHAR(32) NOT NULL,
  change_type VARCHAR(12) NOT NULL,
  result_code VARCHAR(20) DEFAULT NULL,
  update_completed DATE DEFAULT NULL,
  PRIMARY KEY (change_id),
  CONSTRAINT FOREIGN KEY (run_id) REFERENCES synchronisation_run(run_id),
  CONSTRAINT FOREIGN KEY (tt_student_set_id) REFERENCES student_set (tt_student_set_id),
  CONSTRAINT FOREIGN KEY (tt_activity_id) REFERENCES activity (tt_activity_id),
  CONSTRAINT FOREIGN KEY (change_type) REFERENCES change_type(change_type),
  CONSTRAINT FOREIGN KEY (result_code) REFERENCES change_result(result_code)
) ENGINE=InnoDB;

