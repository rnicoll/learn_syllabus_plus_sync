package uk.ac.ed.learn9.bb.timetabling.dao;

import java.util.List;

import blackboard.data.course.Course;
import blackboard.persist.Id;
import uk.ac.ed.learn9.bb.timetabling.data.EnrolmentChange;
import uk.ac.ed.learn9.bb.timetabling.data.EnrolmentChangePart;

/**
 * Data access object for loading student/group enrolment changes from the staging
 * database.
 */
public interface EnrolmentChangePartDao {
    /**
     * Retrieves a change by its ID.
     * 
     * @param partId a change ID.
     * @return the change.
     */
    public EnrolmentChangePart getById(final int partId);
    
    /**
     * Retrieves all changes for a single Learn course.
     * 
     * @param course the course to retrieve changes for.
     * @return a list of changes.
     */
    public List<EnrolmentChangePart> getByCourse(final Course course);
    
    /**
     * Retrieves all changes for a single Learn course.
     * 
     * @param courseId the ID of the course to retrieve changes for.
     * @return a list of changes.
     */
    public List<EnrolmentChangePart> getByCourse(final Id courseId);
    
    /**
     * Get all change parts.
     * 
     * @return a list of change parts.
     */
    public List<EnrolmentChangePart> getAll();
}
