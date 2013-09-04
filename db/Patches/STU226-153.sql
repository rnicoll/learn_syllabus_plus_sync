CREATE OR REPLACE VIEW sync_activity_vw AS
    (SELECT a.tt_activity_id, a.tt_activity_name, a.tt_module_id,
            a.learn_group_name, a.description, a.tt_type_id, a.tt_template_id, t.set_size
        FROM activity a
            LEFT JOIN sync_template_vw t ON t.tt_template_id=a.tt_template_id
        WHERE a.tt_scheduling_method!='0'
            AND a.tt_activity_id NOT IN (SELECT tt_activity_id FROM variant_child_activity_vw)
            AND (a.tt_template_id IS NULL OR t.set_size>'1')
    );
