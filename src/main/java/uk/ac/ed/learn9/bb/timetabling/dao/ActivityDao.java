package uk.ac.ed.learn9.bb.timetabling.dao;

import java.util.List;
import uk.ac.ed.learn9.bb.timetabling.data.cache.Activity;

public interface ActivityDao {
    public Activity getById(final int activityId);
    public List<Activity> getAll();
}
