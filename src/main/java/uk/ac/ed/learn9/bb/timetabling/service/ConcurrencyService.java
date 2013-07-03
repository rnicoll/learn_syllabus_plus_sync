package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ed.learn9.bb.timetabling.dao.SynchronisationRunDao;
import uk.ac.ed.learn9.bb.timetabling.data.SynchronisationResult;
import uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun;

/**
 * Used to ensure each synchronisation run is performed serially (there are no
 * concurrent runs).
 */
@Service
public class ConcurrencyService {
    /**
     * How long to wait before assuming a synchronisation run has failed, and
     * mark it as timed out.
     */
    public static final long SYNCHRONISATION_TIMEOUT_MILLIS = 23 * 60 * 60 * 1000L;
    /**
     * Number of days to keep details of abandoned runs.
     */
    public static final int DAYS_KEEP_ABANDONED_RUNS = 1;
    
    @Autowired
    private DataSource stagingDataSource;
    @Autowired
    private SynchronisationRunDao runDao;
    
    /**
     * Marks a session as abandoned.
     * 
     * @param run the session to mark abandoned.
     * @return whether the change was written out successfully. Failure typically
     * indicates the session has already finished.
     * @throws SQLException if there was a problem communicating with the staging
     * database.
     */
    public boolean abandonSession(final SynchronisationRun run)
            throws SQLException {
        final Connection stagingDatabase = this.getStagingDataSource().getConnection();
        try {
            return this.abandonSession(stagingDatabase, run, new Timestamp(System.currentTimeMillis()));
        } finally {
            stagingDatabase.close();
        }
    }
    
    /**
     * Marks a session as abandoned.
     * 
     * @param stagingDatabase a connection to the staging database.
     * @param now the current time.
     * @param run the session to mark abandoned.
     * @return whether the change was written out successfully. Failure typically
     * indicates the session has already finished.
     * @throws SQLException if there was a problem communicating with the staging
     * database.
     */
    public boolean abandonSession(final Connection stagingDatabase, final SynchronisationRun run, final Timestamp now)
            throws SQLException {
        final PreparedStatement statement = stagingDatabase.prepareStatement(
            "UPDATE synchronisation_run "
                + "SET end_time=?, result_code=? "
                + "WHERE run_id=? AND end_time IS NULL"
            );
        try {
            int paramIdx = 1;
            
            statement.setTimestamp(paramIdx++, now);
            statement.setString(paramIdx++, SynchronisationResult.ABANDONED.name());
            statement.setInt(paramIdx++, run.getRunId());
            return statement.executeUpdate() > 0;
        } finally {
            statement.close();
        }
    }

    /**
     * Allocates a value for the previous synchronisation run that this run should
     * generate a difference set against.
     * 
     * @param stagingDatabase a connection to the staging database.
     * @param runId the ID of the synchronisation run to assign a previous
     * run to.
     * @return the ID of the previous run. May be null if this is the first
     * synchronisation run.
     * @throws SQLException if there was a problem communicating with the staging
     * database.
     * @throws ConcurrencyService.SynchronisationAlreadyInProgressException if
     * there's already a synchronisation run in progress.
     */
    private Integer assignPreviousRun(final Connection stagingDatabase, final int runId)
        throws SQLException, SynchronisationAlreadyInProgressException {
        PreparedStatement statement = stagingDatabase.prepareStatement(
            "INSERT INTO synchronisation_run_prev (run_id, previous_run_id) "
                + "(SELECT ?, MAX(run_id) "
                    + "FROM synchronisation_run "
                    + "WHERE run_id!=? "
                        + "AND (result_code IS NULL OR result_code!=?)"
                + ")");
        try {
            int paramIdx = 1;
            statement.setInt(paramIdx++, runId);
            statement.setInt(paramIdx++, runId);
            statement.setString(paramIdx++, SynchronisationResult.ABANDONED.name());
            // XXX: Handle constraint violation
            statement.executeUpdate();
        } finally {
            statement.close();
        }
        
        statement = stagingDatabase.prepareStatement("SELECT r.run_id, r.end_time "
            + "FROM synchronisation_run r "
                + "JOIN synchronisation_run_prev p ON p.previous_run_id=r.run_id "
            + "WHERE p.run_id=?");
        try {
            statement.setInt(1, runId);
            final ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                final int previousRunId = rs.getInt("run_id");
                final Timestamp endTime = rs.getTimestamp("end_time");
                
                if (null == endTime) {
                    throw new SynchronisationAlreadyInProgressException("The synchronisation run #"
                        + previousRunId + " is already in progress.");
                }
                
                return previousRunId;
            } else {
                // There is no previous run, so we don't have to check if it
                // has finished.
                return null;
            }
        } finally {
            statement.close();
        }
    }
    
    /**
     * Clears out records for old abandoned runs, to stop them from cluttering
     * the database unnecessarily. In this case "old" is defined as occurring
     * over a day ago.
     * 
     * @return the number of records deleted.
     * @throws SQLException if there was a problem communicating with the database.
     */
    public int clearAbandonedRuns()
            throws SQLException {
        final Connection stagingDatabase = this.getStagingDataSource().getConnection();

        try {
            final Calendar calendar = Calendar.getInstance();

            calendar.add(Calendar.DATE, -DAYS_KEEP_ABANDONED_RUNS);

            return clearAbandonedRuns(stagingDatabase, new Timestamp(calendar.getTimeInMillis()));
        } finally {
            stagingDatabase.close();
        }
    }
    
    /**
     * Clears out records for old abandoned runs, to stop them from cluttering
     * the database unnecessarily.
     * 
     * @param stagingDatabase a connection to the staging database.
     * @param before the earliest run end time to select.
     * @return the number of records deleted.
     * @throws SQLException if there was a problem communicating with the database.
     */
    public int clearAbandonedRuns(final Connection stagingDatabase, final Timestamp before)
            throws SQLException {
        final PreparedStatement statement = stagingDatabase.prepareStatement("DELETE FROM synchronisation_run "
            + "WHERE end_time<? AND result_code=?");
        try {
            int paramIdx = 1;
            statement.setTimestamp(paramIdx++, before);
            statement.setString(paramIdx++, SynchronisationResult.ABANDONED.name());
            return statement.executeUpdate();
        } finally {
            statement.close();
        }
    }
    
    /**
     * Gets the next ID for a synchronisation run, from the staging database.
     * 
     * @param stagingDatabase a connection to the staging database.
     * @return the next ID value.
     * @throws SQLException if there was a problem with the database.
     */
    private int getNextId(final Connection stagingDatabase) throws SQLException {
        final int runId;
        final PreparedStatement idStatement;
        
        if (stagingDatabase.getMetaData().getDatabaseProductName().equals("HSQL Database Engine")) {
            idStatement = stagingDatabase.prepareStatement("CALL NEXT VALUE FOR SYNCHRONISATION_RUN_SEQ;");
        } else {
            idStatement = stagingDatabase.prepareStatement("SELECT SYNCHRONISATION_RUN_SEQ.NEXTVAL FROM DUAL");
        }
        
        try {
            final ResultSet rs = idStatement.executeQuery();
            try {
                rs.next();
                runId = rs.getInt(1);
            } finally {
                rs.close();
            }
        } finally {
            idStatement.close();
        }
        
        return runId;
    }

    /**
     * Marks a session's cache copying stage as completed.
     * 
     * @param run the session to mark as having had its copying stage completed.
     * @return whether the change was written out successfully. Failure typically
     * indicates the session has already finished.
     * @throws SQLException if there was a problem communicating with the staging
     * database.
     */
    public boolean markCacheCopyCompleted(final SynchronisationRun run)
        throws SQLException {
        final Connection stagingDatabase = this.getStagingDataSource().getConnection();
        try {
            return this. markCacheCopyCompleted(stagingDatabase, run, new Timestamp(System.currentTimeMillis()));
        } finally {
            stagingDatabase.close();
        }
    }
    
    /**
     * Marks a session's cache copying stage as completed.
     * 
     * @param stagingDatabase a connection to the staging database.
     * @param now the current time.
     * @param run the session to mark as having had its copying stage
     * completed.
     * @return whether the change was written out successfully. Failure typically
     * indicates the session has already finished.
     * @throws SQLException if there was a problem communicating with the staging
     * database.
     */
    public boolean markCacheCopyCompleted(final Connection stagingDatabase, final SynchronisationRun run,
            final Timestamp now)
            throws SQLException {        
        final PreparedStatement statement = stagingDatabase.prepareStatement(
            "UPDATE synchronisation_run "
                + "SET cache_copy_completed=? "
                + "WHERE run_id=? AND end_time IS NULL"
            );
        try {
            int paramIdx = 1;
            
            statement.setTimestamp(paramIdx++, now);
            statement.setInt(paramIdx++, run.getRunId());
            if (statement.executeUpdate() > 0) {
                this.getRunDao().refresh(run);
                return true;
            }
        } finally {
            statement.close();
        }
        
        return false;
    }

    /**
     * Marks a session's difference generation stage as completed.
     * 
     * @param run the session to mark as having had its copying stage completed.
     * @return whether the change was written out successfully. Failure typically
     * indicates the session has already finished.
     * @throws SQLException if there was a problem communicating with the staging
     * database.
     */
    public boolean markDiffCompleted(final SynchronisationRun run)
        throws SQLException {
        final Connection stagingDatabase = this.getStagingDataSource().getConnection();
        try {
            return this. markDiffCompleted(stagingDatabase, run, new Timestamp(System.currentTimeMillis()));
        } finally {
            stagingDatabase.close();
        }
    }
    
    /**
     * Marks a session's difference generation stage as completed.
     * 
     * @param stagingDatabase a connection to the staging database.
     * @param now the current time.
     * @param run the session to mark as having had its difference generation
     * stage completed.
     * @return whether the change was written out successfully. Failure typically
     * indicates the session has already finished.
     * @throws SQLException if there was a problem communicating with the staging
     * database.
     */
    public boolean markDiffCompleted(final Connection stagingDatabase, final SynchronisationRun run,
            final Timestamp now)
            throws SQLException {        
        final PreparedStatement statement = stagingDatabase.prepareStatement(
            "UPDATE synchronisation_run "
                + "SET diff_completed=? "
                + "WHERE run_id=? AND end_time IS NULL"
            );
        try {
            int paramIdx = 1;
            
            statement.setTimestamp(paramIdx++, now);
            statement.setInt(paramIdx++, run.getRunId());
            if (statement.executeUpdate() > 0) {
                this.getRunDao().refresh(run);
                return true;
            }
        } finally {
            statement.close();
        }
        return false;
    }

    /**
     * Marks a session as ending in an error state.
     * 
     * @param run the session to mark as having succeeded.
     * @param cause the cause of the error.
     * @return whether the change was written out successfully. Failure typically
     * indicates the session has already finished.
     * @throws SQLException if there was a problem communicating with the staging
     * database.
     */
    public boolean markErrored(final SynchronisationRun run, final Throwable cause)
        throws SQLException {
        final Connection stagingDatabase = this.getStagingDataSource().getConnection();
        try {
            return this.markErrored(stagingDatabase, run, cause, new Timestamp(System.currentTimeMillis()));
        } finally {
            stagingDatabase.close();
        }
    }
    
    /**
     * Marks a session's cache copying stage as completed.
     * 
     * @param stagingDatabase a connection to the staging database.
     * @param run the session to mark as having succeeded.
     * @param cause the cause of the error.
     * @param now the current time.
     * @return whether the change was written out successfully. Failure typically
     * indicates the session has already finished.
     * @throws SQLException if there was a problem communicating with the staging
     * database.
     */
    public boolean markErrored(final Connection stagingDatabase, final SynchronisationRun run,
            final Throwable cause, final Timestamp now)
            throws SQLException {
        // XXX: Should log the error in the database.
        
        final PreparedStatement statement = stagingDatabase.prepareStatement(
            "UPDATE synchronisation_run "
                + "SET end_time=?, result_code=? "
                + "WHERE run_id=? AND end_time IS NULL"
            );
        try {
            int paramIdx = 1;
            
            statement.setTimestamp(paramIdx++, now);
            statement.setString(paramIdx++, SynchronisationResult.FATAL.name());
            statement.setInt(paramIdx++, run.getRunId());
            if (statement.executeUpdate() > 0) {
                this.getRunDao().refresh(run);
                return true;
            }
        } finally {
            statement.close();
        }
        
        return false;
    }

    /**
     * Marks a session's cache copying stage as completed.
     * 
     * @param run the session to mark as having succeeded.
     * @return whether the change was written out successfully. Failure typically
     * indicates the session has already finished.
     * @throws SQLException if there was a problem communicating with the staging
     * database.
     */
    public boolean markSucceeded(final SynchronisationRun run)
        throws SQLException {
        final Connection stagingDatabase = this.getStagingDataSource().getConnection();
        try {
            return this. markSucceeded(stagingDatabase, run, new Timestamp(System.currentTimeMillis()));
        } finally {
            stagingDatabase.close();
        }
    }
    
    /**
     * Marks a session's cache copying stage as completed.
     * 
     * @param stagingDatabase a connection to the staging database.
     * @param now the current time.
     * @param run the session to mark as having succeeded.
     * @return whether the change was written out successfully. Failure typically
     * indicates the session has already finished.
     * @throws SQLException if there was a problem communicating with the staging
     * database.
     */
    public boolean markSucceeded(final Connection stagingDatabase, final SynchronisationRun run, final Timestamp now)
            throws SQLException {
        final PreparedStatement statement = stagingDatabase.prepareStatement(
            "UPDATE synchronisation_run "
                + "SET end_time=?, result_code=? "
                + "WHERE run_id=? AND end_time IS NULL"
            );
        try {
            int paramIdx = 1;
            
            statement.setTimestamp(paramIdx++, now);
            statement.setString(paramIdx++, SynchronisationResult.SUCCESS.name());
            statement.setInt(paramIdx++, run.getRunId());
            if (statement.executeUpdate() > 0) {
                this.getRunDao().refresh(run);
                return true;
            }
        } finally {
            statement.close();
        }
        
        return false;
    }

    /**
     * Starts a new run of the synchronisation process and returns the ID for
     * the run.
     *
     * @return the ID for the new synchronisation run.
     * @throws SQLException if there was a problem inserting the record.
     * @throws ConcurrencyService.SynchronisationAlreadyInProgressException if
     * there's already a synchronisation run in progress.
     */
    public SynchronisationRun startNewRun()
            throws SynchronisationAlreadyInProgressException, SQLException {
        final int runId;
        final Timestamp now = new Timestamp(System.currentTimeMillis());
        final SynchronisationRun run;
        final Connection stagingDatabase = this.getStagingDataSource().getConnection();
        
        try {
            runId = this.getNextId(stagingDatabase);
            
            insertRunRecord(stagingDatabase, runId, now);
            run = this.getRunDao().getRun(runId);
            
            stagingDatabase.setAutoCommit(false);
            try {
                assignPreviousRun(stagingDatabase, runId);
            } catch(SynchronisationAlreadyInProgressException already) {
                // Roll back the assignment of a previous run.
                stagingDatabase.rollback();
                this.abandonSession(stagingDatabase, run, now);
                throw already;
            } finally {
                // Rollback any uncomitted changes in case of a serious
                // error.
                stagingDatabase.setAutoCommit(true);
            }
        } finally {
            stagingDatabase.close();
        }
        
        return run;
    }
    
    /**
     * Writes a record for a new synchronisation run starting, into the database.
     * 
     * @param stagingDatabase a connection to the staging database.
     * @param runId the ID of the new run.
     * @param now the current time.
     * @throws SQLException if there's a problem communicating with the database.
     */
    protected void insertRunRecord(final Connection stagingDatabase, final int runId,
            final Timestamp now)
        throws SQLException {
        final PreparedStatement insertStatement = stagingDatabase.prepareStatement(
            "INSERT INTO synchronisation_run "
                + "(run_id, start_time) "
                + "VALUES (?, ?)");
        try {
            insertStatement.setInt(1, runId);
            insertStatement.setTimestamp(2, now);
            insertStatement.executeUpdate();
        } finally {
            insertStatement.close();
        }
    }
    
    /**
     * Marks sessions that probably belong to crashed threads, as timed out.
     * 
     * @param stagingDatabase a connection to the staging database.
     * @param now the current time.
     * @return the number of sessions timed out.
     * @throws SQLException 
     */
    public int timeoutOldSessions(final Connection stagingDatabase,
            final Timestamp now)
            throws SQLException {
        final Timestamp timeout = new Timestamp(now.getTime() - SYNCHRONISATION_TIMEOUT_MILLIS);
        
        final PreparedStatement statement = stagingDatabase.prepareStatement(
            "UPDATE synchronisation_run "
                + "SET end_time=?, result_code=? "
                + "WHERE end_time IS NULL AND start_time<?"
            );
        try {
            int paramIdx = 1;
            
            statement.setTimestamp(paramIdx++, now);
            statement.setString(paramIdx++, SynchronisationResult.TIMEOUT.name());
            statement.setTimestamp(paramIdx++, timeout);
            return statement.executeUpdate();
        } finally {
            statement.close();
        }
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
     * Gets the DAO for synchronisation runs.
     * 
     * @return the DAO for synchronisation runs.
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

    /**
     * Sets the staging database data source.
     * 
     * @param dataSource the staging database data source to set.
     */
    public void setStagingDataSource(DataSource dataSource) {
        this.stagingDataSource = dataSource;
    }

    /**
     * Exception used to indicate that a synchronisation process cannot be
     * started, because it is already in progress.
     */
    public static class SynchronisationAlreadyInProgressException extends Exception {
        /**
         * Constructor for the exception with a message and no underlying cause.
         * 
         * @param message the message to report.
         */
        public SynchronisationAlreadyInProgressException(final String message) {
            super(message);
        }
        
        /**
         * Constructor for synchronisation concurrency errors that are caught
         * by SQL errors (for example unique constraint violation).
         * 
         * @param message the message to report.
         * @param cause the SQL exception that caused this exception.
         */
        public SynchronisationAlreadyInProgressException(final String message, final SQLException cause) {
            super(message, cause);
        }
    }
}
