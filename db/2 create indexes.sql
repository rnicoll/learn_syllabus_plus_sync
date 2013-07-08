CREATE INDEX activity_parent_idx ON activity_parents(tt_activity_id) tablespace SATVLE_INDEX;
CREATE INDEX cache_course_code ON module(cache_course_code,cache_semester_code,cache_occurrence_code) tablespace SATVLE_INDEX;
CREATE UNIQUE INDEX enrolment_diff_uniq ON enrolment_change(run_id,tt_activity_id,tt_student_set_id) tablespace SATVLE_INDEX;
CREATE INDEX module_course_module ON module_course(tt_module_id) tablespace SATVLE_INDEX;
