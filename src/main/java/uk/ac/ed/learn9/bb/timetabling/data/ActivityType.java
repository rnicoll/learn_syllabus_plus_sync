package uk.ac.ed.learn9.bb.timetabling.data;



import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="activity_type")
public class ActivityType extends Object {
    private String typeId;
    private String typeName;

    /**
     * @return the ID of this activity type.
     */
    @Id
    @Column(name="tt_type_id", length=32, nullable=false)
    public String getTypeId() {
        return typeId;
    }

    /**
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
