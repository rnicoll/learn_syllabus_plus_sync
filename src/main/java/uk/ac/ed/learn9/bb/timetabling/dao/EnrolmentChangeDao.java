package uk.ac.ed.learn9.bb.timetabling.dao;

import java.util.List;
import uk.ac.ed.learn9.bb.timetabling.data.EnrolmentChange;

/**
 * Data access object for loading student/group enrolment changes from the staging
 * database.
 */
public interface EnrolmentChangeDao {
    /**
     * Retrieves a change by its ID.
     * 
     * @param changeId a change ID.
     * @return the change.
     */
    public EnrolmentChange getById(final int changeId);
    
    /**
     * Get all changes.
     * 
     * @return a list of changes.
     */
    public List<EnrolmentChange> getAll();
}
