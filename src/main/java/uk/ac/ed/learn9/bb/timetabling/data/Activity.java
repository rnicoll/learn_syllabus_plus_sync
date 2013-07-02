package uk.ac.ed.learn9.bb.timetabling.data;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
    private String learnGroupName;
    private String description;
    private List<ActivityGroup> groups;

    /**
     * Gets the ID of the activity (a 32 character identifier used by Timetabling),
     * for example "01A98191A0D874DA715A4EB13A90EC5D".
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
     * Gets the name of the activity, for example "Chemical Medicine Level 10/2".
     * 
     * @return the name of the activity.
     */
    @Column(name="tt_activity_name", nullable=true, length=255)
    public String getActivityName() {
        return activityName;
    }

    /**
     * Gets the description of the group. This is generated from the activity
     * data name, template, type and module as part of the synchronisation process.
     * 
     * @return the description of the group.
     */
    @Column(name="description", nullable=true, length=400)
    public String getDescription() {
        return description;
    }

    /**
     * Get the Learn groups that relate to this activity.
     * 
     * @return the Learn groups that relate to this activity.
     */
    @OneToMany(mappedBy="activity")
    public List<ActivityGroup> getGroups() {
        return groups;
    }

    /**
     * Gets the name of the group in Learn, when it's first created. This
     * is generated as part of the synchronisation process.
     * 
     * @return the Learn group name.
     */
    @Column(name="learn_group_name", nullable=true, length=80)
    public String getLearnGroupName() {
        return learnGroupName;
    }

    /**
     * Gets the module in Timetabling that this activity belongs to.
     * 
     * @return the module this activity belongs to, may be null where not
     * applicable.
     */
    @ManyToOne
    @JoinColumn(name="tt_module_id", nullable=true)
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
     * @param groups the groups to set
     */
    public void setGroups(List<ActivityGroup> groups) {
        this.groups = groups;
    }
}
