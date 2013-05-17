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
    private boolean webctActive;

    /**
     * @return the ID of the module in Timetabling.
     */
    @Id
    @Column(name="tt_module_id", nullable=false, length=32)
    public String getModuleId() {
        return timetablingModueId;
    }

    /**
     * @return the ID of the course in Learn.
     */
    @Column(name="learn_course_id", nullable=true, length=80)
    public String getLearnCourseId() {
        return learnCourseId;
    }

    /**
     * @return the timetablingCourseCode
     */
    @Column(name="tt_course_code", nullable=true, length=20)
    public String getTimetablingCourseCode() {
        return timetablingCourseCode;
    }

    /**
     * @return the timetablingModuleName
     */
    @Column(name="tt_module_name", nullable=true, length=255)
    public String getTimetablingModuleName() {
        return timetablingModuleName;
    }

    /**
     * @return the timetablingAcademicYear
     */
    @Column(name="tt_academic_year", nullable=true, length=12)
    public String getTimetablingAcademicYear() {
        return timetablingAcademicYear;
    }

    /**
     * @return the cacheSemesterCode
     */
    @Column(name="cache_semester_code", nullable=true, length=6, updatable=false)
    public String getCacheSemesterCode() {
        return cacheSemesterCode;
    }

    /**
     * @return the cacheOccurrenceCode
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
     * @return the mergeCourseCode
     */
    @Column(name="merge_course_code", nullable=true, length=40)
    public String getMergeCourseCode() {
        return mergeCourseCode;
    }

    /**
     * @return the learnAcademicYear
     */
    @Column(name="learn_academic_year", nullable=true, length=6, updatable=false)
    public String getLearnAcademicYear() {
        return learnAcademicYear;
    }

    /**
     * @return the learnCourseCode
     */
    @Column(name="learn_course_code", nullable=true, length=40, updatable=false)
    public String getLearnCourseCode() {
        return learnCourseCode;
    }

    /**
     * @return the webctActive
     */
    @Column(name="webct_active", nullable=true)
    @Type(type="yes_no")
    public boolean getWebctActive() {
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
    public void setWebctActive(final boolean webctActive) {
        this.webctActive = webctActive;
    }
}
