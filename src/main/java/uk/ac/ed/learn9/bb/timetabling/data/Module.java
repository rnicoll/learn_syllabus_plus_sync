package uk.ac.ed.learn9.bb.timetabling.data;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

/**
 * Represents an module in timetabling, which can be mapped to a course in Learn.
 */
@Entity
@Table(name="module")
public class Module extends Object implements Serializable {
    private String timetablingModueId;
    private String timetablingCourseCode;
    private String timetablingModuleName;
    private String timetablingAcademicYear;
    private String cacheSemesterCode;
    private String cacheOccurrenceCode;
    private String cacheCourseCode;
    private String mergeCourseCode;
    private String learnAcademicYear;
    private String learnCourseId;
    private String learnCourseCode;
    private Boolean webctActive;

    /**
     * @return the ID of the module in Timetabling.
     */
    @Id
    @Column(name="tt_module_id", nullable=false, length=32)
    public String getModuleId() {
        return timetablingModueId;
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
     * Get the course code as stored in timetabling, for example
     * "HIAR10079_SS1_SEM1".
     * 
     * @return the course code as stored in timetabling.
     */
    @Column(name="tt_course_code", nullable=true, length=20)
    public String getTimetablingCourseCode() {
        return timetablingCourseCode;
    }

    /**
     * Gets the name of the module in Timetabling, for example 
     * "Fractures: The Origin, Development and Influence of Cubist Painting".
     * 
     * @return the name of the module in Timetabling.
     */
    @Column(name="tt_module_name", nullable=true, length=255)
    public String getTimetablingModuleName() {
        return timetablingModuleName;
    }

    /**
     * Gets the academic year recorded against the module in Timetabling,
     * for example "2012/3".
     * 
     * @return the academic year recorded against the module in Timetabling.
     */
    @Column(name="tt_academic_year", nullable=true, length=12)
    public String getTimetablingAcademicYear() {
        return timetablingAcademicYear;
    }

    /**
     * Gets the semester code derived from the timetabling course code.
     * 
     * @return the semester code derived from the timetabling course code, or
     * null if no semester code could be determined.
     */
    @Column(name="cache_semester_code", nullable=true, length=6, updatable=false)
    public String getCacheSemesterCode() {
        return cacheSemesterCode;
    }

    /**
     * Gets the occurrence code derived from the timetabling course code.
     * 
     * @return the occurrence code derived from the timetabling course code, or
     * null if no occurrence code could be determined.
     */
    @Column(name="cache_occurrence_code", nullable=true, length=6, updatable=false)
    public String getCacheOccurrenceCode() {
        return cacheOccurrenceCode;
    }

    /**
     * @return the cacheCourseCode
     */
    @Column(name="cache_course_code", nullable=true, length=12, updatable=false)
    public String getCacheCourseCode() {
        return cacheCourseCode;
    }

    /**
     * Gets the Learn course code of the course that this module is merged
     * into.
     * 
     * @return the Learn course code of the course that this module is merged
     * into, or null if no merging is to be done (the normal case).
     */
    @Column(name="merge_course_code", nullable=true, length=40)
    public String getMergeCourseCode() {
        return mergeCourseCode;
    }

    /**
     * Gets the academic year code as used to generate the Learn course
     * code, as in "2012-3".
     * 
     * @return the academic year code as used to generate the Learn course
     * code
     */
    @Column(name="learn_academic_year", nullable=true, length=6, updatable=false)
    public String getLearnAcademicYear() {
        return learnAcademicYear;
    }

    /**
     * Gets the course code for this module in Learn, for example
     * "HIAR100792012-3SS1SEM1".
     * 
     * @return the course code for this module in Learn.
     */
    @Column(name="learn_course_code", nullable=true, length=40, updatable=false)
    public String getLearnCourseCode() {
        return learnCourseCode;
    }

    /**
     * Gets whether this course is synchronised from EUCLID to Learn (the
     * name of the field reflects the name of the field in EUCLID, which is
     * now out of date).
     * 
     * @return whether this course is synchronised from EUCLID to Learn.
     */
    @Column(name="webct_active", nullable=true)
    @Type(type="yes_no")
    public Boolean getWebctActive() {
        return webctActive;
    }

    /**
     * @param timetablingCourseCode the timetablingCourseCode to set
     */
    public void setTimetablingCourseCode(String timetablingCourseCode) {
        this.timetablingCourseCode = timetablingCourseCode;
    }

    /**
     * @param timetablingModuleName the timetablingModuleName to set
     */
    public void setTimetablingModuleName(String timetablingModuleName) {
        this.timetablingModuleName = timetablingModuleName;
    }

    /**
     * @param timetablingAcademicYear the timetablingAcademicYear to set
     */
    public void setTimetablingAcademicYear(String timetablingAcademicYear) {
        this.timetablingAcademicYear = timetablingAcademicYear;
    }

    /**
     * @param cacheSemesterCode the cacheSemesterCode to set
     */
    public void setCacheSemesterCode(String cacheSemesterCode) {
        this.cacheSemesterCode = cacheSemesterCode;
    }

    /**
     * @param cacheOccurrenceCode the cacheOccurrenceCode to set
     */
    public void setCacheOccurrenceCode(String cacheOccurrenceCode) {
        this.cacheOccurrenceCode = cacheOccurrenceCode;
    }

    /**
     * @param cacheCourseCode the cacheCourseCode to set
     */
    public void setCacheCourseCode(String cacheCourseCode) {
        this.cacheCourseCode = cacheCourseCode;
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
        this.timetablingModueId = moduleId;
    }

    /**
     * @param mergeCourseCode the mergeCourseCode to set
     */
    public void setMergeCourseCode(String mergeCourseCode) {
        this.mergeCourseCode = mergeCourseCode;
    }

    /**
     * @param learnAcademicYear the learnAcademicYear to set
     */
    public void setLearnAcademicYear(String learnAcademicYear) {
        this.learnAcademicYear = learnAcademicYear;
    }

    /**
     * @param learnCourseCode the learnCourseCode to set
     */
    public void setLearnCourseCode(String learnCourseCode) {
        this.learnCourseCode = learnCourseCode;
    }

    /**
     * @param webctActive the webctActive to set
     */
    public void setWebctActive(final Boolean webctActive) {
        this.webctActive = webctActive;
    }
}
