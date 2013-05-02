package uk.ac.ed.learn9.bb.timetabling.data;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="synchronization_run")
public class SynchronizationRun extends Object implements Serializable {
    private int runId;
    private int previousRunId;
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
     * @return the ID for the synchronization run this is diffed against.
     */
    @Column(name="previous_run_id")
    public int getPreviousRunId() {
        return previousRunId;
    }

    /**
     * @return the start time of the synchronization.
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
     * @return the time when the synchronization completed.
     */
    @Column(name="end_time")
    @Temporal(value=TemporalType.TIMESTAMP)
    public Date getEndTime() {
        return endTime;
    }

    /**
     * @param cacheCopyCompleted the cacheCopyCompleted to set
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
     * @param newPreviousRunId
     */
    public void setPreviousRunId(final int newPreviousRunId) {
        this.previousRunId = newPreviousRunId;
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
