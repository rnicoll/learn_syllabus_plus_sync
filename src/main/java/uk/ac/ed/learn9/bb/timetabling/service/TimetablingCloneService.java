package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
public class TimetablingCloneService extends AbstractCloneService {

    public static final String REPORTING_ACTIVITY_TABLE = "ACTIVITY";
    public static final String REPORTING_ACTIVITY_TYPE_TABLE = "ACTIVITYTYPES";
    public static final String REPORTING_ACTIVITY_TEMPLATE_TABLE = "TEMPLATE";
    public static final String REPORTING_MODULE_TABLE = "MODULE";
    public static final String REPORTING_STUDENT_SET_TABLE = "STUDENT_SET";
    
    public static final String CACHE_ACTIVITY_TABLE = "activity";
    public static final String CACHE_ACTIVITY_TYPE_TABLE = "activity_type";
    public static final String CACHE_ACTIVITY_TEMPLATE_TABLE = "activity_template";
    public static final String CACHE_MODULE_TABLE = "module";
    public static final String CACHE_STUDENT_SET_TABLE = "student_set";
    
    public static final String CACHE_ACTIVITY_PRIMARY_KEY = "tt_activity_id";
    public static final String CACHE_ACTIVITY_TEMPLATE_PRIMARY_KEY = "tt_template_id";
    public static final String CACHE_ACTIVITY_TYPE_PRIMARY_KEY = "tt_type_id";
    public static final String CACHE_MODULE_PRIMARY_KEY = "tt_module_id";
    public static final String CACHE_STUDENT_SET_PRIMARY_KEY = "tt_student_set_id";
    
    public static final String[][] ACTIVITY_FIELD_MAPPINGS = {
        {"NAME", "tt_activity_name"},
        {"MODUL", "tt_module_id"},
        {"ACTIVITY_TMPL", "tt_template_id"},
        {"ACTIVITY_TYPE", "tt_type_id"}
    };
    public static final String[][] ACTIVITY_TEMPLATE_FIELD_MAPPINGS = {
        {"NAME", "tt_template_name"}
    };
    public static final String[][] ACTIVITY_TYPE_FIELD_MAPPINGS = {
        {"NAME", "tt_type_name"}
    };
    public static final String[][] MODULE_FIELD_MAPPINGS = {
        {"NAME", "tt_module_name"},
        {"HOST_KEY", "tt_course_code"},
        {"USER_TEXT_2", "tt_academic_year"}
    };
    public static final String[][] STUDENT_SET_FIELD_MAPPINGS = {
        {"HOST_KEY", "tt_host_key"}
    };

    /**
     * Clone activities from reporting to the local database. This copies all
     * activities, not just those that are relevant to Learn, as it's a lot
     * simpler and avoids unneeded complexity. Activities are generally mapped
     * to groups in Learn.
     * 
     * @param source a connection to the reporting database.
     * @param destination a connection to the cache database.
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
                REPORTING_ACTIVITY_TABLE, CACHE_ACTIVITY_TABLE,
                "ID", CACHE_ACTIVITY_PRIMARY_KEY,
                fieldMappings);
    }

    /**
     * Clone activity templates from reporting to the local database. Activity
     * templates are used to group activities together (for example associating
     * all tutorials in a set of tutorial groups).
     * 
     * @param source a connection to the reporting database.
     * @param destination a connection to the cache database.
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
                REPORTING_ACTIVITY_TEMPLATE_TABLE, CACHE_ACTIVITY_TEMPLATE_TABLE,
                "ID", CACHE_ACTIVITY_TEMPLATE_PRIMARY_KEY,
                fieldMappings);
    }

    /**
     * Clone activity types from reporting to the local database. Activity
     * types are used to generate the description of a group in Learn.
     * 
     * @param source a connection to the reporting database.
     * @param destination a connection to the cache database.
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
                REPORTING_ACTIVITY_TYPE_TABLE, CACHE_ACTIVITY_TYPE_TABLE,
                "ID", CACHE_ACTIVITY_TYPE_PRIMARY_KEY,
                fieldMappings);
    }

    /**
     * Clone modules from reporting to the local database. Modules are equivalent
     * to courses in Learn.
     * 
     * @param source a connection to the reporting database.
     * @param destination a connection to the cache database.
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
                REPORTING_MODULE_TABLE, CACHE_MODULE_TABLE,
                "ID", CACHE_MODULE_PRIMARY_KEY,
                fieldMappings);
    }

    /**
     * Synchronises student sets from the timetabling reporting database into the
     * local database. These are primarily used for caching details of the relevant
     * User object in Learn.
     */
    public void cloneStudentSets(final Connection source, final Connection destination)
            throws SQLException {
        final Map<String, String> fieldMappings = new HashMap<String, String>();

        for (String[] mapping : STUDENT_SET_FIELD_MAPPINGS) {
            fieldMappings.put(mapping[0], mapping[1]);
        }

        cloneTable(source, destination,
                REPORTING_STUDENT_SET_TABLE, CACHE_STUDENT_SET_TABLE,
                "ID", CACHE_STUDENT_SET_PRIMARY_KEY,
                fieldMappings);
    }

    /**
     * Imports details of joint taught activity's parent activities. Where an
     * activity is part of a joint taught activity, the course it belongs to
     * is taken from the parent activity instead. We cache this data locally
     * primarily to help understand the process state.
     */
    public void importJtaDetails(final Connection source, final Connection destination)
            throws SQLException {
        // We select the datasets on both source and destination, then simply
        // copy from one to the other
        // Note that the ordering of these queries is important for the synchronisation
        // code to work correctly.
        final PreparedStatement sourceStatement = source.prepareStatement(
            "SELECT A.ID, P.ID JTA_PARENT_ID "
            + "FROM ACTIVITY A "
                + "JOIN VARIANTJTAACTS V ON V.ID=A.ID "
                + "JOIN ACTIVITY_PARENTS AP ON AP.ID=A.ID "
                + "JOIN ACTIVITY P ON P.ID=AP.PARENT_ACTS "
                + "JOIN VARIANTJTAACTS VP ON VP.ID=P.ID "
            + "WHERE V.ISJTACHILD='1' "
                + "AND VP.ISJTAPARENT='1' "
                + "ORDER BY A.ID",
            ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY
        );
        try {
            final PreparedStatement destinationStatement = destination.prepareStatement(
               "SELECT "
                    + CACHE_ACTIVITY_PRIMARY_KEY + ", tt_jta_activity_id "
                    + "FROM " + CACHE_ACTIVITY_TABLE + " "
                    + "ORDER BY " + CACHE_ACTIVITY_PRIMARY_KEY,
               ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE
            );
            try {
                // Map the "JTA_PARENT_ID" field in the source to the "tt_jta_activity_id"
                // field in the destination.
                final Map<String, String> fieldMappings = Collections.singletonMap("JTA_PARENT_ID", "tt_jta_activity_id");
                
                // Synchronize from one statement to the other.
                this.cloneQuery(sourceStatement, destinationStatement,
                    "ID", CACHE_ACTIVITY_PRIMARY_KEY,
                    fieldMappings);
            } finally {
                destinationStatement.close();
            }
        } finally {
            sourceStatement.close();
        }
    }
}
