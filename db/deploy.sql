CREATE DATABASE  IF NOT EXISTS learn_group /*!40100 DEFAULT CHARACTER SET utf8 */;
USE learn_group;

--
-- Table structure for table activity_template
--

DROP TABLE IF EXISTS activity_template;
CREATE TABLE activity_template (
  tt_template_id varchar(32) NOT NULL,
  tt_template_name varchar(255) DEFAULT NULL,
  PRIMARY KEY (tt_template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table activity_type
--

DROP TABLE IF EXISTS activity_type;
CREATE TABLE activity_type (
  tt_type_id varchar(32) NOT NULL,
  tt_type_name varchar(255) DEFAULT NULL,
  PRIMARY KEY (tt_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table module
--

DROP TABLE IF EXISTS module;
CREATE TABLE module (
  tt_module_id varchar(32) NOT NULL,
  tt_course_code varchar(20) DEFAULT NULL,
  tt_module_name varchar(255) DEFAULT NULL,
  tt_academic_year varchar(12) DEFAULT NULL,
  cache_semester_code varchar(6) DEFAULT NULL,
  cache_occurrence_code varchar(6) DEFAULT NULL,
  cache_course_code varchar(12) DEFAULT NULL,
  merge_course_code varchar(40) DEFAULT NULL,
  learn_academic_year varchar(6) DEFAULT NULL,
  learn_course_code varchar(40) DEFAULT NULL,
  learn_course_id varchar(80) DEFAULT NULL,
  webct_active char(1) DEFAULT NULL,
  PRIMARY KEY (tt_module_id),
  KEY cache_course_code (cache_course_code,cache_semester_code,cache_occurrence_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table activity
--

DROP TABLE IF EXISTS activity;
CREATE TABLE activity (
  tt_activity_id varchar(32) NOT NULL,
  tt_activity_name varchar(255) DEFAULT NULL,
  tt_module_id varchar(32) DEFAULT NULL,
  tt_template_id varchar(32) DEFAULT NULL,
  tt_type_id varchar(32) DEFAULT NULL,
  tt_jta_activity_id varchar(32) DEFAULT NULL,
  learn_group_id varchar(80) DEFAULT NULL,
  learn_group_name varchar(255) DEFAULT NULL,
  description text,
  PRIMARY KEY (tt_activity_id),
  KEY tt_module_id (tt_module_id),
  KEY tt_type_id (tt_type_id),
  KEY activity_template (tt_template_id),
  CONSTRAINT activity_module FOREIGN KEY (tt_module_id) REFERENCES module (tt_module_id),
  CONSTRAINT activity_template FOREIGN KEY (tt_template_id) REFERENCES activity_template (tt_template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table synchronisation_run
--

DROP TABLE IF EXISTS synchronisation_run;
CREATE TABLE synchronisation_run (
  run_id int(11) NOT NULL AUTO_INCREMENT,
  previous_run_id int(11) DEFAULT NULL,
  start_time datetime NOT NULL,
  cache_copy_completed datetime DEFAULT NULL,
  diff_completed datetime DEFAULT NULL,
  end_time datetime DEFAULT NULL,
  PRIMARY KEY (run_id),
  UNIQUE KEY synchronisation_previous_run (previous_run_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table cache_enrolment
--

DROP TABLE IF EXISTS cache_enrolment;
CREATE TABLE cache_enrolment (
  run_id int(11) NOT NULL,
  tt_student_set_id varchar(32) NOT NULL,
  tt_activity_id varchar(32) NOT NULL,
  PRIMARY KEY (run_id,tt_student_set_id,tt_activity_id),
  KEY tt_activity_id (tt_activity_id),
  CONSTRAINT cache_enrolment_ibfk_1 FOREIGN KEY (tt_activity_id) REFERENCES activity (tt_activity_id),
  CONSTRAINT cache_enrolment_ibfk_2 FOREIGN KEY (run_id) REFERENCES synchronisation_run (run_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table change_result
--

DROP TABLE IF EXISTS change_result;
CREATE TABLE change_result (
  result_code varchar(20) NOT NULL,
  label varchar(80) NOT NULL,
  retry tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (result_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table change_type
--

DROP TABLE IF EXISTS change_type;
CREATE TABLE change_type (
  change_type varchar(12) NOT NULL,
  PRIMARY KEY (change_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table student_set
--

DROP TABLE IF EXISTS student_set;
CREATE TABLE student_set (
  tt_student_set_id varchar(32) NOT NULL,
  tt_host_key varchar(32) NOT NULL,
  learn_person_id varchar(80) DEFAULT NULL,
  PRIMARY KEY (tt_student_set_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table enrolment_change
--

DROP TABLE IF EXISTS enrolment_change;
CREATE TABLE enrolment_change (
  change_id int(11) NOT NULL AUTO_INCREMENT,
  run_id int(11) NOT NULL,
  tt_activity_id varchar(32) NOT NULL,
  tt_student_set_id varchar(32) NOT NULL,
  change_type varchar(12) NOT NULL,
  result_code varchar(20) DEFAULT NULL,
  update_completed datetime DEFAULT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Final view structure for view activity_set_size_vw
--

CREATE VIEW activity_set_size_vw AS
	(select a.tt_activity_id, count(b.tt_activity_id) AS set_size
		from activity a
			left join activity_template t on t.tt_template_id = a.tt_template_id
			left join activity b on t.tt_template_id = b.tt_template_id
		group by a.tt_activity_id
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
	(select a.run_id AS run_id,a.previous_run_id, ca.tt_student_set_id, ca.tt_activity_id, 'add' AS change_type
		from synchronisation_run a
			join cache_enrolment ca on ca.run_id = a.run_id
			join sync_activities_vw act on act.tt_activity_id = ca.tt_activity_id
			join sync_student_set_vw stu on stu.tt_student_set_id = ca.tt_student_set_id
			left join synchronisation_run b on b.run_id = a.previous_run_id
			left join cache_enrolment cb on cb.run_id = b.run_id and cb.tt_student_set_id = ca.tt_student_set_id and cb.tt_activity_id = ca.tt_activity_id
		where cb.tt_student_set_id IS NULL);

--
-- Final view structure for view removed_enrolment_vw
--

CREATE VIEW removed_enrolment_vw AS
	(select a.run_id AS run_id,a.previous_run_id AS previous_run_id,ca.tt_student_set_id AS tt_student_set_id,ca.tt_activity_id AS tt_activity_id,'remove' AS change_type
		from (((((synchronisation_run a
			join synchronisation_run b on((b.run_id = a.previous_run_id)))
			join cache_enrolment cb on((cb.run_id = b.run_id)))
			join sync_activities_vw act on((act.tt_activity_id = cb.tt_activity_id)))
			join sync_student_set_vw stu on((stu.tt_student_set_id = cb.tt_student_set_id)))
			left join cache_enrolment ca on(((ca.run_id = a.run_id) and (cb.tt_student_set_id = ca.tt_student_set_id) and (cb.tt_activity_id = ca.tt_activity_id))))
		where isnull(ca.tt_student_set_id));

-- Dump completed on 2013-05-15 16:03:58
