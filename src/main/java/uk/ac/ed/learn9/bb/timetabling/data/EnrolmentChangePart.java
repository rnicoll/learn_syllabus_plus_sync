package uk.ac.ed.learn9.bb.timetabling.data;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Part of a change to be applied to Learn, from Timetabling. Each change can
 * have multiple parts in order to model multiple courses for a module.
 */
@Entity
@Table(name="enrolment_change_part")
public class EnrolmentChangePart extends Object implements Comparable<EnrolmentChangePart>, Serializable {
    private int partId;
    private EnrolmentChange change;
    private String learnCourseCode;
    private ChangeResult result;
    private Timestamp updateCompleted;

    @Override
    public int compareTo(final EnrolmentChangePart other) {
        if (null == this.getUpdateCompleted()) {
            if (null == other.getUpdateCompleted()) {
                return this.partId - other.partId;
            } else {
                return -1;
            }
        } else if (null == other.getUpdateCompleted()) {
            return 1;
        } else {
            int timeComparison = this.getUpdateCompleted().compareTo(other.getUpdateCompleted());
            
            if (0 == timeComparison) {
                return this.partId - other.partId;
            } else {
                return timeComparison;
            }
        }
    }
    
    @Override
    public boolean equals(final Object o) {
        if (o instanceof EnrolmentChangePart) {
            final EnrolmentChangePart other = (EnrolmentChangePart)o;
            
            return this.getPartId() == other.getPartId();
        }
        
        return false;
    }
    
    @Override
    public int hashCode() {
        return this.getUpdateCompleted().hashCode();
    }
    
    @Override
    public String toString() {
        return this.getChange().getChangeType() + " student "
            + this.getChange().getStudentSet().getHostKey() + " at "
            + this.getUpdateCompleted() + " to "
            + this.getChange().getActivity().getActivityName() + ".";
    }

    /**
     * @return the partId
     */
    @Id
    @Column(name="part_id", nullable=false)
    public int getPartId() {
        return partId;
    }

    /**
     * Get the enrolment change this part belongs to.
     * 
     * @return the change this part belongs to.
     */
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="change_id")
    public EnrolmentChange getChange() {
        return change;
    }

    /**
     * @return the Learn course code.
     */
    @Column(name="learn_course_code", nullable=false)
    public String getLearnCourseCode() {
        return learnCourseCode;
    }

    /**
     * Get the result of this change part.
     * 
     * @return the result of this change part.
     */
    @ManyToOne
    @JoinColumn(name="RESULT_CODE")
    public ChangeResult getResult() {
        return result;
    }
    
    /**
     * Returns a human readable string describing the outcome of this change.
     * 
     * @return 
     */
    @Transient
    public String getResultLabel() {
        if (null == this.getResult()) {
            return null;
        }
        return this.getResult().getLabel();
    }

    /**
     * Gets the time at which this change was completed.
     * 
     * @return the time at which this change was completed, or null if not
     * yet completed.
     */
    @Column(name="update_completed")
    public Timestamp getUpdateCompleted() {
        return updateCompleted;
    }

    /**
     * @param partId the partId to set
     */
    public void setPartId(int partId) {
        this.partId = partId;
    }

    /**
     * @param change the change to set
     */
    public void setChange(EnrolmentChange change) {
        this.change = change;
    }

    /**
     * @param learnCoursCode the learnCourseCode to set
     */
    public void setLearnCourseCode(String learnCourseCode) {
        this.learnCourseCode = learnCourseCode;
    }

    /**
     * @param result the result to set
     */
    public void setResult(ChangeResult result) {
        this.result = result;
    }

    /**
     * Sets the updated completed time.
     * 
     * @param updateCompleted the update completed timestamp to set.
     */
    public void setUpdateCompleted(final Timestamp updateCompleted) {
        this.updateCompleted = updateCompleted;
    }
}
