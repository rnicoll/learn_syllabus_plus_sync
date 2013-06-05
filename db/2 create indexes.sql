CREATE INDEX cache_course_code  ON module(cache_course_code,cache_semester_code,cache_occurrence_code);
CREATE INDEX enrolment_diff_uniq ON enrolment_change(run_id,tt_activity_id,tt_student_set_id);
