package uk.ac.ed.learn9.bb.timetabling.data;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

/**
 * Represents an activity in timetabling, which in some cases may be mapped to a
 * group in Learn.
 */
@Entity
@Table(name="activity")
@SecondaryTable(name="variantjtaacts", 
        pkJoinColumns=@PrimaryKeyJoinColumn(name="tt_activity_id"))
public class Activity extends Object implements Serializable {
    private String activityId;
    private String activityName;
    private Module module;
    private String learnGroupName;
    private String description;
    private Boolean isJtaParent;
    private Boolean isJtaChild;
    private Boolean isVariantParent;
    private Boolean isVariantChild;
    private int schedulingMethod;
    private List<ActivityGroup> groups;
    private ActivityTemplate template;
    private ActivityType type;
    
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Activity)) {
            return false;
        }
        
        final Activity other = (Activity)o;
        
        return other.getActivityId().equals(this.getActivityId());
    }
    
    @Override
    public int hashCode() {
        return this.getActivityId().hashCode();
    }
    
    @Override
    public String toString() {
        return this.getActivityName();
    }

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
     * @return the isJtaChild
     */
    @Column(table="variantjtaacts", name="tt_is_jta_child")
    public Boolean getIsJtaChild() {
        return isJtaChild;
    }

    /**
     * @return the isJtaParent
     */
    @Column(table="variantjtaacts", name="tt_is_jta_parent")
    public Boolean getIsJtaParent() {
        return isJtaParent;
    }

    /**
     * @return the isVariantChild
     */
    @Column(table="variantjtaacts", name="tt_is_variant_child")
    public Boolean getIsVariantChild() {
        return isVariantChild;
    }

    /**
     * @return the isVariantParent
     */
    @Column(table="variantjtaacts", name="tt_is_variant_parent")
    public Boolean getIsVariantParent() {
        return isVariantParent;
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
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="tt_module_id", nullable=true)
    public Module getModule() {
        return module;
    }

    /**
     * @return the schedulingMethod
     */
    @Column(name="TT_SCHEDULING_METHOD", nullable=false)
    public int getSchedulingMethod() {
        return schedulingMethod;
    }

    /**
     * @return the template
     */
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="tt_template_id", nullable=true)
    public ActivityTemplate getTemplate() {
        return template;
    }

    /**
     * @return the type
     */
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="tt_type_id", nullable=true)
    public ActivityType getType() {
        return type;
    }

    /**
     * @param activityId the ID of this activity.
     */
    public void setActivityId(final String activityId) {
        this.activityId = activityId;
    }

    /**
     * @param activityName the name of this activity.
     */
    public void setActivityName(final String activityName) {
        this.activityName = activityName;
    }

    /**
     * @param description the description for this activity's group.
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * @param groups the groups to set
     */
    public void setGroups(final List<ActivityGroup> newGroups) {
        this.groups = newGroups;
    }

    /**
     * @param learnGroupName the name of this activity's group in Learn.
     */
    public void setLearnGroupName(final String learnGroupName) {
        this.learnGroupName = learnGroupName;
    }

    /**
     * @param module the module this activity belongs to.
     */
    public void setModule(final Module module) {
        this.module = module;
    }

    /**
     * @param template the template to set
     */
    public void setTemplate(final ActivityTemplate template) {
        this.template = template;
    }

    /**
     * @param type the type to set
     */
    public void setType(final ActivityType type) {
        this.type = type;
    }

    /**
     * @param isJtaParent the isJtaParent to set
     */
    public void setIsJtaParent(Boolean isJtaParent) {
        this.isJtaParent = isJtaParent;
    }

    /**
     * @param isJtaChild the isJtaChild to set
     */
    public void setIsJtaChild(Boolean isJtaChild) {
        this.isJtaChild = isJtaChild;
    }

    /**
     * @param isVariantParent the isVariantParent to set
     */
    public void setIsVariantParent(Boolean isVariantParent) {
        this.isVariantParent = isVariantParent;
    }

    /**
     * @param isVariantChild the isVariantChild to set
     */
    public void setIsVariantChild(Boolean isVariantChild) {
        this.isVariantChild = isVariantChild;
    }
    
    /**
     * @param newSchedulingMethod the schedulingMethod to set
     */
    public void setSchedulingMethod(int newSchedulingMethod) {
        this.schedulingMethod = newSchedulingMethod;
    }
}
