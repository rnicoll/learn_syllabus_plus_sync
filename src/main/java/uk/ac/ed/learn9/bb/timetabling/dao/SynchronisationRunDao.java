package uk.ac.ed.learn9.bb.timetabling.dao;

import uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun;

/**
 * Data access object for loading synchronisation run details from the staging
 * database.
 */
public interface SynchronisationRunDao {
    /**
     * Retrieves an synchronisation run by its ID.
     * 
     * @param runId the unique identifier for the run to load.
     * @return the synchronisation run.
     */
    public SynchronisationRun getRun(int runId);
}
