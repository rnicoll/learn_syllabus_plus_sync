CREATE OR REPLACE VIEW sync_activity_vw AS
    (SELECT a.tt_activity_id, a.tt_activity_name, a.tt_module_id,
            a.learn_group_name, a.description, a.tt_type_id, a.tt_template_id, t.set_size
        FROM activity a
            LEFT JOIN sync_template_vw t ON t.tt_template_id=a.tt_template_id
            LEFT JOIN variantjtaacts v ON v.tt_activity_id=a.tt_activity_id
        WHERE a.tt_scheduling_method!='0'
            AND (v.tt_activity_id IS NULL or v.tt_is_variant_child='0')
            AND (a.tt_template_id IS NULL OR t.set_size>'1')
    );
CREATE INDEX variant_jta_activity_id ON variantjtaacts(tt_activity_id) tablespace SATVLE_INDEX;