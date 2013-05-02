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

@Service
public class SynchronisationService extends Object {
    @Autowired
    private DataSource dataSource;
    @Autowired
    private DataSource rdbDataSource;
    @Autowired
    private TimetablingCloneService cloneService;
    @Autowired
    private SynchronisationRunDao runDao;

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
    
    public void syncModulesAndActivities()
            throws SQLException {
        final Connection source = this.getRdbDataSource().getConnection();

        try {
            final Connection destination = this.getDataSource().getConnection();

            try {
                this.cloneService.cloneModules(source, destination);
                this.cloneService.cloneActivities(source, destination);
            } finally {
                destination.close();
            }
        } finally {
            source.close();
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
     * Starts a new run of the synchronization process and returns the ID for
     * the run.
     *
     * @param destination the local database.
     * @return the ID for the new synchronization run.
     * @throws SQLException if there was a problem inserting the record.
     */
    public SynchronisationRun startNewRun(final Connection destination)
            throws SQLException {
        final int runId;
        final PreparedStatement statement = destination.prepareStatement("INSERT INTO synchronization_run "
                + "(previous_run_id, start_time) "
                + "(SELECT MAX(run_id), NOW() FROM synchronization_run WHERE end_time IS NOT NULL)");

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
     * @return the synchronisation run data access object.
     */
    public SynchronisationRunDao getRunDao() {
        return runDao;
    }

    /**
     * @param runDao the runDao to set
     */
    public void setRunDao(SynchronisationRunDao runDao) {
        this.runDao = runDao;
    }

    private void copyStudentSetActivities(final SynchronisationRun run,
        final Connection source, final Connection destination)
        throws SQLException {
        // Check the condition on this, I haven't had an opportunity to check
        // it with real data.
        final PreparedStatement sourceStatement = source.prepareStatement("SELECT A.ID ACTIVITY_ID, S.ID STUDENT_SET_ID "
            + "FROM ACTIVITY A "
            + "JOIN ACTIVITIES_STUDENTSET REL ON REL.ID=A.ID"
            + "JOIN STUDENT_SET S ON REL.STUDENT_SET=S.ID"
            + "WHERE SUBSTR(S.HOST_KEY, 0, 6)!='#SPLUS'");
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
     * @param run
     * @param connection
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
}
