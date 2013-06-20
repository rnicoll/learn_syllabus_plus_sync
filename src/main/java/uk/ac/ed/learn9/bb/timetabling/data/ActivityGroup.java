package uk.ac.ed.learn9.bb.timetabling.data;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * A group in Learn, based on an activity in Timetabling.
 */
@Entity
@Table(name="activity_group")
public class ActivityGroup {
    private int activityGroupId;
    private Activity activity;
    private String learnCourseCode;
    private String learnGroupId;
    private Timestamp learnGroupCreated;

    /**
     * Get the ID for this activity-group relationship.
     * 
     * @return the ID for this activity-group relationship.
     */
    @Id
    @Column(name="activity_group_id", nullable=false)
    public int getActivityGroupId() {
        return activityGroupId;
    }

    /**
     * Get the activity this relationship refers to.
     * 
     * @return the activity this relationship refers to.
     */
    @ManyToOne
    @JoinColumn(name="tt_activity_id", nullable=false)
    public Activity getActivity() {
        return this.activity;
    }
    
    /**
     * The Learn course code for the course this group belongs to. 
     * 
     * @return the Learn course code for the course this group belongs to. 
     */
    @Column(name="learn_course_code", nullable=false)
    public String getLearnCourseCode() {
        return learnCourseCode;
    }


    /**
     * Gets the ID of the group in Learn that the activity maps to,
     * where applicable.
     * 
     * @return the ID of the group in Learn that the activity maps to,
     * or null if no mapping has been established yet.
     */
    @Column(name="learn_group_id", nullable=true, length=80)
    public String getLearnGroupId() {
        return learnGroupId;
    }

    /**
     * Get the time at which the group was created in Learn.
     * 
     * @return the time at which the group was created in Learn.
     */
    public Timestamp getLearnGroupCreated() {
        return learnGroupCreated;
    }
    
    public void setActivity(final Activity newActivity) {
        this.activity = newActivity;
    }

    /**
     * @param activityGroupId the activityGroupId to set
     */
    public void setActivityGroupId(int activityGroupId) {
        this.activityGroupId = activityGroupId;
    }

    /**
     * @param learnCourseCode the learnCourseCode to set
     */
    public void setLearnCourseCode(String learnCourseCode) {
        this.learnCourseCode = learnCourseCode;
    }

    /**
     * Set the ID of the group in Learn that the activity relates to.
     * 
     * @param learnGroupId the ID of the group in Learn that te activity
     * relates to.
     */
    public void setLearnGroupId(String learnGroupId) {
        this.learnGroupId = learnGroupId;
    }

    /**
     * @param learnGroupCreated the learnGroupCreated to set
     */
    public void setLearnGroupCreated(Timestamp learnGroupCreated) {
        this.learnGroupCreated = learnGroupCreated;
    }
    
}
