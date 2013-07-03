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
    private ModuleCourse moduleCourse;
    private ChangeResult result;
    private Timestamp updateCompleted;

    @Override
    public int compareTo(final EnrolmentChangePart other) {
        // First sort any changes that are still pending, to the top of the
        // list
        if (null == this.getResult()) {
            if (null != other.getResult()) {
                return -1;
            }
        } else {
            if (null == other.getResult()) {
                return 1;
            } else {
                if (this.getResult().isRetry()) {
                    if (!other.getResult().isRetry()) {
                        return -1;
                    }
                } else {
                    if (other.getResult().isRetry()) {
                        return 1;
                    }
                }
            }
        }
        
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
     * Gets the human readable name of the activity this change is based on,
     * for example "Chemical Medicine Level 10/2".
     * 
     * @return an activity name.
     */
    @Transient
    public String getActivityName() {
        return this.getChange().getActivityName();
    }

    /**
     * Gets a human readable type for this change.
     * 
     * @return the type of change, such as "Add" or "Remove".
     */
    @Transient
    public String getChangeLabel() {
        return this.getChange().getChangeLabel();
    }
    
    /**
     * Gets the name of the group that this change relates to. Note that
     * this is the initial name, and does not reflect any renaming that
     * has taken place in Learn.
     * 
     * @return the name of the group that this change relates to.
     */
    @Transient
    public String getGroupName() {
        return this.getChange().getGroupName();
    }

    /**
     * Get the module-course this part relates to.
     * 
     * @return the module-course this part relates to.
     */
    @ManyToOne
    @JoinColumn(name="module_course_id", nullable=false)
    public ModuleCourse getModuleCourse() {
        return moduleCourse;
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
     * Returns the host key for the student set this change relates to, which
     * is the username of the student for all synchronisable student sets.
     * 
     * @return a username.
     */
    @Transient
    public String getUsername() {
        return this.getChange().getUsername();
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
     * Set the module-course this part relates to.
     * 
     * @param moduleCourse the module-course this part relates to.
     */
    public void setModuleCourse(final ModuleCourse moduleCourse) {
        this.moduleCourse = moduleCourse;
    }

    /**
     * Set the result for this part of the enrolment change.
     * 
     * @param result the result to set.
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
