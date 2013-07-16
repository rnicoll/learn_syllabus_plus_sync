package uk.ac.ed.learn9.bb.timetabling.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="CONFIGURATION")
public class Configuration extends Object implements Serializable {
    public static final int DEFAULT_CONFIGURATION_ID = 1;
    
    private int recordId;
    private Float removeThresholdPercent;

    public          Configuration() {
        
    }
    
    public          Configuration(final int setId) {
        this.recordId = setId;
    }

    /**
     * Get the unique ID for this configuration record. There should only ever
     * be one configuration, though, so this is mostly here to make Hibernate
     * happy.
     * 
     * @return the unique ID for this configuration record.
     */
    @Id
    @Column(name="RECORD_ID", nullable=false)
    public int getRecordId() {
        return recordId;
    }

    /**
     * Get the threshold percentage of remove operations in comparison to number
     * of records in the previous run, at which to about the synchronisation
     * process.
     * 
     * @return the the threshold percentage of remove operations in comparison to
     * number of records in the previous run. For example 0.5 would reflect 0.5%
     * of the number of enrolment records in the previous run.
     */
    @Column(name="REMOVE_THRESHOLD_PERCENT", nullable=true)
    public Float getRemoveThresholdPercent() {
        return removeThresholdPercent;
    }

    /**
     * Set the unique ID for this configuration record.
     * 
     * @param recordId the configuration record ID to set.
     */
    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    /**
     * @param removeThresholdPercent the removeThresholdPercent to set
     */
    public void setRemoveThresholdPercent(Float removeThresholdPercent) {
        this.removeThresholdPercent = removeThresholdPercent;
    }
}
