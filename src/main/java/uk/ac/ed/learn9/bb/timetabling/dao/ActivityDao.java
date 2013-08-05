package uk.ac.ed.learn9.bb.timetabling.dao;

import java.util.List;

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
     * Refreshes an activity from the database.
     * 
     * @param activity the activity to be refreshed
     */
    public void refresh(final Activity activity);
}
