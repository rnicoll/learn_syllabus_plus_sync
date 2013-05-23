package uk.ac.ed.learn9.bb.timetabling.dao;

import java.util.List;
import uk.ac.ed.learn9.bb.timetabling.data.ActivityType;

/**
 * Data access object for loading Timetabling activity types from the staging
 * database.
 */
public interface ActivityTypeDao {
    /**
     * Retrieves an activity type by its ID.
     * 
     * @param typeId a timetabling ID (as in a 32 character unique identifier).
     * @return the activity type.
     */
    public ActivityType getById(final int typeId);
    
    /**
     * Retrieves all activity types in the staging database.
     * @return a list of activity types.
     */
    public List<ActivityType> getAll();
}
