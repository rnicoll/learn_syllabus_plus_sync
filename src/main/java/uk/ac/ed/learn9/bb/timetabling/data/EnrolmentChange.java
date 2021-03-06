package uk.ac.ed.learn9.bb.timetabling.data;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * A change to enrolments, where a student is to be added to a group
 * in Learn.
 */
@Entity
@Table(name="enrolment_change")
public class EnrolmentChange extends Object implements Serializable {
    /**
     * The type of change this record relates to.
     */
    public enum Type {
        /** Indicates that the change is to add a student to a group. */
        ADD("Add"),
        /** Indicates that the change is to remove a student from a group. */
        REMOVE("Remove");
        
        private final String label;
        
        Type(final String setLabel) {
            this.label = setLabel;
        }
        
        @Override
        public String toString() {
            return this.label;
        }
    }
    
    private int changeId;
    private Type changeType;
    private SynchronisationRun run;
    private Activity activity;
    private StudentSet studentSet;
    private List<EnrolmentChangePart> parts;
    
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
        return this.getChangeId();
    }
    
    @Override
    public String toString() {
        return this.getChangeType() + " student "
            + this.getStudentSet().getHostKey() + " to "
            + this.getActivity().getActivityName() + ".";
    }
    
    /**
     * Gets the ID for this change.
     * 
     * @return the ID of this change.
     */
    @Id
    @Column(name="change_id", nullable=false)
    public int getChangeId() {
        return changeId;
    }

    /**
     * Gets the activity that the student is being added to.
     * 
     * @return the activity that the student is being added to.
     */
    @ManyToOne
    @JoinColumn(name="TT_ACTIVITY_ID")
    public Activity getActivity() {
        return activity;
    }
    
    /**
     * Gets the human readable name of the activity this change is based on,
     * for example "Chemical Medicine Level 10/2".
     * 
     * @return an activity name.
     */
    @Transient
    public String getActivityName() {
        return this.getActivity().getActivityName();
    }

    /**
     * Gets a human readable type for this change.
     * 
     * @return the type of change, such as "Add" or "Remove".
     */
    @Transient
    public String getChangeLabel() {
        return this.changeType.toString();
    }

    /**
     * Gets the type of change this record contains.
     * 
     * @return the type of change.
     */
    @Column(name="change_type", nullable=false, length=12)
    @Enumerated(EnumType.STRING)
    public Type getChangeType() {
        return this.changeType;
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
        return this.getActivity().getLearnGroupName();
    }

    /**
     * Gets the parts of this change set.
     * 
     * @return the parts
     */
    @OneToMany(mappedBy="change")
    public List<EnrolmentChangePart> getParts() {
        return parts;
    }

    /**
     * Gets the synchronisation run this change belongs to.
     * 
     * @return the synchronisation run this change belongs to.
     */
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="RUN_ID")
    public SynchronisationRun getRun() {
        return run;
    }

    /**
     * Gets the student set this change relates to.
     * 
     * @return the student set this change relates to.
     */
    @ManyToOne
    @JoinColumn(name="TT_STUDENT_SET_ID")
    public StudentSet getStudentSet() {
        return studentSet;
    }
    
    /**
     * Returns the host key for the student set this change relates to, which
     * is the username of the student for all synchronisable student sets.
     * 
     * @return a username.
     */
    @Transient
    public String getUsername() {
        return this.getStudentSet().getHostKey();
    }

    /**
     * Sets the activity this change relates to.
     * 
     * @param activity the activity to set.
     */
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    /**
     * Sets the ID for this change.
     * 
     * @param changeId the ID to set.
     */
    public void setChangeId(int changeId) {
        this.changeId = changeId;
    }

    /**
     * Sets the type of this change (add/remove).
     * 
     * @param newChangeType the change type to set.
     */
    public void setChangeType(final Type newChangeType) {
        this.changeType = newChangeType;
    }

    /**
     * @param parts the parts to set
     */
    public void setParts(List<EnrolmentChangePart> parts) {
        this.parts = parts;
    }
    
    /**
     * @param run the run to set.
     */
    public void setRun(SynchronisationRun run) {
        this.run = run;
    }

    /**
     * @param studentSet the student set to set.
     */
    public void setStudentSet(StudentSet studentSet) {
        this.studentSet = studentSet;
    }
}
