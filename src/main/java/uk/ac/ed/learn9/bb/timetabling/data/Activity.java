package uk.ac.ed.learn9.bb.timetabling.data;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Represents an activity in timetabling, which in some cases may be mapped to a
 * group in Learn.
 */
@Entity
@Table(name="activity")
public class Activity extends Object implements Serializable {
    private String activityId;
    private String activityName;
    private Module module;
    private String learnGroupId;
    private String learnGroupName;
    private String description;

    /**
     * Returns the ID of the activity (a 32 character identifier used by Timetabling).
     * 
     * @return the activity ID. This is a primary key copied from the Timetabling
     * system.
     */
    @Id
    @Column(name="tt_activity_id", nullable=false, length=32)
    public String getActivityId() {
        return activityId;
    }

    /**
     * Returns the name of the activity.
     * 
     * @return the name of the activity.
     */
    @Column(name="tt_activity_name", nullable=true, length=255)
    public String getActivityName() {
        return activityName;
    }

    /**
     * Returns the description of the group. This is generated from the activity
     * data name, template, type and module as part of the synchronisation process.
     * 
     * @return the description of the group.
     */
    @Column(name="description", nullable=true, length=400)
    public String getDescription() {
        return description;
    }

    /**
     * Returns the ID of the group in Learn that this activity maps to,
     * where applicable.
     * 
     * @return the ID of the group in Learn that this activity maps to,
     * or null if no mapping has been established yet.
     */
    @Column(name="learn_group_id", nullable=true, length=80)
    public String getLearnGroupId() {
        return learnGroupId;
    }

    /**
     * Returns the name of the group in Learn, when it's first created. This
     * is generated as part of the synchronisation process.
     * 
     * @return the Learn group name.
     */
    @Column(name="learn_group_name", nullable=true, length=80)
    public String getLearnGroupName() {
        return learnGroupName;
    }

    /**
     * @return the module this activity belongs to.
     */
    @ManyToOne
    @JoinColumn(name="tt_module_id")
    public Module getModule() {
        return module;
    }

    /**
     * @param activityId the ID of this activity.
     */
    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    /**
     * @param activityName the name of this activity.
     */
    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    /**
     * @param description the description for this activity's group.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param learnGroupName the name of this activity's group in Learn.
     */
    public void setLearnGroupName(String learnGroupName) {
        this.learnGroupName = learnGroupName;
    }

    /**
     * @param module the module this activity belongs to.
     */
    public void setModule(Module module) {
        this.module = module;
    }

    /**
     * @param learnGroupId the ID of the group in Learn that this activity
     * relates to.
     */
    public void setLearnGroupId(String learnGroupId) {
        this.learnGroupId = learnGroupId;
    }
}
