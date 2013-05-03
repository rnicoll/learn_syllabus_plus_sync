package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ed.learn9.bb.timetabling.dao.SynchronisationRunDao;
import uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun;

/**
 * Service for synchronising activities and enrolments from Timetabling
 * to groups in Learn.
 */
@Service
public class SynchronisationService extends Object {
    @Autowired
    private DataSource dataSource;
    @Autowired
    private DataSource rdbDataSource;
    @Autowired
    private MergedCoursesService mergedCoursesService;
    @Autowired
    private TimetablingCloneService cloneService;
    @Autowired
    private SynchronisationRunDao runDao;

    /**
     * Generates an up to date difference set between the last time the
     * synchronisation service ran, and now.
     * 
     * @return the synchronisation run the difference set has been attributed
     * to. This is used to identify a change set independently of other change
     * sets generated at other points.
     * @throws SQLException 
     */
    public SynchronisationRun generateDiff()
            throws SQLException {
        final Connection source = this.getRdbDataSource().getConnection();

        try {
            final Connection destination = this.getDataSource().getConnection();

            try {
                final SynchronisationRun run = this.startNewRun(destination);
                
                this.copyStudentSetActivities(run, source, destination);
                this.doGenerateDiff(run, destination);
                
                return run;
            } finally {
                destination.close();
            }
        } finally {
            source.close();
        }
    }
    
    /**
     * Resolves the modules that activities belong to, to the courses they
     * represent in Learn, where applicable. This also includes importing
     * details of joint taught activities (so that the child activities can be
     * mapped to the correct module and then onwards to the correct course).
     * 
     * @throws SQLException if there was a problem access one of the databases.
     */
    public void mapModulesToCourses()
            throws SQLException {
        final Connection destination = this.getDataSource().getConnection();

        try {
            final Connection source = this.getRdbDataSource().getConnection();

            try {
                this.getCloneService().importJtaDetails(source, destination);
                this.refreshLearnCourseCodes(destination);
                // this.getMergedCoursesService().resolveMergedCourses(destination);
            } finally {
                source.close();
            }
        } finally {
            destination.close();
        }
    }
    
    public void synchroniseData()
            throws SQLException {
        final Connection source = this.getRdbDataSource().getConnection();

        try {
            final Connection destination = this.getDataSource().getConnection();

            try {
                this.cloneService.cloneModules(source, destination);
                this.cloneService.cloneActivities(source, destination);
                this.cloneService.cloneStudentSets(source, destination);
            } finally {
                destination.close();
            }
        } finally {
            source.close();
        }
    }

    /**
     * Starts a new run of the synchronisation process and returns the ID for
     * the run.
     *
     * @param destination the local database.
     * @return the ID for the new synchronisation run.
     * @throws SQLException if there was a problem inserting the record.
     */
    public SynchronisationRun startNewRun(final Connection destination)
            throws SQLException {
        final int runId;
        final PreparedStatement statement = destination.prepareStatement("INSERT INTO synchronisation_run "
                + "(previous_run_id, start_time) "
                + "(SELECT MAX(run_id), NOW() FROM synchronisation_run WHERE end_time IS NOT NULL)",
                PreparedStatement.RETURN_GENERATED_KEYS);

        try {
            statement.executeUpdate();

            final ResultSet rs = statement.getGeneratedKeys();
            try {
                rs.next();
                runId = rs.getInt(1);
            } finally {
                rs.close();
            }
        } finally {
            statement.close();
        }
        
        return this.getRunDao().getRun(runId);
    }

    /**
     * Copies student set/activity relationships to be synchronised to Learn,
     * from the reporting database. This filters out variant activities as
     * well as whole-course student sets, but does not provide filtering on
     * courses to be mapped to Learn (from SITS).
     */
    private void copyStudentSetActivities(final SynchronisationRun run,
        final Connection source, final Connection destination)
        throws SQLException {
        // Check the condition on this, I haven't had an opportunity to check
        // it with real data.
        final PreparedStatement sourceStatement = source.prepareStatement("SELECT DISTINCT A.ID ACTIVITY_ID, S.ID STUDENT_SET_ID "
            + "FROM ACTIVITY A "
                + "JOIN ACTIVITIES_STUDENTSET REL ON REL.ID=A.ID "
                + "JOIN STUDENT_SET S ON REL.STUDENT_SET=S.ID "
                + "LEFT JOIN VARIANTJTAACTS V ON V.ID=A.ID "
            + "WHERE SUBSTR(S.HOST_KEY, 0, 6)!='#SPLUS' "
                + "AND (V.ISVARIANTCHILD IS NULL OR V.ISVARIANTCHILD='0')");
        try {
            final PreparedStatement destinationStatement = destination.prepareStatement("INSERT INTO cache_enrolment "
                + "(run_id, tt_student_set_id, tt_activity_id) "
                + "VALUES (?, ?, ?)");
            try {
                
                final ResultSet rs = sourceStatement.executeQuery();
                try {
                    while (rs.next()) {
                        destinationStatement.setInt(1, run.getRunId());
                        destinationStatement.setString(2, rs.getString("STUDENT_SET_ID"));
                        destinationStatement.setString(3, rs.getString("ACTIVITY_ID"));
                        destinationStatement.executeUpdate();
                    }
                } finally {
                    rs.close();
                }
            } finally {
                destinationStatement.close();
            }
        } finally {
            sourceStatement.close();
        }
    }

    /**
     * Does the actual production of differences between the last time the process
     * ran, and this time.
     * 
     * @param run the synchronisation run to attribute changes to.
     * @param connection a connection to the cache database.
     */
    private void doGenerateDiff(final SynchronisationRun run, final Connection connection)
        throws SQLException {
        // We generate a difference list in the database using two very similar
        // statements; the first retrieves associations only present in the
        // most recent data sync, the last only associations in the previous
        // version.
        
        final PreparedStatement addStatement = connection.prepareStatement(
            "INSERT INTO enrolment_add "
                + "(run_id, tt_student_set_id, tt_activity_id) "
                + "(SELECT a.run_id, a.tt_student_set_id, a.tt_activity_id "
                    + "FROM synchronisation_run r "
                    + "JOIN cache_enrolment a ON a.run_id=r.run_id "
                    + "LEFT JOIN cache_enrolment b ON b.run_id=r.previous_run_id "
                        + "AND b.tt_student_set_id=a.tt_student_set_id "
                        + "AND b.tt_activity_id=a.tt_activity_id "
                    + "WHERE r.run_id=? "
                        + "AND b.run_id IS NULL)"
        );
        try {
            addStatement.setInt(1, run.getRunId());
            addStatement.executeUpdate();
        } finally {
            addStatement.close();
        }
        
        // XXX: This should check for a previously successful "add" operation
        final PreparedStatement removeStatement = connection.prepareStatement(
            "INSERT INTO enrolment_remove "
                + "(run_id, tt_student_set_id, tt_activity_id) "
                + "(SELECT a.run_id, a.tt_student_set_id, a.tt_activity_id "
                    + "FROM synchronisation_run r "
                    + "JOIN cache_enrolment a ON a.run_id=r.previous_run_id "
                    + "LEFT JOIN cache_enrolment b ON b.run_id=r.run_id "
                        + "AND b.tt_student_set_id=a.tt_student_set_id "
                        + "AND b.tt_activity_id=a.tt_activity_id "
                    + "WHERE r.run_id=? "
                        + "AND b.run_id IS NULL)"
        );
        try {
            removeStatement.setInt(1, run.getRunId());
            removeStatement.executeUpdate();
        } finally {
            removeStatement.close();
        }
    }

    /**
     * Recreates predicted Learn course codes based on the course codes listed
     * against modules.
     * 
     * @param connection a connection to the cache database.
     */
    private void refreshLearnCourseCodes(final Connection destination)
        throws SQLException {
        final PreparedStatement statement = destination.prepareStatement(
                "SELECT tt_module_id, tt_course_code, tt_academic_year, learn_course_code "
                    + "FROM module "
                    + "WHERE learn_course_id IS NULL",
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        try {
            final ResultSet rs = statement.executeQuery();
            try {
                while (rs.next()) {
                    final String courseCode = rs.getString("tt_course_code");
                    
                    // Split the course code into the course, semester and occurrence
                    final String[] courseCodeParts = courseCode.split("_");
                    
                    if (courseCodeParts.length != 3) {
                        continue;
                    }
                    
                    // Rewrite the URL into the form used in Learn course codes
                    final String academicYear = rs.getString("tt_academic_year");
                    
                    if (null == academicYear) {
                        // Without an academic year, we don't know when this course ran
                        continue;
                    }
                    
                    final String learnAyr = academicYear.replace('/', '-');
                    
                    // Build the Learn course code from the parts of the original
                    // course code, with the academic year added in.
                    final String learnCourseCode = courseCodeParts[0]
                        + learnAyr + courseCodeParts[1] + courseCodeParts[2];
                    
                    rs.updateString("learn_course_code", learnCourseCode);
                    rs.updateRow();
                }
            } finally {
                rs.close();
            }
        } finally {
            statement.close();
        }
    }

    /**
     * Returns the reporting database data source.
     *
     * @return the reporting database data source.
     */
    public DataSource getRdbDataSource() {
        return rdbDataSource;
    }

    /**
     * Sets the reporting database data source.
     * 
     * @param rdbDataSource the reporting database data source to set.
     */
    public void setRdbDataSource(DataSource rdbDataSource) {
        this.rdbDataSource = rdbDataSource;
    }

    /**
     * @return the local database data source.
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * @param dataSource the local database data source to set.
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * @return the cloneService
     */
    public TimetablingCloneService getCloneService() {
        return cloneService;
    }

    /**
     * @param cloneService the cloneService to set
     */
    public void setCloneService(TimetablingCloneService cloneService) {
        this.cloneService = cloneService;
    }

    /**
     * @return the mergedCoursesService
     */
    public MergedCoursesService getMergedCoursesService() {
        return mergedCoursesService;
    }

    /**
     * @param mergedCoursesService the mergedCoursesService to set
     */
    public void setMergedCoursesService(MergedCoursesService mergedCoursesService) {
        this.mergedCoursesService = mergedCoursesService;
    }

    /**
     * @return the synchronisation run data access object.
     */
    public SynchronisationRunDao getRunDao() {
        return runDao;
    }

    /**
     * @param runDao the runDao to set
     */
    public void setRunDao(final SynchronisationRunDao newRunDao) {
        this.runDao = newRunDao;
    }
}
