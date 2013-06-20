CREATE INDEX activity_parent_idx ON activity_parents(tt_activity_id);
CREATE INDEX cache_course_code ON module(cache_course_code,cache_semester_code,cache_occurrence_code);
CREATE UNIQUE INDEX enrolment_diff_uniq ON enrolment_change(run_id,tt_activity_id,tt_student_set_id);
CREATE INDEX change_part_course ON enrolment_change_part(learn_course_code);
