package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ed.learn9.bb.timetabling.dao.SynchronisationRunDao;
import uk.ac.ed.learn9.bb.timetabling.data.cache.SynchronisationRun;

/**
 * Used to ensure each synchronisation run is performed serially (there are no
 * concurrent runs).
 */
@Service
public class ConcurrencyService {
    public static final String RESULT_CODE_TIMEOUT = "timeout";
    public static final long SESSION_TIMEOUT = 23 * 60 * 60 * 1000L;
    
    @Autowired
    private DataSource cacheDataSource;
    @Autowired
    private SynchronisationRunDao runDao;

    private void assignPreviousRun(final Connection cacheDatabase, final int runId)
        throws SQLException, SynchronisationAlreadyInProgressException {
        PreparedStatement statement = cacheDatabase.prepareStatement(
            "INSERT INTO synchronisation_run_prev (run_id, previous_run_id) "
                + "(SELECT ?, MAX(run_id) FROM synchronisation_run WHERE run_id!=?)");
        try {
            statement.setInt(1, runId);
            statement.setInt(2, runId);
            // XXX: Handle constraint violation
            statement.executeUpdate();
        } finally {
            statement.close();
        }
        
        cacheDatabase.commit();
        
        statement = cacheDatabase.prepareStatement("SELECT r.run_id, r.end_time "
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
            } else {
                // There is no previous run, so we don't have to check if it
                // has finished.
            }
        } finally {
            statement.close();
        }
    }
    
    private int getNextId(final Connection cacheDatabase) throws SQLException {
        final int runId;
        final PreparedStatement idStatement;
        
        if (cacheDatabase.getMetaData().getDatabaseProductName().equals("HSQL Database Engine")) {
            idStatement = cacheDatabase.prepareStatement("CALL NEXT VALUE FOR SYNCHRONISATION_RUN_SEQ;");
        } else {
            idStatement = cacheDatabase.prepareStatement("SELECT SYNCHRONISATION_RUN_SEQ.NEXTVAL FROM DUAL");
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
     * Starts a new run of the synchronisation process and returns the ID for
     * the run.
     *
     * @param destination the local database.
     * @return the ID for the new synchronisation run.
     * @throws SQLException if there was a problem inserting the record.
     */
    public SynchronisationRun startNewRun()
            throws SynchronisationAlreadyInProgressException, SQLException {
        final int runId;
        final Timestamp now = new Timestamp(System.currentTimeMillis());
        final Connection cacheDatabase = this.getCacheDataSource().getConnection();
        
        try {
            runId = this.getNextId(cacheDatabase);
            
            cacheDatabase.setAutoCommit(false);
            try {
                insertRunRecord(cacheDatabase, runId, now);
                assignPreviousRun(cacheDatabase, runId);
                cacheDatabase.commit();
            } finally {
                cacheDatabase.rollback();
                cacheDatabase.setAutoCommit(true);
            }
        } finally {
            cacheDatabase.close();
        }
        
        return this.getRunDao().getRun(runId);
    }
    
    private void insertRunRecord(final Connection cacheDatabase, final int runId,
            final Timestamp now)
        throws SQLException {
        final PreparedStatement insertStatement = cacheDatabase.prepareStatement(
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
     * @param cacheDatabase
     * @param now the current time.
     * @throws SQLException 
     */
    public void timeoutOldSessions(final Connection cacheDatabase,
            final Timestamp now)
            throws SQLException {
        final Timestamp timeout = new Timestamp(now.getTime() - SESSION_TIMEOUT);
        
        final PreparedStatement statement = cacheDatabase.prepareStatement(
            "UPDATE synchronisation_run "
                + "SET end_time=?, result_code=? "
                + "WHERE end_time IS NULL AND start_time<?"
            );
        try {
            int paramIdx = 1;
            
            statement.setTimestamp(paramIdx++, now);
            statement.setString(paramIdx++, RESULT_CODE_TIMEOUT);
            statement.setTimestamp(paramIdx++, timeout);
        } finally {
            statement.close();
        }
    }

    /**
     * @return the cacheDataSource
     */
    public DataSource getCacheDataSource() {
        return cacheDataSource;
    }

    /**
     * @return the runDao
     */
    public SynchronisationRunDao getRunDao() {
        return runDao;
    }

    /**
     * @param cacheDataSource the cacheDataSource to set
     */
    public void setCacheDataSource(DataSource cacheDataSource) {
        this.cacheDataSource = cacheDataSource;
    }

    /**
     * @param runDao the runDao to set
     */
    public void setRunDao(SynchronisationRunDao runDao) {
        this.runDao = runDao;
    }

    /**
     * Exception used to indicate that a synchronisation process cannot be
     * started, because it is already in progress.
     */
    public static class SynchronisationAlreadyInProgressException extends Exception {
        public SynchronisationAlreadyInProgressException(final String message) {
            super(message);
        }
        
        public SynchronisationAlreadyInProgressException(final String message, final SQLException cause) {
            super(message, cause);
        }
    }
}
