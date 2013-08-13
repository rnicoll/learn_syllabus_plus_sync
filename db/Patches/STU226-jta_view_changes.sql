/**
 * Draft views for allowing resolution of where joint taught activities
 * actually have groups in a common course in Learn.
 */

CREATE OR REPLACE VIEW jta_parent_activity_group_vw AS 
  (SELECT ag.activity_group_id, a.tt_activity_id, a.tt_activity_name,
            a.learn_group_name, a.description, a.tt_type_id, a.tt_template_id,
            ag.learn_group_id, ag.learn_group_created, mc.learn_course_id
        FROM sync_activity_vw a
            JOIN activity_group ag ON ag.tt_activity_id=a.tt_activity_id
            JOIN module_course mc ON mc.module_course_id=ag.module_course_id
            JOIN variantjtaacts v ON v.tt_activity_id=a.tt_activity_id AND v.tt_is_jta_parent='1'
        WHERE a.tt_activity_id IN (SELECT tt_activity_id FROM jta_parent_activity_vw)
    );

CREATE OR REPLACE VIEW activity_group_vw AS 
  (SELECT ag.activity_group_id, a.tt_activity_id, a.tt_activity_name,
            a.learn_group_name, a.description, a.tt_type_id, a.tt_template_id,
            ag.learn_group_id, ag.learn_group_created, mc.learn_course_id
        FROM sync_activity_vw a
            JOIN activity_group ag ON ag.tt_activity_id=a.tt_activity_id
            JOIN module_course mc ON mc.module_course_id=ag.module_course_id
    );
    
CREATE OR REPLACE VIEW jta_parent_child_vw AS
  (SELECT child_group.activity_group_id, parent_group.activity_group_id
            FROM sync_activity_vw child_activity
              JOIN variantjtaacts child_vjta ON child_vjta.tt_activity_id=child_activity.tt_activity_id AND child_vjta.tt_is_jta_child='1'
              JOIN activity_parents ap ON child_activity.tt_activity_id=ap.tt_activity_id
              JOIN sync_activity_vw parent_activity ON parent_activity.tt_activity_id=ap.tt_parent_activity_id
              JOIN variantjtaacts parent_vjta ON parent_vjta.tt_activity_id=parent_activity.tt_activity_id AND parent_vjta.tt_is_jta_parent='1'
              JOIN activity_group child_group ON child_group.tt_activity_id=child_activity.tt_activity_id
              JOIN activity_group parent_group ON parent_group.tt_activity_id=parent_activity.tt_activity_id
              JOIN module_course child_course ON child_course.module_course_id=child_group.module_course_id
              JOIN module_course parent_course ON parent_course.module_course_id=parent_group.module_course_id
            WHERE child_course.learn_course_id=parent_course.learn_course_id);