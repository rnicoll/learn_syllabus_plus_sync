/**
 * Clear cached data which has not been applied to Learn.
 */
 
DELETE FROM cache_enrolment WHERE tt_activity_id NOT IN (SELECT TT_activity_id FROM SYNC_activity_vw);
DELETE FROM cache_enrolment WHERE tt_activity_id NOT IN (SELECT TT_activity_id FROM enrolment_change);
DELETE FROM cache_enrolment WHERE tt_student_set_id NOT IN (SELECT TT_student_set_id FROM SYNC_student_set_vw);
DELETE FROM cache_enrolment WHERE tt_student_set_id NOT IN (SELECT TT_student_set_id FROM enrolment_change);
