package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for monitoring which courses in Learn are synchronised from EUGEX.
 */
@Service
public class EugexService {
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
                    "SELECT tt_module_id, webct_active, staging_course_code course_code, "
                            + "staging_occurrence_code occurrence_code, tt_academic_year academic_year, "
                            + "staging_semester_code period_code "
                        + "FROM module "
                        + "WHERE staging_course_code IS NOT NULL "
                            + "AND tt_academic_year IS NOT NULL "
                        + "ORDER BY tt_academic_year, staging_course_code, staging_occurrence_code, staging_semester_code",
                    ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE
                );
                try {
                    final ResultSet sourceRs = sourceStatement.executeQuery();
                    try {
                        final ResultSet destinationRs = destinationStatement.executeQuery();
                        try {
                            this.doSynchroniseVleActiveCourses(destinationRs, sourceRs);
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
     * Does the actual copying of the WEBCT_ACTIVE field from EUGEX into the local
     * database.
     * @param destinationRs
     * @param sourceRs
     * @throws SQLException 
     */
    private void doSynchroniseVleActiveCourses(final ResultSet destinationRs, final ResultSet sourceRs)
        throws SQLException {
        CourseKey sourceCourse;
        
        if (sourceRs.next()) {
            sourceCourse = new CourseKey(sourceRs.getString("academic_year"),
                sourceRs.getString("course_code"), sourceRs.getString("occurrence_code"),
                sourceRs.getString("period_code"));
        } else {
            sourceCourse = null;
        }
        
        while (destinationRs.next()) {
            if (null == sourceCourse) {
                destinationRs.updateNull(2);
                destinationRs.updateRow();
                continue;
            }
            
            CourseKey destinationCourse = new CourseKey(destinationRs.getString("academic_year"),
                destinationRs.getString("course_code"), destinationRs.getString("occurrence_code"),
                destinationRs.getString("period_code"));
            
            int comparison = destinationCourse.compareTo(sourceCourse);
            
            // If we're too far ahead, continue through the source we find a match
            // or run out of data.
            while (comparison > 0) {
                if (sourceRs.next()) {
                    sourceCourse = new CourseKey(sourceRs.getString("academic_year"),
                        sourceRs.getString("course_code"), sourceRs.getString("occurrence_code"),
                        sourceRs.getString("period_code"));
                    comparison = destinationCourse.compareTo(sourceCourse);
                } else {
                    // End of data
                    destinationRs.updateNull(2);
                    destinationRs.updateRow();
                    sourceCourse = null;
                    break;
                }
            }
            
            if (comparison < 0) {
                // Not yet at a match, jump to the next row
                destinationRs.updateNull(2);
                destinationRs.updateRow();
                continue;
            } else if (comparison == 0) {
                destinationRs.updateString(2, sourceRs.getString("webct_active"));
                destinationRs.updateRow();
            }
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
    
    private static class CourseKey extends Object implements Comparable<CourseKey> {
        private final String ayrCode;
        private final String courseCode;
        private final String occurrenceCode;
        private final String periodCode;

        private             CourseKey(final String setAyrCode,
            final String setCourseCode, final String setOccurrenceCode, final String setPeriodCode) {
            this.ayrCode = setAyrCode;
            this.courseCode = setCourseCode;
            this.occurrenceCode = setOccurrenceCode;
            this.periodCode = setPeriodCode;
        }
        
        @Override
        public int compareTo(final CourseKey other) {
            if (this.ayrCode.equals(other.ayrCode)) {
                if (this.courseCode.equals(other.courseCode)) {
                    if (this.occurrenceCode.equals(other.occurrenceCode)) {
                        if (this.periodCode.equals(other.periodCode)) {
                            return 0;
                        } else {
                            return this.periodCode.compareTo(other.periodCode);
                        }
                    } else {
                        return this.occurrenceCode.compareTo(other.occurrenceCode);
                    }
                } else {
                    return this.courseCode.compareTo(other.courseCode);
                }
            } else {
                return this.ayrCode.compareTo(other.ayrCode);
            }
        }
        
        @Override
        public boolean equals(final Object o) {
            if (null == o) {
                return false;
            }
            
            if (!(o instanceof CourseKey)) {
                return false;
            }
            
            final CourseKey other = (CourseKey)o;
            
            return this.compareTo(other) == 0;
        }
        
        @Override
        public int hashCode() {
            int hash = 1;
            
            hash = hash * 31 + this.ayrCode.hashCode();
            hash = hash * 31 + this.courseCode.hashCode();
            hash = hash * 31 + this.occurrenceCode.hashCode();
            hash = hash * 31 + this.periodCode.hashCode();
            
            return hash;
        }
        
        /**
         * @return the ayrCode
         */
        public String getAyrCode() {
            return ayrCode;
        }

        /**
         * @return the courseCode
         */
        public String getCourseCode() {
            return courseCode;
        }

        /**
         * @return the occurrenceCode
         */
        public String getOccurrenceCode() {
            return occurrenceCode;
        }

        /**
         * @return the periodCode
         */
        public String getPeriodCode() {
            return periodCode;
        }
    }
}
