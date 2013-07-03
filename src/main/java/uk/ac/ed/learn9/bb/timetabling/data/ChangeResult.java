package uk.ac.ed.learn9.bb.timetabling.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * An outcome from an change to a student-group enrolment.
 */
@Entity
@Table(name="change_result")
public class ChangeResult {
    private String resultCode;
    private String label;
    private boolean retry;

    /**
     * @return the resultCode
     */
    @Column(name="RESULT_CODE", length=20)
    @Id
    public String getResultCode() {
        return resultCode;
    }

    /**
     * Get a human readable label for this result.
     * 
     * @return a human readable label for this result.
     */
    @Column(name="LABEL", length=80)
    public String getLabel() {
        return label;
    }

    /**
     * Get whether to retry changes with this result.
     * 
     * @return whether to retry changes with this result.
     */
    @Column(name="RETRY")
    public boolean isRetry() {
        return retry;
    }

    /**
     * @param resultCode the resultCode to set
     */
    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @param retry the retry to set
     */
    public void setRetry(boolean retry) {
        this.retry = retry;
    }
}
