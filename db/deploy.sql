CREATE DATABASE  IF NOT EXISTS learn_group /*!40100 DEFAULT CHARACTER SET UTF8 */;
USE learn_group;

--
-- Table structure for table activity_template
--

DROP TABLE IF EXISTS activity_template;
CREATE TABLE activity_template (
  tt_template_id VARCHAR(32) NOT NULL,
  tt_template_name VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (tt_template_id)
) ENGINE=INNODB DEFAULT CHARSET=UTF8;

--
-- Table structure for table activity_type
--

DROP TABLE IF EXISTS activity_type;
CREATE TABLE activity_type (
  tt_type_id VARCHAR(32) NOT NULL,
  tt_type_name VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (tt_type_id)
) ENGINE=INNODB DEFAULT CHARSET=UTF8;

--
-- Table structure for table module
--

DROP TABLE IF EXISTS module;
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
  PRIMARY KEY (tt_module_id),
  KEY cache_course_code (cache_course_code,cache_semester_code,cache_occurrence_code)
) ENGINE=INNODB DEFAULT CHARSET=UTF8;

--
-- Table structure for table activity
--

DROP TABLE IF EXISTS activity;
CREATE TABLE activity (
  tt_activity_id VARCHAR(32) NOT NULL,
  tt_activity_name VARCHAR(255) DEFAULT NULL,
  tt_module_id VARCHAR(32) DEFAULT NULL,
  tt_template_id VARCHAR(32) DEFAULT NULL,
  tt_type_id VARCHAR(32) DEFAULT NULL,
  tt_jta_activity_id VARCHAR(32) DEFAULT NULL,
  learn_group_id VARCHAR(80) DEFAULT NULL,
  learn_group_name VARCHAR(255) DEFAULT NULL,
  learn_group_created DATETIME DEFAULT NULL,
  description TEXT,
  PRIMARY KEY (tt_activity_id),
  KEY tt_module_id (tt_module_id),
  KEY tt_type_id (tt_type_id),
  KEY activity_template (tt_template_id),
  CONSTRAINT activity_module FOREIGN KEY (tt_module_id) REFERENCES module (tt_module_id),
  CONSTRAINT activity_template FOREIGN KEY (tt_template_id) REFERENCES activity_template (tt_template_id)
) ENGINE=INNODB DEFAULT CHARSET=UTF8;

--
-- Table structure for table synchronisation_run
--

DROP TABLE IF EXISTS synchronisation_run;
CREATE TABLE synchronisation_run (
  run_id INT(11) NOT NULL AUTO_INCREMENT,
  previous_run_id INT(11) DEFAULT NULL,
  start_time DATETIME NOT NULL,
  cache_copy_completed DATETIME DEFAULT NULL,
  diff_completed DATETIME DEFAULT NULL,
  end_time DATETIME DEFAULT NULL,
  PRIMARY KEY (run_id),
  UNIQUE KEY synchronisation_previous_run (previous_run_id)
) ENGINE=INNODB DEFAULT CHARSET=UTF8;

--
-- Table structure for table cache_enrolment
--

DROP TABLE IF EXISTS cache_enrolment;
CREATE TABLE cache_enrolment (
  run_id INT(11) NOT NULL,
  tt_student_set_id VARCHAR(32) NOT NULL,
  tt_activity_id VARCHAR(32) NOT NULL,
  PRIMARY KEY (run_id,tt_student_set_id,tt_activity_id),
  KEY tt_activity_id (tt_activity_id),
  CONSTRAINT cache_enrolment_ibfk_1 FOREIGN KEY (tt_activity_id) REFERENCES activity (tt_activity_id),
  CONSTRAINT cache_enrolment_ibfk_2 FOREIGN KEY (run_id) REFERENCES synchronisation_run (run_id)
) ENGINE=INNODB DEFAULT CHARSET=UTF8;

--
-- Table structure for table change_result
--

DROP TABLE IF EXISTS change_result;
CREATE TABLE change_result (
  result_code VARCHAR(20) NOT NULL,
  label VARCHAR(80) NOT NULL,
  retry TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (result_code)
) ENGINE=INNODB DEFAULT CHARSET=UTF8;

--
-- Table structure for table change_type
--

DROP TABLE IF EXISTS change_type;
CREATE TABLE change_type (
  change_type VARCHAR(12) NOT NULL,
  PRIMARY KEY (change_type)
) ENGINE=INNODB DEFAULT CHARSET=UTF8;

--
-- Table structure for table student_set
--

DROP TABLE IF EXISTS student_set;
CREATE TABLE student_set (
  tt_student_set_id VARCHAR(32) NOT NULL,
  tt_host_key VARCHAR(32) NOT NULL,
  learn_person_id VARCHAR(80) DEFAULT NULL,
  PRIMARY KEY (tt_student_set_id)
) ENGINE=INNODB DEFAULT CHARSET=UTF8;

--
-- Table structure for table enrolment_change
--

DROP TABLE IF EXISTS enrolment_change;
CREATE TABLE enrolment_change (
  change_id INT(11) NOT NULL AUTO_INCREMENT,
  run_id INT(11) NOT NULL,
  tt_activity_id VARCHAR(32) NOT NULL,
  tt_student_set_id VARCHAR(32) NOT NULL,
  change_type VARCHAR(12) NOT NULL,
  result_code VARCHAR(20) DEFAULT NULL,
  update_completed DATETIME DEFAULT NULL,
  PRIMARY KEY (change_id),
  UNIQUE KEY run_id (run_id,tt_activity_id,tt_student_set_id),
  KEY enrolment_change_run (run_id),
  KEY enrolment_change_activ (tt_activity_id),
  KEY enrolment_change_stu (tt_student_set_id),
  KEY enrolment_change_type (change_type),
  KEY enrolment_change_res (result_code),
  CONSTRAINT enrolment_change_activ FOREIGN KEY (tt_activity_id) REFERENCES activity (tt_activity_id),
  CONSTRAINT enrolment_change_res FOREIGN KEY (result_code) REFERENCES change_result (result_code),
  CONSTRAINT enrolment_change_run FOREIGN KEY (run_id) REFERENCES synchronisation_run (run_id),
  CONSTRAINT enrolment_change_stu FOREIGN KEY (tt_student_set_id) REFERENCES student_set (tt_student_set_id),
  CONSTRAINT enrolment_change_type FOREIGN KEY (change_type) REFERENCES change_type (change_type)
) ENGINE=INNODB DEFAULT CHARSET=UTF8;

--
-- Final view structure for view activity_set_size_vw
--

CREATE VIEW activity_set_size_vw AS
	(SELECT a.tt_activity_id, COUNT(b.tt_activity_id) AS set_size
		FROM activity a
			LEFT JOIN activity_template t ON t.tt_template_id = a.tt_template_id
			LEFT JOIN activity b ON t.tt_template_id = b.tt_template_id
		GROUP BY a.tt_activity_id
	);

--
-- Final view structure for view sync_activities_vw
--

CREATE VIEW sync_activities_vw AS
	(SELECT a.tt_activity_id, a.tt_activity_name, a.tt_jta_activity_id, 
			a.learn_group_id, a.description, a.tt_type_id, a.tt_template_id,
			m.learn_course_code, m.learn_course_id, s.set_size
		FROM activity a
			JOIN module m ON m.tt_module_id = a.tt_module_id
			JOIN activity_set_size_vw s ON s.tt_activity_id = a.tt_activity_id
		WHERE m.webct_active = 'Y'
			AND s.set_size > '1'
	);

--
-- Final view structure for view sync_student_set_vw
--
CREATE VIEW sync_student_set_vw AS
	(SELECT s.tt_student_set_id, s.tt_host_key AS `username`, s.learn_person_id
		FROM student_set s
		WHERE SUBSTR(s.tt_host_key,0,6)!='#SPLUS'
	);
	
--
-- Final view structure for view added_enrolment_vw
--

CREATE VIEW added_enrolment_vw AS
	(SELECT a.run_id AS run_id,a.previous_run_id, ca.tt_student_set_id, ca.tt_activity_id, 'add' AS change_type
		FROM synchronisation_run a
			JOIN cache_enrolment ca ON ca.run_id = a.run_id
			JOIN sync_activities_vw act ON act.tt_activity_id = ca.tt_activity_id
			JOIN sync_student_set_vw stu ON stu.tt_student_set_id = ca.tt_student_set_id
			LEFT JOIN synchronisation_run b ON b.run_id = a.previous_run_id
			LEFT JOIN cache_enrolment cb ON cb.run_id = b.run_id AND cb.tt_student_set_id = ca.tt_student_set_id AND cb.tt_activity_id = ca.tt_activity_id
		WHERE cb.tt_student_set_id IS NULL);

--
-- Final view structure for view removed_enrolment_vw
--

CREATE VIEW removed_enrolment_vw AS
	(SELECT a.run_id AS run_id,a.previous_run_id AS previous_run_id,ca.tt_student_set_id AS tt_student_set_id,ca.tt_activity_id AS tt_activity_id,'remove' AS change_type
		FROM (((((synchronisation_run a
			JOIN synchronisation_run b ON((b.run_id = a.previous_run_id)))
			JOIN cache_enrolment cb ON((cb.run_id = b.run_id)))
			JOIN sync_activities_vw act ON((act.tt_activity_id = cb.tt_activity_id)))
			JOIN sync_student_set_vw stu ON((stu.tt_student_set_id = cb.tt_student_set_id)))
			LEFT JOIN cache_enrolment ca ON(((ca.run_id = a.run_id) AND (cb.tt_student_set_id = ca.tt_student_set_id) AND (cb.tt_activity_id = ca.tt_activity_id))))
		WHERE ISNULL(ca.tt_student_set_id));

-- Dump completed on 2013-05-15 16:03:58
