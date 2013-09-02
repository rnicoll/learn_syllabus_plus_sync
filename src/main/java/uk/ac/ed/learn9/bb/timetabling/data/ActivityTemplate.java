package uk.ac.ed.learn9.bb.timetabling.data;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Represents an activity template in timetabling. Activity templates generally
 * are used to collect a set of related activities (for example all tutorials
 * within a set), and therefore may reflect a group set in Learn.
 */
@Entity
@Table(name="activity_template")
public class ActivityTemplate extends Object implements Serializable {
    private String templateId;
    private String templateName;
    private String userText5;
    private String learnGroupSetId;
    private List<Activity> activities;
    
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof ActivityTemplate)) {
            return false;
        }
        
        final ActivityTemplate other = (ActivityTemplate)o;
        
        return other.getTemplateId().equals(this.getTemplateId());
    }
    
    @Override
    public int hashCode() {
        return this.getTemplateId().hashCode();
    }
    
    @Override
    public String toString() {
        return this.getTemplateName();
    }

    /**
     * Returns the ID of this template in Learn (a 32 character identifier).
     * 
     * @return the activity template ID. This is a primary key copied from the Timetabling
     * system.
     */
    @Id
    @Column(name="tt_template_id", nullable=false, length=32)
    public String getTemplateId() {
        return templateId;
    }

    /**
     * Get the activities based on this template.
     * 
     * @return the activities based on this template.
     */
    @OneToMany(mappedBy="template", fetch=FetchType.LAZY)
    public List<Activity> getActivities() {
        return this.activities;
    }

    /**
     * Returns the name of the activity template.
     * 
     * @return the name of the activity template.
     */
    @Column(name="tt_template_name", nullable=true, length=255)
    public String getTemplateName() {
        return templateName;
    }

    /**
     * Returns the value of the "USER_TEXT_5" field from the reporting database.
     * In this case this is used to indicate templates that are not intended to
     * be synchronised into Learn, by setting the value to "Not for VLE".
     * 
     * @return the "USER_TEXT_5" field from the reporting database.
     */
    @Column(name="tt_user_text_5", nullable=true, length=255)
    public String getUserText5() {
        return userText5;
    }

    /**
     * The ID of the group set that activities based on this template, should
     * be placed into, in Learn. This is currently unused, however is reserved
     * for future expansion.
     * 
     * @return the ID of the group set this template reflects, in Learn.
     */
    @Column(name="learn_group_set_id", nullable=true, length=255)
    public String getLearnGroupSetId() {
        return learnGroupSetId;
    }
    
    public void setActivities(final List<Activity> newActivities) {
        this.activities = newActivities;
    }

    /**
     * Sets the ID in Learn for the group set that groups created for activities
     * based on this template, should be placed into.
     * 
     * @param learnGroupSetId the ID in Learn for the group set this template
     * relates to.
     */
    public void setLearnGroupSetId(String learnGroupSetId) {
        this.learnGroupSetId = learnGroupSetId;
    }

    /**
     * @param activityTemplateId the ID to set for this activity template.
     */
    public void setTemplateId(String activityTemplateId) {
        this.templateId = activityTemplateId;
    }

    /**
     * @param activityTemplateName the name to set for this activity template.
     */
    public void setTemplateName(String activityTemplateName) {
        this.templateName = activityTemplateName;
    }

    /**
     * Sets the value of the USER_TEXT_5 field as cached from Timetabling.
     * @param userText5 
     */
    public void setUserText5(String userText5) {
        this.userText5 = userText5;
    }
}
