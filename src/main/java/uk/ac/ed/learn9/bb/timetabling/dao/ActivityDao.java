package uk.ac.ed.learn9.bb.timetabling.dao;

import java.util.List;

import blackboard.persist.Id;
import uk.ac.ed.learn9.bb.timetabling.data.Activity;

/**
 * Data access object for loading Timetabling activities from the staging
 * database.
 */
public interface ActivityDao {
    /**
     * Retrieves an activity by its ID.
     * 
     * @param activityId a timetabling ID (as in a 32 character unique identifier).
     * @return the activity.
     */
    public Activity getById(final String activityId);
    
    /**
     * Retrieves all activities in the staging database.
     * @return a list of activities.
     */
    public List<Activity> getAll();

    /**
     * Retrieves activities by the ID of the Learn course.
     * 
     * @param id a Learn course ID.
     * @return a list of activities matching the given Learn course.
     */
    public List<Activity> getByCourseLearnId(final Id id);
    
    /**
     * Refreshes an activity from the database.
     * 
     * @param activity the activity to be refreshed
     */
    public void refresh(final Activity activity);
}
