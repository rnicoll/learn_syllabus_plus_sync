
CREATE OR REPLACE VIEW template_set_size_vw AS
    (SELECT t.tt_template_id, COUNT(b.tt_activity_id) AS set_size
        FROM activity_template t
            LEFT JOIN activity b ON t.tt_template_id = b.tt_template_id
        GROUP BY t.tt_template_id
    );

CREATE OR REPLACE VIEW jta_child_activity_vw AS
    (SELECT a.tt_activity_id
        FROM variantjtaacts a WHERE a.tt_is_jta_child='1');
CREATE OR REPLACE VIEW jta_parent_activity_vw AS
    (SELECT a.tt_activity_id
        FROM variantjtaacts a WHERE a.tt_is_jta_parent='1');
CREATE OR REPLACE VIEW variant_child_activity_vw AS
    (SELECT a.tt_activity_id
        FROM variantjtaacts a WHERE a.tt_is_variant_child='1');
CREATE OR REPLACE VIEW variant_parent_activity_vw AS
    (SELECT a.tt_activity_id
        FROM variantjtaacts a WHERE a.tt_is_variant_parent='1'
    );

CREATE OR REPLACE VIEW sync_module_vw AS
    (SELECT m.tt_module_id, m.tt_course_code, m.tt_module_name, m.tt_academic_year,
        m.learn_course_code
        FROM module m
        WHERE m.webct_active = 'Y'
    );

CREATE OR REPLACE VIEW sync_template_vw AS
    (SELECT t.tt_template_id, t.tt_template_name, t.tt_user_text_5, t.learn_group_set_id, s.set_size
        FROM activity_template t
            JOIN template_set_size_vw s ON s.tt_template_id = t.tt_template_id
        WHERE (t.tt_user_text_5 IS NULL OR t.tt_user_text_5!='Not for VLE') 
            AND s.set_size > '1'
    );

CREATE OR REPLACE VIEW sync_activity_vw AS
    (SELECT a.tt_activity_id, a.tt_activity_name, a.tt_module_id,
            a.learn_group_name, a.description, a.tt_type_id, a.tt_template_id, t.set_size
        FROM activity a
            JOIN sync_module_vw m ON m.tt_module_id = a.tt_module_id
            JOIN sync_template_vw t ON t.tt_template_id=a.tt_template_id
        WHERE a.tt_scheduling_method!='0'
            AND a.tt_activity_id NOT IN (SELECT tt_activity_id FROM variant_child_activity_vw)
            AND t.set_size>'1'
    );

CREATE OR REPLACE VIEW non_jta_activity_group_vw AS
    (SELECT ag.activity_group_id, a.tt_activity_id, a.tt_activity_name,
            a.learn_group_name, a.description, a.tt_type_id, a.tt_template_id,
            ag.learn_group_id, ag.learn_group_created
        FROM sync_activity_vw a
            JOIN activity_group ag ON ag.tt_activity_id=a.tt_activity_id
        WHERE a.tt_activity_id NOT IN (SELECT tt_activity_id FROM jta_child_activity_vw)
    );
CREATE OR REPLACE VIEW jta_activity_group_vw AS
    (SELECT ag.activity_group_id, a.tt_activity_id, p.tt_activity_name,
            p.learn_group_name, p.description, p.tt_type_id, p.tt_template_id,
            ag.learn_group_id, ag.learn_group_created
        FROM sync_activity_vw a
            JOIN activity_parents ap ON ap.tt_activity_id=a.tt_activity_id
            JOIN sync_activity_vw p ON p.tt_activity_id=ap.tt_parent_activity_id
            JOIN activity_group ag ON ag.tt_activity_id=p.tt_activity_id
        WHERE a.tt_activity_id IN (SELECT tt_activity_id FROM jta_child_activity_vw)
    );

CREATE OR REPLACE VIEW sync_student_set_vw AS
    (SELECT s.tt_student_set_id, s.tt_host_key username, s.learn_user_id
        FROM student_set s
        WHERE s.tt_host_key IS NOT NULL
            AND SUBSTR(s.tt_host_key, 1, 6)!='#SPLUS'
    );

CREATE OR REPLACE VIEW added_enrolment_vw AS
    (SELECT a.run_id AS run_id,a.previous_run_id, ca.tt_student_set_id, ca.tt_activity_id,
        'ADD' AS change_type
        FROM synchronisation_run_prev a
            JOIN cache_enrolment ca ON ca.run_id = a.run_id
            JOIN sync_activity_vw act ON act.tt_activity_id = ca.tt_activity_id
            JOIN sync_student_set_vw stu ON stu.tt_student_set_id = ca.tt_student_set_id
            LEFT JOIN synchronisation_run_prev b ON b.run_id = a.previous_run_id
            LEFT JOIN cache_enrolment cb ON cb.run_id = b.run_id AND cb.tt_student_set_id = ca.tt_student_set_id AND cb.tt_activity_id = ca.tt_activity_id
        WHERE cb.tt_student_set_id IS NULL
    );

CREATE OR REPLACE VIEW removed_enrolment_vw AS
    (SELECT a.run_id AS run_id, a.previous_run_id AS previous_run_id,
        ca.tt_student_set_id AS tt_student_set_id, ca.tt_activity_id AS tt_activity_id,
        'REMOVE' AS change_type
        FROM synchronisation_run_prev a
            JOIN synchronisation_run_prev b ON b.run_id = a.previous_run_id 
            JOIN cache_enrolment cb ON cb.run_id = b.run_id
            JOIN sync_activity_vw act ON act.tt_activity_id = cb.tt_activity_id
            JOIN sync_student_set_vw stu ON stu.tt_student_set_id = cb.tt_student_set_id
            LEFT JOIN cache_enrolment ca ON ca.run_id = a.run_id AND cb.tt_student_set_id = ca.tt_student_set_id AND cb.tt_activity_id = ca.tt_activity_id
        WHERE ca.tt_student_set_id IS NULL
    );
    
CREATE OR REPLACE VIEW change_part_vw AS
    (SELECT c.run_id, c.change_id, mc.learn_course_code, mc.learn_course_id
        FROM enrolment_change c
            JOIN activity a ON a.tt_activity_id=c.tt_activity_id
            JOIN module m on m.tt_module_id=a.tt_module_id
            JOIN module_course mc ON mc.tt_module_id=m.tt_module_id
    );