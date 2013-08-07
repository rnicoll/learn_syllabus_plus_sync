CREATE OR REPLACE VIEW removed_enrolment_vw AS
    (SELECT a.run_id AS run_id, a.previous_run_id AS previous_run_id,
        cb.tt_student_set_id AS tt_student_set_id, cb.tt_activity_id AS tt_activity_id,
        'REMOVE' AS change_type
        FROM synchronisation_run_prev a
            JOIN synchronisation_run b ON b.run_id = a.previous_run_id 
            JOIN cache_enrolment cb ON cb.run_id = b.run_id
            JOIN sync_activity_vw act ON act.tt_activity_id = cb.tt_activity_id
            JOIN sync_student_set_vw stu ON stu.tt_student_set_id = cb.tt_student_set_id
            LEFT JOIN cache_enrolment ca ON ca.run_id = a.run_id AND cb.tt_student_set_id = ca.tt_student_set_id AND cb.tt_activity_id = ca.tt_activity_id
        WHERE ca.tt_student_set_id IS NULL
    );

