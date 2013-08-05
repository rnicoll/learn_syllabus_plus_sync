package uk.ac.ed.learn9.bb.timetabling.dao;

import java.util.List;
import uk.ac.ed.learn9.bb.timetabling.data.ModuleCourse;

/**
 * Data access object for loading module-course relationships from the staging
 * database.
 */
public interface ModuleCourseDao {
    /**
     * Retrieves an module-course by its ID.
     * 
     * @param moduleCourseId a unique module-course relationship ID.
     */
    public ModuleCourse getById(final int moduleCourseId);
    
    public List<ModuleCourse> getByTimetablingId(final String moduleCourseId);
}
