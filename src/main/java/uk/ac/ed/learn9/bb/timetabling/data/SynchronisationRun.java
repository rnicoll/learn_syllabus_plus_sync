package uk.ac.ed.learn9.bb.timetabling.data;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * A record of a synchronisation process run.
 */
@Entity
@Table(name="synchronisation_run")
public class SynchronisationRun extends Object implements Serializable {
    /**
     * Result codes for the possible outcomes of running a synchronisation
     * process.
     */
    public enum Result {
        ABANDONED,
        FATAL,
        SUCCESS,
        TIMEOUT;
    }
    
    private int runId;
    private Date startTime;
    private Date cacheCopyCompleted;
    private Date diffCompleted;
    private Date endTime;
    private Result result;

    /**
     * Gets the ID for this process run.
     * 
     * @return the ID for this run.
     */
    @Id
    @Column(name="run_id")
    public int getRunId() {
        return runId;
    }

    /**
     * Gets the start time of the synchronisation.
     * 
     * @return the start time of the synchronisation.
     */
    @Column(name="start_time")
    @Temporal(value=TemporalType.TIMESTAMP)
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Gets the time at which copying the data from the reporting database
     * was completed.
     * 
     * @return the time at which copying the data from the reporting database
     * was completed, or null if not yet completed.
     */
    @Column(name="cache_copy_completed")
    @Temporal(value=TemporalType.TIMESTAMP)
    public Date getCacheCopyCompleted() {
        return cacheCopyCompleted;
    }

    /**
     * Gets the time at which the difference generation between the previous
     * run, and this, was completed.
     * 
     * @return the time at which the difference generation between the previous
     * run, and this, was completed, or null if not yet completed.
     */
    @Column(name="diff_completed")
    @Temporal(value=TemporalType.TIMESTAMP)
    public Date getDiffCompleted() {
        return diffCompleted;
    }

    /**
     * Gets the time when the synchronisation completed.
     * 
     * @return the time when the synchronisation completed, or null if not yet
     * completed.
     */
    @Column(name="end_time")
    @Temporal(value=TemporalType.TIMESTAMP)
    public Date getEndTime() {
        return endTime;
    }

    /**
     * @return the result
     */
    @Column(name="result_code")
    @Enumerated(EnumType.STRING)
    public Result getResult() {
        return result;
    }

    /**
     * @param cacheCopyCompleted the time when the cache copy finished, to set.
     */
    public void setCacheCopyCompleted(Date cacheCopyCompleted) {
        this.cacheCopyCompleted = cacheCopyCompleted;
    }

    /**
     * @param diffCompleted the diffCompleted to set
     */
    public void setDiffCompleted(Date diffCompleted) {
        this.diffCompleted = diffCompleted;
    }

    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * @param result the result to set
     */
    public void setResult(Result result) {
        this.result = result;
    }

    /**
     * @param newRunId
     */
    public void setRunId(final int newRunId) {
        this.runId = newRunId;
    }

    /**
     * @param newStartTime
     */
    public void setStartTime(final Date newStartTime) {
        this.startTime = newStartTime;
    }
}
