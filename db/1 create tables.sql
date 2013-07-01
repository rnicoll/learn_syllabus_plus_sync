CREATE TABLE activity_template (
  tt_template_id VARCHAR2(32) NOT NULL,
  tt_template_name NVARCHAR2(255) DEFAULT NULL,
  tt_user_text_5 NVARCHAR2(255) DEFAULT NULL,
  learn_group_set_id VARCHAR2(80) DEFAULT NULL,
  constraint "ACTIVITY_TEMPLATE_PK" PRIMARY KEY (tt_template_id)
) tablespace "SATVLE_DATA";

comment on column activity_template.tt_template_id is 'ID for the activity template, copied from Timetabling RDB.';
comment on column activity_template.tt_template_name is 'Human readable name for the activity template, copied from Timetabling RDB.';
comment on column activity_template.tt_user_text_5 is 'Field from Timetabling RDB, used to indicate whether an template''s activities are not to be synchronised to Learn.';
comment on column activity_template.learn_group_set_id is 'ID of the group set in Learn that activities from this template should belong to. Currently unused.';

CREATE TABLE activity_type (
  tt_type_id VARCHAR2(32) NOT NULL,
  tt_type_name VARCHAR2(255) DEFAULT NULL,
  PRIMARY KEY (tt_type_id)
) tablespace "SATVLE_DATA";
comment on column activity_type.tt_type_id is 'ID for the activity type, copied from Timetabling RDB.';
comment on column activity_type.tt_type_name is 'Human readable name for the activity type, copied from Timetabling RDB.';

CREATE TABLE module (
  tt_module_id VARCHAR2(32) NOT NULL,
  tt_course_code NVARCHAR2(20) DEFAULT NULL,
  tt_module_name NVARCHAR2(255) DEFAULT NULL,
  tt_academic_year NVARCHAR2(12) DEFAULT NULL,
  cache_semester_code VARCHAR2(6) DEFAULT NULL,
  cache_occurrence_code VARCHAR2(6) DEFAULT NULL,
  cache_course_code VARCHAR2(12) DEFAULT NULL,
  learn_academic_year VARCHAR2(6) DEFAULT NULL,
  learn_course_code VARCHAR2(40) DEFAULT NULL,
  webct_active CHAR(1) DEFAULT NULL,
  constraint "MODULE_PK" PRIMARY KEY (tt_module_id)
) tablespace "SATVLE_DATA";
comment on column module.tt_module_id is 'ID for the module, copied from Timetabling RDB.';
comment on column module.tt_course_code is 'Course code, copied from the "HOST_KEY" field in the Timetabling RDB. Typically reflects EUCLID course code, occurrence and semester.';
comment on column module.tt_module_name is 'Human readable name for the module, copied from Timetabling RDB.';
comment on column module.tt_academic_year is 'Academic year code for the module, copied from Timetabling RDB, for example "2002/3".';
comment on column module.cache_semester_code is 'Semester code, copied from "tt_course_code" where present.';
comment on column module.cache_occurrence_code is 'Occurrence code, extracted from "tt_course_code" where present.';
comment on column module.cache_course_code is 'EUCLID course code, extracted from "tt_course_code" where present.';
comment on column module.learn_academic_year is 'Academic year code translated to the form used in Learn course codes, for example "2002-3".';
comment on column module.learn_course_code is 'Course code as used in Learn, before any merging or parent/child course handling.';
comment on column module.webct_active is 'Yes/No indicator for whether this course is to be synchronised from EUGEX to Learn. Copied from EUGEX.';

CREATE TABLE module_course (
  module_course_id INTEGER NOT NULL,
  tt_module_id VARCHAR2(40) NOT NULL,
  merged_course CHAR(1) NOT NULL,
  learn_course_code VARCHAR2(40) NOT NULL,
  learn_course_id VARCHAR2(40) DEFAULT NULL,
  learn_course_available CHAR(1) DEFAULT NULL,
  constraint "MODULE_COURSE_PK" PRIMARY KEY (module_course_id)
) tablespace "SATVLE_DATA";
comment on column module_course.module_course_id is 'Automatically generated ID for this relationship, based on the MODULE_COURSE_SEQ sequence.';
comment on column module_course.tt_module_id is 'ID for the module, copied from Timetabling RDB.';
comment on column module_course.merged_course IS 'Yes/No indicator for whether the course is from the external merge process.';
comment on column module_course.learn_course_code is 'Course code for a course in Learn that the module feeds into.';
comment on column module_course.learn_course_id is 'ID for the course in Learn that the "learn_course_code" field relates to.';
comment on column module_course.learn_course_available is 'Yes/No indicator for whether the course in Learn is available. Null if not yet determined.';

CREATE TABLE learn_merged_course (
  learn_source_course_code VARCHAR2(40) NOT NULL,
  learn_target_course_code VARCHAR2(40) NOT NULL,
  constraint "LEARN_MERGED_COURSE_PK" PRIMARY KEY (learn_source_course_code, learn_target_course_code)
) tablespace "SATVLE_DATA";
comment on column learn_merged_course.learn_source_course_code is 'Course code for the source course in Learn that enrolments are merged from.';
comment on column learn_merged_course.learn_target_course_code is 'Course code for the target course in Learn that enrolments are merged into.';

CREATE TABLE activity (
  tt_activity_id VARCHAR2(32) NOT NULL,
  tt_activity_name NVARCHAR2(255) DEFAULT NULL,
  tt_module_id VARCHAR2(32) DEFAULT NULL CONSTRAINT activity_module REFERENCES module (tt_module_id),
  tt_template_id VARCHAR2(32) DEFAULT NULL CONSTRAINT activity_template REFERENCES activity_template (tt_template_id),
  tt_type_id VARCHAR2(32) DEFAULT NULL,
  tt_scheduling_method NUMBER(10,0) DEFAULT NULL,
  learn_group_name NVARCHAR2(255) DEFAULT NULL,
  description NVARCHAR2(2000) DEFAULT NULL,
  constraint "ACTIVITY_PK" PRIMARY KEY (tt_activity_id)
) tablespace "SATVLE_DATA";
comment on column activity.tt_activity_id is 'ID for the activity, copied from Timetabling RDB.';
comment on column activity.tt_activity_name is 'Human readable name for the activity, copied from Timetabling RDB.';
comment on column activity.tt_module_id is 'ID for the module the activity belongs to, copied from Timetabling RDB.';
comment on column activity.tt_template_id is 'ID for the template the activity belongs to, copied from Timetabling RDB.';
comment on column activity.tt_type_id is 'ID for the type of activity, copied from Timetabling RDB.';
comment on column activity.tt_scheduling_method is 'Numerical identifier for how the activity was scheduled. 0 indicates unscheduled and not to be synchronised.';
comment on column activity.learn_group_name is 'Human readable name for the group(s) to be created in Learn.';
comment on column activity.description is 'Human readable description for the group(s) to be created in Learn.';

CREATE TABLE activity_group (
  activity_group_id INTEGER NOT NULL,
  tt_activity_id VARCHAR2(32) NOT NULL,
  module_course_id INTEGER NOT NULL,
  learn_group_id VARCHAR2(40) DEFAULT NULL,
  learn_group_created DATE DEFAULT NULL,
  constraint ACTIVITY_GROUP_PK PRIMARY KEY(activity_group_id),
  constraint ACTIVITY_GROUP_COURSE FOREIGN KEY (module_course_id) REFERENCES module_course(module_course_id),
  constraint LEARN_GROUP_ACTIVITY FOREIGN KEY (tt_activity_id) REFERENCES activity(tt_activity_id)
);
comment on column activity_group.activity_group_id is 'Automatically generated ID for this relationship, based on the ACTIVITY_GROUP_SEQ sequence.';
comment on column activity_group.tt_activity_id is 'ID for the activity, copied from Timetabling RDB.';
comment on column activity_group.module_course_id is 'ID of the module/course relationship this is group based on.';
comment on column activity_group.learn_group_id is 'ID for the group created in Learn for the activity on the course.';
comment on column activity_group.learn_group_created is 'Timestamp when the group was created in Learn.';

/* There's no referential integrity constraints here due to quality (or lack thereof)
 * of the source data.
 */
CREATE TABLE activity_parents (
    tt_activity_id VARCHAR2(32) NOT NULL,
    tt_parent_activity_id VARCHAR2(32) NOT NULL,
    tt_obsolete_from INTEGER DEFAULT NULL,
    tt_latest_transaction INTEGER DEFAULT NULL,
    constraint "ACTIVITY_PARENT_PK" PRIMARY KEY (tt_activity_id, tt_parent_activity_id)
) tablespace "SATVLE_DATA";
comment on column activity_parents.tt_activity_id is 'ID for the child activity, copied from Timetabling RDB.';
comment on column activity_parents.tt_parent_activity_id is 'ID for the parent activity, copied from Timetabling RDB.';
comment on column activity_parents.tt_obsolete_from is 'Indicator for when this record becomes obsolete, copied from Timetabling RDB. Currently unused.';
comment on column activity_parents.tt_latest_transaction is 'ID for the latest transaction on this record, copied from Timetabling RDB. Currently unused.';

CREATE TABLE variantjtaacts (
    tt_activity_id VARCHAR2(32) NOT NULL CONSTRAINT variant_activity REFERENCES activity (tt_activity_id),
    tt_is_jta_parent NUMBER(3,0) NOT NULL,
    tt_is_jta_child NUMBER(3,0) NOT NULL,
    tt_is_variant_parent NUMBER(3,0) NOT NULL,
    tt_is_variant_child NUMBER(3,0) NOT NULL,
    tt_latest_transaction INTEGER DEFAULT NULL
) tablespace "SATVLE_DATA";
comment on column variantjtaacts.tt_activity_id is 'ID for the activity the record relates to, copied from Timetabling RDB.';
comment on column variantjtaacts.tt_is_jta_parent is 'Indicates whether the activity is a joint taught activity parent, copied from Timetabling RDB.';
comment on column variantjtaacts.tt_is_jta_child is 'Indicates whether the activity is a joint taught activity child, copied from Timetabling RDB.';
comment on column variantjtaacts.tt_is_variant_parent is 'Indicates whether the activity is an activity variant parent, copied from Timetabling RDB. Currently unused';
comment on column variantjtaacts.tt_is_variant_child is 'Indicates whether the activity is an activity variant child, copied from Timetabling RDB. Currently unused.';

CREATE TABLE run_result (
  result_code VARCHAR2(20) NOT NULL,
  result_label NVARCHAR2(80) NOT NULL,
  constraint "RUN_RESULT_PK" PRIMARY KEY (result_code)
) tablespace "SATVLE_DATA";
comment on column run_result.result_code is 'ID for a possible result from a synchronisation run.';
comment on column run_result.result_label is 'Human readable label for this result from a synchronisation run.';

CREATE TABLE synchronisation_run (
  run_id INTEGER NOT NULL,
  start_time DATE NOT NULL,
  cache_copy_completed DATE DEFAULT NULL,
  diff_completed DATE DEFAULT NULL,
  end_time DATE DEFAULT NULL,
  result_code VARCHAR2(20) DEFAULT NULL REFERENCES run_result(result_code),
  constraint "SYNCHRONISATION_RUN_PK" PRIMARY KEY (run_id)
) tablespace "SATVLE_DATA";
comment on column synchronisation_run.run_id is 'Automatically generated ID for this run, based on the SYNCHRONISATION_RUN_SEQ sequence.';
comment on column synchronisation_run.start_time is 'Timestamp when the synchronisation process started.';
comment on column synchronisation_run.cache_copy_completed is 'Timestamp when the copy of data from RDB, EUGEX and BBLFeeds databases has completed.';
comment on column synchronisation_run.diff_completed is 'Timestamp when the difference generation has completed.';
comment on column synchronisation_run.end_time is 'Timestamp when the synchronisation process ended (irrespective of outcome).';
comment on column synchronisation_run.result_code is 'ID for the result of the synchronisation run (as in success, failure, timeout, etc.).';

CREATE TABLE synchronisation_run_prev (
  run_id INTEGER NOT NULL REFERENCES synchronisation_run(run_id),
  previous_run_id INTEGER NULL REFERENCES synchronisation_run(run_id),
  constraint "SYNC_RUN_PREV_PK" PRIMARY KEY(run_id),
  constraint "SYNC_RUN_PREV_UNIQ" UNIQUE(previous_run_id)
) tablespace "SATVLE_DATA";
comment on column synchronisation_run_prev.run_id is 'ID for the synchronisation run.';
comment on column synchronisation_run_prev.previous_run_id is 'ID for the synchronisation run against which a difference is to be generated.';

CREATE TABLE cache_enrolment (
  run_id INTEGER NOT NULL CONSTRAINT cache_run REFERENCES synchronisation_run(run_id),
  tt_student_set_id VARCHAR2(32) NOT NULL,
  tt_activity_id VARCHAR2(32) NOT NULL CONSTRAINT cache_activ REFERENCES activity (tt_activity_id),
  constraint "CACHE_ENROLMENT_PK" PRIMARY KEY (run_id,tt_student_set_id,tt_activity_id)
) tablespace "SATVLE_DATA";
comment on column cache_enrolment.run_id is 'ID for the synchronisation run this data set belongs to.';
comment on column cache_enrolment.tt_student_set_id is 'ID for the student set the enrolment is for, copied from Timetabling RDB.';
comment on column cache_enrolment.tt_activity_id is 'ID for the activity the enrolment is to, copied from Timetabling RDB.';

CREATE TABLE change_result (
  result_code VARCHAR2(20) NOT NULL,
  label NVARCHAR2(80) NOT NULL,
  retry NUMBER(1) DEFAULT '0' NOT NULL,
  constraint "CHANGE_RESULT_PK" PRIMARY KEY (result_code)
) tablespace "SATVLE_DATA";
comment on column change_result.result_code is 'ID for a possible result from an enrolment change.';
comment on column change_result.label is 'Human readable label for this result from an enrolment change.';
comment on column change_result.retry is 'Indicates whether changes with this result code should be re-attempted later.';

CREATE TABLE change_type (
  change_type VARCHAR2(12) NOT NULL,
  constraint "CHANGE_TYPE_PK" PRIMARY KEY (change_type)
) tablespace "SATVLE_DATA";
comment on column change_type.change_type is 'ID for the type of change, determines how it should be applied to Learn.';

CREATE TABLE student_set (
  tt_student_set_id VARCHAR2(32) NOT NULL,
  tt_host_key VARCHAR2(32) NOT NULL,
  learn_user_id VARCHAR2(40) DEFAULT NULL,
  constraint "STUDENT_SET_PK" PRIMARY KEY (tt_student_set_id)
) tablespace "SATVLE_DATA";
comment on column student_set.tt_student_set_id is 'ID for the student set, copied from Timetabling RDB.';
comment on column student_set.tt_host_key is 'Host key for the student set, copied from Timetabling RDB. Typically this is their username.';
comment on column student_set.learn_user_id is 'ID for the user the student set relates to, in Learn, where applicable.';

CREATE TABLE enrolment_change (
  change_id INTEGER NOT NULL,
  run_id INTEGER NOT NULL,
  tt_activity_id VARCHAR2(32) NOT NULL,
  tt_student_set_id VARCHAR2(32) NOT NULL,
  change_type VARCHAR2(12) NOT NULL,
  constraint "ENROLMENT_CHANGE_PK" PRIMARY KEY (change_id),
  CONSTRAINT enrolment_change_activ FOREIGN KEY (tt_activity_id) REFERENCES activity (tt_activity_id),
  CONSTRAINT enrolment_change_run FOREIGN KEY (run_id) REFERENCES synchronisation_run (run_id),
  CONSTRAINT enrolment_change_stu FOREIGN KEY (tt_student_set_id) REFERENCES student_set (tt_student_set_id),
  CONSTRAINT enrolment_change_type FOREIGN KEY (change_type) REFERENCES change_type (change_type)
) tablespace "SATVLE_DATA";
comment on column enrolment_change.change_id is 'Automatically generated ID for this change, based on the ENROLMENT_CHANGE_SEQ sequence.';
comment on column enrolment_change.run_id is 'ID for the synchronisation run this change belongs to.';
comment on column enrolment_change.tt_activity_id is 'ID for the activity, based on generated differences from the cache_enrolment table.';
comment on column enrolment_change.tt_student_set_id is 'ID for the student set, based on generated differences from the cache_enrolment table.';
comment on column enrolment_change.change_type is 'ID for the type of change, determines how it should be applied to Learn.';

CREATE TABLE enrolment_change_part (
  part_id INTEGER NOT NULL,
  change_id INTEGER NOT NULL,
  module_course_id INTEGER NOT NULL,
  result_code VARCHAR2(20) DEFAULT NULL,
  update_completed DATE DEFAULT NULL,
  constraint "ENROLMENT_CHANGE_PART_PK" PRIMARY KEY (part_id),
  CONSTRAINT enrol_change_module FOREIGN KEY (module_course_id) REFERENCES module_course(module_course_id),
  constraint enrol_change_res FOREIGN KEY (result_code) REFERENCES change_result (result_code)
) tablespace "SATVLE_DATA";
comment on column enrolment_change_part.part_id is 'Automatically generated ID for this change part, based on the ENROLMENT_CHANGE_PART_SEQ sequence.';
comment on column enrolment_change_part.change_id is 'ID for the change this is part of.';
comment on column enrolment_change_part.module_course_id is 'Module-course relationship that this part applies to.';
comment on column enrolment_change_part.result_code is 'Change result code for this part.';
comment on column enrolment_change_part.update_completed is 'Timestamp for when the change was completed, where relevant.';
