
package uk.ac.ed.learn9.bb.timetabling.data.cache;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Represents a change to enrolments, where a student is to be added to a group
 * in Learn.
 */
@Entity
@Table(name="enrolment_add")
public class EnrolmentChange extends Object implements Comparable<EnrolmentChange>, Serializable {
    public static final String CHANGE_TYPE_ADD = "add";
    public static final String CHANGE_TYPE_REMOVE = "remove";
    
    private int changeId;
    private String changeType;
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
        if (o instanceof EnrolmentChange) {
            final EnrolmentChange other = (EnrolmentChange)o;
            
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

    /**
     * Returns the activity that the student is being added to.
     * 
     * @return the activity that the student is being added to.
     */
    @ManyToOne
    @JoinColumn(name="TT_ACTIVITY_ID")
    public Activity getActivity() {
        return activity;
    }
    
    @Transient
    public String getActivityName() {
        return this.getActivity().getActivityName();
    }

    @Column(name="change_type", nullable=false, length=12)
    public String getChangeType() {
        return this.changeType;
    }
    
    @Transient
    public String getGroupName() {
        return this.getActivity().getLearnGroupName();
    }

    /**
     * Returns the synchronisation run this change belongs to.
     * 
     * @return the synchronisation run this change belongs to.
     */
    @ManyToOne
    @JoinColumn(name="RUN_ID")
    public SynchronisationRun getRun() {
        return run;
    }

    /**
     * @return the studentSet
     */
    @ManyToOne
    @JoinColumn(name="TT_STUDENT_SET_ID")
    public StudentSet getStudentSet() {
        return studentSet;
    }

    /**
     * @return the time at which this change was applied to Learn.
     */
    @Column(name="update_completed")
    public Timestamp getUpdateCompleted() {
        return updateCompleted;
    }
    
    @Transient
    public String getUserName() {
        return this.getStudentSet().getHostKey();
    }

    /**
     * @param activity the activity to set
     */
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    /**
     * @param changeId the changeId to set
     */
    public void setChangeId(int changeId) {
        this.changeId = changeId;
    }

    public void setChangeType(final String newChangeType) {
        this.changeType = newChangeType;
    }
    
    /**
     * @param run the run to set
     */
    public void setRun(SynchronisationRun run) {
        this.run = run;
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
