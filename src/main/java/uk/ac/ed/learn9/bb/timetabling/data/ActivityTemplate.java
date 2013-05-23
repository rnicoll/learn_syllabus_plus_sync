package uk.ac.ed.learn9.bb.timetabling.data;



import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Represents an activity template in timetabling.
 */
@Entity
@Table(name="activity_template")
public class ActivityTemplate extends Object implements Serializable {
    private String templateId;
    private String templateName;
    private String userText5;
    private String learnGroupSetId;

    /**
     * @return the activityTemplate ID. This is a primary key copied from the Timetabling
     * system.
     */
    @Id
    @Column(name="tt_template_id", nullable=false, length=32)
    public String getTemplateId() {
        return templateId;
    }

    /**
     * Returns the name of the activityTemplate.
     * 
     * @return the name of the activityTemplate.
     */
    @Column(name="tt_template_name", nullable=true, length=255)
    public String getTemplateName() {
        return templateName;
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
     * be placed into, in Learn.
     * 
     * @return the ID of the group set this template reflects, in Learn.
     */
    @Column(name="learn_group_set_id", nullable=true, length=255)
    public String getLearnGroupSetId() {
        return learnGroupSetId;
    }

    public void setUserText5(String userText5) {
        this.userText5 = userText5;
    }

    public void setLearnGroupSetId(String learnGroupSetId) {
        this.learnGroupSetId = learnGroupSetId;
    }
}
