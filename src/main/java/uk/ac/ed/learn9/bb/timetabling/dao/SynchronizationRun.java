package uk.ac.ed.learn9.bb.timetabling.dao;

import java.io.Serializable;
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
    private java.sql.Timestamp startTime;
    private java.sql.Timestamp cacheCopyCompleted;
    private java.sql.Timestamp diffCompleted;
    private java.sql.Timestamp endTime;

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
    public java.sql.Timestamp getStartTime() {
        return startTime;
    }

    /**
     * @return the time at which copying the data from the reporting database
     * was completed.
     */
    @Column(name="cache_copy_completed")
    @Temporal(value=TemporalType.TIMESTAMP)
    public java.sql.Timestamp getCacheCopyCompleted() {
        return cacheCopyCompleted;
    }

    /**
     * @param cacheCopyCompleted the cacheCopyCompleted to set
     */
    public void setCacheCopyCompleted(java.sql.Timestamp cacheCopyCompleted) {
        this.cacheCopyCompleted = cacheCopyCompleted;
    }

    /**
     * @return the diffCompleted
     */
    @Column(name="diff_completed")
    @Temporal(value=TemporalType.TIMESTAMP)
    public java.sql.Timestamp getDiffCompleted() {
        return diffCompleted;
    }

    /**
     * @param diffCompleted the diffCompleted to set
     */
    public void setDiffCompleted(java.sql.Timestamp diffCompleted) {
        this.diffCompleted = diffCompleted;
    }

    /**
     * @return the time when the synchronization completed.
     */
    @Column(name="end_time")
    @Temporal(value=TemporalType.TIMESTAMP)
    public java.sql.Timestamp getEndTime() {
        return endTime;
    }

    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(java.sql.Timestamp endTime) {
        this.endTime = endTime;
    }
}
