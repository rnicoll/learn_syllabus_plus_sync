package uk.ac.ed.learn9.bb.timetabling.dao;

import java.util.List;
import uk.ac.ed.learn9.bb.timetabling.data.cache.ActivityTemplate;

public interface ActivityTemplateDao {
    public ActivityTemplate getById(final int templateId);
    public List<ActivityTemplate> getAll();
}
