package uk.ac.ed.learn9.bb.timetabling.dao;

import java.util.List;

import uk.ac.ed.learn9.bb.timetabling.data.Activity;
import uk.ac.ed.learn9.bb.timetabling.data.ActivityGroup;

/**
 * Data access object for loading Timetabling activities from the staging
 * database.
 */
public interface ActivityGroupDao {
    /**
     * Retrieves activity groups by activity.
     * 
     * @param activity the activity to retrieve groups for.
     * @return a list of activity groups.
     */
    public List<ActivityGroup> getByActivity(final Activity activity);

    public List<ActivityGroup> getAll();
}
