package uk.ac.ed.learn9.bb.timetabling.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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
     * @param typeId the new ID of this activity type.
     */
    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    /**
     * @param typeName the new name of this activity type.
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
