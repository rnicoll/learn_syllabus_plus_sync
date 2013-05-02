package uk.ac.ed.learn9.bb.timetabling.data;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Represents an module in timetabling, which can be mapped to a course in Learn.
 */
@Entity
@Table(name="module")
public class Module extends Object implements Serializable {
    private String moduleId;
    private String learnCourseId;    

    /**
     * @return the ID of the module in Timetabling.
     */
    @Id
    @Column(name="tt_module_id", nullable=false, length=32)
    public String getModuleId() {
        return moduleId;
    }

    /**
     * @return the ID of the course in Learn.
     */
    @Column(name="learn_course_id", nullable=true, length=80)
    public String getLearnCourseId() {
        return learnCourseId;
    }

    /**
     * @param learnCourseId the ID of the course in Learn to set.
     */
    public void setLearnCourseId(String learnCourseId) {
        this.learnCourseId = learnCourseId;
    }

    /**
     * @param moduleId the moduleId to set
     */
    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
}
