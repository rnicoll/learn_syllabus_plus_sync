package uk.ac.ed.learn9.bb.timetabling.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ed.learn9.bb.timetabling.dao.SynchronizationRun;

@Service
public class TimetablingDiffService extends Object {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private DataSource rdbDataSource;
    @Autowired
    private TimetablingCloneService cloneService;

    public SynchronizationRun generateDiff()
            throws SQLException {
        final Connection source = this.getRdbDataSource().getConnection();

        try {
            final Connection destination = this.getRdbDataSource().getConnection();

            try {
                final SynchronizationRun run = this.startNewRun(destination);

                return run;
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
    public SynchronizationRun startNewRun(final Connection destination)
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
        
        // FIXME: Load the synchronization run
        
        return null;
    }
}
