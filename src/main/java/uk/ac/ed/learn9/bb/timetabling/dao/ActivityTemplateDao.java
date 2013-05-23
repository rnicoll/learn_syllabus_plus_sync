package uk.ac.ed.learn9.bb.timetabling.dao;

import java.util.List;
import uk.ac.ed.learn9.bb.timetabling.data.ActivityTemplate;

/**
 * Data access object for loading Timetabling activity templates from the staging
 * database.
 */
public interface ActivityTemplateDao {
    /**
     * Retrieves an activity template by its ID.
     * 
     * @param templateId a timetabling ID (as in a 32 character unique identifier).
     * @return the activity template.
     */
    public ActivityTemplate getById(final int templateId);
    
    /**
     * Retrieves all activity templates in the staging database.
     * @return a list of activity templates.
     */
    public List<ActivityTemplate> getAll();
}
