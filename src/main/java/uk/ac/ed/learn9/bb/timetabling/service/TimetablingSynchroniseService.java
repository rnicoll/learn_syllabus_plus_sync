package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.stereotype.Service;

/**
 * Service for cloning data from the Timetabling reporting database (RDB)
 * to the local ("cache") database.
 * 
 * This is done so that we're not doing data joins in memory between
 * independent databases (which is both memory consuming, and adds significant
 * complexity to ensuring transaction safety).
 */
@Service
public class TimetablingSynchroniseService extends AbstractSynchroniseService {
    /**
     * Name of the activity table in the reporting database.
     */
    public static final String REPORTING_ACTIVITY_TABLE = "ACTIVITY";
    /**
     * Name of the activity parents table in the reporting database.
     */
    public static final String REPORTING_ACTIVITY_PARENTS_TABLE = "ACTIVITY_PARENTS";
    /**
     * Name of the activity type table in the reporting database.
     */
    public static final String REPORTING_ACTIVITY_TYPE_TABLE = "ACTIVITYTYPES";
    /**
     * Name of the activity template table in the reporting database.
     */
    public static final String REPORTING_ACTIVITY_TEMPLATE_TABLE = "TEMPLATE";
    /**
     * Name of the module table in the reporting database.
     */
    public static final String REPORTING_MODULE_TABLE = "MODULE";
    /**
     * Name of the student sets table in the reporting database.
     */
    public static final String REPORTING_STUDENT_SET_TABLE = "STUDENT_SET";
    /**
     * Name of the variant/JTA activity table in the reporting database.
     */
    public static final String REPORTING_VARIANT_JTA_TABLE = "VARIANTJTAACTS";
    
    /**
     * Name of the activity table in the staging database.
     */
    public static final String STAGING_ACTIVITY_TABLE = "activity";
    /**
     * Name of the activity parents table in the staging database.
     */
    public static final String STAGING_ACTIVITY_PARENTS_TABLE = "ACTIVITY_PARENTS";
    /**
     * Name of the activity type table in the staging database.
     */
    public static final String STAGING_ACTIVITY_TYPE_TABLE = "activity_type";
    /**
     * Name of the activity template table in the staging database.
     */
    public static final String STAGING_ACTIVITY_TEMPLATE_TABLE = "activity_template";
    /**
     * Name of the module table in the staging database.
     */
    public static final String STAGING_MODULE_TABLE = "module";
    /**
     * Name of the student sets table in the staging database.
     */
    public static final String STAGING_STUDENT_SET_TABLE = "student_set";
    /**
     * Name of the variant/JTA activity table in the staging database.
     */
    public static final String STAGING_VARIANT_JTA_TABLE = "variantjtaacts";
    
    /**
     * Name of the primary key field for all Timetabling tables.
     */
    public static final String PRIMARY_KEY_TIMETABLING_TABLES = "ID";
    
    /**
     * Maps names of fields in the activity table in the reporting database, to
     * those in the staging database.
     */
    public static final String[][] ACTIVITY_FIELD_MAPPINGS = {
        {"ID", "tt_activity_id"},
        {"NAME", "tt_activity_name"},
        {"MODUL", "tt_module_id"},
        {"ACTIVITY_TMPL", "tt_template_id"},
        {"ACTIVITY_TYPE", "tt_type_id"},
        {"SCHEDULING_METHOD", "tt_scheduling_method"}
    };
    /**
     * Maps names of fields in the activity parents table in the reporting database, to
     * those in the staging database.
     */
    public static final String[][] ACTIVITY_PARENTS_FIELD_MAPPINGS = {
        {"ID", "tt_activity_id"},
        {"PARENT_ACTS", "tt_parent_activity_id"},
        {"OBSOLETEFROM", "tt_obsolete_from"},
        {"LATESTTRANSACTION", "tt_latest_transaction"}
    };
    /**
     * Maps names of fields in the activity templates table in the reporting database, to
     * those in the staging database.
     */
    public static final String[][] ACTIVITY_TEMPLATE_FIELD_MAPPINGS = {
        {"ID", "tt_template_id"},
        {"NAME", "tt_template_name"},
        {"USER_TEXT_5", "tt_user_text_5"}
    };
    /**
     * Maps names of fields in the activity type in the reporting database, to
     * those in the staging database.
     */
    public static final String[][] ACTIVITY_TYPE_FIELD_MAPPINGS = {
        {"ID", "tt_type_id"},
        {"NAME", "tt_type_name"}
    };
    /**
     * Maps names of fields in the module table in the reporting database, to
     * those in the staging database.
     */
    public static final String[][] MODULE_FIELD_MAPPINGS = {
        {"ID", "tt_module_id"},
        {"NAME", "tt_module_name"},
        {"HOST_KEY", "tt_course_code"},
        {"USER_TEXT_2", "tt_academic_year"}
    };
    /**
     * Maps names of fields in the student set table in the reporting database, to
     * those in the staging database.
     */
    public static final String[][] STUDENT_SET_FIELD_MAPPINGS = {
        {"ID", "tt_student_set_id"},
        {"HOST_KEY", "tt_host_key"}
    };
    /**
     * Maps names of fields in the variant/JTA activity table in the reporting
     * database, to those in the staging database.
     */
    public static final String[][] VARIANT_JTA_FIELD_MAPPINGS = {
        {"ID", "tt_activity_id"},
        {"ISJTAPARENT", "tt_is_jta_parent"},
        {"ISJTACHILD", "tt_is_jta_child"},
        {"ISVARIANTPARENT", "tt_is_variant_parent"},
        {"ISVARIANTCHILD", "tt_is_variant_child"},
        {"LATESTTRANSACTION", "tt_latest_transaction"}
    };

    /**
     * Clone activities from reporting to the local database. This copies all
     * activities, not just those that are relevant to Learn, as it's a lot
     * simpler and avoids unneeded complexity. Activities are generally mapped
     * to groups in Learn.
     * 
     * @param source a connection to the reporting database.
     * @param destination a connection to the staging database.
     * @throws SQLException if there was a problem accessing one of the
     * databases.
     */
    public void cloneActivities(final Connection source, final Connection destination)
            throws SQLException {
        final Map<String, String> fieldMappings = new HashMap<String, String>();

        for (String[] mapping : ACTIVITY_FIELD_MAPPINGS) {
            fieldMappings.put(mapping[0], mapping[1]);
        }

        cloneTable(source, destination,
                REPORTING_ACTIVITY_TABLE, STAGING_ACTIVITY_TABLE,
                PRIMARY_KEY_TIMETABLING_TABLES,
                fieldMappings);
    }

    /**
     * Clone activity parents from reporting to the local database. Activity
     * parents are used for joint-taught-activities and variant activity
     * relationships. Variant activities are ignored for our purposes, but
     * joint taught activities are considered to belong to the parent
     * activity's module.
     * 
     * @param source a connection to the reporting database.
     * @param destination a connection to the staging database.
     * @throws SQLException if there was a problem accessing one of the
     * databases.
     */
    public void cloneActivityParents(final Connection source, final Connection destination)
            throws SQLException {
        final Map<String, String> fieldMappings = new HashMap<String, String>();
        final SortedSet<String> primaryKeys = new TreeSet<String>(){{
            add(PRIMARY_KEY_TIMETABLING_TABLES);
            add("PARENT_ACTS");
        }};

        for (String[] mapping : ACTIVITY_PARENTS_FIELD_MAPPINGS) {
            fieldMappings.put(mapping[0], mapping[1]);
        }

        cloneTable(source, destination,
                REPORTING_ACTIVITY_PARENTS_TABLE, STAGING_ACTIVITY_PARENTS_TABLE,
                primaryKeys, fieldMappings);
    }

    /**
     * Clone activity templates from reporting to the local database. Activity
     * templates are used to group activities together (for example associating
     * all tutorials in a set of tutorial groups).
     * 
     * @param source a connection to the reporting database.
     * @param destination a connection to the staging database.
     * @throws SQLException if there was a problem accessing one of the
     * databases.
     */
    public void cloneActivityTemplates(final Connection source, final Connection destination)
            throws SQLException {
        final Map<String, String> fieldMappings = new HashMap<String, String>();

        for (String[] mapping : ACTIVITY_TEMPLATE_FIELD_MAPPINGS) {
            fieldMappings.put(mapping[0], mapping[1]);
        }

        cloneTable(source, destination,
                REPORTING_ACTIVITY_TEMPLATE_TABLE, STAGING_ACTIVITY_TEMPLATE_TABLE,
                PRIMARY_KEY_TIMETABLING_TABLES,
                fieldMappings);
    }

    /**
     * Clone activity types from reporting to the local database. Activity
     * types are used to generate the description of a group in Learn.
     * 
     * @param source a connection to the reporting database.
     * @param destination a connection to the staging database.
     * @throws SQLException if there was a problem accessing one of the
     * databases.
     */
    public void cloneActivityTypes(final Connection source, final Connection destination)
            throws SQLException {
        final Map<String, String> fieldMappings = new HashMap<String, String>();

        for (String[] mapping : ACTIVITY_TYPE_FIELD_MAPPINGS) {
            fieldMappings.put(mapping[0], mapping[1]);
        }

        cloneTable(source, destination,
                REPORTING_ACTIVITY_TYPE_TABLE, STAGING_ACTIVITY_TYPE_TABLE,
                PRIMARY_KEY_TIMETABLING_TABLES,
                fieldMappings);
    }

    /**
     * Synchronises student sets from the timetabling reporting database into the
     * local database. These are primarily used for caching details of the relevant
     * User object in Learn.
     * 
     * @param source a connection to the reporting database.
     * @param destination a connection to the staging database.
     * @throws SQLException if there was a problem accessing one of the
     * databases.
     */
    public void cloneVariantJointTaughtActivities(final Connection source, final Connection destination)
            throws SQLException {
        final Map<String, String> fieldMappings = new HashMap<String, String>();

        for (String[] mapping : VARIANT_JTA_FIELD_MAPPINGS) {
            fieldMappings.put(mapping[0], mapping[1]);
        }

        cloneTable(source, destination,
                REPORTING_VARIANT_JTA_TABLE, STAGING_VARIANT_JTA_TABLE,
                PRIMARY_KEY_TIMETABLING_TABLES,
                fieldMappings);
    }

    /**
     * Clone modules from reporting to the local database. Modules are equivalent
     * to courses in Learn.
     * 
     * @param source a connection to the reporting database.
     * @param destination a connection to the staging database.
     * @throws SQLException if there was a problem accessing one of the
     * databases.
     */
    public void cloneModules(final Connection source, final Connection destination)
            throws SQLException {
        final Map<String, String> fieldMappings = new HashMap<String, String>();

        for (String[] mapping : MODULE_FIELD_MAPPINGS) {
            fieldMappings.put(mapping[0], mapping[1]);
        }

        cloneTable(source, destination,
                REPORTING_MODULE_TABLE, STAGING_MODULE_TABLE,
                PRIMARY_KEY_TIMETABLING_TABLES,
                fieldMappings);
    }

    /**
     * Synchronises student sets from the timetabling reporting database into the
     * local database. These are primarily used for caching details of the relevant
     * User object in Learn.
     * 
     * @param source a connection to the reporting database.
     * @param destination a connection to the staging database.
     * @throws SQLException if there was a problem accessing one of the
     * databases.
     */
    public void cloneStudentSets(final Connection source, final Connection destination)
            throws SQLException {
        final Map<String, String> fieldMappings = new HashMap<String, String>();

        for (String[] mapping : STUDENT_SET_FIELD_MAPPINGS) {
            fieldMappings.put(mapping[0], mapping[1]);
        }

        cloneTable(source, destination,
                REPORTING_STUDENT_SET_TABLE, STAGING_STUDENT_SET_TABLE,
                PRIMARY_KEY_TIMETABLING_TABLES,
                fieldMappings);
    }
}
