package uk.ac.ed.learn9.bb.timetabling.data;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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
    private Module module;
    private String learnGroupId;
    private String description;

    /**
     * @return the activity ID. This is a primary key copied from the Timetabling
     * system.
     */
    @Id
    @Column(name="tt_activity_id", nullable=false, length=32)
    public String getActivityId() {
        return activityId;
    }

    /**
     * @return the module this activity belongs to.
     */
    @OneToMany
    @JoinColumn(name="tt_module_id")
    public Module getModule() {
        return module;
    }

    /**
     * @return the learnGroupId
     */
    @Column(name="learn_group_id", nullable=true, length=80)
    public String getLearnGroupId() {
        return learnGroupId;
    }

    /**
     * @return the description
     */
    @Column(name="description", nullable=true, length=400)
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param activityId the activityId to set
     */
    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    /**
     * @param module the module to set
     */
    public void setModule(Module module) {
        this.module = module;
    }

    /**
     * @param learnGroupId the learnGroupId to set
     */
    public void setLearnGroupId(String learnGroupId) {
        this.learnGroupId = learnGroupId;
    }
}
