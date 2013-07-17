package uk.ac.ed.learn9.bb.timetabling.data;

import java.io.Serializable;
import java.math.BigDecimal;

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
    private Integer removeThresholdCount;

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
     * of records in the previous run, at which to abort the synchronisation
     * process.
     * 
     * @return the threshold percentage of remove operations in comparison to
     * number of records in the previous run. For example 0.5 would reflect 0.5%
     * of the number of enrolment records in the previous run.
     */
    @Column(name="REMOVE_THRESHOLD_PERCENT", nullable=true)
    public Float getRemoveThresholdPercent() {
        return removeThresholdPercent;
    }

    /**
     * Get the threshold number of remove operations at which to abort the
     * synchronisation process.
     * 
     * @return the threshold number of remove operations at which to abort the
     * synchronisation process.
     */
    @Column(name="REMOVE_THRESHOLD_COUNT", nullable=true)
    public Integer getRemoveThresholdCount() {
        return removeThresholdCount;
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
     * Set the threshold number of remove operations at which to abort the
     * synchronisation process.
     * 
     * @param newThresholdCount the threshold number of remove operations at
     * which to abort the synchronisation process.
     */
    public void setRemoveThresholdCount(final Integer newThresholdCount) {
        this.removeThresholdCount = newThresholdCount;
    }

    /**
     * Set the threshold percentage of remove operations in comparison to number
     * of records in the previous run, at which to abort the synchronisation
     * process.
     * 
     * @param newThresholdPercent the threshold percentage of remove operations
     * in comparison to number of records in the previous run.
     */
    public void setRemoveThresholdPercent(final Float newThresholdPercent) {
        this.removeThresholdPercent = newThresholdPercent;
    }

    /**
     * Set the threshold number of remove operations at which to abort the
     * synchronisation process.
     * 
     * @param newThresholdCount the threshold number of remove operations at
     * which to abort the synchronisation process.
     */
    public void setRemoveThresholdPercent(final BigDecimal newThresholdPercent) {
        if (null == newThresholdPercent) {
            this.removeThresholdPercent = null;
        } else {
            this.removeThresholdPercent = newThresholdPercent.floatValue();
        }
    }
}
