package uk.ac.ed.learn9.bb.timetabling.data;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
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
    private int runId;
    private Date startTime;
    private Date cacheCopyCompleted;
    private Date diffCompleted;
    private Date endTime;

    /**
     * @return the ID for this run.
     */
    @Id
    @Column(name="run_id")
    public int getRunId() {
        return runId;
    }

    /**
     * @return the start time of the synchronisation.
     */
    @Column(name="start_time")
    @Temporal(value=TemporalType.TIMESTAMP)
    public Date getStartTime() {
        return startTime;
    }

    /**
     * @return the time at which copying the data from the reporting database
     * was completed.
     */
    @Column(name="cache_copy_completed")
    @Temporal(value=TemporalType.TIMESTAMP)
    public Date getCacheCopyCompleted() {
        return cacheCopyCompleted;
    }

    /**
     * @return the diffCompleted
     */
    @Column(name="diff_completed")
    @Temporal(value=TemporalType.TIMESTAMP)
    public Date getDiffCompleted() {
        return diffCompleted;
    }

    /**
     * @return the time when the synchronisation completed.
     */
    @Column(name="end_time")
    @Temporal(value=TemporalType.TIMESTAMP)
    public Date getEndTime() {
        return endTime;
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
