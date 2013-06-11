package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for monitoring which courses in Learn are synchronised from EUGEX.
 */
@Service
public class EugexService extends AbstractCloneService {
    @Autowired
    private DataSource stagingDataSource;
    @Autowired
    private DataSource eugexDataSource;

    /**
     * Synchronises details of courses in EUGEX marked as active in the VLE,
     * to the staging database.
     * @throws SQLException 
     */
    public void synchroniseVleActiveCourses()
            throws SQLException {
        final Connection stagingDatabase = this.getStagingDataSource().getConnection();
        try {
            final Connection eugexDatabase = this.getEugexDataSource().getConnection();
            try {
                this.synchroniseVleActiveCourses(stagingDatabase, eugexDatabase);
            } finally {
                eugexDatabase.close();
            }
        } finally {
            stagingDatabase.close();
        }
    }

    /**
     * Synchronises details of courses that are copied from EUGEX to Learn,
     * from the EUGEX database into the staging database.
     * 
     * @param stagingDatabase a connection to the staging database.
     * @param eugexDatabase a connection to the EUGEX database.
     * @throws SQLException 
     */
    private void synchroniseVleActiveCourses(final Connection stagingDatabase, final Connection eugexDatabase)
        throws SQLException {
        final SortedMap<String, String> fieldMappings = new TreeMap<String, String>(){{
            put("course_code", "cache_course_code");
            put("occurrence_code", "cache_occurrence_code");
            put("period_code", "cache_semester_code");
            put("academic_year", "tt_academic_year");
            put("webct_active", "webct_active");
        }};
        final SortedSet<String> primaryKeyFields = new TreeSet<String>(){{
            addAll(fieldMappings.keySet());
            remove("webct_active");
        }};
        
        stagingDatabase.setAutoCommit(false);
        try {
            final PreparedStatement sourceStatement = eugexDatabase.prepareStatement(
                "SELECT VCL1_COURSE_CODE course_code, VCL2_COURSE_OCCURENCE occurrence_code, "
                        + "VCL3_COURSE_YEAR_CODE academic_year, VCL4_COURSE_PERIOD period_code, VCL13_WEBCT_ACTIVE webct_active "
                    + "FROM EUGEX_VLE_COURSES_VW "
                    + "ORDER BY VCL3_COURSE_YEAR_CODE, VCL1_COURSE_CODE, VCL2_COURSE_OCCURENCE, VCL4_COURSE_PERIOD"
                
            );
            try {
                final PreparedStatement destinationStatement = stagingDatabase.prepareStatement(
                    "SELECT tt_module_id, webct_active, cache_course_code, "
                            + "cache_occurrence_code, tt_academic_year, cache_semester_code "
                        + "FROM module "
                        + "WHERE cache_course_code IS NOT NULL "
                            + "AND tt_academic_year IS NOT NULL "
                        + "ORDER BY tt_academic_year, cache_course_code, cache_occurrence_code, cache_semester_code",
                    ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE
                );
                try {
                    final ResultSet sourceRs = sourceStatement.executeQuery();
                    try {
                        final ResultSet destinationRs = destinationStatement.executeQuery();
                        try {
                            this.cloneResultSet("module", sourceRs, destinationRs,
                                primaryKeyFields, fieldMappings, Mode.UPDATE_ONLY);
                        } finally {
                            destinationRs.close();
                        }
                    } finally {
                        sourceRs.close();
                    }
                } finally {
                    destinationStatement.close();
                }
            } finally {
                sourceStatement.close();
            }
            stagingDatabase.commit();
        } finally {
            // Roll back any uncommitted changes, then set autocommit on again.
            stagingDatabase.rollback();
            stagingDatabase.setAutoCommit(true);
        }
    }

    /**
     * Gets the data source for the EUGEX database.
     * 
     * @return the data source for the EUGEX database.
     */
    public DataSource getEugexDataSource() {
        return eugexDataSource;
    }

    /**
     * Gets the data source for the staging database.
     * 
     * @return the staging database data source.
     */
    public DataSource getStagingDataSource() {
        return stagingDataSource;
    }

    /**
     * Sets the EUGEX database data source.
     * 
     * @param eugexDataSource the EUGEX database data source to set.
     */
    public void setEugexDataSource(DataSource eugexDataSource) {
        this.eugexDataSource = eugexDataSource;
    }

    /**
     * Sets the staging database data source.
     * 
     * @param dataSource the staging database data source to set.
     */
    public void setStagingDataSource(DataSource dataSource) {
        this.stagingDataSource = dataSource;
    }
}
