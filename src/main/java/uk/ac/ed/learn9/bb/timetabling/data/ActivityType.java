package uk.ac.ed.learn9.bb.timetabling.data;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * An activity type provides a human-readable short description of the purpose
 * of an activity, for example "Tutorial", "Lab", "Lecture", etc.
 */
@Entity
@Table(name="activity_type")
public class ActivityType extends Object {
    private String typeId;
    private String typeName;
    private List<Activity> activities;
    
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof ActivityType)) {
            return false;
        }
        
        final ActivityType other = (ActivityType)o;
        
        return other.getTypeId().equals(this.getTypeId());
    }
    
    @Override
    public int hashCode() {
        return this.getTypeId().hashCode();
    }
    
    @Override
    public String toString() {
        return this.getTypeName();
    }

    /**
     * Gets the ID of this activity type from Timetabling (a 32 character identifier).
     * 
     * @return the ID of this activity type. This is a primary key copied from the Timetabling
     * system.
     */
    @Id
    @Column(name="tt_type_id", length=32, nullable=false)
    public String getTypeId() {
        return typeId;
    }

    /**
     * Get the activities of this activity type.
     * 
     * @return a list of the activities of this activity type.
     */
    @OneToMany(mappedBy="type", fetch=FetchType.LAZY)
    public List<Activity> getActivities() {
        return activities;
    }

    /**
     * Returns the human readable name of this activity type, for example "Lecture",
     * "Tutorial", etc.
     * 
     * @return the name of this activity type.
     */
    @Column(name="tt_type_name", length=255, nullable=true)
    public String getTypeName() {
        return typeName;
    }

    /**
     * Get the activities of this activity type.
     * 
     * @param newActivities list of the activities of this activity type.
     */
    public void setActivities(final List<Activity> newActivities) {
        this.activities = newActivities;
    }

    /**
     * Set the ID of this activity type.
     * 
     * @param typeId the new ID of this activity type.
     */
    public void setTypeId(final String typeId) {
        this.typeId = typeId;
    }

    /**
     * Set the name of this activity type.
     * 
     * @param typeName the new name of this activity type.
     */
    public void setTypeName(final String typeName) {
        this.typeName = typeName;
    }
}
