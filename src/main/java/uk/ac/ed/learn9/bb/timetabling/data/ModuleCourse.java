package uk.ac.ed.learn9.bb.timetabling.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

/**
 * Relationship between a module in Timetabling, and a course in Learn. Required
 * as this can be a one to many relationship.
 */
@Entity
@Table(name="module_course")
public class ModuleCourse {
    private int id;
    private Module module;
    private String learnCourseId;
    private String learnCourseCode;
    private boolean mergedCourse;
    private Boolean learnCourseAvaiable;

    /**
     * Gets the ID of this relationship.
     * 
     * @return the ID of the module-course relationship.
     */
    @Id
    @Column(name="module_course_id", nullable=false)
    public int getId() {
        return this.id;
    }
    
    /**
     * Get the module this relationship relates to.
     * 
     * @return the module this relationship relates to.
     */
    @ManyToOne
    @JoinColumn(name="tt_module_id", nullable=false)
    public Module getModule() {
        return this.module;
    }

    /**
     * Get whether this course is available in Learn.
     * 
     * @return whether this course is available in Learn. May be null if
     * unknown, or no matching course is present in Learn.
     */
    @Column(name="learn_course_available", nullable=true)
    @Type(type="yes_no")
    public Boolean getLearnCourseAvaiable() {
        return learnCourseAvaiable;
    }

    /**
     * Get the ID of the course in Learn, where such a course exists.
     * 
     * @return the ID of the course in Learn, or null if no matching course
     * has been identified yet.
     */
    @Column(name="learn_course_id", nullable=true, length=80)
    public String getLearnCourseId() {
        return learnCourseId;
    }

    /**
     * Gets a course code for this module in Learn, for example
     * "HIAR100792012-3SS1SEM1".
     * 
     * @return the Learn course code.
     */
    @Column(name="learn_course_code", nullable=false, length=40, updatable=false)
    public String getLearnCourseCode() {
        return learnCourseCode;
    }

    /**
     * Determine whether this course represents the module before merging from
     * the BBL Feeds database (false) or after (true).
     * 
     * @return whether this course represents the module before merging from
     * the BBL Feeds database (false) or after (true).
     */
    @Column(name="merged_course", nullable=false)
    @Type(type="yes_no")
    public boolean isMergedCourse() {
        return mergedCourse;
    }

    /**
     * Set the ID of this relationship.
     * 
     * @param id the ID to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Set whether this course is available in Learn, where applicable.
     * 
     * @param learnCourseAvaiable whether this course is available in Learn.
     */
    public void setLearnCourseAvaiable(Boolean learnCourseAvaiable) {
        this.learnCourseAvaiable = learnCourseAvaiable;
    }

    /**
     * Set the ID of the matching course in Learn, where applicable.
     * 
     * @param learnCourseId the ID of the matching course in Learn.
     */
    public void setLearnCourseId(final String learnCourseId) {
        this.learnCourseId = learnCourseId;
    }

    /**
     * @param learnCourseCode the learnCourseCode to set
     */
    public void setLearnCourseCode(String learnCourseCode) {
        this.learnCourseCode = learnCourseCode;
    }

    /**
     * @param mergedCourse the mergedCourse to set
     */
    public void setMergedCourse(boolean mergedCourse) {
        this.mergedCourse = mergedCourse;
    }

    /**
     * @param module the module to set
     */
    public void setModule(Module module) {
        this.module = module;
    }
    
}
