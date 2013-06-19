package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.ed.learn9.bb.timetabling.data.LearnCourseCode;

/**
 * Service for communicating with the "merged courses" database.
 */
@Service
public class MergedCoursesService extends AbstractCloneService {
    private static final Logger logger = Logger.getLogger(BlackboardService.class);
    
    @Autowired
    private DataSource bblFeedsDataSource;
    @Autowired
    private DataSource stagingDataSource;
    
    /**
     * Fetches details of all courses that are merged together to form a
     * course. Where the given parent course doesn't have any courses
     * merged to create it, an empty list is returned.
     *
     * @param parentCourseCode the course code of the parent course to check
     * for courses merged into.
     * @return a collection of courses merged into the given course. Empty (not null)
     * if there are no merged courses under the given course.
     */
    public Collection<LearnCourseCode> getMergedCourses(final LearnCourseCode parentCourseCode)
        throws SQLException {
        assert null != parentCourseCode;
        
        final List<LearnCourseCode> childCourses = new ArrayList<LearnCourseCode>();
        
        final Connection bblFeedsDatabase = this.getBblFeedsDataSource().getConnection();
        try {
            final PreparedStatement statement = bblFeedsDatabase.prepareStatement(
                "SELECT SOURCECOURSEID || SOURCEINSTANCE source_course_code "
                    + "FROM WDF_SHAREDCOURSEINSTANCE "
                    + "WHERE ISERROR='0' "
                        + "AND COURSESOURCEID='EUCLID' "
                        + "AND TARGETSOURCEID='SYSTEM' "
                        + "AND TARGETCOURSEID=? "
                        + "AND TARGETINSTANCE=? ");
            try {
                int paramIdx = 1;
                
                statement.setString(paramIdx++, parentCourseCode.getEuclidCourseId());
                statement.setString(paramIdx++, parentCourseCode.getInstance());
                
                final ResultSet rs = statement.executeQuery();
                try {
                    while (rs.next()) {
                        childCourses.add(new LearnCourseCode(rs.getString("source_course_code")));
                    }
                } finally {
                    rs.close();
                }
            } finally {
                statement.close();
            }
        } finally {
            bblFeedsDatabase.close();
        }
        
        return childCourses;
    }
    
    /**
     * Synchronises details of merged courses from the BBL feeds database,
     * into the staging database.
     */
    public void synchroniseMergedCourses()
            throws SQLException {
        final Connection stagingDatabase = this.getStagingDataSource().getConnection();
        try {
            final Connection bblFeedsDatabase = this.getBblFeedsDataSource().getConnection();
            try {
                this.synchroniseMergedCourses(stagingDatabase, bblFeedsDatabase);
            } finally {
                bblFeedsDatabase.close();
            }
        } finally {
            stagingDatabase.close();
        }
    }

    /**
     * Synchronises details of merged courses from the BBL feeds database,
     * into the staging database.
     */
    private void synchroniseMergedCourses(final Connection stagingDatabase, final Connection bblFeedsDatabase)
            throws SQLException {
        final SortedMap<String, String> fieldMappings = new TreeMap<String, String>(){{
            put("SOURCECOURSEID", "learn_course_code");
            put("TARGETCOURSEID", "merge_course_code");
        }};
        final SortedSet<String> primaryKeyFields = new TreeSet<String>(){{
            add("learn_course_code");
        }};
        
        stagingDatabase.setAutoCommit(false);
        try {
            final PreparedStatement sourceStatement = bblFeedsDatabase.prepareStatement(
                "SELECT SOURCECOURSEID || SOURCEINSTANCE source_course_code, "
                    + "TARGETCOURSEID || TARGETINSTANCE target_course_code "
                    + "FROM WDF_SHAREDCOURSEINSTANCE "
                    + "WHERE ISERROR='0' "
                        + "AND COURSESOURCEID='EUCLID' "
                        + "AND TARGETSOURCEID='SYSTEM' "
                    + "ORDER BY SOURCECOURSEID"
            );
            try {
                final PreparedStatement destinationStatement = stagingDatabase.prepareStatement(
                    "SELECT learn_course_code, merge_course_code "
                        + "FROM module "
                        + "WHERE learn_course_code IS NOT NULL "
                        + "ORDER BY learn_course_code"
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
     * Get the BBL Feeds data source.
     * 
     * @return the BBL Feeds data source.
     */
    public DataSource getBblFeedsDataSource() {
        return bblFeedsDataSource;
    }

    /**
     * Set the BBL Feeds database data source.
     * 
     * @param dataSource the BBL Feeds database data source to set.
     */
    public void setBblFeedsDataSource(DataSource bblFeedsDataSource) {
        this.bblFeedsDataSource = bblFeedsDataSource;
    }
    
    /**
     * Get the data source for the staging database.
     * 
     * @return the staging database data source.
     */
    public DataSource getStagingDataSource() {
        return stagingDataSource;
    }

    /**
     * Set the staging database data source.
     * 
     * @param dataSource the staging database data source to set.
     */
    public void setStagingDataSource(DataSource stagingDataSource) {
        this.stagingDataSource = stagingDataSource;
    }
}
