
package uk.ac.ed.learn9.bb.timetabling.data;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Represents a change to enrolments, where a student is to be removed from
 * a group in Learn.
 */
@Entity
@Table(name="enrolment_add")
public class EnrolmentRemove extends Object implements EnrolmentChange, Serializable {
    public static final String CHANGE_TYPE = "Remove";
    
    private int changeId;
    private SynchronisationRun run;
    private Activity activity;
    private StudentSet studentSet;
    private Timestamp updateCompleted;

    @Override
    public int compareTo(final EnrolmentChange other) {
        return this.getUpdateCompleted().compareTo(other.getUpdateCompleted());
    }
    
    @Override
    public boolean equals(final Object o) {
        if (o instanceof EnrolmentRemove) {
            final EnrolmentRemove other = (EnrolmentRemove)o;
            
            return this.getChangeId() == other.getChangeId();
        }
        
        return false;
    }
    
    @Override
    public int hashCode() {
        return this.getUpdateCompleted().hashCode();
    }
    
    @Override
    public String toString() {
        return this.getChangeType() + " student "
            + this.getStudentSet().getHostKey() + " at "
            + this.getUpdateCompleted() + " to "
            + this.getActivity().getActivityName() + ".";
    }

    /**
     * Returns the ID for this change.
     * 
     * @return the ID of this change.
     */
    @Id
    @Column(name="change_id", nullable=false)
    public int getChangeId() {
        return changeId;
    }

    @Override
    public String getChangeType() {
        return CHANGE_TYPE;
    }

    /**
     * Returns the synchronisation run this change belongs to.
     * 
     * @return the synchronisation run this change belongs to.
     */
    @ManyToOne
    @JoinColumn(name="RUN_ID")
    @Override
    public SynchronisationRun getRun() {
        return run;
    }

    /**
     * Returns the activity that the student is being added to.
     * 
     * @return the activity that the student is being added to.
     */
    @ManyToOne
    @JoinColumn(name="TT_ACTIVITY_ID")
    @Override
    public Activity getActivity() {
        return activity;
    }

    /**
     * @return the studentSet
     */
    @ManyToOne
    @JoinColumn(name="TT_STUDENT_SET_ID")
    @Override
    public StudentSet getStudentSet() {
        return studentSet;
    }

    /**
     * @return the time at which this change was applied to Learn.
     */
    @Column(name="update_completed")
    @Override
    public Timestamp getUpdateCompleted() {
        return updateCompleted;
    }

    /**
     * @param changeId the changeId to set
     */
    public void setChangeId(int changeId) {
        this.changeId = changeId;
    }

    /**
     * @param run the run to set
     */
    public void setRun(SynchronisationRun run) {
        this.run = run;
    }

    /**
     * @param activity the activity to set
     */
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    /**
     * @param studentSet the studentSet to set
     */
    public void setStudentSet(StudentSet studentSet) {
        this.studentSet = studentSet;
    }

    /**
     * @param updateCompleted the updateCompleted to set
     */
    public void setUpdateCompleted(Timestamp updateCompleted) {
        this.updateCompleted = updateCompleted;
    }
}
