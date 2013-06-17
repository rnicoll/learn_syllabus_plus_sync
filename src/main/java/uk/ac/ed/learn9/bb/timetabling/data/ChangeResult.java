package uk.ac.ed.learn9.bb.timetabling.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author jnicoll2
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
     * @return the label
     */
    @Column(name="LABEL", length=80)
    public String getLabel() {
        return label;
    }

    /**
     * @return the retry
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
