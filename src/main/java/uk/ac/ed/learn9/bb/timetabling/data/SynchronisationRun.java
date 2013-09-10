package uk.ac.ed.learn9.bb.timetabling.data;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTables;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * A record of a synchronisation process run.
 */
@Entity
@Table(name="synchronisation_run")
@SecondaryTables({
    @SecondaryTable(name="synchronisation_run_prev", pkJoinColumns={
        @PrimaryKeyJoinColumn(name="run_id", referencedColumnName="run_id") })
})
public class SynchronisationRun extends Object implements Comparable<SynchronisationRun>, Serializable {
    
    private int runId;
    private Integer previousRunId;
    private Date startTime;
    private Date cacheCopyCompleted;
    private Date diffCompleted;
    private Date endTime;
    private SynchronisationResult result;
    
    @Override
    public int compareTo(final SynchronisationRun other) {
        int startTimeCompare = this.getStartTime().compareTo(other.getStartTime());
        
        if (startTimeCompare != 0) {
            // Note that this is in descending order, as we want later runs first
            return 0 - startTimeCompare;
        } else {
            return this.getRunId() - other.getRunId();
        }
    }
    
    @Override
    public boolean equals(final Object o) {
        if (null == o
                || !(o instanceof SynchronisationRun)) {
            return false;
        }
        
        final SynchronisationRun other = (SynchronisationRun)o;
        
        return this.getRunId() == other.getRunId();
    }
    
    @Override
    public int hashCode() {
        return this.getRunId();
    }

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
     * @return the previousRunId
     */
    @Column(name="previous_run_id", table="synchronisation_run_prev", nullable=true)
    public Integer getPreviousRunId() {
        return previousRunId;
    }

    /**
     * @return the result
     */
    @Column(name="result_code")
    @Enumerated(EnumType.STRING)
    public SynchronisationResult getResult() {
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
     * @param previousRunId the previousRunId to set
     */
    public void setPreviousRunId(final Integer previousRunId) {
        this.previousRunId = previousRunId;
    }

    /**
     * @param result the result to set
     */
    public void setResult(SynchronisationResult result) {
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
