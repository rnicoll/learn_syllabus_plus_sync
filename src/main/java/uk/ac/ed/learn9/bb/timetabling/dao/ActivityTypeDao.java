package uk.ac.ed.learn9.bb.timetabling.dao;

import java.util.List;
import uk.ac.ed.learn9.bb.timetabling.data.cache.ActivityType;

public interface ActivityTypeDao {
    public ActivityType getById(final int typeId);
    public List<ActivityType> getAll();
}
