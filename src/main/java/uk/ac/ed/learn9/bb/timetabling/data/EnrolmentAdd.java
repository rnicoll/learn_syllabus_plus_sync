
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
 * Represents a change to enrolments, where a student is to be added to a group
 * in Learn.
 */
@Entity
@Table(name="enrolment_add")
public class EnrolmentAdd extends Object implements Serializable {
    private int changeId;
    private SynchronisationRun run;
    private Activity activity;
    private StudentSet studentSet;
    private Timestamp updateCompleted;

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
     * Returns the activity that the student is being added to.
     * 
     * @return the activity that the student is being added to.
     */
    @ManyToOne
    @JoinColumn(name="TT_ACTIVITY_ID")
    public Activity getActivity() {
        return activity;
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
